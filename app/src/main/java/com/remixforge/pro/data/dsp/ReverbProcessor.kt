package com.remixforge.pro.data.dsp

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class ReverbProcessor(sr: Int, wet: Float, roomSize: Float, damping: Float) {
    private val scale = sr / 44100f
    private val spread = 23

    private val combDL = intArrayOf((1116 * scale).toInt(), (1188 * scale).toInt(),
        (1277 * scale).toInt(), (1356 * scale).toInt())
    private val combDR = combDL.map { it + (spread * scale).toInt() }.toIntArray()
    private val combBL = combDL.map { FloatArray(it.coerceAtLeast(1)) }
    private val combBR = combDR.map { FloatArray(it.coerceAtLeast(1)) }
    private val combIL = IntArray(4); private val combIR = IntArray(4)
    private val storeL = FloatArray(4); private val storeR = FloatArray(4)

    private val apDL = intArrayOf((556 * scale).toInt(), (441 * scale).toInt(),
        (341 * scale).toInt(), (225 * scale).toInt())
    private val apDR = apDL.map { it + (spread * scale).toInt() }.toIntArray()
    private val apBL = apDL.map { FloatArray(it.coerceAtLeast(1)) }
    private val apBR = apDR.map { FloatArray(it.coerceAtLeast(1)) }
    private val apIL = IntArray(4); private val apIR = IntArray(4)

    private val fb = 0.5f + roomSize * 0.48f
    private val d1 = damping * 0.4f
    private val d2 = 1f - d1
    private val w1 = wet * 0.75f
    private val w2 = wet * 0.25f
    private val dry = 1f - wet

    fun process(input: FloatArray): FloatArray {
        val out = FloatArray(input.size)
        val frames = input.size / 2
        for (f in 0 until frames) {
            val inL = input[f * 2]; val inR = input[f * 2 + 1]
            var outL = 0f; var outR = 0f
            for (c in 0..3) {
                val bL = combBL[c]; val iL = combIL[c]
                val yL = bL[iL]; outL += yL
                storeL[c] = yL * d2 + storeL[c] * d1
                bL[iL] = inL + storeL[c] * fb
                combIL[c] = (iL + 1) % bL.size

                val bR = combBR[c]; val iR = combIR[c]
                val yR = bR[iR]; outR += yR
                storeR[c] = yR * d2 + storeR[c] * d1
                bR[iR] = inR + storeR[c] * fb
                combIR[c] = (iR + 1) % bR.size
            }
            for (a in 0..3) {
                val bL = apBL[a]; val iL = apIL[a]
                val oL = bL[iL]; val nL = outL - oL * 0.5f
                bL[iL] = outL + oL * 0.5f; outL = nL
                apIL[a] = (iL + 1) % bL.size

                val bR = apBR[a]; val iR = apIR[a]
                val oR = bR[iR]; val nR = outR - oR * 0.5f
                bR[iR] = outR + oR * 0.5f; outR = nR
                apIR[a] = (iR + 1) % bR.size
            }
            out[f * 2] = inL * dry + outL * w1 + outR * w2
            out[f * 2 + 1] = inR * dry + outR * w1 + outL * w2
        }
        return out
    }
}

class BiquadLowpass(cutoffHz: Int, sr: Int) {
    private val omega = 2.0 * PI * cutoffHz / sr
    private val alpha = sin(omega) / (2.0 * 0.7071)
    private val b0 = ((1.0 - cos(omega)) / 2.0).toFloat()
    private val b1 = (1.0 - cos(omega)).toFloat()
    private val b2 = b0
    private val a0 = (1.0 + alpha).toFloat()
    private val a1 = (-2.0 * cos(omega)).toFloat()
    private val a2 = (1.0 - alpha).toFloat()
    private var x1 = 0f; private var x2 = 0f; private var y1 = 0f; private var y2 = 0f

    fun process(input: FloatArray): FloatArray {
        val out = FloatArray(input.size)
        for (i in input.indices) {
            val x = input[i]
            val y = (b0 / a0) * x + (b1 / a0) * x1 + (b2 / a0) * x2 -
                    (a1 / a0) * y1 - (a2 / a0) * y2
            x2 = x1; x1 = x; y2 = y1; y1 = y
            out[i] = y
        }
        return out
    }
}

class BiquadHighpass(cutoffHz: Int, sr: Int) {
    private val omega = 2.0 * PI * cutoffHz / sr
    private val alpha = sin(omega) / (2.0 * 0.7071)
    private val b0 = ((1.0 + cos(omega)) / 2.0).toFloat()
    private val b1 = (-(1.0 + cos(omega))).toFloat()
    private val b2 = b0
    private val a0 = (1.0 + alpha).toFloat()
    private val a1 = (-2.0 * cos(omega)).toFloat()
    private val a2 = (1.0 - alpha).toFloat()
    private var x1 = 0f; private var x2 = 0f; private var y1 = 0f; private var y2 = 0f

    fun process(input: FloatArray): FloatArray {
        val out = FloatArray(input.size)
        for (i in input.indices) {
            val x = input[i]
            val y = (b0 / a0) * x + (b1 / a0) * x1 + (b2 / a0) * x2 -
                    (a1 / a0) * y1 - (a2 / a0) * y2
            x2 = x1; x1 = x; y2 = y1; y1 = y
            out[i] = y
        }
        return out
    }
}
