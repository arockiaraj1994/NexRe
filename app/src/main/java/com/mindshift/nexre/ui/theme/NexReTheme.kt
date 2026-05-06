package com.mindshift.nexre.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = NexreAccent,
    onPrimary = Color.White,
    primaryContainer = NexreAccentSoft,
    onPrimaryContainer = NexreAccentInk,
    secondary = NexreAccent,
    background = NexreBg,
    surface = NexreSurface,
    onBackground = NexreFg1,
    onSurface = NexreFg1,
    onSurfaceVariant = NexreFg2,
    outline = NexreOutline,
    error = NexreNeg,
)

@Composable
fun NexReTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = NexReTypography,
        content = content,
    )
}
