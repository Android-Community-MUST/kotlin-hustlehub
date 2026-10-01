package must.kdroiders.hustlehub.ui.features.service.domain.usecase

import must.kdroiders.hustlehub.ui.features.service.domain.repository.ReviewRepository
import javax.inject.Inject

class DeleteReviewUseCase
    @Inject
    constructor(private val repository: ReviewRepository) {
        suspend operator fun invoke(reviewId: String): Result<Unit> = repository.deleteReview(reviewId)
    }
