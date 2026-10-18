package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// ┌────────────────────────────────────────────────────────────────────┐
// │  DawaDiary Minimalist Theme                                        │
// │  Light mode only (health apps read best in light). High contrast   │
// │  and dynamic color are optional accessibility layers.              │
// └────────────────────────────────────────────────────────────────────┘

// Authoritative light color scheme (minimalist)
val LightColorScheme = lightColorScheme(
    primary = MdPrimary,
    onPrimary = MdOnPrimary,
    primaryContainer = MdPrimaryContainer,
    onPrimaryContainer = MdOnPrimaryContainer,
    secondary = MdBlue,
    onSecondary = MdOnPrimary,
    secondaryContainer = MdBlueContainer,
    onSecondaryContainer = MdOnSurface,
    tertiary = MdPurple,
    onTertiary = MdOnPrimary,
    tertiaryContainer = MdPurpleContainer,
    onTertiaryContainer = MdOnSurface,
    background = MdBackground,
    onBackground = MdOnSurface,
    surface = MdSurface,
    onSurface = MdOnSurface,
    surfaceVariant = MdSurfaceVariant,
    onSurfaceVariant = MdOnSurfaceVariant,
    outline = MdOutline,
    outlineVariant = MdOutline,
    error = MdError,
    onError = MdOnError,
    errorContainer = MdErrorContainer,
    onErrorContainer = MdOnPrimaryContainer,
    inverseSurface = MdOnSurface,
    inverseOnSurface = MdSurface,
    inversePrimary = MdPrimaryContainer
)

// High-contrast light scheme for enhanced legibility
val HighContrastLightColorScheme = lightColorScheme(
    primary = HighContrastPrimary,
    onPrimary = MdOnPrimary,
    primaryContainer = MdPrimaryContainer,
    onPrimaryContainer = HighContrastText,
    secondary = MdPrimary,
    onSecondary = MdOnPrimary,
    secondaryContainer = MdPrimaryContainer,
    onSecondaryContainer = HighContrastText,
    tertiary = MdPrimary,
    onTertiary = MdOnPrimary,
    tertiaryContainer = MdPrimaryContainer,
    onTertiaryContainer = HighContrastText,
    background = MdBackground,
    onBackground = HighContrastText,
    surface = MdSurface,
    onSurface = HighContrastText,
    surfaceVariant = MdSurfaceVariant,
    onSurfaceVariant = HighContrastSecondaryText,
    outline = HighContrastOutline,
    outlineVariant = HighContrastOutline,
    error = MdError,
    onError = MdOnError,
    errorContainer = MdErrorContainer,
    onErrorContainer = MdOnPrimaryContainer,
    inverseSurface = HighContrastText,
    inverseOnSurface = MdSurface,
    inversePrimary = MdPrimaryContainer
)

@Composable
fun DawaDiaryTheme(
    darkTheme: Boolean = false,
    highContrast: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        highContrast -> HighContrastLightColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            dynamicLightColorScheme(context)
        }
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
