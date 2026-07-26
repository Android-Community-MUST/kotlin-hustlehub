package must.kdroiders.hustlehub.ui.features.service.presentation.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.LocationServices
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleCard
import must.kdroiders.hustlehub.sharedComposables.HustleCardVariant
import must.kdroiders.hustlehub.sharedComposables.HustleTextField
import must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel.LocationSelectionMode

@Composable
fun ServiceLocationCard(
    locationMode: LocationSelectionMode,
    selectedLat: Double?,
    selectedLng: Double?,
    locationLabel: String,
    onModeChange: (LocationSelectionMode) -> Unit,
    onPresetSelect: (String, Double, Double) -> Unit,
    onCustomLocationSelect: (Double, Double, String) -> Unit,
    onLabelChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showMapModal by remember { mutableStateOf(false) }

    val presets = remember {
        listOf(
            Triple("MUST Main Campus (Nchiru)", -0.0076, 37.6534),
            Triple("Tuition & Admin Block", -0.0075, 37.6535),
            Triple("MUST Library", -0.0074, 37.6532),
            Triple("Engineering & Tech Block", -0.0073, 37.6538),
            Triple("Campus Hostels", -0.0080, 37.6530),
        )
    }

    HustleCard(
        modifier = modifier,
        variant = HustleCardVariant.Surface,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = "Service Operating Location",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Where will you provide this service when operating on campus?",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(12.dp))

            // Selection Mode Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = locationMode == LocationSelectionMode.CAMPUS_PRESET,
                    onClick = { onModeChange(LocationSelectionMode.CAMPUS_PRESET) },
                    label = { Text("Preset", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                )

                FilterChip(
                    selected = locationMode == LocationSelectionMode.CURRENT_GPS,
                    onClick = {
                        onModeChange(LocationSelectionMode.CURRENT_GPS)
                        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                        try {
                            fusedClient.lastLocation.addOnSuccessListener { loc ->
                                loc?.let {
                                    onCustomLocationSelect(it.latitude, it.longitude, "Current Device Location")
                                }
                            }
                        } catch (e: SecurityException) {
                            timber.log.Timber.e(e, "GPS permission error")
                        }
                    },
                    label = { Text("My GPS", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                )

                FilterChip(
                    selected = locationMode == LocationSelectionMode.MAP_PICKER,
                    onClick = {
                        onModeChange(LocationSelectionMode.MAP_PICKER)
                        showMapModal = true
                    },
                    label = { Text("Pick on Map", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                )
            }

            Spacer(Modifier.height(12.dp))

            // Details depending on mode
            when (locationMode) {
                LocationSelectionMode.CAMPUS_PRESET -> {
                    Text(
                        text = "Select Campus Landmark:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        for ((name, lat, lng) in presets) {
                            val isSelected = locationLabel == name
                            FilterChip(
                                selected = isSelected,
                                onClick = { onPresetSelect(name, lat, lng) },
                                label = { Text(name, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ),
                            )
                        }
                    }
                }
                LocationSelectionMode.CURRENT_GPS -> {
                    Text(
                        text = if (selectedLat != null) {
                            "Detected Coordinates: ${"%.4f".format(selectedLat)}, ${"%.4f".format(selectedLng)}"
                        } else {
                            "Detecting GPS location..."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                }
                LocationSelectionMode.MAP_PICKER -> {
                    HustleButton(
                        text = if (selectedLat != null) "Change Location on Map" else "Open Map Picker",
                        onClick = { showMapModal = true },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Location note / building name input
            HustleTextField(
                value = locationLabel,
                onValueChange = onLabelChange,
                placeholder = "Location note (e.g. Hostel 3, Room 12 or Block B)",
            )
        }
    }

    if (showMapModal) {
        MapLocationPickerModal(
            initialLat = selectedLat ?: -0.0076,
            initialLng = selectedLng ?: 37.6534,
            onLocationConfirmed = { lat, lng, addressLabel ->
                onCustomLocationSelect(lat, lng, addressLabel)
                showMapModal = false
            },
            onDismiss = { showMapModal = false },
        )
    }
}
