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
import com.remixforge.pro.data.model.RemixPreset
import com.remixforge.pro.data.model.RemixPreset
import com.remixforge.pro.domain.usecase.ProcessRemixUseCase
import com.remixforge.pro.util.FileUtils
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    uri: Uri,
    preset: RemixPreset,
    onBack: () -> Unit,
    onExported: (File) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isProcessing by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var statusText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var outputFile by remember { mutableStateOf<File?>(null) }

    // تنظیمات قابل تغییر
    var tempo by remember { mutableFloatStateOf(preset.tempo) }
    var pitch by remember { mutableFloatStateOf(preset.pitchSemitones) }
    var reverbWet by remember { mutableFloatStateOf(preset.reverbWet) }
    var lowpass by remember { mutableIntStateOf(preset.lowpassHz) }
    var compThreshold by remember { mutableFloatStateOf(preset.compThresholdDb) }
    var limiterCeiling by remember { mutableFloatStateOf(preset.limiterCeilingDb) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(preset.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            // ====== Time & Pitch ======
            SectionTitle("زمان و پیچ")
            SliderRow("سرعت (Tempo)", tempo, 0.5f..1.5f) { tempo = it }
            SliderRow("پیچ (Pitch)", pitch, -8f..8f) { pitch = it }
            Text("پیچ: ${"%.1f".format(pitch)} نیم‌پرده", style = MaterialTheme.typography.labelSmall)

            Spacer(Modifier.height(16.dp))

            // ====== Reverb ======
            SectionTitle("ریورب")
            SliderRow("میزان ریورب", reverbWet, 0f..0.8f) { reverbWet = it }

            Spacer(Modifier.height(16.dp))

            // ====== Filter ======
            SectionTitle("فیلتر")
            SliderRow("Lowpass (Hz)", lowpass.toFloat(), 4000f..20000f) {
                lowpass = it.toInt()
            }
            Text("${lowpass} Hz", style = MaterialTheme.typography.labelSmall)

            Spacer(Modifier.height(16.dp))

            // ====== Dynamics ======
            SectionTitle("دینامیک")
            SliderRow("کمپرسور (Threshold)", compThreshold, -30f..0f) { compThreshold = it }
            SliderRow("لیمیتر (Ceiling)", limiterCeiling, -6f..0f) { limiterCeiling = it }

            Spacer(Modifier.height(24.dp))

            // ====== Processing ======
            if (isProcessing) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(statusText, style = MaterialTheme.typography.bodySmall)
            }

            errorText?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            outputFile?.let { file ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("✓ ریمیکس آماده شد", style = MaterialTheme.typography.titleSmall)
                        Text(file.absolutePath, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    isProcessing = true
                    errorText = null
                    outputFile = null
                    progress = 0f
                    statusText = "در حال دیکد کردن..."

                    scope.launch {
                        val outDir = FileUtils.getRemixDir(context)
                        val outFile = File(outDir, "remix_${preset.id}_${System.currentTimeMillis()}.wav")

                        val modifiedPreset = preset.copy(
                            tempo = tempo,
                            pitchSemitones = pitch,
                            reverbWet = reverbWet,
                            lowpassHz = lowpass,
                            compThresholdDb = compThreshold,
                            limiterCeilingDb = limiterCeiling
                        )

                        val useCase = ProcessRemixUseCase(context)
                        useCase.execute(uri, modifiedPreset, outFile) { p ->
                            when (p) {
                                is ProcessRemixUseCase.Progress.Decoding -> {
                                    progress = p.progress
                                    statusText = "در حال دیکد کردن..."
                                }
                                is ProcessRemixUseCase.Progress.Processing -> {
                                    progress = p.progress
                                    statusText = "در حال پردازش DSP..."
                                }
                                is ProcessRemixUseCase.Progress.Done -> {
                                    progress = 1f
                                    statusText = "تمام شد ✓"
                                    outputFile = p.outputFile
                                    isProcessing = false
                                    onExported(p.outputFile)
                                }
                                is ProcessRemixUseCase.Progress.Error -> {
                                    errorText = p.message
                                    isProcessing = false
                                }
                            }
                        }
                    }
                },
                enabled = !isProcessing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isProcessing) "در حال پردازش..." else "شروع ریمیکس")
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Text(
            "$label: ${"%.2f".format(value)}",
            style = MaterialTheme.typography.labelMedium
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
