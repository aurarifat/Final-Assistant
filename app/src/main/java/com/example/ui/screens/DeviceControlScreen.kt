package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.MayaAccessibilityService
import com.example.ui.theme.*
import com.example.viewmodel.MayaViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DeviceControlScreen(
    viewModel: MayaViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.userSettings.collectAsState()
    val isServiceActive by MayaAccessibilityService.isServiceBound.collectAsState()
    val foregroundApp by MayaAccessibilityService.foregroundApp.collectAsState()
    val visibleElements by viewModel.visibleElements.collectAsState()
    val selectedCoords by viewModel.selectedCoordinates.collectAsState()
    val actionLogs by viewModel.deviceActionLogs.collectAsState()

    var manualX by remember { mutableStateOf("540") }
    var manualY by remember { mutableStateOf("960") }
    var targetLabel by remember { mutableStateOf("") }
    var targetAppName by remember { mutableStateOf("YouTube") }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Advanced Device Control",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Automated clicks, navigation & coordinate gestures",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isServiceActive) CyberEmerald.copy(alpha = 0.2f) else CyberGold.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, if (isServiceActive) CyberEmerald else CyberGold)
            ) {
                Text(
                    text = if (isServiceActive) "ACCESSIBILITY: ACTIVE" else "ACCESSIBILITY: STANDBY",
                    color = if (isServiceActive) CyberEmerald else CyberGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Accessibility Service Control Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CyberSurface,
            border = BorderStroke(1.dp, CyberSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Accessibility Control Engine",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Grants Maya permission to inspect on-screen buttons, dispatch gestures, and click on your behalf.",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = { viewModel.openAccessibilitySettings() },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("enable_accessibility_button")
                    ) {
                        Text(if (isServiceActive) "Manage" else "Enable", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 🧠 Screen Awareness Card
        Surface(
            shape = RoundedCornerShape(16.dp),
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
                        Icon(Icons.Default.Visibility, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Screen Awareness Telemetry",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = { viewModel.scanScreenElements() },
                        modifier = Modifier.size(32.dp).testTag("scan_screen_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Scan", tint = CyberCyan)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Foreground App: $foregroundApp",
                    color = CyberViolet,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (visibleElements.isNotEmpty()) {
                    Text(
                        text = "Detected UI Elements (${visibleElements.size}):",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 100.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        visibleElements.forEach { item ->
                            Text(
                                text = "• $item",
                                color = TextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Tap refresh to scan on-screen clickable buttons and text views.",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 🎯 Interactive Coordinate Control & Touch Pad
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CyberSurface,
            border = BorderStroke(1.dp, CyberSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AdsClick, contentDescription = null, tint = CyberGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🎯 Coordinate Control (X, Y)",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "When UI elements lack accessibility IDs or labels, Maya can tap by exact coordinate positions. Touch the pad below to pick coordinates or enter manually.",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Touch Canvas Pad
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberSurfaceVariant)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val scaledX = (offset.x / size.width) * 1080f
                                val scaledY = (offset.y / size.height) * 2400f
                                manualX = scaledX.toInt().toString()
                                manualY = scaledY.toInt().toString()
                                viewModel.setCoordinateSelection(scaledX, scaledY)
                            }
                        }
                        .testTag("coordinate_touch_pad"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val sel = selectedCoords
                        if (sel != null) {
                            val localX = (sel.first / 1080f) * size.width
                            val localY = (sel.second / 2400f) * size.height
                            drawCircle(color = CyberCyan, radius = 10f, center = Offset(localX, localY))
                            drawCircle(color = CyberCyan.copy(alpha = 0.3f), radius = 22f, center = Offset(localX, localY))
                        }
                    }
                    Text(
                        text = "Touch anywhere to target (X, Y)\nSelected: (${manualX}, ${manualY})",
                        color = TextSecondary.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Coordinate input row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = manualX,
                        onValueChange = { manualX = it },
                        label = { Text("X", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = manualY,
                        onValueChange = { manualY = it },
                        label = { Text("Y", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    Button(
                        onClick = {
                            val x = manualX.toFloatOrNull() ?: 540f
                            val y = manualY.toFloatOrNull() ?: 960f
                            viewModel.executeCoordinateTap(x, y)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberGold),
                        modifier = Modifier.height(48.dp).testTag("tap_xy_button")
                    ) {
                        Text("Tap (X,Y)", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val x = manualX.toFloatOrNull() ?: 540f
                            val y = manualY.toFloatOrNull() ?: 960f
                            viewModel.executeCoordinateSwipe(x, y + 400, x, y - 400)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Swipe Up (X,Y)", color = CyberCyan, fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = {
                            val x = manualX.toFloatOrNull() ?: 540f
                            val y = manualY.toFloatOrNull() ?: 960f
                            viewModel.executeCoordinateSwipe(x, y - 400, x, y + 400)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Swipe Down (X,Y)", color = CyberCyan, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // System Navigation & Quick Gesture Actions
        Text(
            text = "System Navigation Actions",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionTile(
                label = "Back",
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                onClick = { viewModel.executeDeviceAction("Back", "back") },
                modifier = Modifier.weight(1f).testTag("action_back_button")
            )
            ActionTile(
                label = "Home",
                icon = Icons.Default.Home,
                onClick = { viewModel.executeDeviceAction("Home", "home") },
                modifier = Modifier.weight(1f).testTag("action_home_button")
            )
            ActionTile(
                label = "Scroll Down",
                icon = Icons.Default.ArrowDownward,
                onClick = { viewModel.executeDeviceAction("Scroll Down", "scroll down") },
                modifier = Modifier.weight(1f).testTag("action_scroll_down_button")
            )
            ActionTile(
                label = "Scroll Up",
                icon = Icons.Default.ArrowUpward,
                onClick = { viewModel.executeDeviceAction("Scroll Up", "scroll up") },
                modifier = Modifier.weight(1f).testTag("action_scroll_up_button")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Click by Label & App Launcher
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CyberSurface,
            border = BorderStroke(1.dp, CyberSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Click by label
                Text(text = "Click by UI Label", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = targetLabel,
                        onValueChange = { targetLabel = it },
                        placeholder = { Text("e.g. Search, Submit, Settings", color = TextMuted, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f).height(54.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (targetLabel.isNotBlank()) {
                                viewModel.executeDeviceAction("Click Label", "click $targetLabel")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Click", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Launch App by Name
                Text(text = "Open App by Name", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = targetAppName,
                        onValueChange = { targetAppName = it },
                        placeholder = { Text("e.g. YouTube, Chrome, Spotify", color = TextMuted, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f).height(54.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (targetAppName.isNotBlank()) {
                                viewModel.executeDeviceAction("Open App", "open $targetAppName")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberViolet),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Open", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Automation Action Log
        Text(text = "Recent Execution Log", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))

        if (actionLogs.isEmpty()) {
            Text(text = "No actions dispatched yet. Test gestures above.", color = TextMuted, fontSize = 12.sp)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                actionLogs.take(5).forEach { log ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CyberSurface,
                        border = BorderStroke(1.dp, CyberSurfaceBorder.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = log.actionType, color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = log.detail, color = TextSecondary, fontSize = 11.sp)
                            }
                            Text(
                                text = SimpleDateFormat("h:mm:ss", Locale.getDefault()).format(Date(log.timestamp)),
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun ActionTile(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(64.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = CyberSurface,
        border = BorderStroke(1.dp, CyberSurfaceBorder.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = CyberCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = label, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
