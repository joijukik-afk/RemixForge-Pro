package com.remixforge.pro.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.remixforge.pro.data.model.RemixPreset
import com.remixforge.pro.data.repository.PresetRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onPresetSelected: (Uri, RemixPreset) -> Unit
) {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) selectedUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RemixForge Pro") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { picker.launch(arrayOf("audio/*")) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "انتخاب آهنگ")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // کارت انتخاب فایل
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                if (selectedUri == null) "آهنگی انتخاب نشده"
                                else "آهنگ انتخاب شد ✓",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                if (selectedUri == null) "دکمه + پایین رو بزن"
                                else "حالا یه پریست انتخاب کن",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "پریست‌های ریمیکس:",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(PresetRepository.presets) { preset ->
                    PresetCard(
                        preset = preset,
                        enabled = selectedUri != null,
                        onClick = {
                            selectedUri?.let { uri -> onPresetSelected(uri, preset) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetCard(
    preset: RemixPreset,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                preset.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                preset.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row {
                AssistChip(
                    onClick = {},
                    label = { Text("${preset.tempo}x", style = MaterialTheme.typography.labelSmall) }
                )
                Spacer(Modifier.width(4.dp))
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            "${preset.pitchSemitones} st",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            }
        }
    }
}
