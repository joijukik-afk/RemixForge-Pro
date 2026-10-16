package com.remixforge.pro.data.dsp

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.log10
import kotlin.math.pow

class CompressorProcessor(
    private val thresholdDb: Float,
    private val ratio: Float,
    attackMs: Float, releaseMs: Float, sr: Int
) {
    private val ac = exp(-1.0 / (sr * attackMs / 1000.0)).toFloat()
    private val rc = exp(-1.0 / (sr * releaseMs / 1000.0)).toFloat()
    private var env = 0f

    fun processStereo(input: FloatArray): FloatArray {
        val out = FloatArray(input.size)
        for (f in 0 until input.size / 2) {
            val L = input[f * 2]; val R = input[f * 2 + 1]
            val peak = maxOf(abs(L), abs(R))
            env = if (peak > env) ac * env + (1 - ac) * peak else rc * env + (1 - rc) * peak
            val envDb = if (env > 0.00001f) 20f * log10(env.toDouble()).toFloat() else -100f
            val gDb = if (envDb > thresholdDb) -(envDb - thresholdDb) * (1f - 1f / ratio) else 0f
            val g = 10f.pow(gDb / 20f)
            out[f * 2] = L * g
            out[f * 2 + 1] = R * g
        }
        return out
    }
}

class LimiterProcessor(ceilingDb: Float, sr: Int) {
    private val ceiling = 10f.pow(ceilingDb / 20f)
    private val look = (sr * 0.005f).toInt().coerceAtLeast(1)
    private val buf = FloatArray(look)
    private var idx = 0

    fun process(input: FloatArray): FloatArray {
        val out = FloatArray(input.size)
        for (i in input.indices) {
            buf[idx] = input[i]
            var peak = 0f
            for (s in buf) { val a = abs(s); if (a > peak) peak = a }
            val g = if (peak > ceiling) ceiling / peak else 1f
            val r = (idx + 1) % look
            out[i] = buf[r] * g
            idx = r
        }
        return out
    }
}
