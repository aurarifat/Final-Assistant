package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun HomeQuickActions(
    onMusicClick: () -> Unit,
    onStudyClick: () -> Unit,
    onJournalClick: () -> Unit,
    onControlClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionButton(
            label = "Music",
            icon = Icons.Default.MusicNote,
            accentColor = CyberViolet,
            onClick = onMusicClick,
            modifier = Modifier.weight(1f).testTag("quick_action_music")
        )
        QuickActionButton(
            label = "Study",
            icon = Icons.Default.MenuBook,
            accentColor = CyberCyan,
            onClick = onStudyClick,
            modifier = Modifier.weight(1f).testTag("quick_action_study")
        )
        QuickActionButton(
            label = "Journal",
            icon = Icons.Default.EditNote,
            accentColor = CyberGold,
            onClick = onJournalClick,
            modifier = Modifier.weight(1f).testTag("quick_action_journal")
        )
        QuickActionButton(
            label = "Device",
            icon = Icons.Default.Smartphone,
            accentColor = CyberEmerald,
            onClick = onControlClick,
            modifier = Modifier.weight(1f).testTag("quick_action_device")
        )
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(58.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = CyberSurface,
        border = BorderStroke(1.dp, CyberSurfaceBorder.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun HomeInfoCards(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Weather Card
            InfoCard(
                title = "Weather",
                subtitle = "28°C • Partly Sunny",
                detail = "Dhaka • Humidity 62%",
                icon = Icons.Default.WbSunny,
                accentColor = CyberGold,
                modifier = Modifier.weight(1f).testTag("weather_info_card")
            )

            // Today Card
            InfoCard(
                title = "Today",
                subtitle = "Sunday, Oct 18",
                detail = "3 Agenda tasks scheduled",
                icon = Icons.Default.CalendarToday,
                accentColor = CyberCyan,
                modifier = Modifier.weight(1f).testTag("today_info_card")
            )
        }

        // Mood & AI status card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("mood_info_card"),
            shape = RoundedCornerShape(16.dp),
            color = CyberSurface,
            border = BorderStroke(1.dp, CyberSurfaceBorder.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CyberViolet.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Mood",
                        tint = CyberViolet,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Maya Mood Engine",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Focused & Attuned • Ready to Automate",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CyberEmerald.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "OPTIMAL",
                        color = CyberEmerald,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    subtitle: String,
    detail: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = CyberSurface,
        border = BorderStroke(1.dp, CyberSurfaceBorder.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = detail,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun AskMayaBox(
    onSend: (String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    var attachedFile by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Suggested prompt pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SuggestionChip(
                label = "🎵 Play Starboy",
                onClick = { text = "Play Starboy" }
            )
            SuggestionChip(
                label = "🧠 Remember note",
                onClick = { text = "Remember that I have a presentation tomorrow" }
            )
            SuggestionChip(
                label = "🎯 Open YouTube",
                onClick = { text = "Open YouTube" }
            )
        }

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CyberSurfaceVariant,
            border = BorderStroke(1.dp, CyberSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attachment Button
                IconButton(
                    onClick = {
                        attachedFile = if (attachedFile == null) "voice_note_telemetry.wav" else null
                    },
                    modifier = Modifier.testTag("attachment_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attach file",
                        tint = if (attachedFile != null) CyberCyan else TextSecondary
                    )
                }

                if (attachedFile != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CyberCyan.copy(alpha = 0.2f),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            text = "📎 Attached",
                            color = CyberCyan,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                TextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = {
                        Text(
                            "Ask Maya anything...",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ask_maya_input")
                )

                // Send Button
                IconButton(
                    onClick = {
                        if (text.isNotBlank() || attachedFile != null) {
                            onSend(text, attachedFile)
                            text = ""
                            attachedFile = null
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CyberCyan)
                        .testTag("send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CyberSurface,
        border = BorderStroke(1.dp, CyberSurfaceBorder.copy(alpha = 0.6f)),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}
