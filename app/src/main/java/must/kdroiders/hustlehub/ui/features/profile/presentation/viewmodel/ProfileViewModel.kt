package must.kdroiders.hustlehub.ui.features.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthRepository
import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
import must.kdroiders.hustlehub.ui.features.profile.domain.util.HustleScoreCalculator
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetMyServicesUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.UpdateAvailabilityUseCase
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel
    @Inject
    constructor(
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
                        val services = servicesResult.getOrElse { emptyList() }
                        val calculatedReviewCount = services.sumOf { it.reviewCount }
                        val calculatedScore = HustleScoreCalculator.calculate(services)

                        val computedBadges = buildList {
                            if (calculatedScore >= 4.0f && calculatedReviewCount >= 1) {
                                add(Badge("Top Rated", BadgeType.BLUE))
                            }
                            if (calculatedReviewCount >= 1) {
                                add(Badge("Fast Responder", BadgeType.GREEN))
                            }
                            if (user?.isVerified == true) {
                                add(Badge("Verified Student", BadgeType.BLUE))
                            }
                        }

                        val isOnline = if (services.isNotEmpty()) {
                            services.any { it.availability != ServiceAvailability.OFFLINE }
                        } else {
                            user?.isOnline ?: true
                        }

                        _uiState.update {
                            it.copy(
                                user = user?.copy(isOnline = isOnline),
                                services = services,
                                hustleScore = calculatedScore,
                                reviewCount = calculatedReviewCount,
                                badges = computedBadges,
                                isLoading = false,
                                error = null,
                            )
                        }
                    }.onFailure { e ->
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

            val updatedServices = _uiState.value.services.map { svc ->
                if (svc.id == serviceId) svc.copy(availability = newAvailability) else svc
            }
            val anyActive = updatedServices.any { it.availability != ServiceAvailability.OFFLINE }

            _uiState.update { state ->
                state.copy(
                    user = state.user?.copy(isOnline = anyActive),
                    services = updatedServices,
                )
            }

            viewModelScope.launch {
                updateAvailabilityUseCase(serviceId, newAvailability)
                    .onSuccess {
                        userRepository.updateOnlineStatus(anyActive)
                    }
                    .onFailure { e ->
                        Timber.e(e, "Failed to update service availability")
                        _uiState.update { state ->
                            val revertedServices = state.services.map { svc ->
                                if (svc.id == serviceId) svc.copy(availability = currentService.availability) else svc
                            }
                            val revertedAnyActive = revertedServices.any { it.availability != ServiceAvailability.OFFLINE }
                            state.copy(
                                user = state.user?.copy(isOnline = revertedAnyActive),
                                services = revertedServices,
                                error = "Failed to update service availability",
                            )
                        }
                    }
            }
        }

        fun toggleOverallAvailability(isOnline: Boolean) {
            val currentUser = _uiState.value.user ?: return
            val previousUser = currentUser
            val previousServices = _uiState.value.services
            val targetAvailability = if (isOnline) ServiceAvailability.AVAILABLE else ServiceAvailability.OFFLINE

            _uiState.update { state ->
                state.copy(
                    user = currentUser.copy(isOnline = isOnline),
                    services = state.services.map { svc ->
                        svc.copy(availability = targetAvailability)
                    },
                )
            }

            viewModelScope.launch {
                userRepository.updateOnlineStatus(isOnline)

                val serviceUpdates = previousServices.map { service ->
                    async { updateAvailabilityUseCase(service.id, targetAvailability) }
                }
                val results = serviceUpdates.awaitAll()
                if (results.all { it.isFailure } && previousServices.isNotEmpty()) {
                    _uiState.update { state ->
                        state.copy(
                            user = previousUser,
                            services = previousServices,
                            error = "Failed to update availability",
                        )
                    }
                }
            }
        }

        fun retry() {
            loadProfile()
        }
    }
