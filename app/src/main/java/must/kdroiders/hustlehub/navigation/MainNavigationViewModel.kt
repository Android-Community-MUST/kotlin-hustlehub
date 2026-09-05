package must.kdroiders.hustlehub.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

sealed interface DeepLinkAction {
    data class OpenChat(val conversationId: String) : DeepLinkAction
    data class OpenServiceDetail(val serviceId: String) : DeepLinkAction
    data class OpenProviderProfile(val providerId: String) : DeepLinkAction
    data class OpenWriteReview(val serviceId: String, val providerId: String) : DeepLinkAction
    data object OpenNotifications : DeepLinkAction
    data object OpenProfile : DeepLinkAction
    data object OpenChatList : DeepLinkAction
}

@HiltViewModel
class MainNavigationViewModel
    @Inject
    constructor() : ViewModel() {
        private val _deepLinkEvent = MutableSharedFlow<DeepLinkAction>(extraBufferCapacity = 1)
        val deepLinkEvent: SharedFlow<DeepLinkAction> = _deepLinkEvent.asSharedFlow()

        fun triggerDeepLink(action: DeepLinkAction) {
            _deepLinkEvent.tryEmit(action)
        }
    }
