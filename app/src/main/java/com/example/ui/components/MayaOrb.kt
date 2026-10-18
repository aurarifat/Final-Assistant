package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
fun MayaOrb(
    orbState: MayaOrbState,
    energyPercent: Int,
    statusText: String,
    onOrbClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = when (orbState) {
            MayaOrbState.ASLEEP -> 0.94f
            MayaOrbState.LISTENING -> 0.88f
            MayaOrbState.THINKING -> 0.92f
            MayaOrbState.SPEAKING -> 0.85f
            MayaOrbState.READY -> 0.96f
        },
        targetValue = when (orbState) {
            MayaOrbState.ASLEEP -> 1.0f
            MayaOrbState.LISTENING -> 1.15f
            MayaOrbState.THINKING -> 1.08f
            MayaOrbState.SPEAKING -> 1.20f
            MayaOrbState.READY -> 1.04f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (orbState) {
                    MayaOrbState.ASLEEP -> 2400
                    MayaOrbState.LISTENING -> 800
                    MayaOrbState.THINKING -> 600
                    MayaOrbState.SPEAKING -> 700
                    MayaOrbState.READY -> 1500
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (orbState == MayaOrbState.THINKING) 3000 else 12000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Energy Status Pill
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CyberSurfaceVariant.copy(alpha = 0.85f),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.35f)),
            modifier = Modifier
                .padding(bottom = 16.dp)
                .testTag("energy_status_pill")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = CyberGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Energy $energyPercent%",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            when (orbState) {
                                MayaOrbState.ASLEEP -> TextMuted
                                MayaOrbState.LISTENING -> CyberCyan
                                MayaOrbState.THINKING -> CyberGold
                                MayaOrbState.SPEAKING -> CyberPink
                                MayaOrbState.READY -> CyberEmerald
                            }
                        )
                )
            }
        }

        // Central Holographic Orb Box
        Box(
            modifier = Modifier
                .size(230.dp)
                .testTag("central_maya_orb")
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOrbClick
                ),
            contentAlignment = Alignment.Center
        ) {
            // Animated Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = (size.minDimension / 2f) * 0.72f * pulseScale

                val primaryColor = when (orbState) {
                    MayaOrbState.ASLEEP -> Color(0xFF3B4261)
                    MayaOrbState.LISTENING -> CyberCyan
                    MayaOrbState.THINKING -> CyberGold
                    MayaOrbState.SPEAKING -> CyberPink
                    MayaOrbState.READY -> CyberCyan
                }

                val secondaryColor = when (orbState) {
                    MayaOrbState.ASLEEP -> Color(0xFF1E2337)
                    MayaOrbState.LISTENING -> CyberViolet
                    MayaOrbState.THINKING -> CyberCyan
                    MayaOrbState.SPEAKING -> CyberViolet
                    MayaOrbState.READY -> CyberViolet
                }

                // Outer diffuse atmospheric glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = if (orbState == MayaOrbState.ASLEEP) 0.12f else 0.35f),
                            secondaryColor.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = baseRadius * 1.45f
                    ),
                    radius = baseRadius * 1.45f,
                    center = center
                )

                // Concentric orbital rings with dashed/segmented aesthetic
                drawCircle(
                    color = primaryColor.copy(alpha = if (orbState == MayaOrbState.ASLEEP) 0.25f else 0.65f),
                    radius = baseRadius * 1.15f,
                    center = center,
                    style = Stroke(width = 2.5f)
                )

                drawCircle(
                    color = secondaryColor.copy(alpha = if (orbState == MayaOrbState.ASLEEP) 0.2f else 0.55f),
                    radius = baseRadius * 1.30f,
                    center = center,
                    style = Stroke(width = 1.5f)
                )

                // Core luminous sphere
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (orbState == MayaOrbState.ASLEEP) 0.5f else 0.95f),
                            primaryColor,
                            secondaryColor,
                            CyberBackground
                        ),
                        center = center,
                        radius = baseRadius
                    ),
                    radius = baseRadius,
                    center = center
                )

                // Orbiting cyber dots / nodes
                val numDots = 6
                for (i in 0 until numDots) {
                    val angle = Math.toRadians((rotationAngle + i * (360f / numDots)).toDouble())
                    val dotX = center.x + (baseRadius * 1.15f * cos(angle)).toFloat()
                    val dotY = center.y + (baseRadius * 1.15f * sin(angle)).toFloat()
                    drawCircle(
                        color = Color.White.copy(alpha = 0.85f),
                        radius = 3.5f,
                        center = Offset(dotX, dotY)
                    )
                }
            }

            // Central Icon Indicator in Orb
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (orbState) {
                    MayaOrbState.ASLEEP -> {
                        Icon(
                            imageVector = Icons.Default.Nightlight,
                            contentDescription = "Asleep",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Asleep",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    MayaOrbState.LISTENING -> {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Listening",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "Listening",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    MayaOrbState.THINKING -> {
                        Text(
                            text = "Thinking...",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    MayaOrbState.SPEAKING -> {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = "Speaking",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "Speaking",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    MayaOrbState.READY -> {
                        Text(
                            text = "MAYA",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Status caption
        Text(
            text = if (orbState == MayaOrbState.ASLEEP) {
                "Maya is asleep — say \"Hey Maya\" or tap orb"
            } else {
                statusText
            },
            color = if (orbState == MayaOrbState.ASLEEP) TextSecondary else CyberCyan,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}
