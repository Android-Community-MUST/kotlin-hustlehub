package must.kdroiders.hustlehub.ui.features.service.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import must.kdroiders.hustlehub.sharedComposables.StarRatingBar
import must.kdroiders.hustlehub.ui.features.service.domain.model.Review
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

@Composable
fun ReviewItem(
    review: Review,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!review.customerAvatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = review.customerAvatarUrl,
                    contentDescription = review.customerName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = review.customerName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = review.customerName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                val locale = LocalConfiguration.current.locales[0]
                val timeAgo = remember(review.createdAt, locale) {
                    formatTimeAgo(review.createdAt)
                }
                Text(
                    text = timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            StarRatingBar(
                rating = review.rating,
                onRatingChanged = {},
                starSize = 16.dp,
            )
        }

        if (!review.comment.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 52.dp),
            )
        }

        if (showDivider) {
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        }
    }
}

private fun formatTimeAgo(timestampMillis: Long): String {
    if (timestampMillis <= 0) return "Recently"
    val diff = System.currentTimeMillis() - timestampMillis
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
        hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
        days < 30 -> "$days day${if (days > 1) "s" else ""} ago"
        else -> SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault()).format(Date(timestampMillis))
    }
}
