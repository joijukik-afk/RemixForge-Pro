package com.remixforge.pro.data.dsp

import kotlin.math.PI

/**
 * Freeverb — ریورب حرفه‌ای با 8 comb filter و 4 allpass
 * کیفیت بالا، قابل مقایسه با VST های معروف
 */
class ReverbProcessor(
    private val sampleRate: Int,
    private val wet: Float,
    private val roomSize: Float,
    private val damping: Float
) {

    private val scale = sampleRate / 44100f
    private val stereoSpread = 23

    // 8 Comb Filter (4 برای چپ، 4 برای راست)
    private val combDelaysL = intArrayOf(
        (1116 * scale).toInt(),
        (1188 * scale).toInt(),
        (1277 * scale).toInt(),
        (1356 * scale).toInt()
    )
    private val combDelaysR = combDelaysL.map { it + (stereoSpread * scale).toInt() }.toIntArray()

    private val combBufsL = combDelaysL.map { FloatArray(it.coerceAtLeast(1)) }
    private val combBufsR = combDelaysR.map { FloatArray(it.coerceAtLeast(1)) }
    private val combIdxL = IntArray(4)
    private val combIdxR = IntArray(4)
    private val combFilterStoreL = FloatArray(4)
    private val combFilterStoreR = FloatArray(4)

    // 4 Allpass
    private val apDelaysL = intArrayOf(
        (556 * scale).toInt(),
        (441 * scale).toInt(),
        (341 * scale).toInt(),
        (225 * scale).toInt()
    )
    private val apDelaysR = apDelaysL.map { it + (stereoSpread * scale).toInt() }.toIntArray()

    private val apBufsL = apDelaysL.map { FloatArray(it.coerceAtLeast(1)) }
    private val apBufsR = apDelaysR.map { FloatArray(it.coerceAtLeast(1)) }
    private val apIdxL = IntArray(4)
    private val apIdxR = IntArray(4)

    private val feedback = 0.5f + roomSize * 0.48f
    private val damp1 = damping * 0.4f
    private val damp2 = 1f - damp1
    private val wet1 = wet * (1f + 0.5f) / 2f
    private val wet2 = wet * (1f - 0.5f) / 2f
    private val dry = 1f - wet

    fun process(input: FloatArray): FloatArray {
        val out = FloatArray(input.size)
        val frames = input.size / 2

        for (f in 0 until frames) {
            val inL = input[f * 2]
            val inR = input[f * 2 + 1]

            var outL = 0f
            var outR = 0f

            // Comb filters
            for (c in 0..3) {
                // Left
                val bufL = combBufsL[c]
                val idxL = combIdxL[c]
                val yL = bufL[idxL]
                outL += yL
                combFilterStoreL[c] = yL * damp2 + combFilterStoreL[c] * damp1
                bufL[idxL] = inL + combFilterStoreL[c] * feedback
                combIdxL[c] = (idxL + 1) % bufL.size

                // Right
                val bufR = combBufsR[c]
                val idxR = combIdxR[c]
                val yR = bufR[idxR]
                outR += yR
                combFilterStoreR[c] = yR * damp2 + combFilterStoreR[c] * damp1
                bufR[idxR] = inR + combFilterStoreR[c] * feedback
                combIdxR[c] = (idxR + 1) % bufR.size
            }

            // Allpass filters
            for (a in 0..3) {
                // Left
                val bufL = apBufsL[a]
                val idxL = apIdxL[a]
                val bufOutL = bufL[idxL]
                val newOutL = outL - bufOutL * 0.5f
                bufL[idxL] = outL + bufOutL * 0.5f
                outL = newOutL
                apIdxL[a] = (idxL + 1) % bufL.size

                // Right
                val bufR = apBufsR[a]
                val idxR = apIdxR[a]
                val bufOutR = bufR[idxR]
                val newOutR = outR - bufOutR * 0.5f
                bufR[idxR] = outR + bufOutR * 0.5f
                outR = newOutR
                apIdxR[a] = (idxR + 1) % bufR.size
            }

            // Mix dry + wet
            out[f * 2] = inL * dry + outL * wet1 + outR * wet2
            out[f * 2 + 1] = inR * dry + outR * wet1 + outL * wet2
        }

        return out
    }
}

/**
 * Lowpass فیلتر باکیفیت (Biquad)
 */
class BiquadLowpass(private val cutoffHz: Int, private val sampleRate: Int) {

    private val omega = 2.0 * PI * cutoffHz / sampleRate
    private val alpha = kotlin.math.sin(omega) / (2.0 * 0.7071) // Q = 0.7071

    private val b0 = ((1.0 - kotlin.math.cos(omega)) / 2.0).toFloat()
    private val b1 = (1.0 - kotlin.math.cos(omega)).toFloat()
    private val b2 = b0
    private val a0 = (1.0 + alpha).toFloat()
    private val a1 = (-2.0 * kotlin.math.cos(omega)).toFloat()
    private val a2 = (1.0 - alpha).toFloat()

    private var x1 = 0f; private var x2 = 0f
    private var y1 = 0f; private var y2 = 0f

    fun processSample(input: Float): Float {
        val output = (b0 / a0) * input +
                (b1 / a0) * x1 +
                (b2 / a0) * x2 -
                (a1 / a0) * y1 -
                (a2 / a0) * y2

        x2 = x1; x1 = input
        y2 = y1; y1 = output
        return output
    }

    fun process(input: FloatArray): FloatArray {
        val out = FloatArray(input.size)
        for (i in input.indices) {
            out[i] = processSample(input[i])
        }
        return out
    }
}

/**
 * Highpass فیلتر (Biquad)
 */
class BiquadHighpass(private val cutoffHz: Int, private val sampleRate: Int) {

    private val omega = 2.0 * PI * cutoffHz / sampleRate
    private val alpha = kotlin.math.sin(omega) / (2.0 * 0.7071)

    private val b0 = ((1.0 + kotlin.math.cos(omega)) / 2.0).toFloat()
    private val b1 = (-(1.0 + kotlin.math.cos(omega))).toFloat()
    private val b2 = b0
    private val a0 = (1.0 + alpha).toFloat()
    private val a1 = (-2.0 * kotlin.math.cos(omega)).toFloat()
    private val a2 = (1.0 - alpha).toFloat()

    private var x1 = 0f; private var x2 = 0f
    private var y1 = 0f; private var y2 = 0f

    fun processSample(input: Float): Float {
        val output = (b0 / a0) * input +
                (b1 / a0) * x1 +
                (b2 / a0) * x2 -
                (a1 / a0) * y1 -
                (a2 / a0) * y2

        x2 = x1; x1 = input
        y2 = y1; y1 = output
        return output
    }

    fun process(input: FloatArray): FloatArray {
        val out = FloatArray(input.size)
        for (i in input.indices) {
            out[i] = processSample(input[i])
        }
        return out
    }
}
