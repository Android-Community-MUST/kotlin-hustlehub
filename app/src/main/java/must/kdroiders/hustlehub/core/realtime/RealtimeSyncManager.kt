package must.kdroiders.hustlehub.core.realtime

import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import must.kdroiders.hustlehub.BuildConfig
import must.kdroiders.hustlehub.ui.features.service.data.local.dao.ServiceDao
import okhttp3.OkHttpClient
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.headers.StompSubscribeHeaders
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data class mirroring the backend RealtimeEvent payload.
 */
data class RealtimeEvent(
    val eventType: String = "",
    val entityId: String = "",
    val userId: String? = null,
    val timestamp: Long = 0L,
    val payload: Map<String, Any?>? = null,
)

/**
 * Singleton that maintains a STOMP WebSocket session dedicated to real-time
 * app-wide sync events (services, user profile, suspension, account deletion).
 *
 * Subscriptions:
 *  - /topic/services         – global service mutations (create / update / delete)
 *  - /topic/users/{uid}      – per-user events (profile update, suspension, account deletion)
 *
 * Consumers collect [events] in their ViewModel's viewModelScope.
 */
@Singleton
class RealtimeSyncManager
    @Inject
    constructor(
        private val okHttpClient: OkHttpClient,
        private val firebaseAuth: FirebaseAuth?,
        private val serviceDao: ServiceDao,
    ) {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private val gson = Gson()

        private var stompSession: StompSession? = null
        private var connectJob: Job? = null

        private val _events = MutableSharedFlow<RealtimeEvent>(extraBufferCapacity = 64)

        /** Shared flow that ViewModels can collect to react to real-time events. */
        val events: SharedFlow<RealtimeEvent> = _events.asSharedFlow()

        private companion object {
            const val RECONNECT_DELAY_MS = 5_000L
            const val MAX_RECONNECT_ATTEMPTS = 5
        }

        /**
         * Called after a successful login. Starts the STOMP connection and subscribes
         * to global service and personal user topics.
         */
        fun connect() {
            if (connectJob?.isActive == true) return
            connectJob = scope.launch { connectWithRetry() }
        }

        /**
         * Called on sign-out or account deletion. Tears down the STOMP session and
         * cancels any reconnect attempt.
         */
        fun disconnect() {
            connectJob?.cancel()
            connectJob = null
            scope.launch {
                try {
                    stompSession?.disconnect()
                } catch (e: Exception) {
                    Timber.tag("REALTIME").w(e, "Error disconnecting realtime STOMP session")
                } finally {
                    stompSession = null
                }
            }
        }

        private suspend fun connectWithRetry() {
            var attempt = 0
            while (attempt < MAX_RECONNECT_ATTEMPTS) {
                try {
                    val user = firebaseAuth?.currentUser ?: run {
                        Timber.tag("REALTIME").d("No Firebase user — skipping realtime connect")
                        return
                    }
                    val token = user.getIdToken(false).await().token
                        ?: run {
                            Timber.tag("REALTIME").w("Could not obtain Firebase token for realtime")
                            return
                        }

                    val wsUrl = BuildConfig.WS_BASE_URL
                    Timber.tag("REALTIME").d("Connecting to realtime STOMP at $wsUrl (attempt ${attempt + 1})")

                    val wsClient = OkHttpWebSocketClient(okHttpClient)
                    val client = StompClient(wsClient)
                    val session = client.connect(
                        url = wsUrl,
                        customStompConnectHeaders = mapOf("token" to token),
                    )
                    stompSession = session
                    Timber.tag("REALTIME").i("Realtime STOMP connected ✓")

                    // Subscribe to global services topic and per-user topic in parallel
                    scope.launch { subscribeToServices(session) }
                    scope.launch { subscribeToUser(session, user.uid) }
                    return // success — exit retry loop
                } catch (e: Exception) {
                    attempt++
                    Timber.tag("REALTIME").w(e, "Realtime connect failed (attempt $attempt/$MAX_RECONNECT_ATTEMPTS)")
                    if (attempt < MAX_RECONNECT_ATTEMPTS) {
                        delay(RECONNECT_DELAY_MS * attempt)
                    }
                }
            }
            Timber.tag("REALTIME").e("Realtime STOMP failed after $MAX_RECONNECT_ATTEMPTS attempts — giving up")
        }

        private suspend fun subscribeToServices(session: StompSession) {
            val destination = "/topic/services"
            Timber.tag("REALTIME").d("Subscribing to $destination")
            try {
                session.subscribe(StompSubscribeHeaders(destination)).collect { frame ->
                    val event = runCatching {
                        gson.fromJson(frame.bodyAsText, RealtimeEvent::class.java)
                    }.getOrNull() ?: return@collect

                    Timber.tag("REALTIME").d("Service event: ${event.eventType} id=${event.entityId}")

                    // Update Room cache immediately for delete events
                    when (event.eventType) {
                        "SERVICE_DELETED" -> {
                            runCatching { serviceDao.deleteById(event.entityId) }
                        }
                    }

                    _events.emit(event)
                }
            } catch (e: Exception) {
                Timber.tag("REALTIME").w(e, "Service subscription error")
            }
        }

        private suspend fun subscribeToUser(session: StompSession, uid: String) {
            val destination = "/topic/users/$uid"
            Timber.tag("REALTIME").d("Subscribing to $destination")
            try {
                session.subscribe(StompSubscribeHeaders(destination)).collect { frame ->
                    val event = runCatching {
                        gson.fromJson(frame.bodyAsText, RealtimeEvent::class.java)
                    }.getOrNull() ?: return@collect

                    Timber.tag("REALTIME").d("User event: ${event.eventType} uid=${event.userId}")
                    _events.emit(event)
                }
            } catch (e: Exception) {
                Timber.tag("REALTIME").w(e, "User subscription error for uid=$uid")
            }
        }
    }
