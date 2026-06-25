package must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.ui.features.profile.domain.usecase.GetProviderProfileUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetServiceByIdUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.SubmitReviewUseCase
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WriteReviewViewModel
    @Inject
    constructor(
        private val submitReviewUseCase: SubmitReviewUseCase,
        private val getServiceByIdUseCase: GetServiceByIdUseCase,
        private val getProviderProfileUseCase: GetProviderProfileUseCase,
    ) : ViewModel() {
        private var serviceId: String? = null

        private val _uiState = MutableStateFlow(WriteReviewUiState())
        val uiState: StateFlow<WriteReviewUiState> = _uiState.asStateFlow()

        fun initialize(id: String) {
            if (serviceId == id) return
            serviceId = id
            fetchDetails()
        }

        private fun fetchDetails() {
            val id = serviceId ?: return
            viewModelScope.launch {
                _uiState.update { it.copy(isLoadingInfo = true, error = null) }
                getServiceByIdUseCase(id).onSuccess { service ->
                    getProviderProfileUseCase(service.providerId).onSuccess { provider ->
                        _uiState.update { 
                            it.copy(
                                service = service, 
                                provider = provider, 
                                isLoadingInfo = false 
                            ) 
                        }
                    }.onFailure { e ->
                        _uiState.update { 
                            it.copy(isLoadingInfo = false, error = "Failed to load provider profile.") 
                        }
                    }
                }.onFailure { e ->
                    _uiState.update { 
                        it.copy(isLoadingInfo = false, error = "Failed to load service details.") 
                    }
                }
            }
        }

        fun onRatingChanged(rating: Int) = _uiState.update { it.copy(rating = rating) }

        fun onCommentChanged(comment: String) {
            if (comment.length <= _uiState.value.maxCommentLength) {
                _uiState.update { it.copy(comment = comment) }
            }
        }

        fun onAnonymousToggled(value: Boolean) = _uiState.update { it.copy(isAnonymous = value) }

        fun onTagToggled(tag: String) {
            _uiState.update { state ->
                val newTags = if (state.selectedTags.contains(tag)) {
                    state.selectedTags - tag
                } else {
                    state.selectedTags + tag
                }
                state.copy(selectedTags = newTags)
            }
        }

        fun clearError() = _uiState.update { it.copy(error = null) }

        fun submit() {
            val sid = serviceId ?: return
            val state = _uiState.value
            if (!state.canSubmit) return

            viewModelScope.launch {
                _uiState.update { it.copy(isSubmitting = true, error = null) }

                submitReviewUseCase(
                    serviceId = sid,
                    rating = state.rating,
                    comment = state.comment.trim().takeIf { it.isNotBlank() },
                    isAnonymous = state.isAnonymous,
                ).onSuccess {
                    _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) }
                }.onFailure { e ->
                    Timber.e(e, "WriteReviewViewModel: submit failed for serviceId=$sid")
                    _uiState.update { it.copy(isSubmitting = false, error = e.message ?: "Failed to submit review.") }
                }
            }
        }
    }
