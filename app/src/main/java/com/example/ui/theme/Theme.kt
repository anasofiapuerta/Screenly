package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Screenly uses a unified, modern white background and royal blue theme
private val ScreenlyLightColorScheme = lightColorScheme(
    primary = ScreenlyBlue,
    onPrimary = Color.White,
    primaryContainer = ScreenlyBlueContainer,
    onPrimaryContainer = ScreenlyOnBlueContainer,
    secondary = ScreenlyCyan,
    onSecondary = Color.White,
    secondaryContainer = ScreenlyCyanContainer,
    onSecondaryContainer = ScreenlyOnCyanContainer,
    tertiary = ScreenlySky,
    onTertiary = Color(0xFF003258),
    background = Color.White,
    onBackground = ScreenlyTextPrimaryLight,
    surface = Color.White,
    onSurface = ScreenlyTextPrimaryLight,
    surfaceVariant = ScreenlySurfaceVariantLight,
    onSurfaceVariant = ScreenlyTextSecondaryLight,
    outline = ScreenlyBorderLight,
    error = ScreenlyDanger,
    onError = Color.White
)

// Dark theme is intentionally adapted to match the mandated clean white interface
private val ScreenlyDarkColorScheme = lightColorScheme(
    primary = ScreenlyBlue,
    onPrimary = Color.White,
    primaryContainer = ScreenlyBlueContainer,
    onPrimaryContainer = ScreenlyOnBlueContainer,
    secondary = ScreenlyCyan,
    onSecondary = Color.White,
    secondaryContainer = ScreenlyCyanContainer,
    onSecondaryContainer = ScreenlyOnCyanContainer,
    tertiary = ScreenlySky,
    onTertiary = Color(0xFF003258),
    background = Color.White,
    onBackground = ScreenlyTextPrimaryLight,
    surface = Color.White,
    onSurface = ScreenlyTextPrimaryLight,
    surfaceVariant = ScreenlySurfaceVariantLight,
    onSurfaceVariant = ScreenlyTextSecondaryLight,
    outline = ScreenlyBorderLight,
    error = ScreenlyDanger,
    onError = Color.White
)

@Composable
fun ScreenlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve brand visual identity
    content: @Composable () -> Unit
) {
    // Both configurations provide a consistent white and blue user experience
    val colorScheme = if (darkTheme) ScreenlyDarkColorScheme else ScreenlyLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = ScreenlyTheme(darkTheme, dynamicColor, content)
