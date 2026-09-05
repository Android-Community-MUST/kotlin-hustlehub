package must.kdroiders.hustlehub.ui.features.service.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.sharedComposables.RatingBar

@Composable
fun ReviewSummaryCard(
    averageRating: Float,
    totalReviews: Int,
    modifier: Modifier = Modifier,
) {
    // We visually mock the distribution bars based on the average rating,
    // since the backend currently doesn't provide the exact breakdown.
    val distribution = calculateDummyDistribution(averageRating, totalReviews)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Left side: Large Rating
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(0.4f),
        ) {
            Text(
                text = "%.1f".format(averageRating),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            RatingBar(rating = averageRating, starSize = 14.dp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.reviews_count_format, totalReviews),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.width(24.dp))

        // Right side: Distribution Bars
        Column(
            modifier = Modifier.weight(0.6f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // 5 stars to 1 star
            for (stars in 5 downTo 1) {
                val ratio = if (totalReviews == 0) 0f else distribution[stars - 1] / totalReviews.toFloat()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "$stars",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(12.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = ratio)
                                .height(6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.primary),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Creates a visually plausible distribution of ratings based on the average.
 */
private fun calculateDummyDistribution(
    average: Float,
    total: Int,
): IntArray {
    if (total == 0) return intArrayOf(0, 0, 0, 0, 0)

    val dist = IntArray(5)
    when {
        average >= 4.5f -> {
            dist[4] = (total * 0.75).toInt()
            dist[3] = (total * 0.15).toInt()
            dist[2] = (total * 0.05).toInt()
            dist[1] = (total * 0.03).toInt()
            dist[0] = total - dist.sum()
        }
        average >= 3.5f -> {
            dist[4] = (total * 0.40).toInt()
            dist[3] = (total * 0.40).toInt()
            dist[2] = (total * 0.10).toInt()
            dist[1] = (total * 0.05).toInt()
            dist[0] = total - dist.sum()
        }
        average >= 2.5f -> {
            dist[4] = (total * 0.10).toInt()
            dist[3] = (total * 0.20).toInt()
            dist[2] = (total * 0.40).toInt()
            dist[1] = (total * 0.20).toInt()
            dist[0] = total - dist.sum()
        }
        else -> {
            dist[4] = (total * 0.05).toInt()
            dist[3] = (total * 0.10).toInt()
            dist[2] = (total * 0.20).toInt()
            dist[1] = (total * 0.30).toInt()
            dist[0] = total - dist.sum()
        }
    }
    return dist
}
