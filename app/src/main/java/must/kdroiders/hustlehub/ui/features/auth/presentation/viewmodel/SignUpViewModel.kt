package must.kdroiders.hustlehub.ui.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.core.telemetry.HustleAnalytics
import must.kdroiders.hustlehub.core.telemetry.HustleCrashlytics
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.ui.features.auth.domain.usecase.SignUpUseCase
import must.kdroiders.hustlehub.ui.features.profile.domain.model.User
import timber.log.Timber
import javax.inject.Inject

data class SignUpState(
    val name: String = "",
    val nameError: String? = null,
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val passwordStrength: PasswordStrength = PasswordStrength.NONE,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val signUpError: String? = null,
)

enum class PasswordStrength {
    NONE,
    WEAK,
    MEDIUM,
    STRONG,
    VERY_STRONG,
}

@HiltViewModel
class SignUpViewModel
    @Inject
    constructor(
        private val signUpUseCase: SignUpUseCase,
        private val userPreferences: UserPreferences,
        private val hustleAnalytics: HustleAnalytics,
        private val hustleCrashlytics: HustleCrashlytics,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SignUpState())
        val uiState = _uiState.asStateFlow()

        init {
            hustleCrashlytics.setScreen("SignUpScreen")
        }

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
                    passwordStrength = strength,
                )
            }
            // Re-validate confirm password field if it has content
            if (_uiState.value.confirmPassword.isNotEmpty()) {
                validateConfirmPassword()
            }
        }

        fun onConfirmPasswordChanged(confirmPassword: String) {
            _uiState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
        }

        private fun calculatePasswordStrength(password: String): PasswordStrength {
            if (password.isEmpty()) return PasswordStrength.NONE
            var score = 0
            if (password.length >= 8) score++
            if (password.any { it.isUpperCase() }) score++
            if (password.any { it.isLowerCase() }) score++
            if (password.any { it.isDigit() }) score++
            if (password.any { !it.isLetterOrDigit() }) score++

            return when (score) {
                0, 1 -> PasswordStrength.NONE
                2 -> PasswordStrength.WEAK
                3 -> PasswordStrength.MEDIUM
                4 -> PasswordStrength.STRONG
                5 -> PasswordStrength.VERY_STRONG
                else -> PasswordStrength.NONE
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
            return when {
                email.isBlank() -> {
                    _uiState.update { it.copy(emailError = "Email cannot be empty") }
                    false
                }
                !email.endsWith("@must.ac.ke") && !email.endsWith("@students.must.ac.ke") -> {
                    _uiState.update {
                        it.copy(emailError = "Must use a valid @must.ac.ke or @students.must.ac.ke email")
                    }
                    false
                }
                else -> true
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
                _uiState.update { it.copy(confirmPasswordError = null) }
                true
            }
        }

        /**
         * Validates all fields, calls [SignUpUseCase], persists the user to DataStore,
         * and invokes [onSuccess] with the registered email to navigate to the
         * email-verification screen.
         */
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
                        password = _uiState.value.password,
                    ).fold(
                        onSuccess = { result ->
                            // Persist the Firebase user's minimal fields to DataStore so the app can
                            // display the name/avatar offline while the backend profile is created.
                            try {
                                val firebaseUser = result.user
                                // Build a minimal User from the Firebase record — the full backend
                                // profile will be fetched after email verification.
                                val user = User(
                                    id = firebaseUser.uid,
                                    name = _uiState.value.name,
                                    email = _uiState.value.email,
                                )
                                userPreferences.writeUser(user)
                                hustleAnalytics.logSignupCompleted("email")
                                hustleCrashlytics.setCrashlyticsUserContext(user.id, "SignUpScreen")
                            } catch (e: Exception) {
                                Timber.e(e, "Failed to persist user to DataStore after sign-up")
                            }

                            _uiState.update { it.copy(isLoading = false) }
                            onSuccess(_uiState.value.email)
                        },
                        onFailure = { e ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    signUpError = e.message ?: "Sign-up failed. Please try again.",
                                )
                            }
                        },
                    )
                }
            }
        }
    }
