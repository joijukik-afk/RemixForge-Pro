package com.remixforge.pro.data.dsp

import com.naman14.tandroidlame.TAndroidLame
import java.io.File

object Mp3Encoder {
    fun encodeToMp3(input: FloatArray, sampleRate: Int, channels: Int, outputFile: File) {
        val lame = TAndroidLame()
        lame.initEncoder(sampleRate, channels, sampleRate, 320, 2) // 320kbps
        
        val pcmBytes = floatArrayToShortArray(input)
        lame.encode(pcmBytes, pcmBytes.size, outputFile.outputStream())
        
        lame.flush(outputFile.outputStream())
        lame.close()
    }

    private fun floatArrayToShortArray(input: FloatArray): ShortArray {
        return ShortArray(input.size) { (input[it].coerceIn(-1f, 1f) * 32767).toInt().toShort() }
    }
}
