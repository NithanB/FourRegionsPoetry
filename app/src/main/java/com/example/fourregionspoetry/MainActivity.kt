package com.example.fourregionspoetry

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.view.inputmethod.EditorInfo
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit // Added for SharedPreferences KTX
import java.util.ArrayList // Added this import

class MainActivity : AppCompatActivity() {

    private lateinit var btnStart: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        btnStart = findViewById(R.id.btnStart)
    }

    private fun setupListeners() {
        btnStart.setOnClickListener {
            startRegionSelection()
        }
    }

    private fun startRegionSelection() {
        val intent = Intent(this, RegionSelectionActivity::class.java)
        startActivity(intent)
    }
}



