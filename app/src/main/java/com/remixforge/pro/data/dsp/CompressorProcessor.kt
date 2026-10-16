package com.remixforge.pro.data.dsp

import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow

/**
 * کمپرسور دینامیک حرفه‌ای
 * با threshold, ratio, attack, release, makeup gain
 */
class CompressorProcessor(
    private val thresholdDb: Float,
    private val ratio: Float,
    private val attackMs: Float,
    private val releaseMs: Float,
    private val sampleRate: Int
) {

    private val attackCoef = kotlin.math.exp(-1.0 / (sampleRate * attackMs / 1000.0)).toFloat()
    private val releaseCoef = kotlin.math.exp(-1.0 / (sampleRate * releaseMs / 1000.0)).toFloat()

    private var envelope = 0f

    fun process(input: FloatArray): FloatArray {
        val out = FloatArray(input.size)

        for (i in input.indices) {
            val sample = input[i]
            val inputLevel = abs(sample)

            // Envelope follower
            if (inputLevel > envelope) {
                envelope = attackCoef * envelope + (1 - attackCoef) * inputLevel
            } else {
                envelope = releaseCoef * envelope + (1 - releaseCoef) * inputLevel
            }

            // Convert to dB
            val envelopeDb = if (envelope > 0.00001f) {
                20f * log10(envelope.toDouble()).toFloat()
            } else {
                -100f
            }

            // Gain computation
            var gainDb = 0f
            if (envelopeDb > thresholdDb) {
                val overshoot = envelopeDb - thresholdDb
                gainDb = -overshoot * (1f - 1f / ratio)
            }

            // Apply gain
            val gainLinear = 10f.pow(gainDb / 20f)
            out[i] = sample * gainLinear
        }

        return out
    }

    fun processStereo(input: FloatArray): FloatArray {
        val out = FloatArray(input.size)
        val frames = input.size / 2

        for (f in 0 until frames) {
            val L = input[f * 2]
            val R = input[f * 2 + 1]
            val peak = maxOf(abs(L), abs(R))

            if (peak > envelope) {
                envelope = attackCoef * envelope + (1 - attackCoef) * peak
            } else {
                envelope = releaseCoef * envelope + (1 - releaseCoef) * peak
            }

            val envelopeDb = if (envelope > 0.00001f) {
                20f * log10(envelope.toDouble()).toFloat()
            } else -100f

            var gainDb = 0f
            if (envelopeDb > thresholdDb) {
                val overshoot = envelopeDb - thresholdDb
                gainDb = -overshoot * (1f - 1f / ratio)
            }

            val gainLinear = 10f.pow(gainDb / 20f)
            out[f * 2] = L * gainLinear
            out[f * 2 + 1] = R * gainLinear
        }

        return out
    }
}

/**
 * لیمیتر برای جلوگیری از کلیپ
 */
class LimiterProcessor(
    private val ceilingDb: Float,
    private val sampleRate: Int
) {

    private val ceilingLinear = 10f.pow(ceilingDb / 20f)
    private val lookaheadSamples = (sampleRate * 0.005f).toInt() // 5ms lookahead
    private val lookaheadBuffer = FloatArray(lookaheadSamples)
    private var writeIdx = 0

    fun process(input: FloatArray): FloatArray {
        val out = FloatArray(input.size)

        for (i in input.indices) {
            // Write to lookahead buffer
            lookaheadBuffer[writeIdx] = input[i]

            // Find peak in lookahead window
            var peak = 0f
            for (s in lookaheadBuffer) {
                val a = abs(s)
                if (a > peak) peak = a
            }

            // Compute gain
            val gain = if (peak > ceilingLinear) ceilingLinear / peak else 1f

            // Read from buffer (delayed)
            val readIdx = (writeIdx + 1) % lookaheadSamples
            out[i] = lookaheadBuffer[readIdx] * gain

            writeIdx = readIdx
        }

        return out
    }
}
