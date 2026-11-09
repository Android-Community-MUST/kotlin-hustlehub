package must.kdroiders.hustlehub.ui.features.miniapp.presentation.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import must.kdroiders.hustlehub.sharedComposables.HustleBackButton
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleButtonVariant
import must.kdroiders.hustlehub.sharedComposables.HustlePullToRefreshBox
import must.kdroiders.hustlehub.sharedComposables.HustleScaffold
import must.kdroiders.hustlehub.ui.features.miniapp.data.remote.dto.MiniAppRemoteDto
import must.kdroiders.hustlehub.ui.features.miniapp.presentation.viewmodel.MyMiniAppsViewModel
import must.kdroiders.hustlehub.ui.theme.CategoryNeonAmber
import must.kdroiders.hustlehub.ui.theme.CategoryNeonGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyMiniAppsScreen(
    onBack: () -> Unit,
    onAddNewApp: () -> Unit,
    onEditApp: (appId: String) -> Unit,
    onPreviewApp: (appId: String, title: String, url: String) -> Unit,
    viewModel: MyMiniAppsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var appToDelete by remember { mutableStateOf<MiniAppRemoteDto?>(null) }

    LaunchedEffect(state.error) {
        state.error?.let { err ->
            snackbarHostState.showSnackbar(err)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    appToDelete?.let { app ->
        AlertDialog(
            onDismissRequest = { appToDelete = null },
            title = {
                Text(
                    text = "Delete Mini-App",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete \"${app.title}\"? It will be unlisted from the campus feed.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMiniApp(app.id)
                        appToDelete = null
                    },
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { appToDelete = null }) {
                    Text("Cancel")
                }
            },
        )
    }

    HustleScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Developer Hub",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    HustleBackButton(onClick = onBack)
                },
                actions = {
                    IconButton(onClick = onAddNewApp) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New App",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        HustlePullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.loadMyApps(isRefresh = true) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (state.apps.isEmpty() && !state.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.AddBusiness,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Mini-Apps Yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Turn your web app or project into a HustleHub mini-app. Reach students across campus with built-in M-Pesa payments.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    HustleButton(
                        text = "Submit Your First App",
                        onClick = onAddNewApp,
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.apps, key = { it.id }) { app ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(72.dp),
                                ) {
                                    if (!app.coverPhotoUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = app.coverPhotoUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.horizontalGradient(
                                                        listOf(
                                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                                        ),
                                                    ),
                                                ),
                                        )
                                    }

                                    val (statusBg, statusFg, statusText) = when (app.status) {
                                        "ACTIVE" -> Triple(
                                            CategoryNeonGreen.copy(alpha = 0.2f),
                                            CategoryNeonGreen,
                                            "Active",
                                        )
                                        "REJECTED" -> Triple(
                                            MaterialTheme.colorScheme.errorContainer,
                                            MaterialTheme.colorScheme.error,
                                            "Rejected",
                                        )
                                        else -> Triple(
                                            CategoryNeonAmber.copy(alpha = 0.2f),
                                            CategoryNeonAmber,
                                            "In Review",
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(10.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(statusBg)
                                            .padding(horizontal = 10.dp, vertical = 4.dp),
                                    ) {
                                        Text(
                                            text = statusText,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = statusFg,
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 14.dp)
                                        .offset(y = (-20).dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            if (!app.iconUrl.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = app.iconUrl,
                                                    contentDescription = app.title,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize(),
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Storefront,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.size(22.dp),
                                                )
                                            }
                                        }
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
                                        .offset(y = (-10).dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = app.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f),
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text(app.category) },
                                        )
                                    }

                                    Text(
                                        text = "ID: ${app.appId}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = app.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )

                                    if (app.status == "REJECTED" && !app.adminNotes.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp),
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Feedback: ${app.adminNotes}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer,
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        HustleButton(
                                            text = "Preview",
                                            icon = Icons.Default.Visibility,
                                            variant = HustleButtonVariant.Outlined,
                                            onClick = { onPreviewApp(app.appId, app.title, app.entryUrl) },
                                            modifier = Modifier.weight(1f),
                                        )

                                        HustleButton(
                                            text = "Edit",
                                            icon = Icons.Default.Edit,
                                            variant = HustleButtonVariant.Outlined,
                                            onClick = { onEditApp(app.appId) },
                                            modifier = Modifier.weight(1f),
                                        )

                                        HustleButton(
                                            text = "Delete",
                                            icon = Icons.Default.Delete,
                                            variant = HustleButtonVariant.Outlined,
                                            onClick = { appToDelete = app },
                                            loading = state.isDeletingId == app.id,
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
