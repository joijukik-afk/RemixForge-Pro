package com.remixforge.pro.data.dsp

import java.io.File

class AudioEncoder {
    private var handle: Long = 0L

    companion object {
        init { System.loadLibrary("remixforge") }
    }

    private external fun nativeCreate(): Long
    private external fun nativeInit(h: Long, sr: Int, ch: Int, br: Int): Boolean
    private external fun nativeEncode(h: Long, pcm: ShortArray): ByteArray?
    private external fun nativeFinish(h: Long): ByteArray?
    private external fun nativeDestroy(h: Long)

    fun encodeToMp3(samples: FloatArray, sr: Int, ch: Int, bitrate: Int, out: File) {
        handle = nativeCreate()
        require(nativeInit(handle, sr, ch, bitrate)) { "LAME init failed" }

        out.outputStream().use { os ->
            val chunk = 8192 * ch
            var off = 0
            while (off < samples.size) {
                val end = (off + chunk).coerceAtMost(samples.size)
                val part = samples.copyOfRange(off, end)
                val shorts = ShortArray(part.size) {
                    (part[it].coerceIn(-1f, 1f) * 32767f).toInt().toShort()
                }
                nativeEncode(handle, shorts)?.let { if (it.isNotEmpty()) os.write(it) }
                off = end
            }
            nativeFinish(handle)?.let { if (it.isNotEmpty()) os.write(it) }
        }
        nativeDestroy(handle)
    }
}
