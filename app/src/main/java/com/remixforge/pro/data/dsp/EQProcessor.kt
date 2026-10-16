package com.remixforge.pro.data.dsp

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

/**
 * اکولایزر 10 باندی حرفه‌ای
 * هر باند یک فیلتر Peaking EQ با Q قابل تنظیم
 */
class EQProcessor(
    private val sampleRate: Int,
    private val bands: Map<Int, Float> // Hz -> dB
) {

    private data class Biquad(
        val b0: Float, val b1: Float, val b2: Float,
        val a1: Float, val a2: Float
    ) {
        var x1 = 0f; var x2 = 0f
        var y1 = 0f; var y2 = 0f

        fun process(input: Float): Float {
            val out = b0 * input + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2
            x2 = x1; x1 = input
            y2 = y1; y1 = out
            return out
        }

        fun reset() {
            x1 = 0f; x2 = 0f; y1 = 0f; y2 = 0f
        }
    }

    private val filters: List<Biquad> = bands.map { (freq, gainDb) ->
        createPeakingFilter(freq.toFloat(), gainDb, 1.0f)
    }

    private fun createPeakingFilter(freq: Float, gainDb: Float, q: Float): Biquad {
        val A = 10f.pow(gainDb / 40f)
        val omega = 2.0 * PI * freq / sampleRate
        val alpha = sin(omega) / (2.0 * q)

        val b0 = 1 + alpha * A
        val b1 = -2 * cos(omega)
        val b2 = 1 - alpha * A
        val a0 = 1 + alpha / A
        val a1 = -2 * cos(omega)
        val a2 = 1 - alpha / A

        return Biquad(
            (b0 / a0).toFloat(),
            (b1 / a0).toFloat(),
            (b2 / a0).toFloat(),
            (a1 / a0).toFloat(),
            (a2 / a0).toFloat()
        )
    }

    fun process(input: FloatArray): FloatArray {
        val out = FloatArray(input.size)
        val frames = input.size / 2

        for (f in 0 until frames) {
            var L = input[f * 2]
            var R = input[f * 2 + 1]

            for (filter in filters) {
                L = filter.process(L)
                R = filter.process(R)
            }

            out[f * 2] = L
            out[f * 2 + 1] = R
        }

        return out
    }

    fun reset() {
        filters.forEach { it.reset() }
    }
}
