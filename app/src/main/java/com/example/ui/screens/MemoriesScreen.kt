package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.data.database.MemoryEntity
import com.example.ui.theme.*
import com.example.viewmodel.MayaViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MemoriesScreen(
    viewModel: MayaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val memories by viewModel.memories.collectAsState()
    val settings by viewModel.userSettings.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf<String?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Personal", "Preference", "Work", "Restored")

    val filteredMemories = memories.filter { m ->
        val matchesCategory = selectedCategory == "All" || m.category.equals(selectedCategory, ignoreCase = true)
        val matchesQuery = searchQuery.isBlank() || m.title.contains(searchQuery, ignoreCase = true) || m.content.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .statusBarsPadding()
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
                    text = "Saved Memories",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${memories.size} on-device memories stored",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Row {
                // Export Button
                IconButton(
                    onClick = {
                        viewModel.exportMemories { json ->
                            showExportDialog = json
                        }
                    },
                    modifier = Modifier.testTag("export_memories_button")
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = "Export JSON", tint = CyberCyan)
                }

                // Import Button
                IconButton(
                    onClick = { showImportDialog = true },
                    modifier = Modifier.testTag("import_memories_button")
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = "Restore JSON", tint = CyberViolet)
                }

                // Add Memory Button
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(CyberCyan)
                        .size(36.dp)
                        .testTag("add_memory_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add memory", tint = Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Memory Control Toggles Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CyberSurface,
            border = BorderStroke(1.dp, CyberSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Let Maya remember toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Let Maya remember on her own",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (settings.autoRemember) "Autonomous long-term recall ON" else "Auto-memorization disabled",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = settings.autoRemember,
                        onCheckedChange = { checked ->
                            viewModel.updateSettings { it.copy(autoRemember = checked) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = CyberCyan
                        )
                    )
                }

                Divider(
                    color = CyberSurfaceBorder.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Incognito Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Incognito Mode",
                            color = if (settings.isIncognito) CyberGold else TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (settings.isIncognito) "No new memories recorded (Existing kept safe)" else "Standard contextual learning",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = settings.isIncognito,
                        onCheckedChange = { checked ->
                            viewModel.updateSettings { it.copy(isIncognito = checked) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = CyberGold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search memories...", color = TextMuted, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberCyan,
                unfocusedBorderColor = CyberSurfaceBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) CyberCyan.copy(alpha = 0.2f) else CyberSurface,
                    border = BorderStroke(1.dp, if (isSelected) CyberCyan else CyberSurfaceBorder.copy(alpha = 0.5f)),
                    modifier = Modifier.clickable { selectedCategory = cat }
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) CyberCyan else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Memories List
        if (filteredMemories.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No memories found",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Tell Maya \"Remember that...\" or tap + to add manually",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredMemories, key = { it.id }) { memory ->
                    MemoryCard(
                        memory = memory,
                        onDelete = { viewModel.deleteMemory(memory) }
                    )
                }
            }
        }
    }

    // Add Memory Dialog
    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Personal") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Memory", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Memory content") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (content.isNotBlank()) {
                            viewModel.addManualMemory(title.ifBlank { "User Memory" }, content, category)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text("Save Memory", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CyberSurface
        )
    }

    // Export Dialog
    if (showExportDialog != null) {
        AlertDialog(
            onDismissRequest = { showExportDialog = null },
            title = { Text("Exported On-Device Backup", color = TextPrimary) },
            text = {
                Column {
                    Text(
                        text = "All your Maya memories have been serialized into JSON format. You can copy this payload to backup or transfer.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = CyberSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(140.dp).fillMaxWidth()
                    ) {
                        Text(
                            text = showExportDialog ?: "",
                            color = CyberCyan,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboard.setText(AnnotatedString(showExportDialog ?: ""))
                        Toast.makeText(context, "Backup JSON copied to clipboard", Toast.LENGTH_SHORT).show()
                        showExportDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text("Copy JSON", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = null }) {
                    Text("Close", color = TextSecondary)
                }
            },
            containerColor = CyberSurface
        )
    }

    // Import Dialog
    if (showImportDialog) {
        var importJson by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Restore Memories", color = TextPrimary) },
            text = {
                Column {
                    Text(
                        text = "Paste a valid Maya AI exported JSON backup:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importJson,
                        onValueChange = { importJson = it },
                        placeholder = { Text("{\"app\": \"Maya AI\", ...}", color = TextMuted, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJson.isNotBlank()) {
                            viewModel.importMemories(importJson) { count ->
                                Toast.makeText(context, "Restored $count memories successfully", Toast.LENGTH_SHORT).show()
                                showImportDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberViolet)
                ) {
                    Text("Restore", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CyberSurface
        )
    }
}

@Composable
private fun MemoryCard(
    memory: MemoryEntity,
    onDelete: () -> Unit
) {
    val dateStr = remember(memory.timestamp) {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(memory.timestamp))
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CyberSurface,
        border = BorderStroke(1.dp, CyberSurfaceBorder.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CyberCyan.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = memory.category.uppercase(),
                            color = CyberCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (memory.isPinned) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = CyberGold, modifier = Modifier.size(14.dp))
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = dateStr, color = TextMuted, fontSize = 10.sp)
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = memory.title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = memory.content,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}
