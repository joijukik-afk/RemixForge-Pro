package com.remixforge.pro.data.dsp

import com.remixforge.pro.data.model.RemixPreset

/**
 * زنجیره کامل DSP — ترتیب پردازش حرفه‌ای:
 * 1. Highpass
 * 2. EQ
 * 3. Compressor
 * 4. Time-Stretch / Pitch-Shift (SoundTouch)
 * 5. Reverb
 * 6. Lowpass
 * 7. Limiter
 * 8. Normalize
 */
class DspChain(private val preset: RemixPreset, private val sampleRate: Int) {

    fun process(input: FloatArray, channels: Int): FloatArray {
        var samples = input

        // 1. Highpass
        if (preset.highpassHz > 20) {
            samples = BiquadHighpass(preset.highpassHz, sampleRate).process(samples)
        }

        // 2. EQ
        if (preset.eqBands.isNotEmpty()) {
            samples = EQProcessor(sampleRate, preset.eqBands).process(samples)
        }

        // 3. Compressor
        val comp = CompressorProcessor(
            preset.compThresholdDb,
            preset.compRatio,
            preset.compAttackMs,
            preset.compReleaseMs,
            sampleRate
        )
        samples = if (channels == 2) comp.processStereo(samples) else comp.process(samples)

        // 4. Time-Stretch / Pitch-Shift
        if (preset.tempo != 1.0f || preset.pitchSemitones != 0f) {
            val ts = TimeStretchProcessor(sampleRate, channels)
            ts.init()
            ts.setTempo(preset.tempo)
            ts.setPitch(preset.pitchSemitones)

            val chunkSize = 44100 * channels // 1 ثانیه
            val output = ArrayList<Float>()
            var offset = 0

            while (offset < samples.size) {
                val end = (offset + chunkSize).coerceAtMost(samples.size)
                val chunk = samples.copyOfRange(offset, end)
                output.addAll(ts.process(chunk).toList())
                offset = end
            }
            output.addAll(ts.flush().toList())
            ts.release()

            samples = output.toFloatArray()
        }

        // 5. Reverb
        if (preset.reverbWet > 0.01f) {
            val reverb = ReverbProcessor(
                sampleRate,
                preset.reverbWet,
                preset.reverbRoomSize,
                preset.reverbDamping
            )
            samples = reverb.process(samples)
        }

        // 6. Lowpass
        if (preset.lowpassHz < 20000) {
            samples = BiquadLowpass(preset.lowpassHz, sampleRate).process(samples)
        }

        // 7. Limiter
        samples = LimiterProcessor(preset.limiterCeilingDb, sampleRate).process(samples)

        // 8. Normalize
        normalize(samples, 0.95f)

        return samples
    }

    private fun normalize(samples: FloatArray, peakTarget: Float) {
        var peak = 0f
        for (s in samples) {
            val a = kotlin.math.abs(s)
            if (a > peak) peak = a
        }
        if (peak <= 0.0001f) return
        val gain = peakTarget / peak
        for (i in samples.indices) samples[i] *= gain
    }
}
