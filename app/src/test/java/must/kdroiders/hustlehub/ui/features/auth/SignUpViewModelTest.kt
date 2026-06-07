package must.kdroiders.hustlehub.ui.features.auth

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.ui.features.auth.domain.usecase.SignUpUseCase
import must.kdroiders.hustlehub.ui.features.auth.presentation.viewmodel.PasswordStrength
import must.kdroiders.hustlehub.ui.features.auth.presentation.viewmodel.SignUpViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule : TestWatcher() {
    private val testDispatcher = UnconfinedTestDispatcher()
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

class SignUpViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockSignUpUseCase: SignUpUseCase
    private lateinit var mockUserPreferences: UserPreferences
    private lateinit var viewModel: SignUpViewModel

    @Before
    fun setup() {
        mockSignUpUseCase = mockk()
        mockUserPreferences = mockk(relaxed = true)
        viewModel = SignUpViewModel(mockSignUpUseCase, mockUserPreferences)
    }

    @Test
    fun `initial state is empty`() {
        val state = viewModel.uiState.value
        assertEquals("", state.name)
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals("", state.confirmPassword)
        assertNull(state.nameError)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertNull(state.confirmPasswordError)
        assertEquals(PasswordStrength.NONE, state.passwordStrength)
    }

    @Test
    fun `valid email clears error and sets value`() {
        viewModel.onEmailChanged("student@must.ac.ke")
        val state = viewModel.uiState.value
        assertEquals("student@must.ac.ke", state.email)
        assertNull(state.emailError)
    }

    @Test
    fun `password strength is calculated correctly`() {
        // None/Very Weak: < 8 chars or just letters
        viewModel.onPasswordChanged("short")
        assertEquals(PasswordStrength.NONE, viewModel.uiState.value.passwordStrength)

        // Weak: length >= 8, only lowercase
        viewModel.onPasswordChanged("onlylowercase")
        assertEquals(PasswordStrength.WEAK, viewModel.uiState.value.passwordStrength)

        // Strong: >= 8 chars, uppercase, number
        viewModel.onPasswordChanged("Password123")
        assertEquals(PasswordStrength.STRONG, viewModel.uiState.value.passwordStrength)

        // Very Strong: >= 10 chars, uppercase, number, special char
        viewModel.onPasswordChanged("StrongPass123!")
        assertEquals(PasswordStrength.VERY_STRONG, viewModel.uiState.value.passwordStrength)
    }

    @Test
    fun `signUp triggers validation errors on empty fields`() {
        viewModel.signUp {}
        val state = viewModel.uiState.value
        assertEquals("Name cannot be empty", state.nameError)
        assertEquals("Email cannot be empty", state.emailError)
        assertEquals("Password must be at least 8 characters", state.passwordError)
    }

    @Test
    fun `signUp triggers validation error on invalid email domain`() {
        viewModel.onNameChanged("John Doe")
        viewModel.onEmailChanged("john@gmail.com")
        viewModel.onPasswordChanged("Password123")
        viewModel.onConfirmPasswordChanged("Password123")

        viewModel.signUp {}
        val state = viewModel.uiState.value
        assertEquals("Must use a valid @must.ac.ke or @students.must.ac.ke email", state.emailError)
    }

    @Test
    fun `signUp triggers validation error on weak password`() {
        viewModel.onNameChanged("John Doe")
        viewModel.onEmailChanged("john@must.ac.ke")
        viewModel.onPasswordChanged("weak") // < 8 chars
        viewModel.onConfirmPasswordChanged("weak")

        viewModel.signUp {}
        val state = viewModel.uiState.value
        assertEquals("Password must be at least 8 characters", state.passwordError)

        viewModel.onPasswordChanged("alllowercase123") // No uppercase
        viewModel.onConfirmPasswordChanged("alllowercase123")
        viewModel.signUp {}
        assertEquals("Password must contain at least 1 uppercase letter", viewModel.uiState.value.passwordError)

        viewModel.onPasswordChanged("ALLUPPERCASE") // No number
        viewModel.onConfirmPasswordChanged("ALLUPPERCASE")
        viewModel.signUp {}
        assertEquals("Password must contain at least 1 number", viewModel.uiState.value.passwordError)
    }

    @Test
    fun `signUp triggers validation error on password mismatch`() {
        viewModel.onNameChanged("John Doe")
        viewModel.onEmailChanged("john@must.ac.ke")
        viewModel.onPasswordChanged("Password123!")
        viewModel.onConfirmPasswordChanged("Password1234") // Mismatch

        viewModel.signUp {}
        val state = viewModel.uiState.value
        assertEquals("Passwords do not match", state.confirmPasswordError)
    }

    @Test
    fun `signUp succeeds with valid data`() {
        coEvery { mockSignUpUseCase(any(), any(), any()) } returns Result.success(mockk(relaxed = true))
        coEvery { mockUserPreferences.writeUser(any()) } just Runs

        viewModel.onNameChanged("John Doe")
        viewModel.onEmailChanged("john@must.ac.ke")
        viewModel.onPasswordChanged("Password123!")
        viewModel.onConfirmPasswordChanged("Password123!")

        viewModel.signUp {}
        val state = viewModel.uiState.value
        assertNull(state.nameError)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertNull(state.confirmPasswordError)
    }

    @Test
    fun `signUp persists user to DataStore on success`() = runTest {
        coEvery { mockSignUpUseCase(any(), any(), any()) } returns Result.success(mockk(relaxed = true))
        coEvery { mockUserPreferences.writeUser(any()) } just Runs

        viewModel.onNameChanged("John Doe")
        viewModel.onEmailChanged("john@must.ac.ke")
        viewModel.onPasswordChanged("Password123!")
        viewModel.onConfirmPasswordChanged("Password123!")

        viewModel.signUp {}
        
        advanceUntilIdle()

        coVerify { mockUserPreferences.writeUser(any()) }
    }

    @Test
    fun `signUp succeeds with valid student email domain`() {
        coEvery { mockSignUpUseCase(any(), any(), any()) } returns Result.success(mockk(relaxed = true))
        coEvery { mockUserPreferences.writeUser(any()) } just Runs

        viewModel.onNameChanged("John Doe")
        viewModel.onEmailChanged("student@students.must.ac.ke")
        viewModel.onPasswordChanged("Password123!")
        viewModel.onConfirmPasswordChanged("Password123!")

        viewModel.signUp {}
        val state = viewModel.uiState.value
        assertNull(state.nameError)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertNull(state.confirmPasswordError)
    }

    @Test
    fun `signUp shows error message on failure`() {
        coEvery { mockSignUpUseCase(any(), any(), any()) } returns
            Result.failure(Exception("An account with this email already exists. Try logging in instead."))

        viewModel.onNameChanged("Jane")
        viewModel.onEmailChanged("jane@must.ac.ke")
        viewModel.onPasswordChanged("Password123!")
        viewModel.onConfirmPasswordChanged("Password123!")

        viewModel.signUp {}
        assertEquals(
            "An account with this email already exists. Try logging in instead.",
            viewModel.uiState.value.signUpError
        )
    }
}
