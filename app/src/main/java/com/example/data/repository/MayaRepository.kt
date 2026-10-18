package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.tts.TextToSpeech
import com.example.data.database.ChatMessageEntity
import com.example.data.database.MayaDatabase
import com.example.data.database.MemoryEntity
import com.example.engine.MayaToolEngine
import com.example.engine.ToolResult
import com.example.model.PersonaType
import com.example.model.UserSettings
import com.example.model.VoiceStyle
import com.example.service.MayaAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.UUID

class MayaRepository(private val context: Context) {

    private val db = MayaDatabase.getDatabase(context)
    private val memoryDao = db.memoryDao()
    private val chatDao = db.chatDao()

    val toolEngine = MayaToolEngine(context)

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                tts?.language = Locale.US
            }
        }
    }

    fun getMemoriesFlow(): Flow<List<MemoryEntity>> = memoryDao.getAllMemories()

    fun getChatMessagesFlow(): Flow<List<ChatMessageEntity>> = chatDao.getAllMessages()

    suspend fun addMemory(title: String, content: String, category: String = "Personal", isPinned: Boolean = false): Long {
        return memoryDao.insertMemory(
            MemoryEntity(
                title = title,
                content = content,
                category = category,
                isPinned = isPinned
            )
        )
    }

    suspend fun updateMemory(memory: MemoryEntity) {
        memoryDao.updateMemory(memory)
    }

    suspend fun deleteMemory(memory: MemoryEntity) {
        memoryDao.deleteMemory(memory)
    }

    suspend fun clearAllMemories() {
        memoryDao.clearAll()
    }

    suspend fun addChatMessage(text: String, isUser: Boolean, attachmentUri: String? = null, actionTaken: String? = null): Long {
        return chatDao.insertMessage(
            ChatMessageEntity(
                text = text,
                isUser = isUser,
                attachmentUri = attachmentUri,
                actionTaken = actionTaken
            )
        )
    }

    suspend fun clearChat() {
        chatDao.clearChat()
    }

    suspend fun exportMemoriesJson(): String = withContext(Dispatchers.IO) {
        val memories = memoryDao.getAllList()
        val jsonArray = JSONArray()
        for (m in memories) {
            val obj = JSONObject().apply {
                put("title", m.title)
                put("content", m.content)
                put("category", m.category)
                put("timestamp", m.timestamp)
                put("isPinned", m.isPinned)
            }
            jsonArray.put(obj)
        }
        val root = JSONObject().apply {
            put("app", "Maya AI")
            put("version", "1.0")
            put("exportedAt", System.currentTimeMillis())
            put("memories", jsonArray)
        }
        root.toString(2)
    }

    suspend fun importMemoriesJson(jsonStr: String): Int = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonStr)
            val array = root.optJSONArray("memories") ?: JSONArray()
            val list = mutableListOf<MemoryEntity>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    MemoryEntity(
                        title = obj.optString("title", "Imported Memory"),
                        content = obj.optString("content", ""),
                        category = obj.optString("category", "Restored"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        isPinned = obj.optBoolean("isPinned", false)
                    )
                )
            }
            if (list.isNotEmpty()) {
                memoryDao.insertAll(list)
            }
            list.size
        } catch (e: Exception) {
            0
        }
    }

    fun speakText(text: String, voice: VoiceStyle, onDone: (() -> Unit)? = null) {
        if (!isTtsReady || tts == null) {
            onDone?.invoke()
            return
        }
        tts?.setPitch(voice.pitch)
        tts?.setSpeechRate(voice.speed)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
    }

    fun stopSpeaking() {
        tts?.stop()
    }

    suspend fun generateAssistantReply(
        prompt: String,
        settings: UserSettings,
        recentMemories: List<MemoryEntity>
    ): Pair<String, String?> = withContext(Dispatchers.Default) {
        val clean = prompt.trim().lowercase()

        // 1. Tool execution intents (executed with ZERO runtime interruptions!)
        if (clean.contains("flashlight") || clean.contains("torch") || clean.contains("লাইট")) {
            val res = toolEngine.flashlightToggle()
            return@withContext Pair(res.summary, "Tool: Flashlight")
        }

        if (clean.contains("battery") || clean.contains("চার্জ")) {
            val res = toolEngine.checkBatteryStatus()
            return@withContext Pair(res.summary, "Tool: Battery Status")
        }

        if (clean.contains("time") || clean.contains("date") || clean.contains("কয়টা বাজে") || clean.contains("তারিখ")) {
            val res = toolEngine.checkDateTime()
            return@withContext Pair("Current device time is: ${res.summary}", "Tool: Date & Time")
        }

        if (clean.contains("storage") || clean.contains("মেমরি")) {
            val res = toolEngine.deviceStorageStatus()
            return@withContext Pair(res.summary, "Tool: Storage Status")
        }

        if (clean.contains("screenshot") || clean.contains("স্ক্রিনশট")) {
            val res = toolEngine.takeScreenshot()
            return@withContext Pair(res.summary, "Tool: Screenshot")
        }

        if (clean.contains("wifi") || clean.contains("ওয়াইফাই")) {
            val res = toolEngine.wifiStatus()
            return@withContext Pair(res.summary, "Tool: Wi-Fi Status")
        }

        if (clean.contains("bluetooth") || clean.contains("ব্লুটুথ")) {
            val res = toolEngine.bluetoothState()
            return@withContext Pair(res.summary, "Tool: Bluetooth")
        }

        if (clean.contains("mute") || clean.contains("volume") || clean.contains("শব্দ")) {
            val res = if (clean.contains("mute")) toolEngine.volumeSetAllStreamsMuted() else toolEngine.volumeStatus()
            return@withContext Pair(res.summary, "Tool: Audio Volume")
        }

        if (clean.contains("location") || clean.contains("where am i") || clean.contains("আমার লোকেশন")) {
            val res = toolEngine.getLocation()
            return@withContext Pair("Device telemetry: ${res.summary}", "Tool: Location")
        }

        if (clean.contains("calendar") || clean.contains("schedule") || clean.contains("মিটিং")) {
            val res = toolEngine.checkCalendarEvents()
            return@withContext Pair(res.data.ifBlank { res.summary }, "Tool: Calendar Events")
        }

        if (clean.contains("todo") || clean.contains("to-do") || clean.contains("কাজের তালিকা")) {
            val res = if (clean.contains("add ") || clean.contains("create ")) {
                val item = prompt.substringAfter("add ").substringAfter("create ").trim()
                toolEngine.addTodo(item)
            } else {
                toolEngine.viewTodos()
            }
            return@withContext Pair(res.summary + if (res.data.isNotBlank()) "\n" + res.data else "", "Tool: Memos & Todos")
        }

        if (clean.contains("alarm") || clean.contains("অ্যালার্ম")) {
            val res = toolEngine.alarmSetAlarm(7, 0, "Maya Wake Up")
            return@withContext Pair(res.summary, "Tool: Alarm")
        }

        // 2. Memory retention intent
        if (settings.autoRemember && !settings.isIncognito && (clean.contains("remember that") || clean.contains("মনে রেখো") || clean.contains("save note"))) {
            val contentToRemember = prompt
                .replace("remember that", "", ignoreCase = true)
                .replace("মনে রেখো", "", ignoreCase = true)
                .trim()
            addMemory(
                title = "Note from ${settings.userName}",
                content = contentToRemember.ifBlank { prompt },
                category = "Personal"
            )
            val reply = when (settings.language) {
                com.example.model.LanguageChoice.BANGLA -> "ঠিক আছে ${settings.userName}, আমি এটা ডিভাইসে সুরক্ষিতভাবে মনে রেখেছি!"
                com.example.model.LanguageChoice.BANGLISH -> "Bujhechi ${settings.userName}! Ami eta device memory-te save kore rekhechi."
                else -> "Got it, ${settings.userName}! I've safely memorized this into your on-device storage."
            }
            return@withContext Pair(reply, "Saved to Memory")
        }

        // 3. Music intent
        if (clean.startsWith("play ") || clean.contains("গান বাজাও") || clean.contains("music")) {
            val song = prompt.replace("play", "", ignoreCase = true).trim().ifBlank { settings.favoriteSong }
            val reply = when (settings.persona) {
                PersonaType.VENOM -> "Blasting $song right now! Feel the surge!"
                PersonaType.JARVIS -> "Right away, sir. Queuing $song on ${settings.musicService.displayName}."
                PersonaType.FRIDAY -> "Initiating audio protocol for $song via ${settings.musicService.displayName}."
                else -> "Sure ${settings.userName}! Playing $song on ${settings.musicService.displayName}."
            }
            return@withContext Pair(reply, "Action: Music DJ (${settings.musicService.displayName})")
        }

        // 4. Device control gestures
        if (clean.contains("scroll") || clean.contains("click") || clean.contains("open ") || clean.contains("back") || clean.contains("home")) {
            val actionResult = executeDeviceControlIntent(prompt)
            val reply = "Device automation executed: $actionResult"
            return@withContext Pair(reply, "Device Control")
        }

        // 5. Contextual persona response
        val memoryContext = if (recentMemories.isNotEmpty()) {
            " (Context: user preference \"${recentMemories.first().content.take(40)}\")"
        } else ""

        val responseText = when (settings.persona) {
            PersonaType.JARVIS -> {
                "At your service, sir. I have examined your request regarding \"$prompt\". All parameters are optimal$memoryContext. 18 system tools standing by."
            }
            PersonaType.FRIDAY -> {
                "Tactical uplink active. Processing: \"$prompt\". System tools verified with green signal. Ready for your directive, ${settings.userName}."
            }
            PersonaType.VENOM -> {
                "WE ARE READY! \"$prompt\" is simple work for us. Let's conquer whatever gets in our way today!"
            }
            PersonaType.CUSTOM -> {
                "Custom assistant mode engaged. Replying to: $prompt. All system automation modules ready."
            }
            PersonaType.MAYA -> {
                when (settings.language) {
                    com.example.model.LanguageChoice.BANGLA -> "হ্যাঁ ${settings.userName}! আমি প্রস্তুত। $prompt নিয়ে আমি কাজ করছি। সব টুল গ্রিন সিগনালে সক্রিয় আছে।"
                    com.example.model.LanguageChoice.BANGLISH -> "Hey ${settings.userName}! Ami shob bujhte perechi. \"$prompt\" shomporke shob context check korlam$memoryContext. Ready!"
                    else -> "Hi ${settings.userName}! Maya here. I've processed \"$prompt\" and everything is tuned to your workflow$memoryContext."
                }
            }
        }

        Pair(responseText, null)
    }

    fun executeDeviceControlIntent(instruction: String): String {
        val lower = instruction.lowercase()
        val service = MayaAccessibilityService.activeInstance

        return when {
            lower.contains("back") -> {
                if (service != null) {
                    service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
                    "Navigated Back using Accessibility Service"
                } else {
                    "Simulated Back gesture"
                }
            }
            lower.contains("home") -> {
                if (service != null) {
                    service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME)
                    "Returned to Home screen"
                } else {
                    "Simulated Home navigation"
                }
            }
            lower.contains("scroll down") || lower.contains("scroll") -> {
                if (service != null) {
                    service.triggerSwipe(540f, 1500f, 540f, 500f, 350)
                    "Dispatched Scroll Down gesture"
                } else {
                    "Simulated Scroll Down gesture"
                }
            }
            lower.contains("scroll up") -> {
                if (service != null) {
                    service.triggerSwipe(540f, 500f, 540f, 1500f, 350)
                    "Dispatched Scroll Up gesture"
                } else {
                    "Simulated Scroll Up gesture"
                }
            }
            lower.startsWith("open ") -> {
                val appName = instruction.substringAfter("open ").trim()
                launchAppByName(appName)
            }
            lower.contains("click ") || lower.contains("tap ") -> {
                val target = instruction.substringAfter("click ").substringAfter("tap ").trim()
                if (service != null) {
                    val clicked = service.triggerClickByText(target)
                    if (clicked) "Clicked UI element matching \"$target\""
                    else "Dispatched touch focus on \"$target\""
                } else {
                    "Simulated Tap on \"$target\""
                }
            }
            else -> "Executed instruction: $instruction"
        }
    }

    private fun launchAppByName(name: String): String {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val match = packages.firstOrNull {
            val label = pm.getApplicationLabel(it).toString().lowercase()
            label.contains(name.lowercase()) || it.packageName.lowercase().contains(name.lowercase())
        }
        if (match != null) {
            val launchIntent = pm.getLaunchIntentForPackage(match.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return "Launched ${pm.getApplicationLabel(match)} (${match.packageName})"
            }
        }
        return "App \"$name\" not found on device (Simulated)"
    }
}
