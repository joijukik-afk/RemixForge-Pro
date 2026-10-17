package com.remixforge.pro.data.dsp

import com.remixforge.pro.data.model.RemixPreset
import kotlin.math.abs

class DspChain(private val preset: RemixPreset, private val sr: Int) {

    fun process(
        input: FloatArray,
        channels: Int,
        onStage: (String, Float) -> Unit = { _, _ -> }
    ): FloatArray {
        var s = input

        onStage("Highpass...", 0.05f)
        if (preset.highpassHz > 20) {
            s = BiquadHighpass(preset.highpassHz, sr).process(s)
        }

        onStage("EQ...", 0.20f)
        if (preset.eqBands.isNotEmpty()) {
            s = EQProcessor(sr, preset.eqBands).process(s)
        }

        onStage("Compressor...", 0.35f)
        s = CompressorProcessor(
            preset.compThresholdDb, preset.compRatio,
            preset.compAttackMs, preset.compReleaseMs, sr
        ).processStereo(s)

        // استفاده از SoundTouch برای تغییر سرعت و پیچ حرفه‌ای
        onStage("تغییر سرعت (SoundTouch)...", 0.50f)
        val manager = SoundTouchManager(sr, channels)
        s = manager.process(s, preset.tempo, preset.pitchSemitones)

        onStage("Reverb...", 0.70f)
        if (preset.reverbWet > 0.01f && channels == 2) {
            s = ReverbProcessor(
                sr, preset.reverbWet,
                preset.reverbRoomSize, preset.reverbDamping
            ).process(s)
        }

        onStage("Lowpass...", 0.85f)
        if (preset.lowpassHz < 20000) {
            s = BiquadLowpass(preset.lowpassHz, sr).process(s)
        }

        onStage("لیمیتر...", 0.92f)
        s = LimiterProcessor(preset.limiterCeilingDb, sr).process(s)

        onStage("نرمال‌سازی...", 0.97f)
        var peak = 0f
        for (v in s) { val a = abs(v); if (a > peak) peak = a }
        if (peak > 0.0001f) {
            val g = 0.95f / peak
            for (i in s.indices) s[i] *= g
        }

        onStage("تمام", 1.0f)
        return s
    }
}
