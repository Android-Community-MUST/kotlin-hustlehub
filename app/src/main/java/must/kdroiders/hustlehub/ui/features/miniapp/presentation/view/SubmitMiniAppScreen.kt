package must.kdroiders.hustlehub.ui.features.miniapp.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.MiscellaneousServices
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.sharedComposables.HustleBackButton
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleScaffold
import must.kdroiders.hustlehub.sharedComposables.HustleTextField
import must.kdroiders.hustlehub.ui.features.miniapp.presentation.viewmodel.SubmitMiniAppViewModel
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.SectionLabel
import must.kdroiders.hustlehub.ui.theme.CategoryNeonAmber
import must.kdroiders.hustlehub.ui.theme.CategoryNeonBlue
import must.kdroiders.hustlehub.ui.theme.CategoryNeonBrightCyan
import must.kdroiders.hustlehub.ui.theme.CategoryNeonDefault
import must.kdroiders.hustlehub.ui.theme.CategoryNeonLime
import must.kdroiders.hustlehub.ui.theme.CategoryNeonTeal
import must.kdroiders.hustlehub.ui.theme.CategoryNeonViolet

data class MiniAppCategoryOption(
    val id: String,
    val label: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
)

private val MINI_APP_CATEGORIES = listOf(
    MiniAppCategoryOption(
        id = "SERVICES",
        label = "Services & Utilities",
        description = "Laundry, errands, barbershop, academic assistance",
        icon = Icons.Outlined.MiscellaneousServices,
        color = CategoryNeonBrightCyan,
    ),
    MiniAppCategoryOption(
        id = "FOOD",
        label = "Food & Bakery",
        description = "Hostel canteens, snacks, fast food, bakery items",
        icon = Icons.Outlined.Restaurant,
        color = CategoryNeonAmber,
    ),
    MiniAppCategoryOption(
        id = "TECH",
        label = "Tech & Software",
        description = "Developer tools, print ordering, tech utilities",
        icon = Icons.Outlined.Computer,
        color = CategoryNeonTeal,
    ),
    MiniAppCategoryOption(
        id = "FASHION",
        label = "Fashion & Merch",
        description = "Campus thrift, footwear, streetwear, custom merch",
        icon = Icons.Outlined.Checkroom,
        color = CategoryNeonBlue,
    ),
    MiniAppCategoryOption(
        id = "DESIGN",
        label = "Design & Creative",
        description = "Graphic design, artwork, photography portfolios",
        icon = Icons.Outlined.Palette,
        color = CategoryNeonViolet,
    ),
    MiniAppCategoryOption(
        id = "ENTERTAINMENT",
        label = "Events & Entertainment",
        description = "Campus events, ticketing, gaming, music",
        icon = Icons.Outlined.Celebration,
        color = CategoryNeonLime,
    ),
    MiniAppCategoryOption(
        id = "OTHER",
        label = "Other Campus Apps",
        description = "General purpose student-built web mini-apps",
        icon = Icons.Outlined.Category,
        color = CategoryNeonDefault,
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitMiniAppScreen(
    onBack: () -> Unit,
    onSubmitSuccess: () -> Unit,
    editAppId: String? = null,
    viewModel: SubmitMiniAppViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCategorySheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    val selectedCategoryOption = remember(state.category) {
        MINI_APP_CATEGORIES.firstOrNull { it.id == state.category }
            ?: MINI_APP_CATEGORIES.first()
    }

    LaunchedEffect(editAppId) {
        if (!editAppId.isNullOrBlank()) {
            viewModel.loadForEdit(editAppId)
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { err ->
            snackbarHostState.showSnackbar(err)
            viewModel.clearError()
        }
    }

    if (state.isSuccess) {
        AlertDialog(
            onDismissRequest = onSubmitSuccess,
            title = {
                Text(
                    text = if (state.isEditMode) "Changes Saved!" else "Application Submitted!",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = if (state.isEditMode) {
                        "Your mini-app has been updated successfully."
                    } else {
                        "Your mini-app has been submitted to the HustleHub admin team. Once approved, it will go live dynamically on the campus feed."
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = onSubmitSuccess) {
                    Text("Done")
                }
            },
        )
    }

    if (showCategorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCategorySheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            var searchQuery by remember { mutableStateOf("") }
            val filtered = remember(searchQuery) {
                if (searchQuery.isBlank()) {
                    MINI_APP_CATEGORIES
                } else {
                    MINI_APP_CATEGORIES.filter {
                        it.label.contains(searchQuery, ignoreCase = true) ||
                            it.description.contains(searchQuery, ignoreCase = true)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Select Category",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                showCategorySheet = false
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search categories...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filtered, key = { it.id }) { option ->
                        val isSelected = option.id == state.category
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    } else {
                                        Color.Transparent
                                    },
                                ).clickable {
                                    viewModel.onCategoryChange(option.id)
                                    coroutineScope.launch {
                                        sheetState.hide()
                                        showCategorySheet = false
                                    }
                                }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(option.color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = option.icon,
                                    contentDescription = null,
                                    tint = option.color,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = option.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    HustleScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isEditMode) "Edit Mini-App" else "List Your App",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    HustleBackButton(onClick = onBack)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (state.isEditMode) Icons.Default.Edit else Icons.Default.AddBusiness,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (state.isEditMode) "Update Your Mini-App" else "HustleHub Mini-App Platform",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (state.isEditMode) {
                                "Note: Changing your website URL will require re-verification by campus admins."
                            } else {
                                "Deploy your website and let thousands of students access it instantly with seamless native M-Pesa payments."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Column {
                SectionLabel(text = "Mini-App Name", required = true)
                HustleTextField(
                    value = state.title,
                    onValueChange = viewModel::onTitleChange,
                    placeholder = "e.g. MUST Canteen Online",
                    isError = state.titleError != null,
                    errorText = state.titleError,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Column {
                SectionLabel(text = "Unique App ID (Slug)", required = true)
                HustleTextField(
                    value = state.appId,
                    onValueChange = viewModel::onAppIdChange,
                    placeholder = "e.g. must-canteen",
                    enabled = !state.isEditMode,
                    isError = state.appIdError != null,
                    errorText = state.appIdError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        capitalization = KeyboardCapitalization.None,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Column {
                SectionLabel(text = "Category", required = true)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(12.dp),
                        ).clickable { showCategorySheet = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(selectedCategoryOption.color.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = selectedCategoryOption.icon,
                                contentDescription = null,
                                tint = selectedCategoryOption.color,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = selectedCategoryOption.label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Expand category selection",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column {
                SectionLabel(text = "HTTPS Web URL", required = true)
                HustleTextField(
                    value = state.entryUrl,
                    onValueChange = viewModel::onEntryUrlChange,
                    placeholder = "https://your-website.com",
                    isError = state.entryUrlError != null,
                    errorText = state.entryUrlError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        capitalization = KeyboardCapitalization.None,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Column {
                SectionLabel(text = "Description", required = true)
                HustleTextField(
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChange,
                    placeholder = "Briefly describe your services, food menu, or products...",
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
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Column {
                SectionLabel(text = "Icon Image URL (Optional)")
                HustleTextField(
                    value = state.iconUrl,
                    onValueChange = viewModel::onIconUrlChange,
                    placeholder = "https://example.com/icon.png",
                    isError = state.iconUrlError != null,
                    errorText = state.iconUrlError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        capitalization = KeyboardCapitalization.None,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Column {
                SectionLabel(text = "Cover Banner URL (Optional)")
                HustleTextField(
                    value = state.coverPhotoUrl,
                    onValueChange = viewModel::onCoverPhotoUrlChange,
                    placeholder = "https://example.com/banner.jpg",
                    isError = state.coverPhotoUrlError != null,
                    errorText = state.coverPhotoUrlError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        capitalization = KeyboardCapitalization.None,
                        imeAction = ImeAction.Done,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            HustleButton(
                text = if (state.isEditMode) "Save Changes" else "Submit Application",
                onClick = viewModel::submit,
                loading = state.isLoading,
                enabled = state.isFormValid && !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
