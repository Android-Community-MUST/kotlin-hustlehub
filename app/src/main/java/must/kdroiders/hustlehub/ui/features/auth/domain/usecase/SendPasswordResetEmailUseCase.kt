package must.kdroiders.hustlehub.ui.features.auth.domain.usecase

import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class SendPasswordResetEmailUseCase
    @Inject
    constructor(
        private val authRepository: AuthRepository,
    ) {
        suspend operator fun invoke(email: String): Result<Unit> {
            return try {
                authRepository.sendPasswordResetEmail(email)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
