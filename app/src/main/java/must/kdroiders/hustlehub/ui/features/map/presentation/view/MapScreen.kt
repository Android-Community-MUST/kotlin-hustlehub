package must.kdroiders.hustlehub.ui.features.map.presentation.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.ui.features.map.domain.model.MapPin
import must.kdroiders.hustlehub.ui.features.map.presentation.viewmodel.MapViewModel
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory
import timber.log.Timber

private object MapDefaults {
    val MERU_UNIVERSITY = LatLng(0.0515, 37.6456)
    const val DEFAULT_ZOOM = 16f
    const val MIN_ZOOM = 12f
    const val MAX_ZOOM = 20f
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    // Map Settings State
    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    var isLocationPermissionGranted by remember {
        mutableStateOf(
            checkLocationPermission(context)
        )
    }

    // Selected Pin for the bottom details overlay
    var selectedPin by remember { mutableStateOf<MapPin?>(null) }

    // Camera State
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(MapDefaults.MERU_UNIVERSITY, MapDefaults.DEFAULT_ZOOM)
    }

    // Client to fetch user location for recentering
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Map Properties & UI settings
    val mapProperties = remember(mapType, isLocationPermissionGranted) {
        MapProperties(
            mapType = mapType,
            isMyLocationEnabled = isLocationPermissionGranted,
            mapStyleOptions = MapStyleOptions(MapTheme.DARK_JSON),
            minZoomPreference = MapDefaults.MIN_ZOOM,
            maxZoomPreference = MapDefaults.MAX_ZOOM,
        )
    }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = true,
            mapToolbarEnabled = false,
        )
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        isLocationPermissionGranted = granted
        if (!granted) {
            Toast.makeText(context, "Location permission is required to center on your position.", Toast.LENGTH_SHORT).show()
        }
    }

    // Check location permission on launch
    LaunchedEffect(Unit) {
        if (!isLocationPermissionGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Recenter camera and update location in ViewModel when permission is active
    LaunchedEffect(isLocationPermissionGranted) {
        if (isLocationPermissionGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val userLatLng = LatLng(it.latitude, it.longitude)
                        viewModel.updateUserLocation(userLatLng)
                        if (!uiState.isInitialCameraAnimationDone) {
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(
                                        userLatLng,
                                        MapDefaults.DEFAULT_ZOOM
                                    )
                                )
                                viewModel.setInitialCameraAnimationDone()
                            }
                        }
                    }
                }
            } catch (e: SecurityException) {
                Timber.e(e, "Security exception while fetching location")
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 1. Map Renderer
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = uiSettings,
            onMapClick = {
                selectedPin = null
            }
        ) {
            Clustering(
                items = uiState.pins,
                onClusterClick = { cluster ->
                    scope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                cluster.position,
                                cameraPositionState.position.zoom + 2f
                            )
                        )
                    }
                    true
                },
                onClusterItemClick = { pin ->
                    selectedPin = pin
                    true
                },
                clusterContent = { cluster ->
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF7C4DFF), shape = CircleShape)
                            .border(2.dp, Color.White, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cluster.size.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                },
                clusterItemContent = { pin ->
                    ProviderMarkerContent(category = pin.category)
                }
            )
        }

        // 2. Custom Floating Title Badge (Glassmorphic)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Column {
                    Text(
                        text = "Meru University",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                    )
                    Text(
                        text = "HustleHub Campus Map",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                    )
                }
            }
        }

        // 3. Category & Availability Filter Row (Horizontal Scrollable)
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(top = 84.dp, start = 16.dp, end = 16.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Availability Filter Chip
            val isAvailableOnly = uiState.availability == ServiceAvailability.AVAILABLE
            FilterChip(
                selected = isAvailableOnly,
                onClick = {
                    viewModel.selectAvailability(
                        if (isAvailableOnly) null else ServiceAvailability.AVAILABLE
                    )
                },
                label = { Text("Available Only", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                leadingIcon = {
                    if (isAvailableOnly) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            // Category Filter Chips
            ServiceCategory.entries.filter { it != ServiceCategory.ALL }.forEach { category ->
                val isSelected = uiState.selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        viewModel.selectCategory(if (isSelected) null else category)
                    },
                    label = { Text(category.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        // 4. Custom Zoom and Layers Panel (Glassmorphic, Top Right)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Map Type Toggle
            MapControlFloatingButton(
                icon = Icons.Default.Layers,
                contentDescription = "Toggle Map Type",
                onClick = {
                    mapType = if (mapType == MapType.NORMAL) MapType.SATELLITE else MapType.NORMAL
                }
            )

            // Zoom Controls group
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            cameraPositionState.animate(CameraUpdateFactory.zoomIn())
                        }
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(
                    modifier = Modifier
                        .width(44.dp)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                )
                IconButton(
                    onClick = {
                        scope.launch {
                            cameraPositionState.animate(CameraUpdateFactory.zoomOut())
                        }
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 5. Custom Recenter Action Panel (Bottom Right)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Recenter to MUST
            MapControlFloatingButton(
                icon = Icons.Default.School,
                contentDescription = "Center on Meru University",
                onClick = {
                    scope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                MapDefaults.MERU_UNIVERSITY,
                                MapDefaults.DEFAULT_ZOOM
                            )
                        )
                    }
                }
            )

            // Center on user location
            MapControlFloatingButton(
                icon = Icons.Default.MyLocation,
                contentDescription = "Center on My Location",
                onClick = {
                    if (isLocationPermissionGranted) {
                        scope.launch {
                            try {
                                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                    if (location != null) {
                                        scope.launch {
                                            cameraPositionState.animate(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    LatLng(location.latitude, location.longitude),
                                                    MapDefaults.DEFAULT_ZOOM
                                                )
                                            )
                                        }
                                    } else {
                                        Toast.makeText(context, "Fetching current location...", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: SecurityException) {
                                Timber.e(e, "Location access denied")
                            }
                        }
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }
            )
        }

        // 6. Premium Selected Provider Details Card Overlay (Bottom Center)
        AnimatedVisibility(
            visible = selectedPin != null,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 60.dp) // Avoid overlap with bottom navigation bar
        ) {
            selectedPin?.let { pin ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    ),
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Category Icon container
                        val (icon, color) = getCategoryIconAndColor(pin.category)
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(color.copy(alpha = 0.15f), CircleShape)
                                .border(1.5.dp, color, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = pin.category.name,
                                tint = color,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Details
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = pin.serviceTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = pin.providerName,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = pin.averageRating.toString(),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // Availability indicator dot
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            if (pin.availability == ServiceAvailability.AVAILABLE) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                            CircleShape
                                        )
                                )
                                Text(
                                    text = pin.availability.name,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Close button
                        IconButton(
                            onClick = { selectedPin = null },
                            modifier = Modifier.align(Alignment.Top)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 7. Permission Alert Card Overlay (Graceful Handling)
        AnimatedVisibility(
            visible = !isLocationPermissionGranted,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(16.dp)
                .padding(end = 76.dp) // Leave space for bottom right buttons
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ),
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Location Info",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Enable Location Services",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "To find nearby student service providers and view your position on campus, please grant location access.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                    Button(
                        onClick = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "Grant Access",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderMarkerContent(category: ServiceCategory) {
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

private fun getCategoryIconAndColor(category: ServiceCategory): Pair<androidx.compose.ui.graphics.vector.ImageVector, Color> {
    return when (category) {
        ServiceCategory.SALON -> Icons.Default.ContentCut to Color(0xFF9C27B0)
        ServiceCategory.LAUNDRY -> Icons.Default.LocalMall to Color(0xFF2196F3)
        ServiceCategory.TUTORING -> Icons.Default.School to Color(0xFF4CAF50)
        ServiceCategory.FOOD -> Icons.Default.Restaurant to Color(0xFFFF9800)
        ServiceCategory.TECH -> Icons.Default.Computer to Color(0xFF009688)
        ServiceCategory.PHOTOGRAPHY -> Icons.Default.PhotoCamera to Color(0xFFE91E63)
        else -> Icons.Default.Place to Color(0xFF757575)
    }
}

@Composable
private fun MapControlFloatingButton(
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

private fun checkLocationPermission(context: Context): Boolean {
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
