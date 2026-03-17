package must.kdroiders.hustlehub.ui.portfolio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.data.repository.StorageRepository
import must.kdroiders.hustlehub.data.repository.UploadResult
import must.kdroiders.hustlehub.util.ImageUtils
import javax.inject.Inject

data class PortfolioUploadState(
    val selectedUris: List<Uri> = emptyList(),
    val uploadResults: Map<Uri, UploadResult> = emptyMap(),
    val isUploading: Boolean = false
)

@HiltViewModel
class PortfolioUploadViewModel @Inject constructor(
    private val storageRepository: StorageRepository
) : ViewModel() {
    private val vertiGOTag = "Gitau-0628"


    private val _state = MutableStateFlow(PortfolioUploadState())
    val state: StateFlow<PortfolioUploadState> = _state.asStateFlow()

    fun onImagesSelected(uris: List<Uri>) {
        _state.update { it.copy(selectedUris = uris) }
    }

    fun removeImage(uri: Uri) {
        _state.update { it.copy(selectedUris = it.selectedUris.filter { it != uri }) }
    }

    fun uploadPortfolio(context: Context, serviceId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isUploading = true) }
            val uris = _state.value.selectedUris
            
            uris.forEach { uri ->
                _state.update { 
                    it.copy(uploadResults = it.uploadResults + (uri to UploadResult.Progress(0f)))
                }
                
                val bitmap = uriToBitmap(context, uri)
                if (bitmap != null) {
                    val compressedBytes = ImageUtils.compressBitmap(bitmap)
                    storageRepository.uploadPortfolioImage(serviceId, compressedBytes).collect { result ->
                        _state.update { 
                            it.copy(uploadResults = it.uploadResults + (uri to result))
                        }
                    }
                } else {
                    _state.update { 
                        it.copy(uploadResults = it.uploadResults + (uri to UploadResult.Error("Failed to load image")))
                    }
                }
            }
            _state.update { it.copy(isUploading = false) }
        }
    }

    private fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
