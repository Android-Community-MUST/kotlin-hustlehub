package must.kdroiders.hustlehub.ui.auth.domain.usecase

import com.google.firebase.auth.FirebaseUser
import must.kdroiders.hustlehub.ui.auth.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): FirebaseUser? {
        return authRepository.getCurrentUser()
    }
}
