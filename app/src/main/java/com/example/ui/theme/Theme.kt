package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = PrimaryTeal,
    secondary = SurfaceGrey,
    tertiary = SurfaceGreyLight,
    background = DarkBackground,
    surface = SurfaceGrey,
    onPrimary = DarkBackground,
    onSecondary = TextWhite,
    onBackground = TextWhite,
    onSurface = TextWhite,
    error = ErrorRed,
    onError = TextWhite
  )

private val LightColorScheme =
  darkColorScheme( // We want a dark theme for Nova VPN throughout, so we will use the dark color scheme as base
    primary = PrimaryTeal,
    secondary = SurfaceGrey,
    tertiary = SurfaceGreyLight,
    background = DarkBackground,
    surface = SurfaceGrey,
    onPrimary = DarkBackground,
    onSecondary = TextWhite,
    onBackground = TextWhite,
    onSurface = TextWhite,
    error = ErrorRed,
    onError = TextWhite
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for Nova VPN
  dynamicColor: Boolean = false, // Disable dynamic colors to maintain premium dark style
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme


  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
