package must.kdroiders.hustlehub.ui.features.map.presentation.view.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.LatLng
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleButtonVariant
import must.kdroiders.hustlehub.ui.features.map.domain.model.MapPin
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability
import must.kdroiders.hustlehub.ui.theme.HustleSuccess
import must.kdroiders.hustlehub.ui.theme.HustleWarning
import must.kdroiders.hustlehub.ui.theme.HustleWarningAmber

@Composable
fun BottomSheetContent(
    pin: MapPin,
    userLocation: LatLng?,
    onNavigateToServiceDetail: (serviceId: String) -> Unit,
    onNavigateToChatDetail: (providerId: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val distanceMeters = remember(pin, userLocation) {
        userLocation?.let {
            calculateDistanceMeters(it.latitude, it.longitude, pin.lat, pin.lng)
        }
    }
    val distanceText = remember(distanceMeters) {
        distanceMeters?.let { formatDistance(it) }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 1. Header Row (Avatar + Name & Status Info)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Profile photo / default avatar
            if (pin.providerPhotoUrl.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = stringResource(R.string.cd_default_avatar),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp),
                    )
                }
            } else {
                AsyncImage(
                    model = pin.providerPhotoUrl,
                    contentDescription = stringResource(R.string.cd_profile_photo),
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop,
                )
            }

            // Name & Status
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = pin.providerName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                    // Availability Dot & Text
                    val isAvailable = pin.availability == ServiceAvailability.AVAILABLE
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (isAvailable) HustleSuccess else HustleWarning,
                                CircleShape,
                            ),
                    )
                    Text(
                        text = if (isAvailable) stringResource(R.string.status_available) else stringResource(R.string.status_busy_away),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isAvailable) HustleSuccess else HustleWarning,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }

                Text(
                    text = pin.serviceTitle,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }

        // 2. Metadata Info Grid (Rating + Category & Price + Distance)
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Rating
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = stringResource(R.string.cd_rating),
                    tint = HustleWarningAmber,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = pin.averageRating.toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }

            // Category
            Text(
                text = pin.category.label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                ),
            )

            // Distance
            if (distanceText != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = stringResource(R.string.map_cd_location_info),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = distanceText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }
            }
        }

        // 3. Quick Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HustleButton(
                text = stringResource(R.string.action_view_profile),
                onClick = {
                    onDismiss()
                    onNavigateToServiceDetail(pin.serviceId)
                },
                variant = HustleButtonVariant.Outlined,
                modifier = Modifier.weight(1f),
            )

            HustleButton(
                text = stringResource(R.string.action_message),
                onClick = {
                    onDismiss()
                    onNavigateToChatDetail(pin.providerId)
                },
                variant = HustleButtonVariant.Primary,
                modifier = Modifier.weight(1f),
            )
        }

        HustleButton(
            text = stringResource(R.string.action_get_directions),
            onClick = {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("google.navigation:q=${pin.lat},${pin.lng}"),
                ).apply {
                    setPackage("com.google.android.apps.maps")
                }
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    val webIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/maps/search/?api=1&query=${pin.lat},${pin.lng}"),
                    )
                    context.startActivity(webIntent)
                }
            },
            icon = Icons.Default.Place,
            variant = HustleButtonVariant.Secondary,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
