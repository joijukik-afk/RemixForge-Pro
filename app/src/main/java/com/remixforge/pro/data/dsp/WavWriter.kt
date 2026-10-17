package com.remixforge.pro.data.dsp

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object WavWriter {

    fun write(file: File, samples: FloatArray, sr: Int, ch: Int) {
        FileOutputStream(file).use { os ->
            writeToStream(os, samples, sr, ch)
        }
    }

    fun writeToStream(out: OutputStream, samples: FloatArray, sr: Int, ch: Int) {
        val dataSize = samples.size * 2

        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        header.put("RIFF".toByteArray())
        header.putInt(36 + dataSize)
        header.put("WAVE".toByteArray())
        header.put("fmt ".toByteArray())
        header.putInt(16)
        header.putShort(1)
        header.putShort(ch.toShort())
        header.putInt(sr)
        header.putInt(sr * ch * 2)
        header.putShort((ch * 2).toShort())
        header.putShort(16)
        header.put("data".toByteArray())
        header.putInt(dataSize)
        out.write(header.array())

        val chunkSize = 8192
        var i = 0
        val buffer = ByteBuffer.allocate(chunkSize * 2).order(ByteOrder.LITTLE_ENDIAN)
        while (i < samples.size) {
            buffer.clear()
            var count = 0
            while (count < chunkSize && i < samples.size) {
                buffer.putShort((samples[i].coerceIn(-1f, 1f) * 32767f).toInt().toShort())
                i++
                count++
            }
            out.write(buffer.array(), 0, count * 2)
        }
        out.flush()
    }
}
