package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ┌────────────────────────────────────────────────────────────────────┐
// │  DawaDiary Minimalist Healthcare Palette                           │
// │  Soft, restrained colors. Teal is an accent only — never dominant. │
// └────────────────────────────────────────────────────────────────────┘

// ── Brand (medical teal) ─────────────────────────────────────────────
val MdPrimary = Color(0xFF0B7A86)      // primary accent
val MdPrimaryContainer = Color(0xFFE3F2F4) // light teal tint for chips
val MdOnPrimary = Color(0xFFFFFFFF)
val MdOnPrimaryContainer = Color(0xFF003B45)

// ── Surface / background ─────────────────────────────────────────────
val MdBackground = Color(0xFFFCFDFE)   // near-white canvas
val MdSurface = Color(0xFFFFFFFF)      // raised surfaces
val MdOnSurface = Color(0xFF17222D)    // primary text (soft ink)
val MdOnSurfaceVariant = Color(0xFF637380) // secondary text (muted gray)
val MdSurfaceVariant = Color(0xFFF1F5F8)   // subtle chip / tag background
val MdOutline = Color(0xFFE3E8EC)      // very subtle divider

// ── Semantic (soft, used sparingly) ──────────────────────────────────
val MdSuccess = Color(0xFF15804E)
val MdSuccessContainer = Color(0xFFE3F6EC)
val MdOnSuccess = Color(0xFFFFFFFF)

val MdWarning = Color(0xFFB45F09)
val MdWarningContainer = Color(0xFFFDF0DA)
val MdOnWarning = Color(0xFFFFFFFF)

val MdError = Color(0xFFC0392B)
val MdErrorContainer = Color(0xFFFBE9E7)
val MdOnError = Color(0xFFFFFFFF)

// ── Dose status identifiers ──────────────────────────────────────────
val DoseTakenGreen = MdSuccess
val DoseSnoozeOrange = MdWarning
val DoseSkipGray = MdOnSurfaceVariant
val DoseMissedRed = MdError

// ── High contrast (accessibility) ────────────────────────────────────
val HighContrastText = Color(0xFF000000)
val HighContrastSecondaryText = Color(0xFF2E3D46)
val HighContrastOutline = Color(0xFF9AA7B1)
val HighContrastPrimary = Color(0xFF085F68)

// ── Legacy aliases (backward compatibility) ──────────────────────────
val MedicalPrimary = MdPrimary
val MedicalPrimaryContainer = MdPrimaryContainer
val MedicalOnPrimary = MdOnPrimary
val MedicalOnPrimaryContainer = MdOnPrimaryContainer
val MedicalSecondary = MdPrimary
val MedicalSecondaryContainer = MdPrimaryContainer
val MedicalOnSecondary = MdOnPrimary
val MedicalOnSecondaryContainer = MdOnPrimaryContainer
val MedicalTertiary = MdPrimary
val MedicalTertiaryContainer = MdPrimaryContainer
val MedicalOnTertiary = MdOnPrimary
val MedicalOnTertiaryContainer = MdOnPrimaryContainer
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
val MedicalOnSuccessContainer = MdOnPrimaryContainer
val MedicalWarning = MdWarning
val MedicalWarningContainer = MdWarningContainer
val MedicalOnWarning = MdOnWarning
val MedicalOnWarningContainer = MdOnPrimaryContainer
val MedicalError = MdError
val MedicalErrorContainer = MdErrorContainer
val MedicalOnError = MdOnError
val MedicalOnErrorContainer = MdOnPrimaryContainer
val MedicalInfo = MdPrimary
val MedicalInfoContainer = MdPrimaryContainer
val MedicalOnInfo = MdOnPrimary
val MedicalOnInfoContainer = MdOnPrimaryContainer
