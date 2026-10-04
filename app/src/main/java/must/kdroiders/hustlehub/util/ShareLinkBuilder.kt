package must.kdroiders.hustlehub.util

import timber.log.Timber
import java.net.URLEncoder

object ShareLinkBuilder {
    private const val PLAY_STORE_BASE =
        "https://play.google.com/store/apps/details?id=must.kdroiders.hustlehub"

    fun buildProfileShareLink(userId: String): String {
        val referrer = URLEncoder.encode("target=profile&id=$userId", "UTF-8")
        val url = "$PLAY_STORE_BASE&referrer=$referrer"
        Timber.tag("SHARE_LINK").d("[SHARE_LINK] Built profile share link: userId=%s -> %s", userId, url)
        return url
    }

    fun buildServiceShareLink(serviceId: String): String {
        val referrer = URLEncoder.encode("target=service&id=$serviceId", "UTF-8")
        val url = "$PLAY_STORE_BASE&referrer=$referrer"
        Timber.tag("SHARE_LINK").d("[SHARE_LINK] Built service share link: serviceId=%s -> %s", serviceId, url)
        return url
    }
}
