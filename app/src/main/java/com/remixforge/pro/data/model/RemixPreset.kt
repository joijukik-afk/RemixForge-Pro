package com.remixforge.pro.data.model

data class RemixPreset(
    val id: String, val name: String, val description: String,
    val tempo: Float, val pitchSemitones: Float,
    val reverbWet: Float, val reverbRoomSize: Float, val reverbDamping: Float,
    val eqBands: Map<Int, Float>, val compThresholdDb: Float, val compRatio: Float,
    val compAttackMs: Float, val compReleaseMs: Float, val limiterCeilingDb: Float,
    val lowpassHz: Int, val highpassHz: Int
)
