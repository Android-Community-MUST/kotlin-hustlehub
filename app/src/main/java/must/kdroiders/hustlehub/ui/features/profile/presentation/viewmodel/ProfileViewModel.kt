package must.kdroiders.hustlehub.ui.features.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.data.model.Service
import must.kdroiders.hustlehub.data.model.ServiceAvailability
import must.kdroiders.hustlehub.data.repository.UserRepository
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthRepository
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetMyServicesUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.UpdateAvailabilityUseCase
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val getMyServicesUseCase: GetMyServicesUseCase,
    private val updateAvailabilityUseCase: UpdateAvailabilityUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val firebaseUser = authRepository.getCurrentUser()
            if (firebaseUser == null) {
                _uiState.update { it.copy(isLoading = false, error = "Not logged in") }
                return@launch
            }

            // Load user profile and services in parallel
            val userResult = userRepository.getUserProfile(firebaseUser.uid)
            val servicesResult = getMyServicesUseCase()

            userResult
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            user = user,
                            services = servicesResult.getOrElse { emptyList() },
                            hustleScore = 0f,
                            reviewCount = 0,
                            badges = emptyList(),
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { e ->
                    Timber.e(e, "Failed to load profile")
                    _uiState.update {
                        it.copy(isLoading = false, error = "Failed to load profile. Please try again.")
                    }
                }
        }
    }

    fun toggleServiceActive(serviceId: String) {
        val currentService = _uiState.value.services.find { it.id == serviceId } ?: return
        val newAvailability = if (currentService.availability == ServiceAvailability.AVAILABLE) {
            ServiceAvailability.OFFLINE
        } else {
            ServiceAvailability.AVAILABLE
        }

        // Optimistically update UI
        _uiState.update { state ->
            state.copy(
                services = state.services.map { svc ->
                    if (svc.id == serviceId) svc.copy(availability = newAvailability) else svc
                }
            )
        }

        // Update on backend
        viewModelScope.launch {
            updateAvailabilityUseCase(serviceId, newAvailability).onFailure { e ->
                Timber.e(e, "Failed to update service availability")
                // Revert on failure
                _uiState.update { state ->
                    state.copy(
                        services = state.services.map { svc ->
                            if (svc.id == serviceId) svc.copy(availability = currentService.availability) else svc
                        },
                        error = "Failed to update service availability"
                    )
                }
            }
        }
    }

    fun retry() {
        loadProfile()
    }
}
