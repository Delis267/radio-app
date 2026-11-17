package com.iu.radio_app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.iu.radio_app.R

private val Roboto =
    FontFamily(
        Font(R.font.roboto_regular, FontWeight.Normal),
        Font(R.font.roboto_medium, FontWeight.Medium),
        Font(R.font.roboto_semibold, FontWeight.SemiBold),
        Font(R.font.roboto_bold, FontWeight.Bold),
    )

private val SpaceGrotesk = FontFamily(Font(R.font.space_grotesk_bold, FontWeight.Bold))

private fun heading(size: Int, lineHeight: Int, letterSpacing: Double) =
    TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = size.sp,
        lineHeight = lineHeight.sp,
        letterSpacing = letterSpacing.sp,
    )

// Material-Standardgrößen in Roboto, Überschriften aus Figma (FUNK/…) in Space Grotesk
private val Default = Typography()

val Typography =
    Typography(
        displayLarge = Default.displayLarge.copy(fontFamily = Roboto),
        displayMedium = Default.displayMedium.copy(fontFamily = Roboto),
        displaySmall = Default.displaySmall.copy(fontFamily = Roboto),
        headlineLarge = heading(size = 32, lineHeight = 36, letterSpacing = -0.7),
        headlineMedium = Default.headlineMedium.copy(fontFamily = Roboto),
        headlineSmall = Default.headlineSmall.copy(fontFamily = Roboto),
        titleLarge = heading(size = 22, lineHeight = 28, letterSpacing = 0.0),
        titleMedium = heading(size = 18, lineHeight = 24, letterSpacing = 0.0),
        titleSmall = Default.titleSmall.copy(fontFamily = Roboto),
        bodyLarge = Default.bodyLarge.copy(fontFamily = Roboto),
        bodyMedium = Default.bodyMedium.copy(fontFamily = Roboto),
        bodySmall = Default.bodySmall.copy(fontFamily = Roboto),
        labelLarge = Default.labelLarge.copy(fontFamily = Roboto),
        labelMedium = Default.labelMedium.copy(fontFamily = Roboto),
        labelSmall = Default.labelSmall.copy(fontFamily = Roboto),
    )
