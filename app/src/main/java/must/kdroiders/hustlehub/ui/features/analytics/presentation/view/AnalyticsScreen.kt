package must.kdroiders.hustlehub.ui.features.analytics.presentation.view

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.MiscellaneousServices
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import must.kdroiders.hustlehub.sharedComposables.HustleScaffold
import must.kdroiders.hustlehub.sharedComposables.LoadingIndicator
import must.kdroiders.hustlehub.sharedComposables.ProBadge
import must.kdroiders.hustlehub.ui.features.analytics.presentation.view.charts.BarChart
import must.kdroiders.hustlehub.ui.features.analytics.presentation.view.charts.RatingDistributionChart
import must.kdroiders.hustlehub.ui.features.analytics.presentation.view.components.AnimatedStatCard
import must.kdroiders.hustlehub.ui.features.analytics.presentation.view.components.StatCard
import must.kdroiders.hustlehub.ui.features.analytics.presentation.view.components.TransactionItem
import must.kdroiders.hustlehub.ui.features.analytics.presentation.viewmodel.AnalyticsTab
import must.kdroiders.hustlehub.ui.features.analytics.presentation.viewmodel.AnalyticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.state.collectAsState()

    HustleScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Analytics Dashboard")
                        ProBadge(isVisible = true)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                LoadingIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error ?: "Something went wrong",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = viewModel::loadAnalytics) {
                            Text("Retry")
                        }
                    }
                }
            }
            else -> {
                val analytics = uiState.analytics ?: return@HustleScaffold

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // tab selector
                    item(key = "tabs") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FilterChip(
                                selected = uiState.selectedTab == AnalyticsTab.OVERVIEW,
                                onClick = { viewModel.selectTab(AnalyticsTab.OVERVIEW) },
                                label = { Text("Overview") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.BarChart,
                                        contentDescription = null,
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                            )
                            FilterChip(
                                selected = uiState.selectedTab == AnalyticsTab.PAYMENTS,
                                onClick = { viewModel.selectTab(AnalyticsTab.PAYMENTS) },
                                label = { Text("Payment History") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                            )
                        }
                    }

                    when (uiState.selectedTab) {
                        AnalyticsTab.OVERVIEW -> {
                            // stat cards row 1
                            item(key = "stats_row1") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    AnimatedStatCard(
                                        icon = Icons.Default.MiscellaneousServices,
                                        targetValue = analytics.totalServices,
                                        label = "Services",
                                        modifier = Modifier.weight(1f),
                                    )
                                    StatCard(
                                        icon = Icons.Default.RateReview,
                                        value = String.format("%.1f", analytics.averageRating),
                                        label = "Avg Rating",
                                        modifier = Modifier.weight(1f),
                                    )
                                    AnimatedStatCard(
                                        icon = Icons.Default.QuestionAnswer,
                                        targetValue = analytics.totalInquiries.toInt(),
                                        label = "Inquiries",
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }

                            // stat cards row 2
                            item(key = "stats_row2") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    AnimatedStatCard(
                                        icon = Icons.Default.RemoveRedEye,
                                        targetValue = analytics.totalProfileViews.toInt(),
                                        label = "Profile Views",
                                        modifier = Modifier.weight(1f),
                                    )
                                    AnimatedStatCard(
                                        icon = Icons.Default.Search,
                                        targetValue = analytics.totalSearchImpressions.toInt(),
                                        label = "Search Hits",
                                        modifier = Modifier.weight(1f),
                                    )
                                    AnimatedStatCard(
                                        icon = Icons.Default.RateReview,
                                        targetValue = analytics.totalReviews.toInt(),
                                        label = "Reviews",
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }

                            // inquiry bar chart
                            item(key = "inquiry_chart") {
                                BarChart(
                                    data = analytics.weeklyInquiries,
                                    title = "Weekly Inquiries",
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }

                            // views bar chart
                            item(key = "views_chart") {
                                BarChart(
                                    data = analytics.weeklyViews,
                                    title = "Weekly Profile Views",
                                    modifier = Modifier.fillMaxWidth(),
                                    barColor = MaterialTheme.colorScheme.tertiary,
                                )
                            }

                            // rating distribution
                            item(key = "rating_dist") {
                                val dist = analytics.ratingDistribution.mapKeys { (k, _) -> k.toIntOrNull() ?: 0 }
                                RatingDistributionChart(
                                    distribution = dist.mapValues { it.value },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }

                        AnalyticsTab.PAYMENTS -> {
                            // payment stat cards
                            item(key = "payment_stats") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    StatCard(
                                        icon = Icons.Default.MonetizationOn,
                                        value = "KES ${analytics.totalPayments}",
                                        label = "Total Payments",
                                        modifier = Modifier.weight(1f),
                                    )
                                    AnimatedStatCard(
                                        icon = Icons.Default.BarChart,
                                        targetValue = analytics.transactionCount.toInt(),
                                        label = "Transactions",
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }

                            item(key = "monthly_payment") {
                                StatCard(
                                    icon = Icons.Default.MonetizationOn,
                                    value = "KES ${analytics.monthlyPayments}",
                                    label = "Last 30 Days",
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }

                            // transactions header
                            item(key = "tx_header") {
                                Text(
                                    text = "Recent Transactions",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            if (analytics.recentTransactions.isEmpty()) {
                                item(key = "tx_empty") {
                                    Text(
                                        text = "No transactions yet",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            } else {
                                itemsIndexed(
                                    analytics.recentTransactions,
                                    key = { index, tx -> "${tx.date}_${tx.amount}_${tx.type}_$index" },
                                ) { _, tx ->
                                    TransactionItem(transaction = tx)
                                }
                            }
                        }
                    }

                    // bottom spacer
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}
