package must.kdroiders.hustlehub.ui.features.auth.domain.usecase

import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthRepository
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.LoginResult
import javax.inject.Inject

class GoogleSignInUseCase
    @Inject
    constructor(
        private val authRepository: AuthRepository,
    ) {
        suspend operator fun invoke(idToken: String): Result<LoginResult> {
            return runCatching {
                authRepository.signInWithGoogle(idToken)
            }
        }
    }
