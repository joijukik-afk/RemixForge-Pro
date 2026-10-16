package com.remixforge.pro.ui.screen

import android.net.Uri
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
fun EditorScreen(uri: Uri, preset: RemixPreset, onBack: () -> Unit, onExported: (File) -> Unit) {
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(preset.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
            Slider(value = tempo, onValueChange = { tempo = it }, valueRange = 0.5f..1.5f)

            Text("ریورب: ${"%.0f".format(reverbWet * 100)}%")
            Slider(value = reverbWet, onValueChange = { reverbWet = it }, valueRange = 0f..0.8f)

            Text("Lowpass: ${lowpass.toInt()} Hz")
            Slider(value = lowpass, onValueChange = { lowpass = it }, valueRange = 4000f..20000f)

            Spacer(Modifier.height(20.dp))

            if (busy) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(status, style = MaterialTheme.typography.bodySmall)
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            result?.let {
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
                    scope.launch {
                        try {
                            status = "دیکد..."
                            progress = 0.1f
                            val decoded = withContext(Dispatchers.IO) {
                                AudioDecoder.decode(ctx, uri)
                            }
                            progress = 0.3f
                            status = "پردازش..."
                            val modified = preset.copy(
                                tempo = tempo,
                                reverbWet = reverbWet,
                                lowpassHz = lowpass.toInt()
                            )
                            val processed = withContext(Dispatchers.Default) {
                                DspChain(modified, decoded.sampleRate)
                                    .process(decoded.samples, decoded.channels)
                            }
                            progress = 0.8f
                            status = "ذخیره..."
                            val wav = File(
                                FileUtils.remixDir(ctx),
                                "remix_${System.currentTimeMillis()}.wav"
                            )
                            withContext(Dispatchers.IO) {
                                WavWriter.write(wav, processed, decoded.sampleRate, decoded.channels)
                            }
                            progress = 1f
                            status = "تمام ✓"
                            result = wav
                            onExported(wav)
                        } catch (e: Exception) {
                            error = e.message ?: "خطا"
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
