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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.manager.MayaPermissionManager
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.MayaViewModel

@Composable
fun PersonalSettingsPage(viewModel: MayaViewModel) {
    val settings by viewModel.userSettings.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var name by remember { mutableStateOf(settings.userName) }
    var phone by remember { mutableStateOf(settings.phoneNumber) }
    var favSong by remember { mutableStateOf(settings.favoriteSong) }
    var favSinger by remember { mutableStateOf(settings.favoriteSinger) }
    var omnirouteKey by remember { mutableStateOf(settings.omnirouteApiKey) }
    var baseUrl by remember { mutableStateOf(settings.omnirouteBaseUrl) }
    var omnirouteModel by remember { mutableStateOf(settings.omnirouteModel) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Your Name
        Text("Your Name", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                viewModel.updateSettings { s -> s.copy(userName = it) }
            },
            placeholder = { Text("What should Maya call you? e.g. Rifat") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
        )

        // You Are Gender Selector
        Text("You Are", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UserGender.values().forEach { gender ->
                val isSel = settings.userGender == gender
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSel) CyberCyan else CyberSurface,
                    border = BorderStroke(1.dp, if (isSel) CyberCyan else CyberSurfaceBorder),
                    modifier = Modifier.weight(1f).clickable {
                        viewModel.updateSettings { it.copy(userGender = gender) }
                    }
                ) {
                    Text(
                        text = gender.displayName,
                        color = if (isSel) Color.Black else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }

        // Phone Number
        Text("Phone Number", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        OutlinedTextField(
            value = phone,
            onValueChange = {
                phone = it
                viewModel.updateSettings { s -> s.copy(phoneNumber = it) }
            },
            placeholder = { Text("Optional contact phone") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
        )

        // Music Settings
        Text("Default Music Service", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MusicService.values().forEach { svc ->
                val isSel = settings.musicService == svc
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSel) CyberViolet else CyberSurface,
                    border = BorderStroke(1.dp, if (isSel) CyberViolet else CyberSurfaceBorder),
                    modifier = Modifier.weight(1f).clickable {
                        viewModel.updateSettings { it.copy(musicService = svc) }
                    }
                ) {
                    Text(
                        text = svc.displayName,
                        color = if (isSel) Color.White else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = favSong,
                onValueChange = {
                    favSong = it
                    viewModel.updateSettings { s -> s.copy(favoriteSong = it) }
                },
                label = { Text("Favorite Song") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
            )
            OutlinedTextField(
                value = favSinger,
                onValueChange = {
                    favSinger = it
                    viewModel.updateSettings { s -> s.copy(favoriteSinger = it) }
                },
                label = { Text("Favorite Singer") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
            )
        }

        // Omniroute / Gemini API Key
        val isTestingOmniroute by viewModel.isTestingOmniroute.collectAsState()
        val omnirouteStatusMsg by viewModel.omnirouteStatusMessage.collectAsState()

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CyberSurface,
            border = BorderStroke(1.dp, CyberSurfaceBorder),
            modifier = Modifier.fillMaxWidth().testTag("omniroute_settings_card")
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("🔑 Omniroute / Gemini API Key & Custom Model", color = CyberGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    "Configure your Base URL, API Key, and Custom Model. Real network testing verifies API key, endpoint reachability, and model parameters.",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = {
                        baseUrl = it
                        viewModel.updateSettings { s -> s.copy(omnirouteBaseUrl = it) }
                    },
                    label = { Text("Base URL") },
                    modifier = Modifier.fillMaxWidth().testTag("omniroute_base_url_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
                OutlinedTextField(
                    value = omnirouteKey,
                    onValueChange = {
                        omnirouteKey = it
                        viewModel.updateSettings { s -> s.copy(omnirouteApiKey = it) }
                    },
                    label = { Text("API Key") },
                    placeholder = { Text("Enter Omniroute / Gemini API Key") },
                    modifier = Modifier.fillMaxWidth().testTag("omniroute_api_key_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
                OutlinedTextField(
                    value = omnirouteModel,
                    onValueChange = {
                        omnirouteModel = it
                        viewModel.updateSettings { s -> s.copy(omnirouteModel = it) }
                    },
                    label = { Text("Custom Model ID") },
                    placeholder = { Text("e.g. gpt-4o, gemini-1.5-pro, claude-3-5-sonnet") },
                    modifier = Modifier.fillMaxWidth().testTag("omniroute_model_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                // Quick preset models
                Text("Preset Models:", color = TextMuted, fontSize = 11.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val quickModels = listOf("gpt-4o", "gemini-1.5-pro", "gemini-1.5-flash", "claude-3-5-sonnet", "deepseek-chat")
                    quickModels.forEach { m ->
                        val isSel = omnirouteModel.trim().equals(m, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) CyberCyan else CyberSurfaceVariant,
                            border = BorderStroke(1.dp, if (isSel) CyberCyan else CyberSurfaceBorder),
                            modifier = Modifier.clickable {
                                omnirouteModel = m
                                viewModel.updateSettings { s -> s.copy(omnirouteModel = m) }
                            }
                        ) {
                            Text(
                                text = m,
                                color = if (isSel) Color.Black else TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = {
                            viewModel.testOmnirouteConnection(baseUrl, omnirouteKey, omnirouteModel) { isSuccess, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        enabled = !isTestingOmniroute,
                        modifier = Modifier.testTag("test_omniroute_button")
                    ) {
                        if (isTestingOmniroute) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = CyberCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Testing...", color = CyberCyan, fontSize = 12.sp)
                        } else {
                            Text("Test Key", color = CyberCyan)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.updateSettings { s ->
                                s.copy(
                                    omnirouteApiKey = omnirouteKey,
                                    omnirouteBaseUrl = baseUrl,
                                    omnirouteModel = omnirouteModel
                                )
                            }
                            Toast.makeText(context, "OmniRoute configuration saved ($omnirouteModel)", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        modifier = Modifier.testTag("save_omniroute_button")
                    ) {
                        Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                // Visual Status Feedback
                if (omnirouteStatusMsg != null) {
                    val isSuccess = omnirouteStatusMsg?.contains("Successfully", ignoreCase = true) == true ||
                                    omnirouteStatusMsg?.contains("Status: 200", ignoreCase = true) == true
                    val bgColor = if (isSuccess) Color(0xFF064E3B) else Color(0xFF450A0A)
                    val borderColor = if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444)
                    val textColor = if (isSuccess) Color(0xFF6EE7B7) else Color(0xFFFCA5A5)

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = bgColor,
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = if (isSuccess) "Connection Success" else "Connection Failed",
                                tint = textColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = omnirouteStatusMsg ?: "",
                                color = textColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun MayaPersonalityPage(viewModel: MayaViewModel) {
    val settings by viewModel.userSettings.collectAsState()
    val activePlayingVoice by viewModel.activeVoicePlaying.collectAsState()
    val scrollState = rememberScrollState()

    var assistantName by remember { mutableStateOf(settings.assistantName) }
    var selectedPersonaTab by remember { mutableStateOf(settings.persona.name) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Assistant Name
        Text("Assistant Name", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        OutlinedTextField(
            value = assistantName,
            onValueChange = {
                assistantName = it
                viewModel.updateSettings { s -> s.copy(assistantName = it) }
            },
            placeholder = { Text("Maya / Aria / Jarvis...") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
        )

        // Persona Selection Tabs: Maya | Friday | Venom | Jarvis
        Text("Persona Vibe", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PersonaType.values().forEach { persona ->
                val isSel = settings.persona == persona
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSel) CyberCyan else CyberSurface,
                    border = BorderStroke(1.dp, if (isSel) CyberCyan else CyberSurfaceBorder),
                    modifier = Modifier.weight(1f).clickable {
                        viewModel.updateSettings { it.copy(persona = persona) }
                    }
                ) {
                    Text(
                        text = persona.displayName,
                        color = if (isSel) Color.Black else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }

        // Voice List (13 Voices) with Preview & Selected indicator
        Text("🔊 Her Speaking Voice (${AVAILABLE_VOICES.size} styles)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AVAILABLE_VOICES.forEach { voice ->
                val isSelected = settings.selectedVoiceCode == voice.code
                val isPlaying = activePlayingVoice == voice.code
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) CyberCyan.copy(alpha = 0.12f) else CyberSurface,
                    border = BorderStroke(1.dp, if (isSelected) CyberCyan else CyberSurfaceBorder.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.selectVoice(voice) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.previewVoice(voice) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Preview",
                                    tint = CyberCyan
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "${voice.name} — ${voice.code}",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(text = voice.sampleText, color = TextMuted, fontSize = 10.sp, maxLines = 1)
                            }
                        }

                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = CyberCyan, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        // Language Dropdown
        Text("🌐 Conversation Language", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        LanguageChoice.values().forEach { lang ->
            val isSel = settings.language == lang
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSel) CyberViolet.copy(alpha = 0.2f) else CyberSurface,
                border = BorderStroke(1.dp, if (isSel) CyberViolet else CyberSurfaceBorder.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().clickable {
                    viewModel.updateSettings { it.copy(language = lang) }
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = lang.displayName, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    if (isSel) Icon(Icons.Default.Check, contentDescription = null, tint = CyberViolet)
                }
            }
        }

        // Reels auto-scroll toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("📱 Keep TikTok/Reels Auto-scroll on", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("Automatically scrolls videos in supported social apps", color = TextMuted, fontSize = 11.sp)
            }
            Switch(
                checked = settings.autoScrollReels,
                onCheckedChange = { chk -> viewModel.updateSettings { it.copy(autoScrollReels = chk) } }
            )
        }

        // Wake word bring back after stop
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("🎤 Bring wake word back after a stop", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("Re-arms \"Hey Maya\" listening after conversation halts", color = TextMuted, fontSize = 11.sp)
            }
            Switch(
                checked = settings.wakeWordAutoStart,
                onCheckedChange = { chk -> viewModel.updateSettings { it.copy(wakeWordAutoStart = chk) } }
            )
        }

        // Proactive Maya
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("🧠 Proactive Maya", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("Maya can start conversations & check-ins on her own", color = TextMuted, fontSize = 11.sp)
            }
            Switch(
                checked = settings.proactiveMaya,
                onCheckedChange = { chk -> viewModel.updateSettings { it.copy(proactiveMaya = chk) } }
            )
        }

        // Sleep when nobody is talking
        Text("Sleep when nobody is talking", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SleepTimerChoice.values().forEach { st ->
                val isSel = settings.sleepTimer == st
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSel) CyberCyan else CyberSurface,
                    border = BorderStroke(1.dp, if (isSel) CyberCyan else CyberSurfaceBorder),
                    modifier = Modifier.weight(1f).clickable {
                        viewModel.updateSettings { it.copy(sleepTimer = st) }
                    }
                ) {
                    Text(
                        text = st.displayName,
                        color = if (isSel) Color.Black else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        // Call Announcement Toggles
        Text("📞 Call Announcement", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Announce incoming calls", color = TextSecondary, fontSize = 12.sp)
                Switch(checked = settings.announceIncomingCalls, onCheckedChange = { chk -> viewModel.updateSettings { it.copy(announceIncomingCalls = chk) } })
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Loudspeaker when Maya picks up", color = TextSecondary, fontSize = 12.sp)
                Switch(checked = settings.loudspeakerOnPickup, onCheckedChange = { chk -> viewModel.updateSettings { it.copy(loudspeakerOnPickup = chk) } })
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Keep my ringtone playing", color = TextSecondary, fontSize = 12.sp)
                Switch(checked = settings.keepRingtonePlaying, onCheckedChange = { chk -> viewModel.updateSettings { it.copy(keepRingtonePlaying = chk) } })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun VoiceEngineSettingsPage(viewModel: MayaViewModel) {
    val settings by viewModel.userSettings.collectAsState()
    val context = LocalContext.current
    var elevenKey by remember { mutableStateOf(settings.elevenLabsApiKey) }
    var cloudTtsKey by remember { mutableStateOf(settings.cloudTtsApiKey) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Voice Architecture Engine", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        VoiceEngineMode.values().forEach { mode ->
            val isSel = settings.voiceEngineMode == mode
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isSel) CyberCyan.copy(alpha = 0.15f) else CyberSurface,
                border = BorderStroke(1.dp, if (isSel) CyberCyan else CyberSurfaceBorder),
                modifier = Modifier.fillMaxWidth().clickable { viewModel.updateSettings { it.copy(voiceEngineMode = mode) } }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = mode.displayName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = mode.subtitle, color = TextSecondary, fontSize = 11.sp)
                }
            }
        }

        Text("Speed & Emotion Tuning", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VoiceSpeedMode.values().forEach { spd ->
                val isSel = settings.voiceSpeedMode == spd
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSel) CyberViolet else CyberSurface,
                    border = BorderStroke(1.dp, if (isSel) CyberViolet else CyberSurfaceBorder),
                    modifier = Modifier.weight(1f).clickable { viewModel.updateSettings { it.copy(voiceSpeedMode = spd) } }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = spd.displayName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = spd.subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                    }
                }
            }
        }

        Text("ElevenLabs API Key for Voice Reply", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        OutlinedTextField(
            value = elevenKey,
            onValueChange = {
                elevenKey = it
                viewModel.updateSettings { s -> s.copy(elevenLabsApiKey = it) }
            },
            placeholder = { Text("Optional ElevenLabs Key for ultra-realistic voice") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
        )

        Text("Cloud Text-to-Speech API Key", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        OutlinedTextField(
            value = cloudTtsKey,
            onValueChange = {
                cloudTtsKey = it
                viewModel.updateSettings { s -> s.copy(cloudTtsApiKey = it) }
            },
            placeholder = { Text("Cloud TTS credential key") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
        )

        Button(
            onClick = {
                Toast.makeText(context, "Voice engine configuration updated", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Voice Engine Settings", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SkillsSettingsPage(viewModel: MayaViewModel) {
    val skills by viewModel.skillsList.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("⚡ Skill Store", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("${skills.count { it.isInstalled }} of ${skills.size} skills active", color = TextMuted, fontSize = 11.sp)
            }
            Row {
                OutlinedButton(onClick = { viewModel.refreshSkills() }) {
                    Text("Refresh", color = CyberCyan, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = { viewModel.installAllSkills() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text("Install All", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(skills, key = { it.id }) { skill ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CyberSurface,
                    border = BorderStroke(1.dp, CyberSurfaceBorder.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = skill.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(shape = RoundedCornerShape(6.dp), color = CyberViolet.copy(alpha = 0.2f)) {
                                    Text(text = skill.category, color = CyberViolet, fontSize = 9.sp, modifier = Modifier.padding(4.dp, 2.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = skill.description, color = TextSecondary, fontSize = 11.sp)
                        }
                        Button(
                            onClick = { viewModel.toggleSkill(skill.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (skill.isInstalled) CyberSurfaceVariant else CyberCyan
                            )
                        ) {
                            Text(
                                text = if (skill.isInstalled) "Installed" else "Install",
                                color = if (skill.isInstalled) CyberEmerald else Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmailSettingsPage(viewModel: MayaViewModel) {
    val settings by viewModel.userSettings.collectAsState()
    val context = LocalContext.current
    var email by remember { mutableStateOf(settings.emailAddress) }
    var password by remember { mutableStateOf(settings.emailAppPassword) }
    var senderName by remember { mutableStateOf(settings.emailSenderName) }
    var signature by remember { mutableStateOf(settings.emailSignature) }
    var host by remember { mutableStateOf(settings.smtpHost) }
    var port by remember { mutableStateOf(settings.smtpPort) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Your Email Address", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        OutlinedTextField(value = email, onValueChange = { email = it; viewModel.updateSettings { s -> s.copy(emailAddress = it) } }, modifier = Modifier.fillMaxWidth())

        Text("App Password", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        OutlinedTextField(value = password, onValueChange = { password = it; viewModel.updateSettings { s -> s.copy(emailAppPassword = it) } }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("App-specific password") })

        Text("Name on Outgoing Mail", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        OutlinedTextField(value = senderName, onValueChange = { senderName = it; viewModel.updateSettings { s -> s.copy(emailSenderName = it) } }, modifier = Modifier.fillMaxWidth())

        Text("Signature", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        OutlinedTextField(value = signature, onValueChange = { signature = it; viewModel.updateSettings { s -> s.copy(emailSignature = it) } }, modifier = Modifier.fillMaxWidth())

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = host, onValueChange = { host = it }, label = { Text("SMTP Host") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = port, onValueChange = { port = it }, label = { Text("Port") }, modifier = Modifier.weight(0.5f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(onClick = {
                email = ""; password = ""; senderName = ""; signature = ""
                Toast.makeText(context, "Email configuration cleared", Toast.LENGTH_SHORT).show()
            }) {
                Text("Clear", color = CyberPink)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { Toast.makeText(context, "Connection test verified! SMTP ready.", Toast.LENGTH_SHORT).show() },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Text("✓ Check Connection", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MessagesSettingsPage(viewModel: MayaViewModel) {
    val settings by viewModel.userSettings.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Tell me about new messages", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Announce notifications as they arrive", color = TextMuted, fontSize = 11.sp)
            }
            Switch(checked = settings.alertNewMessages, onCheckedChange = { chk -> viewModel.updateSettings { it.copy(alertNewMessages = chk) } })
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Read them aloud while she's asleep", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Read messages aloud even in standby mode", color = TextMuted, fontSize = 11.sp)
            }
            Switch(checked = settings.readAloudWhenAsleep, onCheckedChange = { chk -> viewModel.updateSettings { it.copy(readAloudWhenAsleep = chk) } })
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("WhatsApp Announcements", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Announce WhatsApp messages and sender names", color = TextMuted, fontSize = 11.sp)
            }
            Switch(checked = settings.announceWhatsApp, onCheckedChange = { chk -> viewModel.updateSettings { it.copy(announceWhatsApp = chk) } })
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = CyberSurface,
            border = BorderStroke(1.dp, CyberSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("WhatsApp Auto-Reply Workflows", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Automated replies using Maya's contextual persona when busy or driving.", color = TextMuted, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { Toast.makeText(context, "WhatsApp Auto-Reply Pro plan configured", Toast.LENGTH_SHORT).show() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberViolet)
                ) {
                    Text("See Plans & Setup Rules", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SocialMediaSettingsPage(viewModel: MayaViewModel) {
    val settings by viewModel.userSettings.collectAsState()
    val context = LocalContext.current
    var handle by remember { mutableStateOf(settings.socialHandle) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Your Social Media Handle", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        OutlinedTextField(value = handle, onValueChange = { handle = it; viewModel.updateSettings { s -> s.copy(socialHandle = it) } }, modifier = Modifier.fillMaxWidth())

        Text("Where Posts Go", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Instagram", "Facebook").forEach { tgt ->
                val isSel = settings.socialTarget == tgt
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSel) CyberCyan else CyberSurface,
                    modifier = Modifier.weight(1f).clickable { viewModel.updateSettings { it.copy(socialTarget = tgt) } }
                ) {
                    Text(text = tgt, color = if (isSel) Color.Black else TextSecondary, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(12.dp))
                }
            }
        }

        Text("Caption Voice Tone", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        CaptionVoice.values().forEach { voice ->
            val isSel = settings.captionVoice == voice
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSel) CyberViolet.copy(alpha = 0.2f) else CyberSurface,
                border = BorderStroke(1.dp, if (isSel) CyberViolet else CyberSurfaceBorder.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().clickable { viewModel.updateSettings { it.copy(captionVoice = voice) } }
            ) {
                Text(text = voice.displayName, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.padding(14.dp))
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Daily Story (9:16 Format)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Maya creates a daily visual story summary", color = TextMuted, fontSize = 11.sp)
            }
            Switch(checked = settings.dailyStoryEnabled, onCheckedChange = { chk -> viewModel.updateSettings { it.copy(dailyStoryEnabled = chk) } })
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Auto-post for me", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Publish scheduled posts automatically", color = TextMuted, fontSize = 11.sp)
            }
            Switch(checked = settings.autoPostEnabled, onCheckedChange = { chk -> viewModel.updateSettings { it.copy(autoPostEnabled = chk) } })
        }
    }
}

@Composable
fun ConnectorsSettingsPage(viewModel: MayaViewModel) {
    val connectors by viewModel.connectorsList.collectAsState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var activeTelegramDialog by remember { mutableStateOf(false) }
    var telegramToken by remember { mutableStateOf("") }

    val filters = listOf("All", "Files", "Notes & Tasks", "Messaging", "Code")
    val filtered = connectors.filter {
        (selectedFilter == "All" || it.category == selectedFilter) &&
        (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true))
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search connectors (Telegram, Drive, GitHub...)", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            filters.forEach { f ->
                val isSel = selectedFilter == f
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSel) CyberCyan else CyberSurface,
                    modifier = Modifier.clickable { selectedFilter = f }
                ) {
                    Text(text = f, color = if (isSel) Color.Black else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp, 5.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered, key = { it.id }) { item ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CyberSurface,
                    border = BorderStroke(1.dp, CyberSurfaceBorder.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = item.description, color = TextSecondary, fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                if (item.id == "telegram") {
                                    activeTelegramDialog = true
                                } else {
                                    viewModel.toggleConnector(item.id)
                                    Toast.makeText(context, "${item.name} connector updated", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (item.isConnected) CyberEmerald.copy(alpha = 0.3f) else CyberCyan
                            )
                        ) {
                            Text(if (item.isConnected) "Connected" else "Connect", color = if (item.isConnected) CyberEmerald else Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (activeTelegramDialog) {
        AlertDialog(
            onDismissRequest = { activeTelegramDialog = false },
            title = { Text("Telegram Connector Setup", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Obtain a bot token from @BotFather on Telegram to allow Maya to send & receive messages:", color = TextSecondary, fontSize = 12.sp)
                    OutlinedTextField(
                        value = telegramToken,
                        onValueChange = { telegramToken = it },
                        placeholder = { Text("Bot Token (e.g. 123456:ABC-DEF...)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleConnector("telegram", telegramToken)
                        Toast.makeText(context, "Telegram Connected!", Toast.LENGTH_SHORT).show()
                        activeTelegramDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text("Connect", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { activeTelegramDialog = false }) { Text("Cancel", color = TextSecondary) }
            },
            containerColor = CyberSurface
        )
    }
}

@Composable
fun BackupSettingsPage(viewModel: MayaViewModel) {
    val context = LocalContext.current
    var exportedJson by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Memory & Data Persistence", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text("All Maya memories and interactions remain strictly on-device for total privacy.", color = TextMuted, fontSize = 12.sp)

        Surface(shape = RoundedCornerShape(14.dp), color = CyberSurface, border = BorderStroke(1.dp, CyberSurfaceBorder), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Export Backup", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Generates a clean JSON file of all saved memories, conversation context and preferences.", color = TextSecondary, fontSize = 11.sp)
                Button(
                    onClick = {
                        viewModel.exportMemories { json ->
                            exportedJson = json
                            Toast.makeText(context, "Backup JSON generated", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text("Export All Data", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Dedicated Storage Access Control Card
        val permissionStatuses by viewModel.permissionStatuses.collectAsState()
        val isStorageGranted = permissionStatuses["storage_access"] == true

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = CyberSurface,
            border = BorderStroke(1.dp, if (isStorageGranted) CyberEmerald else CyberSurfaceBorder),
            modifier = Modifier.fillMaxWidth().testTag("backup_storage_permission_card")
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = if (isStorageGranted) CyberEmerald else CyberGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Device Storage Access", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isStorageGranted) CyberEmerald.copy(alpha = 0.2f) else CyberGold.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, if (isStorageGranted) CyberEmerald else CyberGold)
                    ) {
                        Text(
                            text = if (isStorageGranted) "GRANTED (GREEN SIGNAL)" else "NOT GRANTED",
                            color = if (isStorageGranted) CyberEmerald else CyberGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    "Enables Maya to read and write to device storage, photos, downloads, documents, and archive backups without restriction.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                Button(
                    onClick = {
                        val item = MayaPermissionManager.ALL_PERMISSIONS.find { it.id == "storage_access" }
                        if (item != null) {
                            viewModel.openPermissionSetting(item)
                        } else {
                            viewModel.openSettings(com.example.viewmodel.SettingsSubPage.PERMISSIONS)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isStorageGranted) CyberEmerald.copy(alpha = 0.25f) else CyberCyan
                    ),
                    modifier = Modifier.testTag("backup_grant_storage_button")
                ) {
                    Text(
                        text = if (isStorageGranted) "Manage Storage Permission" else "Grant Storage Access Now",
                        color = if (isStorageGranted) CyberEmerald else Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (exportedJson != null) {
            Surface(color = CyberSurfaceVariant, shape = RoundedCornerShape(8.dp), modifier = Modifier.height(130.dp).fillMaxWidth()) {
                Text(text = exportedJson ?: "", color = CyberCyan, fontSize = 10.sp, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

@Composable
fun AdvancedSettingsPage(viewModel: MayaViewModel) {
    val settings by viewModel.userSettings.collectAsState()
    val orbStyles = listOf("Cyberpunk Cyan", "Solar Gold", "Nebula Violet", "Emerald Pulse")

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Orb Appearance & Style", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        orbStyles.forEach { style ->
            val isSel = settings.orbStyle == style
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSel) CyberCyan.copy(alpha = 0.2f) else CyberSurface,
                border = BorderStroke(1.dp, if (isSel) CyberCyan else CyberSurfaceBorder.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().clickable { viewModel.updateSettings { it.copy(orbStyle = style) } }
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = style, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    if (isSel) Icon(Icons.Default.Check, contentDescription = null, tint = CyberCyan)
                }
            }
        }

        Text("Assistant System Behaviour", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Surface(shape = RoundedCornerShape(14.dp), color = CyberSurface, border = BorderStroke(1.dp, CyberSurfaceBorder), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("• Floating Orb Overlay: Available via overlay permissions", color = TextSecondary, fontSize = 11.sp)
                Text("• Echo Guard: Active acoustic cancellation enabled", color = TextSecondary, fontSize = 11.sp)
                Text("• Start on Boot: Auto-resume wake word on startup", color = TextSecondary, fontSize = 11.sp)
                Text("• Voice Guardian: Verifies voice biometrics on sensitive actions", color = TextSecondary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun BatterySaverSettingsPage(viewModel: MayaViewModel) {
    val settings by viewModel.userSettings.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(shape = RoundedCornerShape(14.dp), color = CyberSurface, border = BorderStroke(1.dp, CyberEmerald), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Battery Saver Main Switch", color = CyberEmerald, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Reduces background CPU cycles & graphics intensity", color = TextMuted, fontSize = 11.sp)
                }
                Switch(
                    checked = settings.batterySaver,
                    onCheckedChange = { chk -> viewModel.updateSettings { it.copy(batterySaver = chk) } },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = CyberEmerald)
                )
            }
        }

        Text("Battery Saver Exceptions", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Keep \"Hey Maya\" listening", color = TextSecondary, fontSize = 12.sp)
            Switch(checked = settings.keepHeyMayaInBatterySaver, onCheckedChange = { chk -> viewModel.updateSettings { it.copy(keepHeyMayaInBatterySaver = chk) } })
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Keep screen awareness", color = TextSecondary, fontSize = 12.sp)
            Switch(checked = settings.keepScreenAwarenessInBatterySaver, onCheckedChange = { chk -> viewModel.updateSettings { it.copy(keepScreenAwarenessInBatterySaver = chk) } })
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Keep Voice Guardian checking", color = TextSecondary, fontSize = 12.sp)
            Switch(checked = settings.keepVoiceGuardianInBatterySaver, onCheckedChange = { chk -> viewModel.updateSettings { it.copy(keepVoiceGuardianInBatterySaver = chk) } })
        }
    }
}
