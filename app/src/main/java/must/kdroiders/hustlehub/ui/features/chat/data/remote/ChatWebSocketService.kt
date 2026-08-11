package must.kdroiders.hustlehub.ui.features.chat.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import must.kdroiders.hustlehub.BuildConfig
import must.kdroiders.hustlehub.core.security.CryptoManager
import must.kdroiders.hustlehub.core.security.EncryptedPayload
import must.kdroiders.hustlehub.core.security.KeyExchangeHandler
import must.kdroiders.hustlehub.ui.features.chat.data.remote.dto.EncryptedMessagePayload
import must.kdroiders.hustlehub.ui.features.chat.data.remote.dto.MessageResponse
import must.kdroiders.hustlehub.ui.features.chat.data.remote.dto.SendMessageRequest
import must.kdroiders.hustlehub.ui.features.chat.data.remote.dto.TypingIndicator
import must.kdroiders.hustlehub.ui.features.chat.data.remote.dto.UserPresence
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
                if (stompSession != null) return
                try {
                    val currentUser = firebaseAuth?.currentUser
                        ?: throw IllegalStateException("User not logged in")
                    val token = currentUser.getIdToken(false).await().token
                        ?: throw IllegalStateException("Could not get Firebase token")

                    val wsUrl = BuildConfig.WS_BASE_URL
                    val webSocketClient = OkHttpWebSocketClient(okHttpClient)
                    val stompClient = StompClient(webSocketClient)

                    stompSession = stompClient.connect(
                        url = wsUrl,
                        customStompConnectHeaders = mapOf("token" to token),
                    )
                    Timber.d("Connected to STOMP WebSocket server")
                } catch (e: Exception) {
                    Timber.e(e, "Error connecting to STOMP WebSocket server")
                    throw e
                }
            }
        }

        suspend fun subscribeToConversation(conversationId: String): Flow<MessageResponse> {
            val session = stompSession
                ?: throw IllegalStateException("STOMP session not initialized")
            val destination = "/topic/conversation/$conversationId"

            return session.subscribe(StompSubscribeHeaders(destination)).map { frame ->
                val rawJson = frame.bodyAsText

                // Try to decrypt if it's an encrypted payload
                try {
                    val encrypted = gson.fromJson(rawJson, EncryptedMessagePayload::class.java)
                    if (encrypted.encryptedContent != null && encrypted.iv != null) {
                        val secretKey = keyExchangeHandler.getCachedSecret(conversationId)
                        if (secretKey != null) {
                            val payload = EncryptedPayload(
                                ciphertext = encrypted.encryptedContent,
                                iv = encrypted.iv,
                                authTag = encrypted.authTag,
                            )
                            val plaintext = cryptoManager.decrypt(payload, secretKey)
                            val baseResponse = gson.fromJson(rawJson, MessageResponse::class.java)
                            baseResponse.copy(content = plaintext)
                        } else {
                            val baseResponse = gson.fromJson(rawJson, MessageResponse::class.java)
                            baseResponse.copy(content = "\uD83D\uDD12 Encrypted message")
                        }
                    } else {
                        gson.fromJson(rawJson, MessageResponse::class.java)
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Decrypt failed, falling back to plaintext")
                    gson.fromJson(rawJson, MessageResponse::class.java)
                }
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
            return session.subscribe(StompSubscribeHeaders(destination)).map { frame ->
                gson.fromJson(frame.bodyAsText, UserPresence::class.java)
            }
        }

        /** Encrypts content if E2EE keys are exchanged, otherwise sends plaintext. */
        suspend fun sendMessage(request: SendMessageRequest) {
            val session = stompSession
                ?: throw IllegalStateException("STOMP session not initialized")

            val secretKey = keyExchangeHandler.getCachedSecret(request.conversationId)

            val payloadJson = if (secretKey != null && request.content != null) {
                val encrypted = cryptoManager.encrypt(request.content, secretKey)
                val encryptedRequest = request.copy(
                    content = gson.toJson(
                        EncryptedMessagePayload(
                            encryptedContent = encrypted.ciphertext,
                            iv = encrypted.iv,
                            authTag = encrypted.authTag,
                            type = request.type,
                        ),
                    ),
                )
                gson.toJson(encryptedRequest)
            } else {
                gson.toJson(request)
            }

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
                stompSession = null
                Timber.d("Disconnected STOMP session")
            } catch (e: Exception) {
                Timber.e(e, "Error disconnecting STOMP session")
            }
        }
    }
