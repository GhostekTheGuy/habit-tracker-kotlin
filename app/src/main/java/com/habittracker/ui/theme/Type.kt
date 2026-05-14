package com.habittracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.habittracker.R

@OptIn(ExperimentalTextApi::class)
private fun interTight(weight: FontWeight) = Font(
    R.font.inter_tight_variable,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val InterTight = FontFamily(
    interTight(FontWeight.Normal),
    interTight(FontWeight.Medium),
    interTight(FontWeight.SemiBold),
    interTight(FontWeight.Bold),
)

private val base = Typography()

val AppTypography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = InterTight),
    displayMedium = base.displayMedium.copy(fontFamily = InterTight),
    displaySmall = base.displaySmall.copy(fontFamily = InterTight),
    headlineLarge = base.headlineLarge.copy(fontFamily = InterTight),
    headlineMedium = base.headlineMedium.copy(fontFamily = InterTight),
    headlineSmall = base.headlineSmall.copy(fontFamily = InterTight),
    titleLarge = base.titleLarge.copy(fontFamily = InterTight),
    titleMedium = base.titleMedium.copy(fontFamily = InterTight),
    titleSmall = base.titleSmall.copy(fontFamily = InterTight),
    bodyLarge = base.bodyLarge.copy(fontFamily = InterTight),
    bodyMedium = base.bodyMedium.copy(fontFamily = InterTight),
    bodySmall = base.bodySmall.copy(fontFamily = InterTight),
    labelLarge = base.labelLarge.copy(fontFamily = InterTight),
    labelMedium = base.labelMedium.copy(fontFamily = InterTight),
    labelSmall = base.labelSmall.copy(fontFamily = InterTight),
)
