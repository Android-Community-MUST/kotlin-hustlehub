package must.kdroiders.hustlehub.ui.features.map.presentation.view.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory
import kotlin.math.*

@Composable
fun ProviderMarkerContent(category: ServiceCategory) {
    val (icon, color) = getCategoryIconAndColor(category)

    Box(
        modifier = Modifier
            .size(36.dp)
            .background(Color.White, CircleShape)
            .border(2.dp, color, CircleShape)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category.name,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

fun checkLocationPermission(context: Context): Boolean {
    val fineLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val coarseLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    return fineLocation || coarseLocation
}

fun calculateDistanceMeters(
    lat1: Double,
    lng1: Double,
    lat2: Double,
    lng2: Double
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
