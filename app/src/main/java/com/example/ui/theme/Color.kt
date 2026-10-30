package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ┌────────────────────────────────────────────────────────────────────┐
// │  DawaDiary "Warm Clinical Material" Palette (Stitch design)        │
// │  Deep teal primary, mint/lavender/amber/coral accents.             │
// │  Pristine white cards over a warm off-white canvas.                │
// └────────────────────────────────────────────────────────────────────┘

// ── Brand (deep clinical teal) ───────────────────────────────────────
val MdPrimary = Color(0xFF005050)          // primary interactive actions
val MdPrimaryPressed = Color(0xFF004D40)   // active pressed state
val MdPrimaryContainer = Color(0xFF006A6A) // high-emphasis teal container
val MdOnPrimary = Color(0xFFFFFFFF)
val MdOnPrimaryContainer = Color(0xFF97E7E6)

val MdPrimaryFixed = Color(0xFFA0F0F0)
val MdPrimaryFixedDim = Color(0xFF84D4D3)
val MdOnPrimaryFixed = Color(0xFF002020)
val MdOnPrimaryFixedVariant = Color(0xFF004F4F)

// ── Secondary (soft mint) ────────────────────────────────────────────
val MdSecondary = Color(0xFF006A62)
val MdOnSecondary = Color(0xFFFFFFFF)
val MdSecondaryContainer = Color(0xFF81F3E5)
val MdOnSecondaryContainer = Color(0xFF006F66)

val MdSecondaryFixed = Color(0xFF84F5E8)
val MdSecondaryFixedDim = Color(0xFF66D9CC)
val MdOnSecondaryFixed = Color(0xFF00201D)
val MdOnSecondaryFixedVariant = Color(0xFF005049)

// ── Tertiary (lavender — external care / appointments) ───────────────
val MdTertiary = Color(0xFF443C8B)
val MdOnTertiary = Color(0xFFFFFFFF)
val MdTertiaryContainer = Color(0xFF5C54A5)
val MdOnTertiaryContainer = Color(0xFFDAD4FF)
val MdTertiaryFixed = Color(0xFFE4DFFF)
val MdTertiaryFixedDim = Color(0xFFC6BFFF)
val MdOnTertiaryFixed = Color(0xFF17065F)
val MdOnTertiaryFixedVariant = Color(0xFF443B8B)

// ── Surface / background ─────────────────────────────────────────────
val MdBackground = Color(0xFFF8FAF9)       // warm off-white canvas
val MdSurface = Color(0xFFF8FAF9)          // canvas surfaces (top strip, nav)
val MdOnSurface = Color(0xFF191C1C)        // primary text (soft ink)
val MdOnSurfaceVariant = Color(0xFF3E4948) // secondary text
val MdSurfaceVariant = Color(0xFFE1E3E2)   // tonal chip / tag background
val MdOutline = Color(0xFFBEC9C8)          // subtle dividers / borders
val MdOutlineStrong = Color(0xFF6E7979)    // form rest borders

val MdSurfaceContainerLowest = Color(0xFFFFFFFF) // pristine white cards
val MdSurfaceContainerLow = Color(0xFFF2F4F3)
val MdSurfaceContainer = Color(0xFFEDEEED)
val MdSurfaceContainerHigh = Color(0xFFE7E8E8)
val MdSurfaceContainerHighest = Color(0xFFE1E3E2)

// ── Semantic (used sparingly) ────────────────────────────────────────
val MdSuccess = Color(0xFF006F66)          // mint-tinted affirmation ink
val MdSuccessContainer = Color(0xFF81F3E5) // soft mint container
val MdOnSuccess = Color(0xFFFFFFFF)

val MdWarning = Color(0xFF7C5800)          // warm amber ink
val MdWarningContainer = Color(0xFFFFDEA3) // warm amber container
val MdOnWarning = Color(0xFFFFFFFF)

val MdError = Color(0xFFBA1A1A)            // alert coral ink
val MdErrorContainer = Color(0xFFFFDAD6)   // alert coral container
val MdOnError = Color(0xFFFFFFFF)

// ── Card elevation shadows (design tokens for Level 1 / Level 2) ─────
val Level1AmbientAlpha = 0.04f             // 0px 2px 8px rgba(25,28,28,0.04)
val Level2AmbientAlpha = 0.12f             // 0px 6px 16px rgba(0,77,64,0.12)
val OutlineTintAlpha = 0.08f               // 1px solid rgba(0,106,106,0.08)

// ── Dose status identifiers ──────────────────────────────────────────
val DoseTakenGreen = MdSuccess
val DoseSnoozeOrange = MdWarning
val DoseSkipGray = MdOutlineStrong
val DoseMissedRed = MdError

// ── High contrast (accessibility) ────────────────────────────────────
val HighContrastText = Color(0xFF000000)
val HighContrastSecondaryText = Color(0xFF191C1C)
val HighContrastOutline = Color(0xFF6E7979)
val HighContrastPrimary = Color(0xFF004D40)

// ── Legacy aliases (backward compatibility) ──────────────────────────
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
val MedicalOnErrorContainer = MdOnPrimaryContainer
val MedicalInfo = MdPrimary
val MedicalInfoContainer = MdTertiaryFixed
val MedicalOnInfo = MdOnPrimary
val MedicalOnInfoContainer = MdOnTertiaryFixedVariant
