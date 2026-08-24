package must.kdroiders.hustlehub.ui.features.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import must.kdroiders.hustlehub.BuildConfig
import must.kdroiders.hustlehub.core.telemetry.HustleCrashlytics
import must.kdroiders.hustlehub.data.local.AppDatabase
import must.kdroiders.hustlehub.datastore.AppTheme
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthRepository
import must.kdroiders.hustlehub.ui.features.auth.domain.usecase.SignOutUseCase
import must.kdroiders.hustlehub.ui.features.chat.domain.repository.ChatRepository
import timber.log.Timber
import javax.inject.Inject

enum class DeleteAccountStep {
    NONE,
    WARNING,
    PASSWORD_INPUT,
}

data class SettingsUiState(
    // User identity shown in the profile card
    val displayName: String = "",
    val username: String = "",
    val avatarUrl: String = "",
    val isVerified: Boolean = false,
    val paymentMethod: String = "M-Pesa",

    // Preferences & Appearance
    val isDarkMode: Boolean = true,
    val selectedTheme: AppTheme = AppTheme.SYSTEM,
    val selectedLanguage: String = "English",

    // App meta
    val appVersion: String = BuildConfig.VERSION_NAME,
    val buildNumber: String = BuildConfig.VERSION_CODE.toString(),

    // Dialog & async states
    val showThemeDialog: Boolean = false,
    val deleteAccountStep: DeleteAccountStep = DeleteAccountStep.NONE,
    val deletePasswordInput: String = "",
    val deletePasswordError: String? = null,
    val isLoggingOut: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val error: String? = null,
) {
    val showDeleteAccountDialog: Boolean get() = deleteAccountStep != DeleteAccountStep.NONE
}

// One-shot navigation events

sealed interface SettingsEvent {
    data object LoggedOut : SettingsEvent
    data object AccountDeleted : SettingsEvent
    data object NavigateToNotifications : SettingsEvent
    data object NavigateToPrivacy : SettingsEvent
    data object NavigateToBlockedUsers : SettingsEvent
    data object NavigateToChangePassword : SettingsEvent
    data object NavigateToVerification : SettingsEvent
    data object NavigateToPayments : SettingsEvent
    data object NavigateToLanguage : SettingsEvent
    data object NavigateToHelp : SettingsEvent
    data object NavigateToContactUs : SettingsEvent
    data object NavigateToReport : SettingsEvent
    data object NavigateToEditProfile : SettingsEvent
    data object NavigateToTerms : SettingsEvent
    data object NavigateToPrivacyPolicy : SettingsEvent
    data object NavigateToLicenses : SettingsEvent
}

// ViewModel

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val signOutUseCase: SignOutUseCase,
        private val deleteAccountUseCase: must.kdroiders.hustlehub.ui.features.auth.domain.usecase.DeleteAccountUseCase,
        private val userPreferences: UserPreferences,
        private val appDatabase: AppDatabase,
        private val chatRepository: ChatRepository,
        private val hustleCrashlytics: HustleCrashlytics,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SettingsUiState())
        val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

        fun triggerTestCrash() {
            hustleCrashlytics.triggerTestCrash()
        }

        private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
        val events = _events.receiveAsFlow()

        init {
            loadCurrentUser()
            observeAppTheme()
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

        private fun observeAppTheme() {
            viewModelScope.launch {
                userPreferences.appTheme.collect { theme ->
                    _uiState.update {
                        it.copy(
                            selectedTheme = theme,
                            isDarkMode = theme == AppTheme.DARK,
                        )
                    }
                }
            }
        }

        // Preferences

        fun onThemeClicked() {
            _uiState.update { it.copy(showThemeDialog = true) }
        }

        fun onThemeDismissed() {
            _uiState.update { it.copy(showThemeDialog = false) }
        }

        fun onThemeSelected(theme: AppTheme) {
            _uiState.update {
                it.copy(
                    showThemeDialog = false,
                    selectedTheme = theme,
                    isDarkMode = theme == AppTheme.DARK,
                )
            }
            viewModelScope.launch {
                userPreferences.saveTheme(theme)
            }
        }

        fun onDarkModeToggled(enabled: Boolean) {
            val newTheme = if (enabled) AppTheme.DARK else AppTheme.LIGHT
            onThemeSelected(newTheme)
        }

        // Navigation triggers

        fun onEditProfileClicked() = emit(SettingsEvent.NavigateToEditProfile)
        fun onVerificationClicked() = emit(SettingsEvent.NavigateToVerification)
        fun onPaymentMethodsClicked() = emit(SettingsEvent.NavigateToPayments)
        fun onNotificationsClicked() = emit(SettingsEvent.NavigateToNotifications)
        fun onPrivacyClicked() = emit(SettingsEvent.NavigateToPrivacy)
        fun onBlockedUsersClicked() = emit(SettingsEvent.NavigateToBlockedUsers)
        fun onChangePasswordClicked() = emit(SettingsEvent.NavigateToChangePassword)
        fun onLanguageClicked() = emit(SettingsEvent.NavigateToLanguage)
        fun onHelpCenterClicked() = emit(SettingsEvent.NavigateToHelp)
        fun onContactUsClicked() = emit(SettingsEvent.NavigateToContactUs)
        fun onReportProblemClicked() = emit(SettingsEvent.NavigateToReport)
        fun onTermsOfServiceClicked() = emit(SettingsEvent.NavigateToTerms)
        fun onPrivacyPolicyClicked() = emit(SettingsEvent.NavigateToPrivacyPolicy)
        fun onLicensesClicked() = emit(SettingsEvent.NavigateToLicenses)

        // Destructive actions

        /**
         * Signs the user out of Firebase Auth via [SignOutUseCase], clears the cached
         * user from DataStore, disconnects active WebSockets, clears local Room database tables,
         * and emits [SettingsEvent.LoggedOut] to trigger navigation back to the Login screen.
         */
        fun onLogOutClicked() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoggingOut = true, error = null) }
                try {
                    runCatching { chatRepository.disconnectWebSocket() }
                    signOutUseCase()
                    userPreferences.clearUser()
                    withContext(Dispatchers.IO) {
                        appDatabase.clearAllTables()
                    }
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
            _uiState.update {
                it.copy(
                    deleteAccountStep = DeleteAccountStep.WARNING,
                    deletePasswordInput = "",
                    deletePasswordError = null,
                )
            }
        }

        fun onDeleteWarningConfirmed() {
            val currentUser = authRepository.getCurrentUser()
            val isPasswordUser = currentUser?.providerData?.any { it.providerId == "password" } ?: true

            if (isPasswordUser) {
                _uiState.update {
                    it.copy(
                        deleteAccountStep = DeleteAccountStep.PASSWORD_INPUT,
                        deletePasswordError = null,
                    )
                }
            } else {
                onDeleteAccountConfirmed()
            }
        }

        fun onDeletePasswordChanged(password: String) {
            _uiState.update {
                it.copy(
                    deletePasswordInput = password,
                    deletePasswordError = null,
                )
            }
        }

        fun onDeleteAccountDismissed() {
            _uiState.update {
                it.copy(
                    deleteAccountStep = DeleteAccountStep.NONE,
                    deletePasswordInput = "",
                    deletePasswordError = null,
                )
            }
        }

        fun onDeleteAccountConfirmed() {
            val currentUser = authRepository.getCurrentUser()
            val isPasswordUser = currentUser?.providerData?.any { it.providerId == "password" } ?: true
            val password = _uiState.value.deletePasswordInput.trim()

            if (isPasswordUser && password.isBlank()) {
                _uiState.update { it.copy(deletePasswordError = "Password is required to confirm deletion.") }
                return
            }

            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        deleteAccountStep = DeleteAccountStep.NONE,
                        isDeletingAccount = true,
                        error = null,
                    )
                }
                val result = deleteAccountUseCase(if (isPasswordUser) password else null)
                result.fold(
                    onSuccess = {
                        Timber.d("Account deletion successful")
                        _uiState.update { state -> state.copy(isDeletingAccount = false) }
                        _events.send(SettingsEvent.AccountDeleted)
                    },
                    onFailure = { throwable ->
                        Timber.e(throwable, "Account deletion failed")
                        _uiState.update { state ->
                            state.copy(
                                isDeletingAccount = false,
                                deleteAccountStep = if (isPasswordUser) DeleteAccountStep.PASSWORD_INPUT else DeleteAccountStep.NONE,
                                deletePasswordError = throwable.message ?: "Failed to delete account.",
                            )
                        }
                    },
                )
            }
        }

        // Helpers

        private fun emit(event: SettingsEvent) {
            viewModelScope.launch { _events.send(event) }
        }
    }
