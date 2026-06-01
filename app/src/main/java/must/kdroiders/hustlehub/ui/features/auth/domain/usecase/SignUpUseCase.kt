package must.kdroiders.hustlehub.ui.features.auth.domain.usecase

import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthRepository
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.LoginResult
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(name: String, email: String, password: String): Result<LoginResult> {
        return runCatching {
            authRepository.signUp(name, email, password)
        }
    }
}
