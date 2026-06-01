package must.kdroiders.hustlehub.ui.features.service.domain.usecase

import must.kdroiders.hustlehub.data.model.Service
import must.kdroiders.hustlehub.domain.repository.ServiceRepository
import javax.inject.Inject

class GetServiceByIdUseCase @Inject constructor(
    private val repository: ServiceRepository
) {
    suspend operator fun invoke(serviceId: String): Result<Service> =
        repository.getServiceById(serviceId)
}
