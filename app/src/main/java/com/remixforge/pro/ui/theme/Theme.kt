package com.remixforge.pro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Dark = darkColorScheme(
    primary = Color(0xFF9C27B0), onPrimary = Color.White,
    primaryContainer = Color(0xFF4A148C), onPrimaryContainer = Color(0xFFE1BEE7),
    secondary = Color(0xFF00BCD4),
    background = Color(0xFF0A0A0A), surface = Color(0xFF121212)
)

private val Light = lightColorScheme(
    primary = Color(0xFF7B1FA2), onPrimary = Color.White,
    primaryContainer = Color(0xFFF3E5F5), onPrimaryContainer = Color(0xFF4A148C),
    secondary = Color(0xFF00838F),
    background = Color.White, surface = Color(0xFFFAFAFA)
)

@Composable
fun RemixForgeProTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) Dark else Light, content = content)
}
