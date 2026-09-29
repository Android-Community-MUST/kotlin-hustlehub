package must.kdroiders.hustlehub.ui.features.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.ui.features.bookmarks.domain.repository.BookmarkRepository
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel
    @Inject
    constructor(
        private val bookmarkRepository: BookmarkRepository,
    ) : ViewModel() {
        val uiState: StateFlow<BookmarkUiState> =
            bookmarkRepository
                .getBookmarksFlow()
                .map { items ->
                    if (items.isEmpty()) {
                        BookmarkUiState.Empty
                    } else {
                        BookmarkUiState.Success(items)
                    }
                }.catch { e ->
                    Timber.e(e, "Error observing bookmarks")
                    emit(BookmarkUiState.Error(e.localizedMessage ?: "Failed to load bookmarks"))
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = BookmarkUiState.Loading,
                )

        init {
            refresh()
        }

        fun refresh() {
            viewModelScope.launch {
                bookmarkRepository.refreshBookmarks()
            }
        }

        fun removeBookmark(itemId: String) {
            viewModelScope.launch {
                bookmarkRepository.removeBookmark(itemId)
            }
        }
    }
