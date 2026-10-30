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

// Authoritative light color scheme ("Warm Clinical Material")
val LightColorScheme = lightColorScheme(
    primary = MdPrimary,
    onPrimary = MdOnPrimary,
    primaryContainer = MdPrimaryContainer,
    onPrimaryContainer = MdOnPrimaryContainer,
    secondary = MdSecondary,
    onSecondary = MdOnSecondary,
    secondaryContainer = MdSecondaryContainer,
    onSecondaryContainer = MdOnSecondaryContainer,
    tertiary = MdTertiary,
    onTertiary = MdOnTertiary,
    tertiaryContainer = MdTertiaryContainer,
    onTertiaryContainer = MdOnTertiaryContainer,
    background = MdBackground,
    onBackground = MdOnSurface,
    surface = MdSurface,
    onSurface = MdOnSurface,
    surfaceVariant = MdSurfaceVariant,
    onSurfaceVariant = MdOnSurfaceVariant,
    surfaceContainerLowest = MdSurfaceContainerLowest,
    surfaceContainerLow = MdSurfaceContainerLow,
    surfaceContainer = MdSurfaceContainer,
    surfaceContainerHigh = MdSurfaceContainerHigh,
    surfaceContainerHighest = MdSurfaceContainerHighest,
    outline = MdOutline,
    outlineVariant = MdOutline,
    error = MdError,
    onError = MdOnError,
    errorContainer = MdErrorContainer,
    onErrorContainer = MdOnPrimaryContainer,
    inverseSurface = MdOnSurface,
    inverseOnSurface = MdSurface,
    inversePrimary = MdPrimaryFixedDim
)

// High-contrast light scheme for enhanced legibility
val HighContrastLightColorScheme = lightColorScheme(
    primary = HighContrastPrimary,
    onPrimary = MdOnPrimary,
    primaryContainer = MdPrimaryPressed,
    onPrimaryContainer = MdPrimaryFixed,
    secondary = HighContrastPrimary,
    onSecondary = MdOnPrimary,
    secondaryContainer = MdSecondaryContainer,
    onSecondaryContainer = HighContrastText,
    tertiary = MdTertiary,
    onTertiary = MdOnTertiary,
    tertiaryContainer = MdTertiaryContainer,
    onTertiaryContainer = HighContrastText,
    background = MdBackground,
    onBackground = HighContrastText,
    surface = MdSurface,
    onSurface = HighContrastText,
    surfaceVariant = MdSurfaceVariant,
    onSurfaceVariant = HighContrastSecondaryText,
    surfaceContainerLowest = MdSurfaceContainerLowest,
    surfaceContainerLow = MdSurfaceContainerLow,
    surfaceContainer = MdSurfaceContainer,
    surfaceContainerHigh = MdSurfaceContainerHigh,
    surfaceContainerHighest = MdSurfaceContainerHighest,
    outline = HighContrastOutline,
    outlineVariant = HighContrastOutline,
    error = MdError,
    onError = MdOnError,
    errorContainer = MdErrorContainer,
    onErrorContainer = MdOnPrimaryContainer,
    inverseSurface = HighContrastText,
    inverseOnSurface = MdSurface,
    inversePrimary = MdPrimaryFixedDim
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
