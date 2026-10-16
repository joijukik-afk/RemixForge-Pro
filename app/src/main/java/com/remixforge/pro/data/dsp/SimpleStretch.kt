package com.remixforge.pro.data.dsp

object SimpleStretch {
    fun process(input: FloatArray, speed: Float, channels: Int): FloatArray {
        if (speed == 1.0f) return input
        val outLen = (input.size / speed).toInt().coerceAtLeast(1)
        val out = FloatArray(outLen)
        val maxIn = input.size - 1
        for (i in 0 until outLen) {
            val pos = i * speed
            val i0 = pos.toInt()
            if (i0 >= maxIn) {
                out[i] = 0f
            } else {
                val i1 = i0 + 1
                val frac = pos - i0
                out[i] = input[i0] * (1f - frac) + input[i1] * frac
            }
        }
        return out
    }
}
