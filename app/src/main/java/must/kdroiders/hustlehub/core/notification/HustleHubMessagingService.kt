package must.kdroiders.hustlehub.core.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HustleHubMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var userRepository: UserRepository

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            userRepository.updateFcmToken(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val type = remoteMessage.data["type"] ?: return
        val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title ?: "HustleHub"
        val body = remoteMessage.data["body"] ?: remoteMessage.notification?.body ?: ""

        when (type) {
            "new_message" -> {
                val conversationId = remoteMessage.data["conversationId"] ?: return
                val senderName = remoteMessage.data["senderName"] ?: title
                val content = remoteMessage.data["content"] ?: body

                if (conversationId == ActiveConversationTracker.activeConversationId) return
                NotificationHelper.postMessageNotification(this, conversationId, senderName, content)
            }
            "new_review" -> {
                NotificationHelper.postReviewNotification(this, title, body)
            }
            "inquiry" -> {
                NotificationHelper.postInquiryNotification(this, title, body)
            }
            else -> {
                Timber.d("Received unknown FCM message type: $type")
            }
        }
    }
}
