package must.kdroiders.hustlehub.ui.features.service.domain.usecase

import must.kdroiders.hustlehub.ui.features.service.domain.model.Review
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ReviewRepository
import javax.inject.Inject

class GetMyReviewUseCase
    @Inject
    constructor(private val repository: ReviewRepository) {
        suspend operator fun invoke(serviceId: String): Result<Review?> = repository.getMyReviewForService(serviceId)
    }
