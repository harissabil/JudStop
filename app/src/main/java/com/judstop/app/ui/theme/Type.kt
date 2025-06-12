package com.judstop.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.judstop.app.R

object AppFont {
    val poppins = FontFamily(
        Font(R.font.pregular),
        Font(R.font.pitalic, style = FontStyle.Italic),
        Font(R.font.pmedium, FontWeight.Medium),
        Font(R.font.pmedium_italic, FontWeight.Medium, style = FontStyle.Italic),
        Font(R.font.psemibold, FontWeight.SemiBold),
        Font(R.font.psemibold_italic, FontWeight.SemiBold, style = FontStyle.Italic),
        Font(R.font.psemibold, FontWeight.Bold),
        Font(R.font.psemibold_italic, FontWeight.Bold, style = FontStyle.Italic),
        Font(R.font.plight, FontWeight.Light)
    )
}

private val defaultTypography = Typography()
val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = AppFont.poppins),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = AppFont.poppins),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = AppFont.poppins),

    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = AppFont.poppins),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = AppFont.poppins),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = AppFont.poppins),

    titleLarge = defaultTypography.titleLarge.copy(fontFamily = AppFont.poppins),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = AppFont.poppins),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = AppFont.poppins),

    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = AppFont.poppins),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = AppFont.poppins),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = AppFont.poppins),

    labelLarge = defaultTypography.labelLarge.copy(fontFamily = AppFont.poppins),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = AppFont.poppins),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = AppFont.poppins)
)