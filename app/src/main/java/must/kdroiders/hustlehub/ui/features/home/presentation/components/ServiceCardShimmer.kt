package must.kdroiders.hustlehub.ui.features.home.presentation.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

@Composable
fun ServiceCardShimmer(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue = -300f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_x",
    )

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.surfaceVariant,
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateX, 0f),
        end = Offset(translateX + 300f, 300f),
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Image placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(brush),
        )
        // Provider name placeholder
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(brush),
        )
        // Title placeholder
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(brush),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Rating placeholder
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(brush),
            )
            // Price placeholder
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(brush),
            )
        }
        // Badge placeholder
        Box(
            modifier = Modifier
                .width(70.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(brush),
        )
        Spacer(Modifier.height(4.dp))
    }
}
