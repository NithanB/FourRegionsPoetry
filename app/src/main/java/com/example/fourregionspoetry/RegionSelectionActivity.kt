package com.example.fourregionspoetry

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject

data class FavoritePoem(
    val id: Long,
    val poem: String,
    val region: String,
    val keywords: List<String>,
    val timestamp: String
)

class FavoritePoemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val poemTextView: TextView = itemView.findViewById(R.id.tvFavoritePoemText)
    val regionTextView: TextView = itemView.findViewById(R.id.tvFavoritePoemRegion)
    val deleteButton: ImageButton = itemView.findViewById(R.id.btnDeleteFavorite)
    val copyButton: ImageButton = itemView.findViewById(R.id.btnCopyFavorite)
}

class FavoritesAdapter(
    private val context: Context, // Added context for Toast and ClipboardManager
    private val favoritePoems: MutableList<FavoritePoem>, // Changed to MutableList
    private val onFavoritesChanged: () -> Unit // Callback to update UI in activity
) : RecyclerView.Adapter<FavoritePoemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritePoemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_poem, parent, false)
        return FavoritePoemViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoritePoemViewHolder, position: Int) {
        val poem = favoritePoems[position]
        holder.poemTextView.text = poem.poem
        holder.regionTextView.text = "Region: ${poem.region}"

        holder.deleteButton.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                //val poemToRemove = favoritePoems[currentPosition] // Not strictly needed here
                favoritePoems.removeAt(currentPosition)
                notifyItemRemoved(currentPosition)
                // To prevent issues with positions after removal, especially if animations are running
                notifyItemRangeChanged(currentPosition, favoritePoems.size - currentPosition)
                saveFavoritesToSharedPreferences()
                onFavoritesChanged() // Notify activity to update "No favorites" view
                Toast.makeText(context, "Poem removed from favorites", Toast.LENGTH_SHORT).show()
            }
        }

        holder.copyButton.setOnClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("poem", poem.poem)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Poem copied to clipboard!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = favoritePoems.size

    private fun saveFavoritesToSharedPreferences() {
        val sharedPref = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        favoritePoems.forEach { favPoem ->
            val jsonObject = JSONObject().apply {
                put("id", favPoem.id)
                put("poem", favPoem.poem)
                put("region", favPoem.region)
                put("keywords", JSONArray(favPoem.keywords))
                put("timestamp", favPoem.timestamp)
            }
            jsonArray.put(jsonObject)
        }
        with(sharedPref.edit()) {
            putString("favorite_poems", jsonArray.toString())
            apply()
        }
    }
}


class RegionSelectionActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnNorth: Button
    private lateinit var btnSouth: Button
    private lateinit var btnNortheast: Button
    private lateinit var btnCentral: Button

    private lateinit var recyclerViewFavorites: RecyclerView
    private lateinit var textViewNoFavorites: TextView
    private lateinit var favoritesAdapter: FavoritesAdapter
    private var favoritePoemsList = mutableListOf<FavoritePoem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_region_selection)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        initViews()
        setupListeners()
        setupFavoritesRecyclerView()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnNorth = findViewById(R.id.btnNorth)
        btnSouth = findViewById(R.id.btnSouth)
        btnNortheast = findViewById(R.id.btnNortheast)
        btnCentral = findViewById(R.id.btnCentral)

        recyclerViewFavorites = findViewById(R.id.recyclerViewFavorites)
        textViewNoFavorites = findViewById(R.id.textViewNoFavorites)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }
        btnNorth.setOnClickListener { navigateToRegionInput("north") }
        btnSouth.setOnClickListener { navigateToRegionInput("south") }
        btnNortheast.setOnClickListener { navigateToRegionInput("northeast") }
        btnCentral.setOnClickListener { navigateToRegionInput("central") }
    }

    private fun setupFavoritesRecyclerView() {
        recyclerViewFavorites.layoutManager = LinearLayoutManager(this)
        // Pass context, the list, and a lambda to update the "no favorites" view
        favoritesAdapter = FavoritesAdapter(this, favoritePoemsList) {
            updateNoFavoritesView()
        }
        recyclerViewFavorites.adapter = favoritesAdapter
    }

    private fun loadFavoritePoems() {
        favoritePoemsList.clear()
        val sharedPref = getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val poemsJsonString = sharedPref.getString("favorite_poems", "[]") ?: "[]"

        try {
            val jsonArray = JSONArray(poemsJsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val keywordsJsonArray = jsonObject.optJSONArray("keywords") ?: JSONArray()
                val keywords = mutableListOf<String>()
                for (j in 0 until keywordsJsonArray.length()) {
                    keywords.add(keywordsJsonArray.getString(j))
                }
                favoritePoemsList.add(
                    FavoritePoem(
                        id = jsonObject.getLong("id"),
                        poem = jsonObject.getString("poem"),
                        region = jsonObject.getString("region"),
                        keywords = keywords,
                        timestamp = jsonObject.getString("timestamp")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        favoritesAdapter.notifyDataSetChanged()
        updateNoFavoritesView()
    }

     fun updateNoFavoritesView() { // Made public for adapter callback
        if (favoritePoemsList.isEmpty()) {
            recyclerViewFavorites.visibility = View.GONE
            textViewNoFavorites.visibility = View.VISIBLE
        } else {
            recyclerViewFavorites.visibility = View.VISIBLE
            textViewNoFavorites.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        loadFavoritePoems()
    }

    private fun navigateToRegionInput(region: String) {
        val intent = Intent(this, RegionInputActivity::class.java)
        intent.putExtra("REGION", region)
        startActivity(intent)
    }
}
