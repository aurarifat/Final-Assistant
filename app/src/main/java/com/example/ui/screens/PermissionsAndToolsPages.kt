package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.manager.MayaPermissionManager
import com.example.manager.PermissionItem
import com.example.ui.theme.*
import com.example.viewmodel.MayaViewModel

@Composable
fun PermissionsSettingsPage(
    viewModel: MayaViewModel,
    onRequestAllRuntime: () -> Unit
) {
    val permissionStatuses by viewModel.permissionStatuses.collectAsState()
    val context = LocalContext.current
    val grantedCount = permissionStatuses.count { it.value }
    val totalCount = MayaPermissionManager.ALL_PERMISSIONS.size

    var activeTab by remember { mutableStateOf(0) } // 0 = Permissions Status, 1 = Modify System Settings

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tab Switcher Header
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CyberSurface,
            border = BorderStroke(1.dp, CyberSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (activeTab == 0) CyberCyan else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeTab = 0 }
                        .testTag("tab_permissions_status")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = if (activeTab == 0) Color.Black else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Permissions ($grantedCount/$totalCount)",
                            color = if (activeTab == 0) Color.Black else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (activeTab == 1) CyberCyan else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeTab = 1 }
                        .testTag("tab_modify_system_settings")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = if (activeTab == 1) Color.Black else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Modify Settings",
                            color = if (activeTab == 1) Color.Black else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (activeTab == 0) {
            // VIEW 1: PERMISSIONS STATUS WITH VISUAL GREEN SIGNALS
            PermissionsStatusView(
                viewModel = viewModel,
                grantedCount = grantedCount,
                totalCount = totalCount,
                permissionStatuses = permissionStatuses,
                onRequestAllRuntime = onRequestAllRuntime
            )
        } else {
            // VIEW 2: DIRECT TOGGLES TO MODIFY SYSTEM SETTINGS
            ModifySystemSettingsView(viewModel = viewModel)
        }
    }
}

@Composable
private fun PermissionsStatusView(
    viewModel: MayaViewModel,
    grantedCount: Int,
    totalCount: Int,
    permissionStatuses: Map<String, Boolean>,
    onRequestAllRuntime: () -> Unit
) {
    val context = LocalContext.current

    // Pulsing animation for the green signal beacon
    val infiniteTransition = rememberInfiniteTransition(label = "green_signal_pulse")
    val beaconAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beacon"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Summary Green Signal Beacon Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CyberSurface,
            border = BorderStroke(1.dp, if (grantedCount == totalCount) CyberEmerald else CyberCyan.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Permission Verification Engine",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$grantedCount of $totalCount Access Levels Verified",
                            color = if (grantedCount == totalCount) CyberEmerald else CyberGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Visual Green Signal Beacon
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CyberEmerald.copy(alpha = 0.2f),
                        border = BorderStroke(1.5.dp, CyberEmerald.copy(alpha = beaconAlpha))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CyberEmerald)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "GREEN SIGNAL",
                                color = CyberEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Granted permissions show a visual green signal. All tools execute with zero runtime interruptions.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onRequestAllRuntime()
                            Toast.makeText(context, "Requesting runtime permissions...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("grant_all_runtime_button")
                    ) {
                        Text("Grant All Runtime", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.speakPermissionBriefing() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Voice Briefing", color = CyberCyan, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // All 18 Permissions List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(MayaPermissionManager.ALL_PERMISSIONS, key = { it.id }) { item ->
                val isGranted = permissionStatuses[item.id] ?: false
                PermissionCardItem(
                    item = item,
                    isGranted = isGranted,
                    onOpenSetting = {
                        viewModel.openPermissionSetting(item)
                    }
                )
            }
        }
    }
}

@Composable
private fun PermissionCardItem(
    item: PermissionItem,
    isGranted: Boolean,
    onOpenSetting: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = CyberSurface,
        border = BorderStroke(
            1.dp,
            if (isGranted) CyberEmerald.copy(alpha = 0.6f) else CyberSurfaceBorder.copy(alpha = 0.6f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (item.isSpecial) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyberViolet.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "SPECIAL",
                                color = CyberViolet,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.description,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (isGranted) {
                // VISUAL GREEN SIGNAL GRANTED
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CyberEmerald.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, CyberEmerald)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(CyberEmerald)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Granted",
                            tint = CyberEmerald,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "GRANTED",
                            color = CyberEmerald,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            } else {
                Button(
                    onClick = onOpenSetting,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
                    border = BorderStroke(1.dp, CyberGold),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "GRANT",
                        color = CyberGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ModifySystemSettingsView(viewModel: MayaViewModel) {
    val autoRotate by viewModel.autoRotate.collectAsState()
    val brightness by viewModel.brightness.collectAsState()
    val screenTimeout by viewModel.screenTimeout.collectAsState()
    val hapticFeedback by viewModel.hapticFeedback.collectAsState()
    val touchSounds by viewModel.touchSounds.collectAsState()
    val eyeComfort by viewModel.eyeComfort.collectAsState()
    val dndMode by viewModel.dndMode.collectAsState()
    val flashlight by viewModel.flashlight.collectAsState()
    val wifi by viewModel.wifi.collectAsState()
    val bluetooth by viewModel.bluetooth.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Direct System Controls",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        // 1. Brightness Slider
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = CyberSurface,
            border = BorderStroke(1.dp, CyberSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BrightnessMedium, contentDescription = null, tint = CyberGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Screen Brightness", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("$brightness%", color = CyberGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = brightness.toFloat(),
                    onValueChange = { viewModel.setBrightnessLevel(it.toInt()) },
                    valueRange = 5f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = CyberGold,
                        activeTrackColor = CyberGold,
                        inactiveTrackColor = CyberSurfaceVariant
                    )
                )
            }
        }

        // 2. Auto Rotate Toggle
        SettingToggleCard(
            title = "Auto-Rotate Screen",
            subtitle = "Automatically rotate screen orientation with sensor",
            icon = Icons.Default.ScreenRotation,
            iconTint = CyberCyan,
            isChecked = autoRotate,
            onCheckedChange = { viewModel.toggleAutoRotate(it) }
        )

        // 3. Eye Comfort (Night Mode) Toggle
        SettingToggleCard(
            title = "Eye Comfort (Night Light)",
            subtitle = "Warmer color temperature to reduce blue light eye strain",
            icon = Icons.Default.NightsStay,
            iconTint = CyberViolet,
            isChecked = eyeComfort,
            onCheckedChange = { viewModel.toggleEyeComfort(it) }
        )

        // 4. Screen Off Timeout
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = CyberSurface,
            border = BorderStroke(1.dp, CyberSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Screen Off Timeout", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(15 to "15s", 30 to "30s", 60 to "1m", 120 to "2m", 300 to "5m").forEach { (sec, label) ->
                        val isSel = screenTimeout == sec
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) CyberCyan else CyberSurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setScreenTimeoutSeconds(sec) }
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color.Black else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // 5. Flashlight Torch Toggle
        SettingToggleCard(
            title = "Flashlight Torch",
            subtitle = "Toggle hardware rear camera LED flashlight",
            icon = Icons.Default.FlashlightOn,
            iconTint = CyberGold,
            isChecked = flashlight,
            onCheckedChange = { viewModel.toggleFlashlight(it) }
        )

        // 6. Do Not Disturb (DND) Toggle
        SettingToggleCard(
            title = "Do Not Disturb (DND)",
            subtitle = "Mute calls, alerts and non-priority notifications",
            icon = Icons.Default.DoNotDisturbOn,
            iconTint = CyberPink,
            isChecked = dndMode,
            onCheckedChange = { viewModel.toggleDnd(it) }
        )

        // 7. Haptic Touch Feedback
        SettingToggleCard(
            title = "Haptic Touch Feedback",
            subtitle = "Vibration response on screen clicks & gestures",
            icon = Icons.Default.Vibration,
            iconTint = CyberEmerald,
            isChecked = hapticFeedback,
            onCheckedChange = { viewModel.toggleHapticFeedback(it) }
        )

        // 8. System Touch Sounds
        SettingToggleCard(
            title = "System Touch Sounds",
            subtitle = "Audio clicks when tapping on-screen buttons",
            icon = Icons.Default.VolumeUp,
            iconTint = CyberCyan,
            isChecked = touchSounds,
            onCheckedChange = { viewModel.toggleTouchSounds(it) }
        )

        // 9. Wi-Fi Connectivity
        SettingToggleCard(
            title = "Wi-Fi Connectivity",
            subtitle = "Wireless network scanning and connection state",
            icon = Icons.Default.Wifi,
            iconTint = CyberCyan,
            isChecked = wifi,
            onCheckedChange = { viewModel.toggleWifi(it) }
        )

        // 10. Bluetooth Radio
        SettingToggleCard(
            title = "Bluetooth Radio",
            subtitle = "Pairing with headphones and nearby accessories",
            icon = Icons.Default.Bluetooth,
            iconTint = CyberViolet,
            isChecked = bluetooth,
            onCheckedChange = { viewModel.toggleBluetooth(it) }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingToggleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = CyberSurface,
        border = BorderStroke(1.dp, CyberSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(text = subtitle, color = TextMuted, fontSize = 11.sp, lineHeight = 15.sp)
                }
            }
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = CyberCyan
                )
            )
        }
    }
}

@Composable
fun ToolsHubSettingsPage(viewModel: MayaViewModel) {
    val lastResult by viewModel.lastToolExecution.collectAsState()
    var selectedCategory by remember { mutableStateOf("A. System") }

    val categories = listOf(
        "A. System",
        "B. Screen & Apps",
        "C. Contacts & SMS",
        "D. Skills",
        "E. Mail",
        "F. Workflows",
        "G. Location",
        "H. Calendar",
        "I. Memos"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Tool Management & Execution Hub", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Execute any of the 60+ system tools with zero interruption", color = TextMuted, fontSize = 11.sp)

        Spacer(modifier = Modifier.height(10.dp))

        // Category Tab selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.take(5).forEach { cat ->
                val isSel = selectedCategory == cat
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSel) CyberCyan else CyberSurface,
                    modifier = Modifier.clickable { selectedCategory = cat }
                ) {
                    Text(
                        text = cat.substringBefore(" "),
                        color = if (isSel) Color.Black else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.drop(5).forEach { cat ->
                val isSel = selectedCategory == cat
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSel) CyberCyan else CyberSurface,
                    modifier = Modifier.clickable { selectedCategory = cat }
                ) {
                    Text(
                        text = cat,
                        color = if (isSel) Color.Black else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Live Output Banner
        if (lastResult != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CyberSurfaceVariant,
                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Last Tool: ${lastResult?.toolName}", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(lastResult?.summary ?: "", color = TextPrimary, fontSize = 11.sp)
                    if (!lastResult?.data.isNullOrBlank()) {
                        Text(lastResult?.data ?: "", color = TextSecondary, fontSize = 10.sp, maxLines = 4)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tool buttons for selected category
        val toolsForCat = when (selectedCategory) {
            "A. System" -> listOf(
                "alarm_set_alarm" to "Set 7:30 AM Alarm",
                "alarm_modify_alarm" to "Modify Alarm Time",
                "check_battery_status" to "Check Battery Status",
                "device_health" to "Device Health & Thermal",
                "device_memory_status" to "Check Memory & RAM",
                "device_storage_status" to "Check Storage Capacity",
                "check_date_and_time" to "Check Date and Time",
                "flashlight_toggle" to "Toggle Flashlight Torch",
                "flashlight_status" to "Check Flashlight Status",
                "foneclaw_permission_status" to "Audit 18 Permission Levels",
                "system_panel_open" to "Open System Notification Panel",
                "dnd_status" to "Check Do Not Disturb Status",
                "dnd_set_mode" to "Set DND Priority Mode",
                "volume_status" to "Check Audio Volume Levels",
                "volume_adjust_stream" to "Adjust Media Volume Up",
                "volume_set_all_streams_muted" to "Mute All Audio Streams",
                "wifi_status" to "Check Wi-Fi Status",
                "wifi_scan_networks" to "Scan Wi-Fi Networks",
                "bluetooth_state" to "Check Bluetooth State",
                "open_bluetooth" to "Open Bluetooth Settings",
                "bluetooth_nearby_devices" to "Scan Nearby Bluetooth",
                "system_settings_status" to "System Settings Status",
                "system_settings_set_brightness" to "Set Brightness (80%)",
                "system_settings_set_auto_rotate" to "Toggle Auto-Rotate",
                "system_settings_set_screen_timeout" to "Set 60s Screen Timeout"
            )
            "B. Screen & Apps" -> listOf(
                "Read the screen" to "Read On-Screen UI Elements",
                "cross_app_read_screen" to "Cross-App Foreground Read",
                "Take a screenshot" to "Take System Screenshot",
                "Open a screenshot" to "Open Screenshot Gallery",
                "Take a photo" to "Launch Camera Capture",
                "Open a photo" to "Open Gallery Photos",
                "get_installed_map_apps" to "Get Installed Map Apps",
                "scroll" to "Dispatch Scroll Gesture",
                "Tap on the screen" to "Dispatch Tap at (540, 960)"
            )
            "C. Contacts & SMS" -> listOf(
                "contacts_list" to "List Device Contacts",
                "contacts_create" to "Create New Contact",
                "call_log_list" to "List Recent Calls",
                "phone_dial" to "Open Phone Dialer",
                "sms_list" to "List Recent SMS Messages",
                "send_to_sms_message" to "Prepare SMS Message"
            )
            "D. Skills" -> listOf(
                "list_skills" to "List Available Skills",
                "add_skill" to "Add 'study-focus' Skill",
                "list_skill_proposals" to "List Skill Proposals",
                "scheduled_task_list" to "View Scheduled Background Tasks"
            )
            "E. Mail" -> listOf(
                "Check email accounts" to "Check Configured Mail Accounts",
                "Check email" to "Check Unread Emails",
                "Read email" to "Read Active Memo Email"
            )
            "F. Workflows" -> listOf(
                "Plan the next steps" to "Plan Next Steps for Task",
                "workflow_list" to "List Multi-Step Workflows"
            )
            "G. Location" -> listOf(
                "Get location" to "Get GPS Location Coordinates",
                "location_search_nearby_place" to "Search Nearby Places",
                "map_navigate" to "Initiate Turn-by-Turn Navigation"
            )
            "H. Calendar" -> listOf(
                "Check calendar events" to "Check Upcoming Calendar Events",
                "Create a calendar event" to "Create Calendar Event (+1 Hour)"
            )
            "I. Memos" -> listOf(
                "View to-dos" to "View Active To-dos",
                "Add to-do" to "Add To-do ('System review')"
            )
            else -> emptyList()
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(toolsForCat) { (code, label) ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CyberSurface,
                    border = BorderStroke(1.dp, CyberSurfaceBorder.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = label, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = code, color = TextMuted, fontSize = 10.sp)
                        }
                        Button(
                            onClick = { viewModel.runTool(code) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("RUN", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
