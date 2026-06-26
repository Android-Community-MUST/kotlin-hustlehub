package must.kdroiders.hustlehub.ui.features.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import must.kdroiders.hustlehub.ui.features.home.domain.model.TopHustler
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory
import must.kdroiders.hustlehub.ui.theme.HustleWarningAmber

@Composable
fun TopHustlersRow(
    hustlers: List<TopHustler>,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = hustlers, key = { it.id }) { hustler ->
            TopHustlerCard(hustler = hustler)
        }
    }
}

@Composable
fun TopHustlerCard(
    hustler: TopHustler,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(180.dp)
            .height(240.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { /* TODO: navigate to service detail */ },
    ) {
        // Hero image (placeholder gradient when no URL)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(
                    Brush.linearGradient(
                        colors = when (hustler.category) {
                            ServiceCategory.SALON -> listOf(Color(0xFF4A148C), Color(0xFF880E4F))
                            ServiceCategory.TUTORING -> listOf(Color(0xFF1A237E), Color(0xFF0D47A1))
                            ServiceCategory.DESIGN -> listOf(Color(0xFF004D40), Color(0xFF006064))
                            else -> listOf(Color(0xFF1B5E20), Color(0xFF33691E))
                        },
                    ),
                ),
        ) {
            // Load actual image if URL provided
            if (hustler.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = hustler.imageUrl,
                    contentDescription = hustler.serviceTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            // Rating badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = HustleWarningAmber,
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = hustler.rating.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                    )
                }
            }
        }

        // Bottom info
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp, top = 6.dp),
        ) {
            Text(
                text = hustler.category.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
            )
            Text(
                text = hustler.serviceTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = hustler.priceLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
            )
        }
    }
}
