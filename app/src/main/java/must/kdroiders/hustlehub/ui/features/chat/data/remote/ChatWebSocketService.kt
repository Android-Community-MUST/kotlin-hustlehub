package must.kdroiders.hustlehub.ui.features.chat.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import must.kdroiders.hustlehub.BuildConfig
import must.kdroiders.hustlehub.core.security.Base64Util
import must.kdroiders.hustlehub.core.security.CryptoManager
import must.kdroiders.hustlehub.core.security.KeyExchangeHandler
import must.kdroiders.hustlehub.ui.features.chat.data.remote.dto.MessageResponse
import must.kdroiders.hustlehub.ui.features.chat.data.remote.dto.SendMessageRequest
import must.kdroiders.hustlehub.ui.features.chat.domain.model.TypingIndicator
import must.kdroiders.hustlehub.ui.features.chat.domain.model.UserPresence
import okhttp3.OkHttpClient
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.headers.StompSubscribeHeaders
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatWebSocketService
    @Inject
    constructor(
        private val okHttpClient: OkHttpClient,
        private val firebaseAuth: FirebaseAuth?,
        private val cryptoManager: CryptoManager,
        private val keyExchangeHandler: KeyExchangeHandler,
    ) {
        private var stompSession: StompSession? = null
        private val gson = Gson()
        private val connectMutex = Mutex()

        suspend fun connect() {
            connectMutex.withLock {
                if (stompSession != null) {
                    Timber.tag("CHAT_WS").d("[CHAT_WS] connect() called with existing session — session already active, skipping reconnect")
                    return
                }
                try {
                    val currentUser = firebaseAuth?.currentUser
                        ?: throw IllegalStateException("User not logged in")
                    val token = currentUser.getIdToken(false).await().token
                        ?: throw IllegalStateException("Could not get Firebase token")

                    val wsUrl = BuildConfig.WS_BASE_URL
                    Timber.tag("CHAT_WS").d("[CHAT_WS] Connecting to STOMP WebSocket server at: %s", wsUrl)
                    val webSocketClient = OkHttpWebSocketClient(okHttpClient)
                    val stompClient = StompClient(webSocketClient)

                    stompSession = stompClient.connect(
                        url = wsUrl,
                        customStompConnectHeaders = mapOf("token" to token),
                    )
                    Timber.tag("CHAT_WS").d("[CHAT_WS] Connected to STOMP WebSocket server successfully")
                } catch (e: Exception) {
                    stompSession = null
                    Timber.tag("CHAT_WS").e(e, "[CHAT_WS] Error connecting to STOMP WebSocket server")
                    throw e
                }
            }
        }

        /** Returns true if a STOMP session is currently established. */
        fun isConnected(): Boolean = stompSession != null

        fun invalidateSession() {
            stompSession = null
            Timber.tag("CHAT_WS").d("[CHAT_WS] STOMP session invalidated")
        }

        suspend fun subscribeToConversation(conversationId: String): Flow<MessageResponse> {
            val session = stompSession
                ?: throw IllegalStateException("STOMP session not initialized")
            val destination = "/topic/conversation/$conversationId"
            Timber.tag("CHAT_WS").d("[CHAT_WS] Subscribing to %s", destination)

            return session.subscribe(StompSubscribeHeaders(destination)).map { frame ->
                Timber.tag("CHAT_RECEIVE").d(
                    "[CHAT_RECEIVE] Raw STOMP frame received on %s: %s",
                    destination,
                    frame.bodyAsText,
                )
                val response = gson.fromJson(frame.bodyAsText, MessageResponse::class.java)
                Timber.tag("CHAT_RECEIVE").d(
                    "[CHAT_RECEIVE] Parsed MessageResponse: id=%s, sender=%s, type=%s, encryptedContent=%s, content='%s', iv=%s, authTag=%s",
                    response.id,
                    response.senderId,
                    response.type,
                    response.encryptedContent,
                    response.content,
                    response.iv,
                    response.authTag,
                )
                response
            }
        }

        suspend fun subscribeToTyping(conversationId: String): Flow<TypingIndicator> {
            val session = stompSession
                ?: throw IllegalStateException("STOMP session not initialized")

            val destination = "/topic/conversation/$conversationId/typing"
            return session.subscribe(StompSubscribeHeaders(destination)).map { frame ->
                gson.fromJson(frame.bodyAsText, TypingIndicator::class.java)
            }
        }

        suspend fun subscribeToPresence(otherUserId: String): Flow<UserPresence> {
            val session = stompSession
                ?: throw IllegalStateException("STOMP session not initialized")

            val destination = "/topic/user/$otherUserId/presence"
            Timber.tag("CHAT_PRESENCE").d("[CHAT_PRESENCE] Subscribing to presence destination: %s", destination)
            return session.subscribe(StompSubscribeHeaders(destination)).map { frame ->
                Timber.tag("CHAT_PRESENCE").d(
                    "[CHAT_PRESENCE] Raw presence frame on %s: %s",
                    destination,
                    frame.bodyAsText,
                )
                val presence = gson.fromJson(frame.bodyAsText, UserPresence::class.java)
                Timber.tag("CHAT_PRESENCE").d(
                    "[CHAT_PRESENCE] Received presence update for user %s: online=%b, lastSeenAt=%s",
                    otherUserId,
                    presence.online,
                    presence.lastSeenAt,
                )
                presence
            }
        }

        suspend fun sendMessage(
            request: SendMessageRequest,
            otherUserId: String? = null,
        ) {
            val session = stompSession
                ?: throw IllegalStateException("STOMP session not initialized")

            Timber.tag("CHAT_SEND").d(
                "[CHAT_SEND] ChatWebSocketService.sendMessage: convId=%s, otherUserId=%s, type=%s, content='%s'",
                request.conversationId,
                otherUserId,
                request.type,
                request.content,
            )

            val secretKey = keyExchangeHandler.getCachedSecret(request.conversationId)
                ?: keyExchangeHandler.ensureKeysExchanged(request.conversationId, otherUserId)

            if (secretKey == null) {
                Timber.tag("CHAT_SEND").w(
                    "[CHAT_SEND] No shared secret available for conversation %s — sending unencrypted plaintext",
                    request.conversationId,
                )
                val payloadJson = gson.toJson(request)
                Timber.tag("CHAT_SEND").d("[CHAT_SEND] Sending plaintext STOMP payload: %s", payloadJson)
                session.sendText("/app/chat.send", payloadJson)
                return
            }

            val keyBase64 = runCatching { Base64Util.encodeToString(secretKey.encoded) }.getOrDefault("<unknown>")
            Timber.tag("CHAT_SEND").d(
                "[CHAT_SEND] Using SecretKey (Base64): %s to encrypt message for /app/chat.send",
                keyBase64,
            )

            val finalRequest: SendMessageRequest = if (!request.content.isNullOrBlank()) {
                val encrypted = cryptoManager.encrypt(request.content, secretKey)
                request.copy(
                    encryptedContent = encrypted.ciphertext,
                    iv = encrypted.iv,
                    authTag = encrypted.authTag,
                    content = request.content,
                )
            } else {
                request
            }

            val payloadJson = gson.toJson(finalRequest)
            Timber.tag("CHAT_SEND").d(
                "[CHAT_SEND] Sending encrypted STOMP payload to /app/chat.send: %s",
                payloadJson,
            )
            session.sendText("/app/chat.send", payloadJson)
        }

        suspend fun sendTypingIndicator(indicator: TypingIndicator) {
            val session = stompSession
                ?: throw IllegalStateException("STOMP session not initialized")
            session.sendText("/app/chat.typing", gson.toJson(indicator))
        }

        suspend fun disconnect() {
            try {
                stompSession?.disconnect()
                Timber.d("Disconnected STOMP session")
            } catch (e: Exception) {
                Timber.e(e, "Error disconnecting STOMP session")
            } finally {
                stompSession = null
            }
        }
    }
