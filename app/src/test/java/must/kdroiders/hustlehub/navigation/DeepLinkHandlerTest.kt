package must.kdroiders.hustlehub.navigation

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DeepLinkHandlerTest {
    private fun parseDeepLink(uriString: String): DeepLinkAction? {
        val uri = Uri.parse(uriString)
        if (uri.scheme != "hustlehub") return null
        val host = uri.host ?: return null
        val lastSegment = uri.lastPathSegment

        return when (host) {
            "chat" -> {
                val conversationId = lastSegment ?: uri.getQueryParameter("conversationId")
                if (!conversationId.isNullOrBlank()) DeepLinkAction.OpenChat(conversationId) else null
            }
            "service" -> {
                if (!lastSegment.isNullOrBlank()) DeepLinkAction.OpenServiceDetail(lastSegment) else null
            }
            "profile" -> {
                if (!lastSegment.isNullOrBlank()) DeepLinkAction.OpenProviderProfile(lastSegment) else null
            }
            "review" -> {
                val serviceId = lastSegment
                val providerId = uri.getQueryParameter("providerId") ?: ""
                if (!serviceId.isNullOrBlank()) DeepLinkAction.OpenWriteReview(serviceId, providerId) else null
            }
            "notifications" -> DeepLinkAction.OpenNotifications
            "app" -> {
                when {
                    uri.path?.contains("chat") == true -> {
                        val conversationId = uri.getQueryParameter("conversationId")
                        if (!conversationId.isNullOrBlank()) DeepLinkAction.OpenChat(conversationId) else null
                    }
                    uri.path?.contains("profile") == true -> DeepLinkAction.OpenProfile
                    uri.path?.contains("inquiries") == true -> DeepLinkAction.OpenChatList
                    else -> null
                }
            }
            else -> null
        }
    }

    @Test
    fun `chat deep link parses conversationId correctly`() {
        val action = parseDeepLink("hustlehub://chat/conv-12345")
        assertTrue(action is DeepLinkAction.OpenChat)
        assertEquals("conv-12345", (action as DeepLinkAction.OpenChat).conversationId)
    }

    @Test
    fun `service deep link parses serviceId correctly`() {
        val action = parseDeepLink("hustlehub://service/serv-999")
        assertTrue(action is DeepLinkAction.OpenServiceDetail)
        assertEquals("serv-999", (action as DeepLinkAction.OpenServiceDetail).serviceId)
    }

    @Test
    fun `profile deep link parses providerId correctly`() {
        val action = parseDeepLink("hustlehub://profile/user-888")
        assertTrue(action is DeepLinkAction.OpenProviderProfile)
        assertEquals("user-888", (action as DeepLinkAction.OpenProviderProfile).providerId)
    }

    @Test
    fun `review deep link parses serviceId and providerId query param`() {
        val action = parseDeepLink("hustlehub://review/serv-100?providerId=usr-200")
        assertTrue(action is DeepLinkAction.OpenWriteReview)
        assertEquals("serv-100", (action as DeepLinkAction.OpenWriteReview).serviceId)
        assertEquals("usr-200", action.providerId)
    }

    @Test
    fun `notifications deep link parses to OpenNotifications action`() {
        val action = parseDeepLink("hustlehub://notifications")
        assertEquals(DeepLinkAction.OpenNotifications, action)
    }

    @Test
    fun `legacy app chat deep link parses conversationId query param`() {
        val action = parseDeepLink("hustlehub://app/chat?conversationId=legacy-conv")
        assertTrue(action is DeepLinkAction.OpenChat)
        assertEquals("legacy-conv", (action as DeepLinkAction.OpenChat).conversationId)
    }

    @Test
    fun `invalid scheme returns null gracefully`() {
        val action = parseDeepLink("https://hustlehub.com/chat/123")
        assertNull(action)
    }

    @Test
    fun `unknown host returns null gracefully`() {
        val action = parseDeepLink("hustlehub://unknownhost/123")
        assertNull(action)
    }
}
