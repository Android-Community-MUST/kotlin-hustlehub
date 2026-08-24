package must.kdroiders.hustlehub.ui.features.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability
import must.kdroiders.hustlehub.ui.theme.HustleActiveGreen
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.getValue
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import must.kdroiders.hustlehub.navigation.LocalSharedTransitionScope
import must.kdroiders.hustlehub.sharedComposables.FeaturedBadge
import must.kdroiders.hustlehub.sharedComposables.bouncyClickable

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ServiceCard(
    service: Service,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val displayPrice = remember(service.priceRange) {
        "KES ${service.priceRange.split("-").first().trim()}"
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .bouncyClickable(onClick = onClick)
            .padding(bottom = 12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            val imageUrl = service.portfolio.firstOrNull() ?: service.iconUrl
            if (imageUrl.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest
                        .Builder(LocalContext.current)
                        .data(imageUrl)
                        // Request exactly card-thumbnail resolution to avoid downloading full-res
                        .size(Size(360, 200))
                        .scale(Scale.FILL)
                        .crossfade(true)
                        .build(),
                    contentDescription = service.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            LocalSharedTransitionScope.current?.run {
                                Modifier.sharedElement(
                                    rememberSharedContentState(key = "service_image_${service.id}"),
                                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                )
                            } ?: Modifier
                        ),
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                horizontalArrangement = Arrangement
                    .spacedBy(6.dp),
            ) {
                AvailabilityBadge(
                    availability = service.availability,
                )
                if (service.isFeatured) {
                   FeaturedBadge()
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = service.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                )
                Text(
                    text = displayPrice,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                )
            }

            Spacer(Modifier.height(4.dp))

            service.location?.label?.let { label ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AvailabilityBadge(
    availability: ServiceAvailability,
    modifier: Modifier = Modifier,
) {
    // Derive target color for every availability state — keep composable mounted
    // so animateColorAsState transitions smoothly instead of unmounting/remounting.
    val targetColor = when (availability) {
        ServiceAvailability.AVAILABLE -> HustleActiveGreen
        ServiceAvailability.BUSY      -> MaterialTheme.colorScheme.tertiary
        ServiceAvailability.OFFLINE   -> MaterialTheme.colorScheme.error
        else                          -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val label = when (availability) {
        ServiceAvailability.AVAILABLE -> "LIVE"
        ServiceAvailability.BUSY      -> "BUSY"
        ServiceAvailability.OFFLINE   -> "AWAY"
        else                          -> "AWAY"
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "availability_color",
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.65f))
            .padding(horizontal = 6.dp, vertical = 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(animatedColor),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}
