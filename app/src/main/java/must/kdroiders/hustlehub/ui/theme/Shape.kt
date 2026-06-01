package must.kdroiders.hustlehub.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * HustleHub shape system.
 *
 * Defines consistent corner radii used throughout the app for cards, buttons,
 * bottom sheets, dialogs, and other containers.
 */
val HustleShapes = Shapes(
    // Chips, small badges, compact tags
    extraSmall = RoundedCornerShape(4.dp),

    // Text fields, list items
    small = RoundedCornerShape(8.dp),

    // Cards, buttons
    medium = RoundedCornerShape(12.dp),

    // Bottom sheets, dialogs, large cards
    large = RoundedCornerShape(16.dp),

    // FABs, image containers, full-round elements
    extraLarge = RoundedCornerShape(24.dp),
)
