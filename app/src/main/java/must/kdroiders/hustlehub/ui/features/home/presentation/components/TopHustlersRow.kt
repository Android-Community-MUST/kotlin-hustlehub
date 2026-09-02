package must.kdroiders.hustlehub.ui.features.home.presentation.components

import android.widget.Toast
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import must.kdroiders.hustlehub.ui.features.home.domain.model.TopHustler
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory
import must.kdroiders.hustlehub.ui.theme.CategoryNeonCyan
import must.kdroiders.hustlehub.ui.theme.CategoryNeonDefault
import must.kdroiders.hustlehub.ui.theme.CategoryNeonGreen
import must.kdroiders.hustlehub.ui.theme.CategoryNeonPurple
import must.kdroiders.hustlehub.ui.theme.HustleWarningAmber

@Composable
fun TopHustlersRow(
    hustlers: List<TopHustler>,
    onHustlerClick: (hustlerId: String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (hustlers.isEmpty()) return

    Column(modifier = modifier) {
        TopHustlersHeader()
        Spacer(modifier = Modifier.height(12.dp))
        TopHustlersCarousel(hustlers = hustlers, onHustlerClick = onHustlerClick)
    }
}

@Composable
fun TopHustlersHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Top Hustlers",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun TopHustlersCarousel(
    hustlers: List<TopHustler>,
    onHustlerClick: (hustlerId: String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = hustlers, key = { it.id }) { hustler ->
            TopHustlerCard(hustler = hustler, onHustlerClick = onHustlerClick)
        }
    }
}

@Composable
fun TopHustlerCard(
    hustler: TopHustler,
    onHustlerClick: (hustlerId: String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .width(180.dp)
            .height(240.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable {
                Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
                onHustlerClick(hustler.id)
            },
    ) {
        // Hero image (placeholder gradient when no URL)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(
                    Brush.linearGradient(
                        colors = when (hustler.category) {
                            ServiceCategory.SALON -> listOf(CategoryNeonPurple.copy(alpha = 0.4f), MaterialTheme.colorScheme.surfaceVariant)
                            ServiceCategory.TUTORING -> listOf(CategoryNeonGreen.copy(alpha = 0.4f), MaterialTheme.colorScheme.surfaceVariant)
                            ServiceCategory.DESIGN -> listOf(CategoryNeonCyan.copy(alpha = 0.4f), MaterialTheme.colorScheme.surfaceVariant)
                            else -> listOf(CategoryNeonDefault.copy(alpha = 0.4f), MaterialTheme.colorScheme.surfaceVariant)
                        },
                    ),
                ),
        ) {
            if (hustler.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = hustler.imageUrl,
                    contentDescription = hustler.providerName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }

        // Card info bottom overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(12.dp),
        ) {
            Text(
                text = hustler.providerName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = hustler.serviceTitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = HustleWarningAmber,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "%.1f".format(hustler.rating),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Text(
                    text = hustler.priceLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
