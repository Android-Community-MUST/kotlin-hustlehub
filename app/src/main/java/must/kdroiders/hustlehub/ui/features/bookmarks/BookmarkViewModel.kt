
package must.kdroiders.hustlehub.ui.features.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookmarkViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<BookmarkUiState>(BookmarkUiState.Loading)
    val uiState: StateFlow<BookmarkUiState> = _uiState.asStateFlow()

    init {
        loadBookmarks()
    }

    fun loadBookmarks() {
        viewModelScope.launch {
            _uiState.value = BookmarkUiState.Loading
            try {
                // Instantly resolve state to Empty for testing/initial setup
                val items = emptyList<BookmarkItem>()

                _uiState.value = if (items.isEmpty()) {
                    BookmarkUiState.Empty
                } else {
                    BookmarkUiState.Success(items)
                }
            } catch (e: Exception) {
                _uiState.value = BookmarkUiState.Error(
                    e.localizedMessage ?: "Failed to load bookmarks"
                )
            }
        }
    }

    fun removeBookmark(itemId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is BookmarkUiState.Success) {
                val updatedList = currentState.items.filterNot { it.id == itemId }
                _uiState.value = if (updatedList.isEmpty()) {
                    BookmarkUiState.Empty
                } else {
                    BookmarkUiState.Success(updatedList)
                }
            }
        }
    }
}
