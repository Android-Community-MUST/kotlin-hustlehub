package must.kdroiders.hustlehub.ui.features.profile.presentation.view

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.core.auth.AdminAuthUtils
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleButtonVariant
import must.kdroiders.hustlehub.sharedComposables.HustlePullToRefreshBox
import must.kdroiders.hustlehub.sharedComposables.HustleScaffold
import must.kdroiders.hustlehub.ui.features.profile.domain.model.UserRole
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ErrorState
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.LoadingState
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileAvatar
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileBadges
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileBadgesEmptyState
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileBottomTabs
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileHeader
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileInfo
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileSegmentedTabs
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileStatsRow
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProviderOnboardingCard
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ServiceCard
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ServicesHeader
import must.kdroiders.hustlehub.ui.features.profile.presentation.viewmodel.ProfileUiState
import must.kdroiders.hustlehub.ui.features.profile.presentation.viewmodel.ProfileViewModel
import must.kdroiders.hustlehub.ui.theme.LocalDimensions

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onEditClick: () -> Unit = {},
    onAddNewServiceClick: () -> Unit = {},
    onServiceClick: (serviceId: String) -> Unit = {},
    onNavigateToMyServices: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    onNavigateToAnalytics: (tab: String) -> Unit = {},
    onNavigateToAdminDashboard: () -> Unit = {},
) {
    val state by profileViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val shareSubject = stringResource(R.string.profile_share_subject)
    val shareTextFormat = stringResource(R.string.profile_share_text_format)
    val shareChooserTitle = stringResource(R.string.profile_share_chooser_title)
    val defaultErrorMsg = stringResource(R.string.error_default_title)

    HustleScaffold(
        topBar = {
            ProfileHeader(
                onSettingsClick = onSettingsClick,
                onShareClick = {
                    val userId = state.user?.id.orEmpty()
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, shareSubject)
                        putExtra(
                            Intent.EXTRA_TEXT,
                            String.format(shareTextFormat, userId),
                        )
                    }
                    context.startActivity(Intent.createChooser(shareIntent, shareChooserTitle))
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
            when {
                state.isLoading && !state.isRefreshing -> LoadingState()
                state.error != null -> ErrorState(
                    message = state.error ?: defaultErrorMsg,
                    onRetry = profileViewModel::retry,
                )
                else -> {
                    HustlePullToRefreshBox(
                        isRefreshing = state.isRefreshing,
                        onRefresh = profileViewModel::loadProfile,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        ProfileContent(
                            state = state,
                            onEditClick = onEditClick,
                            onToggleService = profileViewModel::toggleServiceActive,
                            onToggleOverallAvailability = profileViewModel::toggleOverallAvailability,
                            onAddNewServiceClick = onAddNewServiceClick,
                            onServiceClick = onServiceClick,
                            onNavigateToMyServices = onNavigateToMyServices,
                            onSettingsClick = onSettingsClick,
                            onNavigateToSubscription = onNavigateToSubscription,
                            onNavigateToAnalytics = onNavigateToAnalytics,
                            onNavigateToAdminDashboard = onNavigateToAdminDashboard,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(
    state: ProfileUiState,
    onEditClick: () -> Unit,
    onToggleService: (String) -> Unit,
    onToggleOverallAvailability: (Boolean) -> Unit = {},
    onAddNewServiceClick: () -> Unit,
    onServiceClick: (serviceId: String) -> Unit = {},
    onNavigateToMyServices: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    onNavigateToAnalytics: (tab: String) -> Unit = {},
    onNavigateToAdminDashboard: () -> Unit = {},
) {
    val user = state.user ?: return
    val horizontalPadding = LocalDimensions.current.horizontalPadding
    val isProvider = user.role == UserRole.ROLE_PROVIDER || user.role == UserRole.ROLE_BOTH || state.services.isNotEmpty()
    val isAdmin = AdminAuthUtils
        .isAuthorizedAdmin(user.email, user.role.name)

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 8.dp,
            bottom = 16.dp,
        ),
    ) {
        item(key = "hero_profile") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ProfileAvatar(
                    photoUrl = user.profilePhotoUrl,
                    isVerified = user.isVerified,
                    avatarSize = 96.dp,
                    onEditPhotoClick = onEditClick,
                )
                Spacer(Modifier.height(10.dp))
                ProfileInfo(
                    name = user.name,
                    phone = user.phone,
                    campusLocation = user.campusLocation,
                    bio = user.bio,
                    isOnline = user.isOnline,
                    allowCalls = user.allowCalls,
                    isOwnProfile = true,
                    isProvider = isProvider,
                    isVerifiedPro = user.isVerifiedPro,
                    onAvailabilityToggle = onToggleOverallAvailability,
                )
                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    HustleButton(
                        text = stringResource(R.string.profile_edit_button),
                        icon = Icons.Default.Edit,
                        variant = HustleButtonVariant.Secondary,
                        onClick = onEditClick,
                        modifier = Modifier.weight(1f),
                    )
                    HustleButton(
                        text = stringResource(R.string.action_add_service),
                        icon = Icons.Default.Add,
                        variant = HustleButtonVariant.Primary,
                        onClick = onAddNewServiceClick,
                        modifier = Modifier.weight(1f),
                    )
                }

                if (isAdmin) {
                    Spacer(Modifier.height(10.dp))
                    HustleButton(
                        text = stringResource(R.string.admin_center_title),
                        icon = Icons.Default.AdminPanelSettings,
                        variant = HustleButtonVariant.Primary,
                        onClick = onNavigateToAdminDashboard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding),
                    )
                }
            }
        }

        item(key = "stats_dashboard") {
            Spacer(Modifier.height(16.dp))
            ProfileStatsRow(
                hustleScore = state.hustleScore,
                serviceCount = state.services.size,
                reviewCount = state.reviewCount,
                onReviewsClick = onNavigateToMyServices,
                onServicesClick = { selectedTabIndex = 0 },
                modifier = Modifier.padding(horizontal = horizontalPadding),
            )
        }

        item(key = "segmented_tabs") {
            Spacer(Modifier.height(20.dp))
            ProfileSegmentedTabs(
                selectedIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it },
                serviceCount = state.services.size,
                badgeCount = state.badges.size,
                modifier = Modifier.padding(horizontal = horizontalPadding),
            )
            Spacer(Modifier.height(12.dp))
        }

        when (selectedTabIndex) {
            0 -> {
                if (state.services.isEmpty()) {
                    item(key = "provider_onboarding") {
                        ProviderOnboardingCard(
                            onCreateServiceClick = onAddNewServiceClick,
                            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 4.dp),
                        )
                    }
                } else {
                    item(key = "services_header") {
                        ServicesHeader(
                            onAddNewServiceClick = onAddNewServiceClick,
                            onManageServicesClick = onNavigateToMyServices,
                            modifier = Modifier.padding(horizontal = horizontalPadding),
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    items(
                        items = state.services,
                        key = { it.id },
                    ) { service ->
                        ServiceCard(
                            service = service,
                            onClick = { onServiceClick(service.id) },
                            onToggle = { onToggleService(service.id) },
                            modifier = Modifier.padding(
                                horizontal = horizontalPadding,
                                vertical = 6.dp,
                            ),
                        )
                    }
                }
            }
            1 -> {
                if (state.badges.isEmpty()) {
                    item(key = "badges_empty") {
                        ProfileBadgesEmptyState(
                            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 8.dp),
                        )
                    }
                } else {
                    item(key = "badges_list") {
                        ProfileBadges(
                            badges = state.badges,
                            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 8.dp),
                        )
                    }
                }
            }
            2 -> {
                item(key = "insights_tab") {
                    ProfileBottomTabs(
                        modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 8.dp),
                        onAnalyticsClick = {
                            if (user.isVerifiedPro) {
                                onNavigateToAnalytics("OVERVIEW")
                            } else {
                                onNavigateToSubscription()
                            }
                        },
                        onEarningsClick = {
                            if (user.isVerifiedPro) {
                                onNavigateToAnalytics("PAYMENTS")
                            } else {
                                onNavigateToSubscription()
                            }
                        },
                    )
                }
            }
        }
    }
}
