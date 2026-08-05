package must.kdroiders.hustlehub.ui.features.privacy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.ui.features.privacy.data.remote.dto.MessagingPermission
import must.kdroiders.hustlehub.ui.features.privacy.data.remote.dto.UpdatePrivacySettingsRequestDto
import must.kdroiders.hustlehub.ui.features.privacy.domain.repository.PrivacyRepository
import timber.log.Timber
import javax.inject.Inject

data class PrivacySettingsUiState(
    val showLocationOnMap: Boolean = true,
    val messagingPermission: MessagingPermission = MessagingPermission.EVERYONE,
    val showOnlineStatus: Boolean = true,
    val showLastSeen: Boolean = true,
    val allowReviews: Boolean = true,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val showMessagingDialog: Boolean = false,
)

@HiltViewModel
class PrivacySettingsViewModel
    @Inject
    constructor(
        private val privacyRepository: PrivacyRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(PrivacySettingsUiState())
        val uiState: StateFlow<PrivacySettingsUiState> = _uiState.asStateFlow()

        init {
            loadPrivacySettings()
        }

        fun loadPrivacySettings() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                privacyRepository.getPrivacySettings()
                    .onSuccess { settings ->
                        _uiState.update {
                            it.copy(
                                showLocationOnMap = settings.showLocationOnMap,
                                messagingPermission = settings.messagingPermission,
                                showOnlineStatus = settings.showOnlineStatus,
                                showLastSeen = settings.showLastSeen,
                                allowReviews = settings.allowReviews,
                                isLoading = false,
                            )
                        }
                    }.onFailure { e ->
                        Timber.e(e, "Failed to load privacy settings")
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load settings") }
                    }
            }
        }

        fun onLocationSharingToggled(enabled: Boolean) {
            _uiState.update { it.copy(showLocationOnMap = enabled) }
            saveSetting(UpdatePrivacySettingsRequestDto(showLocationOnMap = enabled))
        }

        fun onOnlineStatusToggled(enabled: Boolean) {
            _uiState.update { it.copy(showOnlineStatus = enabled) }
            saveSetting(UpdatePrivacySettingsRequestDto(showOnlineStatus = enabled))
        }

        fun onLastSeenToggled(enabled: Boolean) {
            _uiState.update { it.copy(showLastSeen = enabled) }
            saveSetting(UpdatePrivacySettingsRequestDto(showLastSeen = enabled))
        }

        fun onAllowReviewsToggled(enabled: Boolean) {
            _uiState.update { it.copy(allowReviews = enabled) }
            saveSetting(UpdatePrivacySettingsRequestDto(allowReviews = enabled))
        }

        fun onMessagingClicked() {
            _uiState.update { it.copy(showMessagingDialog = true) }
        }

        fun onMessagingDialogDismissed() {
            _uiState.update { it.copy(showMessagingDialog = false) }
        }

        fun onMessagingPermissionSelected(permission: MessagingPermission) {
            _uiState.update { it.copy(showMessagingDialog = false, messagingPermission = permission) }
            saveSetting(UpdatePrivacySettingsRequestDto(messagingPermission = permission))
        }

        private fun saveSetting(request: UpdatePrivacySettingsRequestDto) {
            viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true) }
                privacyRepository.updatePrivacySettings(request)
                    .onSuccess { settings ->
                        _uiState.update {
                            it.copy(
                                showLocationOnMap = settings.showLocationOnMap,
                                messagingPermission = settings.messagingPermission,
                                showOnlineStatus = settings.showOnlineStatus,
                                showLastSeen = settings.showLastSeen,
                                allowReviews = settings.allowReviews,
                                isSaving = false,
                            )
                        }
                    }.onFailure { e ->
                        Timber.e(e, "Failed to save setting update")
                        _uiState.update { it.copy(isSaving = false, errorMessage = "Failed to save change") }
                    }
            }
        }
    }
