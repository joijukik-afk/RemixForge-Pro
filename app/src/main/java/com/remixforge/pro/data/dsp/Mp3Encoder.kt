package com.remixforge.pro.data.dsp

import com.naman14.tandroidlame.TAndroidLame
import java.io.File
import java.io.OutputStream

object Mp3Encoder {
    
    fun encodeToMp3(input: FloatArray, sampleRate: Int, channels: Int, outputFile: File) {
        outputFile.outputStream().use { os ->
            encodeToMp3(input, sampleRate, channels, os)
        }
    }

    fun encodeToMp3(input: FloatArray, sampleRate: Int, channels: Int, outputStream: OutputStream) {
        val lame = TAndroidLame()
        lame.initEncoder(sampleRate, channels, sampleRate, 320, 2) // 320kbps
        
        val pcmBytes = floatArrayToShortArray(input)
        // TAndroidLame در نسخه فعلی به فایل نیاز دارد، بنابراین یک فایل موقت می‌سازیم
        val tempFile = File.createTempFile("temp", ".mp3")
        lame.encode(pcmBytes, pcmBytes.size, tempFile.outputStream())
        lame.flush(tempFile.outputStream())
        lame.close()
        
        // کپی کردن فایل موقت به خروجی نهایی
        tempFile.inputStream().copyTo(outputStream)
        tempFile.delete()
    }

    private fun floatArrayToShortArray(input: FloatArray): ShortArray {
        return ShortArray(input.size) { (input[it].coerceIn(-1f, 1f) * 32767).toInt().toShort() }
    }
}
