package com.remixforge.pro.data.dsp

/**
 * تغییر سرعت و پیچ بدون NDK
 * با Linear Interpolation (کیفیت متوسط، ولی کار می‌کنه)
 */
object SimpleStretch {

    /**
     * تغییر سرعت با تغییر پیچ همزمان (مثل Slowed/Nightcore)
     * speed > 1 = سریع‌تر و بالاتر
     * speed < 1 = کندتر و پایین‌تر
     */
    fun process(input: FloatArray, speed: Float, channels: Int): FloatArray {
        if (speed == 1.0f) return input
        val outLen = (input.size / speed).toInt().coerceAtLeast(1)
        val out = FloatArray(outLen)
        for (i in 0 until outLen) {
            val pos = i * speed
            val i0 = pos.toInt()
            val i1 = (i0 + 1).coerceAtMost(input.size - 1)
            val frac = pos - i0
            out[i] = if (i0 < input.size) input[i0] * (1f - frac) + input[i1] * frac else 0f
        }
        return out
    }
}
