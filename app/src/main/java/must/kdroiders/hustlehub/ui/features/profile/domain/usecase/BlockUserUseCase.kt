package must.kdroiders.hustlehub.ui.features.profile.domain.usecase

import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Encapsulates the business logic for blocking a user.
 */
class BlockUserUseCase
    @Inject
    constructor(
        private val userRepository: UserRepository,
    ) {
        suspend operator fun invoke(targetId: String): Result<Unit> {
            val trimmedId = targetId.trim()
            if (trimmedId.isBlank()) {
                return Result.failure(IllegalArgumentException("Target user ID cannot be blank"))
            }
            return userRepository.blockUser(trimmedId)
        }
    }
