package com.remixforge.pro.data.dsp

import com.remixforge.pro.data.model.RemixPreset
import kotlin.math.abs
import kotlin.math.pow

class DspChain(private val preset: RemixPreset, private val sr: Int) {

    fun process(input: FloatArray, channels: Int): FloatArray {
        var s = input

        // 1. Highpass
        if (preset.highpassHz > 20) {
            s = BiquadHighpass(preset.highpassHz, sr).process(s)
        }

        // 2. EQ
        if (preset.eqBands.isNotEmpty()) {
            s = EQProcessor(sr, preset.eqBands).process(s)
        }

        // 3. Compressor
        s = CompressorProcessor(
            preset.compThresholdDb, preset.compRatio,
            preset.compAttackMs, preset.compReleaseMs, sr
        ).processStereo(s)

        // 4. Speed + Pitch (بدون NDK)
        if (preset.tempo != 1.0f) {
            s = SimpleStretch.process(s, preset.tempo, channels)
        }
        // توجه: pitch به تنهایی با این روش قابل تغییر نیست.
        // فعلاً فقط tempo اعمال می‌شه (که پیچ رو هم به همراه داره)

        // 5. Reverb
        if (preset.reverbWet > 0.01f && channels == 2) {
            s = ReverbProcessor(
                sr, preset.reverbWet,
                preset.reverbRoomSize, preset.reverbDamping
            ).process(s)
        }

        // 6. Lowpass
        if (preset.lowpassHz < 20000) {
            s = BiquadLowpass(preset.lowpassHz, sr).process(s)
        }

        // 7. Limiter
        s = LimiterProcessor(preset.limiterCeilingDb, sr).process(s)

        // 8. Normalize
        var peak = 0f
        for (v in s) { val a = abs(v); if (a > peak) peak = a }
        if (peak > 0.0001f) {
            val g = 0.95f / peak
            for (i in s.indices) s[i] *= g
        }

        return s
    }
}
