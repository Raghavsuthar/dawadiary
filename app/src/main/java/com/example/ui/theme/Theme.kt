package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
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
    error = MdError,
    onError = MdOnError,
    errorContainer = MdErrorContainer
)

private val HighContrastColorScheme = lightColorScheme(
    primary = HighContrastPrimary,
    onPrimary = MdOnPrimary,
    primaryContainer = MdPrimaryContainer,
    onPrimaryContainer = HighContrastText,
    secondary = HighContrastPrimary,
    onSecondary = MdOnPrimary,
    secondaryContainer = MdPrimaryContainer,
    onSecondaryContainer = HighContrastText,
    tertiary = HighContrastPrimary,
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
    error = MdError,
    onError = MdOnError,
    errorContainer = MdErrorContainer
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
        content = content
    )
}
