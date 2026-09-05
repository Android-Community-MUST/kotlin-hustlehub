package must.kdroiders.hustlehub.ui.features.service.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import must.kdroiders.hustlehub.R

/**
 * A horizontal row of up to 3 portfolio thumbnails.
 * If there are more than 3 images, the 3rd thumbnail displays a dark overlay with '+X' text.
 *
 * @param imageUrls Ordered list of image URLs to display.
 * @param onImageClick Called with the tapped image index.
 */
@Composable
fun PortfolioGallery(
    imageUrls: List<String>,
    modifier: Modifier = Modifier,
    onImageClick: (index: Int) -> Unit = {},
) {
    if (imageUrls.isEmpty()) return

    val displayImages = imageUrls.take(3)
    val remainingCount = imageUrls.size - 3

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        displayImages.forEachIndexed { index, url ->
            val isLastAndMore = index == 2 && remainingCount > 0

            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onImageClick(index) },
            ) {
                AsyncImage(
                    model = url,
                    contentDescription = stringResource(R.string.cd_portfolio_image_format, index + 1),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )

                if (isLastAndMore) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "+$remainingCount",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
        }
    }
}
