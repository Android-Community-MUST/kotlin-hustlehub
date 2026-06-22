package must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel

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
import must.kdroiders.hustlehub.ui.features.service.domain.model.Review
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetServiceByIdUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetServiceReviewsUseCase
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ServiceDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val getServiceByIdUseCase: GetServiceByIdUseCase,
        private val getProviderProfileUseCase: GetProviderProfileUseCase,
        private val getServiceReviewsUseCase: GetServiceReviewsUseCase,
        private val authRepository: AuthRepository,
    ) : ViewModel() {
        private val serviceId: String = checkNotNull(savedStateHandle["serviceId"])

        private val _uiState = MutableStateFlow(ServiceDetailUiState())
        val uiState: StateFlow<ServiceDetailUiState> = _uiState.asStateFlow()

        init {
            load()
        }

        fun load() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val currentUid = authRepository.getCurrentUser()?.uid

                val serviceDeferred = async { getServiceByIdUseCase(serviceId) }
                val reviewsDeferred = async { getServiceReviewsUseCase(serviceId, page = 0, size = 5) }

                val serviceResult = serviceDeferred.await()
                val reviewsResult = reviewsDeferred.await()

                serviceResult
                    .onSuccess { service ->
                        val providerResult = getProviderProfileUseCase(service.providerId)

                        val reviews = reviewsResult.getOrElse { emptyList<Review>().let { it } }
                        val reviewPage = reviewsResult.getOrNull()

                        _uiState.update {
                            it.copy(
                                service = service,
                                provider = providerResult.getOrNull(),
                                reviews = reviewPage?.content ?: emptyList(),
                                totalReviewCount = reviewPage?.totalElements?.toInt() ?: service.reviewCount,
                                isOwnService = currentUid != null && currentUid == service.providerId,
                                isLoading = false,
                                error = null,
                            )
                        }
                    }.onFailure { e ->
                        Timber.e(e, "ServiceDetailViewModel: failed to load service id=$serviceId")
                        _uiState.update { it.copy(isLoading = false, error = "Failed to load service details.") }
                    }
            }
        }

        fun retry() = load()
    }
