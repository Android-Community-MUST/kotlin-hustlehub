package must.kdroiders.hustlehub.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SignUpViewModelTest {

    private lateinit var viewModel: SignUpViewModel

    @Before
    fun setup() {
        viewModel = SignUpViewModel()
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
        viewModel.onNameChanged("John Doe")
        viewModel.onEmailChanged("john@must.ac.ke")
        viewModel.onPasswordChanged("Password123!")
        viewModel.onConfirmPasswordChanged("Password123!")
        
        viewModel.signUp()
        val state = viewModel.uiState.value
        assertNull(state.nameError)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertNull(state.confirmPasswordError)
        assertEquals(true, state.isLoading) // Simulating successful validation leads to loading
    }
}
