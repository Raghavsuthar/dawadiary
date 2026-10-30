package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// ┌────────────────────────────────────────────────────────────────────┐
// │  DawaDiary Typography — Atkinson Hyperlegible Next (Stitch design) │
// │  Exclusive family for low-vision legibility & safe dose glyphs.    │
// │  Weights: 400 instructions • 600 headers/names • 700 time anchors. │
// └────────────────────────────────────────────────────────────────────┘

val AtkinsonHyperlegibleFamily = FontFamily(
    Font(R.font.atkinson_hyperlegible_next_400_normal, FontWeight.Normal),
    Font(R.font.atkinson_hyperlegible_next_500_normal, FontWeight.Medium),
    Font(R.font.atkinson_hyperlegible_next_600_normal, FontWeight.SemiBold),
    Font(R.font.atkinson_hyperlegible_next_700_normal, FontWeight.Bold)
)

val Typography = Typography(
    displayLarge = TextStyle( // display-lg • 36/44 • 700 • -0.02em
        fontFamily = AtkinsonHyperlegibleFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.72).sp
    ),
    displayMedium = TextStyle( // display-lg-mobile • 30/38 • 700 • -0.01em
        fontFamily = AtkinsonHyperlegibleFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.3).sp
    ),
    displaySmall = TextStyle(
        fontFamily = AtkinsonHyperlegibleFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.2).sp
    ),
    headlineLarge = TextStyle( // headline-lg • 28/36 • 700
        fontFamily = AtkinsonHyperlegibleFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle( // headline-md • 24/32 • 600
        fontFamily = AtkinsonHyperlegibleFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    headlineSmall = TextStyle( // headline-sm • 20/28 • 600
        fontFamily = AtkinsonHyperlegibleFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle( // title-lg • 18/26 • 600
        fontFamily = AtkinsonHyperlegibleFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AtkinsonHyperlegibleFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = AtkinsonHyperlegibleFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle( // body-lg • 17/26 • 400
        fontFamily = AtkinsonHyperlegibleFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 26.sp
    ),
    bodyMedium = TextStyle( // body-md • 15/22 • 400
        fontFamily = AtkinsonHyperlegibleFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodySmall = TextStyle(
        fontFamily = AtkinsonHyperlegibleFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle( // label-lg • 14/20 • 600
        fontFamily = AtkinsonHyperlegibleFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.14.sp
    ),
    labelMedium = TextStyle( // label-md • 13/18 • 500
        fontFamily = AtkinsonHyperlegibleFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelSmall = TextStyle( // label-sm • 11/16 • 600 • 0.03em
        fontFamily = AtkinsonHyperlegibleFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.33.sp
    )
)
