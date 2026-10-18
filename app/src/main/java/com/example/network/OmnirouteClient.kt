package com.example.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object OmnirouteClient {

    private fun normalizeBaseUrl(rawUrl: String): String {
        var url = rawUrl.trim()
        if (url.isEmpty()) return ""
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://$url"
        }
        return url.removeSuffix("/")
    }

    suspend fun testConnection(baseUrl: String, apiKey: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        if (cleanKey.isEmpty()) {
            return@withContext Pair(false, "Failed to connect: API key is empty.")
        }
        val cleanBaseUrl = normalizeBaseUrl(baseUrl)
        if (cleanBaseUrl.isEmpty()) {
            return@withContext Pair(false, "Failed to connect: Base URL is empty.")
        }

        try {
            // First attempt: GET {cleanBaseUrl}/models
            val modelsUrl = URL("$cleanBaseUrl/models")
            val conn = modelsUrl.openConnection() as HttpURLConnection
            conn.connectTimeout = 7000
            conn.readTimeout = 7000
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $cleanKey")
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("User-Agent", "MayaAI/1.0")

            val responseCode = try {
                conn.responseCode
            } catch (e: Exception) {
                -1
            }

            if (responseCode in 200..299) {
                return@withContext Pair(true, "Successfully connected to OmniRoute! (Status: $responseCode OK)")
            }

            if (responseCode == 401) {
                return@withContext Pair(false, "Failed to connect: Unauthorized (401). Invalid or revoked API key.")
            }
            if (responseCode == 403) {
                return@withContext Pair(false, "Failed to connect: Forbidden (403). Access denied for this key.")
            }

            // Fallback attempt: POST ping to /chat/completions (OpenAI / OmniRoute standard)
            try {
                val chatUrl = URL("$cleanBaseUrl/chat/completions")
                val chatConn = chatUrl.openConnection() as HttpURLConnection
                chatConn.connectTimeout = 7000
                chatConn.readTimeout = 7000
                chatConn.requestMethod = "POST"
                chatConn.setRequestProperty("Authorization", "Bearer $cleanKey")
                chatConn.setRequestProperty("Content-Type", "application/json")
                chatConn.setRequestProperty("Accept", "application/json")
                chatConn.doOutput = true

                val pingPayload = JSONObject().apply {
                    put("model", "default")
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", "ping")
                        })
                    })
                    put("max_tokens", 2)
                }

                OutputStreamWriter(chatConn.outputStream).use { writer ->
                    writer.write(pingPayload.toString())
                    writer.flush()
                }

                val chatCode = chatConn.responseCode
                if (chatCode in 200..299) {
                    return@withContext Pair(true, "Successfully connected to OmniRoute! (Status: $chatCode OK)")
                }
                if (chatCode == 401 || chatCode == 403) {
                    return@withContext Pair(false, "Failed to connect: Unauthorized ($chatCode). Check your API key.")
                }
                if (chatCode in 400..499 && chatCode != 404) {
                    val errBody = try {
                        chatConn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    } catch (e: Exception) { "" }
                    if (errBody.contains("model", ignoreCase = true) || errBody.contains("quota", ignoreCase = true)) {
                        return@withContext Pair(true, "Successfully connected to OmniRoute gateway!")
                    }
                }
            } catch (e: Exception) {
                // Ignore fallback exception, will use primary or root error
            }

            if (responseCode == 404) {
                return@withContext Pair(false, "Failed to connect: Endpoint not found (HTTP 404). Check Base URL.")
            } else if (responseCode > 0) {
                return@withContext Pair(false, "Failed to connect: Server returned HTTP $responseCode")
            } else {
                return@withContext Pair(false, "Failed to connect: Unable to reach host ($cleanBaseUrl)")
            }
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: e.message ?: "Network unreachable or server offline"
            return@withContext Pair(false, "Failed to connect: $errorMsg")
        }
    }

    suspend fun sendChatCompletion(
        baseUrl: String,
        apiKey: String,
        prompt: String,
        systemPrompt: String = "You are Maya, an intelligent AI assistant with deep device automation access on Android. Always answer in English concisely."
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        if (cleanKey.isEmpty()) {
            return@withContext Pair(false, "Failed to connect: API key is empty.")
        }
        val cleanBaseUrl = normalizeBaseUrl(baseUrl)
        if (cleanBaseUrl.isEmpty()) {
            return@withContext Pair(false, "Failed to connect: Base URL is empty.")
        }

        try {
            val chatUrl = URL("$cleanBaseUrl/chat/completions")
            val conn = chatUrl.openConnection() as HttpURLConnection
            conn.connectTimeout = 12000
            conn.readTimeout = 15000
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer $cleanKey")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json")
            conn.doOutput = true

            val payload = JSONObject().apply {
                put("model", "default")
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
                put("temperature", 0.7)
                put("max_tokens", 800)
            }

            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(responseText)
                val choices = root.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val firstChoice = choices.getJSONObject(0)
                    val message = firstChoice.optJSONObject("message")
                    val content = message?.optString("content") ?: ""
                    if (content.isNotBlank()) {
                        return@withContext Pair(true, content.trim())
                    }
                }
                return@withContext Pair(true, responseText)
            } else {
                val err = try {
                    conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                } catch (e: Exception) { "" }
                return@withContext Pair(false, "Failed to connect: Server returned HTTP $responseCode ${err.take(120)}")
            }
        } catch (e: Exception) {
            return@withContext Pair(false, "Failed to connect: ${e.localizedMessage ?: "Unknown network error"}")
        }
    }
}
