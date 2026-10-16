package com.remixforge.pro.domain.usecase

import android.content.Context
import android.net.Uri
import com.remixforge.pro.data.dsp.AudioDecoder
import com.remixforge.pro.data.dsp.DspChain
import com.remixforge.pro.data.model.RemixPreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ProcessRemixUseCase(private val context: Context) {

    sealed class Progress {
        data class Decoding(val progress: Float) : Progress()
        data class Processing(val progress: Float) : Progress()
        data class Done(val outputFile: File) : Progress()
        data class Error(val message: String) : Progress()
    }

    suspend fun execute(
        inputUri: Uri,
        preset: RemixPreset,
        outputFile: File,
        onProgress: (Progress) -> Unit
    ) {
        withContext(Dispatchers.Default) {
            try {
                onProgress(Progress.Decoding(0.1f))
                val decoded = AudioDecoder.decode(context, inputUri)

                onProgress(Progress.Processing(0.3f))
                val chain = DspChain(preset, decoded.sampleRate)
                val processed = chain.process(decoded.samples, decoded.channels)

                onProgress(Progress.Processing(0.8f))

                // ذخیره به عنوان WAV خام — انکود MP3 در مرحله بعد
                withContext(Dispatchers.IO) {
                    WavWriter.write(outputFile, processed, decoded.sampleRate, decoded.channels)
                }

                onProgress(Progress.Done(outputFile))

            } catch (e: Exception) {
                onProgress(Progress.Error(e.message ?: "خطای ناشناخته"))
            }
        }
    }
}
