package com.example.fourregionspoetry

import android.content.ComponentCallbacks2 // Added this import
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log // Added for logging in onTrimMemory
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

class MainActivity : AppCompatActivity(), ComponentCallbacks2 { // Implemented ComponentCallbacks2

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
        // You can add more specific logging or resource release logic here
        Log.d("MainActivity", "onTrimMemory called with level: $level")
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                /*
                   Release UI objects that are not visible.
                   The user interface has moved to the background.
                */
                Log.d("MainActivity", "Releasing UI hidden resources.")
            }
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                /*
                   Release resources that can be easily recreated if needed.
                   The system is running low on memory.
                */
                Log.d("MainActivity", "Releasing resources due to low memory (running).")
            }
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND,
            ComponentCallbacks2.TRIM_MEMORY_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                /*
                   Release as much memory as possible.
                   The app is in the background and may be killed.
                */
                Log.d("MainActivity", "Releasing resources due to background or system pressure.")
            }
            else -> {
                /*
                   Release any non-critical data structures.
                */
                Log.d("MainActivity", "Releasing other non-critical resources.")
            }
        }
    }
}
