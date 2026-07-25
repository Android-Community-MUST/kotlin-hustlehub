@file:OptIn(ExperimentalMaterial3Api::class)

package must.kdroiders.hustlehub.ui.features.service.presentation.view.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.sharedComposables.HustleButton

// Default area for the map picker — Nchiru / MUST campus
private val NCHIRU_LATLNG = LatLng(-0.0076, 37.6534)
private const val MAP_PICKER_ZOOM = 17f

// Represents the three map view modes available in the picker
private enum class PickerMapType(val label: String, val mapType: MapType) {
    NORMAL("Normal", MapType.NORMAL),
    SATELLITE("Satellite", MapType.SATELLITE),
    HYBRID("Hybrid", MapType.HYBRID),
}

@Composable
fun MapLocationPickerModal(
    initialLat: Double,
    initialLng: Double,
    onLocationConfirmed: (Double, Double) -> Unit,
    onDismiss: () -> Unit,
) {
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

    // Pinned location — null means no tap yet (center-drag mode only)
    var pinnedLatLng by remember { mutableStateOf<LatLng?>(null) }
    val markerState = rememberUpdatedMarkerState(position = startLatLng)

    // Current map type selection
    var pickerMapType by remember { mutableStateOf(PickerMapType.NORMAL) }

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

    // The coordinate shown in the confirmation card
    val confirmedLat: Double
    val confirmedLng: Double
    if (pinnedLatLng != null) {
        confirmedLat = pinnedLatLng!!.latitude
        confirmedLng = pinnedLatLng!!.longitude
    } else {
        confirmedLat = cameraPositionState.position.target.latitude
        confirmedLng = cameraPositionState.position.target.longitude
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null, // Removed drag handle — map needs all vertical space
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
                        text = "Pin Your Location",
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
                    // Cycle through map types
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
                        // Tap auto-pins the location and flies the camera there
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
                    // Show marker only when the user has tapped a point
                    if (pinnedLatLng != null) {
                        Marker(
                            state = markerState,
                            title = "Selected Location",
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
            }

            // Confirmation card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp,
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = if (pinnedLatLng != null) "Pinned location" else "Center of view",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = "${"%.5f".format(confirmedLat)}, ${"%.5f".format(confirmedLng)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (pinnedLatLng != null)
                            "Tap anywhere else to move pin"
                        else
                            "Drag the map, then tap to drop a pin",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )

                    Spacer(Modifier.height(8.dp))

                    HustleButton(
                        text = "Confirm Location",
                        onClick = { onLocationConfirmed(confirmedLat, confirmedLng) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
