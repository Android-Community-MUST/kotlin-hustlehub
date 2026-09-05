package must.kdroiders.hustlehub.ui.features.profile.presentation.view

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleButtonVariant
import must.kdroiders.hustlehub.sharedComposables.HustlePullToRefreshBox
import must.kdroiders.hustlehub.sharedComposables.HustleScaffold
import must.kdroiders.hustlehub.ui.features.profile.domain.model.UserRole
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ErrorState
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.LoadingState
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileAvatar
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileBadges
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileBottomTabs
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileHeader
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileInfo
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileStatsRow
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProviderOnboardingCard
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ServiceCard
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ServicesHeader
import must.kdroiders.hustlehub.ui.features.profile.presentation.viewmodel.ProfileUiState
import must.kdroiders.hustlehub.ui.features.profile.presentation.viewmodel.ProfileViewModel
import must.kdroiders.hustlehub.ui.theme.LocalDimensions

/**
 * Alias for ProfileScreen to satisfy MyProfileScreen naming convention.
 */
@Composable
fun MyProfileScreen(
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
    ProfileScreen(
        profileViewModel = profileViewModel,
        onEditClick = onEditClick,
        onAddNewServiceClick = onAddNewServiceClick,
        onServiceClick = onServiceClick,
        onNavigateToMyServices = onNavigateToMyServices,
        onSettingsClick = onSettingsClick,
        onNavigateToSubscription = onNavigateToSubscription,
        onNavigateToAnalytics = onNavigateToAnalytics,
        onNavigateToAdminDashboard = onNavigateToAdminDashboard,
    )
}

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

    HustleScaffold(
        topBar = {
            ProfileHeader(
                onSettingsClick = onSettingsClick,
                onShareClick = {
                    val userId = state.user?.id ?: ""
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNewServiceClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add_new_service))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                state.isLoading && !state.isRefreshing -> LoadingState()
                state.error != null -> ErrorState(
                    message = state.error ?: "Unknown error",
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

// Main content — LazyColumn for performance

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
    val isProvider = user.role == UserRole.PROVIDER || user.role == UserRole.BOTH || state.services.isNotEmpty()
    val isAdmin = must.kdroiders.hustlehub.core.auth.AdminAuthUtils.isAuthorizedAdmin(user.email, user.role.name)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 16.dp,
            bottom = 80.dp, // FAB clearance
        ),
    ) {
        item(key = "avatar") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ProfileAvatar(
                    photoUrl = user.profilePhotoUrl,
                    isVerified = user.isVerified,
                )
                Spacer(Modifier.height(12.dp))
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
                Spacer(Modifier.height(16.dp))
                HustleButton(
                    text = stringResource(R.string.profile_edit_button),
                    variant = HustleButtonVariant.Secondary,
                    onClick = onEditClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding),
                )
                if (isAdmin) {
                    Spacer(Modifier.height(10.dp))
                    HustleButton(
                        text = "🛡️ Admin Center",
                        variant = HustleButtonVariant.Primary,
                        onClick = onNavigateToAdminDashboard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding),
                    )
                }
            }
        }

        item(key = "stats") {
            Spacer(Modifier.height(20.dp))
            ProfileStatsRow(
                hustleScore = state.hustleScore,
                serviceCount = state.services.size,
                reviewCount = state.reviewCount,
                onReviewsClick = onNavigateToMyServices,
                modifier = Modifier.padding(horizontal = horizontalPadding),
            )
        }

        item(key = "badges") {
            Spacer(Modifier.height(16.dp))
            ProfileBadges(
                badges = state.badges,
                modifier = Modifier.padding(horizontal = horizontalPadding),
            )
        }

        item(key = "services_header") {
            Spacer(Modifier.height(24.dp))
            ServicesHeader(
                onAddNewServiceClick = onAddNewServiceClick,
                onManageServicesClick = onNavigateToMyServices,
                modifier = Modifier.padding(horizontal = horizontalPadding),
            )
            Spacer(Modifier.height(12.dp))
        }

        item(key = "provider_onboarding") {
            AnimatedVisibility(
                visible = state.services.isEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                ProviderOnboardingCard(
                    onCreateServiceClick = onAddNewServiceClick,
                    modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 4.dp),
                )
            }
        }

        items(
            items = state.services,
            key = { it.id },
        ) { service ->
            ServiceCard(
                service = service,
                onClick = { onServiceClick(service.id) },
                onToggle = {
                    onToggleService(service.id)
                },
                modifier = Modifier.padding(
                    horizontal = horizontalPadding,
                    vertical = 6.dp,
                ),
            )
        }

        item(key = "bottom_tabs") {
            Spacer(Modifier.height(20.dp))
            ProfileBottomTabs(
                modifier = Modifier.padding(horizontal = horizontalPadding),
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
