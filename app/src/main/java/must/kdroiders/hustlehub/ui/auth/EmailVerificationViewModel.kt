package must.kdroiders.hustlehub.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class EmailVerificationUiState(
    val isLoading: Boolean = false,
    val isVerified: Boolean = false,
    val resendSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EmailVerificationViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmailVerificationUiState())
    val uiState: StateFlow<EmailVerificationUiState> = _uiState.asStateFlow()

    // Send verification email when screen opens
    init {
        sendVerificationEmail()
    }

    fun sendVerificationEmail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, resendSuccess = false) }
            try {
                auth.currentUser?.sendEmailVerification()?.await()
                _uiState.update { it.copy(isLoading = false, resendSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to send verification email"
                    )
                }
            }
        }
    }

    fun checkEmailVerified(otp: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // TODO: Firebase person to implement actual OTP verification here
                // For now we reload and check isEmailVerified
                auth.currentUser?.reload()?.await()
                val isVerified = auth.currentUser?.isEmailVerified == true
                if (isVerified) {
                    _uiState.update { it.copy(isLoading = false, isVerified = true) }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Invalid or expired code. Please try again."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Something went wrong"
                    )
                }
            }
        }
    }
}
