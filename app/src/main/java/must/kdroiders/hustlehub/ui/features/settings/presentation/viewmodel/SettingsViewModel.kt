package must.kdroiders.hustlehub.ui.features.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.data.local.dao.ServiceDao
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthRepository
import must.kdroiders.hustlehub.ui.features.auth.domain.usecase.SignOutUseCase
import timber.log.Timber
import javax.inject.Inject

// UI State

data class SettingsUiState(
    // User identity shown in the profile card
    val displayName: String = "",
    val username: String = "",
    val avatarUrl: String = "",
    val isVerified: Boolean = false,
    val paymentMethod: String = "M-Pesa",

    // Preferences
    val isDarkMode: Boolean = true,

    // App meta
    val appVersion: String = "2.4.0",
    val buildNumber: String = "2045",

    // Async states
    val isLoggingOut: Boolean = false,
    val error: String? = null,
)

// One-shot navigation events

sealed interface SettingsEvent {
    data object LoggedOut : SettingsEvent
    data object AccountDeleted : SettingsEvent
    data object NavigateToNotifications : SettingsEvent
    data object NavigateToPrivacy : SettingsEvent
    data object NavigateToChangePassword : SettingsEvent
    data object NavigateToVerification : SettingsEvent
    data object NavigateToPayments : SettingsEvent
    data object NavigateToHelp : SettingsEvent
    data object NavigateToReport : SettingsEvent
    data object NavigateToEditProfile : SettingsEvent
}

// ViewModel

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val signOutUseCase: SignOutUseCase,
        private val userPreferences: UserPreferences,
        private val serviceDao: ServiceDao,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SettingsUiState())
        val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

        private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
        val events = _events.receiveAsFlow()

        init {
            loadCurrentUser()
        }

        private fun loadCurrentUser() {
            val user = authRepository.getCurrentUser() ?: return
            val handle = user.displayName
                ?.replace(" ", "")
                ?.let { "@${it}_Hustler" }
                ?: user.email?.substringBefore("@")?.let { "@$it" }
                ?: "@HustleUser"

            _uiState.update {
                it.copy(
                    displayName = user.displayName ?: "Hustler",
                    username = handle,
                    avatarUrl = user.photoUrl?.toString() ?: "",
                    isVerified = user.isEmailVerified,
                )
            }
        }

        // Preferences

        fun onDarkModeToggled(enabled: Boolean) {
            _uiState.update { it.copy(isDarkMode = enabled) }
            // TODO: persist via DataStore — userPreferences.setDarkMode(enabled)
        }

        // Navigation triggers

        fun onEditProfileClicked() = emit(SettingsEvent.NavigateToEditProfile)
        fun onVerificationClicked() = emit(SettingsEvent.NavigateToVerification)
        fun onPaymentMethodsClicked() = emit(SettingsEvent.NavigateToPayments)
        fun onNotificationsClicked() = emit(SettingsEvent.NavigateToNotifications)
        fun onPrivacyClicked() = emit(SettingsEvent.NavigateToPrivacy)
        fun onChangePasswordClicked() = emit(SettingsEvent.NavigateToChangePassword)
        fun onHelpCenterClicked() = emit(SettingsEvent.NavigateToHelp)
        fun onReportProblemClicked() = emit(SettingsEvent.NavigateToReport)

        // Destructive actions

        /**
         * Signs the user out of Firebase Auth via [SignOutUseCase], clears the cached
         * user from DataStore, and emits [SettingsEvent.LoggedOut] to trigger navigation
         * back to the Login screen.
         */
        fun onLogOutClicked() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoggingOut = true, error = null) }
                try {
                    signOutUseCase()
                    userPreferences.clearUser()
                    serviceDao.clearAll()
                    _events.send(SettingsEvent.LoggedOut)
                } catch (e: Exception) {
                    Timber.e(e, "Sign out failed")
                    _uiState.update {
                        it.copy(isLoggingOut = false, error = "Sign out failed. Try again.")
                    }
                }
            }
        }

        fun onDeleteAccountClicked() {
            // TODO: call DELETE /api/v1/users/me — needs confirmation dialog first
            Timber.w("Delete account — not yet implemented")
        }

        // Helpers

        private fun emit(event: SettingsEvent) {
            viewModelScope.launch { _events.send(event) }
        }
    }
