package must.kdroiders.hustlehub.ui.features.home.presentation.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.ui.features.home.presentation.components.CategoryChipRow
import must.kdroiders.hustlehub.ui.features.home.presentation.components.EmptyServicesView
import must.kdroiders.hustlehub.ui.features.home.presentation.components.FeaturedServicesRow
import must.kdroiders.hustlehub.ui.features.home.presentation.components.HomeSearchBar
import must.kdroiders.hustlehub.ui.features.home.presentation.components.HomeTopBar
import must.kdroiders.hustlehub.ui.features.home.presentation.components.ProviderBannerCard
import must.kdroiders.hustlehub.ui.features.home.presentation.components.ServiceCard
import must.kdroiders.hustlehub.ui.features.home.presentation.components.ServiceCardShimmer
import must.kdroiders.hustlehub.ui.features.home.presentation.viewmodel.HomeViewModel
import must.kdroiders.hustlehub.ui.theme.HustleActiveGreen
import must.kdroiders.hustlehub.ui.theme.LocalDimensions

/** Number of shimmer placeholders shown while the initial page loads. */
private const val SHIMMER_COUNT = 6

/**
 * Discovery / Home screen — the primary browsing destination.
 *
 * NavKey: [BottomHome]
 * Layout:
 *  - Full-span header: TopBar → SearchBar (with AI chip) → CategoryChips → Featured row
 *  - 2-column [LazyVerticalGrid] of service cards with shimmer loading
 *  - Pagination trigger on scroll near the end
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = hiltViewModel(),
    onNavigateToServiceDetail: (serviceId: String) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToAiSearch: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToCreateService: () -> Unit = {},
) {
    val state by homeViewModel.uiState.collectAsState()
    val gridState = rememberLazyGridState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val stableOnNavigateToServiceDetail = remember { onNavigateToServiceDetail }

    // Detect when the user scrolls to 3 items before the end to trigger next page.
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo
                .lastOrNull()
                ?.index ?: 0
            val totalItems = gridState.layoutInfo.totalItemsCount
            lastVisible >= totalItems - 3 &&
                !state.isLoadingServices &&
                !state.isLoadingMore &&
                state.hasMorePages &&
                state.services.isNotEmpty()
        }
    }

    LaunchedEffect(gridState) {
        snapshotFlow { shouldLoadMore }
            .distinctUntilChanged()
            .collect { atEnd -> if (atEnd) homeViewModel.loadNextPage() }
    }

    // Surface errors as snackbars so the grid stays visible (offline-first UX).
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            homeViewModel.clearError()
        }
    }

    val dimensions = LocalDimensions.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        val pullToRefreshState = rememberPullToRefreshState()

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = homeViewModel::onRefresh,
            modifier = Modifier.fillMaxSize(),
            state = pullToRefreshState,
            indicator = {
                val progress = pullToRefreshState.distanceFraction.coerceIn(0f, 1f)
                val scale by animateFloatAsState(
                    targetValue = if (state.isRefreshing) 1f else progress,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "logo_pull_scale",
                )
                val rotation by animateFloatAsState(
                    targetValue = if (state.isRefreshing) 360f else progress * 180f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "logo_pull_rotation",
                )

                if (progress > 0f || state.isRefreshing) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                            .size(38.dp)
                            .scale(scale)
                            .graphicsLayer(rotationZ = rotation)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary,
                                    ),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "H",
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 18.sp,
                        )
                    }
                }
            },
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(dimensions.gridColumns),
                state = gridState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("home_service_grid"),
                contentPadding = PaddingValues(
                    start = dimensions.horizontalPadding,
                    end = dimensions.horizontalPadding,
                    bottom = 100.dp,
                ),
                verticalArrangement = Arrangement
                    .spacedBy(dimensions.gridSpacing),
                horizontalArrangement = Arrangement
                    .spacedBy(dimensions.gridSpacing),
            ) {
                // Header items span both columns.

                item(key = "topbar", span = { GridItemSpan(maxLineSpan) }) {
                    HomeTopBar(
                        initials = state.providerInitials,
                        notificationCount = state.notificationCount,
                        onNotificationClick = onNavigateToNotifications,
                    )
                }

                item(key = "provider_banner", span = { GridItemSpan(maxLineSpan) }) {
                    AnimatedVisibility(
                        visible = state.showProviderBanner,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        ProviderBannerCard(
                            onListServiceClick = onNavigateToCreateService,
                            onDismiss = homeViewModel::dismissProviderBanner,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                        )
                    }
                }

                item(key = "searchbar", span = { GridItemSpan(maxLineSpan) }) {
                    HomeSearchBar(
                        query = state.searchQuery,
                        onQueryChanged = homeViewModel::onSearchQueryChanged,
                        onSearchClick = onNavigateToSearch,
                        onAiSearchClick = onNavigateToAiSearch,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                    )
                }

                item(key = "categories", span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(Modifier.height(4.dp))
                    CategoryChipRow(
                        selected = state.selectedCategory,
                        onSelected = homeViewModel::onCategorySelected,
                    )
                }

                // Featured section — top 5 rated services, conditionally shown.

                if (state.featuredServices.isNotEmpty()) {
                    item(key = "featured_header", span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Featured Hustlers",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            TextButton(
                                onClick = {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Coming soon")
                                    }
                                },
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Text(
                                    text = "View All",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp,
                                )
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                    }

                    item(key = "featured_row", span = { GridItemSpan(maxLineSpan) }) {
                        FeaturedServicesRow(
                            services = state.featuredServices,
                            onServiceClick = onNavigateToServiceDetail,
                            modifier = Modifier,
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                }

                // Section label for the paginated service grid below.

                item(key = "browse_header", span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.padding(start = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(HustleActiveGreen),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Available Now",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Show shimmer skeletons while the first page is in flight.

                if (state.isLoadingServices) {
                    items(
                        count = SHIMMER_COUNT,
                        key = { "shimmer_$it" },
                        span = { GridItemSpan(1) },
                    ) {
                        ServiceCardShimmer()
                    }
                }

                // Empty state — shown only after a successful load returns no results.

                if (!state.isLoadingServices && state.services.isEmpty()) {
                    item(key = "empty", span = { GridItemSpan(maxLineSpan) }) {
                        EmptyServicesView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                        )
                    }
                }

                // Service cards rendered in the 2-column grid.

                itemsIndexed(
                    items = state.services,
                    key = { _, service -> service.id },
                    span = { _, _ -> GridItemSpan(1) },
                ) { index, service ->
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(index * 50L) // Stagger by 50ms — runs once per slot
                        visible = true
                    }
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    ) {
                        ServiceCard(
                            service = service,
                            onClick = { stableOnNavigateToServiceDetail(service.id) },
                            modifier = Modifier.testTag("service_card_${service.id}"),
                        )
                    }
                }

                // Pagination progress indicator appended at the end of the list.

                if (state.isLoadingMore) {
                    item(key = "loading_more", span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularWavyProgressIndicator(
                                modifier = Modifier.padding(8.dp),
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
