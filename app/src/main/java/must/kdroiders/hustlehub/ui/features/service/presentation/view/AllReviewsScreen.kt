package must.kdroiders.hustlehub.ui.features.service.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.sharedComposables.EmptyStateView
import must.kdroiders.hustlehub.sharedComposables.HustleBackButton
import must.kdroiders.hustlehub.sharedComposables.HustlePullToRefreshBox
import must.kdroiders.hustlehub.sharedComposables.HustleScaffold
import must.kdroiders.hustlehub.sharedComposables.LoadingIndicator
import must.kdroiders.hustlehub.ui.features.service.domain.model.Review
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.ReplyToReviewBottomSheet
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.ReviewItem
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.ReviewSummaryCard
import must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel.AllReviewsViewModel
import must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel.ReviewSortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllReviewsScreen(
    serviceId: String,
    viewModel: AllReviewsViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onNavigateToWriteReview: (
        serviceId: String,
        providerId: String,
        reviewId: String?,
        initialRating: Int?,
        initialComment: String?,
        initialIsAnonymous: Boolean?,
    ) -> Unit = { _, _, _, _, _, _ -> },
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    var reviewToDelete by remember { mutableStateOf<Review?>(null) }

    LaunchedEffect(serviceId) {
        viewModel.initialize(serviceId)
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo
                .lastOrNull()
                ?.index ?: 0
            totalItems > 0 && lastVisibleItemIndex >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && state.hasMore && !state.isLoadingMore && !state.isLoading) {
            viewModel.loadNextPage()
        }
    }

    HustleScaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.service_reviews_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.semantics { heading() },
                        )
                        if (state.service != null) {
                            Text(
                                text = state.service?.title ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    HustleBackButton(onClick = onBack)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        HustlePullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
        ) {
            if (state.isLoading && !state.isRefreshing) {
                LoadingIndicator(
                    modifier = Modifier.fillMaxSize(),
                )
            } else if (state.reviews.isEmpty() && !state.isLoading) {
                EmptyStateView(
                    title = stringResource(R.string.reviews_empty_title),
                    description = stringResource(R.string.reviews_empty_desc),
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (state.isFromCache) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudOff,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Text(
                                        text = "Viewing cached reviews (offline)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }

                    item {
                        ReviewSummaryCard(
                            averageRating = state.averageRating,
                            totalReviews = state.totalReviews,
                            ratingDistribution = state.ratingDistribution,
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.reviews_sort_by),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            ReviewSortOption.entries.forEach { option ->
                                val isSelected = option == state.sortOption
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            },
                                        ).clickable { viewModel.onSortOptionSelected(option) }
                                        .semantics {
                                            role = Role.RadioButton
                                            selected = isSelected
                                        }.padding(horizontal = 12.dp, vertical = 6.dp),
                                ) {
                                    Text(
                                        text = option.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                    )
                                }
                            }
                        }
                    }

                    itemsIndexed(
                        items = state.reviews,
                        key = { _, review -> review.id },
                    ) { index, review ->
                        val isAuthor = (state.myReviewId != null && state.myReviewId == review.id) ||
                            (!state.currentUserUuid.isNullOrBlank() && state.currentUserUuid == review.customerId) ||
                            (!state.currentUserId.isNullOrBlank() && state.currentUserId == review.customerId)

                        ReviewItem(
                            review = review,
                            showDivider = index < state.reviews.size - 1,
                            canReply = state.isOwnService,
                            providerName = state.service?.title,
                            isAuthor = isAuthor,
                            onReplyClick = { viewModel.onReplyClicked(review) },
                            onEditClick = {
                                onNavigateToWriteReview(
                                    review.serviceId,
                                    state.service?.providerId ?: "",
                                    review.id,
                                    review.rating,
                                    review.comment,
                                    review.isAnonymous,
                                )
                            },
                            onDeleteClick = {
                                reviewToDelete = review
                            },
                        )
                    }

                    if (state.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (reviewToDelete != null) {
        AlertDialog(
            onDismissRequest = { reviewToDelete = null },
            title = {
                Text(
                    text = stringResource(R.string.review_delete_dialog_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() },
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.review_delete_dialog_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = reviewToDelete?.id
                        reviewToDelete = null
                        if (id != null) {
                            viewModel.deleteReview(id)
                        }
                    },
                ) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { reviewToDelete = null }) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            },
        )
    }

    state.replyingReview?.let { review ->
        ReplyToReviewBottomSheet(
            review = review,
            isSubmitting = state.isSubmittingReply,
            onDismiss = viewModel::onDismissReplyDialog,
            onSubmitReply = { replyText ->
                viewModel.submitReply(review.id, replyText)
            },
        )
    }
}
