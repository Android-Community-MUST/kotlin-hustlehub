package must.kdroiders.hustlehub.ui.features.auth.domain.usecase

import must.kdroiders.hustlehub.data.repository.UserRepository
import javax.inject.Inject

class CheckUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<Boolean> {
        return userRepository.hasUserProfile(userId)
    }
}
