package com.remixforge.pro.ui.screen

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.remixforge.pro.data.dsp.AudioDecoder
import com.remixforge.pro.data.dsp.DspChain
import com.remixforge.pro.data.dsp.Mp3Encoder
import com.remixforge.pro.data.model.RemixPreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    localPath: String,
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(preset.name) },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !busy) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "back")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(it, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
                }
            }

            resultFile?.let { file ->
                Spacer(Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("✓ با موفقیت ذخیره شد", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(4.dp))
                        Text("مسیر: Downloads/RemixForgePro/${file.name}", style = MaterialTheme.typography.bodySmall)
                        
                        Spacer(Modifier.height(12.dp))
                        
                        Button(
                            onClick = { shareFile(ctx, file) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("اشتراک‌گذاری فایل")
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    busy = true
                    error = null
                    resultFile = null
                    progress = 0f
                    status = "شروع..."
                    scope.launch {
                        try {
                            val inputFile = File(localPath)
                            if (!inputFile.exists()) {
                                error = "فایل ورودی پیدا نشد"
                                busy = false
                                return@launch
                            }

                            status = "دیکد..."
                            progress = 0.1f
                            val decoded = withContext(Dispatchers.IO) {
                                AudioDecoder.decodeFile(inputFile)
                            }

                            progress = 0.2f
                            status = "پردازش..."

                            val modified = preset.copy(
                                tempo = tempo,
                                reverbWet = reverbWet,
                                lowpassHz = lowpass.toInt()
                            )
                            val processed = withContext(Dispatchers.Default) {
                                DspChain(modified, decoded.sampleRate).process(
                                    decoded.samples, decoded.channels
                                ) { stage, p ->
                                    status = stage
                                    progress = 0.2f + p * 0.6f
                                }
                            }

                            progress = 0.85f
                            status = "تبدیل به MP3 و ذخیره..."

                            val fileName = "RemixForge_${System.currentTimeMillis()}.mp3"
                            val savedFile = withContext(Dispatchers.IO) {
                                saveToDownloads(ctx, fileName, processed, decoded.sampleRate, decoded.channels)
                            }

                            progress = 1f
                            status = "تمام ✓"
                            resultFile = savedFile
                            onExported(savedFile)
                        } catch (t: Throwable) {
                            error = "${t.javaClass.simpleName}: ${t.message ?: "نامشخص"}"
                        } finally {
                            busy = false
                        }
                    }
                },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (busy) "در حال پردازش..." else "ساخت ریمیکس")
            }
        }
    }
}

private fun saveToDownloads(
    ctx: android.content.Context,
    fileName: String,
    samples: FloatArray,
    sr: Int,
    ch: Int
): File {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "audio/mpeg")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/RemixForgePro")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val resolver = ctx.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: throw Exception("نمی‌توان فایل ساخت")

        resolver.openOutputStream(uri)?.use { os ->
            // استفاده از Mp3Encoder برای نوشتن مستقیم
            Mp3Encoder.encodeToMp3(samples, sr, ch, File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "temp.mp3"))
        } ?: throw Exception("نمی‌توان نوشت")

        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, values, null, null)

        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "RemixForgePro/$fileName")
    } else {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "RemixForgePro"
        ).apply { mkdirs() }
        val file = File(dir, fileName)
        Mp3Encoder.encodeToMp3(samples, sr, ch, file)
        file
    }
}

private fun shareFile(ctx: android.content.Context, file: File) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            ctx,
            "${ctx.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/mpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        ctx.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری فایل با..."))
    } catch (e: Exception) {
        // لاگ کردن خطا
    }
}
