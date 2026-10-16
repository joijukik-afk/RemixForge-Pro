package com.remixforge.pro.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9C27B0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A148C),
    onPrimaryContainer = Color(0xFFE1BEE7),
    secondary = Color(0xFF00BCD4),
    surface = Color(0xFF121212),
    background = Color(0xFF0A0A0A)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF7B1FA2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3E5F5),
    onPrimaryContainer = Color(0xFF4A148C),
    secondary = Color(0xFF00838F),
    surface = Color(0xFFFAFAFA),
    background = Color(0xFFFFFFFF)
)

@Composable
fun RemixForgeProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
