package com.remixforge.pro.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.remixforge.pro.data.dsp.AudioDecoder
import com.remixforge.pro.data.dsp.DspChain
import com.remixforge.pro.data.dsp.WavWriter
import com.remixforge.pro.data.model.RemixPreset
import com.remixforge.pro.util.FileUtils
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
    var result by remember { mutableStateOf<File?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    var tempo by remember { mutableStateOf(preset.tempo) }
    var reverbWet by remember { mutableStateOf(preset.reverbWet) }
    var lowpass by remember { mutableStateOf(preset.lowpassHz.toFloat()) }

    // این متغیر باعث می‌شه Compose با هر تغییر progress، UI رو رفرش کنه
    var uiTick by remember { mutableStateOf(0) }

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
            Slider(
                value = tempo,
                onValueChange = { tempo = it },
                valueRange = 0.5f..1.5f,
                enabled = !busy
            )

            Text("ریورب: ${"%.0f".format(reverbWet * 100)}%")
            Slider(
                value = reverbWet,
                onValueChange = { reverbWet = it },
                valueRange = 0f..0.8f,
                enabled = !busy
            )

            Text("Lowpass: ${lowpass.toInt()} Hz")
            Slider(
                value = lowpass,
                onValueChange = { lowpass = it },
                valueRange = 4000f..20000f,
                enabled = !busy
            )

            Spacer(Modifier.height(20.dp))

            if (busy) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "$status  (${(progress * 100).toInt()}%)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            error?.let {
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        it,
                        Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            result?.let {
                Spacer(Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("✓ آماده شد", style = MaterialTheme.typography.titleSmall)
                        Text(it.absolutePath, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    busy = true
                    error = null
                    result = null
                    progress = 0f
                    status = "شروع..."
                    uiTick++

                    scope.launch {
                        try {
                            val inputFile = File(localPath)
                            if (!inputFile.exists()) {
                                error = "فایل ورودی پیدا نشد"
                                busy = false
                                return@launch
                            }

                            val sizeMb = inputFile.length() / (1024 * 1024)
                            if (sizeMb > 80) {
                                error = "فایل خیلی بزرگه (${sizeMb}MB). آهنگ کوتاه‌تر انتخاب کن."
                                busy = false
                                return@launch
                            }

                            status = "دیکد..."
                            progress = 0.1f
                            uiTick++
                            val decoded = withContext(Dispatchers.IO) {
                                AudioDecoder.decodeFile(inputFile)
                            }

                            val durationSec = decoded.samples.size / decoded.channels / decoded.sampleRate
                            if (durationSec > 480) {
                                error = "آهنگ بیش از ۸ دقیقه‌ست. کوتاه‌تر انتخاب کن."
                                busy = false
                                return@launch
                            }

                            progress = 0.2f
                            status = "پردازش..."
                            uiTick++

                            val modified = preset.copy(
                                tempo = tempo,
                                reverbWet = reverbWet,
                                lowpassHz = lowpass.toInt()
                            )
                            val processed = withContext(Dispatchers.Default) {
                                DspChain(modified, decoded.sampleRate).process(
                                    decoded.samples,
                                    decoded.channels
                                ) { stage, p ->
                                    // این callback داخل thread پس‌زمینه اجرا می‌شه
                                    // برای امنیت، مقادیر رو آپدیت می‌کنیم
                                    status = stage
                                    progress = 0.2f + p * 0.6f
                                    uiTick++
                                }
                            }

                            progress = 0.85f
                            status = "ذخیره..."
                            uiTick++
                            val wav = File(
                                FileUtils.remixDir(ctx),
                                "remix_${System.currentTimeMillis()}.wav"
                            )
                            withContext(Dispatchers.IO) {
                                WavWriter.write(wav, processed, decoded.sampleRate, decoded.channels)
                            }
                            progress = 1f
                            status = "تمام ✓"
                            uiTick++
                            result = wav
                            onExported(wav)
                        } catch (t: Throwable) {
                            val msg = when (t) {
                                is OutOfMemoryError -> "حافظه کافی نیست. آهنگ کوتاه‌تر انتخاب کن."
                                else -> "${t.javaClass.simpleName}: ${t.message ?: "نامشخص"}"
                            }
                            error = msg
                        } finally {
                            busy = false
                            uiTick++
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
