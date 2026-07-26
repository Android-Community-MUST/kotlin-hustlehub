package must.kdroiders.hustlehub.ui.features.profile.presentation.view

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import must.kdroiders.hustlehub.sharedComposables.ErrorView
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

                    item(key = "avatar") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-45).dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            ProfileAvatar(
                                photoUrl = provider.profilePhotoUrl,
                                isVerified = provider.isVerified,
                            )
                            Spacer(Modifier.height(10.dp))
                            ProfileInfo(
                                name = provider.name,
                                phone = provider.phone,
                                campusLocation = provider.campusLocation,
                                bio = provider.bio,
                                isOnline = provider.isOnline,
                                allowCalls = provider.allowCalls,
                                isOwnProfile = state.isOwnProfile,
                            )
                        }
                    }

                    item(key = "stats") {
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
