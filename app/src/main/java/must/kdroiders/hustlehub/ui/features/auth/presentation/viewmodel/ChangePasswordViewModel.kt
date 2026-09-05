package must.kdroiders.hustlehub.ui.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.core.api.userFriendlyMessage
import must.kdroiders.hustlehub.ui.features.auth.domain.usecase.ChangePasswordUseCase
import javax.inject.Inject

data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class ChangePasswordViewModel
    @Inject
    constructor(
        private val changePasswordUseCase: ChangePasswordUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ChangePasswordUiState())
        val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

        fun onCurrentPasswordChange(password: String) {
            _uiState.update { it.copy(currentPassword = password, errorMessage = null) }
        }

        fun onNewPasswordChange(password: String) {
            _uiState.update { it.copy(newPassword = password, errorMessage = null) }
        }

        fun onConfirmPasswordChange(password: String) {
            _uiState.update { it.copy(confirmPassword = password, errorMessage = null) }
        }

        fun getPasswordStrength(): PasswordStrength {
            val pwd = _uiState.value.newPassword
            return when {
                pwd.isEmpty() -> PasswordStrength.NONE
                pwd.length < 6 -> PasswordStrength.WEAK
                pwd.length >= 8 && pwd.any { it.isDigit() } && pwd.any { it.isUpperCase() } -> PasswordStrength.STRONG
                else -> PasswordStrength.MEDIUM
            }
        }

        fun changePassword(onSuccess: () -> Unit) {
            val state = _uiState.value
            if (state.currentPassword.isBlank() || state.newPassword.isBlank() || state.confirmPassword.isBlank()) {
                _uiState.update { it.copy(errorMessage = "All fields are required") }
                return
            }
            if (state.newPassword != state.confirmPassword) {
                _uiState.update { it.copy(errorMessage = "New passwords do not match") }
                return
            }
            if (state.newPassword.length < 6) {
                _uiState.update { it.copy(errorMessage = "New password must be at least 6 characters") }
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                changePasswordUseCase(state.currentPassword, state.newPassword).fold(
                    onSuccess = {
                        _uiState.update { it.copy(isLoading = false) }
                        onSuccess()
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = e.userFriendlyMessage("Failed to change password"),
                            )
                        }
                    },
                )
            }
        }
    }
