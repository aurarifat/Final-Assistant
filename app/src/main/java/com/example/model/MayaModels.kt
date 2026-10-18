package com.example.model

enum class MayaOrbState {
    ASLEEP,
    LISTENING,
    THINKING,
    SPEAKING,
    READY
}

enum class PersonaType(val displayName: String, val systemPromptVoice: String) {
    MAYA("Maya", "Warm, witty, intuitive and helpful Bengali-English personal companion."),
    FRIDAY("Friday", "Crisp, efficient, tactical and calm Irish-toned AI operating system."),
    VENOM("Venom", "Deep, gritty, bold, fierce and brutally honest symbiote intelligence."),
    JARVIS("Jarvis", "Sophisticated, impeccably polite, British-gentleman precision advisor."),
    CUSTOM("Custom", "Customized personality tuned to user instructions.")
}

enum class VoiceEngineMode(val displayName: String, val subtitle: String) {
    NORMAL("Normal", "Fast single-model conversational flow"),
    ADVANCED_AGENTIC("Advanced Agentic (Beta)", "Multi-layered reasoning, listening & speaking pipeline")
}

enum class VoiceSpeedMode(val displayName: String, val subtitle: String) {
    SWIFT("Maya Swift", "Ultra low-latency quick answers"),
    SOUL("Maya Soul", "Expressive, rich emotional depth and nuanced pauses")
}

enum class MusicService(val displayName: String, val iconName: String) {
    YT_MUSIC("YT Music", "YouTube Music"),
    SPOTIFY("Spotify", "Spotify"),
    YOUTUBE("YouTube", "YouTube Video")
}

enum class LanguageChoice(val displayName: String) {
    BANGLISH("BANGLISH (Bangla + English)"),
    ENGLISH("English (Global)"),
    BANGLA("Bangla (বাংলা)"),
    HINDI("Hindi (हिन्दी)"),
    SPANISH("Spanish (Español)")
}

enum class UserGender(val displayName: String) {
    MALE("Male"),
    FEMALE("Female"),
    PREFER_NOT_TO_SAY("Prefer not to say")
}

enum class SleepTimerChoice(val displayName: String, val minutes: Int) {
    NEVER("Never", 0),
    MIN_5("5 min", 5),
    MIN_10("10 min", 10),
    MIN_20("20 min", 20),
    MIN_45("45 min", 45)
}

enum class CaptionVoice(val displayName: String) {
    FUNNY("Funny & Sarcastic"),
    SIMPLE("Simple & Clean"),
    MOTIVATIONAL("Motivational & Inspiring")
}

data class VoiceStyle(
    val name: String,
    val code: String,
    val personaGroup: String, // Maya, Friday, Venom, Jarvis
    val pitch: Float = 1.0f,
    val speed: Float = 1.0f,
    val sampleText: String
)

val AVAILABLE_VOICES = listOf(
    VoiceStyle("Breezy", "Aoede", "Maya", 1.15f, 1.05f, "Hey there! Ready to conquer today with you."),
    VoiceStyle("Firm", "Kore", "Friday", 0.95f, 1.0f, "Systems operational. Standing by for instructions."),
    VoiceStyle("Youthful", "Leda", "Maya", 1.25f, 1.1f, "Hi Rifat! What are we exploring now?"),
    VoiceStyle("Bright", "Zephyr", "Maya", 1.1f, 1.0f, "The weather looks great and your schedule is primed!"),
    VoiceStyle("Upbeat", "Laomedeia", "Maya", 1.2f, 1.15f, "Let's put some rhythm into this work session!"),
    VoiceStyle("Smooth", "Despina", "Friday", 1.0f, 0.95f, "All telemetry nominal. Smooth sailing ahead."),
    VoiceStyle("Clear", "Erinome", "Maya", 1.05f, 1.0f, "Crisp clarity for your next briefing."),
    VoiceStyle("Easy-going", "Callirrhoe", "Maya", 0.98f, 0.95f, "Take it easy, I've got your reminders covered."),
    VoiceStyle("Bright", "Autonoe", "Maya", 1.12f, 1.05f, "Lighting up your creative ideas!"),
    VoiceStyle("Mature", "Gacrux", "Jarvis", 0.82f, 0.95f, "Good evening, sir. The protocols have been executed."),
    VoiceStyle("Forward", "Pulcherrima", "Friday", 1.02f, 1.08f, "Advancing task parameters immediately."),
    VoiceStyle("Warm", "Sulafat", "Maya", 0.92f, 0.9f, "Take a breath, you are doing wonderfully."),
    VoiceStyle("Gentle", "Vindemiatrix", "Maya", 1.05f, 0.92f, "Soft ambient focus initiated for your study.")
)

data class SkillItem(
    val id: String,
    val name: String,
    val description: String,
    val iconKey: String,
    val isInstalled: Boolean,
    val category: String
)

val DEFAULT_SKILLS = listOf(
    SkillItem("daily-briefing", "daily-briefing", "Curates daily news, weather, calendar events and personal agenda", "briefing", true, "Assistant"),
    SkillItem("whatsapp-pro", "whatsapp-pro", "Auto-reply, voice message transcription, and smart message alerts", "whatsapp", true, "Messaging"),
    SkillItem("call-secretary", "call-secretary", "Caller announcement, auto loudspeaker and caller filtering", "call", true, "Phone"),
    SkillItem("photo-share", "photo-share", "Automated photo cleanup, framing and instant secure media dispatch", "photo", false, "Media"),
    SkillItem("music-dj", "music-dj", "Voice-controlled Spotify/YT playlist curation and mood mixes", "music", true, "Media"),
    SkillItem("phone-care", "phone-care", "Monitors battery health, RAM usage, storage junk and background tasks", "battery", true, "System"),
    SkillItem("study-focus", "study-focus", "Pomodoro timer, distraction blocking and study summaries", "study", false, "Productivity"),
    SkillItem("chatgpt-image-gen", "chatgpt-image-gen", "Generates high quality artwork and illustrations from prompts", "art", true, "Creative")
)

data class ConnectorItem(
    val id: String,
    val name: String,
    val description: String,
    val category: String, // Files, Notes & Tasks, Messaging, Code, All
    val isConnected: Boolean,
    val tokenOrKey: String = ""
)

val DEFAULT_CONNECTORS = listOf(
    ConnectorItem("telegram", "Telegram", "Connect with BotFather token for bidirectional messaging", "Messaging", false),
    ConnectorItem("gdrive", "Google Drive", "Sync documents, backups and voice notes to Cloud", "Files", false),
    ConnectorItem("notion", "Notion", "Sync Maya memories, daily journal and task databases", "Notes & Tasks", false),
    ConnectorItem("github", "GitHub", "Inspect repositories, pull requests and trigger actions", "Code", false),
    ConnectorItem("todoist", "Todoist", "Manage personal task lists and voice-added todos", "Notes & Tasks", false),
    ConnectorItem("vercel", "Vercel", "Monitor deployments and build pipeline telemetry", "Code", false),
    ConnectorItem("linear", "Linear", "Sync engineering issues and sprint deliverables", "Notes & Tasks", false),
    ConnectorItem("gitlab", "GitLab", "Repository and CI pipeline integration", "Code", false)
)

data class DeviceActionLog(
    val id: String,
    val actionType: String,
    val detail: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSuccess: Boolean = true
)

data class UserSettings(
    val userName: String = "Rifat",
    val userGender: UserGender = UserGender.MALE,
    val phoneNumber: String = "+880 1700-000000",
    val musicService: MusicService = MusicService.YT_MUSIC,
    val favoriteSong: String = "Starboy - The Weeknd",
    val favoriteSinger: String = "The Weeknd",
    val omnirouteApiKey: String = "",
    val omnirouteBaseUrl: String = "http://localhost:8080/v1",
    val assistantName: String = "Maya",
    val persona: PersonaType = PersonaType.MAYA,
    val autoRemember: Boolean = true,
    val isIncognito: Boolean = false,
    val selectedVoiceCode: String = "Aoede",
    val voiceEngineMode: VoiceEngineMode = VoiceEngineMode.NORMAL,
    val voiceSpeedMode: VoiceSpeedMode = VoiceSpeedMode.SWIFT,
    val elevenLabsApiKey: String = "",
    val cloudTtsApiKey: String = "",
    val autoScrollReels: Boolean = false,
    val language: LanguageChoice = LanguageChoice.BANGLISH,
    val wakeWordAutoStart: Boolean = true,
    val proactiveMaya: Boolean = true,
    val sleepTimer: SleepTimerChoice = SleepTimerChoice.MIN_10,
    val announceIncomingCalls: Boolean = true,
    val loudspeakerOnPickup: Boolean = false,
    val keepRingtonePlaying: Boolean = true,
    val drivingMode: Boolean = false,
    // Email settings
    val emailAddress: String = "rifat@example.com",
    val emailAppPassword: String = "",
    val emailSenderName: String = "Rifat (via Maya)",
    val emailSignature: String = "Sent via Maya AI Assistant",
    val smtpHost: String = "smtp.gmail.com",
    val smtpPort: String = "587",
    // Messaging
    val alertNewMessages: Boolean = true,
    val readAloudWhenAsleep: Boolean = false,
    val announceWhatsApp: Boolean = true,
    // Social Media
    val socialHandle: String = "@rifat_ai",
    val socialTarget: String = "Instagram", // Instagram / Facebook
    val captionVoice: CaptionVoice = CaptionVoice.FUNNY,
    val dailyStoryEnabled: Boolean = true,
    val autoPostEnabled: Boolean = false,
    // Advanced & Device Control
    val accessibilityEnabled: Boolean = false,
    val screenAwarenessEnabled: Boolean = true,
    val batterySaver: Boolean = false,
    val keepHeyMayaInBatterySaver: Boolean = true,
    val keepScreenAwarenessInBatterySaver: Boolean = false,
    val keepVoiceGuardianInBatterySaver: Boolean = false,
    val orbStyle: String = "Cyberpunk Cyan", // Cyberpunk Cyan, Solar Gold, Nebula Violet, Emerald Pulse
    val orbSize: Float = 1.0f
)
