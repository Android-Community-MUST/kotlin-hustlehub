package must.kdroiders.hustlehub.ui.features.service.presentation.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.WorkOff
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import must.kdroiders.hustlehub.sharedComposables.HustlePullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import must.kdroiders.hustlehub.sharedComposables.EmptyStateView
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleScaffold
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.DeleteConfirmDialog
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.PortfolioSlots
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.ServiceManagementCard
import must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel.MyServicesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyServicesScreen(
    viewModel: MyServicesViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onCreateService: () -> Unit,
    onEditService: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        uri?.let { viewModel.onGalleryImageAdded(it) }
    }

    // Show errors as snackbar
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Refresh list when navigating back from the edit screen
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadServices()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Delete confirmation dialog
    val pendingDeleteId = state.pendingDeleteServiceId
    if (pendingDeleteId != null) {
        val serviceName = state.services
            .find { it.id == pendingDeleteId }
            ?.title ?: "this service"
        DeleteConfirmDialog(
            serviceName = serviceName,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete,
        )
    }

    // Gallery Bottom Sheet
    if (state.selectedServiceForGallery != null) {
        val selectedService = state.services.find { it.id == state.selectedServiceForGallery }
        if (selectedService != null) {
            ModalBottomSheet(
                onDismissRequest = viewModel::closeGallery,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = 32.dp), // extra padding for bottom navigation
                ) {
                    Text(
                        text = "Quick Gallery",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Manage photos for ${selectedService.title}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    PortfolioSlots(
                        existingUrls = state.existingPortfolioUrls,
                        newUris = state.portfolioUris,
                        onAddClick = {
                            imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onRemoveExisting = viewModel::onGalleryExistingImageRemoved,
                        onRemoveNew = viewModel::onGalleryNewImageRemoved,
                        maxSlots = 6,
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    if (state.isGallerySaving) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularWavyProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    } else {
                        HustleButton(
                            text = "Save Changes",
                            onClick = viewModel::saveGallery,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }

    HustleScaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Text(
                        text = "My Services",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateService,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add service")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            HustlePullToRefreshBox(
                isRefreshing = state.isLoading,
                onRefresh = viewModel::loadServices,
                modifier = Modifier.fillMaxSize(),
            ) {
                when {
                    state.isLoading && state.services.isEmpty() -> {
                        CircularWavyProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    state.services.isEmpty() -> {
                        EmptyServicesPlaceholder(
                            onCreateClick = onCreateService,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                horizontal = 16.dp,
                                vertical = 12.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(
                                items = state.services,
                                key = { it.id },
                            ) { service ->
                                ServiceManagementCard(
                                    service = service,
                                    isUpdating = state.updatingServiceId == service.id,
                                    onEditClick = { onEditService(service.id) },
                                    onGalleryClick = { viewModel.openGallery(service.id) },
                                    onDeleteClick = { viewModel.requestDelete(service.id) },
                                    onAvailabilityChange = { newAvailability ->
                                        viewModel.onAvailabilityChange(service.id, newAvailability)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyServicesPlaceholder(
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EmptyStateView(
        title = "No service listings",
        description = "List your first service to start earning on campus.",
        icon = Icons.Default.WorkOff,
        modifier = modifier,
        action = {
            HustleButton(
                text = "Add Service",
                onClick = onCreateClick,
            )
        },
    )
}
