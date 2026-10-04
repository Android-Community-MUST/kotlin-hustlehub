package must.kdroiders.hustlehub.util

import java.net.URLEncoder

object ShareLinkBuilder {
    private const val PLAY_STORE_BASE =
        "https://play.google.com/store/apps/details?id=must.kdroiders.hustlehub"

    fun buildProfileShareLink(userId: String): String {
        val referrer = URLEncoder.encode("target=profile&id=$userId", "UTF-8")
        return "$PLAY_STORE_BASE&referrer=$referrer"
    }

    fun buildServiceShareLink(serviceId: String): String {
        val referrer = URLEncoder.encode("target=service&id=$serviceId", "UTF-8")
        return "$PLAY_STORE_BASE&referrer=$referrer"
    }
}
