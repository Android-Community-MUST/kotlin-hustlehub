package must.kdroiders.hustlehub.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

class AndroidConnectivityObserver(
    private val context: Context,
) : ConnectivityObserver {
    private val connectivityManager =
        requireNotNull(context.getSystemService<ConnectivityManager>()) {
            "ConnectivityManager is not available on this device"
        }

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    override val isConnected: Flow<Boolean> = callbackFlow {
        val callback = object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                launch {
                    trySend(checkActiveInternetConnectivity())
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                trySend(false)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                launch {
                    trySend(checkActiveInternetConnectivity())
                }
            }

            override fun onUnavailable() {
                super.onUnavailable()
                trySend(false)
            }

            override fun onLosing(
                network: Network,
                maxMsToLive: Int,
            ) {
                super.onLosing(network, maxMsToLive)
                trySend(false)
            }
        }

        val networkRequest = NetworkRequest
            .Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, callback)

        launch {
            trySend(checkActiveInternetConnectivity())
        }

        awaitClose {
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (e: IllegalArgumentException) {
                Timber.tag("ConnectivityObserver").w("Callback already unregistered: ${e.message}")
            }
        }
    }.distinctUntilChanged()
        .debounce { online -> if (online) 0L else 1_000L }

    private suspend fun checkActiveInternetConnectivity(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val network = connectivityManager.activeNetwork ?: return@withContext false
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                    ?: return@withContext false
                if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    return@withContext false
                }
                Socket().use { socket ->
                    socket.connect(InetSocketAddress("8.8.8.8", 53), 1_200)
                }
                true
            } catch (e: IOException) {
                Timber.tag("ConnectivityObserver").d("Internet probe failed: ${e.message}")
                false
            } catch (e: Exception) {
                false
            }
        }
}
