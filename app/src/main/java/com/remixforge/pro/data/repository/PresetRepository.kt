package com.remixforge.pro.data.repository

import com.remixforge.pro.data.model.PresetCategory
import com.remixforge.pro.data.model.RemixPreset

object PresetRepository {

    val presets: List<RemixPreset> = listOf(

        RemixPreset(
            id = "slowed_reverb",
            name = "Slowed + Reverb",
            description = "کند، توهم‌آمیز و پر از فضا",
            category = PresetCategory.SLOWED,
            tempo = 0.82f,
            pitchSemitones = -2.5f,
            reverbWet = 0.40f,
            reverbRoomSize = 0.75f,
            reverbDamping = 0.5f,
            eqBands = mapOf(
                60 to 3.5f, 120 to 2.5f, 250 to 1f, 500 to 0f,
                1000 to -0.5f, 2000 to -1f, 4000 to -1.5f,
                8000 to -3f, 12000 to -5f, 16000 to -7f
            ),
            compThresholdDb = -14f,
            compRatio = 3f,
            compAttackMs = 15f,
            compReleaseMs = 250f,
            limiterCeilingDb = -0.5f,
            lowpassHz = 11000,
            highpassHz = 30,
            stereoWidth = 1.3f
        ),

        RemixPreset(
            id = "nightcore",
            name = "Nightcore",
            description = "سریع، پرانرژی، شفاف",
            category = PresetCategory.NIGHTCORE,
            tempo = 1.25f,
            pitchSemitones = 3f,
            reverbWet = 0.08f,
            reverbRoomSize = 0.3f,
            reverbDamping = 0.7f,
            eqBands = mapOf(
                60 to -2f, 120 to -1f, 250 to 0f, 500 to 0f,
                1000 to 0.5f, 2000 to 1f, 4000 to 1.5f,
                8000 to 1f, 12000 to 0.5f, 16000 to 0f
            ),
            compThresholdDb = -10f,
            compRatio = 4f,
            compAttackMs = 5f,
            compReleaseMs = 100f,
            limiterCeilingDb = -0.3f,
            lowpassHz = 19000,
            highpassHz = 50,
            stereoWidth = 1.0f
        ),

        RemixPreset(
            id = "chopped_screwed",
            name = "Chopped & Screwed",
            description = "خیلی کند، بیس سنگین، حالت هیپ‌هاپ جنوبی",
            category = PresetCategory.CHOPPED,
            tempo = 0.70f,
            pitchSemitones = -4f,
            reverbWet = 0.35f,
            reverbRoomSize = 0.6f,
            reverbDamping = 0.6f,
            eqBands = mapOf(
                60 to 6f, 120 to 4f, 250 to 2f, 500 to 0f,
                1000 to -1f, 2000 to -2f, 4000 to -3f,
                8000 to -4f, 12000 to -6f, 16000 to -8f
            ),
            compThresholdDb = -16f,
            compRatio = 3.5f,
            compAttackMs = 25f,
            compReleaseMs = 350f,
            limiterCeilingDb = -0.5f,
            lowpassHz = 9000,
            highpassHz = 25,
            stereoWidth = 1.1f
        ),

        RemixPreset(
            id = "lofi",
            name = "Lo-fi",
            description = "گرم، کهنه، آنالوگ",
            category = PresetCategory.LOFI,
            tempo = 1.0f,
            pitchSemitones = -0.5f,
            reverbWet = 0.20f,
            reverbRoomSize = 0.4f,
            reverbDamping = 0.8f,
            eqBands = mapOf(
                60 to 2f, 120 to 1.5f, 250 to 1f, 500 to 0f,
                1000 to -1f, 2000 to -2f, 4000 to -4f,
                8000 to -7f, 12000 to -10f, 16000 to -14f
            ),
            compThresholdDb = -12f,
            compRatio = 2.5f,
            compAttackMs = 20f,
            compReleaseMs = 300f,
            limiterCeilingDb = -1f,
            lowpassHz = 6500,
            highpassHz = 80,
            stereoWidth = 0.9f
        ),

        RemixPreset(
            id = "super_slowed",
            name = "Super Slowed",
            description = "خیلی کند و عمیق، برای رویا",
            category = PresetCategory.SLOWED,
            tempo = 0.72f,
            pitchSemitones = -3.5f,
            reverbWet = 0.50f,
            reverbRoomSize = 0.9f,
            reverbDamping = 0.4f,
            eqBands = mapOf(
                60 to 4f, 120 to 3f, 250 to 1.5f, 500 to 0f,
                1000 to -1f, 2000 to -2f, 4000 to -3f,
                8000 to -5f, 12000 to -8f, 16000 to -12f
            ),
            compThresholdDb = -15f,
            compRatio = 3f,
            compAttackMs = 30f,
            compReleaseMs = 400f,
            limiterCeilingDb = -0.5f,
            lowpassHz = 10000,
            highpassHz = 30,
            stereoWidth = 1.4f
        ),

        RemixPreset(
            id = "bass_boosted",
            name = "Bass Boosted",
            description = "بیس قوی و پرحجم",
            category = PresetCategory.BASS_BOOST,
            tempo = 1.0f,
            pitchSemitones = 0f,
            reverbWet = 0.05f,
            reverbRoomSize = 0.2f,
            reverbDamping = 0.5f,
            eqBands = mapOf(
                40 to 9f, 60 to 7f, 120 to 5f, 250 to 2f,
                500 to 0f, 1000 to -0.5f, 2000 to -1f,
                4000 to -1f, 8000 to -1.5f, 16000 to -2f
            ),
            compThresholdDb = -12f,
            compRatio = 4f,
            compAttackMs = 10f,
            compReleaseMs = 200f,
            limiterCeilingDb = -0.3f,
            lowpassHz = 20000,
            highpassHz = 20,
            stereoWidth = 1.0f
        ),

        RemixPreset(
            id = "eight_d",
            name = "8D Audio",
            description = "صدای چرخان دور سر",
            category = PresetCategory.EIGHT_D,
            tempo = 1.0f,
            pitchSemitones = 0f,
            reverbWet = 0.35f,
            reverbRoomSize = 0.7f,
            reverbDamping = 0.5f,
            eqBands = mapOf(
                60 to 1f, 120 to 0.5f, 250 to 0f, 500 to 0f,
                1000 to 0f, 2000 to 0f, 4000 to 0.5f,
                8000 to 0.5f, 12000 to 0f, 16000 to -1f
            ),
            compThresholdDb = -14f,
            compRatio = 2.5f,
            compAttackMs = 15f,
            compReleaseMs = 250f,
            limiterCeilingDb = -0.5f,
            lowpassHz = 18000,
            highpassHz = 30,
            stereoWidth = 2.0f
        )
    )

    fun getById(id: String): RemixPreset? = presets.find { it.id == id }
}
