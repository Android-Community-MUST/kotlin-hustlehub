package must.kdroiders.hustlehub.ui.features.auth.domain.usecase

import must.kdroiders.hustlehub.ui.features.profile.domain.model.User
import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
import javax.inject.Inject

class SyncUserProfileUseCase
    @Inject
    constructor(
        private val userRepository: UserRepository,
    ) {
        suspend operator fun invoke(user: User): Result<User> {
            return userRepository.saveUserProfile(user)
        }
    }
