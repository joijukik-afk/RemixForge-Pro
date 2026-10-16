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
import com.remixforge.pro.data.dsp.AudioEncoder
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
    var progress by remember { mutableFloatStateOf(0f) }
    var result by remember { mutableStateOf<File?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    var tempo by remember { mutableFloatStateOf(preset.tempo) }
    var pitch by remember { mutableFloatStateOf(preset.pitchSemitones) }
    var reverbWet by remember { mutableFloatStateOf(preset.reverbWet) }
    var lowpass by remember { mutableIntStateOf(preset.lowpassHz) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(preset.name) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "back") } }
            )
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text("سرعت: ${"%.2f".format(tempo)}x")
            Slider(tempo, { tempo = it }, valueRange = 0.5f..1.5f)
            Text("پیچ: ${"%.1f".format(pitch)} نیم‌پرده")
            Slider(pitch, { pitch = it }, valueRange = -8f..8f)
            Text("ریورب: ${"%.0f".format(reverbWet * 100)}%")
            Slider(reverbWet, { reverbWet = it }, valueRange = 0f..0.8f)
            Text("Lowpass: $lowpass Hz")
            Slider(lowpass.toFloat(), { lowpass = it.toInt() }, valueRange = 4000f..20000f)

            Spacer(Modifier.height(20.dp))
            if (busy) {
                LinearProgressIndicator({ progress }, Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Text(status, style = MaterialTheme.typography.bodySmall)
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            result?.let {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("✓ آماده شد", style = MaterialTheme.typography.titleSmall)
                        Text(it.absolutePath, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    busy = true; error = null; result = null; progress = 0f
                    scope.launch {
                        try {
                            status = "دیکد..."
                            progress = 0.1f
                            val decoded = withContext(Dispatchers.IO) { AudioDecoder.decode(ctx, uri) }
                            progress = 0.3f; status = "پردازش..."
                            val modified = preset.copy(tempo = tempo, pitchSemitones = pitch,
                                reverbWet = reverbWet, lowpassHz = lowpass)
                            val processed = withContext(Dispatchers.Default) {
                                DspChain(modified, decoded.sampleRate).process(decoded.samples, decoded.channels)
                            }
                            progress = 0.6f; status = "WAV..."
                            val wav = File(FileUtils.remixDir(ctx), "remix_${System.currentTimeMillis()}.wav")
                            withContext(Dispatchers.IO) {
                                WavWriter.write(wav, processed, decoded.sampleRate, decoded.channels)
                            }
                            progress = 0.8f; status = "MP3..."
                            val mp3 = File(FileUtils.exportDir(), "RemixForge_${System.currentTimeMillis()}.mp3")
                            withContext(Dispatchers.IO) {
                                AudioEncoder().encodeToMp3(processed, decoded.sampleRate, decoded.channels, 320, mp3)
                            }
                            progress = 1f; status = "تمام ✓"
                            result = mp3
                            onExported(mp3)
                        } catch (e: Exception) {
                            error = e.message ?: "خطا"
                        } finally { busy = false }
                    }
                },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (busy) "در حال پردازش..." else "ساخت ریمیکس") }
        }
    }
}
