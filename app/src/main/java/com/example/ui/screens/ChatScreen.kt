package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ChatMessageEntity
import com.example.model.AVAILABLE_VOICES
import com.example.ui.theme.*
import com.example.viewmodel.MayaViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    viewModel: MayaViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val settings by viewModel.userSettings.collectAsState()
    val listState = rememberLazyListState()

    var inputMessage by remember { mutableStateOf("") }
    var attachedFile by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .statusBarsPadding()
    ) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CyberCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Conversation with ${settings.assistantName}",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${settings.persona.displayName} Mode • On-device Memory Active",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(
                onClick = { viewModel.clearChatHistory() },
                modifier = Modifier.testTag("clear_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Chat",
                    tint = TextMuted
                )
            }
        }

        Divider(color = CyberSurfaceBorder.copy(alpha = 0.4f), thickness = 1.dp)

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageItem(
                    message = msg,
                    onPlayAudio = {
                        val voice = AVAILABLE_VOICES.find { it.code == settings.selectedVoiceCode }
                            ?: AVAILABLE_VOICES.first()
                        viewModel.previewVoice(voice.copy(sampleText = msg.text))
                    }
                )
            }
        }

        // Bottom Input Row
        Surface(
            color = CyberSurface,
            border = BorderStroke(1.dp, CyberSurfaceBorder.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                if (attachedFile != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📎 File ready: $attachedFile",
                            color = CyberCyan,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { attachedFile = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            attachedFile = if (attachedFile == null) "device_screenshot.png" else null
                        },
                        modifier = Modifier.testTag("chat_attach_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attach",
                            tint = if (attachedFile != null) CyberCyan else TextSecondary
                        )
                    }

                    TextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        placeholder = { Text("Write to ${settings.assistantName}...", color = TextMuted, fontSize = 14.sp) },
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
                            .testTag("chat_text_input")
                    )

                    IconButton(
                        onClick = {
                            if (inputMessage.isNotBlank() || attachedFile != null) {
                                viewModel.sendPrompt(inputMessage, attachedFile)
                                inputMessage = ""
                                attachedFile = null
                            }
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(CyberCyan)
                            .testTag("chat_send_button")
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
}

@Composable
private fun ChatMessageItem(
    message: ChatMessageEntity,
    onPlayAudio: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val timeFormatted = remember(message.timestamp) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    val isUser = message.isUser

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(if (isUser) "user_chat_bubble" else "maya_chat_bubble"),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) CyberCyan.copy(alpha = 0.22f) else CyberSurfaceVariant,
            border = BorderStroke(
                1.dp,
                if (isUser) CyberCyan.copy(alpha = 0.5f) else CyberSurfaceBorder.copy(alpha = 0.5f)
            ),
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Action tag banner if any
                if (!message.actionTaken.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CyberViolet.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, CyberViolet.copy(alpha = 0.5f)),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "⚡ ${message.actionTaken}",
                            color = CyberViolet,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (!message.attachmentUri.isNullOrBlank()) {
                    Text(
                        text = "📎 ${message.attachmentUri}",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Text(
                    text = message.text,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = timeFormatted,
                        color = TextMuted,
                        fontSize = 10.sp
                    )

                    Row {
                        // Copy button
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(message.text))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextMuted, modifier = Modifier.size(14.dp))
                        }

                        // Play audio button for Maya messages
                        if (!isUser) {
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = onPlayAudio,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Play voice", tint = CyberCyan, modifier = Modifier.size(15.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
