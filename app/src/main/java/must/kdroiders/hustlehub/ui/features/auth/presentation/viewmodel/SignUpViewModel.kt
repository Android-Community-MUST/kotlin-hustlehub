package must.kdroiders.hustlehub.ui.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.ui.auth.domain.usecase.SignUpUseCase
import javax.inject.Inject

data class SignUpState(
    val name: String = "",
    val nameError: String? = null,
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val passwordStrength: PasswordStrength = PasswordStrength.WEAK,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val signUpError: String? = null
)

enum class PasswordStrength {
    WEAK, MEDIUM, STRONG
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpState())
    val uiState = _uiState.asStateFlow()

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChanged(password: String) {
        val strength = calculatePasswordStrength(password)
        _uiState.update { 
            it.copy(
                password = password, 
                passwordError = null,
                passwordStrength = strength
            ) 
        }
        // Re-validate confirm password if it's already filled
        if (_uiState.value.confirmPassword.isNotEmpty()) {
            validateConfirmPassword()
        }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
    }

    private fun calculatePasswordStrength(password: String): PasswordStrength {
        if (password.length < 8) return PasswordStrength.WEAK
        val hasUppercase = password.any { it.isUpperCase() }
        val hasNumber = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        
        return when {
            hasUppercase && hasNumber && hasSpecial && password.length >= 10 -> PasswordStrength.STRONG
            hasUppercase && hasNumber -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
    }

    private fun validateName(): Boolean {
        return if (_uiState.value.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name cannot be empty") }
            false
        } else {
            true
        }
    }

    private fun validateEmail(): Boolean {
        val email = _uiState.value.email
        return if (email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email cannot be empty") }
            false
        } else if (!email.endsWith("@must.ac.ke") && !email.endsWith("@students.must.ac.ke")) {
            _uiState.update { it.copy(emailError = "Must use a valid @must.ac.ke or @students.must.ac.ke email") }
            false
        } else {
            true
        }
    }

    private fun validatePassword(): Boolean {
        val password = _uiState.value.password
        return when {
            password.length < 8 -> {
                _uiState.update { it.copy(passwordError = "Password must be at least 8 characters") }
                false
            }
            !password.any { it.isUpperCase() } -> {
                _uiState.update { it.copy(passwordError = "Password must contain at least 1 uppercase letter") }
                false
            }
            !password.any { it.isDigit() } -> {
                _uiState.update { it.copy(passwordError = "Password must contain at least 1 number") }
                false
            }
            else -> true
        }
    }

    private fun validateConfirmPassword(): Boolean {
        return if (_uiState.value.password != _uiState.value.confirmPassword) {
            _uiState.update { it.copy(confirmPasswordError = "Passwords do not match") }
            false
        } else {
            true
        }
    }

    fun signUp(onSuccess: (email: String) -> Unit) {
        val isNameValid = validateName()
        val isEmailValid = validateEmail()
        val isPasswordValid = validatePassword()
        val isConfirmPasswordValid = validateConfirmPassword()

        if (isNameValid && isEmailValid && isPasswordValid && isConfirmPasswordValid) {
            _uiState.update { it.copy(isLoading = true, signUpError = null) }
            viewModelScope.launch {
                signUpUseCase(
                    name = _uiState.value.name,
                    email = _uiState.value.email,
                    password = _uiState.value.password
                ).onSuccess { result ->
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess(_uiState.value.email)
                }.onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            signUpError = e.message ?: "Sign-up failed. Please try again."
                        )
                    }
                }
            }
        }
    }
}
