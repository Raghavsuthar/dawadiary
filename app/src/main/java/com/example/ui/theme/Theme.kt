package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Authoritative Light Material 3 Color Scheme
val LightColorScheme = lightColorScheme(
    primary = MedicalPrimary,
    onPrimary = MedicalOnPrimary,
    primaryContainer = MedicalPrimaryContainer,
    onPrimaryContainer = MedicalOnPrimaryContainer,
    secondary = MedicalSecondary,
    onSecondary = MedicalOnSecondary,
    secondaryContainer = MedicalSecondaryContainer,
    onSecondaryContainer = MedicalOnSecondaryContainer,
    tertiary = MedicalTertiary,
    onTertiary = MedicalOnTertiary,
    tertiaryContainer = MedicalTertiaryContainer,
    onTertiaryContainer = MedicalOnTertiaryContainer,
    background = MedicalBackground,
    onBackground = MedicalOnBackground,
    surface = MedicalSurface,
    onSurface = MedicalOnSurface,
    surfaceVariant = MedicalSurfaceVariant,
    onSurfaceVariant = MedicalOnSurfaceVariant,
    outline = MedicalOutline,
    outlineVariant = MedicalOutlineVariant,
    error = MedicalError,
    onError = MedicalOnError,
    errorContainer = MedicalErrorContainer,
    onErrorContainer = MedicalOnErrorContainer,
    inverseSurface = MedicalOnSurface,
    inverseOnSurface = MedicalSurface,
    inversePrimary = MedicalPrimaryContainer
)

// High-Contrast Light Scheme for enhanced legibility
val HighContrastLightColorScheme = lightColorScheme(
    primary = HighContrastPrimary,
    onPrimary = MedicalOnPrimary,
    primaryContainer = MedicalPrimaryContainer,
    onPrimaryContainer = HighContrastText,
    secondary = MedicalSecondary,
    onSecondary = MedicalOnSecondary,
    secondaryContainer = MedicalSecondaryContainer,
    onSecondaryContainer = HighContrastText,
    tertiary = MedicalTertiary,
    onTertiary = MedicalOnTertiary,
    tertiaryContainer = MedicalTertiaryContainer,
    onTertiaryContainer = HighContrastText,
    background = MedicalBackground,
    onBackground = HighContrastText,
    surface = MedicalSurface,
    onSurface = HighContrastText,
    surfaceVariant = MedicalSurfaceVariant,
    onSurfaceVariant = HighContrastSecondaryText,
    outline = HighContrastOutline,
    outlineVariant = HighContrastOutline,
    error = MedicalError,
    onError = MedicalOnError,
    errorContainer = MedicalErrorContainer,
    onErrorContainer = MedicalOnErrorContainer,
    inverseSurface = HighContrastText,
    inverseOnSurface = MedicalSurface,
    inversePrimary = MedicalPrimaryContainer
)

@Composable
fun DawaDiaryTheme(
    darkTheme: Boolean = false, // Crucial: default is strictly false (Light Mode) regardless of system setting
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
