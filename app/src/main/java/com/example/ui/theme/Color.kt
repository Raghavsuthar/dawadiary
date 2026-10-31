package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// DawaDiary — Stitch visual system: calm, friendly and clinically clear.
// Core palette sampled from the approved Figma screens.
val MdPrimary = Color(0xFF147D70)
val MdPrimaryPressed = Color(0xFF0E665D)
val MdPrimaryContainer = Color(0xFFD1F0E0)
val MdOnPrimary = Color(0xFFFFFFFF)
val MdOnPrimaryContainer = Color(0xFF123B37)

val BrandGradientStart = Color(0xFF147D70)
val BrandGradientMid = Color(0xFF23998A)
val BrandGradientEnd = Color(0xFF57B9A4)
val BrandGlow = Color(0xFFB8E8D7)
val CardShadowTint = Color(0xFF315E56)

val MdPrimaryFixed = Color(0xFFB8E8D7)
val MdPrimaryFixedDim = Color(0xFF8DD5C0)
val MdOnPrimaryFixed = Color(0xFF123B37)
val MdOnPrimaryFixedVariant = Color(0xFF275E55)

val MdSecondary = Color(0xFF477F72)
val MdOnSecondary = Color(0xFFFFFFFF)
val MdSecondaryContainer = Color(0xFFD1F0E0)
val MdOnSecondaryContainer = Color(0xFF24584F)

val MdSecondaryFixed = Color(0xFFD1F0E0)
val MdSecondaryFixedDim = Color(0xFFB6E2D0)
val MdOnSecondaryFixed = Color(0xFF173D36)
val MdOnSecondaryFixedVariant = Color(0xFF315E56)

val MdTertiary = Color(0xFF6655A5)
val MdOnTertiary = Color(0xFFFFFFFF)
val MdTertiaryContainer = Color(0xFFE8E0FA)
val MdOnTertiaryContainer = Color(0xFF35265F)
val MdTertiaryFixed = Color(0xFFE8E0FA)
val MdTertiaryFixedDim = Color(0xFFD4C8F1)
val MdOnTertiaryFixed = Color(0xFF35265F)
val MdOnTertiaryFixedVariant = Color(0xFF55458D)

// Pale green page canvas and clean white cards from Figma.
val MdBackground = Color(0xFFF7FAF5)
val MdSurface = Color(0xFFF7FAF5)
val MdOnSurface = Color(0xFF1A2B33)
val MdOnSurfaceVariant = Color(0xFF61757A)
val MdSurfaceVariant = Color(0xFFE9EFEB)
val MdOutline = Color(0xFFDCE6E0)
val MdOutlineStrong = Color(0xFF82958F)

val MdSurfaceContainerLowest = Color(0xFFFFFFFF)
val MdSurfaceContainerLow = Color(0xFFF1F6F1)
val MdSurfaceContainer = Color(0xFFEAF1EB)
val MdSurfaceContainerHigh = Color(0xFFE3ECE5)
val MdSurfaceContainerHighest = Color(0xFFDCE6DF)
val MdSurfaceDim = Color(0xFFD4DED7)

// Accent aliases retained for compatibility with existing screens.
val MdBlue = Color(0xFF397D9B)
val MdBlueContainer = Color(0xFFE1F0F5)
val MdPurple = MdTertiary
val MdPurpleContainer = MdTertiaryFixed
val MdCoral = Color(0xFFFA7D61)
val MdCoralContainer = Color(0xFFFFE8DB)
val MdMint = Color(0xFF147D70)
val MdMintContainer = Color(0xFFD1F0E0)

// Semantic status colours.
val MdSuccess = Color(0xFF147D70)
val MdSuccessContainer = Color(0xFFD1F0E0)
val MdOnSuccess = Color(0xFFFFFFFF)

val MdWarning = Color(0xFF805A13)
val MdWarningContainer = Color(0xFFFFEBA6)
val MdOnWarning = Color(0xFF382600)

val MdError = Color(0xFFB84E3B)
val MdErrorContainer = Color(0xFFFFE8DB)
val MdOnErrorContainer = Color(0xFF512116)
val MdOnError = Color(0xFFFFFFFF)

val Level1AmbientAlpha = 0.035f
val Level2AmbientAlpha = 0.09f
val OutlineTintAlpha = 0.06f

val DoseTakenGreen = MdSuccess
val DoseSnoozeOrange = MdWarning
val DoseSkipGray = MdOutlineStrong
val DoseMissedRed = MdError

val HighContrastText = Color(0xFF000000)
val HighContrastSecondaryText = Color(0xFF1A2B33)
val HighContrastOutline = Color(0xFF61757A)
val HighContrastPrimary = Color(0xFF0E665D)

// Legacy aliases — preserve all existing call sites.
val MedicalPrimary = MdPrimary
val MedicalPrimaryContainer = MdPrimaryContainer
val MedicalOnPrimary = MdOnPrimary
val MedicalOnPrimaryContainer = MdOnPrimaryContainer
val MedicalSecondary = MdSecondary
val MedicalSecondaryContainer = MdSecondaryContainer
val MedicalOnSecondary = MdOnSecondary
val MedicalOnSecondaryContainer = MdOnSecondaryContainer
val MedicalTertiary = MdTertiary
val MedicalTertiaryContainer = MdTertiaryContainer
val MedicalOnTertiary = MdOnTertiary
val MedicalOnTertiaryContainer = MdOnTertiaryContainer
val MedicalBackground = MdBackground
val MedicalOnBackground = MdOnSurface
val MedicalSurface = MdSurface
val MedicalOnSurface = MdOnSurface
val MedicalSurfaceVariant = MdSurfaceVariant
val MedicalOnSurfaceVariant = MdOnSurfaceVariant
val MedicalOutline = MdOutline
val MedicalOutlineVariant = MdOutline
val MedicalSuccess = MdSuccess
val MedicalSuccessContainer = MdSuccessContainer
val MedicalOnSuccess = MdOnSuccess
val MedicalOnSuccessContainer = MdOnSecondaryContainer
val MedicalWarning = MdWarning
val MedicalWarningContainer = MdWarningContainer
val MedicalOnWarning = MdOnWarning
val MedicalOnWarningContainer = MdOnSecondaryContainer
val MedicalError = MdError
val MedicalErrorContainer = MdErrorContainer
val MedicalOnError = MdOnError
val MedicalOnErrorContainer = MdOnErrorContainer
val MedicalInfo = MdPrimary
val MedicalInfoContainer = MdTertiaryFixed
val MedicalOnInfo = MdOnPrimary
val MedicalOnInfoContainer = MdOnTertiaryFixedVariant
