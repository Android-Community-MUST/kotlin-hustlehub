package must.kdroiders.hustlehub.ui.features.map.presentation.view.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import must.kdroiders.hustlehub.ui.features.map.domain.model.MapPin
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory
import kotlin.math.*

@Composable
fun ProviderMarkerContent(pin: MapPin) {
    val (icon, baseColor) = getCategoryIconAndColor(pin.category)
    val neonColor = remember(pin.category) {
        when (pin.category) {
            ServiceCategory.SALON -> Color(0xFFB388FF)       // Neon light purple
            ServiceCategory.LAUNDRY -> Color(0xFF80D8FF)     // Neon cyan/blue
            ServiceCategory.TUTORING -> Color(0xFF00E676)    // Neon vibrant green
            ServiceCategory.FOOD -> Color(0xFFFFB300)        // Neon amber/gold
            ServiceCategory.TECH -> Color(0xFF00E5FF)        // Neon cyan
            ServiceCategory.FASHION -> Color(0xFF82B1FF)     // Neon blue/indigo
            ServiceCategory.PHOTOGRAPHY -> Color(0xFFFF4081) // Neon pink
            else -> Color(0xFFE0E0E0)
        }
    }

    val pillBgColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    val titleTextColor = MaterialTheme.colorScheme.onSurface
    val subtitleTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it * 2 },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        ) + fadeIn(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(IntrinsicSize.Max),
        ) {
            // Pill Layout with Glowing Neon Outer Halo
            Row(
                modifier = Modifier
                    .border(
                        width = 3.dp,
                        color = neonColor.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(26.dp),
                    )
                    .background(
                        color = pillBgColor,
                        shape = RoundedCornerShape(24.dp),
                    )
                    .border(
                        width = 1.5.dp,
                        color = neonColor,
                        shape = RoundedCornerShape(24.dp),
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Icon Badge
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = baseColor.copy(alpha = 0.25f),
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = neonColor,
                        modifier = Modifier.size(14.dp),
                    )
                }

                // Info Column
                Column(
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = pin.providerName,
                        color = titleTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        maxLines = 1,
                        softWrap = false,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = pin.category.label,
                            color = subtitleTextColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = "•",
                            color = subtitleTextColor,
                            fontSize = 9.sp,
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD740),
                            modifier = Modifier.size(10.dp),
                        )
                        Text(
                            text = String.format(java.util.Locale.US, "%.1f", pin.averageRating),
                            color = titleTextColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            // Pointer Triangle — downward caret below the pill
            Box(
                modifier = Modifier
                    .offset(y = (-2).dp)
                    .size(10.dp)
                    .graphicsLayer(rotationZ = 45f)
                    .background(pillBgColor)
                    .border(
                        width = 1.5.dp,
                        color = neonColor,
                        shape = RoundedCornerShape(topStart = 0.dp),
                    ),
            )
        }
    }
}

fun getCategoryIconAndColor(category: ServiceCategory): Pair<androidx.compose.ui.graphics.vector.ImageVector, Color> {
    return when (category) {
        ServiceCategory.SALON -> Icons.Default.ContentCut to Color(0xFF9C27B0)
        ServiceCategory.LAUNDRY -> Icons.Default.LocalMall to Color(0xFF2196F3)
        ServiceCategory.TUTORING -> Icons.Default.School to Color(0xFF4CAF50)
        ServiceCategory.FOOD -> Icons.Default.Restaurant to Color(0xFFFF9800)
        ServiceCategory.TECH -> Icons.Default.Computer to Color(0xFF009688)
        ServiceCategory.FASHION -> Icons.Default.Checkroom to Color(0xFF3F51B5)
        ServiceCategory.PHOTOGRAPHY -> Icons.Default.PhotoCamera to Color(0xFFE91E63)
        else -> Icons.Default.Place to Color(0xFF757575)
    }
}

@Composable
fun MapControlFloatingButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape)
            .padding(2.dp),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

fun checkLocationPermission(context: Context): Boolean {
    val fineLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED

    val coarseLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED

    return fineLocation || coarseLocation
}

fun calculateDistanceMeters(
    lat1: Double,
    lng1: Double,
    lat2: Double,
    lng2: Double,
): Double {
    val r = 6371000.0 // Earth's radius in meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2).pow(2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
        sin(dLng / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}

fun formatDistance(meters: Double): String {
    return if (meters < 1000.0) {
        "${kotlin.math.round(meters).toInt()}m away"
    } else {
        val km = meters / 1000.0
        String.format(java.util.Locale.US, "%.1fkm away", km)
    }
}
