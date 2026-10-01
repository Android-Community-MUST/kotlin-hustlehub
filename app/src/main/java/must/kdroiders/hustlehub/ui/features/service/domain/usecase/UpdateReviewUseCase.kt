package must.kdroiders.hustlehub.ui.features.service.domain.usecase

import must.kdroiders.hustlehub.ui.features.service.domain.model.Review
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ReviewRepository
import javax.inject.Inject

class UpdateReviewUseCase
    @Inject
    constructor(private val repository: ReviewRepository) {
        suspend operator fun invoke(
            reviewId: String,
            rating: Int,
            comment: String? = null,
            isAnonymous: Boolean = false,
        ): Result<Review> = repository.updateReview(reviewId, rating, comment, isAnonymous)
    }
