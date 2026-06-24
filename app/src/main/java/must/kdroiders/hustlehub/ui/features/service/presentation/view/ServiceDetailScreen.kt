package must.kdroiders.hustlehub.ui.features.service.presentation.view

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import must.kdroiders.hustlehub.sharedComposables.ErrorView
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.LoadingIndicator
import must.kdroiders.hustlehub.sharedComposables.RatingBar
import must.kdroiders.hustlehub.sharedComposables.SectionHeader
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.AvailabilityBadge
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.FullScreenImageViewer
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.PortfolioGallery
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.ReviewCard
import must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel.ServiceDetailUiState
import must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel.ServiceDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    serviceId: String,
    serviceDetailViewModel: ServiceDetailViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onNavigateToChat: (providerId: String) -> Unit = {},
    onNavigateToProviderProfile: (providerId: String) -> Unit = {},
    onNavigateToWriteReview: (serviceId: String, providerId: String) -> Unit = { _, _ -> },
) {
    val state by serviceDetailViewModel.uiState.collectAsState()

    LaunchedEffect(serviceId) {
        serviceDetailViewModel.initialize(serviceId)
    }

    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.service?.title ?: "Service Detail",
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
                actions = {
                    IconButton(onClick = { /* Share intent */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Report service") },
                                onClick = { showMenu = false },
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
            if (!state.isLoading && state.error == null && !state.isOwnService) {
                Surface(shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (!state.isOwnService) {
                            HustleButton(
                                text = "Write Review",
                                onClick = {
                                    val service = state.service ?: return@HustleButton
                                    onNavigateToWriteReview(service.id, service.providerId)
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        HustleButton(
                            text = "Message Provider",
                            onClick = {
                                state.service?.providerId?.let { onNavigateToChat(it) }
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingIndicator(modifier = Modifier.padding(innerPadding).fillMaxSize())
            state.error != null -> ErrorView(
                message = state.error ?: "Unknown error",
                onRetry = serviceDetailViewModel::retry,
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
            )
            else -> ServiceDetailContent(
                state = state,
                contentPadding = innerPadding,
                onImageClick = { url -> fullScreenImageUrl = url },
                onProviderClick = { state.service?.providerId?.let { onNavigateToProviderProfile(it) } },
            )
        }
    }

    fullScreenImageUrl?.let { url ->
        FullScreenImageViewer(
            imageUrl = url,
            onDismiss = { fullScreenImageUrl = null },
        )
    }
}

@Composable
private fun ServiceDetailContent(
    state: ServiceDetailUiState,
    contentPadding: PaddingValues,
    onImageClick: (String) -> Unit,
    onProviderClick: () -> Unit,
) {
    val service = state.service ?: return
    val provider = state.provider

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding() + 16.dp,
        ),
    ) {
        // Portfolio gallery
        if (service.portfolio.isNotEmpty()) {
            item(key = "gallery") {
                PortfolioGallery(
                    imageUrls = service.portfolio,
                    onImageClick = onImageClick,
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        // Provider info row
        item(key = "provider_info") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onProviderClick)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AsyncImage(
                    model = provider?.profilePhotoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = provider?.name ?: "Loading…",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    provider?.campusLocation?.takeIf { it.isNotBlank() }?.let { location ->
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                AvailabilityBadge(availability = service.availability)
            }
            Spacer(Modifier.height(16.dp))
        }

        // Rating + price
        item(key = "rating_price") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RatingBar(rating = service.averageRating, starSize = 18.dp)
                Text(
                    text = "%.1f".format(service.averageRating),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "· ${state.totalReviewCount} reviews",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "KES ${service.priceRange}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        // About section
        if (service.description.isNotBlank()) {
            item(key = "about_header") {
                SectionHeader(title = "About", modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(6.dp))
            }
            item(key = "about_body") {
                Text(
                    text = service.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        // Tags
        if (service.tags.isNotEmpty()) {
            item(key = "tags") {
                Text(
                    text = service.tags.joinToString("  ") { "#${it}" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(20.dp))
            }
        }

        // Reviews header
        item(key = "reviews_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Reviews (${state.totalReviewCount})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        if (state.reviews.isEmpty()) {
            item(key = "no_reviews") {
                Text(
                    text = "No reviews yet. Be the first to leave one!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        } else {
            items(items = state.reviews, key = { it.id }) { review ->
                ReviewCard(
                    review = review,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            if (state.totalReviewCount > state.reviews.size) {
                item(key = "see_all") {
                    TextButton(
                        onClick = {},
                        modifier = Modifier.padding(horizontal = 8.dp),
                    ) {
                        Text("See all ${state.totalReviewCount} reviews")
                    }
                }
            }
        }
    }
}
