package must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthRepository
import must.kdroiders.hustlehub.ui.features.profile.domain.usecase.GetProviderProfileUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.model.Review
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ReviewRepository
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.DeleteReviewUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetMyReviewUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetServiceByIdUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetServiceReviewsUseCase
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
@Suppress("LongParameterList")
class AllReviewsViewModel
    @Inject
    constructor(
        private val getServiceReviewsUseCase: GetServiceReviewsUseCase,
        private val getServiceByIdUseCase: GetServiceByIdUseCase,
        private val getMyReviewUseCase: GetMyReviewUseCase,
        private val deleteReviewUseCase: DeleteReviewUseCase,
        private val reviewRepository: ReviewRepository,
        private val authRepository: AuthRepository,
        private val userPreferences: UserPreferences,
        private val getProviderProfileUseCase: GetProviderProfileUseCase,
    ) : ViewModel() {
        private var serviceId: String? = null
        private var rawReviews: List<Review> = emptyList()

        private val _uiState = MutableStateFlow(AllReviewsUiState())
        val uiState: StateFlow<AllReviewsUiState> = _uiState.asStateFlow()

        fun initialize(id: String) {
            if (serviceId == id) return
            serviceId = id
            loadData(isRefresh = false)
        }

        fun refresh() {
            loadData(isRefresh = true)
        }

        fun loadNextPage() {
            val sid = serviceId ?: return
            val state = _uiState.value
            if (state.isLoading || state.isLoadingMore || !state.hasMore) return

            val nextPage = state.currentPage + 1
            viewModelScope.launch {
                _uiState.update { it.copy(isLoadingMore = true) }
                val result = getServiceReviewsUseCase(sid, page = nextPage, size = 20)
                result.onSuccess { pageResponse ->
                    val newItems = pageResponse.content
                    val existingIds = rawReviews.map { it.id }.toSet()
                    val filteredNew = newItems.filterNot { it.id in existingIds }
                    rawReviews = rawReviews + filteredNew
                    val sortedReviews = sortReviews(rawReviews, _uiState.value.sortOption)
                    val hasMore = (pageResponse.page + 1) < pageResponse.totalPages
                    _uiState.update {
                        it.copy(
                            reviews = sortedReviews,
                            currentPage = nextPage,
                            hasMore = hasMore,
                            isLoadingMore = false,
                        )
                    }
                }.onFailure { e ->
                    Timber.w(e, "AllReviewsViewModel.loadNextPage failed for page=$nextPage")
                    _uiState.update { it.copy(isLoadingMore = false) }
                }
            }
        }

        fun onSortOptionSelected(option: ReviewSortOption) {
            _uiState.update { state ->
                val sorted = sortReviews(rawReviews, option)
                state.copy(sortOption = option, reviews = sorted)
            }
        }

        fun onReplyClicked(review: Review) {
            _uiState.update { it.copy(replyingReview = review) }
        }

        fun onDismissReplyDialog() {
            _uiState.update { it.copy(replyingReview = null) }
        }

        fun submitReply(reviewId: String, replyText: String) {
            if (replyText.isBlank()) return
            viewModelScope.launch {
                _uiState.update { it.copy(isSubmittingReply = true) }
                val result = reviewRepository.replyToReview(reviewId, replyText.trim())
                result.onSuccess { updatedReview ->
                    rawReviews = rawReviews.map { if (it.id == reviewId) updatedReview else it }
                    val sorted = sortReviews(rawReviews, _uiState.value.sortOption)
                    _uiState.update {
                        it.copy(
                            reviews = sorted,
                            replyingReview = null,
                            isSubmittingReply = false,
                        )
                    }
                }.onFailure { e ->
                    Timber.e(e, "Failed to submit reply to review $reviewId")
                    _uiState.update {
                        it.copy(
                            isSubmittingReply = false,
                            error = e.message ?: "Failed to post reply. Please try again.",
                        )
                    }
                }
            }
        }

        fun clearError() {
            _uiState.update { it.copy(error = null) }
        }

        private fun loadData(isRefresh: Boolean) {
            val sid = serviceId ?: return
            viewModelScope.launch {
                if (isRefresh) {
                    _uiState.update { it.copy(isRefreshing = true, error = null) }
                } else {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                }

                val currentUid = authRepository.getCurrentUser()?.uid
                val cachedUser = userPreferences.cachedUser.firstOrNull()
                val serviceDeferred = async { getServiceByIdUseCase(sid) }
                val reviewsDeferred = async { getServiceReviewsUseCase(sid, page = 0, size = 20) }
                val distDeferred = async { reviewRepository.getRatingDistribution(sid) }
                val myReviewDeferred = async { getMyReviewUseCase(sid) }

                val serviceResult = serviceDeferred.await()
                val reviewsResult = reviewsDeferred.await()
                val distResult = distDeferred.await()
                val myReview = myReviewDeferred.await().getOrNull()

                val service = serviceResult.getOrNull()
                val pageResponse = reviewsResult.getOrNull()
                val dist = distResult.getOrNull()

                val isOwn = if (currentUid != null && service != null) {
                    if (currentUid == service.providerId) {
                        true
                    } else {
                        val provider = getProviderProfileUseCase(service.providerId).getOrNull()
                        provider != null && (currentUid == provider.id || currentUid == provider.uuid)
                    }
                } else {
                    false
                }

                rawReviews = pageResponse?.content ?: emptyList()
                val sortedReviews = sortReviews(rawReviews, _uiState.value.sortOption)

                val avg = dist?.averageRating ?: service?.averageRating ?: 0f
                val count = dist?.totalReviews ?: pageResponse?.totalElements?.toInt() ?: service?.reviewCount ?: rawReviews.size
                val distribution = if (dist != null) {
                    intArrayOf(dist.count1Stars, dist.count2Stars, dist.count3Stars, dist.count4Stars, dist.count5Stars)
                } else {
                    val counts = IntArray(5)
                    for (r in rawReviews) {
                        val star = r.rating.coerceIn(1, 5)
                        counts[star - 1]++
                    }
                    counts
                }

                val hasMore = pageResponse?.let { (it.page + 1) < it.totalPages } ?: false
                val isCache = distResult.isFailure && reviewsResult.isSuccess

                _uiState.update {
                    it.copy(
                        service = service,
                        reviews = sortedReviews,
                        totalReviews = count,
                        averageRating = avg,
                        ratingDistribution = distribution,
                        isOwnService = isOwn,
                        isFromCache = isCache,
                        currentPage = 0,
                        hasMore = hasMore,
                        isLoading = false,
                        isRefreshing = false,
                        currentUserId = cachedUser?.id ?: currentUid,
                        currentUserUuid = cachedUser?.uuid,
                        myReviewId = myReview?.id,
                        error = if (serviceResult.isFailure && reviewsResult.isFailure) "Failed to load reviews." else null,
                    )
                }
            }
        }

        fun deleteReview(reviewId: String) {
            viewModelScope.launch {
                deleteReviewUseCase(reviewId)
                    .onSuccess {
                        rawReviews = rawReviews.filterNot { it.id == reviewId }
                        val sorted = sortReviews(rawReviews, _uiState.value.sortOption)
                        val newCount = (_uiState.value.totalReviews - 1).coerceAtLeast(0)
                        _uiState.update {
                            it.copy(
                                reviews = sorted,
                                totalReviews = newCount,
                                myReviewId = if (it.myReviewId == reviewId) null else it.myReviewId,
                            )
                        }
                        refresh()
                    }.onFailure { e ->
                        Timber.e(e, "AllReviewsViewModel.deleteReview failed for reviewId=$reviewId")
                        _uiState.update {
                            it.copy(error = e.message ?: "Failed to delete review. Please try again.")
                        }
                    }
            }
        }

        private fun sortReviews(
            reviews: List<Review>,
            option: ReviewSortOption,
        ): List<Review> {
            return when (option) {
                ReviewSortOption.NEWEST -> reviews.sortedByDescending { it.createdAt }
                ReviewSortOption.HIGHEST -> reviews.sortedWith(compareByDescending<Review> { it.rating }.thenByDescending { it.createdAt })
                ReviewSortOption.LOWEST -> reviews.sortedWith(compareBy<Review> { it.rating }.thenByDescending { it.createdAt })
            }
        }
    }

