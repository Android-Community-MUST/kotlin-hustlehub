package must.kdroiders.hustlehub.ui.auth.domain.usecase

import must.kdroiders.hustlehub.data.repository.AuthRepository
import javax.inject.Inject

class ResendOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return runCatching {
            authRepository.resendOtp(email)
        }
    }
}
