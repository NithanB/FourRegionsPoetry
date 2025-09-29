// REFERENCE ONLY - This Kotlin code won't work in this web environment
// This would be used in Android Studio for Gemini AI integration

package com.example.fourregionspoetry // Changed package name

import androidx.lifecycle.MutableLiveData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay // Added import for delay
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GeminiResponse(
    val candidates: List<Candidate>
)

data class Candidate(
    val content: Content
)

interface GeminiApiService {
    @POST("v1/models/gemini-pro:generateContent")
    suspend fun generateContent(
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}

class GeminiRepository {

    private val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }

    // Mock poems for demonstration
    private val mockPoems = mapOf(
        "north" to listOf(
            """ภูเขาสูงใสใน ลานนาโบราณ
ดอกไม้บานสวย งามตาดุจใส
ลมหวานพัดมา เสียงธรรมชาติ
ใจเย็นสบาย ในแผ่นดินไทย""",

            """เชียงใหม่งามตา วัดทองระยิบ
ดอยสุเทพสูง เสียงระฆังก้อง
ลือนามไกลโพ้น ศิลปะลานนา
หัวใจคนไทย รักแผ่นดินนี้"""
        ),
        "south" to listOf(
            """ทะเลใต้คลื่นใส ปลาแหวกว่าย
เกาะเล็กเกาะใหญ่ งามเหลือคำ
ลมทะเลพัดเซา หอมกลิ่นเกลือ
ใต้ฟ้าใสสดใส สีฟ้าแสงทอง""",

            """มะพร้าวริมชาย คลื่นใสซู่ซ่า
อาหารใต้จัด รสเด็ดเผ็ดร้อน
เรือประมงยาว แล่นผ่านคลื่น
ชาวใต้ใจดี ยิ้มแย้มต้อนรับ"""
        ),
        "northeast" to listOf(
            """อีสานแผ่นดิน แห้งแล้งกว้างใหญ่
ข้าวโพดเหลือง ผลผลิตดี
ปลาร้าส้มตำ รสชาติถิ่นฐาน
ใจคนอีสาน อบอุ่นจริงใจ""",

            """ไผ่ป่าไผ่เหลือง โบกสะบัดลม
ดินแดนอีสาน วัฒนธรรมดี
แซ่บจี๊ดหอมหวล รสชาติจัดจ้าน
ฟ้าใสเวียงจันทน์ ถิ่นไทยแท้"""
        ),
        "central" to listOf(
            """กรุงเทพมหานคร เมืองใหญ่คึกคัก
วัดพระแก้วงาม พระบรมราชวัง
เจ้าพระยาไหล ผ่านใจกลางเมือง
ดินแดนกลางไทย มรดกโลกงาม""",

            """นาข้าวเขียวขจี ในดินกลางไทย
ชาวนาใจดี ปลูกข้าวขาวขำ
วัฒนธรรมดี งดงามยิ่งนัก
แผ่นดินกลางไทย หัวใจของชาติ"""
        )
    )

    suspend fun generatePoem(region: String, keywords: List<String>): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // For demonstration, return mock poem
                // In real implementation, uncomment the API call below

                delay(2000) // Simulate network delay
                val regionPoems = mockPoems[region] ?: mockPoems["central"]!!
                var selectedPoem = regionPoems.random()

                // Simple keyword incorporation
                keywords.forEachIndexed { index, keyword ->
                    when (index) {
                        0 -> selectedPoem = selectedPoem.replace("งาม", keyword)
                        1 -> selectedPoem = selectedPoem.replace("ใส", keyword)
                    }
                }

                Result.success(selectedPoem)

                /* Real API implementation:
                val prompt = buildPrompt(region, keywords)
                val request = GeminiRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(Part(prompt))
                        )
                    )
                )

                val response = apiService.generateContent(
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    request = request
                )

                if (response.isSuccessful) {
                    val poem = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (poem != null) {
                        Result.success(poem)
                    } else {
                        Result.failure(Exception("Empty response"))
                    }
                } else {
                    Result.failure(Exception("API Error: ${response.code()}"))
                }
                */

            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun buildPrompt(region: String, keywords: List<String>): String {
        val regionNames = mapOf(
            "north" to "northern thailand",
            "south" to "southern thailand",
            "northeast" to "isan",
            "central" to "central thailand"
        )

        val regionName = regionNames[region] ?: "thailand"
        val shortRhyming = if (region == "north") "short rhyming " else ""

        return "create a ${shortRhyming}thai poem with $regionName and ${keywords.joinToString(", ")}"
    }
}

// ViewModel for managing UI state
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