package com.remixforge.pro.data.repository

import com.remixforge.pro.data.model.RemixPreset

object PresetRepository {
    val presets = listOf(
        RemixPreset(
            "slowed_reverb", "Slowed + Reverb", "کند و فضایی",
            0.82f, -2.5f, 0.40f, 0.75f, 0.5f,
            mapOf(60 to 3.5f, 120 to 2.5f, 250 to 1f, 500 to 0f, 1000 to -0.5f,
                2000 to -1f, 4000 to -1.5f, 8000 to -3f, 12000 to -5f, 16000 to -7f),
            -14f, 3f, 15f, 250f, -0.5f, 11000, 30
        ),
        RemixPreset(
            "nightcore", "Nightcore", "سریع و پرانرژی",
            1.25f, 3f, 0.08f, 0.3f, 0.7f,
            mapOf(60 to -2f, 120 to -1f, 250 to 0f, 500 to 0f, 1000 to 0.5f,
                2000 to 1f, 4000 to 1.5f, 8000 to 1f, 12000 to 0.5f, 16000 to 0f),
            -10f, 4f, 5f, 100f, -0.3f, 19000, 50
        ),
        RemixPreset(
            "chopped", "Chopped & Screwed", "خیلی کند، بیس سنگین",
            0.70f, -4f, 0.35f, 0.6f, 0.6f,
            mapOf(60 to 6f, 120 to 4f, 250 to 2f, 500 to 0f, 1000 to -1f,
                2000 to -2f, 4000 to -3f, 8000 to -4f, 12000 to -6f, 16000 to -8f),
            -16f, 3.5f, 25f, 350f, -0.5f, 9000, 25
        ),
        RemixPreset(
            "lofi", "Lo-fi", "گرم و کهنه",
            1.0f, -0.5f, 0.20f, 0.4f, 0.8f,
            mapOf(60 to 2f, 120 to 1.5f, 250 to 1f, 500 to 0f, 1000 to -1f,
                2000 to -2f, 4000 to -4f, 8000 to -7f, 12000 to -10f, 16000 to -14f),
            -12f, 2.5f, 20f, 300f, -1f, 6500, 80
        ),
        RemixPreset(
            "super_slowed", "Super Slowed", "خیلی کند و عمیق",
            0.72f, -3.5f, 0.50f, 0.9f, 0.4f,
            mapOf(60 to 4f, 120 to 3f, 250 to 1.5f, 500 to 0f, 1000 to -1f,
                2000 to -2f, 4000 to -3f, 8000 to -5f, 12000 to -8f, 16000 to -12f),
            -15f, 3f, 30f, 400f, -0.5f, 10000, 30
        ),
        RemixPreset(
            "bass_boosted", "Bass Boosted", "بیس قوی",
            1.0f, 0f, 0.05f, 0.2f, 0.5f,
            mapOf(40 to 9f, 60 to 7f, 120 to 5f, 250 to 2f, 500 to 0f,
                1000 to -0.5f, 2000 to -1f, 4000 to -1f, 8000 to -1.5f, 16000 to -2f),
            -12f, 4f, 10f, 200f, -0.3f, 20000, 20
        )
    )

    fun getById(id: String): RemixPreset? = presets.find { it.id == id }
}
