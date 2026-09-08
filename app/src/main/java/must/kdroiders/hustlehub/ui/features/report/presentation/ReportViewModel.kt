package must.kdroiders.hustlehub.ui.features.report.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.core.api.userFriendlyMessage
import must.kdroiders.hustlehub.ui.features.report.domain.repository.ReportRepository
import javax.inject.Inject

data class ReportUiState(
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ReportViewModel
    @Inject
    constructor(
        private val reportRepository: ReportRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ReportUiState())
        val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

        fun submitReport(
            targetId: String,
            targetType: String,
            reason: String,
            description: String?,
        ) {
            viewModelScope.launch {
                _uiState.update { it.copy(isSubmitting = true, error = null, isSuccess = false) }
                val result = reportRepository.submitReport(targetId, targetType, reason, description)
                result.fold(
                    onSuccess = {
                        _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(isSubmitting = false, error = e.userFriendlyMessage("Failed to submit report")) }
                    },
                )
            }
        }

        fun resetState() {
            _uiState.value = ReportUiState()
        }
    }
