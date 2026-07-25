@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package must.kdroiders.hustlehub.ui.features.service.presentation.view.components

import android.location.Geocoder
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import java.util.Locale

// Default area for the map picker — Nchiru / MUST campus
private val NCHIRU_LATLNG = LatLng(-0.0076, 37.6534)
private const val MAP_PICKER_ZOOM = 17f

// Represents the three map view modes available in the picker
private enum class PickerMapType(val label: String, val mapType: MapType) {
    NORMAL("Normal", MapType.NORMAL),
    SATELLITE("Satellite", MapType.SATELLITE),
    HYBRID("Hybrid", MapType.HYBRID),
}

/**
 * Helper to extract a clean, concise area/locality name from full geocoded address.
 * E.g., "Nchiru Market, Meru-Maua Road, Nchiru, Meru, Kenya" → "Nchiru, Meru"
 */
fun extractAreaName(fullAddress: String): String {
    if (fullAddress.isBlank()) return "Selected Location"
    val parts = fullAddress.split(",").map { it.trim() }
    return when {
        parts.size >= 3 -> {
            val locality = parts.getOrNull(1)?.takeIf { !it.contains(Regex("\\d{5}")) } ?: parts.getOrNull(0)
            val city = parts.getOrNull(2)?.replace(Regex("\\d+"), "")?.trim()?.takeIf { it.isNotBlank() && it != "Kenya" }
            if (city != null && locality != null && city != locality) {
                "$locality, $city"
            } else {
                locality ?: parts[0]
            }
        }
        parts.size == 2 -> "${parts[0]}, ${parts[1]}"
        else -> parts.firstOrNull() ?: fullAddress
    }
}

@Composable
fun MapLocationPickerModal(
    initialLat: Double,
    initialLng: Double,
    onLocationConfirmed: (lat: Double, lng: Double, label: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Default to Nchiru if no previous coordinate is set
    val startLatLng = remember {
        if (initialLat == 0.0 && initialLng == 0.0) NCHIRU_LATLNG
        else LatLng(initialLat, initialLng)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startLatLng, MAP_PICKER_ZOOM)
    }

    // Pinned location — null means no tap yet (center-drag mode)
    var pinnedLatLng by remember { mutableStateOf<LatLng?>(null) }
    val markerState = rememberUpdatedMarkerState(position = startLatLng)

    // Current map type selection
    var pickerMapType by remember { mutableStateOf(PickerMapType.NORMAL) }

    // Geocoded address & loading state
    var geocodedAddress by remember { mutableStateOf("") }
    var isGeocoding by remember { mutableStateOf(false) }

    val mapProperties = remember(pickerMapType) {
        MapProperties(mapType = pickerMapType.mapType)
    }
    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = true,
            mapToolbarEnabled = false,
        )
    }

    // Swallow vertical scroll so the bottom sheet cannot close when panning the map
    val mapNestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset =
                Offset(0f, available.y)
        }
    }

    // The active coordinate shown in the confirmation card
    val confirmedLat: Double
    val confirmedLng: Double
    if (pinnedLatLng != null) {
        confirmedLat = pinnedLatLng!!.latitude
        confirmedLng = pinnedLatLng!!.longitude
    } else {
        confirmedLat = cameraPositionState.position.target.latitude
        confirmedLng = cameraPositionState.position.target.longitude
    }

    // Asynchronously reverse-geocode target coordinates (Bongesha pattern)
    LaunchedEffect(confirmedLat, confirmedLng) {
        isGeocoding = true
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(confirmedLat, confirmedLng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val lines = (0..addr.maxAddressLineIndex).mapNotNull { addr.getAddressLine(it) }
                    geocodedAddress = if (lines.isNotEmpty()) lines.joinToString(", ") else ""
                } else {
                    geocodedAddress = ""
                }
            } catch (e: Exception) {
                geocodedAddress = ""
            } finally {
                isGeocoding = false
            }
        }
    }

    val displayAreaName = remember(geocodedAddress, confirmedLat, confirmedLng) {
        if (geocodedAddress.isNotBlank()) {
            extractAreaName(geocodedAddress)
        } else {
            "Custom Map Location (${"%.4f".format(confirmedLat)}, ${"%.4f".format(confirmedLng)})"
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f),
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "Pin Operating Location",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Tap the map to drop a pin, or drag to center",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                // Map type toggle button (layers icon)
                IconButton(onClick = {
                    pickerMapType = PickerMapType.entries[
                        (pickerMapType.ordinal + 1) % PickerMapType.entries.size
                    ]
                }) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Toggle map type",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // Map type chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PickerMapType.entries.forEach { type ->
                    val isSelected = pickerMapType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { pickerMapType = type },
                        label = { Text(type.label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                }
            }

            // Map area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .nestedScroll(mapNestedScrollConnection),
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties,
                    uiSettings = mapUiSettings,
                    onMapClick = { latLng ->
                        pinnedLatLng = latLng
                        markerState.position = latLng
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLng(latLng),
                                durationMs = 300,
                            )
                        }
                    },
                ) {
                    if (pinnedLatLng != null) {
                        Marker(
                            state = markerState,
                            title = displayAreaName,
                        )
                    }
                }

                // Center crosshair — visible when no pin is dropped yet
                if (pinnedLatLng == null) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Drag target",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.Center),
                    )
                }

                // Floating "My Location" button (Bongesha pattern)
                IconButton(
                    onClick = {
                        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                        try {
                            fusedClient.lastLocation.addOnSuccessListener { location ->
                                location?.let {
                                    val userLatLng = LatLng(it.latitude, it.longitude)
                                    pinnedLatLng = userLatLng
                                    markerState.position = userLatLng
                                    scope.launch {
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(userLatLng, MAP_PICKER_ZOOM),
                                            durationMs = 300,
                                        )
                                    }
                                }
                            }
                        } catch (e: SecurityException) {
                            // Permission check error
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .shadow(4.dp, CircleShape)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .size(44.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "My Location",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // Confirmation card (Bongesha styled)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (isGeocoding) {
                        LinearWavyProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp),
                                )
                                .padding(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = displayAreaName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = if (geocodedAddress.isNotBlank()) geocodedAddress else "${"%.5f".format(confirmedLat)}, ${"%.5f".format(confirmedLng)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    HustleButton(
                        text = "Confirm Location",
                        onClick = {
                            onLocationConfirmed(confirmedLat, confirmedLng, displayAreaName)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
