package must.kdroiders.hustlehub.ui.features.auth.domain.usecase

import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Signs the current user out of Firebase Auth.
 *
 * Encapsulates the sign-out call behind a use-case boundary so that
 * ViewModels never hold a direct reference to [AuthRepository] just for
 * logout, keeping the dependency graph clean.
 */
class SignOutUseCase
    @Inject
    constructor(
        private val authRepository: AuthRepository,
    ) {
        /** Calls [AuthRepository.logout] to sign the user out immediately. */
        suspend operator fun invoke() {
            authRepository.logout()
        }
    }

