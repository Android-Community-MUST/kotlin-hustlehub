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
        private val getServiceByIdUseCase: GetServiceByIdUseCase,
        private val getProviderProfileUseCase: GetProviderProfileUseCase,
        private val getServiceReviewsUseCase: GetServiceReviewsUseCase,
        private val authRepository: AuthRepository,
    ) : ViewModel() {
        private var serviceId: String? = null

        private val _uiState = MutableStateFlow(ServiceDetailUiState())
        val uiState: StateFlow<ServiceDetailUiState> = _uiState.asStateFlow()

        fun initialize(id: String) {
            if (serviceId == id) return
            serviceId = id
            load()
        }

        fun load() {
            val id = serviceId ?: return
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val currentUid = authRepository.getCurrentUser()?.uid

                val serviceDeferred = async { getServiceByIdUseCase(id) }
                val reviewsDeferred = async { getServiceReviewsUseCase(id, page = 0, size = 5) }

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
                        Timber.e(e, "ServiceDetailViewModel: failed to load service id=$id")
                        _uiState.update { it.copy(isLoading = false, error = "Failed to load service details.") }
                    }
            }
        }

        fun retry() = load()
    }
