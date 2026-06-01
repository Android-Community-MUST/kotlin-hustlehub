package must.kdroiders.hustlehub.ui.features.service.domain.usecase

import must.kdroiders.hustlehub.data.model.Service
import must.kdroiders.hustlehub.data.model.ServiceAvailability
import must.kdroiders.hustlehub.domain.repository.ServiceRepository
import javax.inject.Inject

class UpdateAvailabilityUseCase @Inject constructor(
    private val repository: ServiceRepository
) {
    suspend operator fun invoke(
        serviceId: String,
        availability: ServiceAvailability
    ): Result<Service> = repository.updateAvailability(serviceId, availability)
}
