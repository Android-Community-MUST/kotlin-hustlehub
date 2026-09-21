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
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val currentPage: Int = 0,
    val totalReviews: Int = 0,
    val averageRating: Float = 0f,
    val ratingDistribution: IntArray = IntArray(5),
    val isFromCache: Boolean = false,
    val isOwnService: Boolean = false,
    val sortOption: ReviewSortOption = ReviewSortOption.NEWEST,
    val error: String? = null,
    val replyingReview: Review? = null,
    val isSubmittingReply: Boolean = false,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AllReviewsUiState

        if (service != other.service) return false
        if (reviews != other.reviews) return false
        if (isLoading != other.isLoading) return false
        if (isRefreshing != other.isRefreshing) return false
        if (isLoadingMore != other.isLoadingMore) return false
        if (hasMore != other.hasMore) return false
        if (currentPage != other.currentPage) return false
        if (totalReviews != other.totalReviews) return false
        if (averageRating != other.averageRating) return false
        if (!ratingDistribution.contentEquals(other.ratingDistribution)) return false
        if (isFromCache != other.isFromCache) return false
        if (isOwnService != other.isOwnService) return false
        if (sortOption != other.sortOption) return false
        if (error != other.error) return false
        if (replyingReview != other.replyingReview) return false
        if (isSubmittingReply != other.isSubmittingReply) return false

        return true
    }

    override fun hashCode(): Int {
        var result = service?.hashCode() ?: 0
        result = 31 * result + reviews.hashCode()
        result = 31 * result + isLoading.hashCode()
        result = 31 * result + isRefreshing.hashCode()
        result = 31 * result + isLoadingMore.hashCode()
        result = 31 * result + hasMore.hashCode()
        result = 31 * result + currentPage
        result = 31 * result + totalReviews
        result = 31 * result + averageRating.hashCode()
        result = 31 * result + ratingDistribution.contentHashCode()
        result = 31 * result + isFromCache.hashCode()
        result = 31 * result + isOwnService.hashCode()
        result = 31 * result + sortOption.hashCode()
        result = 31 * result + (error?.hashCode() ?: 0)
        result = 31 * result + (replyingReview?.hashCode() ?: 0)
        result = 31 * result + isSubmittingReply.hashCode()
        return result
    }
}

