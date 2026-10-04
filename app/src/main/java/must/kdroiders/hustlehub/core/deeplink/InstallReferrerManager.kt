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
                                parseReferrer(client.installReferrer.installReferrer)
                            } catch (e: Exception) {
                                Timber.e(e, "Failed to read install referrer")
                                null
                            }
                        } else {
                            null
                        }
                    client.endConnection()
                    if (continuation.isActive) continuation.resume(action)
                }

                override fun onInstallReferrerServiceDisconnected() {
                    if (continuation.isActive) continuation.resume(null)
                }
            })
        }

    private fun parseReferrer(raw: String?): DeepLinkAction? {
        if (raw.isNullOrBlank()) return null
        return try {
            val params = raw.split("&").associate {
                URLDecoder.decode(it.substringBefore("="), "UTF-8") to
                    URLDecoder.decode(it.substringAfter("=", ""), "UTF-8")
            }
            val target = params["target"]
            val id = params["id"]
            Timber.d("Install referrer: target=$target id=$id")
            when {
                target == "profile" && !id.isNullOrBlank() -> DeepLinkAction.OpenProviderProfile(id)
                target == "service" && !id.isNullOrBlank() -> DeepLinkAction.OpenServiceDetail(id)
                else -> null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error parsing referrer: $raw")
            null
        }
    }
}
