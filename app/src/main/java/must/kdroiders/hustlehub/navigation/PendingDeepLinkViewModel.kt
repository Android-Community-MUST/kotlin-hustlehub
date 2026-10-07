package must.kdroiders.hustlehub.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.datastore.UserPreferences
import javax.inject.Inject

@HiltViewModel
class PendingDeepLinkViewModel
    @Inject
    constructor(
        private val userPreferences: UserPreferences,
    ) : ViewModel() {
        val pendingLink = userPreferences.pendingDeepLink
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

        fun consume() {
            viewModelScope.launch {
                userPreferences.clearPendingDeepLink()
            }
        }
    }
