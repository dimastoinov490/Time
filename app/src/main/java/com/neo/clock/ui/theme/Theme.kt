package com.neo.clock.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NeuBlue,
    onPrimary = NeuText,
    primaryContainer = NeuBlueDark,
    onPrimaryContainer = NeuBlue2,
    secondary = NeuBlue2,
    background = NeuBg,
    onBackground = NeuText,
    surface = NeuBg2,
    onSurface = NeuText,
    surfaceVariant = NeuSurfaceSunk,
    onSurfaceVariant = NeuMuted,
    outline = NeuLightShadow
)

@Composable
fun NeoClockTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
