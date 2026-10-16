package com.remixforge.pro.data.dsp

import java.io.File

/**
 * انکودر MP3 با LAME NDK
 */
class AudioEncoder {

    private var nativeHandle: Long = 0L
    private var initialized = false

    companion object {
        init {
            System.loadLibrary("remixforge")
        }
    }

    private external fun nativeCreate(): Long
    private external fun nativeInit(handle: Long, sampleRate: Int, channels: Int, bitrate: Int): Boolean
    private external fun nativeEncode(handle: Long, pcm: ShortArray): ByteArray?
    private external fun nativeFinish(handle: Long): ByteArray?
    private external fun nativeDestroy(handle: Long)

    fun encodeToMp3(
        samples: FloatArray,
        sampleRate: Int,
        channels: Int,
        bitrateKbps: Int,
        outputFile: File
    ) {
        nativeHandle = nativeCreate()

        val ok = nativeInit(nativeHandle, sampleRate, channels, bitrateKbps)
        require(ok) { "LAME init نشد" }

        outputFile.outputStream().use { out ->

            // به صورت chunk پردازش کن تا حافظه منفجر نشه
            val chunkSize = 8192 * channels
            var offset = 0

            while (offset < samples.size) {
                val end = (offset + chunkSize).coerceAtMost(samples.size)
                val chunk = samples.copyOfRange(offset, end)
                val shorts = FloatArrayToShorts(chunk)
                val mp3Bytes = nativeEncode(nativeHandle, shorts)
                if (mp3Bytes != null && mp3Bytes.isNotEmpty()) {
                    out.write(mp3Bytes)
                }
                offset = end
            }

            val tail = nativeFinish(nativeHandle)
            if (tail != null && tail.isNotEmpty()) {
                out.write(tail)
            }
        }

        nativeDestroy(nativeHandle)
    }

    private fun FloatArrayToShorts(input: FloatArray): ShortArray {
        val result = ShortArray(input.size)
        for (i in input.indices) {
            result[i] = (input[i].coerceIn(-1f, 1f) * 32767f).toInt().toShort()
        }
        return result
    }
}
