package must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel

import must.kdroiders.hustlehub.ui.features.service.domain.model.Review
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service

enum class ReviewSortOption(val label: String) {
    NEWEST("Newest"),
    HIGHEST("Highest Rating"),
    LOWEST("Lowest Rating"),
}

data class AllReviewsUiState(
    val service: Service? = null,
    val reviews: List<Review> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val totalReviews: Int = 0,
    val averageRating: Float = 0f,
    val sortOption: ReviewSortOption = ReviewSortOption.NEWEST,
    val error: String? = null,
)
