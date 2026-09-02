package must.kdroiders.hustlehub.ui.features.profile.presentation.view

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.WorkOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import must.kdroiders.hustlehub.sharedComposables.ErrorView
import must.kdroiders.hustlehub.sharedComposables.HustleBackButton
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleButtonVariant
import must.kdroiders.hustlehub.sharedComposables.HustleScaffold
import must.kdroiders.hustlehub.sharedComposables.LoadingIndicator
import must.kdroiders.hustlehub.sharedComposables.SectionHeader
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileAvatar
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileBadges
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileInfo
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileStatsRow
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ServiceCard
import must.kdroiders.hustlehub.ui.features.profile.presentation.viewmodel.ProviderProfileViewModel
import must.kdroiders.hustlehub.ui.features.report.presentation.ReportDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderProfileScreen(
    providerId: String,
    providerProfileViewModel: ProviderProfileViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onNavigateToChat: (providerId: String) -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToMyServices: () -> Unit = {},
    onNavigateToServiceDetail: (serviceId: String) -> Unit = {},
) {
    val state by providerProfileViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(providerId) {
        providerProfileViewModel.initialize(providerId)
    }

    HustleScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.provider?.name ?: "Provider Profile",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    HustleBackButton(onClick = onBack)
                },
                actions = {
                    if (!state.isOwnProfile && state.provider != null) {
                        var showMenu by remember { mutableStateOf(false) }
                        var showReportDialog by remember { mutableStateOf(false) }
                        var showBlockDialog by remember { mutableStateOf(false) }

                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More options")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Report Profile") },
                                    onClick = {
                                        showMenu = false
                                        showReportDialog = true
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Block User", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showMenu = false
                                        showBlockDialog = true
                                    },
                                )
                            }
                        }

                        if (showBlockDialog) {
                            AlertDialog(
                                onDismissRequest = { showBlockDialog = false },
                                title = { Text("Block ${state.provider?.name ?: "User"}?") },
                                text = { Text("They will no longer be able to message you, view your profile, or see your map pins.") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            showBlockDialog = false
                                            providerProfileViewModel.blockUser {
                                                Toast.makeText(context, "User blocked", Toast.LENGTH_SHORT).show()
                                                onBack()
                                            }
                                        },
                                    ) {
                                        Text("Block", color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showBlockDialog = false }) {
                                        Text("Cancel")
                                    }
                                },
                            )
                        }

                        if (showReportDialog) {
                            ReportDialog(
                                targetId = providerId,
                                targetType = "user",
                                onDismiss = { showReportDialog = false },
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            if (!state.isLoading && state.error == null) {
                Surface(shadowElevation = 8.dp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        if (state.isOwnProfile) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                HustleButton(
                                    text = "Edit Profile",
                                    variant = HustleButtonVariant.Secondary,
                                    onClick = onNavigateToEditProfile,
                                    modifier = Modifier.weight(1f),
                                )
                                HustleButton(
                                    text = "Manage Services",
                                    variant = HustleButtonVariant.Primary,
                                    onClick = onNavigateToMyServices,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        } else {
                            val provider = state.provider
                            val canCall = provider?.allowCalls == true && !provider.phone.isBlank()

                            if (canCall) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    HustleButton(
                                        text = "Call",
                                        variant = HustleButtonVariant.Secondary,
                                        icon = Icons.Default.Call,
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${provider.phone}"))
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.weight(1f),
                                    )
                                    HustleButton(
                                        text = "Message",
                                        variant = HustleButtonVariant.Primary,
                                        onClick = {
                                            onNavigateToChat(provider.id)
                                        },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            } else {
                                HustleButton(
                                    text = "Message",
                                    onClick = {
                                        state.provider?.id?.let { onNavigateToChat(it) }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
            }
        },
    ) { innerPadding ->

        when {
            state.isLoading -> LoadingIndicator(modifier = Modifier.padding(innerPadding).fillMaxSize())
            state.error != null -> ErrorView(
                message = state.error ?: "Unknown error",
                onRetry = providerProfileViewModel::retry,
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
            )
            else -> {
                val provider = state.provider ?: return@HustleScaffold
                val pullToRefreshState = rememberPullToRefreshState()

                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = providerProfileViewModel::refresh,
                    modifier = Modifier.fillMaxSize(),
                    state = pullToRefreshState,
                    indicator = {
                        PullToRefreshDefaults.Indicator(
                            modifier = Modifier.align(Alignment.TopCenter),
                            isRefreshing = state.isRefreshing,
                            state = pullToRefreshState,
                            color = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.surface,
                        )
                    },
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentPadding = PaddingValues(
                            top = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding() + 16.dp,
                        ),
                    ) {
                        item(key = "header_banner") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                MaterialTheme.colorScheme.background,
                                            ),
                                        ),
                                    ),
                            )
                        }

                        item(key = "profile_info") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = (-40).dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                ProfileAvatar(
                                    photoUrl = provider.profilePhotoUrl,
                                    isVerified = provider.isVerified,
                                )
                                Spacer(Modifier.height(8.dp))
                                ProfileInfo(
                                    name = provider.name,
                                    phone = provider.phone,
                                    campusLocation = provider.campusLocation,
                                    bio = provider.bio,
                                    isOnline = provider.isOnline,
                                    allowCalls = provider.allowCalls,
                                    isOwnProfile = state.isOwnProfile,
                                    isVerifiedPro = provider.isVerifiedPro,
                                )
                            }
                        }

                        item(key = "stats_row") {
                            ProfileStatsRow(
                                hustleScore = state.hustleScore,
                                serviceCount = state.services.size,
                                reviewCount = state.reviewCount,
                                modifier = Modifier
                                    .offset(y = (-30).dp)
                                    .padding(horizontal = 16.dp),
                            )
                        }

                        if (state.badges.isNotEmpty()) {
                            item(key = "badges") {
                                ProfileBadges(
                                    badges = state.badges,
                                    modifier = Modifier
                                        .offset(y = (-20).dp)
                                        .padding(horizontal = 16.dp),
                                )
                            }
                        }

                        item(key = "services_header") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = (-10).dp)
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                SectionHeader(title = "Services (${state.services.size})")
                            }
                            Spacer(Modifier.height(4.dp))
                        }

                        if (state.services.isEmpty()) {
                            item(key = "empty_services") {
                                must.kdroiders.hustlehub.sharedComposables.EmptyStateView(
                                    title = "No services listed yet",
                                    description = "This provider has not added any active services.",
                                    icon = Icons.Default.WorkOff,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                )
                            }
                        } else {
                            items(items = state.services, key = { it.id }) { service ->
                                ServiceCard(
                                    service = service,
                                    onClick = { onNavigateToServiceDetail(service.id) },
                                    onToggle = {},
                                    modifier = Modifier
                                        .offset(y = (-10).dp)
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
