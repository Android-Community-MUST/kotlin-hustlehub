package must.kdroiders.hustlehub.ui.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
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

    private lateinit var mockAuth: FirebaseAuth
    private lateinit var viewModel: SignUpViewModel

    @Before
    fun setup() {
        mockAuth = mockk()
        viewModel = SignUpViewModel(mockAuth)
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
        assertEquals(PasswordStrength.WEAK, state.passwordStrength)
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
        // Weak: < 8 chars or just letters
        viewModel.onPasswordChanged("short")
        assertEquals(PasswordStrength.WEAK, viewModel.uiState.value.passwordStrength)
        
        viewModel.onPasswordChanged("onlylowercase")
        assertEquals(PasswordStrength.WEAK, viewModel.uiState.value.passwordStrength)

        // Medium: >= 8 chars, uppercase, number
        viewModel.onPasswordChanged("Password123")
        assertEquals(PasswordStrength.MEDIUM, viewModel.uiState.value.passwordStrength)

        // Strong: >= 10 chars, uppercase, number, special char
        viewModel.onPasswordChanged("StrongPass123!")
        assertEquals(PasswordStrength.STRONG, viewModel.uiState.value.passwordStrength)
    }

    @Test
    fun `signUp triggers validation errors on empty fields`() {
        viewModel.signUp()
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
        
        viewModel.signUp()
        val state = viewModel.uiState.value
        assertEquals("Must use a valid @must.ac.ke email", state.emailError)
    }

    @Test
    fun `signUp triggers validation error on weak password`() {
        viewModel.onNameChanged("John Doe")
        viewModel.onEmailChanged("john@must.ac.ke")
        viewModel.onPasswordChanged("weak") // < 8 chars
        viewModel.onConfirmPasswordChanged("weak")
        
        viewModel.signUp()
        val state = viewModel.uiState.value
        assertEquals("Password must be at least 8 characters", state.passwordError)

        viewModel.onPasswordChanged("alllowercase123") // No uppercase
        viewModel.onConfirmPasswordChanged("alllowercase123")
        viewModel.signUp()
        assertEquals("Password must contain at least 1 uppercase letter", viewModel.uiState.value.passwordError)
        
        viewModel.onPasswordChanged("ALLUPPERCASE") // No number
        viewModel.onConfirmPasswordChanged("ALLUPPERCASE")
        viewModel.signUp()
        assertEquals("Password must contain at least 1 number", viewModel.uiState.value.passwordError)
    }

    @Test
    fun `signUp triggers validation error on password mismatch`() {
        viewModel.onNameChanged("John Doe")
        viewModel.onEmailChanged("john@must.ac.ke")
        viewModel.onPasswordChanged("Password123!")
        viewModel.onConfirmPasswordChanged("Password1234") // Mismatch
        
        viewModel.signUp()
        val state = viewModel.uiState.value
        assertEquals("Passwords do not match", state.confirmPasswordError)
    }

    @Test
    fun `signUp succeeds with valid data`() {
        viewModel.onNameChanged("must")
        viewModel.onEmailChanged("john@must.ac.ke")
        viewModel.onPasswordChanged("Password123!")
        viewModel.onConfirmPasswordChanged("Password123!")

        val mockTask = mockk<Task<AuthResult>>()
        every { mockAuth.createUserWithEmailAndPassword("john@must.ac.ke", "Password123!") } returns mockTask
        every { mockTask.isComplete } returns true
        every { mockTask.exception } returns null
        every { mockTask.isCanceled } returns false
        every { mockTask.result } returns mockk()

        viewModel.signUp()
        
        val state = viewModel.uiState.value
        assertNull(state.nameError)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertNull(state.confirmPasswordError)
        assertEquals(false, state.isLoading)
        assertTrue(state.isSignUpSuccess)
    }

    @Test
    fun `signUp fails with exception`() {
        viewModel.onNameChanged("John Doe")
        viewModel.onEmailChanged("john@must.ac.ke")
        viewModel.onPasswordChanged("Password123!")
        viewModel.onConfirmPasswordChanged("Password123!")

        val mockTask = mockk<Task<AuthResult>>()
        val errorMessage = "Email already in use"
        every { mockAuth.createUserWithEmailAndPassword("john@must.ac.ke", "Password123!") } returns mockTask
        every { mockTask.isComplete } returns true
        every { mockTask.exception } returns RuntimeException(errorMessage)
        every { mockTask.isCanceled } returns false

        viewModel.signUp()
        
        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(errorMessage, state.signUpError)
    }
}
