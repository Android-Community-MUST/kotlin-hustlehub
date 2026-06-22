package must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.SubmitReviewUseCase
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WriteReviewViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val submitReviewUseCase: SubmitReviewUseCase,
    ) : ViewModel() {
        private val serviceId: String = checkNotNull(savedStateHandle["serviceId"])

        private val _uiState = MutableStateFlow(WriteReviewUiState())
        val uiState: StateFlow<WriteReviewUiState> = _uiState.asStateFlow()

        fun onRatingChanged(rating: Int) = _uiState.update { it.copy(rating = rating) }

        fun onCommentChanged(comment: String) {
            if (comment.length <= _uiState.value.maxCommentLength) {
                _uiState.update { it.copy(comment = comment) }
            }
        }

        fun onAnonymousToggled(value: Boolean) = _uiState.update { it.copy(isAnonymous = value) }

        fun clearError() = _uiState.update { it.copy(error = null) }

        fun submit() {
            val state = _uiState.value
            if (!state.canSubmit) return

            viewModelScope.launch {
                _uiState.update { it.copy(isSubmitting = true, error = null) }

                submitReviewUseCase(
                    serviceId = serviceId,
                    rating = state.rating,
                    comment = state.comment.trim().takeIf { it.isNotBlank() },
                    isAnonymous = state.isAnonymous,
                ).onSuccess {
                    _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) }
                }.onFailure { e ->
                    Timber.e(e, "WriteReviewViewModel: submit failed for serviceId=$serviceId")
                    _uiState.update { it.copy(isSubmitting = false, error = e.message ?: "Failed to submit review.") }
                }
            }
        }
    }
