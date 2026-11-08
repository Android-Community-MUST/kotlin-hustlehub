package must.kdroiders.hustlehub.ui.features.miniapp.presentation.view

import android.annotation.SuppressLint
import android.content.Intent
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import must.kdroiders.hustlehub.ui.features.miniapp.presentation.bridge.HustleBridgeImpl
import must.kdroiders.hustlehub.ui.features.miniapp.presentation.bridge.MiniAppPaymentRequest

/**
 * Sandboxed mini-app host screen.
 *
 * Renders an external web mini-app inside a hardened [WebView]. The host shell
 * keeps full control over hardware-back navigation (web history is drained first),
 * intent-scheme interception (tel:, mailto:, intent://), and the page title / progress bar.
 *
 * @param appId  Unique identifier for this mini-app. Reserved for bridge calls and analytics.
 * @param title  Display name shown in the [TopAppBar].
 * @param url    HTTPS entry-point URL the WebView loads on creation.
 * @param onBack Called when the user wants to leave the mini-app entirely (backstack pop).
 */
@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniAppContainerScreen(
    appId: String,
    title: String,
    url: String,
    onBack: () -> Unit = {},
) {
    var webViewCanGoBack by remember { mutableStateOf(false) }
    var webviewRef by remember { mutableStateOf<WebView?>(null) }
    var loadingProgress by remember { mutableIntStateOf(0) }
    var pendingPayment by remember { mutableStateOf<MiniAppPaymentRequest?>(null) }

    BackHandler(enabled = webViewCanGoBack) {
        webviewRef?.goBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (webViewCanGoBack) webviewRef?.goBack() else onBack()
                    }) {
                        Icon(
                            imageVector = if (webViewCanGoBack) {
                                Icons.AutoMirrored.Filled.ArrowBack
                            } else {
                                Icons.Filled.Close
                            },
                            contentDescription = if (webViewCanGoBack) "Go Back" else "Close mini-app",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        webviewRef?.reload()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Reload",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),

        ) {
            AndroidView(
                factory = { context ->
                    WebView(context)
                        .apply {
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                allowFileAccess = false
                                allowContentAccess = false
                                @Suppress("DEPRECATION")
                                allowFileAccessFromFileURLs = false
                                @Suppress("DEPRECATION")
                                allowUniversalAccessFromFileURLs = false
                            }

                            val bridge = HustleBridgeImpl(
                                appId = appId,
                                onPaymentRequested = { request ->
                                    pendingPayment = request
                                },
                                getPublicProfile = {
                                    """{"appId":"$appId","platform":"HustleHub-Android","version":"1.0"}"""
                                },
                                onClose = onBack,
                            )
                            addJavascriptInterface(bridge, "HustleBridge")

                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView,
                                    request: WebResourceRequest,
                                ): Boolean {
                                    val uri = request.url
                                    val scheme = uri.scheme ?: return false
                                    return when (scheme) {
                                        "https", "http", "file" -> false
                                        else -> {
                                            runCatching {
                                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                                view.context.startActivity(intent)
                                            }
                                            true
                                        }
                                    }
                                }

                                override fun doUpdateVisitedHistory(
                                    view: WebView,
                                    url: String,
                                    isReload: Boolean,
                                ) {
                                    webViewCanGoBack = view.canGoBack()
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(
                                    view: WebView,
                                    newProgress: Int,
                                ) {
                                    loadingProgress = newProgress
                                    webViewCanGoBack = view.canGoBack()
                                }
                            }
                            loadUrl(url)
                        }.also { webviewRef = it }
                },
                modifier = Modifier.fillMaxSize(),
            )

            if (loadingProgress < 100) {
                LinearProgressIndicator(
                    progress = { loadingProgress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }

    // Native M-Pesa Payment Bottom Sheet
    pendingPayment?.let { request ->
        ModalBottomSheet(
            onDismissRequest = {
                // User dismissed without paying -> notify JS
                webviewRef?.let { wv ->
                    HustleBridgeImpl.dispatchPaymentResult(
                        webView = wv,
                        callbackId = request.callbackId,
                        success = false,
                        errorMessage = "Payment cancelled by student"
                    )
                }
                pendingPayment = null
            },
            sheetState = rememberModalBottomSheetState(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            ) {
                Text(
                    text = "Authorize Campus Payment",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "App: $title",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Purpose: ${request.memo}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Amount: KES ${request.amount}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        // Simulate successful STK completion for now
                        webviewRef?.let { wv ->
                            HustleBridgeImpl.dispatchPaymentResult(
                                webView = wv,
                                callbackId = request.callbackId,
                                success = true,
                                receiptNumber = "MHK" + System.currentTimeMillis().toString().takeLast(7),
                            )
                        }
                        pendingPayment = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Pay with M-Pesa")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        webviewRef?.let { wv ->
                            HustleBridgeImpl.dispatchPaymentResult(
                                webView = wv,
                                callbackId = request.callbackId,
                                success = false,
                                errorMessage = "User cancelled payment"
                            )
                        }
                        pendingPayment = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
