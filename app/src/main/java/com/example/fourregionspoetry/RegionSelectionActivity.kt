package com.example.fourregionspoetry

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedCallback

class RegionSelectionActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnNorth: Button
    private lateinit var btnSouth: Button
    private lateinit var btnNortheast: Button
    private lateinit var btnCentral: Button

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
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnNorth = findViewById(R.id.btnNorth)
        btnSouth = findViewById(R.id.btnSouth)
        btnNortheast = findViewById(R.id.btnNortheast)
        btnCentral = findViewById(R.id.btnCentral)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish() // Go back to MainActivity
        }

        btnNorth.setOnClickListener {
            navigateToRegionInput("north")
        }

        btnSouth.setOnClickListener {
            navigateToRegionInput("south")
        }

        btnNortheast.setOnClickListener {
            navigateToRegionInput("northeast")
        }

        btnCentral.setOnClickListener {
            navigateToRegionInput("central")
        }
    }

    private fun navigateToRegionInput(region: String) {
        val intent = Intent(this, RegionInputActivity::class.java)
        intent.putExtra("REGION", region)
        startActivity(intent)
    }

}