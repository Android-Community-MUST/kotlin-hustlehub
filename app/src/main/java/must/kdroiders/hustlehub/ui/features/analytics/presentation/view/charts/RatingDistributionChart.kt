package must.kdroiders.hustlehub.ui.features.analytics.presentation.view.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RatingDistributionChart(
    distribution: Map<Int, Long>,
    modifier: Modifier = Modifier,
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(distribution) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(800))
    }

    val maxCount = distribution.values.maxOrNull()?.coerceAtLeast(1) ?: 1
    val barColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier = modifier) {
        Text(
            text = "Rating Breakdown",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        (5 downTo 1).forEach { star ->
            val count = distribution[star] ?: 0
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$star\u2605",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(28.dp),
                    fontSize = 12.sp,
                )
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp),
                ) {
                    // track
                    drawRoundRect(
                        color = trackColor,
                        cornerRadius = CornerRadius(7.dp.toPx()),
                        size = Size(size.width, size.height),
                    )
                    // fill
                    val fillWidth = (count.toFloat() / maxCount) * size.width * animProgress.value
                    if (fillWidth > 0) {
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset.Zero,
                            size = Size(fillWidth, size.height),
                            cornerRadius = CornerRadius(7.dp.toPx()),
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(28.dp),
                    fontSize = 12.sp,
                )
            }
        }
    }
}
