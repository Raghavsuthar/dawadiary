package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ┌────────────────────────────────────────────────────────────────────┐
// │  DawaDiary "Warm Clinical Material" Palette (Stitch design)          │
// │  Deep teal primary, mint / lavender / amber / coral accents.         │
// │  Pristine white cards over a warm off-white canvas.                  │
// └────────────────────────────────────────────────────────────────────┘

// ── Brand (deep clinical teal) ───────────────────────────────────────
val MdPrimary = Color(0xFF147D70)          // primary interactive actions
val MdPrimaryPressed = Color(0xFF116B61)   // active pressed state
val MdPrimaryContainer = Color(0xFFD1F0E0) // soft mint-teal tile (icons/text use MdPrimary on top)
val MdOnPrimary = Color(0xFFFFFFFF)
val MdOnPrimaryContainer = Color(0xFF0D302D)

// ── Brand gradient (hero cards, primary buttons, logo tile) ──────────
val BrandGradientStart = Color(0xFF147D70)
val BrandGradientMid = Color(0xFF168D80)
val BrandGradientEnd = Color(0xFF42A99B)
val BrandGlow = Color(0xFFB4E9D4)          // light accent used on dark gradients
val CardShadowTint = Color(0xFF116B61)     // tinted ambient/spot shadow for cards

val MdPrimaryFixed = Color(0xFFA0F0F0)
val MdPrimaryFixedDim = Color(0xFF84D4D3)
val MdOnPrimaryFixed = Color(0xFF0D302D)
val MdOnPrimaryFixedVariant = Color(0xFF147D70)

// ── Secondary (soft mint) ────────────────────────────────────────────
val MdSecondary = Color(0xFF61757A)
val MdOnSecondary = Color(0xFFFFFFFF)
val MdSecondaryContainer = Color(0xFFD1F0E0)
val MdOnSecondaryContainer = Color(0xFF147D70)

val MdSecondaryFixed = Color(0xFF84F5E8)
val MdSecondaryFixedDim = Color(0xFF66D9CC)
val MdOnSecondaryFixed = Color(0xFF00201D)
val MdOnSecondaryFixedVariant = Color(0xFF116B61)

// ── Tertiary (lavender — external care / appointments) ───────────────
val MdTertiary = Color(0xFF443C8B)
val MdOnTertiary = Color(0xFFFFFFFF)
val MdTertiaryContainer = Color(0xFF8D7BB8)
val MdOnTertiaryContainer = Color(0xFF1A2B33)
val MdTertiaryFixed = Color(0xFFE8E0FA)
val MdTertiaryFixedDim = Color(0xFFD8CCF3)
val MdOnTertiaryFixed = Color(0xFF342A55)
val MdOnTertiaryFixedVariant = Color(0xFF51456F)

// ── Surface / background ─────────────────────────────────────────────
val MdBackground = Color(0xFFF7FAF5)       // warm off-white canvas
val MdSurface = Color(0xFFF7FAF5)          // canvas surfaces (top strip, nav)
val MdOnSurface = Color(0xFF1A2B33)        // primary text (soft ink)
val MdOnSurfaceVariant = Color(0xFF61757A) // secondary text
val MdSurfaceVariant = Color(0xFFE8EFEB)   // tonal chip / tag background
val MdOutline = Color(0xFFDCE6E1)          // subtle dividers / borders
val MdOutlineStrong = Color(0xFF61757A)    // form rest borders

val MdSurfaceContainerLowest = Color(0xFFFFFFFF) // pristine white cards
val MdSurfaceContainerLow = Color(0xFFF2F4F3)
val MdSurfaceContainer = Color(0xFFEDEEED)
val MdSurfaceContainerHigh = Color(0xFFE7E8E8)
val MdSurfaceContainerHighest = Color(0xFFE8EFEB)
val MdSurfaceDim = Color(0xFFD9DADA)

// ── Accent aliases (kept for source compatibility, mapped to the
//    Warm Clinical Material accent roles) ─────────────────────────────
val MdBlue = Color(0xFF168D80)             // information → brand teal
val MdBlueContainer = Color(0xFFE1F3F2)
val MdPurple = MdTertiary                  // appointments → lavender
val MdPurpleContainer = MdTertiaryFixed
val MdCoral = Color(0xFFFA7D61)            // alert coral
val MdCoralContainer = Color(0xFFFFE8DB)
val MdMint = MdSecondary                   // wellness → mint
val MdMintContainer = MdSecondaryContainer

// ── Semantic (used sparingly) ────────────────────────────────────────
val MdSuccess = Color(0xFF147D70)          // mint-tinted affirmation ink
val MdSuccessContainer = Color(0xFFD1F0E0) // soft mint container
val MdOnSuccess = Color(0xFFFFFFFF)

val MdWarning = Color(0xFF87621B)          // warm amber ink
val MdWarningContainer = Color(0xFFFFEBA6) // warm amber container
val MdOnWarning = Color(0xFFFFFFFF)

val MdError = Color(0xFFFA7D61)            // alert coral ink
val MdErrorContainer = Color(0xFFFFE8DB)   // alert coral container
val MdOnErrorContainer = Color(0xFF6B342B) // readable ink on coral container
val MdOnError = Color(0xFFFFFFFF)

// ── Elevation / stroke tokens ────────────────────────────────────────
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
val HighContrastSecondaryText = Color(0xFF1A2B33)
val HighContrastOutline = Color(0xFF61757A)
val HighContrastPrimary = Color(0xFF116B61)

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
val MedicalOnErrorContainer = MdOnErrorContainer
val MedicalInfo = MdPrimary
val MedicalInfoContainer = MdTertiaryFixed
val MedicalOnInfo = MdOnPrimary
val MedicalOnInfoContainer = MdOnTertiaryFixedVariant
