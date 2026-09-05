package must.kdroiders.hustlehub.core.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object InAppBannerManager {
    private val queue = ArrayDeque<InAppBannerData>()
    private val _activeBanner = MutableStateFlow<InAppBannerData?>(null)
    val activeBanner: StateFlow<InAppBannerData?> = _activeBanner.asStateFlow()

    fun postBanner(banner: InAppBannerData) {
        // Skip if user is actively viewing that specific conversation screen
        if (!banner.conversationId.isNullOrBlank() &&
            banner.conversationId == ActiveConversationTracker.activeConversationId
        ) {
            return
        }

        synchronized(queue) {
            if (_activeBanner.value == null) {
                _activeBanner.value = banner
            } else {
                queue.addLast(banner)
            }
        }
    }

    fun dismissCurrentBanner() {
        synchronized(queue) {
            if (queue.isNotEmpty()) {
                _activeBanner.value = queue.removeFirst()
            } else {
                _activeBanner.value = null
            }
        }
    }

    fun clearQueue() {
        synchronized(queue) {
            queue.clear()
            _activeBanner.value = null
        }
    }
}
