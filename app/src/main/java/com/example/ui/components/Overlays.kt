package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.manager.MayaPermissionManager
import com.example.ui.theme.*
import com.example.viewmodel.MayaViewModel
import com.example.viewmodel.SettingsSubPage

@Composable
fun NotificationsDialog(
    viewModel: MayaViewModel,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = CyberGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Maya Alerts & System", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AlertRow(
                    title = "Battery & Health",
                    detail = "System running at 98% efficiency. Thermal and cache limits optimal.",
                    time = "10m ago"
                )
                AlertRow(
                    title = "App Access Levels",
                    detail = "All 18 permissions managed. Green signal indicators verified.",
                    time = "1m ago"
                )
                AlertRow(
                    title = "Skill Store Update",
                    detail = "New autonomous workflow available: 'chatgpt-image-gen'.",
                    time = "1h ago"
                )
                AlertRow(
                    title = "Memory Synchronized",
                    detail = "3 on-device preferences locked to local database.",
                    time = "3h ago"
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = CyberCyan)
            }
        },
        containerColor = CyberSurface,
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
fun StartupPermissionDialog(
    viewModel: MayaViewModel,
    onRequestAllRuntime: () -> Unit,
    onDismiss: () -> Unit
) {
    val permissionStatuses by viewModel.permissionStatuses.collectAsState()
    val grantedCount = permissionStatuses.count { it.value }
    val totalCount = MayaPermissionManager.ALL_PERMISSIONS.size

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = CyberEmerald)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("App Access Levels Setup", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("$grantedCount of $totalCount Access Levels Verified", color = CyberEmerald, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Welcome to Maya AI. All permissions are configured on app start so that when you execute any task or automation, no permission will ever be requested during execution.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CyberSurfaceVariant,
                    border = BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("• Accessibility & Overlay: Clicks, gestures, screen awareness", color = TextPrimary, fontSize = 11.sp)
                        Text("• System Controls: Volume, DND, Wi-Fi, Bluetooth, Flashlight", color = TextPrimary, fontSize = 11.sp)
                        Text("• Communications: SMS, Contacts, Dial, Call logs", color = TextPrimary, fontSize = 11.sp)
                        Text("• Productivity: Calendar, To-dos, Workflows, Mail", color = TextPrimary, fontSize = 11.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onRequestAllRuntime()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Grant All Runtime", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.speakPermissionBriefing()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Voice Briefing", color = CyberCyan, fontSize = 10.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald)
            ) {
                Text("Proceed (Zero Interruption Mode)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = {
                viewModel.openSettings(SettingsSubPage.PERMISSIONS)
                onDismiss()
            }) {
                Text("View All in Settings", color = CyberCyan, fontSize = 11.sp)
            }
        },
        containerColor = CyberSurface,
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
private fun AlertRow(title: String, detail: String, time: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = CyberSurfaceVariant,
        border = BorderStroke(1.dp, CyberSurfaceBorder.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(text = time, color = TextMuted, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = detail, color = TextSecondary, fontSize = 11.sp)
        }
    }
}
