package must.kdroiders.hustlehub.ui.features.service.presentation.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleTextField
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.AvailabilityChipSelector
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.CategoryDropdown
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.ErrorText
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.PortfolioSlots
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.SectionLabel
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.ServiceLocationCard
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.TagChip
import must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel.CreateServiceEvent
import must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel.CreateServiceViewModel
import must.kdroiders.hustlehub.ui.theme.HustleActiveGreen

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CreateServiceScreen(
    serviceId: String? = null,
    createServiceViewModel: CreateServiceViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSuccess: () -> Unit,
) {
    val state by createServiceViewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        uri?.let { createServiceViewModel.onPortfolioImageAdded(it) }
    }

    LaunchedEffect(serviceId) {
        if (serviceId != null) createServiceViewModel.loadForEdit(serviceId)
    }

    LaunchedEffect(Unit) {
        createServiceViewModel.events.collect { event ->
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
                        contentDescription = "Navigate back",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Text(
                    text = if (state.isEditMode) "Edit Service" else "Create Service",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.semantics { heading()}
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
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly,
                            ),
                        )
                    },
                    onRemoveExisting = createServiceViewModel::onPortfolioExistingImageRemoved,
                    onRemoveNew = createServiceViewModel::onPortfolioNewImageRemoved,
                )
                Spacer(Modifier.height(20.dp))

                // Service title
                SectionLabel(text = "Service Title", required = true)
                HustleTextField(
                    value = state.title,
                    onValueChange = createServiceViewModel::onTitleChange,
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
                    onSelect = createServiceViewModel::onCategoryChange,
                    hasError = state.categoryError != null,
                )
                ErrorText(state.categoryError)
                Spacer(Modifier.height(16.dp))

                // Description
                SectionLabel(text = "Description")
                HustleTextField(
                    value = state.description,
                    onValueChange = createServiceViewModel::onDescriptionChange,
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
                        onValueChange = createServiceViewModel::onMinPriceChange,
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
                        onValueChange = createServiceViewModel::onMaxPriceChange,
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
                            TagChip(label = tag, onRemove = { createServiceViewModel.removeTag(tag) })
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HustleTextField(
                        value = state.tagInput,
                        onValueChange = createServiceViewModel::onTagInputChange,
                        placeholder = "e.g. braids",
                        isError = state.tagError != null,
                        errorText = state.tagError,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                createServiceViewModel.addTag()
                                focusManager.clearFocus()
                            },
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .semantics {
                                role = Role.Button
                                contentDescription = "Add tag"
                            }
                            .clickable { createServiceViewModel.addTag() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
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
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .semantics {
                            role = Role.Switch
                            stateDescription = if (state.openToBarter) "On" else "Off"
                            contentDescription = "Open to Barter Service"
                        },
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
                        onCheckedChange = createServiceViewModel::onOpenToBarterChange,
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
                    onModeChange = createServiceViewModel::onLocationModeChange,
                    onPresetSelect = createServiceViewModel::onLocationPresetSelect,
                    onCustomLocationSelect = createServiceViewModel::onCustomLocationSelect,
                    onLabelChange = createServiceViewModel::onLocationLabelChange,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))

                // Current status
                SectionLabel(text = "Current Status")
                AvailabilityChipSelector(
                    current = state.availability,
                    onSelect = createServiceViewModel::onAvailabilityChange,
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
                        createServiceViewModel.publish()
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
