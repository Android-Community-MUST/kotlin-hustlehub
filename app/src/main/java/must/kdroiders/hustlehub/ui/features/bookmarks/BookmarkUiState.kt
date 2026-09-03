package must.kdroiders.hustlehub.ui.features.bookmarks

data class BookmarkItem(
    val id: String,
    val title: String,
    val category: String,
    val price: String,
    val rating: Double
)

sealed interface BookmarkUiState {
    data object Loading : BookmarkUiState
    data object Empty : BookmarkUiState
    data class Success(val items: List<BookmarkItem>) : BookmarkUiState
    data class Error(val message: String) : BookmarkUiState
}
