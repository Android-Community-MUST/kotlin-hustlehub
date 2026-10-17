package must.kdroiders.hustlehub.ui.features.settings.presentation.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
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
import must.kdroiders.hustlehub.core.api.userFriendlyMessage
import must.kdroiders.hustlehub.core.auth.AdminAuthUtils
import must.kdroiders.hustlehub.core.review.AppReviewManager
import must.kdroiders.hustlehub.core.telemetry.HustleCrashlytics
import must.kdroiders.hustlehub.data.local.AppDatabase
import must.kdroiders.hustlehub.datastore.AppTheme
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthRepository
import must.kdroiders.hustlehub.ui.features.auth.domain.usecase.DeleteAccountUseCase
import must.kdroiders.hustlehub.ui.features.auth.domain.usecase.SignOutUseCase
import must.kdroiders.hustlehub.ui.features.chat.domain.repository.ChatRepository
import must.kdroiders.hustlehub.ui.features.profile.domain.model.UserRole
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetMyServicesUseCase
import timber.log.Timber
import javax.inject.Inject

enum class DeleteAccountStep {
    NONE,
    WARNING,
    PASSWORD_INPUT,
}

data class SettingsUiState(
    val displayName: String = "",
    val username: String = "",
    val avatarUrl: String = "",
    val isVerified: Boolean = false,
    val isProvider: Boolean = false,
    val isProUser: Boolean = false,
    val isDarkMode: Boolean = true,
    val selectedTheme: AppTheme = AppTheme.SYSTEM,
    val appVersion: String = BuildConfig.VERSION_NAME,
    val buildNumber: String = BuildConfig.VERSION_CODE.toString(),
    val showThemeDialog: Boolean = false,
    val showProviderEducationDialog: Boolean = false,
    val deleteAccountStep: DeleteAccountStep = DeleteAccountStep.NONE,
    val deletePasswordInput: String = "",
    val deletePasswordError: String? = null,
    val isLoggingOut: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val error: String? = null,
) {
    val showDeleteAccountDialog: Boolean get() = deleteAccountStep != DeleteAccountStep.NONE
}

sealed interface SettingsEvent {
    data object LoggedOut : SettingsEvent
    data object AccountDeleted : SettingsEvent
    data class ShowError(val message: String) : SettingsEvent
    data object NavigateToNotifications : SettingsEvent
    data object NavigateToPrivacy : SettingsEvent
    data object NavigateToBlockedUsers : SettingsEvent
    data object NavigateToChangePassword : SettingsEvent
    data object NavigateToSubscription : SettingsEvent
    data object NavigateToHelp : SettingsEvent
    data object NavigateToContactUs : SettingsEvent
    data object NavigateToReport : SettingsEvent
    data object NavigateToEditProfile : SettingsEvent
    data object NavigateToTerms : SettingsEvent
    data object NavigateToPrivacyPolicy : SettingsEvent
    data object TriggerRateApp : SettingsEvent
}

@HiltViewModel
@Suppress("LongParameterList")
class SettingsViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val signOutUseCase: SignOutUseCase,
        private val deleteAccountUseCase: DeleteAccountUseCase,
        private val userPreferences: UserPreferences,
        private val appDatabase: AppDatabase,
        private val chatRepository: ChatRepository,
        private val hustleCrashlytics: HustleCrashlytics,
        private val appReviewManager: AppReviewManager,
        private val getMyServicesUseCase: GetMyServicesUseCase,
    ) : ViewModel() {
        internal var ioDispatcher: CoroutineDispatcher = Dispatchers.IO

        private val _uiState = MutableStateFlow(SettingsUiState())
        val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

        fun triggerTestCrash() {
            hustleCrashlytics.triggerTestCrash()
        }

        private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
        val events = _events.receiveAsFlow()

        init {
            loadCurrentUser()
            observeCurrentUser()
            observeAppTheme()
            observeProStatus()
            checkServicesForProviderStatus()
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

        private fun observeCurrentUser() {
            viewModelScope.launch {
                userPreferences.cachedUser.collect { cached ->
                    val firebaseUser = authRepository.getCurrentUser()
                    val name = cached.name.ifBlank { firebaseUser?.displayName.orEmpty() }.ifBlank { "Hustler" }
                    val handle = name
                        .replace(" ", "")
                        .let { "@${it}_Hustler" }
                    val avatar = cached.profilePhotoUrl.ifBlank { firebaseUser?.photoUrl?.toString().orEmpty() }
                    val isVerified = firebaseUser?.isEmailVerified == true || AdminAuthUtils.isAuthorizedAdmin(firebaseUser?.email)
                    val isProviderRole = cached.role == UserRole.ROLE_PROVIDER ||
                        cached.role == UserRole.ROLE_BOTH ||
                        cached.role == UserRole.ROLE_ADMIN ||
                        cached.role == UserRole.ROLE_SUPER_ADMIN
                    val isVerified = firebaseUser?.isEmailVerified == true

                    _uiState.update {
                        it.copy(
                            displayName = name,
                            username = handle,
                            avatarUrl = avatar,
                            isVerified = isVerified,
                            isProvider = it.isProvider || isProviderRole,
                        )
                    }
                }
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

        private fun observeProStatus() {
            viewModelScope.launch {
                userPreferences.isProUser.collect { isPro ->
                    _uiState.update { it.copy(isProUser = isPro) }
                }
            }
        }

        private fun checkServicesForProviderStatus() {
            viewModelScope.launch {
                runCatching {
                    val services = getMyServicesUseCase().getOrNull().orEmpty()
                    if (services.isNotEmpty()) {
                        _uiState.update { it.copy(isProvider = true) }
                    }
                }
            }
        }

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

        fun onSubscriptionClicked() {
            val state = _uiState.value
            if (state.isProvider || state.isProUser) {
                emit(SettingsEvent.NavigateToSubscription)
            } else {
                _uiState.update { it.copy(showProviderEducationDialog = true) }
            }
        }

        fun onDismissProviderEducationDialog() {
            _uiState.update { it.copy(showProviderEducationDialog = false) }
        }

        fun onContinueToSubscriptionFromDialog() {
            _uiState.update { it.copy(showProviderEducationDialog = false) }
            emit(SettingsEvent.NavigateToSubscription)
        }

        fun onEditProfileClicked() = emit(SettingsEvent.NavigateToEditProfile)
        fun onNotificationsClicked() = emit(SettingsEvent.NavigateToNotifications)
        fun onPrivacyClicked() = emit(SettingsEvent.NavigateToPrivacy)
        fun onBlockedUsersClicked() = emit(SettingsEvent.NavigateToBlockedUsers)
        fun onChangePasswordClicked() = emit(SettingsEvent.NavigateToChangePassword)
        fun onHelpCenterClicked() = emit(SettingsEvent.NavigateToHelp)
        fun onContactUsClicked() = emit(SettingsEvent.NavigateToContactUs)
        fun onReportProblemClicked() = emit(SettingsEvent.NavigateToReport)
        fun onTermsOfServiceClicked() = emit(SettingsEvent.NavigateToTerms)
        fun onPrivacyPolicyClicked() = emit(SettingsEvent.NavigateToPrivacyPolicy)
        fun onRateAppClicked() = emit(SettingsEvent.TriggerRateApp)

        fun launchReviewFlow(activity: Activity) {
            appReviewManager.openPlayStorePage(activity)
        }

        fun onLogOutClicked() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoggingOut = true, error = null) }
                try {
                    runCatching { chatRepository.disconnectWebSocket() }
                    signOutUseCase()
                    userPreferences.clearUser()
                    withContext(ioDispatcher) {
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
                        Timber.d("Account deletion successful, cleaning up local state")
                        runCatching { chatRepository.disconnectWebSocket() }
                        runCatching { signOutUseCase() }
                        runCatching { userPreferences.clearUser() }
                        withContext(ioDispatcher) {
                            runCatching { appDatabase.clearAllTables() }
                        }
                        _uiState.update { state -> state.copy(isDeletingAccount = false) }
                        _events.send(SettingsEvent.AccountDeleted)
                    },
                    onFailure = { throwable ->
                        Timber.e(throwable, "Account deletion failed")
                        val errorMessage = throwable.userFriendlyMessage("Failed to delete account.")
                        _uiState.update { state ->
                            state.copy(
                                isDeletingAccount = false,
                                deleteAccountStep = if (isPasswordUser) DeleteAccountStep.PASSWORD_INPUT else DeleteAccountStep.NONE,
                                deletePasswordError = errorMessage,
                                error = errorMessage,
                            )
                        }
                        _events.send(SettingsEvent.ShowError(errorMessage))
                    },
                )
            }
        }

        private fun emit(event: SettingsEvent) {
            viewModelScope.launch { _events.send(event) }
        }
    }
