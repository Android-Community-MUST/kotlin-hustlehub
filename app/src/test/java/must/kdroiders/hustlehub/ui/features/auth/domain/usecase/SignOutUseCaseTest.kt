package must.kdroiders.hustlehub.ui.features.auth.domain.usecase

import io.mockk.mockk
import io.mockk.verify
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthRepository
import org.junit.Before
import org.junit.Test

/**
 * Verifies that [SignOutUseCase] delegates to [AuthRepository.logout].
 */
class SignOutUseCaseTest {
    private lateinit var authRepository: AuthRepository
    private lateinit var signOutUseCase: SignOutUseCase

    @Before
    fun setup() {
        authRepository = mockk(relaxed = true)
        signOutUseCase = SignOutUseCase(authRepository)
    }

    @Test
    fun `should_call_authRepository_logout_when_invoked`() {
        signOutUseCase()
        verify(exactly = 1) { authRepository.logout() }
    }
}
