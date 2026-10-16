package com.remixforge.pro.data.dsp

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Time-Stretch + Pitch-Shift با SoundTouch NDK
 * تغییر سرعت بدون تغییر پیچ و بالعکس
 */
class TimeStretchProcessor(private val sampleRate: Int, private val channels: Int) {

    private var nativeHandle: Long = 0L
    private var initialized = false

    companion object {
        init {
            System.loadLibrary("remixforge")
        }
    }

    private external fun nativeCreate(sampleRate: Int, channels: Int): Long
    private external fun nativeSetTempo(handle: Long, tempo: Float)
    private external fun nativeSetPitch(handle: Long, semitones: Float)
    private external fun nativeProcess(handle: Long, input: ShortArray): ShortArray
    private external fun nativeFlush(handle: Long): ShortArray
    private external fun nativeDestroy(handle: Long)

    fun init() {
        if (!initialized) {
            nativeHandle = nativeCreate(sampleRate, channels)
            initialized = true
        }
    }

    fun setTempo(tempo: Float) {
        require(tempo in 0.25f..4.0f) { "tempo باید بین 0.25 و 4.0 باشه" }
        nativeSetTempo(nativeHandle, tempo)
    }

    fun setPitch(semitones: Float) {
        require(semitones in -24f..24f) { "pitch باید بین -24 و +24 نیم‌پرده باشه" }
        nativeSetPitch(nativeHandle, semitones)
    }

    /**
     * ورودی: FloatArray نرمال‌شده (-1..1)
     * خروجی: FloatArray نرمال‌شده
     */
    fun process(input: FloatArray): FloatArray {
        val shorts = FloatArrayToShorts(input)
        val out = nativeProcess(nativeHandle, shorts)
        return ShortsToFloatArray(out)
    }

    fun flush(): FloatArray {
        val out = nativeFlush(nativeHandle)
        return ShortsToFloatArray(out)
    }

    fun release() {
        if (initialized) {
            nativeDestroy(nativeHandle)
            initialized = false
        }
    }

    private fun FloatArrayToShorts(input: FloatArray): ShortArray {
        val result = ShortArray(input.size)
        for (i in input.indices) {
            result[i] = (input[i].coerceIn(-1f, 1f) * 32767f).toInt().toShort()
        }
        return result
    }

    private fun ShortsToFloatArray(input: ShortArray): FloatArray {
        val result = FloatArray(input.size)
        for (i in input.indices) {
            result[i] = input[i] / 32768f
        }
        return result
    }
}
