package must.kdroiders.hustlehub.ui.features.home.domain.usecase

import must.kdroiders.hustlehub.core.api.PageResponse
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ServiceRepository
import javax.inject.Inject

class BrowseServicesUseCase
    @Inject
    constructor(
        private val repository: ServiceRepository,
    ) {
        suspend operator fun invoke(
            page: Int = 0,
            size: Int = 10,
            category: ServiceCategory? = null,
            query: String? = null,
        ): Result<PageResponse<Service>> = repository.browseServices(page = page, size = size, category = category, query = query)
    }
