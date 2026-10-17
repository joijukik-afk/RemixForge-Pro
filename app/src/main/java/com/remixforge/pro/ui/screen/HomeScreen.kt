package com.remixforge.pro.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import androidx.core.content.ContextCompat
import com.remixforge.pro.data.model.RemixPreset
import com.remixforge.pro.data.repository.PresetRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onPresetSelected: (Uri, RemixPreset) -> Unit) {
    val ctx = LocalContext.current
    var uri by remember { mutableStateOf<Uri?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { if (it != null) uri = it }

    Scaffold(
        topBar = { TopAppBar(title = { Text("RemixForge Pro") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val perm = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
                if (ContextCompat.checkSelfPermission(ctx, perm) == PackageManager.PERMISSION_GRANTED) {
                    picker.launch(arrayOf("audio/*"))
                } else {
                    // درخواست مجوز ساده
                }
            }) { Icon(Icons.Default.Add, null) }
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp)) {
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MusicNote, null)
                    Spacer(Modifier.width(12.dp))
                    Text(if (uri == null) "آهنگی انتخاب نشده" else "آهنگ انتخاب شد ✓")
                }
            }
            Spacer(Modifier.height(16.dp))
            LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(PresetRepository.presets) { p ->
                    Card(onClick = { uri?.let { onPresetSelected(it, p) } }, enabled = uri != null) {
                        Column(Modifier.padding(12.dp)) {
                            Text(p.name, style = MaterialTheme.typography.titleMedium)
                            Text(p.description, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
