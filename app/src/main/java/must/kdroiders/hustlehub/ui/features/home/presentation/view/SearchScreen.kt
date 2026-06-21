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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import must.kdroiders.hustlehub.data.model.ServiceAvailability
import must.kdroiders.hustlehub.ui.features.home.domain.model.SearchFilters
import must.kdroiders.hustlehub.ui.features.home.domain.model.SortOrder
import must.kdroiders.hustlehub.ui.features.home.presentation.components.EmptyServicesView
import must.kdroiders.hustlehub.ui.features.home.presentation.components.FilterBottomSheet
import must.kdroiders.hustlehub.ui.features.home.presentation.components.ServiceCard
import must.kdroiders.hustlehub.ui.features.home.presentation.components.ServiceCardShimmer
import must.kdroiders.hustlehub.ui.features.home.presentation.viewmodel.SearchViewModel

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
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val gridState = rememberLazyGridState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusRequester = remember { FocusRequester() }

    // Auto-focus the search field when the screen opens.
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    // Pagination trigger.
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = gridState.layoutInfo.totalItemsCount
            lastVisible >= total - 3 &&
                !state.isLoading &&
                !state.isLoadingMore &&
                state.hasMorePages &&
                state.services.isNotEmpty()
        }
    }
    LaunchedEffect(gridState) {
        snapshotFlow { shouldLoadMore }.distinctUntilChanged().collect { if (it) viewModel.loadNextPage() }
    }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    if (state.isFilterSheetOpen) {
        FilterBottomSheet(
            draft = state.draftFilters,
            onDraftChanged = viewModel::onDraftFilterChanged,
            onApply = viewModel::onFiltersApplied,
            onReset = viewModel::onFiltersReset,
            onDismiss = viewModel::onFilterSheetToggle,
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .testTag("search_result_grid"),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Top bar: back + search field + filter icon
            item(key = "topbar", span = { GridItemSpan(maxLineSpan) }) {
                SearchTopBar(
                    query = state.query,
                    onQueryChanged = viewModel::onQueryChanged,
                    onBack = onBack,
                    onFilterClick = viewModel::onFilterSheetToggle,
                    focusRequester = focusRequester,
                    hasActiveFilters = !state.filters.isDefault,
                )
            }

            // Active filter chips — visible only when filters differ from defaults.
            if (!state.filters.isDefault) {
                item(key = "active_filters", span = { GridItemSpan(maxLineSpan) }) {
                    ActiveFilterChipRow(
                        filters = state.filters,
                        onRemoveCategory = { cat ->
                            viewModel.onFiltersApplied()
                            viewModel.onDraftFilterChanged(
                                state.filters.copy(categories = state.filters.categories - cat),
                            )
                            viewModel.onFiltersApplied()
                        },
                        onRemoveRating = {
                            viewModel.onDraftFilterChanged(state.filters.copy(minRating = 0f))
                            viewModel.onFiltersApplied()
                        },
                        onRemovePrice = {
                            viewModel.onDraftFilterChanged(state.filters.copy(maxPrice = 5000))
                            viewModel.onFiltersApplied()
                        },
                        onRemoveAvailability = {
                            viewModel.onDraftFilterChanged(state.filters.copy(availability = null))
                            viewModel.onFiltersApplied()
                        },
                        onRemoveSort = {
                            viewModel.onDraftFilterChanged(state.filters.copy(sortOrder = SortOrder.NEWEST))
                            viewModel.onFiltersApplied()
                        },
                        onClearAll = viewModel::onFiltersReset,
                    )
                }
            }

            // Recent searches — shown when query is empty and history exists.
            if (state.query.isEmpty() && state.recentSearches.isNotEmpty()) {
                item(key = "recent_header", span = { GridItemSpan(maxLineSpan) }) {
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
                        TextButton(onClick = { viewModel.clearRecentSearches() }) {
                            Text("Clear", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                item(key = "recent_searches", span = { GridItemSpan(maxLineSpan) }) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((state.recentSearches.size * 48).dp.coerceAtMost(240.dp)),
                        userScrollEnabled = false,
                    ) {
                        items(items = state.recentSearches, key = { it }) { term ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.onQueryChanged(term) }
                                    .padding(horizontal = 4.dp, vertical = 12.dp),
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
                items(count = SEARCH_SHIMMER_COUNT, key = { "shimmer_$it" }, span = { GridItemSpan(1) }) {
                    ServiceCardShimmer()
                }
            }

            // Empty state after successful load with no results.
            if (!state.isLoading && state.services.isEmpty() && state.query.isNotEmpty()) {
                item(key = "empty", span = { GridItemSpan(maxLineSpan) }) {
                    EmptyServicesView(
                        message = "No results for \"${state.query}\"",
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    )
                }
            }

            // Result cards.
            items(items = state.services, key = { it.id }, span = { GridItemSpan(1) }) { service ->
                ServiceCard(
                    service = service,
                    onClick = { onNavigateToServiceDetail(service.id) },
                    modifier = Modifier.testTag("search_card_${service.id}"),
                )
            }

            // Pagination spinner.
            if (state.isLoadingMore) {
                item(key = "loading_more", span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.padding(8.dp),
                        )
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
            InputChip(
                selected = true,
                onClick = { onRemoveCategory(cat) },
                label = { Text(cat) },
                trailingIcon = {
                    Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                },
            )
        }
        if (filters.minRating > 0f) {
            item(key = "rating") {
                InputChip(
                    selected = true,
                    onClick = onRemoveRating,
                    label = { Text("%.1f+ stars".format(filters.minRating)) },
                    trailingIcon = {
                        Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                    },
                )
            }
        }
        if (filters.maxPrice < 5000) {
            item(key = "price") {
                InputChip(
                    selected = true,
                    onClick = onRemovePrice,
                    label = { Text("≤ KES ${filters.maxPrice}") },
                    trailingIcon = {
                        Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                    },
                )
            }
        }
        filters.availability?.let { avail ->
            item(key = "avail") {
                InputChip(
                    selected = true,
                    onClick = onRemoveAvailability,
                    label = { Text(if (avail == ServiceAvailability.AVAILABLE) "Available" else "Busy") },
                    trailingIcon = {
                        Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                    },
                )
            }
        }
        if (filters.sortOrder != SortOrder.NEWEST) {
            item(key = "sort") {
                InputChip(
                    selected = true,
                    onClick = onRemoveSort,
                    label = { Text(filters.sortOrder.label) },
                    trailingIcon = {
                        Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                    },
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
