@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package must.kdroiders.hustlehub.ui.features.chat.presentation.components

import android.location.Geocoder
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.MapLocationPickerModal
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.extractAreaName
import java.util.Locale

// Campus Landmark Presets
// Coordinates centered on MUST / Nchiru campus, Meru, Kenya
data class CampusLandmark(val label: String, val lat: Double, val lng: Double)

private val MUST_LANDMARKS = listOf(
    CampusLandmark("Main Gate", -0.0076, 37.6534),
    CampusLandmark("Library", -0.0072, 37.6540),
    CampusLandmark("Sci & Tech", -0.0068, 37.6548),
    CampusLandmark("Hostels Area", -0.0090, 37.6520),
    CampusLandmark("Student Center", -0.0082, 37.6528),
    CampusLandmark("Administration", -0.0074, 37.6530),
)

/**
 * WhatsApp-style location sharing sheet.
 *
 * Three ways to share a location in chat:
 *  1. Send Current Location — live GPS with accuracy badge + reverse-geocoded address preview
 *  2. Campus Landmark presets — quick-select chips for MUST/Nchiru landmarks
 *  3. Choose on Map — opens [MapLocationPickerModal] for exact pin drop
 */
@Composable
fun ChatLocationPickerSheet(
    onDismiss: () -> Unit,
    onLocationSelected: (lat: Double, lng: Double, label: String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Sub-screen: show map picker when requested
    var showMapPicker by remember { mutableStateOf(false) }

    // GPS state
    var gpsLat by remember { mutableStateOf(0.0) }
    var gpsLng by remember { mutableStateOf(0.0) }
    var gpsAccuracy by remember { mutableStateOf<Float?>(null) }
    var gpsAddress by remember { mutableStateOf("") }
    var gpsAreaName by remember { mutableStateOf("") }
    var isLoadingGps by remember { mutableStateOf(true) }
    var isGeocodingGps by remember { mutableStateOf(false) }

    // Fetch current GPS location on first composition
    LaunchedEffect(Unit) {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        isLoadingGps = true
        try {
            @Suppress("MissingPermission")
            val location = fusedClient.lastLocation.await()
            if (location != null) {
                gpsLat = location.latitude
                gpsLng = location.longitude
                gpsAccuracy = location.accuracy
                isLoadingGps = false

                // Reverse-geocode in background
                isGeocodingGps = true
                withContext(Dispatchers.IO) {
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(gpsLat, gpsLng, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            val lines = (0..addr.maxAddressLineIndex).mapNotNull { addr.getAddressLine(it) }
                            gpsAddress = lines.joinToString(", ")
                            gpsAreaName = extractAreaName(gpsAddress)
                        }
                    } catch (_: Exception) {
                        gpsAreaName = "Current Location"
                    } finally {
                        isGeocodingGps = false
                    }
                }
            } else {
                isLoadingGps = false
            }
        } catch (_: SecurityException) {
            isLoadingGps = false
        }
    }

    // Open MapLocationPickerModal when requested
    if (showMapPicker) {
        MapLocationPickerModal(
            initialLat = if (gpsLat != 0.0) gpsLat else -0.0076,
            initialLng = if (gpsLng != 0.0) gpsLng else 37.6534,
            onLocationConfirmed = { lat, lng, label ->
                onLocationSelected(lat, lng, label)
                showMapPicker = false
                onDismiss()
            },
            onDismiss = { showMapPicker = false },
        )
        return
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
        ) {
            // Sheet title
            Text(
                text = "Share Location",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Spacer(Modifier.height(8.dp))

            // Section 1: Current GPS location
            CurrentLocationCard(
                isLoadingGps = isLoadingGps,
                isGeocodingGps = isGeocodingGps,
                gpsAreaName = gpsAreaName.ifBlank { "Current Location" },
                gpsAddress = gpsAddress,
                gpsAccuracy = gpsAccuracy,
                onSend = {
                    if (gpsLat != 0.0 || gpsLng != 0.0) {
                        onLocationSelected(gpsLat, gpsLng, gpsAreaName.ifBlank { "Current Location" })
                        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                    }
                },
            )

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            )
            Spacer(Modifier.height(12.dp))

            // Section 2: Campus landmark presets
            Text(
                text = "Campus Landmarks",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )
            Spacer(Modifier.height(6.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(MUST_LANDMARKS) { landmark ->
                    LandmarkChip(
                        label = landmark.label,
                        onClick = {
                            onLocationSelected(landmark.lat, landmark.lng, landmark.label)
                            scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                        },
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            )
            Spacer(Modifier.height(4.dp))

            // Section 3: Open map picker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showMapPicker = true }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(12.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Choose on Map",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Drop a pin anywhere on the map",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// Sub-composables

@Composable
private fun CurrentLocationCard(
    isLoadingGps: Boolean,
    isGeocodingGps: Boolean,
    gpsAreaName: String,
    gpsAddress: String,
    gpsAccuracy: Float?,
    onSend: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(22.dp),
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            if (isLoadingGps) {
                Text(
                    text = "Getting your location…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                LinearWavyProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(3.dp),
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = gpsAreaName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (isGeocodingGps) {
                        Spacer(Modifier.width(6.dp))
                        LinearWavyProgressIndicator(
                            modifier = Modifier
                                .width(32.dp)
                                .height(3.dp),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Accuracy dot
                    if (gpsAccuracy != null) {
                        AccuracyDot(accuracy = gpsAccuracy)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Accurate to ${gpsAccuracy.toInt()}m",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (gpsAddress.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = gpsAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        // Send button
        Button(
            onClick = onSend,
            enabled = !isLoadingGps,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(
                horizontal = 16.dp,
                vertical = 8.dp,
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text("Send", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

/**
 * Animated green dot that pulses to indicate live GPS lock.
 * Turns amber when accuracy is poor (> 50m).
 */
@Composable
private fun AccuracyDot(accuracy: Float) {
    val dotColor = if (accuracy <= 50f) Color(0xFF34C759) else Color(0xFFFF9500)
    val infiniteTransition = rememberInfiniteTransition(label = "gpsDot")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "gpsDotAlpha",
    )
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(dotColor.copy(alpha = alpha), CircleShape),
    )
}

@Composable
private fun LandmarkChip(label: String, onClick: () -> Unit) {
    SuggestionChip(
        onClick = onClick,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(4.dp))
                Text(label, style = MaterialTheme.typography.labelMedium)
            }
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            labelColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = SuggestionChipDefaults.suggestionChipBorder(
            enabled = true,
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
        ),
    )
}
