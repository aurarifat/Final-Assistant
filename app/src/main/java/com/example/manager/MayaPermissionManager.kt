package com.example.manager

import android.Manifest
import android.app.NotificationManager
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.service.MayaAccessibilityService
import com.example.service.MayaNotificationListenerService

data class PermissionItem(
    val id: String,
    val name: String,
    val description: String,
    val isSpecial: Boolean = false,
    val intentAction: String? = null,
    val runtimePermissions: List<String> = emptyList()
)

object MayaPermissionManager {

    val ALL_PERMISSIONS = listOf(
        PermissionItem(
            id = "accessibility",
            name = "Accessibility Service",
            description = "Allows Maya to click, scroll, navigate screens, and automate UI gestures.",
            isSpecial = true,
            intentAction = Settings.ACTION_ACCESSIBILITY_SETTINGS
        ),
        PermissionItem(
            id = "notification_listener",
            name = "Notification Listener",
            description = "Allows Maya to announce WhatsApp/SMS alerts and read messages aloud.",
            isSpecial = true,
            intentAction = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
        ),
        PermissionItem(
            id = "overlay",
            name = "Overlay Window",
            description = "Displays floating controls, coordinate targeting pad, and status indicators.",
            isSpecial = true,
            intentAction = Settings.ACTION_MANAGE_OVERLAY_PERMISSION
        ),
        PermissionItem(
            id = "battery_opt",
            name = "Ignore Battery Optimization",
            description = "Ensures wake-word and background task execution without OS freezing.",
            isSpecial = true,
            intentAction = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
        ),
        PermissionItem(
            id = "write_settings",
            name = "Modify System Settings",
            description = "Enables Maya to adjust brightness, screen timeout, auto-rotation, and haptics.",
            isSpecial = true,
            intentAction = Settings.ACTION_MANAGE_WRITE_SETTINGS
        ),
        PermissionItem(
            id = "dnd",
            name = "Do Not Disturb Access",
            description = "Controls Do Not Disturb ringer modes and silent focus sessions.",
            isSpecial = true,
            intentAction = Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
        ),
        PermissionItem(
            id = "mic",
            name = "Microphone",
            description = "Voice recognition and Hey Maya wake word detection.",
            runtimePermissions = listOf(Manifest.permission.RECORD_AUDIO)
        ),
        PermissionItem(
            id = "camera",
            name = "Camera",
            description = "Taking photos and visual analysis tools.",
            runtimePermissions = listOf(Manifest.permission.CAMERA)
        ),
        PermissionItem(
            id = "contacts",
            name = "Contacts",
            description = "Listing and creating contacts, resolving names for calls.",
            runtimePermissions = listOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)
        ),
        PermissionItem(
            id = "calendar",
            name = "Calendar",
            description = "Reading, creating, and modifying calendar events.",
            runtimePermissions = listOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
        ),
        PermissionItem(
            id = "sms",
            name = "SMS",
            description = "Listing SMS and sending text messages.",
            runtimePermissions = listOf(Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS)
        ),
        PermissionItem(
            id = "call_log",
            name = "Call Log",
            description = "Reading call history for caller announcement & call secretary.",
            runtimePermissions = listOf(Manifest.permission.READ_CALL_LOG)
        ),
        PermissionItem(
            id = "location_fine",
            name = "Fine Location",
            description = "GPS location coordinates, weather discovery, and navigation.",
            runtimePermissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        ),
        PermissionItem(
            id = "location_bg",
            name = "Background Location",
            description = "Proactive location awareness and geofenced triggers.",
            runtimePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                listOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else emptyList()
        ),
        PermissionItem(
            id = "gallery",
            name = "Gallery Access",
            description = "Accessing device photos, media library, and screenshots.",
            runtimePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listOf(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        ),
        PermissionItem(
            id = "wifi_nearby",
            name = "Nearby Wi-Fi Devices",
            description = "Scanning and controlling Wi-Fi networks and peer connections.",
            runtimePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listOf(Manifest.permission.NEARBY_WIFI_DEVICES, Manifest.permission.ACCESS_WIFI_STATE)
            } else {
                listOf(Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE)
            }
        ),
        PermissionItem(
            id = "bt_scan",
            name = "Bluetooth Scan",
            description = "Discovering nearby accessories, headphones, and smart sensors.",
            runtimePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                listOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_ADMIN)
            } else {
                listOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
            }
        ),
        PermissionItem(
            id = "bt_connect",
            name = "Bluetooth Connect",
            description = "Pairing and maintaining audio and peripheral connectivity.",
            runtimePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                listOf(Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                listOf(Manifest.permission.BLUETOOTH)
            }
        ),
        PermissionItem(
            id = "default_assistant",
            name = "Default Assistant App (Maya)",
            description = "Set Maya as the Android default digital voice assistant and assist interaction app.",
            isSpecial = true,
            intentAction = Settings.ACTION_VOICE_INPUT_SETTINGS
        ),
        PermissionItem(
            id = "storage_access",
            name = "Device Storage & All Files Access",
            description = "Manage and read device files, documents, photos, media archives, and storage backups.",
            isSpecial = true,
            intentAction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
            } else {
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            },
            runtimePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
                )
            } else {
                listOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        )
    )

    fun isPermissionGranted(context: Context, item: PermissionItem): Boolean {
        return when (item.id) {
            "accessibility" -> MayaAccessibilityService.isServiceBound.value
            "notification_listener" -> {
                val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
                flat != null && flat.contains(context.packageName)
            }
            "overlay" -> {
                Settings.canDrawOverlays(context)
            }
            "battery_opt" -> {
                val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                pm?.isIgnoringBatteryOptimizations(context.packageName) ?: false
            }
            "write_settings" -> {
                Settings.System.canWrite(context)
            }
            "dnd" -> {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                nm?.isNotificationPolicyAccessGranted ?: false
            }
            "default_assistant" -> {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val rm = context.getSystemService(RoleManager::class.java)
                        if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_ASSISTANT)) {
                            if (rm.isRoleHeld(RoleManager.ROLE_ASSISTANT)) return true
                        }
                    }
                    val currentAssist = Settings.Secure.getString(context.contentResolver, "assistant")
                    val currentVoice = Settings.Secure.getString(context.contentResolver, "voice_interaction_service")
                    (currentAssist != null && currentAssist.contains(context.packageName)) ||
                    (currentVoice != null && currentVoice.contains(context.packageName))
                } catch (e: Exception) {
                    false
                }
            }
            "storage_access" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Environment.isExternalStorageManager() ||
                    (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) ||
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED)
                } else {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                }
            }
            else -> {
                if (item.runtimePermissions.isEmpty()) true
                else {
                    item.runtimePermissions.all { perm ->
                        ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
                    }
                }
            }
        }
    }

    fun getAllRuntimePermissions(): List<String> {
        val list = mutableListOf<String>()
        ALL_PERMISSIONS.forEach { item ->
            list.addAll(item.runtimePermissions)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return list.distinct()
    }

    fun openSpecialPermissionSetting(context: Context, item: PermissionItem) {
        if (item.id == "default_assistant") {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val rm = context.getSystemService(RoleManager::class.java)
                    if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_ASSISTANT) && !rm.isRoleHeld(RoleManager.ROLE_ASSISTANT)) {
                        val roleIntent = rm.createRequestRoleIntent(RoleManager.ROLE_ASSISTANT).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(roleIntent)
                        return
                    }
                } catch (e: Exception) {
                    // Fallback to voice input settings
                }
            }
            try {
                val intent = Intent(Settings.ACTION_VOICE_INPUT_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return
            } catch (e: Exception) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    return
                } catch (e2: Exception) {
                    // fallback below
                }
            }
        }

        if (item.id == "storage_access") {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    return
                } catch (e: Exception) {
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                        return
                    } catch (e2: Exception) {
                        // fallback below
                    }
                }
            }
        }

        val action = item.intentAction ?: Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        try {
            val intent = Intent(action).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                if (action == Settings.ACTION_APPLICATION_DETAILS_SETTINGS || action == Settings.ACTION_MANAGE_OVERLAY_PERMISSION || action == Settings.ACTION_MANAGE_WRITE_SETTINGS) {
                    data = Uri.parse("package:${context.packageName}")
                }
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(fallback)
        }
    }
}
