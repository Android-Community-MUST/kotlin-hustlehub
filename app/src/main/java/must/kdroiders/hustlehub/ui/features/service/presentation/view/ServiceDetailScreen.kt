package must.kdroiders.hustlehub.ui.features.service.presentation.view

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.sharedComposables.EmptyStateView
import must.kdroiders.hustlehub.sharedComposables.ErrorView
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.LoadingIndicator
import must.kdroiders.hustlehub.sharedComposables.SectionHeader
import must.kdroiders.hustlehub.sharedComposables.ServiceProviderBadge
import must.kdroiders.hustlehub.ui.features.report.presentation.ReportDialog
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.AvailabilityBadge
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.CategoryBadge
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.FullScreenImageViewer
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.PortfolioGallery
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.ReviewItem
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.ReviewSummaryCard
import must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel.ServiceDetailUiState
import must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel.ServiceDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    serviceId: String,
    serviceDetailViewModel: ServiceDetailViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onNavigateToChat: (
        providerId: String,
        serviceId: String,
        serviceTitle: String,
        serviceCategory: String,
        servicePriceRange: String,
        providerName: String,
    ) -> Unit = { _, _, _, _, _, _ -> },
    onNavigateToProviderProfile: (providerId: String) -> Unit = {},
    onNavigateToWriteReview: (serviceId: String, providerId: String) -> Unit = { _, _ -> },
    onNavigateToAllReviews: (serviceId: String) -> Unit = {},
) {
    val state by serviceDetailViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(serviceId) {
        serviceDetailViewModel.initialize(serviceId)
    }

    var fullScreenImageIndex by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!state.isLoading && state.error == null && !state.isOwnService) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 16.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                text = "STARTING AT",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "KES ${state.service?.priceRange ?: ""} +",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }

                        HustleButton(
                            text = if (state.isOwnService) "Your Service" else "DM Provider",
                            enabled = !state.isOwnService,
                            onClick = {
                                val svc = state.service
                                if (svc != null) {
                                    onNavigateToChat(
                                        svc.providerId,
                                        svc.id,
                                        svc.title,
                                        svc.category.name,
                                        svc.priceRange,
                                        state.provider?.name ?: "",
                                    )
                                }
                            },
                            modifier = Modifier.width(200.dp),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            when {
                state.isLoading -> LoadingIndicator(modifier = Modifier.fillMaxSize())
                state.error != null -> ErrorView(
                    message = state.error ?: "Unknown error",
                    onRetry = serviceDetailViewModel::retry,
                    modifier = Modifier.fillMaxSize(),
                )
                else -> ServiceDetailContent(
                    state = state,
                    contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 24.dp),
                    onImageClick = { index -> fullScreenImageIndex = index },
                    onProviderClick = { state.service?.providerId?.let { onNavigateToProviderProfile(it) } },
                    onNavigateToWriteReview = onNavigateToWriteReview,
                    onNavigateToAllReviews = onNavigateToAllReviews,
                )
            }

            // Top Action Buttons overlaid on image
            if (!state.isLoading && state.error == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(WindowInsets.statusBars.asPaddingValues())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    // Back Button
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.3f)),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                        )
                    }

                    // Share, Bookmark and More
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = { scope.launch { snackbarHostState.showSnackbar("Share feature coming soon!") } },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.3f)),
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                        }
                        var isBookmarked by remember { mutableStateOf(false) }
                        val bookmarkInteractionSource = remember { MutableInteractionSource() }
                        val isBookmarkPressed by bookmarkInteractionSource.collectIsPressedAsState()
                        val bookmarkScale by animateFloatAsState(
                            targetValue = if (isBookmarkPressed) 0.8f else if (isBookmarked) 1.2f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "bookmark_scale"
                        )

                        IconButton(
                            onClick = {
                                isBookmarked = !isBookmarked
                                scope.launch {
                                    val msg = if (isBookmarked) "Saved!" else "Removed from saved"
                                    snackbarHostState.showSnackbar(msg)
                                }
                            },
                            interactionSource = bookmarkInteractionSource,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.3f))
                                .scale(bookmarkScale),
                        ) {
                            Icon(
                                if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Save",
                                tint = if (isBookmarked) MaterialTheme.colorScheme.primary else Color.White
                            )
                        }

                        var showMenu by remember { mutableStateOf(false) }
                        var showReportDialog by remember { mutableStateOf(false) }

                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.3f)),
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = Color.White)
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Report Service") },
                                    onClick = {
                                        showMenu = false
                                        showReportDialog = true
                                    },
                                )
                            }
                        }

                        if (showReportDialog) {
                            ReportDialog(
                                targetId = serviceId,
                                targetType = "service",
                                onDismiss = { showReportDialog = false },
                            )
                        }
                    }
                }
            }
        }
    }

    fullScreenImageIndex?.let { index ->
        state.service?.portfolio?.let { portfolio ->
            FullScreenImageViewer(
                imageUrls = portfolio,
                initialIndex = index,
                onDismiss = { fullScreenImageIndex = null },
            )
        }
    }
}

@Composable
private fun ServiceDetailContent(
    state: ServiceDetailUiState,
    contentPadding: PaddingValues,
    onImageClick: (Int) -> Unit,
    onProviderClick: () -> Unit,
    onNavigateToWriteReview: (serviceId: String, providerId: String) -> Unit,
    onNavigateToAllReviews: (serviceId: String) -> Unit,
) {
    val service = state.service ?: return
    val provider = state.provider

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        // Hero Image Header
        item(key = "hero_image") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                if (service.portfolio.isNotEmpty()) {
                    val pagerState = rememberPagerState(pageCount = { service.portfolio.size })

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                    ) { page ->
                        AsyncImage(
                            model = service.portfolio[page],
                            contentDescription = "Service Hero Image ${page + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { onImageClick(page) },
                        )
                    }

                    // Pager Indicators
                    if (service.portfolio.size > 1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 32.dp), // Padding to keep above the curved bottom
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            repeat(service.portfolio.size) { iteration ->
                                val color = if (pagerState.currentPage ==
                                    iteration
                                ) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    Color.White.copy(alpha = 0.5f)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(color)
                                        .size(if (pagerState.currentPage == iteration) 8.dp else 6.dp),
                                )
                            }
                        }
                    }
                } else if (service.iconUrl.isNotBlank()) {
                    AsyncImage(
                        model = service.iconUrl,
                        contentDescription = "Service Hero Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                // Curved shape at the bottom to create a bottom-sheet effect
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(MaterialTheme.colorScheme.background),
                )
            }
        }

        // Title and Header Badges
        item(key = "header_info") {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                ) {
                    AvailabilityBadge(availability = service.availability)
                    CategoryBadge(category = service.category)
                }

                Text(
                    text = service.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "KES ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                    Text(
                        text = service.priceRange,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Provider Card
        item(key = "provider_card") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .clickable(onClick = onProviderClick)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Avatar with Verified badge
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = provider?.profilePhotoUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                    if (provider?.isVerified == true) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    ServiceProviderBadge(
                        name = provider?.name ?: "Loading…",
                        isVerifiedPro = provider?.isVerifiedPro == true,
                    )
                    provider?.campusLocation?.takeIf { it.isNotBlank() }?.let { location ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Mini stats block
                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "%.1f".format(service.averageRating),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(14.dp),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${state.totalReviewCount} jobs done",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        // About section
        if (service.description.isNotBlank()) {
            item(key = "about_header") {
                SectionHeader(title = "About Service", modifier = Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(12.dp))
            }
            item(key = "about_body") {
                Text(
                    text = service.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        // Portfolio
        if (service.portfolio.isNotEmpty()) {
            item(key = "portfolio_header") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SectionHeader(title = "Portfolio")
                    TextButton(onClick = { /* Full gallery view later */ }) {
                        Text("View All", style = MaterialTheme.typography.labelLarge)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            item(key = "portfolio_row") {
                PortfolioGallery(
                    imageUrls = service.portfolio,
                    onImageClick = onImageClick,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(Modifier.height(32.dp))
            }
        }

        // Reviews section
        item(key = "reviews_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Reviews",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() },
                )
                if (!state.isOwnService) {
                    Button(
                        onClick = { onNavigateToWriteReview(service.id, service.providerId) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp),
                    ) {
                        Text("Write a Review", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        item(key = "reviews_summary") {
            ReviewSummaryCard(
                averageRating = service.averageRating,
                totalReviews = state.totalReviewCount,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(24.dp))
        }

        if (state.reviews.isEmpty()) {
            item(key = "no_reviews") {
                EmptyStateView(
                    title = "No reviews yet",
                    description = "Complete a service to leave the first review!",
                    icon = Icons.Default.RateReview,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                )
            }
        } else {
            val latestReviews = state.reviews.take(3)
            items(items = latestReviews, key = { it.id }) { review ->
                ReviewItem(
                    review = review,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            if (state.totalReviewCount > 0) {
                item(key = "see_all") {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        TextButton(onClick = { onNavigateToAllReviews(service.id) }) {
                            Text("See all ${state.totalReviewCount} reviews")
                        }
                    }
                }
            }
        }
    }
}
