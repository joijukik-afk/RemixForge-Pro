package com.remixforge.pro.data.dsp

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

class EQProcessor(sr: Int, bands: Map<Int, Float>) {
    private class Biquad(b0: Float, b1: Float, b2: Float, a1: Float, a2: Float) {
        private val B0=b0; private val B1=b1; private val B2=b2
        private val A1=a1; private val A2=a2
        private var x1=0f; private var x2=0f; private var y1=0f; private var y2=0f
        fun process(x: Float): Float {
            val y = B0*x + B1*x1 + B2*x2 - A1*y1 - A2*y2
            x2=x1; x1=x; y2=y1; y1=y
            return y
        }
    }

    private val filters = bands.map { (freq, gDb) ->
        val A = 10f.pow(gDb / 40f)
        val w = 2.0 * PI * freq / sr
        val alpha = sin(w) / 2.0
        val b0 = 1 + alpha * A
        val b1 = -2 * cos(w)
        val b2 = 1 - alpha * A
        val a0 = 1 + alpha / A
        val a1 = -2 * cos(w)
        val a2 = 1 - alpha / A
        Biquad((b0/a0).toFloat(), (b1/a0).toFloat(), (b2/a0).toFloat(),
            (a1/a0).toFloat(), (a2/a0).toFloat())
    }

    fun process(input: FloatArray): FloatArray {
        val out = FloatArray(input.size)
        for (f in 0 until input.size / 2) {
            var L = input[f * 2]; var R = input[f * 2 + 1]
            for (flt in filters) { L = flt.process(L); R = flt.process(R) }
            out[f * 2] = L; out[f * 2 + 1] = R
        }
        return out
    }
}
