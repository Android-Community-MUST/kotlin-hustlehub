package must.kdroiders.hustlehub.ui.features.profile.presentation.view

import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.*
import must.kdroiders.hustlehub.ui.features.profile.presentation.viewmodel.ProfileViewModel
import must.kdroiders.hustlehub.ui.features.profile.presentation.viewmodel.ProfileUiState
import must.kdroiders.hustlehub.ui.features.profile.presentation.viewmodel.Badge
import must.kdroiders.hustlehub.ui.features.profile.presentation.viewmodel.BadgeType
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import must.kdroiders.hustlehub.data.model.Service
import must.kdroiders.hustlehub.ui.theme.HustleActiveGreen
import must.kdroiders.hustlehub.ui.theme.HustleBadgeBlue
import must.kdroiders.hustlehub.ui.theme.HustleBadgeGold
import must.kdroiders.hustlehub.ui.theme.HustleBadgeGreen
import must.kdroiders.hustlehub.ui.theme.HustleDarkSurfaceBright
import must.kdroiders.hustlehub.ui.theme.HustleDarkSurfaceVariant
import must.kdroiders.hustlehub.ui.theme.HustleOfflineGray
import must.kdroiders.hustlehub.ui.theme.HustlePrimary
import must.kdroiders.hustlehub.ui.theme.HustlePrimaryVariant
import must.kdroiders.hustlehub.ui.theme.HustleWarningAmber
import androidx.compose.material3.*

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onEditClick: () -> Unit = {},
    onAddNewServiceClick: () -> Unit = {}
) {
    val state by profileViewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            state.isLoading -> LoadingState()
            state.error != null -> ErrorState(
                message = state.error!!,
                onRetry = profileViewModel::retry
            )
            else -> ProfileContent(
                state = state,
                onEditClick = onEditClick,
                onToggleService = profileViewModel::toggleServiceActive,
                onAddNewServiceClick = onAddNewServiceClick
            )
        }
    }
}

// ─────────────────────────────────────────────────
// Main content — LazyColumn for performance
// ─────────────────────────────────────────────────

@Composable
private fun ProfileContent(
    state: ProfileUiState,
    onEditClick: () -> Unit,
    onToggleService: (String) -> Unit,
    onAddNewServiceClick: () -> Unit
) {
    val user = state.user ?: return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            top = 16.dp,
            bottom = 100.dp
        )
    ) {
        // Header
        item(key = "header") {
            ProfileHeader(onEditClick = onEditClick)
        }

        // Avatar + info
        item(key = "avatar") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileAvatar(
                    photoUrl = user.profilePhotoUrl
                )
                Spacer(Modifier.height(12.dp))
                ProfileInfo(
                    name = user.name,
                    course = user.course,
                    yearOfStudy = user.yearOfStudy,
                    campus = user.campus
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
                    horizontal = 16.dp
                )
            )
        }

        // Badges
        item(key = "badges") {
            Spacer(Modifier.height(16.dp))
            ProfileBadges(
                badges = state.badges,
                modifier = Modifier.padding(
                    horizontal = 16.dp
                )
            )
        }

        // Services header
        item(key = "services_header") {
            Spacer(Modifier.height(24.dp))
            ServicesHeader(
                onAddNewServiceClick = onAddNewServiceClick,
                modifier = Modifier.padding(
                    horizontal = 16.dp
                )
            )
            Spacer(Modifier.height(12.dp))
        }

        // Service cards
        items(
            items = state.services,
            key = { it.id }
        ) { service ->
            ServiceCard(
                service = service,
                onToggle = {
                    onToggleService(service.id)
                },
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 6.dp
                )
            )
        }

        // Bottom tabs
        item(key = "bottom_tabs") {
            Spacer(Modifier.height(20.dp))
            ProfileBottomTabs(
                modifier = Modifier.padding(
                    horizontal = 16.dp
                )
            )
        }
    }
}

