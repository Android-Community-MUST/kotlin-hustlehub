package must.kdroiders.hustlehub.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * HustleHub spacing system based on a 4dp grid.
 *
 * Example usage:
 * ```kotlin
 * Column(
 *     modifier = Modifier.padding(LocalDimensions.current.spacing16)
 * ) {
 *     Text("Hello")
 *     Spacer(modifier = Modifier.height(LocalDimensions.current.spacing8))
 *     Text("World")
 * }
 * ```
 */
@Immutable
data class Dimensions(
    val spacing4: Dp = 4.dp,
    val spacing8: Dp = 8.dp,
    val spacing12: Dp = 12.dp,
    val spacing16: Dp = 16.dp,
    val spacing24: Dp = 24.dp,
    val spacing32: Dp = 32.dp,
    val spacing48: Dp = 48.dp,
)

// Shared singleton — never changes at runtime
val DefaultDimensions = Dimensions()

// staticCompositionLocalOf: reads are NOT tracked, so no recomposition when provided value changes
val LocalDimensions = staticCompositionLocalOf { DefaultDimensions }
