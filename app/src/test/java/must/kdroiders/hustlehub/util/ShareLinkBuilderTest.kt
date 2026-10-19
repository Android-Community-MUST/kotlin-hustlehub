package must.kdroiders.hustlehub.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ShareLinkBuilderTest {
    @Test
    fun `buildProfileShareLink formats profile deep link correctly`() {
        val link = ShareLinkBuilder.buildProfileShareLink("test-uuid-123")
        assertEquals("https://hustlehub-8367.web.app/profile/test-uuid-123", link)
    }

    @Test
    fun `buildServiceShareLink formats service deep link correctly`() {
        val link = ShareLinkBuilder.buildServiceShareLink("test-service-456")
        assertEquals("https://hustlehub-8367.web.app/service/test-service-456", link)
    }

    @Test
    fun `buildAppInviteLink formats base invite link correctly`() {
        val link = ShareLinkBuilder.buildAppInviteLink()
        assertEquals("https://hustlehub-8367.web.app", link)
    }
}
