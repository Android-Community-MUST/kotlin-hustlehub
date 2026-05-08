package must.kdroiders.hustlehub.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import must.kdroiders.hustlehub.R

// Bundled Inter font — no internet or Google Play Services required
val InterFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

// Base style — Inter applied to every role so no screen ever falls back to the system font
private fun inter(
    weight: FontWeight,
    size: Float,
    lineHeight: Float,
    tracking: Float = 0f
) = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = tracking.sp
)

val Typography = Typography(
    // Display
    displayLarge  = inter(FontWeight.Bold,     57f, 64f),
    displayMedium = inter(FontWeight.Bold,     45f, 52f),
    displaySmall  = inter(FontWeight.SemiBold, 36f, 44f),

    // Headline (H1 / H2 / H3 per spec)
    headlineLarge  = inter(FontWeight.Bold,     28f, 36f),  // H1
    headlineMedium = inter(FontWeight.SemiBold, 22f, 28f),  // H2
    headlineSmall  = inter(FontWeight.SemiBold, 18f, 24f),  // H3

    // Title
    titleLarge  = inter(FontWeight.SemiBold, 20f, 28f),
    titleMedium = inter(FontWeight.Medium,   16f, 24f, 0.15f),
    titleSmall  = inter(FontWeight.Medium,   14f, 20f, 0.1f),

    // Body (Body / BodySmall per spec)
    bodyLarge  = inter(FontWeight.Normal, 16f, 24f, 0.5f),  // Body
    bodyMedium = inter(FontWeight.Normal, 14f, 20f, 0.25f), // BodySmall
    bodySmall  = inter(FontWeight.Normal, 12f, 16f, 0.4f),

    // Label / Caption
    labelLarge  = inter(FontWeight.Medium, 14f, 20f, 0.1f),
    labelMedium = inter(FontWeight.Medium, 12f, 16f, 0.5f), // Caption per spec
    labelSmall  = inter(FontWeight.Medium, 11f, 16f, 0.5f)
)
