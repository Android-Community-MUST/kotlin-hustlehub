package must.kdroiders.hustlehub.ui.auth.domain.usecase

import must.kdroiders.hustlehub.data.repository.AuthRepository
import javax.inject.Inject

class VerifyOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, otp: String): Result<Unit> {
        return runCatching {
            authRepository.verifyOtp(email, otp)
        }
    }
}
