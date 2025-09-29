// REFERENCE ONLY - This Kotlin code won't work in this web environment
// This would be used in Android Studio

package com.example.fourregionspoetry

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexboxLayout
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ResultActivity : AppCompatActivity() {

    private lateinit var btnHome: ImageButton
    private lateinit var btnCopy: Button
    private lateinit var btnSave: Button
    private lateinit var btnBackToStart: Button
    private lateinit var tvPoem: TextView
    private lateinit var tvRegionTitle: TextView
    private lateinit var tvRegionThai: TextView
    private lateinit var tvEmoji: TextView
    private lateinit var flexboxKeywords: FlexboxLayout

    private lateinit var poem: String
    private lateinit var region: String
    private lateinit var keywords: ArrayList<String>

    private val regionData = mapOf(
        "north" to Triple("Northern Thailand", "ภาคเหนือ", "🏔️"),
        "south" to Triple("Southern Thailand", "ภาคใต้", "🏝️"),
        "northeast" to Triple("Isan", "อีสาน", "🌾"),
        "central" to Triple("Central Thailand", "ภาคกลาง", "🏛️")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // Get data from intent
        poem = intent.getStringExtra("POEM") ?: ""
        region = intent.getStringExtra("REGION") ?: "central"
        keywords = intent.getStringArrayListExtra("KEYWORDS") ?: arrayListOf()

        initViews()
        setupListeners()
        updateUI()
        savePoemToHistory()
    }

    private fun initViews() {
        btnHome = findViewById(R.id.btnHome)
        btnCopy = findViewById(R.id.btnCopy)
        btnSave = findViewById(R.id.btnSave)
        btnBackToStart = findViewById(R.id.btnBackToStart)
        tvPoem = findViewById(R.id.tvPoem)
        tvRegionTitle = findViewById(R.id.tvRegionTitle)
        tvRegionThai = findViewById(R.id.tvRegionThai)
        tvEmoji = findViewById(R.id.tvEmoji)
        flexboxKeywords = findViewById(R.id.flexboxKeywords)
    }

    private fun setupListeners() {
        btnHome.setOnClickListener {
            navigateToStart()
        }

        btnCopy.setOnClickListener {
            copyPoemToClipboard()
        }

        btnSave.setOnClickListener {
            savePoemToFavorites()
        }

        btnBackToStart.setOnClickListener {
            navigateToStart()
        }
    }

    private fun updateUI() {
        // Set poem text
        tvPoem.text = poem

        // Set region info
        val data = regionData[region]
        if (data != null) {
            tvRegionTitle.text = data.first
            tvRegionThai.text = data.second
            tvEmoji.text = data.third
        }

        // Add keyword chips
        flexboxKeywords.removeAllViews()
        keywords.forEach { keyword ->
            val chip = layoutInflater.inflate(R.layout.chip_keyword, flexboxKeywords, false)
            val tvKeyword = chip.findViewById<TextView>(R.id.tvKeyword)
            tvKeyword.text = keyword
            flexboxKeywords.addView(chip)
        }
    }

    private fun copyPoemToClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Thai Poem", poem)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "Poem copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    private fun savePoemToFavorites() {
        val sharedPref = getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val existingFavorites = sharedPref.getString("favorite_poems", "[]")
        val favoritesArray = JSONArray(existingFavorites)

        // Check if poem already exists in favorites
        for (i in 0 until favoritesArray.length()) {
            val favorite = favoritesArray.getJSONObject(i)
            if (favorite.getString("poem") == poem) {
                Toast.makeText(this, "Poem already in favorites!", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Add new favorite
        val newFavorite = JSONObject().apply {
            put("id", System.currentTimeMillis())
            put("poem", poem)
            put("region", region)
            put("keywords", JSONArray(keywords))
            put("timestamp", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date()))
        }

        favoritesArray.put(newFavorite)

        with(sharedPref.edit()) {
            putString("favorite_poems", favoritesArray.toString())
            apply()
        }

        Toast.makeText(this, "Poem saved to favorites!", Toast.LENGTH_SHORT).show()
    }

    private fun savePoemToHistory() {
        val sharedPref = getSharedPreferences("history", Context.MODE_PRIVATE)
        val existingHistory = sharedPref.getString("saved_poems", "[]")
        val historyArray = JSONArray(existingHistory)

        // Add new poem to history
        val newPoem = JSONObject().apply {
            put("id", System.currentTimeMillis())
            put("poem", poem)
            put("region", region)
            put("keywords", JSONArray(keywords))
            put("timestamp", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date()))
        }

        // Add to beginning of array
        val newHistoryArray = JSONArray()
        newHistoryArray.put(newPoem)

        // Add existing poems (keep only last 9 for total of 10)
        val maxHistory = 9
        for (i in 0 until minOf(historyArray.length(), maxHistory)) {
            newHistoryArray.put(historyArray.getJSONObject(i))
        }

        with(sharedPref.edit()) {
            putString("saved_poems", newHistoryArray.toString())
            apply()
        }
    }

    private fun navigateToStart() {
        // Clear the activity stack and go back to MainActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        navigateToStart()
    }
}