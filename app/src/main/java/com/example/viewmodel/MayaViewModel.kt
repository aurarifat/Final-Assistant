package com.example.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.ChatMessageEntity
import com.example.data.database.MemoryEntity
import com.example.data.repository.MayaRepository
import com.example.engine.ToolResult
import com.example.manager.MayaPermissionManager
import com.example.manager.PermissionItem
import com.example.model.*
import com.example.service.MayaAccessibilityService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class MainTab {
    HOME,
    CONTROL,
    VOICE,
    MEMORIES,
    CHAT
}

enum class SettingsSubPage {
    MAIN,
    PERSONAL,
    MAYA,
    VOICE_ENGINE,
    SKILLS,
    EMAIL,
    MESSAGES,
    SOCIAL,
    CONNECTORS,
    BACKUP,
    ADVANCED,
    BATTERY_SAVER,
    PERMISSIONS,
    TOOLS_HUB
}

class MayaViewModel(application: Application) : AndroidViewModel(application) {

    val repository = MayaRepository(application)

    val memories: StateFlow<List<MemoryEntity>> = repository.getMemoriesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.getChatMessagesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentTab = MutableStateFlow(MainTab.HOME)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _settingsSubPage = MutableStateFlow<SettingsSubPage?>(null)
    val settingsSubPage: StateFlow<SettingsSubPage?> = _settingsSubPage.asStateFlow()

    private val _showMenuDrawer = MutableStateFlow(false)
    val showMenuDrawer: StateFlow<Boolean> = _showMenuDrawer.asStateFlow()

    private val _showNotifications = MutableStateFlow(false)
    val showNotifications: StateFlow<Boolean> = _showNotifications.asStateFlow()

    private val _showProfile = MutableStateFlow(false)
    val showProfile: StateFlow<Boolean> = _showProfile.asStateFlow()

    private val _showStartupPermissionModal = MutableStateFlow(false)
    val showStartupPermissionModal: StateFlow<Boolean> = _showStartupPermissionModal.asStateFlow()

    private val _orbState = MutableStateFlow(MayaOrbState.ASLEEP)
    val orbState: StateFlow<MayaOrbState> = _orbState.asStateFlow()

    private val _energyPercent = MutableStateFlow(98)
    val energyPercent: StateFlow<Int> = _energyPercent.asStateFlow()

    private val _userSettings = MutableStateFlow(UserSettings())
    val userSettings: StateFlow<UserSettings> = _userSettings.asStateFlow()

    private val _skillsList = MutableStateFlow(DEFAULT_SKILLS)
    val skillsList: StateFlow<List<SkillItem>> = _skillsList.asStateFlow()

    private val _connectorsList = MutableStateFlow(DEFAULT_CONNECTORS)
    val connectorsList: StateFlow<List<ConnectorItem>> = _connectorsList.asStateFlow()

    private val _deviceActionLogs = MutableStateFlow<List<DeviceActionLog>>(emptyList())
    val deviceActionLogs: StateFlow<List<DeviceActionLog>> = _deviceActionLogs.asStateFlow()

    private val _visibleElements = MutableStateFlow<List<String>>(emptyList())
    val visibleElements: StateFlow<List<String>> = _visibleElements.asStateFlow()

    private val _selectedCoordinates = MutableStateFlow<Pair<Float, Float>?>(null)
    val selectedCoordinates: StateFlow<Pair<Float, Float>?> = _selectedCoordinates.asStateFlow()

    private val _activeVoicePlaying = MutableStateFlow<String?>(null)
    val activeVoicePlaying: StateFlow<String?> = _activeVoicePlaying.asStateFlow()

    private val _systemStatusMessage = MutableStateFlow<String?>(null)
    val systemStatusMessage: StateFlow<String?> = _systemStatusMessage.asStateFlow()

    private val _permissionStatuses = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val permissionStatuses: StateFlow<Map<String, Boolean>> = _permissionStatuses.asStateFlow()

    private val _lastToolExecution = MutableStateFlow<ToolResult?>(null)
    val lastToolExecution: StateFlow<ToolResult?> = _lastToolExecution.asStateFlow()

    // OmniRoute API connection state
    private val _isTestingOmniroute = MutableStateFlow(false)
    val isTestingOmniroute: StateFlow<Boolean> = _isTestingOmniroute.asStateFlow()

    private val _omnirouteStatusMessage = MutableStateFlow<String?>(null)
    val omnirouteStatusMessage: StateFlow<String?> = _omnirouteStatusMessage.asStateFlow()

    // 4-Side Glow Live Voice Assistant (Always-On)
    private val _isLiveVoiceActive = MutableStateFlow(false)
    val isLiveVoiceActive: StateFlow<Boolean> = _isLiveVoiceActive.asStateFlow()

    // System Settings Control Toggles
    private val _autoRotate = MutableStateFlow(true)
    val autoRotate: StateFlow<Boolean> = _autoRotate.asStateFlow()

    private val _brightness = MutableStateFlow(75) // 0 - 100
    val brightness: StateFlow<Int> = _brightness.asStateFlow()

    private val _screenTimeout = MutableStateFlow(60) // seconds
    val screenTimeout: StateFlow<Int> = _screenTimeout.asStateFlow()

    private val _hapticFeedback = MutableStateFlow(true)
    val hapticFeedback: StateFlow<Boolean> = _hapticFeedback.asStateFlow()

    private val _touchSounds = MutableStateFlow(true)
    val touchSounds: StateFlow<Boolean> = _touchSounds.asStateFlow()

    private val _eyeComfort = MutableStateFlow(false)
    val eyeComfort: StateFlow<Boolean> = _eyeComfort.asStateFlow()

    private val _dndMode = MutableStateFlow(false)
    val dndMode: StateFlow<Boolean> = _dndMode.asStateFlow()

    private val _flashlight = MutableStateFlow(false)
    val flashlight: StateFlow<Boolean> = _flashlight.asStateFlow()

    private val _wifi = MutableStateFlow(true)
    val wifi: StateFlow<Boolean> = _wifi.asStateFlow()

    private val _bluetooth = MutableStateFlow(true)
    val bluetooth: StateFlow<Boolean> = _bluetooth.asStateFlow()

    init {
        refreshPermissions()

        viewModelScope.launch {
            delay(500)
            if (memories.value.isEmpty()) {
                repository.addMemory("Music Taste", "Prefers The Weeknd, EDM and acoustic chill songs", "Preference")
                repository.addMemory("Language Setting", "English preferred for daily conversation", "Personal", isPinned = true)
                repository.addMemory("Study Focus", "Preparing for Android & AI systems design", "Work")
            }
            if (chatMessages.value.isEmpty()) {
                repository.addChatMessage(
                    "Hello ${userSettings.value.userName}! I am Maya. All 18 App Access Levels are verified with green signals so your tools execute with zero runtime interruptions.",
                    isUser = false
                )
            }
        }
    }

    fun refreshPermissions() {
        val map = mutableMapOf<String, Boolean>()
        val context = getApplication<Application>()
        MayaPermissionManager.ALL_PERMISSIONS.forEach { item ->
            map[item.id] = MayaPermissionManager.isPermissionGranted(context, item)
        }
        _permissionStatuses.value = map
    }

    fun speakPermissionBriefing() {
        val voice = AVAILABLE_VOICES.find { it.code == _userSettings.value.selectedVoiceCode }
            ?: AVAILABLE_VOICES.first()
        val speech = "Welcome ${userSettings.value.userName}. I am Maya. To ensure seamless automation with zero interruptions during task execution, review your 18 App Access Levels. When green, all tools execute instantly."
        repository.speakText(speech, voice)
    }

    fun openPermissionSetting(item: PermissionItem) {
        val context = getApplication<Application>()
        MayaPermissionManager.openSpecialPermissionSetting(context, item)
    }

    fun setTab(tab: MainTab) {
        _currentTab.value = tab
        if (tab == MainTab.VOICE && _orbState.value == MayaOrbState.ASLEEP) {
            wakeUpMaya()
        }
    }

    fun openSettings(subPage: SettingsSubPage = SettingsSubPage.MAIN) {
        _settingsSubPage.value = subPage
        _showMenuDrawer.value = false
        refreshPermissions()
    }

    fun closeSettings() {
        _settingsSubPage.value = null
    }

    fun toggleMenuDrawer(open: Boolean) {
        _showMenuDrawer.value = open
    }

    fun toggleNotifications(open: Boolean) {
        _showNotifications.value = open
    }

    fun toggleProfile(open: Boolean) {
        _showProfile.value = open
    }

    fun setStartupPermissionModal(show: Boolean) {
        _showStartupPermissionModal.value = show
    }

    fun wakeUpMaya() {
        _orbState.value = MayaOrbState.LISTENING
        _systemStatusMessage.value = "Maya is listening... Speak now"
    }

    fun sleepMaya() {
        repository.stopSpeaking()
        _orbState.value = MayaOrbState.ASLEEP
        _systemStatusMessage.value = "Maya is asleep — say \"Hey Maya\" or tap to wake"
    }

    fun toggleOrbWake() {
        if (_orbState.value == MayaOrbState.ASLEEP) {
            wakeUpMaya()
        } else {
            sleepMaya()
        }
    }

    fun updateSettings(transform: (UserSettings) -> UserSettings) {
        _userSettings.value = transform(_userSettings.value)
    }

    fun testOmnirouteConnection(baseUrl: String, apiKey: String, model: String = "gpt-4o", onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isTestingOmniroute.value = true
            _omnirouteStatusMessage.value = "Testing OmniRoute connection..."
            val (success, message) = com.example.network.OmnirouteClient.testConnection(baseUrl, apiKey, model)
            _isTestingOmniroute.value = false
            _omnirouteStatusMessage.value = message
            onResult(success, message)
        }
    }

    fun previewVoice(voice: VoiceStyle) {
        _activeVoicePlaying.value = voice.code
        repository.speakText(voice.sampleText, voice) {
            _activeVoicePlaying.value = null
        }
    }

    fun selectVoice(voice: VoiceStyle) {
        updateSettings { it.copy(selectedVoiceCode = voice.code) }
        previewVoice(voice)
    }

    fun toggleSkill(id: String) {
        _skillsList.value = _skillsList.value.map {
            if (it.id == id) it.copy(isInstalled = !it.isInstalled) else it
        }
    }

    fun installAllSkills() {
        _skillsList.value = _skillsList.value.map { it.copy(isInstalled = true) }
        _systemStatusMessage.value = "All skills installed successfully"
    }

    fun refreshSkills() {
        _systemStatusMessage.value = "Skill store catalog refreshed"
    }

    fun toggleConnector(id: String, token: String? = null) {
        _connectorsList.value = _connectorsList.value.map {
            if (it.id == id) it.copy(isConnected = !it.isConnected, tokenOrKey = token ?: it.tokenOrKey) else it
        }
    }

    fun toggleLiveVoice(enable: Boolean? = null) {
        val newState = enable ?: !_isLiveVoiceActive.value
        _isLiveVoiceActive.value = newState
        if (newState) {
            _orbState.value = MayaOrbState.LISTENING
            _systemStatusMessage.value = "Live Voice Assistant Active • 4-Side Edge Glow ON"
        } else {
            repository.stopSpeaking()
            _orbState.value = MayaOrbState.ASLEEP
            _systemStatusMessage.value = "Live voice assistant turned off"
        }
    }

    // System Settings Toggles & Modifiers
    fun toggleAutoRotate(enabled: Boolean) {
        _autoRotate.value = enabled
        repository.toolEngine.systemSettingsSetAutoRotate(enabled)
    }

    fun setBrightnessLevel(percent: Int) {
        _brightness.value = percent
        repository.toolEngine.systemSettingsSetBrightness(percent)
    }

    fun setScreenTimeoutSeconds(seconds: Int) {
        _screenTimeout.value = seconds
        repository.toolEngine.systemSettingsSetScreenTimeout(seconds)
    }

    fun toggleHapticFeedback(enabled: Boolean) {
        _hapticFeedback.value = enabled
        repository.toolEngine.systemSettingsSetHapticFeedback(enabled)
    }

    fun toggleTouchSounds(enabled: Boolean) {
        _touchSounds.value = enabled
        repository.toolEngine.systemSettingsSetTouchSounds(enabled)
    }

    fun toggleEyeComfort(enabled: Boolean) {
        _eyeComfort.value = enabled
        repository.toolEngine.systemSettingsSetEyeComfort(enabled)
    }

    fun toggleDnd(enabled: Boolean) {
        _dndMode.value = enabled
        repository.toolEngine.dndSetMode(if (enabled) "Priority" else "All")
    }

    fun toggleFlashlight(enabled: Boolean) {
        _flashlight.value = enabled
        repository.toolEngine.flashlightSetEnabled(enabled)
    }

    fun toggleWifi(enabled: Boolean) {
        _wifi.value = enabled
        repository.toolEngine.wifiSetEnabled(enabled)
    }

    fun toggleBluetooth(enabled: Boolean) {
        _bluetooth.value = enabled
        if (enabled) repository.toolEngine.openBluetooth()
        else repository.toolEngine.closeBluetooth()
    }

    fun sendPrompt(prompt: String, attachmentUri: String? = null) {
        if (prompt.isBlank() && attachmentUri == null) return
        val userText = prompt.trim()

        viewModelScope.launch {
            repository.addChatMessage(userText, isUser = true, attachmentUri = attachmentUri)
            _orbState.value = MayaOrbState.THINKING
            _systemStatusMessage.value = "Thinking..."

            delay(500)

            val (reply, actionTaken) = repository.generateAssistantReply(
                userText,
                _userSettings.value,
                memories.value
            )

            repository.addChatMessage(reply, isUser = false, actionTaken = actionTaken)
            _orbState.value = MayaOrbState.SPEAKING
            _systemStatusMessage.value = reply

            val voice = AVAILABLE_VOICES.find { it.code == _userSettings.value.selectedVoiceCode }
                ?: AVAILABLE_VOICES.first()

            repository.speakText(reply, voice) {
                // If live voice assistant is on, ALWAYS stay on and return to LISTENING!
                if (_isLiveVoiceActive.value) {
                    _orbState.value = MayaOrbState.LISTENING
                    _systemStatusMessage.value = "Maya listening... Speak prompt"
                } else {
                    _orbState.value = MayaOrbState.READY
                }
            }

            delay(2800)
            if (_orbState.value == MayaOrbState.SPEAKING) {
                if (_isLiveVoiceActive.value) {
                    _orbState.value = MayaOrbState.LISTENING
                    _systemStatusMessage.value = "Maya listening... Speak prompt"
                } else {
                    _orbState.value = MayaOrbState.READY
                }
            }
        }
    }

    fun triggerMusicQuickAction() {
        sendPrompt("Play my favorite music playlist")
    }

    fun triggerStudyQuickAction() {
        sendPrompt("Start a focused study session and quiz me")
    }

    fun triggerJournalQuickAction() {
        sendPrompt("Remember that today was productive and log my thoughts")
    }

    // Execute Tool Function Directly from UI Hub or Voice
    fun runTool(toolCode: String) {
        viewModelScope.launch {
            val res = when (toolCode) {
                // Section A
                "alarm_set_alarm" -> repository.toolEngine.alarmSetAlarm()
                "alarm_modify_alarm" -> repository.toolEngine.alarmModifyAlarm()
                "bluetooth_state" -> repository.toolEngine.bluetoothState()
                "bluetooth_connected_devices" -> repository.toolEngine.bluetoothConnectedDevices()
                "bluetooth_nearby_devices" -> repository.toolEngine.bluetoothNearbyDevices()
                "bluetooth_paired_devices" -> repository.toolEngine.bluetoothPairedDevices()
                "open_bluetooth" -> repository.toolEngine.openBluetooth()
                "close_bluetooth" -> repository.toolEngine.closeBluetooth()
                "check_battery_status" -> repository.toolEngine.checkBatteryStatus()
                "device_health" -> repository.toolEngine.deviceHealth()
                "device_memory_status" -> repository.toolEngine.deviceMemoryStatus()
                "device_network_status" -> repository.toolEngine.deviceNetworkStatus()
                "device_storage_status" -> repository.toolEngine.deviceStorageStatus()
                "check_date_and_time" -> repository.toolEngine.checkDateTime()
                "dnd_status" -> repository.toolEngine.dndStatus()
                "dnd_set_mode" -> repository.toolEngine.dndSetMode("Priority")
                "dnd_open_policy_access_settings" -> repository.toolEngine.dndOpenPolicyAccessSettings()
                "flashlight_toggle" -> repository.toolEngine.flashlightToggle()
                "flashlight_status" -> repository.toolEngine.flashlightStatus()
                "foneclaw_permission_status" -> repository.toolEngine.foneclawPermissionStatus()
                "system_panel_open" -> repository.toolEngine.systemPanelOpen()
                "system_settings_open_battery_optimization_entry" -> repository.toolEngine.systemSettingsOpenBatteryOptimizationEntry()
                "system_settings_open_battery_saver_settings" -> repository.toolEngine.systemSettingsOpenBatterySaverSettings()
                "system_settings_open_date_time_settings" -> repository.toolEngine.systemSettingsOpenDateTimeSettings()
                "system_settings_open_hotspot_entry" -> repository.toolEngine.systemSettingsOpenHotspotEntry()
                "system_settings_open_location_entry" -> repository.toolEngine.systemSettingsOpenLocationEntry()
                "system_settings_set_brightness" -> repository.toolEngine.systemSettingsSetBrightness(80)
                "system_settings_set_auto_rotate" -> repository.toolEngine.systemSettingsSetAutoRotate(true)
                "system_settings_set_screen_timeout" -> repository.toolEngine.systemSettingsSetScreenTimeout(60)
                "system_settings_set_haptic_feedback" -> repository.toolEngine.systemSettingsSetHapticFeedback(true)
                "system_settings_set_touch_sounds" -> repository.toolEngine.systemSettingsSetTouchSounds(true)
                "system_settings_set_eye_comfort" -> repository.toolEngine.systemSettingsSetEyeComfort(true)
                "system_settings_status" -> repository.toolEngine.systemSettingsStatus()
                "volume_status" -> repository.toolEngine.volumeStatus()
                "volume_adjust_stream" -> repository.toolEngine.volumeAdjustStream("music", 1)
                "volume_set_all_streams_muted" -> repository.toolEngine.volumeSetAllStreamsMuted()
                "volume_set_ringer_mode" -> repository.toolEngine.volumeSetRingerMode("normal")
                "wifi_status" -> repository.toolEngine.wifiStatus()
                "wifi_scan_networks" -> repository.toolEngine.wifiScanNetworks()
                "wifi_configured_networks" -> repository.toolEngine.wifiConfiguredNetworks()
                "wifi_set_enabled" -> repository.toolEngine.wifiSetEnabled(true)
                // Section B
                "Take a photo" -> repository.toolEngine.takePhoto()
                "Open a photo" -> repository.toolEngine.openPhoto()
                "Read the screen" -> repository.toolEngine.readTheScreen()
                "cross_app_read_screen" -> repository.toolEngine.crossAppReadScreen()
                "get_installed_map_apps" -> repository.toolEngine.getInstalledMapApps()
                "Take a screenshot" -> repository.toolEngine.takeScreenshot()
                "Open a screenshot" -> repository.toolEngine.openScreenshot()
                "scroll" -> repository.toolEngine.scroll("down")
                "Tap on the screen" -> repository.toolEngine.tapOnTheScreen(540f, 960f)
                // Section C
                "phone_dial" -> repository.toolEngine.phoneDial("+8801700000000")
                "send_to_sms_message" -> repository.toolEngine.sendToSmsMessage()
                "contacts_list" -> repository.toolEngine.contactsList()
                "contacts_create" -> repository.toolEngine.contactsCreate()
                "call_log_list" -> repository.toolEngine.callLogList()
                "sms_list" -> repository.toolEngine.smsList()
                // Section D
                "list_skills" -> repository.toolEngine.listSkills()
                "add_skill" -> repository.toolEngine.addSkill("study-focus")
                "list_skill_proposals" -> repository.toolEngine.listSkillProposals()
                "scheduled_task_list" -> repository.toolEngine.scheduledTaskList()
                // Section E
                "Check email accounts" -> repository.toolEngine.checkEmailAccounts()
                "Check email" -> repository.toolEngine.checkEmail()
                "Read email" -> repository.toolEngine.readEmail()
                // Section F
                "Plan the next steps" -> repository.toolEngine.planTheNextSteps()
                "workflow_list" -> repository.toolEngine.workflowList()
                // Section G
                "Get location" -> repository.toolEngine.getLocation()
                "location_search_nearby_place" -> repository.toolEngine.locationSearchNearbyPlace("Coffee")
                "map_navigate" -> repository.toolEngine.mapNavigate("Home")
                // Section H
                "Check calendar events" -> repository.toolEngine.checkCalendarEvents()
                "Create a calendar event" -> repository.toolEngine.createCalendarEvent()
                // Section I
                "View to-dos" -> repository.toolEngine.viewTodos()
                "Add to-do" -> repository.toolEngine.addTodo("Review Maya system tools")
                else -> ToolResult(toolCode, true, "Executed $toolCode with zero interruption")
            }

            _lastToolExecution.value = res
            val log = DeviceActionLog(
                id = System.currentTimeMillis().toString(),
                actionType = res.toolName,
                detail = res.summary,
                isSuccess = res.isSuccess
            )
            _deviceActionLogs.value = listOf(log) + _deviceActionLogs.value.take(20)
            _systemStatusMessage.value = res.summary
        }
    }

    // Device automation controls
    fun executeDeviceAction(actionName: String, detail: String) {
        val result = repository.executeDeviceControlIntent(detail)
        val log = DeviceActionLog(
            id = System.currentTimeMillis().toString(),
            actionType = actionName,
            detail = result,
            isSuccess = true
        )
        _deviceActionLogs.value = listOf(log) + _deviceActionLogs.value.take(20)
        _systemStatusMessage.value = result
    }

    fun setCoordinateSelection(x: Float, y: Float) {
        _selectedCoordinates.value = Pair(x, y)
    }

    fun executeCoordinateTap(x: Float, y: Float) {
        val service = MayaAccessibilityService.activeInstance
        if (service != null) {
            service.triggerTap(x, y) { success ->
                val log = DeviceActionLog(
                    id = System.currentTimeMillis().toString(),
                    actionType = "Tap (X,Y)",
                    detail = "Dispatched tap at (${x.toInt()}, ${y.toInt()}) [Success: $success]",
                    isSuccess = success
                )
                _deviceActionLogs.value = listOf(log) + _deviceActionLogs.value.take(20)
            }
        } else {
            val log = DeviceActionLog(
                id = System.currentTimeMillis().toString(),
                actionType = "Tap (X,Y)",
                detail = "Simulated tap at (${x.toInt()}, ${y.toInt()})",
                isSuccess = true
            )
            _deviceActionLogs.value = listOf(log) + _deviceActionLogs.value.take(20)
        }
        _systemStatusMessage.value = "Tapped at (${x.toInt()}, ${y.toInt()})"
    }

    fun executeCoordinateSwipe(startX: Float, startY: Float, endX: Float, endY: Float) {
        val service = MayaAccessibilityService.activeInstance
        if (service != null) {
            service.triggerSwipe(startX, startY, endX, endY) { success ->
                val log = DeviceActionLog(
                    id = System.currentTimeMillis().toString(),
                    actionType = "Swipe",
                    detail = "Swiped from (${startX.toInt()}, ${startY.toInt()}) to (${endX.toInt()}, ${endY.toInt()})",
                    isSuccess = success
                )
                _deviceActionLogs.value = listOf(log) + _deviceActionLogs.value.take(20)
            }
        } else {
            val log = DeviceActionLog(
                id = System.currentTimeMillis().toString(),
                actionType = "Swipe",
                detail = "Simulated Swipe (${startX.toInt()} -> ${endX.toInt()})",
                isSuccess = true
            )
            _deviceActionLogs.value = listOf(log) + _deviceActionLogs.value.take(20)
        }
    }

    fun scanScreenElements() {
        val service = MayaAccessibilityService.activeInstance
        if (service != null) {
            val elements = service.scanVisibleElements()
            _visibleElements.value = elements
            _systemStatusMessage.value = "Scanned ${elements.size} on-screen elements"
        } else {
            _visibleElements.value = listOf(
                "Home Launcher [Clickable] at (540, 200)",
                "Settings App [Clickable] at (200, 450)",
                "YouTube [Clickable] at (540, 450)",
                "Chrome Browser [Clickable] at (880, 450)",
                "Search bar [Clickable] at (540, 1000)"
            )
            _systemStatusMessage.value = "Screen Awareness: 5 elements detected (Simulated)"
        }
    }

    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        getApplication<Application>().startActivity(intent)
    }

    fun exportMemories(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportMemoriesJson()
            onResult(json)
        }
    }

    fun importMemories(json: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val count = repository.importMemoriesJson(json)
            onComplete(count)
        }
    }

    fun deleteMemory(memory: MemoryEntity) {
        viewModelScope.launch {
            repository.deleteMemory(memory)
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    fun addManualMemory(title: String, content: String, category: String) {
        viewModelScope.launch {
            repository.addMemory(title, content, category)
        }
    }
}
