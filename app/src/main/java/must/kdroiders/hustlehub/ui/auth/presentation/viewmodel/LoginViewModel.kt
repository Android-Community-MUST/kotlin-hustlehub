package must.kdroiders.hustlehub.ui.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.ui.auth.domain.usecase.CheckUserProfileUseCase
import must.kdroiders.hustlehub.ui.auth.domain.usecase.GoogleSignInUseCase
import must.kdroiders.hustlehub.ui.auth.domain.usecase.LoginUseCase
import must.kdroiders.hustlehub.ui.auth.domain.usecase.SyncUserProfileUseCase
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val googleSignInUseCase: GoogleSignInUseCase,
    private val checkUserProfileUseCase: CheckUserProfileUseCase,
    private val syncUserProfileUseCase: SyncUserProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    private suspend fun checkAndSyncProfile(firebaseUser: com.google.firebase.auth.FirebaseUser): Boolean {
        val uid = firebaseUser.uid
        val hasProfile = checkUserProfileUseCase(uid).getOrDefault(false)
        if (!hasProfile) {
            val defaultUser = must.kdroiders.hustlehub.data.model.User(
                id = uid,
                name = firebaseUser.displayName ?: "Student",
                email = firebaseUser.email ?: "",
                phone = "",
                campusLocation = "",
                role = must.kdroiders.hustlehub.data.model.UserRole.CUSTOMER,
                profilePhotoUrl = firebaseUser.photoUrl?.toString() ?: "",
                bio = "",
                isVerified = false,
                isOnline = true
            )
            val result = syncUserProfileUseCase(defaultUser)
            return result.isSuccess
        }
        return true
    }

    fun login(
        onSuccess: () -> Unit,
        onEmailNotVerified: (email: String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            loginUseCase(
                email = _uiState.value.email,
                password = _uiState.value.password
            ).onSuccess { result ->
                if (result.isEmailVerified) {
                    val syncSuccess = checkAndSyncProfile(result.user)
                    _uiState.update { it.copy(isLoading = false) }
                    if (syncSuccess) {
                        onSuccess()
                    } else {
                        _uiState.update {
                            it.copy(errorMessage = "Failed to sync profile with database. Please try again.")
                        }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                    onEmailNotVerified(_uiState.value.email)
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Login failed. Please try again."
                    )
                }
            }
        }
    }

    fun signInWithGoogle(
        idToken: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            googleSignInUseCase(idToken)
                .onSuccess { result ->
                    val syncSuccess = checkAndSyncProfile(result.user)
                    _uiState.update { it.copy(isLoading = false) }
                    if (syncSuccess) {
                        onSuccess()
                    } else {
                        _uiState.update {
                            it.copy(errorMessage = "Failed to sync profile with database. Please try again.")
                        }
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Google sign-in failed. Please try again."
                        )
                    }
                }
        }
    }
}
