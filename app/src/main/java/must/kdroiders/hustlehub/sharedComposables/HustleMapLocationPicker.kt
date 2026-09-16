@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package must.kdroiders.hustlehub.sharedComposables

import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import must.kdroiders.hustlehub.R
import java.util.Locale

private val NCHIRU_LATLNG = LatLng(-0.0076, 37.6534)
private const val MAP_PICKER_ZOOM = 17f

private enum class PickerMapType(val label: String, val mapType: MapType) {
    NORMAL("Normal", MapType.NORMAL),
    SATELLITE("Satellite", MapType.SATELLITE),
    HYBRID("Hybrid", MapType.HYBRID),
}

/** Extracts a concise locality name from a full geocoded address string. */
fun extractAreaName(fullAddress: String): String {
    if (fullAddress.isBlank()) return "Selected Location"
    val parts = fullAddress.split(",").map { it.trim() }
    return when {
        parts.size >= 3 -> {
            val locality = parts.getOrNull(1)?.takeIf { !it.contains(Regex("\\d{5}")) } ?: parts.getOrNull(0)
            val city = parts
                .getOrNull(2)
                ?.replace(Regex("\\d+"), "")
                ?.trim()
                ?.takeIf { it.isNotBlank() && it != "Kenya" }
            if (city != null && locality != null && city != locality) "$locality, $city"
            else locality ?: parts[0]
        }
        parts.size == 2 -> "${parts[0]}, ${parts[1]}"
        else -> parts.firstOrNull() ?: fullAddress
    }
}

/**
 * Full-screen map dialog for dropping a pin and confirming a location.
 * Reusable across chat, service creation, and any other feature that needs location picking.
 */
@Composable
fun HustleMapLocationPicker(
    initialLat: Double,
    initialLng: Double,
    onLocationConfirmed: (lat: Double, lng: Double, label: String) -> Unit,
    onDismiss: () -> Unit,
    title: String = stringResource(R.string.map_pin_operating_location),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val startLatLng = remember {
        if (initialLat == 0.0 && initialLng == 0.0) NCHIRU_LATLNG
        else LatLng(initialLat, initialLng)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startLatLng, MAP_PICKER_ZOOM)
    }

    var pinnedLatLng by remember { mutableStateOf<LatLng?>(null) }
    val markerState = rememberUpdatedMarkerState(position = startLatLng)
    var pickerMapType by remember { mutableStateOf(PickerMapType.NORMAL) }
    var geocodedAddress by remember { mutableStateOf("") }
    var isGeocoding by remember { mutableStateOf(false) }

    val mapProperties = remember(pickerMapType) { MapProperties(mapType = pickerMapType.mapType) }
    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = true,
            mapToolbarEnabled = false,
        )
    }

    val confirmedLat = pinnedLatLng?.latitude ?: cameraPositionState.position.target.latitude
    val confirmedLng = pinnedLatLng?.longitude ?: cameraPositionState.position.target.longitude

    var lastGeocodedLat by remember { mutableStateOf(0.0) }
    var lastGeocodedLng by remember { mutableStateOf(0.0) }

    LaunchedEffect(pinnedLatLng, cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving) return@LaunchedEffect

        val targetLat = pinnedLatLng?.latitude ?: cameraPositionState.position.target.latitude
        val targetLng = pinnedLatLng?.longitude ?: cameraPositionState.position.target.longitude

        val dLat = kotlin.math.abs(targetLat - lastGeocodedLat)
        val dLng = kotlin.math.abs(targetLng - lastGeocodedLng)
        if (dLat < 0.0002 && dLng < 0.0002 && geocodedAddress.isNotBlank()) return@LaunchedEffect

        delay(700L)
        if (!Geocoder.isPresent()) return@LaunchedEffect

        isGeocoding = true
        withContext(Dispatchers.IO) {
            try {
                withTimeoutOrNull(2500L) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(targetLat, targetLng, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]
                        val lines = (0..addr.maxAddressLineIndex).mapNotNull { addr.getAddressLine(it) }
                        geocodedAddress = if (lines.isNotEmpty()) lines.joinToString(", ") else ""
                        lastGeocodedLat = targetLat
                        lastGeocodedLng = targetLng
                    }
                }
            } catch (_: Exception) {
            } finally {
                isGeocoding = false
            }
        }
    }

    val customLocFormat = stringResource(R.string.map_custom_location_format)
    val displayAreaName = remember(geocodedAddress, confirmedLat, confirmedLng, customLocFormat) {
        if (geocodedAddress.isNotBlank()) extractAreaName(geocodedAddress)
        else String.format(customLocFormat, confirmedLat, confirmedLng)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.semantics { heading() },
                            )
                            Text(
                                text = stringResource(R.string.map_drop_pin_guide),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = {
                            pickerMapType = PickerMapType.entries[
                                (pickerMapType.ordinal + 1) % PickerMapType.entries.size,
                            ]
                        }) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = stringResource(R.string.map_cd_toggle_type),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.cd_close),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        PickerMapType.entries.forEach { type ->
                            FilterChip(
                                selected = pickerMapType == type,
                                onClick = { pickerMapType = type },
                                label = { Text(type.label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ),
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
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
                                Marker(state = markerState, title = displayAreaName)
                            }
                        }

                        val findCurrentLocCd = stringResource(R.string.map_cd_find_current_loc)

                        if (pinnedLatLng == null) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = stringResource(R.string.map_cd_drag_target),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(40.dp)
                                    .align(Alignment.Center),
                            )
                        }

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
                                } catch (_: SecurityException) {}
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                                .shadow(4.dp, CircleShape)
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                .minimumInteractiveComponentSize()
                                .size(44.dp)
                                .semantics {
                                    role = Role.Button
                                    contentDescription = findCurrentLocCd
                                },
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = stringResource(R.string.map_cd_my_location),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

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
                                        text = if (geocodedAddress.isNotBlank()) {
                                            geocodedAddress
                                        } else {
                                            "${"%.5f".format(confirmedLat)}, ${"%.5f".format(confirmedLng)}"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }

                            Spacer(Modifier.height(4.dp))

                            HustleButton(
                                text = stringResource(R.string.action_confirm_location),
                                onClick = { onLocationConfirmed(confirmedLat, confirmedLng, displayAreaName) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}
