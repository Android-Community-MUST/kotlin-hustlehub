package must.kdroiders.hustlehub.ui.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.data.model.User
import must.kdroiders.hustlehub.data.model.UserRole
import must.kdroiders.hustlehub.data.repository.UserRepository
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.ui.features.auth.domain.usecase.CheckUserProfileUseCase
import must.kdroiders.hustlehub.ui.features.auth.domain.usecase.GoogleSignInUseCase
import must.kdroiders.hustlehub.ui.features.auth.domain.usecase.LoginUseCase
import timber.log.Timber
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
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences,
    private val firebaseAuth: FirebaseAuth?
) : ViewModel() {

    companion object {
        private val ALLOWED_DOMAINS = listOf("@must.ac.ke", "@students.must.ac.ke")
    }

    /** Returns true if the email belongs to an allowed MUST domain. */
    private fun isAllowedDomain(email: String): Boolean =
        ALLOWED_DOMAINS.any { email.endsWith(it) }

    private val _navigateToHome = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    /** Emits a single Boolean event (hasProfile) when Google sign-in completes successfully. */
    val navigateToHome: SharedFlow<Boolean> = _navigateToHome.asSharedFlow()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    /** Returns true if the backend has a registered profile for [firebaseUser]. */
    private suspend fun hasProfile(
        firebaseUser: com.google.firebase.auth.FirebaseUser
    ): Boolean = checkUserProfileUseCase(firebaseUser.uid).getOrDefault(false)

    /**
     * Persists lightweight user identity to DataStore after a successful login.
     *
     * Falls back to the Firebase user data if the backend profile fetch fails so
     * we always write something to DataStore.
     */
    private suspend fun persistUser(firebaseUser: com.google.firebase.auth.FirebaseUser) {
        try {
            val backendUser = userRepository.getUserProfile(firebaseUser.uid).getOrNull()
            val userToSave = backendUser ?: User(
                id = firebaseUser.uid,
                name = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                role = UserRole.CUSTOMER,
                profilePhotoUrl = firebaseUser.photoUrl?.toString() ?: ""
            )
            userPreferences.writeUser(userToSave)
        } catch (e: Exception) {
            Timber.e(e, "Failed to persist user to DataStore after login")
        }
    }

    fun login(
        onSuccess: (hasProfile: Boolean) -> Unit,
        onEmailNotVerified: (email: String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            loginUseCase(
                email = _uiState.value.email,
                password = _uiState.value.password
            ).fold(
                onSuccess = { result ->
                    if (result.isEmailVerified) {
                        val hasProfile = hasProfile(result.user)
                        persistUser(result.user)
                        _uiState.update { it.copy(isLoading = false) }
                        onSuccess(hasProfile)
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                        onEmailNotVerified(_uiState.value.email)
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Login failed. Please try again."
                        )
                    }
                }
            )
        }
    }

    fun signInWithGoogle(
        idToken: String,
        onSuccess: (hasProfile: Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            googleSignInUseCase(idToken).fold(
                onSuccess = { result ->
                    val email = result.user.email ?: ""

                    // Reject non-MUST email addresses immediately — sign out from Firebase
                    // so the user is not left in a broken authenticated state.
                    if (!isAllowedDomain(email)) {
                        firebaseAuth?.signOut()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Only @must.ac.ke or @students.must.ac.ke accounts " +
                                    "are allowed. Please use your MUST institutional email."
                            )
                        }
                        return@fold
                    }

                    val hasProfile = hasProfile(result.user)
                    persistUser(result.user)
                    _uiState.update { it.copy(isLoading = false) }
                    _navigateToHome.tryEmit(hasProfile)
                    onSuccess(hasProfile)
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Google sign-in failed. Please try again."
                        )
                    }
                }
            )
        }
    }
}
