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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleTextField
import must.kdroiders.hustlehub.ui.features.home.data.remote.QueryUnderstanding
import must.kdroiders.hustlehub.ui.features.home.presentation.components.AiMatchCard
import must.kdroiders.hustlehub.ui.features.home.presentation.viewmodel.AiSearchViewModel

/**
 * AI Search screen — accepts a natural language query and displays ranked service matches.
 *
 * The Gemini call happens server-side. If the backend falls back to keyword search,
 * a warning banner is shown above the results.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSearchScreen(
    onBack: () -> Unit,
    onNavigateToServiceDetail: (serviceId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AiSearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("AI Search", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_ai_sparkle),
                        contentDescription = "AI powered",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 16.dp).size(20.dp),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                modifier = Modifier.statusBarsPadding(),
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Query input + search button
                item(key = "input") {
                    Column {
                        HustleTextField(
                            value = state.query,
                            onValueChange = viewModel::onQueryChanged,
                            label = "What do you need?",
                            placeholder = "e.g. braids near Hostel C under 500",
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_search_input"),
                        )
                        Spacer(Modifier.height(12.dp))
                        HustleButton(
                            text = "Find Match",
                            onClick = { viewModel.onSearch() },
                            enabled = state.query.isNotBlank(),
                            loading = state.isLoading,
                            icon = Icons.Default.Search,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_search_button"),
                        )
                    }
                }

                // Loading indicator
                if (state.isLoading) {
                    item(key = "loading") {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("ai_search_loading"),
                            )
                        }
                    }
                }

                // Fallback warning banner
                if (state.usedFallback && state.matches.isNotEmpty()) {
                    item(key = "fallback_banner") {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "AI is temporarily unavailable — showing keyword results instead.",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                    }
                }

                // Query understanding chips
                state.queryUnderstanding?.let { understanding ->
                    if (!state.isLoading && state.matches.isNotEmpty()) {
                        item(key = "understanding") {
                            QueryUnderstandingRow(understanding = understanding)
                        }
                    }
                }

                // Results header
                if (!state.isLoading && state.matches.isNotEmpty()) {
                    item(key = "results_header") {
                        Text(
                            text = "Best Matches",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }

                // Match cards
                if (!state.isLoading) {
                    items(items = state.matches, key = { it.serviceId }) { match ->
                        AiMatchCard(
                            match = match,
                            onViewClick = { onNavigateToServiceDetail(match.serviceId) },
                        )
                    }
                }

                // Empty state — shown after a search that returned nothing
                if (!state.isLoading && state.matches.isEmpty() && state.query.isNotEmpty() && state.error == null) {
                    item(key = "empty") {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No matches found.\nTry a different query.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun QueryUnderstandingRow(understanding: QueryUnderstanding) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
    ) {
        understanding.service?.let {
            item { InputChip(selected = false, onClick = {}, label = { Text("Service: $it") }) }
        }
        understanding.category?.let {
            item { InputChip(selected = false, onClick = {}, label = { Text("Category: $it") }) }
        }
        understanding.location?.let {
            item { InputChip(selected = false, onClick = {}, label = { Text("Near: $it") }) }
        }
        understanding.maxPrice?.let {
            item { InputChip(selected = false, onClick = {}, label = { Text("≤ KES $it") }) }
        }
    }
}
