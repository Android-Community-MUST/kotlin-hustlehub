package must.kdroiders.hustlehub.ui.features.home.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import must.kdroiders.hustlehub.sharedComposables.SectionHeader
import must.kdroiders.hustlehub.ui.features.home.presentation.components.AvailableNowGrid
import must.kdroiders.hustlehub.ui.features.home.presentation.components.AvailableNowHeader
import must.kdroiders.hustlehub.ui.features.home.presentation.components.CategoryChipRow
import must.kdroiders.hustlehub.ui.features.home.presentation.components.HomeSearchBar
import must.kdroiders.hustlehub.ui.features.home.presentation.components.HomeTopBar
import must.kdroiders.hustlehub.ui.features.home.presentation.components.TopHustlersRow
import must.kdroiders.hustlehub.ui.features.home.presentation.viewmodel.HomeViewModel
import must.kdroiders.hustlehub.ui.theme.HustleHubTheme

/**
 * HomeScreen — Discovery Feed
 *
 * NavKey: [BottomHome]
 * Displays the top app bar, category chips, "Top Hustlers" carousel,
 * and the "Available Now" 2-column grid.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Top App Bar
        HomeTopBar(
            initials = state.providerInitials,
            notificationCount = state.notificationCount
        )

        // Scrollable body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            // Search bar
            HomeSearchBar(
                query = state.searchQuery,
                onQueryChanged = viewModel::onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Category chips
            CategoryChipRow(
                selected = state.selectedCategory,
                onSelected = viewModel::onCategorySelected
            )

            Spacer(Modifier.height(24.dp))

            // Top Hustlers section
            SectionHeader(
                title = "Top Hustlers",
                actionLabel = "View All",
                onAction = { /* TODO: navigate to full list */ },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(12.dp))

            TopHustlersRow(hustlers = state.topHustlers)

            Spacer(Modifier.height(24.dp))

            // Available Now section
            AvailableNowHeader(
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(12.dp))

            AvailableNowGrid(
                services = state.availableNow,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    HustleHubTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                HomeTopBar(initials = "JK", notificationCount = 3)
                HomeSearchBar(
                    query = "",
                    onQueryChanged = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(12.dp))
                SectionHeader(
                    title = "Top Hustlers",
                    actionLabel = "View All",
                    onAction = {},
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}
