package com.remixforge.pro.data.dsp

import com.remixforge.pro.data.model.RemixPreset
import kotlin.math.abs

class DspChain(private val preset: RemixPreset, private val sr: Int) {
    fun process(input: FloatArray, channels: Int): FloatArray {
        var s = input

        if (preset.highpassHz > 20) s = BiquadHighpass(preset.highpassHz, sr).process(s)
        if (preset.eqBands.isNotEmpty()) s = EQProcessor(sr, preset.eqBands).process(s)

        s = CompressorProcessor(preset.compThresholdDb, preset.compRatio,
            preset.compAttackMs, preset.compReleaseMs, sr).processStereo(s)

        if (preset.tempo != 1.0f || preset.pitchSemitones != 0f) {
            val ts = TimeStretchProcessor(sr, channels)
            ts.init()
            ts.setTempo(preset.tempo)
            ts.setPitch(preset.pitchSemitones)
            val chunk = 44100 * channels
            val out = ArrayList<Float>()
            var off = 0
            while (off < s.size) {
                val end = (off + chunk).coerceAtMost(s.size)
                out.addAll(ts.process(s.copyOfRange(off, end)).toList())
                off = end
            }
            out.addAll(ts.flush().toList())
            ts.release()
            s = out.toFloatArray()
        }

        if (preset.reverbWet > 0.01f) {
            s = ReverbProcessor(sr, preset.reverbWet, preset.reverbRoomSize, preset.reverbDamping).process(s)
        }
        if (preset.lowpassHz < 20000) s = BiquadLowpass(preset.lowpassHz, sr).process(s)
        s = LimiterProcessor(preset.limiterCeilingDb, sr).process(s)

        var peak = 0f
        for (v in s) { val a = abs(v); if (a > peak) peak = a }
        if (peak > 0.0001f) {
            val g = 0.95f / peak
            for (i in s.indices) s[i] *= g
        }
        return s
    }
}
