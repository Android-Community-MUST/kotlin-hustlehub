package must.kdroiders.hustlehub.ui.features.profile.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthRepository
import must.kdroiders.hustlehub.ui.features.profile.domain.usecase.GetProviderProfileUseCase
import must.kdroiders.hustlehub.ui.features.profile.domain.usecase.GetServicesByProviderUseCase
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProviderProfileViewModel
    @Inject
    constructor(
        private val getProviderProfileUseCase: GetProviderProfileUseCase,
        private val getServicesByProviderUseCase: GetServicesByProviderUseCase,
        private val authRepository: AuthRepository,
    ) : ViewModel() {
        private var providerId: String? = null

        private val _uiState = MutableStateFlow(ProviderProfileUiState())
        val uiState: StateFlow<ProviderProfileUiState> = _uiState.asStateFlow()

        fun initialize(id: String) {
            if (providerId == id) return
            providerId = id
            load()
        }

        fun load() {
            val id = providerId ?: return
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val currentUid = authRepository.getCurrentUser()?.uid

                val profileDeferred = async { getProviderProfileUseCase(id) }
                val servicesDeferred = async { getServicesByProviderUseCase(id) }

                val profileResult = profileDeferred.await()
                val servicesResult = servicesDeferred.await()

                profileResult
                    .onSuccess { provider ->
                        val services = servicesResult.getOrElse { emptyList() }
                        val totalReviews = services.sumOf { it.reviewCount }
                        val avgRating = services.map { it.averageRating }.average().toFloat()

                        // Compute hustle score: (avg rating / 5) × 100, weighted by review count
                        val hustleScore = if (totalReviews > 0) (avgRating / 5f * 100f) else 0f

                        // Compute badges based on thresholds
                        val badges = buildList {
                            if (totalReviews >= 20 && avgRating >= 4.5f) add(Badge("Top Rated", BadgeType.GOLD))
                            if (services.size >= 3) add(Badge("Multi-Service", BadgeType.BLUE))
                            if (provider?.isVerified == true) add(Badge("Verified", BadgeType.GREEN))
                        }

                        _uiState.update {
                            it.copy(
                                provider = provider,
                                services = services,
                                reviewCount = totalReviews,
                                hustleScore = hustleScore,
                                badges = badges,
                                isOwnProfile = currentUid != null && currentUid == provider?.id,
                                isLoading = false,
                                error = null,
                            )
                        }
                    }.onFailure { e ->
                        Timber.e(e, "ProviderProfileViewModel: failed to load provider id=$id")
                        _uiState.update { it.copy(isLoading = false, error = "Failed to load provider profile.") }
                    }
            }
        }

        fun retry() = load()
    }
