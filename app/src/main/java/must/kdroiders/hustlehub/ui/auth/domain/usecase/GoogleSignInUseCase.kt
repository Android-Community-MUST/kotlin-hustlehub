package must.kdroiders.hustlehub.ui.auth.domain.usecase

import must.kdroiders.hustlehub.data.repository.AuthRepository
import must.kdroiders.hustlehub.data.repository.LoginResult
import javax.inject.Inject

class GoogleSignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<LoginResult> {
        return runCatching {
            authRepository.signInWithGoogle(idToken)
        }
    }
}
