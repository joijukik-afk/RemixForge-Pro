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
import androidx.compose.ui.unit.dp
import com.remixforge.pro.data.model.RemixPreset
import com.remixforge.pro.data.repository.PresetRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onPresetSelected: (Uri, RemixPreset) -> Unit) {
    var uri by remember { mutableStateOf<Uri?>(null) }
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { if (it != null) uri = it }

    Scaffold(
        topBar = { TopAppBar(title = { Text("RemixForge Pro") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { picker.launch(arrayOf("audio/*")) }) {
                Icon(Icons.Default.Add, "add")
            }
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp)) {
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MusicNote, null)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(if (uri == null) "آهنگی انتخاب نشده" else "آهنگ انتخاب شد ✓",
                            style = MaterialTheme.typography.titleMedium)
                        Text(if (uri == null) "دکمه + پایین" else "یه پریست انتخاب کن",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("پریست‌ها:", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(PresetRepository.presets) { p ->
                    Card(
                        onClick = { uri?.let { onPresetSelected(it, p) } },
                        enabled = uri != null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(p.name, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(p.description, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
