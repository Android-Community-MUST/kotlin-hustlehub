package must.kdroiders.hustlehub.ui.features.profile.presentation.view

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import must.kdroiders.hustlehub.sharedComposables.ErrorView
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.LoadingIndicator
import must.kdroiders.hustlehub.sharedComposables.SectionHeader
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileAvatar
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileBadges
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileInfo
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ProfileStatsRow
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.components.ServiceCard
import must.kdroiders.hustlehub.ui.features.profile.presentation.viewmodel.ProviderProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderProfileScreen(
    providerId: String,
    providerProfileViewModel: ProviderProfileViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onNavigateToChat: (providerId: String) -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToServiceDetail: (serviceId: String) -> Unit = {},
) {
    val state by providerProfileViewModel.uiState.collectAsState()

    LaunchedEffect(providerId) {
        providerProfileViewModel.initialize(providerId)
    }

    Scaffold(
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
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                            HustleButton(
                                text = "Edit Profile",
                                onClick = onNavigateToEditProfile,
                                modifier = Modifier.fillMaxWidth(),
                            )
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
                val provider = state.provider ?: return@Scaffold

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(
                        top = innerPadding.calculateTopPadding() + 16.dp,
                        bottom = innerPadding.calculateBottomPadding() + 16.dp,
                    ),
                ) {
                    item(key = "avatar") {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            ProfileAvatar(photoUrl = provider.profilePhotoUrl)
                            Spacer(Modifier.height(12.dp))
                            ProfileInfo(
                                name = provider.name,
                                phone = provider.phone,
                                campusLocation = provider.campusLocation,
                                bio = provider.bio,
                            )
                        }
                    }

                    item(key = "stats") {
                        Spacer(Modifier.height(20.dp))
                        ProfileStatsRow(
                            hustleScore = state.hustleScore,
                            serviceCount = state.services.size,
                            reviewCount = state.reviewCount,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }

                    if (state.badges.isNotEmpty()) {
                        item(key = "badges") {
                            Spacer(Modifier.height(16.dp))
                            ProfileBadges(
                                badges = state.badges,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }

                    item(key = "services_header") {
                        Spacer(Modifier.height(24.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SectionHeader(title = "Services (${state.services.size})")
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    items(items = state.services, key = { it.id }) { service ->
                        ServiceCard(
                            service = service,
                            onClick = { onNavigateToServiceDetail(service.id) },
                            onToggle = {},
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        )
                    }
                }
            }
        }
    }
}
