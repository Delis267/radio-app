package com.iu.radio_app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Nur ein helles Schema, bis es ein Dark-Mode-Design gibt
private val LightColorScheme =
    lightColorScheme(
        primary = FunkOrange,
        onPrimary = Color.White,
        inversePrimary = FunkOrangeLight,
        background = FunkSand,
        onBackground = FunkInk,
        surface = FunkSand,
        onSurface = FunkInk,
        onSurfaceVariant = FunkInk,
        inverseSurface = FunkInk,
        inverseOnSurface = FunkSand,
        surfaceContainer = FunkSandDark,
        surfaceContainerLowest = Color.White,
        outlineVariant = FunkLine,
    )

@Composable
fun RadioappTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content,
    )
}
