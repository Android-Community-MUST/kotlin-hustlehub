package must.kdroiders.hustlehub.sharedComposables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A reusable 5-star rating bar.
 *
 * Displays filled, half-filled, or outlined stars based on [rating].
 * When [onRatingChanged] is provided the stars become tappable so users can
 * submit their own rating.
 *
 * @param rating     Current rating value (0.0 – 5.0). Supports half-stars.
 * @param modifier   Optional [Modifier].
 * @param starCount  Total number of stars (default 5).
 * @param starSize   Size of each star icon.
 * @param filledColor Color for filled / half-filled stars.
 * @param emptyColor  Color for outline stars.
 * @param onRatingChanged Called with the new rating (1–5) when a star is tapped.
 *                        Pass `null` to make the bar read-only.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RatingBar(
    rating: Float,
    modifier: Modifier = Modifier,
    starCount: Int = 5,
    starSize: Dp = 24.dp,
    filledColor: Color = MaterialTheme.colorScheme.primary,
    emptyColor: Color = MaterialTheme.colorScheme.outlineVariant,
    onRatingChanged: ((Int) -> Unit)? = null,
) {
    val motionScheme = MaterialTheme.motionScheme
    val scaleSpec = motionScheme.defaultEffectsSpec<Float>()

    Row(
        modifier = modifier.semantics {
            contentDescription = "Rating: ${"%.1f".format(rating)} out of $starCount stars"
        },
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 1..starCount) {
            val isFull = rating >= i
            val isHalf = rating >= i - 0.5f && rating < i

            // Scale animation driven by MotionScheme
            val scale by animateFloatAsState(
                targetValue = if (isFull || isHalf) 1.05f else 1f,
                animationSpec = scaleSpec,
                label = "starScale$i",
            )

            val icon = when {
                isFull -> Icons.Filled.Star
                isHalf -> Icons.AutoMirrored.Filled.StarHalf
                else -> Icons.Filled.StarOutline
            }
            val tint = if (isFull || isHalf) filledColor else emptyColor

            Icon(
                imageVector = icon,
                contentDescription = "Star $i",
                tint = tint,
                modifier = Modifier
                    .size(starSize)
                    .scale(scale)
                    .then(
                        if (onRatingChanged != null) {
                            Modifier.clickable { onRatingChanged(i) }
                        } else {
                            Modifier
                        },
                    ),
            )
        }
    }
}
