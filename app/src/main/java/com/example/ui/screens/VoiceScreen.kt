package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MayaOrbState
import com.example.model.VoiceEngineMode
import com.example.model.VoiceSpeedMode
import com.example.ui.theme.*
import com.example.viewmodel.MayaViewModel
import kotlin.math.sin

@Composable
fun VoiceScreen(
    viewModel: MayaViewModel,
    modifier: Modifier = Modifier
) {
    val orbState by viewModel.orbState.collectAsState()
    val settings by viewModel.userSettings.collectAsState()
    val statusMsg by viewModel.systemStatusMessage.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()

    val lastMayaMsg = chatMessages.lastOrNull { !it.isUser }?.text
        ?: "Ready to assist you, ${settings.userName}. Say anything or test commands."

    val infiniteTransition = rememberInfiniteTransition(label = "audio_visualizer")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    val micPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (orbState == MayaOrbState.LISTENING) 1.15f else 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .statusBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Voice Header Mode Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Swift vs Soul switcher
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CyberSurface,
                border = BorderStroke(1.dp, CyberSurfaceBorder)
            ) {
                Row(modifier = Modifier.padding(3.dp)) {
                    SpeedModeButton(
                        label = "Swift ⚡",
                        isSelected = settings.voiceSpeedMode == VoiceSpeedMode.SWIFT,
                        onClick = {
                            viewModel.updateSettings { it.copy(voiceSpeedMode = VoiceSpeedMode.SWIFT) }
                        }
                    )
                    SpeedModeButton(
                        label = "Soul 💙",
                        isSelected = settings.voiceSpeedMode == VoiceSpeedMode.SOUL,
                        onClick = {
                            viewModel.updateSettings { it.copy(voiceSpeedMode = VoiceSpeedMode.SOUL) }
                        }
                    )
                }
            }

            // Engine Mode Badge (Normal / Advanced Agentic)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CyberSurface,
                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f)),
                modifier = Modifier.clickable {
                    val next = if (settings.voiceEngineMode == VoiceEngineMode.NORMAL)
                        VoiceEngineMode.ADVANCED_AGENTIC else VoiceEngineMode.NORMAL
                    viewModel.updateSettings { it.copy(voiceEngineMode = next) }
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (settings.voiceEngineMode == VoiceEngineMode.NORMAL) CyberCyan else CyberViolet)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = settings.voiceEngineMode.displayName,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Persona & Voice Indicator
        Text(
            text = "${settings.assistantName} (${settings.persona.displayName})",
            color = CyberCyan,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Voice: ${settings.selectedVoiceCode} • ${settings.language.displayName}",
            color = TextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        // Soundwave Graphic Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val midY = size.height / 2f
                val count = 28
                val spacing = size.width / (count + 1)
                val isActive = orbState == MayaOrbState.LISTENING || orbState == MayaOrbState.SPEAKING

                for (i in 0 until count) {
                    val x = spacing * (i + 1)
                    val factor = if (isActive) {
                        (sin(wavePhase + i * 0.35) * 45f + 12f).toFloat().coerceIn(6f, 50f)
                    } else {
                        (sin(wavePhase * 0.3f + i * 0.2) * 8f + 10f).toFloat().coerceIn(4f, 15f)
                    }

                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(CyberCyan, CyberViolet)
                        ),
                        start = Offset(x, midY - factor),
                        end = Offset(x, midY + factor),
                        strokeWidth = 5f
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Transcription / Status Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CyberSurface,
            border = BorderStroke(1.dp, CyberSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (orbState) {
                        MayaOrbState.LISTENING -> "Listening to your voice..."
                        MayaOrbState.THINKING -> "Synthesizing contextual reasoning..."
                        MayaOrbState.SPEAKING -> "Maya is speaking..."
                        MayaOrbState.ASLEEP -> "Maya is asleep. Tap microphone or say \"Hey Maya\"."
                        MayaOrbState.READY -> "Ready for next voice instruction."
                    },
                    color = CyberCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"$lastMayaMsg\"",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Wake word reminder pill
        if (settings.wakeWordAutoStart) {
            Text(
                text = "Wake word listening active: \"Hey ${settings.assistantName}\"",
                color = TextMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Live Voice Assistant Always-On 4-Side Edge Glow Switch
        val isLiveActive by viewModel.isLiveVoiceActive.collectAsState()
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isLiveActive) CyberCyan.copy(alpha = 0.2f) else CyberSurface,
            border = BorderStroke(1.5.dp, if (isLiveActive) CyberCyan else CyberSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable { viewModel.toggleLiveVoice() }
                .testTag("toggle_live_voice_assistant_card")
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isLiveActive) CyberCyan else TextMuted)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isLiveActive) "LIVE VOICE ASSISTANT: ON" else "START LIVE VOICE ASSISTANT",
                            color = if (isLiveActive) CyberCyan else TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (isLiveActive) "4-Side screen edge glow active • Always on until turned off"
                            else "Click to activate screen 4-side edge glow & always-on listening",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
                Switch(
                    checked = isLiveActive,
                    onCheckedChange = { viewModel.toggleLiveVoice(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = CyberCyan
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Large Central Microphone Action Button
        Box(
            modifier = Modifier
                .scale(micPulse)
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = if (orbState == MayaOrbState.LISTENING) {
                            listOf(CyberCyan, CyberViolet)
                        } else {
                            listOf(CyberSurfaceVariant, Color(0xFF1E293B))
                        }
                    )
                )
                .clickable {
                    viewModel.toggleLiveVoice()
                }
                .testTag("voice_main_mic_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (orbState == MayaOrbState.LISTENING) Icons.Default.Mic else Icons.Default.MicNone,
                contentDescription = "Voice toggle",
                tint = if (orbState == MayaOrbState.LISTENING) Color.Black else CyberCyan,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick speech command buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            QuickVoiceChip("Play Music") { viewModel.triggerMusicQuickAction() }
            Spacer(modifier = Modifier.width(8.dp))
            QuickVoiceChip("Scroll Down") { viewModel.executeDeviceAction("Scroll Down", "scroll down") }
            Spacer(modifier = Modifier.width(8.dp))
            QuickVoiceChip("Go Home") { viewModel.executeDeviceAction("Go Home", "home") }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SpeedModeButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) CyberCyan else Color.Transparent,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.Black else TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun QuickVoiceChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = CyberSurfaceVariant,
        border = BorderStroke(1.dp, CyberSurfaceBorder.copy(alpha = 0.5f)),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}
