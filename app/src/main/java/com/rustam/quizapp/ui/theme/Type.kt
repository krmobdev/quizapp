package com.rustam.quizapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.rustam.quizapp.R

/**
 * Inter font family — closest free equivalent to SF Pro on Android.
 * Falls back to the system sans-serif if the font file is missing.
 */
val InterFontFamily = try {
    FontFamily(
        Font(R.font.inter_regular,    FontWeight.Normal),
        Font(R.font.inter_medium,     FontWeight.Medium),
        Font(R.font.inter_semibold,   FontWeight.SemiBold),
        Font(R.font.inter_bold,       FontWeight.Bold),
        Font(R.font.inter_extrabold,  FontWeight.ExtraBold)
    )
} catch (e: Exception) {
    FontFamily.SansSerif
}

/**
 * iOS-style typography scale — slightly tightened for Russian text which is
 * wider than English on average.  Mirrors Apple HIG sizes but –2..–4 sp each
 * to prevent word-wrapping in card layouts.
 *
 * Mapping:
 *   displaySmall  → Large Title  28 sp  (was 34)
 *   headlineMedium → Title 1     22 sp  (was 28)
 *   titleLarge     → Title 2     20 sp  (was 22)
 *   headlineSmall  → Title 3     18 sp  (was 20)
 *   titleMedium    → Headline    15 sp  (was 17) ← used in card titles
 *   bodyLarge      → Body        15 sp  (was 17)
 *   bodyMedium     → Callout     14 sp  (was 16)
 *   bodySmall      → Subhead     13 sp  (was 15)
 *   labelLarge     → Footnote    12 sp  (was 13)
 *   labelMedium    → Caption 1   11 sp  (was 12)
 *   labelSmall     → Caption 2   10 sp  (was 11)
 *   titleSmall     → Title Small 13 sp  (was 15)
 */
val Typography = Typography(
    // Large Title — screen headers (AnimaQuiz, Магазин …)
    displaySmall = TextStyle(
        fontFamily   = InterFontFamily,
        fontWeight   = FontWeight.Bold,
        fontSize     = 28.sp,
        lineHeight   = 34.sp,
        letterSpacing = (-0.3).sp
    ),
    // Title 1
    headlineMedium = TextStyle(
        fontFamily   = InterFontFamily,
        fontWeight   = FontWeight.Bold,
        fontSize     = 22.sp,
        lineHeight   = 28.sp,
        letterSpacing = (-0.2).sp
    ),
    // Title 2
    titleLarge = TextStyle(
        fontFamily   = InterFontFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 20.sp,
        lineHeight   = 26.sp,
        letterSpacing = (-0.2).sp
    ),
    // Title 3
    headlineSmall = TextStyle(
        fontFamily   = InterFontFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 18.sp,
        lineHeight   = 23.sp,
        letterSpacing = (-0.1).sp
    ),
    // Headline (semibold body) — card titles, section headers
    titleMedium = TextStyle(
        fontFamily   = InterFontFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 15.sp,
        lineHeight   = 20.sp,
        letterSpacing = (-0.1).sp
    ),
    // Body
    bodyLarge = TextStyle(
        fontFamily   = InterFontFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 15.sp,
        lineHeight   = 20.sp,
        letterSpacing = (-0.1).sp
    ),
    // Callout
    bodyMedium = TextStyle(
        fontFamily   = InterFontFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 14.sp,
        lineHeight   = 19.sp,
        letterSpacing = (-0.05).sp
    ),
    // Subheadline — secondary text in cards
    bodySmall = TextStyle(
        fontFamily   = InterFontFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 13.sp,
        lineHeight   = 18.sp,
        letterSpacing = 0.sp
    ),
    // Footnote
    labelLarge = TextStyle(
        fontFamily   = InterFontFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 12.sp,
        lineHeight   = 16.sp,
        letterSpacing = 0.sp
    ),
    // Caption 1
    labelMedium = TextStyle(
        fontFamily   = InterFontFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 11.sp,
        lineHeight   = 14.sp,
        letterSpacing = 0.sp
    ),
    // Caption 2
    labelSmall = TextStyle(
        fontFamily   = InterFontFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 10.sp,
        lineHeight   = 13.sp,
        letterSpacing = 0.sp
    ),
    // Title Small — price tags, trailing chips
    titleSmall = TextStyle(
        fontFamily   = InterFontFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 13.sp,
        lineHeight   = 18.sp,
        letterSpacing = (-0.1).sp
    )
)