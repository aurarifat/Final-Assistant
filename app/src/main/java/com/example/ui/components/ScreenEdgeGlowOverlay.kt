package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MayaOrbState
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ScreenEdgeGlowOverlay(
    isActive: Boolean,
    orbState: MayaOrbState,
    statusText: String,
    onTurnOff: () -> Unit,
    onQuickPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val infiniteTransition = rememberInfiniteTransition(label = "edge_glow_transition")

    // Pulsing intensity for border thickness & glow radius
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = when (orbState) {
            MayaOrbState.LISTENING -> 0.65f
            MayaOrbState.SPEAKING -> 0.85f
            MayaOrbState.THINKING -> 0.70f
            else -> 0.50f
        },
        targetValue = when (orbState) {
            MayaOrbState.LISTENING -> 1.0f
            MayaOrbState.SPEAKING -> 1.15f
            MayaOrbState.THINKING -> 0.95f
            else -> 0.80f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (orbState) {
                    MayaOrbState.SPEAKING -> 600
                    MayaOrbState.LISTENING -> 1100
                    MayaOrbState.THINKING -> 750
                    else -> 1200
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    // Moving phase along the 4 borders
    val borderPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (orbState == MayaOrbState.SPEAKING) 1400 else 3200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "border_phase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("screen_4_side_edge_glow_overlay")
    ) {
        // 4-Side Perimeter Glow Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val primaryColor: Color
            val secondaryColor: Color
            val edgeThickness: Float

            when (orbState) {
                MayaOrbState.SPEAKING -> {
                    primaryColor = Color(0xFFD946EF) // Electric Magenta
                    secondaryColor = Color(0xFFA855F7) // Violet
                    edgeThickness = 32f * glowPulse
                }
                MayaOrbState.THINKING -> {
                    primaryColor = Color(0xFFFFB800) // Solar Gold
                    secondaryColor = Color(0xFFF97316) // Amber
                    edgeThickness = 24f * glowPulse
                }
                else -> { // LISTENING / ALWAYS ON
                    primaryColor = Color(0xFF00F0FF) // Cyber Cyan
                    secondaryColor = Color(0xFF38BDF8) // Electric Blue
                    edgeThickness = 26f * glowPulse
                }
            }

            // Top Edge Glow
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.85f * glowPulse), primaryColor.copy(alpha = 0.25f), Color.Transparent),
                    startY = 0f,
                    endY = edgeThickness * 3.5f
                ),
                topLeft = Offset(0f, 0f),
                size = Size(w, edgeThickness * 3.5f)
            )

            // Bottom Edge Glow
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, secondaryColor.copy(alpha = 0.25f), secondaryColor.copy(alpha = 0.85f * glowPulse)),
                    startY = h - (edgeThickness * 3.5f),
                    endY = h
                ),
                topLeft = Offset(0f, h - (edgeThickness * 3.5f)),
                size = Size(w, edgeThickness * 3.5f)
            )

            // Left Edge Glow
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.80f * glowPulse), primaryColor.copy(alpha = 0.20f), Color.Transparent),
                    startX = 0f,
                    endX = edgeThickness * 3.0f
                ),
                topLeft = Offset(0f, 0f),
                size = Size(edgeThickness * 3.0f, h)
            )

            // Right Edge Glow
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, secondaryColor.copy(alpha = 0.20f), secondaryColor.copy(alpha = 0.80f * glowPulse)),
                    startX = w - (edgeThickness * 3.0f),
                    endX = w
                ),
                topLeft = Offset(w - (edgeThickness * 3.0f), 0f),
                size = Size(edgeThickness * 3.0f, h)
            )

            // Outer Neon Border Stroke
            drawRoundRect(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        primaryColor,
                        secondaryColor,
                        primaryColor,
                        secondaryColor,
                        primaryColor
                    ),
                    center = Offset(w / 2f, h / 2f)
                ),
                size = size,
                style = Stroke(width = edgeThickness * 0.45f)
            )

            // Orbiting cyber light nodes travelling along perimeter
            val perimeter = 2 * (w + h)
            val numParticles = if (orbState == MayaOrbState.SPEAKING) 8 else 4
            for (i in 0 until numParticles) {
                val fraction = ((borderPhase / 6.283f) + (i.toFloat() / numParticles)) % 1.0f
                val distance = fraction * perimeter
                val pos = when {
                    distance < w -> Offset(distance, 4f)
                    distance < w + h -> Offset(w - 4f, distance - w)
                    distance < 2 * w + h -> Offset(w - (distance - (w + h)), h - 4f)
                    else -> Offset(4f, h - (distance - (2 * w + h)))
                }
                drawCircle(
                    color = Color.White,
                    radius = (4.5f * glowPulse),
                    center = pos
                )
                drawCircle(
                    color = primaryColor.copy(alpha = 0.7f),
                    radius = (9f * glowPulse),
                    center = pos
                )
            }
        }

        // Floating Voice Control HUD Banner (Non-blocking bottom overlay)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 72.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = CyberSurface.copy(alpha = 0.95f),
                border = BorderStroke(
                    1.5.dp,
                    when (orbState) {
                        MayaOrbState.SPEAKING -> Color(0xFFD946EF)
                        MayaOrbState.THINKING -> CyberGold
                        else -> CyberCyan
                    }
                ),
                shadowElevation = 12.dp,
                modifier = Modifier.fillMaxWidth().testTag("live_voice_floating_hud")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header row: Status + Always On Indicator + Turn Off Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (orbState) {
                                            MayaOrbState.SPEAKING -> Color(0xFFD946EF)
                                            MayaOrbState.THINKING -> CyberGold
                                            else -> CyberCyan
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (orbState) {
                                    MayaOrbState.SPEAKING -> "MAYA SPEAKING"
                                    MayaOrbState.THINKING -> "THINKING & REASONING"
                                    else -> "LIVE VOICE ASSISTANT • ALWAYS ON"
                                },
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        // Manual Turn Off Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CyberPink.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, CyberPink),
                            modifier = Modifier
                                .clickable(onClick = onTurnOff)
                                .testTag("turn_off_live_voice_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PowerSettingsNew, contentDescription = "Turn Off", tint = CyberPink, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Turn Off", color = CyberPink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Live Speech / Transcription line
                    Text(
                        text = if (orbState == MayaOrbState.SPEAKING) "\"$statusText\""
                        else if (statusText.isNotBlank()) statusText
                        else "Listening to your voice... Speak any prompt.",
                        color = when (orbState) {
                            MayaOrbState.SPEAKING -> Color(0xFFF0ABFC)
                            MayaOrbState.THINKING -> CyberGold
                            else -> CyberCyan
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Spoken Prompt Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        QuickPromptPill("🔦 Flashlight") { onQuickPrompt("turn on flashlight") }
                        QuickPromptPill("🔋 Battery") { onQuickPrompt("check battery status") }
                        QuickPromptPill("⏰ 7:30 Alarm") { onQuickPrompt("set alarm for 7:30 AM") }
                        QuickPromptPill("🎵 Play Music") { onQuickPrompt("play music") }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickPromptPill(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CyberSurfaceVariant,
        border = BorderStroke(1.dp, CyberSurfaceBorder.copy(alpha = 0.5f)),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
