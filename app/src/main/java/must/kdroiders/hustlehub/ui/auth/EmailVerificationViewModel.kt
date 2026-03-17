package must.kdroiders.hustlehub.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmailVerificationState(
    val isLoading: Boolean = false,
    val isResendCooldown: Boolean = false,
    val cooldownSeconds: Int = 0,
    val resendError: String? = null,
    val resendSuccess: String? = null
)

@HiltViewModel
class EmailVerificationViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(EmailVerificationState())
    val uiState = _uiState.asStateFlow()

    private var cooldownJob: Job? = null

    fun resendVerificationEmail() {
        _uiState.update { it.copy(isLoading = true, resendError = null, resendSuccess = null) }

        // TODO: Call Firebase resend verification email
        // For now simulate success and start cooldown
        _uiState.update {
            it.copy(
                isLoading = false,
                resendSuccess = "Verification email resent successfully!"
            )
        }
        startCooldown()
    }

    private fun startCooldown(seconds: Int = 60) {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            _uiState.update { it.copy(isResendCooldown = true, cooldownSeconds = seconds) }
            for (remaining in seconds downTo 1) {
                _uiState.update { it.copy(cooldownSeconds = remaining) }
                delay(1000L)
            }
            _uiState.update { it.copy(isResendCooldown = false, cooldownSeconds = 0) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cooldownJob?.cancel()
    }
}
