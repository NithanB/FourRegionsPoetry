package com.example.fourregionspoetry

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.TextPart
import kotlinx.coroutines.launch

// --------------------
// Repository using Firebase Gemini SDK (Idiomatic & Secure)
// --------------------
class GeminiRepository {

    private val generativeModel by lazy {
        // 💡 Use GenerativeBackend.googleAI() to connect to the Gemini API securely.
        // This is the recommended Firebase way and avoids passing the API key directly.
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(modelName = "gemini-2.5-flash") // Using latest flash model
    }

    /**
     * Generates a poem using the Gemini model.
     * @param region The region of Thailand (e.g., "north", "south").
     * @param keywords A list of words to include in the poem.
     * @return A Result wrapping the generated poem string or an Exception.
     */
    suspend fun generatePoem(region: String, keywords: List<String>): Result<String> {
        return try {
            val prompt = buildPrompt(region, keywords)

            // The Content object uses the correct Firebase types
            val response = generativeModel.generateContent(
                Content(
                    role = "user", // Role can be optional for single-turn content
                    parts = listOf(TextPart(prompt)) // Corrected to use TextPart
                )
            )

            val poem = response.text
            if (!poem.isNullOrBlank()) {
                Result.success(poem)
            } else {
                // Check if the response was blocked by safety settings
                val safetyReason = response.promptFeedback?.blockReason?.name ?: "Unknown safety reason"
                val blockMessage = response.promptFeedback?.blockReasonMessage ?: "No specific block message"
                Result.failure(Exception("Empty response or blocked by safety settings: $safetyReason. Message: $blockMessage"))
            }
        } catch (e: Exception) {
            // Log the exception for better debugging
            android.util.Log.e("GeminiRepository", "Error generating poem", e)
            Result.failure(e)
        }
    }

    /**
     * Constructs the prompt string for the Gemini model.
     */
    private fun buildPrompt(region: String, keywords: List<String>): String {
        val regionNames = mapOf(
            "north" to "Northern Thailand",
            "south" to "Southern Thailand",
            "northeast" to "Isan (Northeastern Thailand)",
            "central" to "Central Thailand"
        )
        val regionName = regionNames[region] ?: "Thailand"
        return "Write a short Thai poem about $regionName including the words: ${keywords.joinToString(", ")}"
    }
}

// --------------------
// ViewModel
// --------------------
class RegionInputViewModel(
    private val repository: GeminiRepository = GeminiRepository()
) : ViewModel() {

    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val poem: String) : UiState()
        data class Error(val message: String) : UiState()
    }

    /**
     * Initiates the coroutine to generate the poem and updates the UiState.
     */
    fun generatePoem(region: String, keywords: List<String>) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            repository.generatePoem(region, keywords)
                .onSuccess { poem ->
                    _uiState.value = UiState.Success(poem)
                }
                .onFailure { exception ->
                    _uiState.value = UiState.Error(exception.message ?: "Unknown error")
                }
        }
    }
}