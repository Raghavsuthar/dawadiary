package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// ┌────────────────────────────────────────────────────────────────────┐
// │  DawaDiary Theme — "Warm Clinical Material"                        │
// │  Light-first (health apps read best in light). High contrast and   │
// │  dynamic colour stay available as accessibility layers.            │
// └────────────────────────────────────────────────────────────────────┘

private val LightColorScheme = lightColorScheme(
    primary = MdPrimary,
    onPrimary = MdOnPrimary,
    primaryContainer = MdPrimaryContainer,
    onPrimaryContainer = MdOnPrimaryContainer,
    inversePrimary = MdPrimaryFixedDim,
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
    surfaceDim = MdSurfaceDim,
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
    onErrorContainer = MdOnErrorContainer,
    inverseSurface = MdOnSurface,
    inverseOnSurface = MdSurface
)

private val HighContrastColorScheme = lightColorScheme(
    primary = HighContrastPrimary,
    onPrimary = MdOnPrimary,
    primaryContainer = MdPrimaryFixed,
    onPrimaryContainer = HighContrastText,
    secondary = MdSecondary,
    onSecondary = MdOnSecondary,
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
    surfaceDim = MdSurfaceDim,
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
    onErrorContainer = HighContrastText,
    inverseSurface = HighContrastText,
    inverseOnSurface = MdSurface
)

private val DawaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun DawaDiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    highContrast: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        highContrast -> HighContrastColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = DawaShapes,
        content = content
    )
}
