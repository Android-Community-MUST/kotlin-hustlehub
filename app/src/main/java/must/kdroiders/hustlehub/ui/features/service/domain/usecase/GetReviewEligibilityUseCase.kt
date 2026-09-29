package must.kdroiders.hustlehub.ui.features.service.domain.usecase

import must.kdroiders.hustlehub.ui.features.service.domain.model.ReviewEligibility
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ReviewRepository
import javax.inject.Inject

class GetReviewEligibilityUseCase
    @Inject
    constructor(private val repository: ReviewRepository) {
        suspend operator fun invoke(serviceId: String): Result<ReviewEligibility> =
            repository.getReviewEligibility(serviceId)
    }
