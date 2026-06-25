package must.kdroiders.hustlehub.ui.features.profile.domain.usecase

import must.kdroiders.hustlehub.ui.features.profile.domain.model.User
import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
import javax.inject.Inject

class GetProviderProfileUseCase
    @Inject
    constructor(private val repository: UserRepository) {
        suspend operator fun invoke(providerId: String): Result<User?> = repository.getProviderProfile(providerId)
    }
