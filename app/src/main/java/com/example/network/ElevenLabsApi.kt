package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class ElevenLabsVoiceSettings(
    @Json(name = "stability") val stability: Float = 0.5f,
    @Json(name = "similarity_boost") val similarityBoost: Float = 0.75f,
    @Json(name = "style") val style: Float = 0.0f,
    @Json(name = "use_speaker_boost") val useSpeakerBoost: Boolean = true
)

@JsonClass(generateAdapter = true)
data class ElevenLabsTtsRequest(
    @Json(name = "text") val text: String,
    @Json(name = "model_id") val modelId: String = "eleven_monolingual_v1",
    @Json(name = "voice_settings") val voiceSettings: ElevenLabsVoiceSettings = ElevenLabsVoiceSettings()
)

interface ElevenLabsApiService {

    @Headers(
        "Accept: audio/mpeg",
        "Content-Type: application/json"
    )
    @POST("v1/text-to-speech/{voice_id}")
    @Streaming
    suspend fun textToSpeechStream(
        @Path("voice_id") voiceId: String,
        @Header("xi-api-key") apiKey: String,
        @Query("optimize_streaming_latency") optimizeLatency: Int = 3,
        @Body request: ElevenLabsTtsRequest
    ): Response<ResponseBody>

    @GET("v1/user")
    suspend fun getUserInfo(
        @Header("xi-api-key") apiKey: String
    ): Response<ResponseBody>

    @GET("v1/voices")
    suspend fun getVoices(
        @Header("xi-api-key") apiKey: String
    ): Response<ResponseBody>
}

object ElevenLabsClient {
    private const val BASE_URL = "https://api.elevenlabs.io/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val apiService: ElevenLabsApiService = retrofit.create(ElevenLabsApiService::class.java)

    suspend fun streamTextToSpeech(
        apiKey: String,
        voiceId: String,
        text: String,
        modelId: String = "eleven_monolingual_v1"
    ): Response<ResponseBody> = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        val cleanVoiceId = if (voiceId.isBlank()) "21m00Tcm4TlvDq8ikWAM" else voiceId.trim()
        val request = ElevenLabsTtsRequest(
            text = text,
            modelId = if (modelId.isBlank()) "eleven_monolingual_v1" else modelId.trim(),
            voiceSettings = ElevenLabsVoiceSettings(
                stability = 0.5f,
                similarityBoost = 0.75f,
                style = 0.0f,
                useSpeakerBoost = true
            )
        )
        apiService.textToSpeechStream(
            voiceId = cleanVoiceId,
            apiKey = cleanKey,
            optimizeLatency = 3,
            request = request
        )
    }

    suspend fun testConnection(apiKey: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        if (cleanKey.isEmpty()) {
            return@withContext Pair(false, "Failed to connect: ElevenLabs API Key is missing.")
        }
        try {
            val response = apiService.getUserInfo(cleanKey)
            if (response.isSuccessful) {
                val bodyStr = response.body()?.string() ?: ""
                var details = "ElevenLabs API verified successfully!"
                try {
                    val json = JSONObject(bodyStr)
                    val subscription = json.optJSONObject("subscription")
                    if (subscription != null) {
                        val tier = subscription.optString("tier", "standard")
                        val charCount = subscription.optInt("character_count", 0)
                        val charLimit = subscription.optInt("character_limit", 0)
                        details = "Connected to ElevenLabs ($tier tier, used $charCount / $charLimit chars)."
                    }
                } catch (_: Exception) {}
                Pair(true, details)
            } else {
                val err = response.errorBody()?.string() ?: "HTTP ${response.code()}"
                var msg = "Failed to connect: $err"
                try {
                    val errJson = JSONObject(err)
                    val detail = errJson.optJSONObject("detail")?.optString("message")
                        ?: errJson.optString("message")
                    if (detail.isNotEmpty()) msg = "ElevenLabs error: $detail"
                } catch (_: Exception) {}
                Pair(false, msg)
            }
        } catch (e: Exception) {
            Pair(false, "Failed to connect to ElevenLabs: ${e.localizedMessage ?: "Network error"}")
        }
    }
}
