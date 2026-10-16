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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onPresetSelected: (String, RemixPreset) -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var localPath by remember { mutableStateOf<String?>(null) }
    var fileName by remember { mutableStateOf<String?>(null) }
    var isCopying by remember { mutableStateOf(false) }
    var copyError by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { selected ->
        if (selected != null) {
            isCopying = true
            copyError = null
            scope.launch {
                try {
                    val localFile = withContext(Dispatchers.IO) {
                        copyUriToCache(ctx, selected)
                    }
                    localPath = localFile.absolutePath
                    fileName = localFile.name
                } catch (e: Exception) {
                    copyError = "خطا در کپی: ${e.message}"
                } finally {
                    isCopying = false
                }
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("RemixForge Pro") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    Manifest.permission.READ_MEDIA_AUDIO
                else
                    Manifest.permission.READ_EXTERNAL_STORAGE

                val has = ContextCompat.checkSelfPermission(ctx, permission) ==
                        PackageManager.PERMISSION_GRANTED

                if (has) picker.launch(arrayOf("audio/*"))
                else permissionLauncher.launch(permission)
            }) {
                Icon(Icons.Default.Add, contentDescription = "add")
            }
        }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            when {
                                isCopying -> "در حال کپی فایل..."
                                localPath != null -> "آهنگ آماده ✓"
                                else -> "آهنگی انتخاب نشده"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            when {
                                isCopying -> "صبر کن..."
                                fileName != null -> fileName!!
                                else -> "دکمه + پایین"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2
                        )
                    }
                }
            }

            copyError?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
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
                        onClick = { localPath?.let { onPresetSelected(it, p) } },
                        enabled = localPath != null,
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

private fun copyUriToCache(ctx: android.content.Context, uri: Uri): File {
    val dir = File(ctx.cacheDir, "input").apply { mkdirs() }
    val name = queryName(ctx, uri) ?: "input_${System.currentTimeMillis()}.audio"
    val out = File(dir, name)

    ctx.contentResolver.openInputStream(uri)?.use { input ->
        out.outputStream().use { output ->
            input.copyTo(output)
        }
    } ?: throw Exception("نمی‌توان فایل را باز کرد")

    return out
}

private fun queryName(ctx: android.content.Context, uri: Uri): String? {
    return try {
        ctx.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else null
        }
    } catch (_: Exception) { null }
}
