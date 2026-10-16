package com.remixforge.pro.data.dsp

import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object WavWriter {
    fun write(file: File, samples: FloatArray, sr: Int, ch: Int) {
        val dataSize = samples.size * 2
        FileOutputStream(file).use { out ->
            val h = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
            h.put("RIFF".toByteArray()); h.putInt(36 + dataSize)
            h.put("WAVE".toByteArray()); h.put("fmt ".toByteArray())
            h.putInt(16); h.putShort(1); h.putShort(ch.toShort())
            h.putInt(sr); h.putInt(sr * ch * 2)
            h.putShort((ch * 2).toShort()); h.putShort(16)
            h.put("data".toByteArray()); h.putInt(dataSize)
            out.write(h.array())
            val pcm = ByteBuffer.allocate(samples.size * 2).order(ByteOrder.LITTLE_ENDIAN)
            for (s in samples) pcm.putShort((s.coerceIn(-1f, 1f) * 32767f).toInt().toShort())
            out.write(pcm.array())
        }
    }
}
