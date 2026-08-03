package must.kdroiders.hustlehub.ui.features.notification.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.ui.features.notification.domain.model.NotificationPreferences
import must.kdroiders.hustlehub.ui.features.notification.domain.repository.NotificationRepository
import timber.log.Timber
import javax.inject.Inject

data class NotificationPreferencesUiState(
    val preferences: NotificationPreferences = NotificationPreferences(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class NotificationPreferencesViewModel
    @Inject
    constructor(
        private val notificationRepository: NotificationRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(NotificationPreferencesUiState())
        val uiState: StateFlow<NotificationPreferencesUiState> = _uiState.asStateFlow()

        init {
            loadPreferences()
        }

        private fun loadPreferences() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }
                notificationRepository.getPreferences()
                    .onSuccess { prefs ->
                        _uiState.update { it.copy(preferences = prefs, isLoading = false) }
                    }
                    .onFailure { e ->
                        Timber.e(e, "Failed to load notification preferences")
                        _uiState.update { it.copy(isLoading = false, error = "Failed to load preferences") }
                    }
            }
        }

        fun onNewMessagesToggled(enabled: Boolean) = updatePrefs { it.copy(newMessages = enabled) }
        fun onNewReviewsToggled(enabled: Boolean) = updatePrefs { it.copy(newReviews = enabled) }
        fun onServiceInquiriesToggled(enabled: Boolean) = updatePrefs { it.copy(serviceInquiries = enabled) }
        fun onMarketingToggled(enabled: Boolean) = updatePrefs { it.copy(marketing = enabled) }
        fun onSoundToggled(enabled: Boolean) = updatePrefs { it.copy(soundEnabled = enabled) }
        fun onVibrationToggled(enabled: Boolean) = updatePrefs { it.copy(vibrationEnabled = enabled) }
        fun onQuietHoursStartChanged(hour: Int) = updatePrefs { it.copy(quietHoursStart = hour) }
        fun onQuietHoursEndChanged(hour: Int) = updatePrefs { it.copy(quietHoursEnd = hour) }

        private fun updatePrefs(transform: (NotificationPreferences) -> NotificationPreferences) {
            val updated = transform(_uiState.value.preferences)
            _uiState.update { it.copy(preferences = updated) }
            savePreferences(updated)
        }

        private fun savePreferences(preferences: NotificationPreferences) {
            viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true, error = null) }
                notificationRepository.updatePreferences(preferences)
                    .onSuccess {
                        _uiState.update { it.copy(isSaving = false) }
                    }
                    .onFailure { e ->
                        Timber.e(e, "Failed to save notification preferences")
                        _uiState.update { it.copy(isSaving = false, error = "Failed to save preferences") }
                    }
            }
        }
    }
