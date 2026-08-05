package must.kdroiders.hustlehub.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.datastore.AppTheme
import must.kdroiders.hustlehub.datastore.UserPreferences
import javax.inject.Inject

/**
 * Global ViewModel managing application-wide dark/light theme preference.
 */
@HiltViewModel
class ThemeViewModel
    @Inject
    constructor(
        private val userPreferences: UserPreferences,
    ) : ViewModel() {
        /**
         * StateFlow emitting the current [AppTheme] selection persisted in DataStore.
         */
        val theme: StateFlow<AppTheme> = userPreferences.appTheme.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppTheme.SYSTEM,
        )

        /**
         * Persists the newly selected [AppTheme] to DataStore.
         */
        fun setTheme(theme: AppTheme) {
            viewModelScope.launch {
                userPreferences.saveTheme(theme)
            }
        }
    }
