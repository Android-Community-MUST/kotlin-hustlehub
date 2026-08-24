package must.kdroiders.hustlehub.sharedComposables

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Interactive 5-star rating bar supporting both tap and drag gestures.
 *
 * @param rating Current selected rating (1 to 5, or 0 if unselected).
 * @param onRatingChanged Callback triggered when user taps or drags to select a rating.
 * @param starSize Size of each individual star.
 * @param activeColor Color for filled active stars.
 * @param inactiveColor Color for outlined inactive stars.
 * @param modifier Optional modifier.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StarRatingBar(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    starSize: Dp = 40.dp,
    activeColor: Color = Color(0xFFFFD700),
    inactiveColor: Color = Color(0xFFCCCCCC),
) {
    var rowWidth by remember { mutableIntStateOf(0) }

    fun calculateRating(xPosition: Float): Int {
        if (rowWidth <= 0) return rating
        val fraction = (xPosition / rowWidth.toFloat()).coerceIn(0f, 1f)
        val calculated = (fraction * 5).toInt() + 1
        return calculated.coerceIn(1, 5)
    }

    Row(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                rowWidth = coordinates.size.width
            }.pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newRating = calculateRating(offset.x)
                    onRatingChanged(newRating)
                }
            }.pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val newRating = calculateRating(offset.x)
                        onRatingChanged(newRating)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val newRating = calculateRating(change.position.x)
                        onRatingChanged(newRating)
                    },
                )
            }.semantics {
                contentDescription = "Rating bar: $rating out of 5 stars"
            },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        (1..5).forEach { star ->
            val isSelected = star <= rating
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.2f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
                label = "starScale_$star",
            )

            Icon(
                imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "$star star${if (star > 1) "s" else ""}",
                tint = if (isSelected) activeColor else inactiveColor,
                modifier = Modifier
                    .size(starSize)
                    .scale(scale),
            )
        }
    }
}
