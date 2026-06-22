package must.kdroiders.hustlehub.ui.features.service.domain.usecase

import must.kdroiders.hustlehub.core.api.PageResponse
import must.kdroiders.hustlehub.ui.features.service.domain.model.Review
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ServiceRepository
import javax.inject.Inject

class GetServiceReviewsUseCase
    @Inject
    constructor(private val repository: ServiceRepository) {
        suspend operator fun invoke(
            serviceId: String,
            page: Int = 0,
            size: Int = 10,
        ): Result<PageResponse<Review>> = repository.getServiceReviews(serviceId, page, size)
    }
