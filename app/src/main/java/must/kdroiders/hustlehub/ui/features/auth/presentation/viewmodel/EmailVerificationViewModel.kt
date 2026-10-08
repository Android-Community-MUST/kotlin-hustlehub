package must.kdroiders.hustlehub.ui.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.core.api.userFriendlyMessage
import must.kdroiders.hustlehub.ui.features.auth.domain.usecase.ResendOtpUseCase
import must.kdroiders.hustlehub.ui.features.auth.domain.usecase.SignOutUseCase
import must.kdroiders.hustlehub.ui.features.auth.domain.usecase.VerifyOtpUseCase
import timber.log.Timber
import javax.inject.Inject

data class EmailVerificationUiState(
    val isLoading: Boolean = false,
    val isVerified: Boolean = false,
    val errorMessage: String? = null,
    val resendCooldown: Int = 0,
)

@HiltViewModel
class EmailVerificationViewModel
    @Inject
    constructor(
        private val verifyOtpUseCase: VerifyOtpUseCase,
        private val resendOtpUseCase: ResendOtpUseCase,
        private val signOutUseCase: SignOutUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(EmailVerificationUiState())
        val uiState: StateFlow<EmailVerificationUiState> = _uiState.asStateFlow()

        private var userEmail: String = ""
        private var pollingJob: Job? = null

        fun setEmail(email: String) {
            userEmail = email
        }

        fun startAutoPolling(onSuccess: (() -> Unit)? = null) {
            pollingJob?.cancel()
            pollingJob = viewModelScope.launch {
                while (true) {
                    delay(POLLING_INTERVAL_MS)
                    if (!_uiState.value.isLoading && !_uiState.value.isVerified) {
                        verifyOtpUseCase(email = userEmail, otp = "")
                            .onSuccess {
                                _uiState.update { it.copy(isVerified = true, isLoading = false, errorMessage = null) }
                                pollingJob?.cancel()
                                onSuccess?.invoke()
                                return@launch
                            }.onFailure {
                                // Silently ignore periodic polling failure
                            }
                    }
                }
            }
        }

        fun stopAutoPolling() {
            pollingJob?.cancel()
            pollingJob = null
        }

        fun verifyOtp(
            otp: String = "",
            onSuccess: (() -> Unit)? = null,
        ) {
            if (_uiState.value.isLoading || _uiState.value.isVerified) return
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                verifyOtpUseCase(email = userEmail, otp = otp)
                    .onSuccess {
                        _uiState.update { it.copy(isVerified = true, isLoading = false, errorMessage = null) }
                        stopAutoPolling()
                        onSuccess?.invoke()
                    }.onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = e.userFriendlyMessage("Email not yet verified. Please check your inbox and tap the link."),
                            )
                        }
                    }
            }
        }

        // Called on resume — checks if Firebase verified the email.
        fun checkVerificationStatus(onSuccess: (() -> Unit)? = null) {
            if (_uiState.value.isLoading || _uiState.value.isVerified) return
            viewModelScope.launch {
                verifyOtpUseCase(email = userEmail, otp = "")
                    .onSuccess {
                        _uiState.update { it.copy(isVerified = true, isLoading = false, errorMessage = null) }
                        stopAutoPolling()
                        onSuccess?.invoke()
                    }.onFailure {
                        // Silently retain current state without clearing errors
                    }
            }
        }

        fun resendOtp() {
            if (_uiState.value.resendCooldown > 0 || _uiState.value.isLoading) return
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                resendOtpUseCase(email = userEmail)
                    .onSuccess {
                        _uiState.update { it.copy(isLoading = false) }
                        for (i in RESEND_COOLDOWN_SECONDS downTo 1) {
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

        fun signOut(onComplete: () -> Unit) {
            stopAutoPolling()
            viewModelScope.launch {
                try {
                    signOutUseCase()
                } catch (e: Exception) {
                    Timber.e(e, "Error signing out during email verification")
                } finally {
                    onComplete()
                }
            }
        }

        override fun onCleared() {
            super.onCleared()
            stopAutoPolling()
        }

        companion object {
            private const val POLLING_INTERVAL_MS = 4000L
            private const val RESEND_COOLDOWN_SECONDS = 60
        }
    }
