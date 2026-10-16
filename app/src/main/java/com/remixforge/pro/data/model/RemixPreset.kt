package com.remixforge.pro.data.model

data class RemixPreset(
    val id: String,
    val name: String,
    val description: String,
    val category: PresetCategory,
    // Time & Pitch
    val tempo: Float,          // 1.0 = نرمال
    val pitchSemitones: Float, // 0 = نرمال
    // Reverb
    val reverbWet: Float,      // 0..1
    val reverbRoomSize: Float, // 0..1
    val reverbDamping: Float,  // 0..1
    // EQ
    val eqBands: Map<Int, Float>, // Hz -> dB
    // Dynamics
    val compThresholdDb: Float,
    val compRatio: Float,
    val compAttackMs: Float,
    val compReleaseMs: Float,
    val limiterCeilingDb: Float,
    // Filter
    val lowpassHz: Int,
    val highpassHz: Int,
    // Stereo
    val stereoWidth: Float     // 1.0 = نرمال، 2.0 = عریض
)

enum class PresetCategory {
    SLOWED, NIGHTCORE, CHOPPED, LOFI, BASS_BOOST, EIGHT_D, CUSTOM
}
