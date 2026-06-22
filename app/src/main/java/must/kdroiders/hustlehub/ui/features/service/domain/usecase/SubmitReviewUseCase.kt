package must.kdroiders.hustlehub.ui.features.service.domain.usecase

import must.kdroiders.hustlehub.ui.features.service.domain.model.Review
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ServiceRepository
import javax.inject.Inject

class SubmitReviewUseCase
    @Inject
    constructor(private val repository: ServiceRepository) {
        suspend operator fun invoke(
            serviceId: String,
            rating: Int,
            comment: String? = null,
            isAnonymous: Boolean = false,
        ): Result<Review> = repository.submitReview(serviceId, rating, comment, isAnonymous)
    }
