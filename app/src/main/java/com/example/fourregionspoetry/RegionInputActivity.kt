package com.example.fourregionspoetry

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RegionInputActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnGenerate: Button
    private lateinit var etKeyword: EditText
    private lateinit var btnAddKeyword: Button
    private lateinit var rvKeywords: RecyclerView
    private lateinit var tvRegionTitle: TextView
    private lateinit var tvRegionThai: TextView
    private lateinit var tvEmoji: TextView
    private lateinit var cardPreview: CardView
    private lateinit var tvPreview: TextView

    private lateinit var keywordsAdapter: KeywordsAdapter
    private val keywords = mutableListOf<String>()
    private lateinit var region: String

    private val regionData = mapOf(
        "north" to Triple("Northern Thailand", "ภาคเหนือ", "🏔️"),
        "south" to Triple("Southern Thailand", "ภาคใต้", "🏝️"),
        "northeast" to Triple("Isan", "อีสาน", "🌾"),
        "central" to Triple("Central Thailand", "ภาคกลาง", "🏛️")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_region_input)

        region = intent.getStringExtra("REGION") ?: "central"

        initViews()
        setupRecyclerView()
        setupListeners()
        updateUI()
        loadSavedKeywords()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnGenerate = findViewById(R.id.btnGenerate)
        etKeyword = findViewById(R.id.etKeyword)
        btnAddKeyword = findViewById(R.id.btnAddKeyword)
        rvKeywords = findViewById(R.id.rvKeywords)
        tvRegionTitle = findViewById(R.id.tvRegionTitle)
        tvRegionThai = findViewById(R.id.tvRegionThai)
        tvEmoji = findViewById(R.id.tvEmoji)
        cardPreview = findViewById(R.id.cardPreview)
        tvPreview = findViewById(R.id.tvPreview)
    }

    private fun setupRecyclerView() {
        keywordsAdapter = KeywordsAdapter(keywords) { position ->
            removeKeyword(position)
        }
        rvKeywords.adapter = keywordsAdapter
        rvKeywords.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnAddKeyword.setOnClickListener {
            addKeyword()
        }

        etKeyword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addKeyword()
                true
            } else {
                false
            }
        }

        btnGenerate.setOnClickListener {
            generatePoetry()
        }
    }

    private fun updateUI() {
        val data = regionData[region] ?: return
        tvRegionTitle.text = data.first
        tvRegionThai.text = data.second
        tvEmoji.text = data.third

        updateGenerateButton()
        updatePreview()
    }

    private fun addKeyword() {
        val keyword = etKeyword.text.toString().trim()
        if (keyword.isNotEmpty() && keywords.size < 3 && !keywords.contains(keyword)) {
            keywords.add(keyword)
            keywordsAdapter.notifyItemInserted(keywords.size - 1)
            etKeyword.text.clear()

            saveKeywords()
            updateGenerateButton()
            updatePreview()
        }
    }

    private fun removeKeyword(position: Int) {
        keywords.removeAt(position)
        keywordsAdapter.notifyItemRemoved(position)

        saveKeywords()
        updateGenerateButton()
        updatePreview()
    }

    private fun updateGenerateButton() {
        btnGenerate.isEnabled = keywords.isNotEmpty()
        btnAddKeyword.isEnabled = keywords.size < 3
        etKeyword.isEnabled = keywords.size < 3
    }

    private fun updatePreview() {
        if (keywords.isNotEmpty()) {
            val regionTitle = regionData[region]?.first?.lowercase() ?: region
            val shortRhyming = if (region == "north") "short rhyming " else ""
            val preview = "create a ${shortRhyming}thai poem with $regionTitle and ${keywords.joinToString(", ")}"
            tvPreview.text = "something"
            cardPreview.visibility = View.VISIBLE
        } else {
            cardPreview.visibility = View.GONE
        }
    }

    private fun generatePoetry() {
        val intent = Intent(this, LoadingActivity::class.java)
        intent.putExtra("REGION", region)
        intent.putStringArrayListExtra("KEYWORDS", ArrayList(keywords))
        startActivity(intent)
    }

    private fun saveKeywords() {
        val sharedPref = getSharedPreferences("keywords", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putStringSet("keywords_$region", keywords.toSet())
            apply()
        }
    }

    private fun loadSavedKeywords() {
        val sharedPref = getSharedPreferences("keywords", Context.MODE_PRIVATE)
        val savedKeywords = sharedPref.getStringSet("keywords_$region", emptySet())
        keywords.addAll(savedKeywords ?: emptySet())
        keywordsAdapter.notifyDataSetChanged()
        updateGenerateButton()
        updatePreview()
    }
}

// Keywords RecyclerView Adapter
class KeywordsAdapter(
    private val keywords: MutableList<String>,
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<KeywordsAdapter.KeywordViewHolder>() {

    class KeywordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvKeyword: TextView = itemView.findViewById(R.id.tvKeyword)
        val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeywordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_keyword, parent, false)
        return KeywordViewHolder(view)
    }

    override fun onBindViewHolder(holder: KeywordViewHolder, position: Int) {
        holder.tvKeyword.text = keywords[position]
        holder.btnRemove.setOnClickListener {
            onRemoveClick(position)
        }
    }

    override fun getItemCount(): Int = keywords.size
}