package must.kdroiders.hustlehub.ui.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.data.repository.AuthRepository
import javax.inject.Inject

data class EmailVerificationUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val resendCooldown: Int = 0
)

@HiltViewModel
class EmailVerificationViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmailVerificationUiState())
    val uiState: StateFlow<EmailVerificationUiState> = _uiState.asStateFlow()

    // Email is passed from LoginScreen via nav and held here
    private var userEmail: String = ""

    fun setEmail(email: String) {
        userEmail = email
    }

    fun verifyOtp(otp: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                authRepository.verifyOtp(email = userEmail, otp = otp)
                onSuccess() // navigates to ProfileSetup
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Verification failed. Check your code."
                    )
                }
            }
        }
    }

    fun resendOtp() {
        viewModelScope.launch {
            try {
                authRepository.resendOtp(email = userEmail)
                // Start 60-second countdown
                for (i in 60 downTo 1) {
                    _uiState.update { it.copy(resendCooldown = i) }
                    delay(1000)
                }
                _uiState.update { it.copy(resendCooldown = 0) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Failed to resend OTP")
                }
            }
        }
    }
}
