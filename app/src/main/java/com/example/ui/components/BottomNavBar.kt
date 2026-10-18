package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.MainTab

@Composable
fun BottomNavBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = CyberSurface.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, CyberSurfaceBorder.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Home Tab
            NavTabItem(
                label = "Home",
                icon = Icons.Default.Home,
                isSelected = currentTab == MainTab.HOME,
                testTag = "nav_tab_home",
                onClick = { onTabSelected(MainTab.HOME) }
            )

            // Control Tab (Advanced Device Control)
            NavTabItem(
                label = "Control",
                icon = Icons.Default.TouchApp,
                isSelected = currentTab == MainTab.CONTROL,
                testTag = "nav_tab_control",
                onClick = { onTabSelected(MainTab.CONTROL) }
            )

            // Central Elevated Big Voice Button
            CentralVoiceNavButton(
                isSelected = currentTab == MainTab.VOICE,
                onClick = { onTabSelected(MainTab.VOICE) }
            )

            // Memories Tab
            NavTabItem(
                label = "Memories",
                icon = Icons.Default.Psychology,
                isSelected = currentTab == MainTab.MEMORIES,
                testTag = "nav_tab_memories",
                onClick = { onTabSelected(MainTab.MEMORIES) }
            )

            // Chat Tab
            NavTabItem(
                label = "Chat",
                icon = Icons.Default.ChatBubble,
                isSelected = currentTab == MainTab.CHAT,
                testTag = "nav_tab_chat",
                onClick = { onTabSelected(MainTab.CHAT) }
            )
        }
    }
}

@Composable
private fun NavTabItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) CyberCyan else TextMuted,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isSelected) CyberCyan else TextMuted,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(CyberCyan)
            )
        }
    }
}

@Composable
private fun CentralVoiceNavButton(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "voice_button_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isSelected) 1.08f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "voice_pulse"
    )

    Box(
        modifier = Modifier
            .offset(y = (-10).dp)
            .scale(pulse)
            .size(56.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = if (isSelected) listOf(CyberCyan, CyberViolet)
                    else listOf(CyberSurfaceVariant, Color(0xFF1E293B))
                )
            )
            .clickable(onClick = onClick)
            .testTag("nav_tab_voice_center"),
        contentAlignment = Alignment.Center
    ) {
        // Glowing outline
        Surface(
            shape = CircleShape,
            color = Color.Transparent,
            border = BorderStroke(2.dp, if (isSelected) Color.White else CyberCyan.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Interaction",
                    tint = if (isSelected) Color.White else CyberCyan,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
