package must.kdroiders.hustlehub.ui.features.miniapp.presentation.bridge

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import timber.log.Timber

data class MiniAppPaymentRequest(
    val appId: String,
    val amount: Double,
    val memo: String,
    val reference: String,
    val callbackId: String,
)

/**
 * JavaScript-to-Native bridge injected into the mini-app [android.webkit.WebView] under
 * the global name `window.HustleBridge`.
 *
 * Security contract:
 * - NEVER expose Firebase ID tokens, raw JWTs, or passwords to the JavaScript context.
 * - Payments are only initiated through [onPaymentRequested]; the mini-app never touches
 *   Safaricom Daraja credentials.
 * - All callbacks to JavaScript MUST be dispatched on the main thread.
 *
 * @param appId              The mini-app identifier (from [MiniApp] NavKey).
 * @param onPaymentRequested Called on the main thread when the mini-app requests a payment.
 * @param getPublicProfile   Lambda that returns a JSON string of the student's public info.
 */
class HustleBridgeImpl(
    private val appId: String,
    private val onPaymentRequested: (MiniAppPaymentRequest) -> Unit,
    private val getPublicProfile: () -> String,
    private val onClose: () -> Unit = {},
) {
    @JavascriptInterface
    fun getStudentProfile(): String {
        Timber.tag("HustleBridge").d("[$appId] getStudentProfile called")
        return getPublicProfile()
    }

    @JavascriptInterface
    fun requestPayment(
        amount: Double,
        memo: String,
        reference: String,
        callbackId: String,
    ) {
        Timber.tag("HustleBridge").d("[$appId] requestPayment: KES $amount for '$memo'")
        Handler(Looper.getMainLooper()).post {
            onPaymentRequested(
                MiniAppPaymentRequest(
                    appId = appId,
                    amount = amount,
                    memo = memo,
                    reference = reference,
                    callbackId = callbackId,
                ),
            )
        }
    }

    @JavascriptInterface
    fun close() {
        Timber.tag("HustleBridge").d("[$appId] close() called by mini-app")
        Handler(Looper.getMainLooper()).post {
            onClose()
        }
    }

    companion object {
        fun dispatchPaymentResult(
            webView: android.webkit.WebView,
            callbackId: String,
            success: Boolean,
            receiptNumber: String? = null,
            errorMessage: String? = null,
        ) {
            val script = """
                window.dispatchEvent(new CustomEvent('HustlePaymentComplete', {
                    detail: {
                        callbackId: '$callbackId',
                        success: $success,
                        receipt: '${receiptNumber ?: ""}',
                        errorMessage: '${errorMessage ?: ""}'
                    }
                }));
            """.trimIndent()
            Handler(Looper.getMainLooper()).post {
                webView.evaluateJavascript(script, null)
            }
        }
    }
}
