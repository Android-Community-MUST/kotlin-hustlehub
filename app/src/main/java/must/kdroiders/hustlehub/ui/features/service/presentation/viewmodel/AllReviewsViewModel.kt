package must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.ui.features.service.domain.model.Review
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetServiceByIdUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetServiceReviewsUseCase
import javax.inject.Inject

@HiltViewModel
class AllReviewsViewModel
    @Inject
    constructor(
        private val getServiceReviewsUseCase: GetServiceReviewsUseCase,
        private val getServiceByIdUseCase: GetServiceByIdUseCase,
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

        fun onSortOptionSelected(option: ReviewSortOption) {
            _uiState.update { state ->
                val sorted = sortReviews(rawReviews, option)
                state.copy(sortOption = option, reviews = sorted)
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

                val serviceDeferred = async { getServiceByIdUseCase(sid) }
                val reviewsDeferred = async { getServiceReviewsUseCase(sid, page = 0, size = 50) }

                val serviceResult = serviceDeferred.await()
                val reviewsResult = reviewsDeferred.await()

                val service = serviceResult.getOrNull()
                val pageResponse = reviewsResult.getOrNull()

                rawReviews = pageResponse?.content ?: emptyList()
                val sortedReviews = sortReviews(rawReviews, _uiState.value.sortOption)

                val avg = service?.averageRating?.toFloat() ?: 0f
                val count = pageResponse?.totalElements?.toInt() ?: service?.reviewCount ?: rawReviews.size

                _uiState.update {
                    it.copy(
                        service = service,
                        reviews = sortedReviews,
                        totalReviews = count,
                        averageRating = avg,
                        isLoading = false,
                        isRefreshing = false,
                        error = if (serviceResult.isFailure && reviewsResult.isFailure) "Failed to load reviews." else null,
                    )
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
