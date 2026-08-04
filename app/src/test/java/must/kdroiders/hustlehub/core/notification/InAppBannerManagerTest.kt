package must.kdroiders.hustlehub.core.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class InAppBannerManagerTest {
    @Before
    fun setup() {
        InAppBannerManager.clearQueue()
        ActiveConversationTracker.activeConversationId = null
    }

    @Test
    fun `postBanner displays banner when active banner is null`() {
        val banner = InAppBannerData(title = "Jane", body = "Hello", conversationId = "conv-1")
        InAppBannerManager.postBanner(banner)

        assertEquals(banner, InAppBannerManager.activeBanner.value)
    }

    @Test
    fun `postBanner skips banner when user is on active conversation screen`() {
        ActiveConversationTracker.activeConversationId = "conv-active"
        val banner = InAppBannerData(title = "Jane", body = "Hello", conversationId = "conv-active")
        InAppBannerManager.postBanner(banner)

        assertNull(InAppBannerManager.activeBanner.value)
    }

    @Test
    fun `postBanner queues subsequent banners in FIFO order`() {
        val banner1 = InAppBannerData(title = "Jane", body = "Message 1", conversationId = "conv-1")
        val banner2 = InAppBannerData(title = "Bob", body = "Message 2", conversationId = "conv-2")

        InAppBannerManager.postBanner(banner1)
        InAppBannerManager.postBanner(banner2)

        assertEquals(banner1, InAppBannerManager.activeBanner.value)

        InAppBannerManager.dismissCurrentBanner()

        assertEquals(banner2, InAppBannerManager.activeBanner.value)
    }

    @Test
    fun `dismissCurrentBanner sets active banner to null when queue empty`() {
        val banner1 = InAppBannerData(title = "Jane", body = "Message 1", conversationId = "conv-1")
        InAppBannerManager.postBanner(banner1)

        InAppBannerManager.dismissCurrentBanner()

        assertNull(InAppBannerManager.activeBanner.value)
    }
}
