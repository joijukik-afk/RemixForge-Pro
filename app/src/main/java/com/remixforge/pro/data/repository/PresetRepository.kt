package com.remixforge.pro.data.repository
import com.remixforge.pro.data.model.RemixPreset

object PresetRepository {
    val presets = listOf(
        RemixPreset("slowed", "Slowed + Reverb", "کند و فضایی", 0.82f, 0f, 0.4f, 0.7f, 0.5f, emptyMap(), -14f, 3f, 15f, 250f, -0.5f, 11000, 30),
        RemixPreset("nightcore", "Nightcore", "سریع و پرانرژی", 1.25f, 0f, 0.1f, 0.3f, 0.7f, emptyMap(), -10f, 4f, 5f, 100f, -0.3f, 19000, 50),
        RemixPreset("lofi", "Lo-fi", "گرم و کهنه", 1.0f, 0f, 0.2f, 0.4f, 0.8f, emptyMap(), -12f, 2.5f, 20f, 300f, -1f, 6500, 80)
    )
    fun getById(id: String) = presets.find { it.id == id }
}
