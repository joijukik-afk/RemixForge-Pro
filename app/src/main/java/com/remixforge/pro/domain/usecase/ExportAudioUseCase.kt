package com.remixforge.pro.domain.usecase

import android.content.Context
import android.media.MediaScannerConnection
import com.remixforge.pro.data.dsp.AudioEncoder
import com.remixforge.pro.data.dsp.AudioDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ExportAudioUseCase(private val context: Context) {

    /**
     * ورودی: فایل WAV پردازش‌شده
     * خروجی: MP3 در پوشه Music/RemixForgePro
     */
    suspend fun exportToMp3(
        wavFile: File,
        sampleRate: Int,
        channels: Int,
        bitrateKbps: Int = 320
    ): File = withContext(Dispatchers.IO) {

        // Decode WAV خام
        val decoded = AudioDecoder.decodeFile(wavFile, sampleRate, channels)
        val encoder = AudioEncoder()

        val musicDir = File(
            android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_MUSIC
            ),
            "RemixForgePro"
        ).apply { mkdirs() }

        val outputFile = File(
            musicDir,
            "RemixForge_${System.currentTimeMillis()}.mp3"
        )

        encoder.encodeToMp3(
            decoded,
            sampleRate,
            channels,
            bitrateKbps,
            outputFile
        )

        // اعلام به MediaStore
        MediaScannerConnection.scanFile(
            context,
            arrayOf(outputFile.absolutePath),
            arrayOf("audio/mpeg"),
            null
        )

        outputFile
    }
}
