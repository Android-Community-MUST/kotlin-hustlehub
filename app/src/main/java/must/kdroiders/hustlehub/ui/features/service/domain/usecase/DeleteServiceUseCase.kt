package must.kdroiders.hustlehub.ui.features.service.domain.usecase

import must.kdroiders.hustlehub.domain.repository.ServiceRepository
import javax.inject.Inject

class DeleteServiceUseCase
    @Inject
    constructor(
        private val repository: ServiceRepository,
    ) {
        suspend operator fun invoke(serviceId: String): Result<Unit> = repository.deleteService(serviceId)
    }
