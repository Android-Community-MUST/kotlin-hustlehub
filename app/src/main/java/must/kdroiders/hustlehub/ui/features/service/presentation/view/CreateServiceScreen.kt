@file:OptIn(ExperimentalMaterial3Api::class)

package must.kdroiders.hustlehub.ui.features.service.presentation.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleCard
import must.kdroiders.hustlehub.sharedComposables.HustleCardVariant
import must.kdroiders.hustlehub.sharedComposables.HustleTextField
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.AvailabilityChipSelector
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.CategoryDropdown
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.ErrorText
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.PortfolioSlots
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.SectionLabel
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.TagChip
import must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel.CreateServiceEvent
import must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel.CreateServiceViewModel
import must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel.LocationSelectionMode
import must.kdroiders.hustlehub.ui.theme.HustleActiveGreen

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CreateServiceScreen(
    serviceId: String? = null,
    viewModel: CreateServiceViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSuccess: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        uri?.let { viewModel.onPortfolioImageAdded(it) }
    }

    LaunchedEffect(serviceId) {
        if (serviceId != null) viewModel.loadForEdit(serviceId)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CreateServiceEvent.Success -> onSuccess()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Text(
                    text = if (state.isEditMode) "Edit Service" else "Create Service",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            ) {
                Spacer(Modifier.height(8.dp))

                // Portfolio section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SectionLabel(text = "Portfolio")
                    val totalImages = state.existingPortfolioUrls.size + state.portfolioUris.size
                    Text(
                        text = "Max 3 images • $totalImages/3",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(8.dp))
                PortfolioSlots(
                    existingUrls = state.existingPortfolioUrls,
                    newUris = state.portfolioUris,
                    onAddClick = {
                        imagePicker.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly,
                            ),
                        )
                    },
                    onRemoveExisting = viewModel::onPortfolioExistingImageRemoved,
                    onRemoveNew = viewModel::onPortfolioNewImageRemoved,
                )
                Spacer(Modifier.height(20.dp))

                // Service title
                SectionLabel(text = "Service Title", required = true)
                HustleTextField(
                    value = state.title,
                    onValueChange = viewModel::onTitleChange,
                    placeholder = "e.g. Professional Braiding Services",
                    isError = state.titleError != null,
                    errorText = state.titleError,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next,
                    ),
                )
                Spacer(Modifier.height(16.dp))

                // Category
                SectionLabel(text = "Category", required = true)
                CategoryDropdown(
                    selected = state.category,
                    onSelect = viewModel::onCategoryChange,
                    hasError = state.categoryError != null,
                )
                ErrorText(state.categoryError)
                Spacer(Modifier.height(16.dp))

                // Description
                SectionLabel(text = "Description")
                HustleTextField(
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChange,
                    placeholder = "Describe your service details, what you offer, and any prerequisites...",
                    isError = state.descriptionError != null,
                    errorText = state.descriptionError,
                    singleLine = false,
                    minLines = 4,
                    maxLines = 6,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next,
                    ),
                    suffix = {
                        Text(
                            text = "${state.description.length}/300",
                            fontSize = 11.sp,
                            color = if (state.description.length > 280) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    },
                )
                Spacer(Modifier.height(16.dp))

                // Price range
                SectionLabel(text = "Price Range (KES)", required = true)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HustleTextField(
                        value = state.minPrice,
                        onValueChange = viewModel::onMinPriceChange,
                        placeholder = "Min",
                        isError = state.priceError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    HustleTextField(
                        value = state.maxPrice,
                        onValueChange = viewModel::onMaxPriceChange,
                        placeholder = "Max",
                        isError = state.priceError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                }
                AnimatedVisibility(visible = state.priceError != null) {
                    Text(
                        text = state.priceError ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                    )
                }
                Spacer(Modifier.height(16.dp))

                // Tags
                SectionLabel(text = "Tags")
                Text(
                    text = "Max 5 tags • press Enter or + to add",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                if (state.tags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp),
                    ) {
                        state.tags.forEach { tag ->
                            TagChip(label = tag, onRemove = { viewModel.removeTag(tag) })
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HustleTextField(
                        value = state.tagInput,
                        onValueChange = viewModel::onTagInputChange,
                        placeholder = "e.g. braids",
                        isError = state.tagError != null,
                        errorText = state.tagError,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                viewModel.addTag()
                                focusManager.clearFocus()
                            },
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { viewModel.addTag() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add tag",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Open to barter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Open to Barter",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "Accept service exchanges instead of cash",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                    Switch(
                        checked = state.openToBarter,
                        onCheckedChange = viewModel::onOpenToBarterChange,
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = HustleActiveGreen,
                            checkedThumbColor = Color.White,
                            uncheckedTrackColor = MaterialTheme.colorScheme.outline,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
                Spacer(Modifier.height(16.dp))

                // Service Operating Location
                ServiceLocationCard(
                    locationMode = state.locationMode,
                    selectedLat = state.selectedLat,
                    selectedLng = state.selectedLng,
                    locationLabel = state.locationLabel,
                    onModeChange = viewModel::onLocationModeChange,
                    onPresetSelect = viewModel::onLocationPresetSelect,
                    onCustomLocationSelect = viewModel::onCustomLocationSelect,
                    onLabelChange = viewModel::onLocationLabelChange,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))

                // Current status
                SectionLabel(text = "Current Status")
                AvailabilityChipSelector(
                    current = state.availability,
                    onSelect = viewModel::onAvailabilityChange,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Global error
                AnimatedVisibility(
                    visible = state.error != null,
                    enter = fadeIn(animationSpec = effectsSpec),
                    exit = fadeOut(animationSpec = effectsSpec),
                ) {
                    state.error?.let {
                        Spacer(Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .padding(12.dp),
                        ) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                HustleButton(
                    text = if (state.isLoading) "Publishing…" else "Publish Service",
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.publish()
                    },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(32.dp))
            }
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularWavyProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun ServiceLocationCard(
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
                        text = if (selectedLat !=
                            null
                        ) {
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
            onLocationConfirmed = { lat, lng ->
                onCustomLocationSelect(lat, lng, "Custom Map Location (${"%.4f".format(lat)}, ${"%.4f".format(lng)})")
                showMapModal = false
            },
            onDismiss = { showMapModal = false },
        )
    }
}

@Composable
private fun MapLocationPickerModal(
    initialLat: Double,
    initialLng: Double,
    onLocationConfirmed: (Double, Double) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(initialLat, initialLng), 16f)
    }

    val mapNestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                return Offset(0f, available.y)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
        ) {
            Text(
                text = "Drag map to center location pin",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .nestedScroll(mapNestedScrollConnection),
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                )

                // Center pin overlay
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Target Pin",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.Center),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HustleButton(
                    text = "Confirm Location",
                    onClick = {
                        val target = cameraPositionState.position.target
                        onLocationConfirmed(target.latitude, target.longitude)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
