package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SSKNDarkColorScheme = darkColorScheme(
    primary = BrightBlue,
    onPrimary = Color.White,
    primaryContainer = SecondaryNavy,
    onPrimaryContainer = SoftBlue,
    secondary = SoftBlue,
    onSecondary = MidnightNavy,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurfaceCard,
    onSurface = DarkTextPrimary,
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    error = ErrorRed,
    onError = Color.White
)

private val SSKNLightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = VeryLightBlue,
    onPrimaryContainer = PrimaryBlue,
    secondary = BrightBlue,
    onSecondary = Color.White,
    background = AppBackground,
    onBackground = PrimaryText,
    surface = SurfaceCard,
    onSurface = PrimaryText,
    surfaceVariant = VeryLightBlue,
    onSurfaceVariant = SecondaryText,
    outline = BorderSubtle,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep SSKN branded colors consistent
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SSKNDarkColorScheme else SSKNLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
