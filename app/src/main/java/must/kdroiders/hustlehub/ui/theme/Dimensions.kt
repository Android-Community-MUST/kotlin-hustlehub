package must.kdroiders.hustlehub.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Adaptive responsive dimensions system.
 * Optimized for card-heavy marketplace layouts with immersive edge-to-edge margins.
 */
@Immutable
data class Dimensions(
    val horizontalPadding: Dp = 12.dp,
    val verticalPadding: Dp = 12.dp,
    val gridSpacing: Dp = 10.dp,
    val gridColumns: Int = 2,
    val cardCornerRadius: Dp = 20.dp,
    val cardContentPadding: Dp = 16.dp,
    val spacing4: Dp = 4.dp,
    val spacing8: Dp = 8.dp,
    val spacing12: Dp = 12.dp,
    val spacing16: Dp = 16.dp,
    val spacing24: Dp = 24.dp,
    val spacing32: Dp = 32.dp,
    val spacing48: Dp = 48.dp,
)

/** Small compact phones (< 360dp width e.g. Pixel 4a / small budget devices) */
fun compactDimensions() = Dimensions(
    horizontalPadding = 8.dp,
    verticalPadding = 8.dp,
    gridSpacing = 8.dp,
    gridColumns = 2,
    cardCornerRadius = 16.dp,
    cardContentPadding = 12.dp,
)

/** Standard devices (360dp – 600dp width) */
fun standardDimensions() = Dimensions(
    horizontalPadding = 12.dp,
    verticalPadding = 12.dp,
    gridSpacing = 10.dp,
    gridColumns = 2,
    cardCornerRadius = 20.dp,
    cardContentPadding = 16.dp,
)

/** Large foldables and tablets (>= 600dp width) */
fun expandedDimensions() = Dimensions(
    horizontalPadding = 20.dp,
    verticalPadding = 16.dp,
    gridSpacing = 14.dp,
    gridColumns = 3,
    cardCornerRadius = 24.dp,
    cardContentPadding = 20.dp,
)

val DefaultDimensions = standardDimensions()

val LocalDimensions = compositionLocalOf { DefaultDimensions }
