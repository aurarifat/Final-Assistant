package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.manager.MayaPermissionManager
import com.example.ui.components.BottomNavBar
import com.example.ui.components.NotificationsDialog
import com.example.ui.components.ScreenEdgeGlowOverlay
import com.example.ui.components.StartupPermissionDialog
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainTab
import com.example.viewmodel.MayaViewModel
import com.example.viewmodel.SettingsSubPage

class MainActivity : ComponentActivity() {

    private val viewModel: MayaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MayaApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissions()
    }
}

@Composable
fun MayaApp(viewModel: MayaViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val settingsSubPage by viewModel.settingsSubPage.collectAsState()
    val showNotifications by viewModel.showNotifications.collectAsState()
    val permissionStatuses by viewModel.permissionStatuses.collectAsState()
    val isLiveVoiceActive by viewModel.isLiveVoiceActive.collectAsState()
    val orbState by viewModel.orbState.collectAsState()
    val statusText by viewModel.systemStatusMessage.collectAsState()

    var showStartupDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshPermissions()
    }

    val requestAllRuntime = {
        val runtimeList = MayaPermissionManager.getAllRuntimePermissions()
        permissionLauncher.launch(runtimeList.toTypedArray())
    }

    // Check on startup if any runtime permission is pending
    LaunchedEffect(Unit) {
        val hasPending = permissionStatuses.values.any { !it }
        if (hasPending) {
            showStartupDialog = true
            viewModel.speakPermissionBriefing()
        }
    }

    // Handle Android system back button cleanly
    BackHandler(enabled = settingsSubPage != null || currentTab != MainTab.HOME) {
        if (settingsSubPage != null) {
            if (settingsSubPage == SettingsSubPage.MAIN) {
                viewModel.closeSettings()
            } else {
                viewModel.openSettings(SettingsSubPage.MAIN)
            }
        } else if (currentTab != MainTab.HOME) {
            viewModel.setTab(MainTab.HOME)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (settingsSubPage != null) {
            SettingsScreen(
                viewModel = viewModel,
                onRequestAllRuntime = requestAllRuntime
            )
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    BottomNavBar(
                        currentTab = currentTab,
                        onTabSelected = { viewModel.setTab(it) }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                ) {
                    Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                        when (tab) {
                            MainTab.HOME -> HomeScreen(viewModel = viewModel)
                            MainTab.CONTROL -> DeviceControlScreen(viewModel = viewModel)
                            MainTab.VOICE -> VoiceScreen(viewModel = viewModel)
                            MainTab.MEMORIES -> MemoriesScreen(viewModel = viewModel)
                            MainTab.CHAT -> ChatScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }

        if (showNotifications) {
            NotificationsDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.toggleNotifications(false) }
            )
        }

        if (showStartupDialog) {
            StartupPermissionDialog(
                viewModel = viewModel,
                onRequestAllRuntime = requestAllRuntime,
                onDismiss = { showStartupDialog = false }
            )
        }

        // 4-Side Screen Edge Glow Overlay (Always-On Live Voice Assistant)
        ScreenEdgeGlowOverlay(
            isActive = isLiveVoiceActive,
            orbState = orbState,
            statusText = statusText ?: "",
            onTurnOff = { viewModel.toggleLiveVoice(false) },
            onQuickPrompt = { prompt -> viewModel.sendPrompt(prompt) }
        )
    }
}
