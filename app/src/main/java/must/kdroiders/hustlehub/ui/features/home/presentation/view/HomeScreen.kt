package must.kdroiders.hustlehub.ui.features.home.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import must.kdroiders.hustlehub.ui.features.home.presentation.components.CategoryChipRow
import must.kdroiders.hustlehub.ui.features.home.presentation.components.EmptyServicesView
import must.kdroiders.hustlehub.ui.features.home.presentation.components.HomeSearchBar
import must.kdroiders.hustlehub.ui.features.home.presentation.components.HomeTopBar
import must.kdroiders.hustlehub.ui.features.home.presentation.components.ServiceCard
import must.kdroiders.hustlehub.ui.features.home.presentation.components.ServiceCardShimmer
import must.kdroiders.hustlehub.ui.features.home.presentation.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Detect end of list to trigger next page
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisible >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore }
            .distinctUntilChanged()
            .collect { atEnd ->
                if (atEnd) viewModel.loadNextPage()
            }
    }

    // Show errors as snackbar
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Top app bar
                item(key = "topbar") {
                    HomeTopBar(
                        initials = state.providerInitials,
                        notificationCount = state.notificationCount
                    )
                }

                // Search bar
                item(key = "search") {
                    Spacer(Modifier.height(16.dp))
                    HomeSearchBar(
                        query = state.searchQuery,
                        onQueryChanged = viewModel::onSearchQueryChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }

                // Category chips
                item(key = "categories") {
                    Spacer(Modifier.height(16.dp))
                    CategoryChipRow(
                        selected = state.selectedCategory,
                        onSelected = viewModel::onCategorySelected
                    )
                }

                // Browse services section header
                item(key = "browse_header") {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Browse Services",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // Shimmer placeholders while initial load
                if (state.isLoadingServices) {
                    items(count = 4, key = { "shimmer_$it" }) {
                        ServiceCardShimmer(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }

                // Empty state
                if (!state.isLoadingServices && state.services.isEmpty()) {
                    item(key = "empty") {
                        EmptyServicesView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp)
                        )
                    }
                }

                // Real service cards
                itemsIndexed(
                    items = state.services,
                    key = { _, service -> service.id }
                ) { _, service ->
                    ServiceCard(
                        service = service,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                // Load-more spinner at the bottom
                if (state.isLoadingMore) {
                    item(key = "loading_more") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
