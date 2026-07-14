package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkMode
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00D084),
    secondary = Color(0xFFFFB84D),
    tertiary = Color(0xFF0F3460),
    background = Color(0xFF1a1a2e),
    surface = Color(0xFF16213e),
    error = Color(0xFFFF6B6B)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkMode(),
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}