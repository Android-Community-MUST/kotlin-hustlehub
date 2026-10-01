package must.kdroiders.hustlehub.ui.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.core.api.userFriendlyMessage
import must.kdroiders.hustlehub.ui.features.auth.domain.usecase.ResendOtpUseCase
import must.kdroiders.hustlehub.ui.features.auth.domain.usecase.VerifyOtpUseCase
import javax.inject.Inject

data class EmailVerificationUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val resendCooldown: Int = 0,
)

@HiltViewModel
class EmailVerificationViewModel
    @Inject
    constructor(
        private val verifyOtpUseCase: VerifyOtpUseCase,
        private val resendOtpUseCase: ResendOtpUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(EmailVerificationUiState())
        val uiState: StateFlow<EmailVerificationUiState> = _uiState.asStateFlow()

        private var userEmail: String = ""

        fun setEmail(email: String) {
            userEmail = email
        }

        fun verifyOtp(
            otp: String,
            onSuccess: () -> Unit,
        ) {
            if (_uiState.value.isLoading) return
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                verifyOtpUseCase(email = userEmail, otp = otp)
                    .onSuccess { onSuccess() }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = e.userFriendlyMessage("Email not yet verified. Please check your inbox."),
                            )
                        }
                    }
            }
        }

        // Called on each resume — silently checks if Firebase verified the email.
        fun checkVerificationStatus(onSuccess: () -> Unit) {
            if (_uiState.value.isLoading) return
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                verifyOtpUseCase(email = userEmail, otp = "")
                    .onSuccess { onSuccess() }
                    .onFailure { _uiState.update { it.copy(isLoading = false) } }
            }
        }

        fun resendOtp() {
            if (_uiState.value.resendCooldown > 0 || _uiState.value.isLoading) return
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                resendOtpUseCase(email = userEmail)
                    .onSuccess {
                        _uiState.update { it.copy(isLoading = false) }
                        for (i in 60 downTo 1) {
                            _uiState.update { it.copy(resendCooldown = i) }
                            delay(1000)
                        }
                        _uiState.update { it.copy(resendCooldown = 0) }
                    }.onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = e.userFriendlyMessage("Failed to resend verification email"),
                            )
                        }
                    }
            }
        }
    }
