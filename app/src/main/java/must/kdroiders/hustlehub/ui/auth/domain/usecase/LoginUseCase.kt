package must.kdroiders.hustlehub.ui.auth.domain.usecase

import must.kdroiders.hustlehub.data.repository.AuthRepository
import must.kdroiders.hustlehub.data.repository.LoginResult
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<LoginResult> {
        return runCatching {
            authRepository.login(email, password)
        }
    }
}
