package must.kdroiders.hustlehub.feature.chat.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import must.kdroiders.hustlehub.BuildConfig
import must.kdroiders.hustlehub.feature.chat.data.remote.dto.MessageResponse
import must.kdroiders.hustlehub.feature.chat.data.remote.dto.SendMessageRequest
import must.kdroiders.hustlehub.feature.chat.data.remote.dto.TypingIndicator
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
class ChatWebSocketService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val firebaseAuth: FirebaseAuth?,
) {
    private var stompSession: StompSession? = null
    private val gson = Gson()

    suspend fun connect() {
        if (stompSession != null) return
        try {
            val currentUser = firebaseAuth?.currentUser ?: throw IllegalStateException("User not logged in")
            val token = currentUser.getIdToken(false).await().token ?: throw IllegalStateException("Could not get Firebase token")
            
            val wsUrl = "${BuildConfig.WS_BASE_URL}?token=$token"
            val webSocketClient = OkHttpWebSocketClient(okHttpClient)
            val stompClient = StompClient(webSocketClient)
            
            stompSession = stompClient.connect(wsUrl)
            Timber.d("Connected to STOMP WebSocket server")
        } catch (e: Exception) {
            Timber.e(e, "Error connecting to STOMP WebSocket server")
            throw e
        }
    }

    suspend fun subscribeToConversation(conversationId: String): Flow<MessageResponse> {
        val session = stompSession ?: throw IllegalStateException("STOMP session not initialized")
        val destination = "/topic/conversation/$conversationId"
        return session.subscribe(StompSubscribeHeaders(destination)).map { frame ->
            gson.fromJson(frame.bodyAsText, MessageResponse::class.java)
        }
    }

    suspend fun subscribeToTyping(conversationId: String): Flow<TypingIndicator> {
        val session = stompSession ?: throw IllegalStateException("STOMP session not initialized")
        val destination = "/topic/conversation/$conversationId/typing"
        return session.subscribe(StompSubscribeHeaders(destination)).map { frame ->
            gson.fromJson(frame.bodyAsText, TypingIndicator::class.java)
        }
    }

    suspend fun sendMessage(request: SendMessageRequest) {
        val session = stompSession ?: throw IllegalStateException("STOMP session not initialized")
        val payloadJson = gson.toJson(request)
        session.sendText("/app/chat.send", payloadJson)
    }

    suspend fun sendTypingIndicator(indicator: TypingIndicator) {
        val session = stompSession ?: throw IllegalStateException("STOMP session not initialized")
        val payloadJson = gson.toJson(indicator)
        session.sendText("/app/chat.typing", payloadJson)
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
