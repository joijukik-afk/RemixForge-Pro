package com.remixforge.pro.ui.screen

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.remixforge.pro.data.dsp.AudioEngine
import com.remixforge.pro.data.model.RemixPreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    localUri: Uri,
    preset: RemixPreset,
    onBack: () -> Unit,
    onExported: (File) -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var progress by remember { mutableStateOf(0f) }
    var resultFile by remember { mutableStateOf<File?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    var tempo by remember { mutableStateOf(preset.tempo) }
    var reverbWet by remember { mutableStateOf(preset.reverbWet) }
    var lowpass by remember { mutableStateOf(preset.lowpassHz.toFloat()) }
    var removeVocals by remember { mutableStateOf(false) }
    var secondUri by remember { mutableStateOf<Uri?>(null) }

    val pickSecondAudio = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        if (it != null) secondUri = it
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(preset.name) }, navigationIcon = {
            IconButton(onClick = onBack, enabled = !busy) { Icon(Icons.Default.ArrowBack, null) }
        }) }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp)) {
            
            Button(onClick = { pickSecondAudio.launch(arrayOf("audio/*")) }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                Text(if (secondUri == null) "افزودن آهنگ دوم برای میکس" else "آهنگ دوم انتخاب شد ✓")
            }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = removeVocals, onCheckedChange = { removeVocals = it }, enabled = !busy)
                Text("حذف وکال (Karaoke Mode)")
            }

            Spacer(Modifier.height(16.dp))

            Text("سرعت: ${"%.2f".format(tempo)}x")
            Slider(value = tempo, onValueChange = { tempo = it }, valueRange = 0.5f..1.5f, enabled = !busy)

            Text("ریورب: ${"%.0f".format(reverbWet * 100)}%")
            Slider(value = reverbWet, onValueChange = { reverbWet = it }, valueRange = 0f..0.8f, enabled = !busy)

            Text("Lowpass: ${lowpass.toInt()} Hz")
            Slider(value = lowpass, onValueChange = { lowpass = it }, valueRange = 4000f..20000f, enabled = !busy)

            Spacer(Modifier.height(20.dp))

            if (busy) {
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Text("$status (${(progress * 100).toInt()}%)", style = MaterialTheme.typography.bodyMedium)
            }

            error?.let {
                Spacer(Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(it, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }

            resultFile?.let { file ->
                Spacer(Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("✓ ریمیکس با موفقیت ذخیره شد", style = MaterialTheme.typography.titleSmall)
                        Text("Downloads/RemixForgePro/${file.name}", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { shareFile(ctx, file) }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Share, null)
                            Spacer(Modifier.width(8.dp))
                            Text("اشتراک‌گذاری فایل MP3")
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    busy = true; error = null; resultFile = null; progress = 0f
                    scope.launch {
                        try {
                            val result = AudioEngine.processAudio(
                                context = ctx,
                                inputUri = localUri,
                                secondUri = secondUri,
                                tempo = tempo,
                                pitch = 0f,
                                reverb = reverbWet,
                                lowpass = lowpass.toInt(),
                                removeVocals = removeVocals
                            ) { p, s ->
                                progress = p
                                status = s
                            }

                            val savedFile = withContext(Dispatchers.IO) {
                                saveToDownloads(ctx, result.name, result)
                            }
                            resultFile = savedFile
                            onExported(savedFile)
                        } catch (t: Throwable) {
                            error = "خطا: ${t.message}"
                        } finally {
                            busy = false
                        }
                    }
                },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(if (busy) "در حال پردازش..." else "شروع ریمیکس حرفه‌ای (MP3)", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

private fun saveToDownloads(ctx: android.content.Context, fileName: String, sourceFile: File): File {
    val finalName = "RemixForge_${System.currentTimeMillis()}.mp3"
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, finalName)
            put(MediaStore.Downloads.MIME_TYPE, "audio/mpeg")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/RemixForgePro")
        }
        val uri = ctx.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)!!
        ctx.contentResolver.openOutputStream(uri)?.use { os -> sourceFile.inputStream().copyTo(os) }
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "RemixForgePro/$finalName")
    } else {
        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "RemixForgePro").apply { mkdirs() }
        val out = File(dir, finalName)
        sourceFile.copyTo(out, overwrite = true)
        out
    }
}

private fun shareFile(ctx: android.content.Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/mpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        ctx.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری با..."))
    } catch (_: Exception) {}
}
