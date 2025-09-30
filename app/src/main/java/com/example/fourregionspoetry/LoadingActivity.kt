package com.example.fourregionspoetry

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
// import com.example.fourregionspoetry.GeminiApiService
import androidx.activity.OnBackPressedCallback

class LoadingActivity : AppCompatActivity() {

    private lateinit var ivLogo: ImageView
    private lateinit var tvLoadingText: TextView
    private lateinit var tvLoadingThai: TextView

    private lateinit var region: String
    private lateinit var keywords: ArrayList<String>
    private lateinit var viewModel: RegionInputViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        // Get data from intent
        region = intent.getStringExtra("REGION") ?: "central"
        keywords = intent.getStringArrayListExtra("KEYWORDS") ?: arrayListOf()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Disable back button during loading
                // User must wait for completion or error
            }
        })

        initViews()
        setupAnimation()
        initViewModel()
        generatePoetry()
    }

    private fun initViews() {
        ivLogo = findViewById(R.id.ivLogo)
        tvLoadingText = findViewById(R.id.tvLoadingText)
        tvLoadingThai = findViewById(R.id.tvLoadingThai)
    }

    private fun setupAnimation() {
        // Rotate animation for logo
        val rotateAnimation = RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 2000
            repeatCount = Animation.INFINITE
        }

        ivLogo.startAnimation(rotateAnimation)

        // Animate loading text
        animateLoadingDots()
    }

    private fun animateLoadingDots() {
        val handler = Handler(Looper.getMainLooper())
        val loadingTexts = arrayOf(
            "Creating Your Poem",
            "Creating Your Poem.",
            "Creating Your Poem..",
            "Creating Your Poem..."
        )

        var index = 0
        val runnable = object : Runnable {
            override fun run() {
                tvLoadingText.text = loadingTexts[index % loadingTexts.size]
                index++
                handler.postDelayed(this, 500)
            }
        }
        handler.post(runnable)
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this)[RegionInputViewModel::class.java]

        viewModel.uiState.observe(this) { state ->
            when (state) {
                is RegionInputViewModel.UiState.Success -> {
                    navigateToResult(state.poem)
                }
                is RegionInputViewModel.UiState.Error -> {
                    // Handle error - could show error dialog or go back
                    showError(state.message)
                }
                else -> {
                    // Loading state - already handled by UI
                }
            }
        }
    }

    private fun generatePoetry() {
        viewModel.generatePoem(region, keywords)
    }

    private fun navigateToResult(poem: String) {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("POEM", poem)
        intent.putExtra("REGION", region)
        intent.putStringArrayListExtra("KEYWORDS", keywords)
        startActivity(intent)
        finish() // Kill this activity as requested
    }

    private fun showError(message: String) {
        // Simple error handling - in production you might want a proper error dialog
        runOnUiThread {
            tvLoadingText.text = "Something"
            tvLoadingThai.text = message
        }

        // Go back after showing error
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 2000)
    }

}