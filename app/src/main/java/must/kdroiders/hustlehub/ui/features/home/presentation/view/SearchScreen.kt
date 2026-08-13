package must.kdroiders.hustlehub.ui.features.home.presentation.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import must.kdroiders.hustlehub.ui.features.home.domain.model.SearchFilters
import must.kdroiders.hustlehub.ui.features.home.domain.model.SortOrder
import must.kdroiders.hustlehub.ui.features.home.presentation.components.EmptyServicesView
import must.kdroiders.hustlehub.ui.features.home.presentation.components.FilterBottomSheet
import must.kdroiders.hustlehub.ui.features.home.presentation.components.SearchServiceRow
import must.kdroiders.hustlehub.ui.features.home.presentation.components.ServiceCardShimmer
import must.kdroiders.hustlehub.ui.features.home.presentation.viewmodel.SearchViewModel
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability

private const val SEARCH_SHIMMER_COUNT = 6

/**
 * Full search screen with real-time text search, filter bottom sheet, active filter chips,
 * recent search suggestions, and a paginated 2-column result grid.
 *
 * The search field is auto-focused on entry. Results update after a 300ms debounce
 * in [SearchViewModel]. When the field is empty, recent search suggestions are shown.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onNavigateToServiceDetail: (serviceId: String) -> Unit,
    onNavigateToChat: (providerId: String) -> Unit = {},
    modifier: Modifier = Modifier,
    searchViewModel: SearchViewModel = hiltViewModel(),
) {
    val state by searchViewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusRequester = remember { FocusRequester() }

    // Auto-focus the search field when the screen opens.
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    // Pagination trigger.
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo
                .lastOrNull()
                ?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            lastVisible >= total - 3 &&
                !state.isLoading &&
                !state.isLoadingMore &&
                state.hasMorePages &&
                state.services.isNotEmpty()
        }
    }
    LaunchedEffect(listState) {
        snapshotFlow { shouldLoadMore }.distinctUntilChanged().collect { if (it) searchViewModel.loadNextPage() }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            searchViewModel.clearError()
        }
    }

    if (state.isFilterSheetOpen) {
        FilterBottomSheet(
            draft = state.draftFilters,
            onDraftChanged = searchViewModel::onDraftFilterChanged,
            onApply = searchViewModel::onFiltersApplied,
            onReset = searchViewModel::onFiltersReset,
            onDismiss = searchViewModel::onFilterSheetToggle,
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        val pullToRefreshState = rememberPullToRefreshState()
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { searchViewModel.onQueryChanged(state.query) },
            modifier = Modifier.fillMaxSize(),
            state = pullToRefreshState,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = state.isLoading,
                    state = pullToRefreshState,
                    color = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            },
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .testTag("search_result_list"),
                contentPadding = PaddingValues(bottom = 100.dp),
            ) {
            // Top bar: back + search field + filter icon
            item(key = "topbar") {
                SearchTopBar(
                    query = state.query,
                    onQueryChanged = searchViewModel::onQueryChanged,
                    onBack = onBack,
                    onFilterClick = searchViewModel::onFilterSheetToggle,
                    focusRequester = focusRequester,
                    hasActiveFilters = !state.filters.isDefault,
                )
            }

            // Active filter chips — visible only when filters differ from defaults.
            if (!state.filters.isDefault) {
                item(key = "active_filters") {
                    ActiveFilterChipRow(
                        filters = state.filters,
                        onRemoveCategory = { cat ->
                            searchViewModel.onFiltersApplied()
                            searchViewModel.onDraftFilterChanged(
                                state.filters.copy(categories = state.filters.categories - cat),
                            )
                            searchViewModel.onFiltersApplied()
                        },
                        onRemoveRating = {
                            searchViewModel.onDraftFilterChanged(state.filters.copy(minRating = 0f))
                            searchViewModel.onFiltersApplied()
                        },
                        onRemovePrice = {
                            searchViewModel.onDraftFilterChanged(state.filters.copy(maxPrice = 5000))
                            searchViewModel.onFiltersApplied()
                        },
                        onRemoveAvailability = {
                            searchViewModel.onDraftFilterChanged(state.filters.copy(availability = null))
                            searchViewModel.onFiltersApplied()
                        },
                        onRemoveSort = {
                            searchViewModel.onDraftFilterChanged(state.filters.copy(sortOrder = SortOrder.NEWEST))
                            searchViewModel.onFiltersApplied()
                        },
                        onClearAll = searchViewModel::onFiltersReset,
                    )
                }
            }

            // Recent searches — shown when query is empty and history exists.
            if (state.query.isEmpty() && state.recentSearches.isNotEmpty()) {
                item(key = "recent_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Recent",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TextButton(onClick = { searchViewModel.clearRecentSearches() }) {
                            Text("Clear", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                item(key = "recent_searches") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((state.recentSearches.size * 48).dp.coerceAtMost(240.dp)),
                    ) {
                        state.recentSearches.forEach { term ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { searchViewModel.onQueryChanged(term) }
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = term,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            }

            // Shimmer placeholders during initial load.
            if (state.isLoading) {
                items(count = SEARCH_SHIMMER_COUNT, key = { "shimmer_$it" }) {
                    ServiceCardShimmer()
                }
            }

            // Empty state after successful load with no results.
            if (!state.isLoading && state.services.isEmpty() && state.query.isNotEmpty()) {
                item(key = "empty") {
                    EmptyServicesView(
                        message = "No results for \"${state.query}\"",
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    )
                }
            }

            // Result cards.
            items(items = state.services, key = { it.id }) { service ->
                SearchServiceRow(
                    service = service,
                    onClick = { onNavigateToServiceDetail(service.id) },
                    onChatClick = { onNavigateToChat(service.providerId) },
                    modifier = Modifier.testTag("search_card_${service.id}"),
                )
            }

            // Pagination spinner.
            if (state.isLoadingMore) {
                item(key = "loading_more") {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularWavyProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary,
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

@Composable
private fun SearchTopBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onBack: () -> Unit,
    onFilterClick: () -> Unit,
    focusRequester: FocusRequester,
    hasActiveFilters: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(Modifier.width(4.dp))
        // Inline search text field styled as a capsule.
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChanged,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { /* query fires on change */ }),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Search services...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    inner()
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .testTag("search_text_field"),
            )
            AnimatedVisibility(visible = query.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                IconButton(onClick = { onQueryChanged("") }, modifier = Modifier.size(20.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        Spacer(Modifier.width(4.dp))
        IconButton(
            onClick = onFilterClick,
            modifier = Modifier.testTag("filter_icon_button"),
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Filters",
                tint = if (hasActiveFilters) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun ActiveFilterChipRow(
    filters: SearchFilters,
    onRemoveCategory: (String) -> Unit,
    onRemoveRating: () -> Unit,
    onRemovePrice: () -> Unit,
    onRemoveAvailability: () -> Unit,
    onRemoveSort: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("active_filter_row"),
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Per-category chips
        items(items = filters.categories.toList(), key = { "cat_$it" }) { cat ->
            PremiumFilterChip(
                label = cat,
                onRemove = { onRemoveCategory(cat) },
            )
        }
        if (filters.minRating > 0f) {
            item(key = "rating") {
                PremiumFilterChip(
                    label = "%.1f+".format(filters.minRating),
                    onRemove = onRemoveRating,
                )
            }
        }
        if (filters.maxPrice < 5000) {
            item(key = "price") {
                PremiumFilterChip(
                    label = "< ${filters.maxPrice} KES",
                    onRemove = onRemovePrice,
                )
            }
        }
        filters.availability?.let { avail ->
            item(key = "avail") {
                PremiumFilterChip(
                    label = if (avail == ServiceAvailability.AVAILABLE) "Available" else "Busy",
                    onRemove = onRemoveAvailability,
                )
            }
        }
        if (filters.sortOrder != SortOrder.NEWEST) {
            item(key = "sort") {
                PremiumFilterChip(
                    label = filters.sortOrder.label,
                    onRemove = onRemoveSort,
                )
            }
        }
        item(key = "clear_all") {
            TextButton(
                onClick = onClearAll,
                modifier = Modifier.testTag("clear_all_filters_button"),
            ) {
                Text(
                    text = "Clear All",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun PremiumFilterChip(
    label: String,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            .clickable(onClick = onRemove)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remove",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(14.dp),
        )
    }
}
