package com.remixforge.pro.data.dsp

class TimeStretchProcessor(private val sampleRate: Int, private val channels: Int) {
    private var handle: Long = 0L
    private var init = false

    companion object {
        init { System.loadLibrary("remixforge") }
    }

    private external fun nativeCreate(sr: Int, ch: Int): Long
    private external fun nativeSetTempo(h: Long, tempo: Float)
    private external fun nativeSetPitch(h: Long, semitones: Float)
    private external fun nativeProcess(h: Long, input: ShortArray): ShortArray
    private external fun nativeFlush(h: Long): ShortArray
    private external fun nativeDestroy(h: Long)

    fun init() { if (!init) { handle = nativeCreate(sampleRate, channels); init = true } }
    fun setTempo(t: Float) { nativeSetTempo(handle, t) }
    fun setPitch(s: Float) { nativeSetPitch(handle, s) }

    fun process(input: FloatArray): FloatArray {
        val shorts = ShortArray(input.size) { (input[it].coerceIn(-1f, 1f) * 32767f).toInt().toShort() }
        val out = nativeProcess(handle, shorts)
        return FloatArray(out.size) { out[it] / 32768f }
    }

    fun flush(): FloatArray {
        val out = nativeFlush(handle)
        return FloatArray(out.size) { out[it] / 32768f }
    }

    fun release() { if (init) { nativeDestroy(handle); init = false } }
}
