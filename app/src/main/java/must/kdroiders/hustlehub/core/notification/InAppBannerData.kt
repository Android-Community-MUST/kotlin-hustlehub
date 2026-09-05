package must.kdroiders.hustlehub.core.notification

import java.util.UUID

data class InAppBannerData(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    val senderPhotoUrl: String? = null,
    val conversationId: String? = null,
    val deepLinkUri: String? = null,
)
