package must.kdroiders.hustlehub.ui.features.service.domain.usecase

import must.kdroiders.hustlehub.ui.features.service.domain.repository.ReviewRepository
import javax.inject.Inject

class CheckDuplicateReviewUseCase
    @Inject
    constructor(private val repository: ReviewRepository) {
        suspend operator fun invoke(serviceId: String): Result<Boolean> = repository.checkDuplicateReview(serviceId)
    }
