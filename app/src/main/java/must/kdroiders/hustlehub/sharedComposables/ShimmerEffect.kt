package must.kdroiders.hustlehub.sharedComposables

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun shimmerBrush(
    showShimmer: Boolean = true,
    targetValue: Float = 1000f,
): Brush {
    return if (showShimmer) {
        val shimmerColors = listOf(
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
        )
        val transition = rememberInfiniteTransition(label = "shimmer_transition")
        val translateAnim by transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = FastOutLinearInEasing),
            ),
            label = "shimmer_anim",
        )
        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnim, y = translateAnim),
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent),
            start = Offset.Zero,
            end = Offset.Zero,
        )
    }
}

fun Modifier.shimmerEffect(): Modifier =
    composed {
        this.background(shimmerBrush())
    }
