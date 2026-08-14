package must.kdroiders.hustlehub.core.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ConnectivityViewModel @Inject constructor(
    private val connectivityObserver: ConnectivityObserver,
) : ViewModel() {

    val isConnected = connectivityObserver.isConnected.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = true,
    )

    fun retryConnectivityCheck() {
        if (connectivityObserver is AndroidConnectivityObserver) {
            // Re-registration is handled automatically by the callbackFlow restart;
            // this method is a hook for manual UI retry triggering if needed.
        }
    }
}
