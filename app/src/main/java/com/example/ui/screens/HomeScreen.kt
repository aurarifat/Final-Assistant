package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.manager.MayaPermissionManager
import com.example.model.MayaOrbState
import com.example.ui.components.AskMayaBox
import com.example.ui.components.HomeInfoCards
import com.example.ui.components.HomeQuickActions
import com.example.ui.components.MayaOrb
import com.example.ui.theme.*
import com.example.viewmodel.MainTab
import com.example.viewmodel.MayaViewModel
import com.example.viewmodel.SettingsSubPage

@Composable
fun HomeScreen(
    viewModel: MayaViewModel,
    modifier: Modifier = Modifier
) {
    val orbState by viewModel.orbState.collectAsState()
    val energy by viewModel.energyPercent.collectAsState()
    val settings by viewModel.userSettings.collectAsState()
    val statusMsg by viewModel.systemStatusMessage.collectAsState()
    val permissionStatuses by viewModel.permissionStatuses.collectAsState()

    val grantedCount = permissionStatuses.count { it.value }
    val totalCount = MayaPermissionManager.ALL_PERMISSIONS.size

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // ☰ Menu Button (Top Left)
            IconButton(
                onClick = { viewModel.openSettings(SettingsSubPage.MAIN) },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CyberSurface)
                    .testTag("menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu / Settings",
                    tint = CyberCyan,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Title & Status
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = settings.assistantName.uppercase(),
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "${settings.persona.displayName} Mode • ${settings.language.displayName.substringBefore(" ")}",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }

            // Right icons (🔔 Notification + 👤 Profile)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // 🔔 Notification Button
                IconButton(
                    onClick = { viewModel.toggleNotifications(true) },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CyberSurface)
                        .testTag("notification_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = CyberGold,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 👤 Maya / Profile Icon
                IconButton(
                    onClick = { viewModel.openSettings(SettingsSubPage.PERSONAL) },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CyberSurface)
                        .testTag("profile_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile & Personal Settings",
                        tint = CyberViolet,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Verified Green Signal Status Banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = CyberSurface,
            border = BorderStroke(1.dp, if (grantedCount == totalCount) CyberEmerald.copy(alpha = 0.6f) else CyberGold.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable { viewModel.openSettings(SettingsSubPage.PERMISSIONS) }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (grantedCount == totalCount) CyberEmerald else CyberGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$grantedCount/$totalCount Permissions Verified • Zero Interruption Mode",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CyberEmerald.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "GREEN SIGNAL",
                        color = CyberEmerald,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Scrollable content body
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            // Central Holographic Maya Orb
            MayaOrb(
                orbState = orbState,
                energyPercent = energy,
                statusText = statusMsg ?: "Ready for instructions",
                onOrbClick = { viewModel.toggleLiveVoice() },
                modifier = Modifier.padding(top = 4.dp)
            )

            // Home Quick Actions: Music, Study, Journal, Device
            HomeQuickActions(
                onMusicClick = { viewModel.triggerMusicQuickAction() },
                onStudyClick = { viewModel.triggerStudyQuickAction() },
                onJournalClick = { viewModel.triggerJournalQuickAction() },
                onControlClick = { viewModel.setTab(MainTab.CONTROL) }
            )

            // Home Information Cards (Weather, Today, Mood)
            HomeInfoCards()

            // Ask Maya Box with attachments and send button
            AskMayaBox(
                onSend = { prompt, attachment ->
                    viewModel.sendPrompt(prompt, attachment)
                    viewModel.setTab(MainTab.CHAT)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
