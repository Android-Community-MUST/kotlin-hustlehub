package must.kdroiders.hustlehub.core.notification

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
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
            val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
            if (currentUser != null) {
                userRepository.updateFcmToken(token)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val rawType = remoteMessage.data["type"] ?: "generic"
        val type = rawType.lowercase()
        val title = remoteMessage.data["title"]
            ?: remoteMessage.notification?.title
            ?: "HustleHub"
        val body = remoteMessage.data["body"]
            ?: remoteMessage.notification?.body
            ?: ""

        val customDeepLink = remoteMessage.data["deepLink"] ?: remoteMessage.data["targetUri"]

        Timber.d("FCM message received — type=$type, title=$title, body=$body")

        when (type) {
            "payment_success", "payment_completed" -> {
                val deepLink = customDeepLink ?: "hustlehub://notifications"
                NotificationHelper.postPaymentNotification(
                    context = this,
                    title = title,
                    body = body,
                    isSuccess = true,
                    deepLinkUri = deepLink,
                )
                InAppBannerManager.postBanner(
                    InAppBannerData(
                        title = title,
                        body = body,
                        deepLinkUri = deepLink,
                    ),
                )
            }
            "payment_failed" -> {
                val deepLink = customDeepLink ?: "hustlehub://notifications"
                NotificationHelper.postPaymentNotification(
                    context = this,
                    title = title,
                    body = body,
                    isSuccess = false,
                    deepLinkUri = deepLink,
                )
                InAppBannerManager.postBanner(
                    InAppBannerData(
                        title = title,
                        body = body,
                        deepLinkUri = deepLink,
                    ),
                )
            }
            "new_message" -> {
                val conversationId = remoteMessage.data["conversationId"] ?: return
                val senderName = remoteMessage.data["senderName"] ?: title
                val content = remoteMessage.data["content"] ?: body

                if (conversationId == ActiveConversationTracker.activeConversationId) return
                NotificationHelper.postMessageNotification(this, conversationId, senderName, content)
                InAppBannerManager.postBanner(
                    InAppBannerData(
                        title = senderName,
                        body = content,
                        senderPhotoUrl = remoteMessage.data["senderPhotoUrl"],
                        conversationId = conversationId,
                        deepLinkUri = "hustlehub://chat/$conversationId",
                    ),
                )
            }
            "new_review" -> {
                val deepLink = customDeepLink ?: run {
                    val serviceId = remoteMessage.data["serviceId"]
                    val providerId = remoteMessage.data["providerId"]
                    if (!serviceId.isNullOrBlank() && !providerId.isNullOrBlank()) {
                        "hustlehub://review/$serviceId?providerId=$providerId"
                    } else if (!serviceId.isNullOrBlank()) {
                        "hustlehub://service/$serviceId"
                    } else {
                        "hustlehub://notifications"
                    }
                }
                NotificationHelper.postReviewNotification(this, title, body, deepLink)
                InAppBannerManager.postBanner(
                    InAppBannerData(
                        title = title,
                        body = body,
                        deepLinkUri = deepLink,
                    ),
                )
            }
            "inquiry" -> {
                val deepLink = customDeepLink ?: run {
                    val conversationId = remoteMessage.data["conversationId"]
                    if (!conversationId.isNullOrBlank()) {
                        "hustlehub://chat/$conversationId"
                    } else {
                        "hustlehub://notifications"
                    }
                }
                NotificationHelper.postInquiryNotification(this, title, body, deepLink)
                InAppBannerManager.postBanner(
                    InAppBannerData(
                        title = title,
                        body = body,
                        deepLinkUri = deepLink,
                    ),
                )
            }
            else -> {
                Timber.d("Received generic or unknown FCM message type: $rawType")
                if (title.isNotBlank() && body.isNotBlank()) {
                    NotificationHelper.postReviewNotification(
                        this,
                        title,
                        body,
                        customDeepLink ?: "hustlehub://notifications",
                    )
                }
            }
        }
    }
}
