package must.kdroiders.hustlehub.ui.auth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class LoginState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val loginError: String? = null,
    val isLoginSuccessful: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    private fun validateEmail(): Boolean {
        val email = _uiState.value.email
        return when {
            email.isBlank() -> {
                _uiState.update { it.copy(emailError = "Email cannot be empty") }
                false
            }
            !email.endsWith("@must.ac.ke") -> {
                _uiState.update { it.copy(emailError = "Must use a valid @must.ac.ke email") }
                false
            }
            else -> true
        }
    }

    private fun validatePassword(): Boolean {
        return if (_uiState.value.password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Password cannot be empty") }
            false
        } else {
            true
        }
    }

    fun login() {
        val isEmailValid = validateEmail()
        val isPasswordValid = validatePassword()

        if (isEmailValid && isPasswordValid) {
            _uiState.update { it.copy(isLoading = true, loginError = null) }
            // TODO: Implement actual login logic with Firebase Auth
        }
    }
}
