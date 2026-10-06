package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = CyanPrimaryDark,
    onPrimary = Color(0xFF00363A),
    primaryContainer = Color(0xFF004D54),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = SlateSecondaryDark,
    onSecondary = Color(0xFF003735),
    tertiary = AmberAccentDark,
    onTertiary = Color(0xFF452B00),
    background = DarkNavyBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = DarkNavySurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = DarkNavySurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = DangerRed,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CyanPrimary,
    onPrimary = Color.White,
    primaryContainer = CyanLight,
    onPrimaryContainer = Color(0xFF001F24),
    secondary = SlateSecondary,
    onSecondary = Color.White,
    tertiary = AmberAccent,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    error = DangerRed,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
