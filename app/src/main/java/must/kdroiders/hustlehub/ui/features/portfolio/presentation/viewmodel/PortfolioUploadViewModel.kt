package must.kdroiders.hustlehub.ui.features.portfolio.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.data.repository.UploadResult
import must.kdroiders.hustlehub.ui.features.portfolio.domain.usecase.UploadPortfolioImageUseCase
import javax.inject.Inject

/**
 * UI state for the portfolio image upload screen.
 *
 * @param selectedUris   Images chosen from the device gallery (max 6).
 * @param uploadResults  Per-image upload progress / outcome, keyed by [Uri].
 * @param uploadedUrls   Public URLs returned by the backend on successful uploads.
 * @param isUploading    True while any upload coroutine is active.
 */
data class PortfolioUploadState(
    val selectedUris: List<Uri> = emptyList(),
    val uploadResults: Map<Uri, UploadResult> = emptyMap(),
    val uploadedUrls: List<String> = emptyList(),
    val isUploading: Boolean = false,
)

@HiltViewModel
class PortfolioUploadViewModel @Inject constructor(
    private val uploadPortfolioImageUseCase: UploadPortfolioImageUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PortfolioUploadState())
    val state: StateFlow<PortfolioUploadState> = _state.asStateFlow()

    fun onImagesSelected(uris: List<Uri>) {
        _state.update { it.copy(selectedUris = uris) }
    }

    fun removeImage(uri: Uri) {
        _state.update {
            it.copy(
                selectedUris = it.selectedUris.filter { existing -> existing != uri },
                uploadResults = it.uploadResults - uri,
            )
        }
    }

    /** Uploads all images that have not yet succeeded. */
    fun uploadPortfolio(context: Context, serviceId: String) {
        if (_state.value.isUploading) return
        val pending = _state.value.selectedUris.filter {
            _state.value.uploadResults[it] !is UploadResult.Success
        }
        if (pending.isEmpty()) return

        viewModelScope.launch {
            try {
                _state.update { it.copy(isUploading = true) }
                pending.forEach { uri -> uploadSingle(context, uri, serviceId) }
            } finally {
                _state.update { it.copy(isUploading = false) }
            }
        }
    }

    /** Retries a single image that is currently in the Error state. */
    fun retryImage(context: Context, uri: Uri, serviceId: String) {
        if (_state.value.uploadResults[uri] !is UploadResult.Error) return
        viewModelScope.launch {
            _state.update { it.copy(isUploading = true) }
            uploadSingle(context, uri, serviceId)
            val stillUploading = _state.value.uploadResults.values.any { it is UploadResult.Progress }
            _state.update { it.copy(isUploading = stillUploading) }
        }
    }

    // Private helpers

    private suspend fun uploadSingle(context: Context, uri: Uri, serviceId: String) {
        uploadPortfolioImageUseCase(context, uri, serviceId).collect { result ->
            _state.update {
                if (!it.selectedUris.contains(uri)) return@update it

                val newResults = it.uploadResults + (uri to result)
                val newUrls = if (result is UploadResult.Success) {
                    (it.uploadedUrls + result.url).distinct()
                } else {
                    it.uploadedUrls
                }
                it.copy(uploadResults = newResults, uploadedUrls = newUrls)
            }
        }
    }
}
