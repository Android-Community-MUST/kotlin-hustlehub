package must.kdroiders.hustlehub.ui.features.service.presentation.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import must.kdroiders.hustlehub.data.model.ServiceCategory
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel.CreateServiceEvent
import must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel.CreateServiceUiState
import must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel.CreateServiceViewModel
import must.kdroiders.hustlehub.ui.theme.HustleActiveGreen
import must.kdroiders.hustlehub.ui.theme.HustleDarkBackground
import must.kdroiders.hustlehub.ui.theme.HustleDarkOutline
import must.kdroiders.hustlehub.ui.theme.HustleDarkSurface
import must.kdroiders.hustlehub.ui.theme.HustleDarkSurfaceVariant
import must.kdroiders.hustlehub.ui.theme.HustleError
import must.kdroiders.hustlehub.ui.theme.HustlePrimaryBlue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateServiceScreen(
    viewModel: CreateServiceViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

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
            .background(HustleDarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "Create Service",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                // Title
                SectionLabel(text = "Service Title", required = true)
                HustleOutlinedTextField(
                    value = state.title,
                    onValueChange = viewModel::onTitleChange,
                    placeholder = "e.g. Professional Braiding Services",
                    error = state.titleError,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    )
                )
                ErrorText(state.titleError)
                Spacer(Modifier.height(16.dp))

                // Category
                SectionLabel(text = "Category", required = true)
                CategoryDropdown(
                    selected = state.category,
                    onSelect = viewModel::onCategoryChange,
                    error = state.categoryError
                )
                ErrorText(state.categoryError)
                Spacer(Modifier.height(16.dp))

                // Description
                SectionLabel(text = "Description")
                HustleOutlinedTextField(
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChange,
                    placeholder = "What do you offer? (max 300 characters)",
                    error = state.descriptionError,
                    minLines = 3,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    suffix = {
                        Text(
                            text = "${state.description.length}/300",
                            fontSize = 11.sp,
                            color = if (state.description.length > 280)
                                HustleError else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                ErrorText(state.descriptionError)
                Spacer(Modifier.height(16.dp))

                // Price range
                SectionLabel(text = "Price Range (KES)", required = true)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HustleOutlinedTextField(
                        value = state.minPrice,
                        onValueChange = viewModel::onMinPriceChange,
                        placeholder = "Min",
                        error = if (state.priceError != null) "" else null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    HustleOutlinedTextField(
                        value = state.maxPrice,
                        onValueChange = viewModel::onMaxPriceChange,
                        placeholder = "Max",
                        error = if (state.priceError != null) "" else null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
                ErrorText(state.priceError)
                Spacer(Modifier.height(16.dp))

                // Tags
                SectionLabel(text = "Tags")
                Text(
                    text = "Max 5 tags • press Enter or + to add",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                // Tag chips
                if (state.tags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        state.tags.forEach { tag ->
                            TagChip(
                                label = tag,
                                onRemove = { viewModel.removeTag(tag) }
                            )
                        }
                    }
                }
                // Tag input row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HustleOutlinedTextField(
                        value = state.tagInput,
                        onValueChange = viewModel::onTagInputChange,
                        placeholder = "e.g. braids",
                        error = if (state.tagError != null) "" else null,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                viewModel.addTag()
                                focusManager.clearFocus()
                            }
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(HustlePrimaryBlue)
                            .clickable { viewModel.addTag() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add tag",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                ErrorText(state.tagError)
                Spacer(Modifier.height(16.dp))

                // Open to Barter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(HustleDarkSurface)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Open to Barter",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Accept service exchanges instead of cash",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.openToBarter,
                        onCheckedChange = viewModel::onOpenToBarterChange,
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = HustleActiveGreen,
                            checkedThumbColor = Color.White
                        )
                    )
                }

                // Global error
                AnimatedVisibility(
                    visible = state.error != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    state.error?.let {
                        Spacer(Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(HustleError.copy(alpha = 0.12f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = HustleError
                            )
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                // Publish button
                HustleButton(
                    text = if (state.isLoading) "Publishing…" else "Publish Service",
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.publish()
                    },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(32.dp))
            }
        }

        // Full-screen loading overlay
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = HustlePrimaryBlue)
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, required: Boolean = false) {
    Row(modifier = Modifier.padding(bottom = 6.dp)) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (required) {
            Text(
                text = " *",
                style = MaterialTheme.typography.labelLarge,
                color = HustleError
            )
        }
    }
}

@Composable
private fun HustleOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    minLines: Int = 1,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    suffix: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        isError = error != null,
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        suffix = suffix,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = HustlePrimaryBlue,
            unfocusedBorderColor = HustleDarkOutline,
            focusedContainerColor = HustleDarkSurface,
            unfocusedContainerColor = HustleDarkSurface,
            errorBorderColor = HustleError,
            errorContainerColor = HustleDarkSurface,
            cursorColor = HustlePrimaryBlue
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun ErrorText(error: String?) {
    AnimatedVisibility(
        visible = !error.isNullOrEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        error?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = HustleError,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun CategoryDropdown(
    selected: ServiceCategory?,
    onSelect: (ServiceCategory) -> Unit,
    error: String?
) {
    var expanded by remember { mutableStateOf(false) }
    val categories = ServiceCategory.entries.filter { it != ServiceCategory.ALL }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(HustleDarkSurface)
                .border(
                    width = 1.dp,
                    color = if (error != null) HustleError else HustleDarkOutline,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selected?.label ?: "Select category",
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected != null)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(HustleDarkSurfaceVariant)
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = category.label,
                            color = if (category == selected)
                                HustlePrimaryBlue
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onSelect(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun TagChip(label: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(HustlePrimaryBlue.copy(alpha = 0.2f), HustlePrimaryBlue.copy(alpha = 0.1f))
                )
            )
            .border(1.dp, HustlePrimaryBlue.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = HustlePrimaryBlue,
            fontWeight = FontWeight.Medium
        )
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remove tag",
            tint = HustlePrimaryBlue.copy(alpha = 0.7f),
            modifier = Modifier
                .size(14.dp)
                .clickable { onRemove() }
        )
    }
}
