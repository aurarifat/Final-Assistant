package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.MayaViewModel
import com.example.viewmodel.SettingsSubPage

@Composable
fun SettingsScreen(
    viewModel: MayaViewModel,
    onRequestAllRuntime: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentSubPage by viewModel.settingsSubPage.collectAsState()
    val page = currentSubPage ?: SettingsSubPage.MAIN

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .statusBarsPadding()
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (page == SettingsSubPage.MAIN) {
                        viewModel.closeSettings()
                    } else {
                        viewModel.openSettings(SettingsSubPage.MAIN)
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CyberSurface)
                    .testTag("settings_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = CyberCyan
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = when (page) {
                    SettingsSubPage.MAIN -> "Settings & Control"
                    SettingsSubPage.PERMISSIONS -> "Permissions & Access Levels"
                    SettingsSubPage.TOOLS_HUB -> "Tools & System Hub"
                    SettingsSubPage.PERSONAL -> "Personal Profile & API"
                    SettingsSubPage.MAYA -> "Maya Personality & Voice"
                    SettingsSubPage.VOICE_ENGINE -> "Voice Engine & TTS"
                    SettingsSubPage.SKILLS -> "Skill Store & Modules"
                    SettingsSubPage.EMAIL -> "Email Configuration"
                    SettingsSubPage.MESSAGES -> "Messages & WhatsApp"
                    SettingsSubPage.SOCIAL -> "Social Media Automation"
                    SettingsSubPage.CONNECTORS -> "External Connectors"
                    SettingsSubPage.BACKUP -> "Backup & Memory Data"
                    SettingsSubPage.ADVANCED -> "Advanced Configuration"
                    SettingsSubPage.BATTERY_SAVER -> "Battery Saver Guard"
                },
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Divider(color = CyberSurfaceBorder.copy(alpha = 0.5f), thickness = 1.dp)

        Box(modifier = Modifier.weight(1f)) {
            when (page) {
                SettingsSubPage.MAIN -> MainSettingsMenu(viewModel)
                SettingsSubPage.PERMISSIONS -> PermissionsSettingsPage(viewModel, onRequestAllRuntime)
                SettingsSubPage.TOOLS_HUB -> ToolsHubSettingsPage(viewModel)
                SettingsSubPage.PERSONAL -> PersonalSettingsPage(viewModel)
                SettingsSubPage.MAYA -> MayaPersonalityPage(viewModel)
                SettingsSubPage.VOICE_ENGINE -> VoiceEngineSettingsPage(viewModel)
                SettingsSubPage.SKILLS -> SkillsSettingsPage(viewModel)
                SettingsSubPage.EMAIL -> EmailSettingsPage(viewModel)
                SettingsSubPage.MESSAGES -> MessagesSettingsPage(viewModel)
                SettingsSubPage.SOCIAL -> SocialMediaSettingsPage(viewModel)
                SettingsSubPage.CONNECTORS -> ConnectorsSettingsPage(viewModel)
                SettingsSubPage.BACKUP -> BackupSettingsPage(viewModel)
                SettingsSubPage.ADVANCED -> AdvancedSettingsPage(viewModel)
                SettingsSubPage.BATTERY_SAVER -> BatterySaverSettingsPage(viewModel)
            }
        }
    }
}

@Composable
private fun MainSettingsMenu(viewModel: MayaViewModel) {
    val scrollState = rememberScrollState()
    val permissionStatuses by viewModel.permissionStatuses.collectAsState()
    val grantedCount = permissionStatuses.count { it.value }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val isStorageGranted = permissionStatuses["storage_access"] == true
        val isAssistantGranted = permissionStatuses["default_assistant"] == true

        // High-Priority: Permissions & Access Levels
        SettingsMenuItem(
            icon = Icons.Default.Security,
            iconTint = CyberEmerald,
            title = "Permissions & App Access Levels",
            subtitle = "$grantedCount of ${com.example.manager.MayaPermissionManager.ALL_PERMISSIONS.size} Verified • Green Signal status & direct controls",
            onClick = { viewModel.openSettings(SettingsSubPage.PERMISSIONS) }
        )

        // Default Assistant App (Maya)
        SettingsMenuItem(
            icon = Icons.Default.RecordVoiceOver,
            iconTint = if (isAssistantGranted) CyberEmerald else CyberCyan,
            title = "Default Assistant App (Maya)",
            subtitle = if (isAssistantGranted) "Maya active as Android default voice assistant (Green Signal)" else "Tap to choose Maya as your system default assistant & voice app",
            onClick = {
                val item = com.example.manager.MayaPermissionManager.ALL_PERMISSIONS.find { it.id == "default_assistant" }
                if (item != null) viewModel.openPermissionSetting(item)
                else viewModel.openSettings(SettingsSubPage.PERMISSIONS)
            }
        )

        // Device Storage Access
        SettingsMenuItem(
            icon = Icons.Default.Folder,
            iconTint = if (isStorageGranted) CyberEmerald else CyberGold,
            title = "Device Storage & Files Access",
            subtitle = if (isStorageGranted) "Storage access granted (Green Signal) • All files & media accessible" else "Storage access not granted • Tap to grant All Files & Media access",
            onClick = {
                val item = com.example.manager.MayaPermissionManager.ALL_PERMISSIONS.find { it.id == "storage_access" }
                if (item != null) viewModel.openPermissionSetting(item)
                else viewModel.openSettings(SettingsSubPage.PERMISSIONS)
            }
        )

        // High-Priority: Tools & System Hub
        SettingsMenuItem(
            icon = Icons.Default.BuildCircle,
            iconTint = CyberCyan,
            title = "Tool Management & Execution Hub",
            subtitle = "60+ runnable system tools (Alarms, Wi-Fi, Volume, DND, SMS, Calendar, Memos)",
            onClick = { viewModel.openSettings(SettingsSubPage.TOOLS_HUB) }
        )

        SettingsMenuItem(
            icon = Icons.Default.Person,
            iconTint = CyberCyan,
            title = "Personal",
            subtitle = "Name, gender, phone, music service, Omniroute / Gemini API key",
            onClick = { viewModel.openSettings(SettingsSubPage.PERSONAL) }
        )

        SettingsMenuItem(
            icon = Icons.Default.SmartToy,
            iconTint = CyberViolet,
            title = "Maya Personality",
            subtitle = "Assistant name, Persona (Maya/Friday/Venom/Jarvis), 13 voices, sleep timer",
            onClick = { viewModel.openSettings(SettingsSubPage.MAYA) }
        )

        SettingsMenuItem(
            icon = Icons.Default.GraphicEq,
            iconTint = CyberPink,
            title = "Voice Engine",
            subtitle = "Normal vs Advanced Agentic, Swift vs Soul, Cloud TTS & ElevenLabs key",
            onClick = { viewModel.openSettings(SettingsSubPage.VOICE_ENGINE) }
        )

        SettingsMenuItem(
            icon = Icons.Default.Bolt,
            iconTint = CyberGold,
            title = "Skills & Skill Store",
            subtitle = "Install all, daily-briefing, whatsapp-pro, call-secretary, music-dj, etc.",
            onClick = { viewModel.openSettings(SettingsSubPage.SKILLS) }
        )

        SettingsMenuItem(
            icon = Icons.Default.Email,
            iconTint = CyberEmerald,
            title = "Email",
            subtitle = "SMTP server, app passwords, outgoing name, signature, test connection",
            onClick = { viewModel.openSettings(SettingsSubPage.EMAIL) }
        )

        SettingsMenuItem(
            icon = Icons.Default.Chat,
            iconTint = CyberCyan,
            title = "Messages & WhatsApp",
            subtitle = "Alert on new messages, read aloud when asleep, WhatsApp auto-reply",
            onClick = { viewModel.openSettings(SettingsSubPage.MESSAGES) }
        )

        SettingsMenuItem(
            icon = Icons.Default.Share,
            iconTint = CyberViolet,
            title = "Social Media",
            subtitle = "Instagram, Facebook, caption style (Funny/Simple/Motivational), daily story",
            onClick = { viewModel.openSettings(SettingsSubPage.SOCIAL) }
        )

        SettingsMenuItem(
            icon = Icons.Default.Extension,
            iconTint = CyberGold,
            title = "Connectors",
            subtitle = "Telegram bot token, Google Drive, GitHub, Notion, Linear, Todoist",
            onClick = { viewModel.openSettings(SettingsSubPage.CONNECTORS) }
        )

        SettingsMenuItem(
            icon = Icons.Default.Storage,
            iconTint = CyberEmerald,
            title = "Memory & Backup",
            subtitle = "Export JSON backup of all memories and restore existing archives",
            onClick = { viewModel.openSettings(SettingsSubPage.BACKUP) }
        )

        SettingsMenuItem(
            icon = Icons.Default.Tune,
            iconTint = CyberCyan,
            title = "Advanced",
            subtitle = "Theme styling, Orb appearance, system behavior, Voice Guardian",
            onClick = { viewModel.openSettings(SettingsSubPage.ADVANCED) }
        )

        SettingsMenuItem(
            icon = Icons.Default.BatteryChargingFull,
            iconTint = CyberEmerald,
            title = "Battery Saver",
            subtitle = "Low power toggles, keep wake word listening, screen awareness rules",
            onClick = { viewModel.openSettings(SettingsSubPage.BATTERY_SAVER) }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SettingsMenuItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CyberSurface,
        border = BorderStroke(1.dp, CyberSurfaceBorder.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
        }
    }
}
