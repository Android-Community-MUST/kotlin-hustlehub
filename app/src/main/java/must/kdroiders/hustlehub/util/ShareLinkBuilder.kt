package must.kdroiders.hustlehub.util

import timber.log.Timber

object ShareLinkBuilder {
    private const val DEEP_LINK_BASE = "https://hustlehub-8367.web.app"

    fun buildProfileShareLink(userId: String): String {
        val url = "$DEEP_LINK_BASE/profile/$userId"
        Timber.tag("SHARE_LINK").d("[SHARE_LINK] Built profile share link: userId=%s -> %s", userId, url)
        return url
    }

    fun buildServiceShareLink(serviceId: String): String {
        val url = "$DEEP_LINK_BASE/service/$serviceId"
        Timber.tag("SHARE_LINK").d("[SHARE_LINK] Built service share link: serviceId=%s -> %s", serviceId, url)
        return url
    }
}
