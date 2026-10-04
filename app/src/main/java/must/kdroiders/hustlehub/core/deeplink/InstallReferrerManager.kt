package must.kdroiders.hustlehub.core.deeplink

import android.content.Context
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import must.kdroiders.hustlehub.navigation.DeepLinkAction
import timber.log.Timber
import java.net.URLDecoder
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class InstallReferrerManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun getDeferredDeepLink(): DeepLinkAction? =
        suspendCancellableCoroutine { continuation ->
            val client = InstallReferrerClient.newBuilder(context).build()
            client.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    val action =
                        if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                            try {
                                val raw = client.installReferrer.installReferrer
                                Timber.tag("SHARE_LINK").d("[SHARE_LINK] Raw install referrer string: %s", raw)
                                parseReferrer(raw)
                            } catch (e: Exception) {
                                Timber.tag("SHARE_LINK").e(e, "[SHARE_LINK] Failed to read install referrer")
                                null
                            }
                        } else {
                            Timber.tag("SHARE_LINK").w("[SHARE_LINK] Install referrer setup finished with non-OK code: %d", responseCode)
                            null
                        }
                    client.endConnection()
                    if (continuation.isActive) continuation.resume(action)
                }

                override fun onInstallReferrerServiceDisconnected() {
                    Timber.tag("SHARE_LINK").w("[SHARE_LINK] Install referrer service disconnected")
                    if (continuation.isActive) continuation.resume(null)
                }
            })
        }

    private fun parseReferrer(raw: String?): DeepLinkAction? {
        if (raw.isNullOrBlank()) {
            Timber.tag("SHARE_LINK").d("[SHARE_LINK] Referrer raw string is null or blank")
            return null
        }
        return try {
            val decodedRaw = if (raw.contains("%")) {
                try {
                    URLDecoder.decode(raw, "UTF-8")
                } catch (_: Exception) {
                    raw
                }
            } else {
                raw
            }
            val params = decodedRaw.split("&").associate {
                URLDecoder.decode(it.substringBefore("="), "UTF-8") to
                    URLDecoder.decode(it.substringAfter("=", ""), "UTF-8")
            }
            val target = params["target"]
            val id = params["id"]
            Timber.tag("SHARE_LINK").d("[SHARE_LINK] Parsed install referrer: target=%s, id=%s (from raw=%s)", target, id, raw)
            when {
                target == "profile" && !id.isNullOrBlank() -> DeepLinkAction.OpenProviderProfile(id)
                target == "service" && !id.isNullOrBlank() -> DeepLinkAction.OpenServiceDetail(id)
                else -> {
                    Timber.tag("SHARE_LINK").w("[SHARE_LINK] Unrecognized or incomplete referrer params: target=%s, id=%s", target, id)
                    null
                }
            }
        } catch (e: Exception) {
            Timber.tag("SHARE_LINK").e(e, "[SHARE_LINK] Error parsing referrer: %s", raw)
            null
        }
    }
}
