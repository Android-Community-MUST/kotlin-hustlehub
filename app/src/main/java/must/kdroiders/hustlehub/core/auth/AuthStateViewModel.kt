package must.kdroiders.hustlehub.core.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthState
import javax.inject.Inject

/**
 * Hosts the app-wide [AuthState] so that any Composable in the hierarchy can
 * observe authentication changes without holding a direct reference to [AuthManager].
 *
 * Scoped to the [MainActivity] — obtain via `hiltViewModel(viewModelStoreOwner = activity)`.
 *
 * The [authState] StateFlow starts in [AuthState.Loading] and transitions to
 * [AuthState.Authenticated] or [AuthState.Unauthenticated] as Firebase notifies the listener.
 */
@HiltViewModel
class AuthStateViewModel @Inject constructor(
    authManager: AuthManager
) : ViewModel() {

    val authState: StateFlow<AuthState> = authManager.authState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = AuthState.Loading
        )
}
