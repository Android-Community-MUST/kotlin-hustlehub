package must.kdroiders.hustlehub.ui.features.auth.domain.usecase

import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class ChangePasswordUseCase
    @Inject
    constructor(
        private val authRepository: AuthRepository,
    ) {
        suspend operator fun invoke(
            currentPassword: String,
            newPassword: String,
        ): Result<Unit> {
            return authRepository.changePassword(currentPassword, newPassword)
        }
    }
