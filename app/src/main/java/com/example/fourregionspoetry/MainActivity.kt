package com.example.fourregionspoetry

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.view.inputmethod.EditorInfo
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import java.util.ArrayList

class MainActivity : AppCompatActivity(), ComponentCallbacks2 {

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

    // Added onTrimMemory method
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.d("MainActivity", "onTrimMemory called with level: $level")

        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                // App’s UI is not visible → free UI resources
                Log.d("MainActivity", "Releasing UI hidden resources.")
            }
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> {
                // App moved to background → release caches and other non-critical resources
                Log.d("MainActivity", "Releasing resources due to background.")
            }
            else -> {
                // Handle any other memory warnings
                Log.d("MainActivity", "Releasing other non-critical resources.")
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        // Free everything that’s not essential
        Log.d("MainActivity", "System is running critically low on memory!")
    }

}
