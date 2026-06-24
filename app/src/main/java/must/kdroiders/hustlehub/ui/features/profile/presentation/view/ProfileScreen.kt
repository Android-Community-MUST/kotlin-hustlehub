package must.kdroiders.hustlehub.ui.features.profile.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import must.kdroiders.hustlehub.ui.features.profile.domain.model.UserRole
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleCard
import must.kdroiders.hustlehub.sharedComposables.HustleCardVariant
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ErrorState
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.LoadingState
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileAvatar
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileBadges
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileBottomTabs
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileHeader
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileInfo
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileStatsRow
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ServiceCard
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ServicesHeader
import must.kdroiders.hustlehub.ui.features.profile.presentation.viewmodel.ProfileUiState
import must.kdroiders.hustlehub.ui.features.profile.presentation.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onEditClick: () -> Unit = {},
    onAddNewServiceClick: () -> Unit = {},
    onServiceClick: (serviceId: String) -> Unit = {},
    onNavigateToMyServices: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    val state by profileViewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when {
            state.isLoading -> LoadingState()
            state.error != null -> ErrorState(
                message = state.error ?: "Unknown error",
                onRetry = profileViewModel::retry,
            )
            else -> ProfileContent(
                state = state,
                onEditClick = onEditClick,
                onToggleService = profileViewModel::toggleServiceActive,
                onAddNewServiceClick = onAddNewServiceClick,
                onServiceClick = onServiceClick,
                onNavigateToMyServices = onNavigateToMyServices,
                onSettingsClick = onSettingsClick,
            )
        }
    }
}

// Main content — LazyColumn for performance

@Composable
private fun ProfileContent(
    state: ProfileUiState,
    onEditClick: () -> Unit,
    onToggleService: (String) -> Unit,
    onAddNewServiceClick: () -> Unit,
    onServiceClick: (serviceId: String) -> Unit = {},
    onNavigateToMyServices: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    val user = state.user ?: return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            top = 16.dp,
            bottom = 100.dp,
        ),
    ) {
        // Header
        item(key = "header") {
            ProfileHeader(
                onEditClick = onEditClick,
                onSettingsClick = onSettingsClick,
            )
        }

        // Avatar + info
        item(key = "avatar") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ProfileAvatar(
                    photoUrl = user.profilePhotoUrl,
                )
                Spacer(Modifier.height(12.dp))
                ProfileInfo(
                    name = user.name,
                    phone = user.phone,
                    campusLocation = user.campusLocation,
                    bio = user.bio,
                )
            }
        }

        // Stats row
        item(key = "stats") {
            Spacer(Modifier.height(20.dp))
            ProfileStatsRow(
                hustleScore = state.hustleScore,
                serviceCount = state.services.size,
                reviewCount = state.reviewCount,
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                ),
            )
        }

        // Badges
        item(key = "badges") {
            Spacer(Modifier.height(16.dp))
            ProfileBadges(
                badges = state.badges,
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                ),
            )
        }

        if (user.role == UserRole.CUSTOMER) {
            item(key = "become_provider_banner") {
                Spacer(Modifier.height(16.dp))
                HustleCard(
                    variant = HustleCardVariant.Elevated,
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Earn Money on HustleHub!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Complete your provider profile to list your services, show your portfolio, and receive booking requests from fellow students.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(12.dp))
                        HustleButton(
                            text = "Become a Service Provider",
                            onClick = onEditClick,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }

        // Services header
        item(key = "services_header") {
            Spacer(Modifier.height(24.dp))
            ServicesHeader(
                onAddNewServiceClick = onAddNewServiceClick,
                onManageServicesClick = onNavigateToMyServices,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(12.dp))
        }

        // Service cards — each tappable to manage that specific service
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
                    horizontal = 16.dp,
                    vertical = 6.dp,
                ),
            )
        }

        // Bottom tabs
        item(key = "bottom_tabs") {
            Spacer(Modifier.height(20.dp))
            ProfileBottomTabs(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                ),
            )
        }
    }
}
