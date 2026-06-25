package must.kdroiders.hustlehub.ui.features.profile.domain.usecase

import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service
import javax.inject.Inject

class GetServicesByProviderUseCase
    @Inject
    constructor(private val repository: UserRepository) {
        suspend operator fun invoke(providerId: String): Result<List<Service>> = repository.getServicesByProvider(providerId)
    }
