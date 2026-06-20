package must.kdroiders.hustlehub.ui.features.chat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import must.kdroiders.hustlehub.ui.features.chat.data.local.dao.ConversationDao
import must.kdroiders.hustlehub.ui.features.chat.domain.model.Message
import must.kdroiders.hustlehub.ui.features.chat.domain.model.MessageType
import must.kdroiders.hustlehub.ui.features.chat.data.remote.ChatWebSocketService
import must.kdroiders.hustlehub.data.remote.MediaApiService
import must.kdroiders.hustlehub.ui.features.chat.domain.repository.ChatRepository
import must.kdroiders.hustlehub.ui.features.chat.presentation.audio.PlayerState
import must.kdroiders.hustlehub.ui.features.chat.presentation.audio.VoicePlayer
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.File
import javax.inject.Inject

data class ChatDetailUiState(
    val messages: List<Message> = emptyList(),
    val currentUserId: String = "",
    val otherUserName: String = "",
    val otherUserAvatar: String? = null,
    val isTyping: Boolean = false,
    val isLoading: Boolean = false,
    val playerState: PlayerState = PlayerState(),
    val error: String? = null,
)

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val chatWebSocketService: ChatWebSocketService,
    private val mediaApiService: MediaApiService,
    private val conversationDao: ConversationDao,
    private val firebaseAuth: FirebaseAuth?,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatDetailUiState())
    val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()

    private var conversationId: String? = null
    private val voicePlayer = VoicePlayer()
    private val gson = Gson()

    init {
        // Observe voice player state changes
        viewModelScope.launch {
            voicePlayer.playerState.collect { pState ->
                _uiState.update { it.copy(playerState = pState) }
            }
        }
    }

    fun initialize(conversationId: String) {
        if (this.conversationId == conversationId) return
        this.conversationId = conversationId

        _uiState.update { it.copy(currentUserId = firebaseAuth?.currentUser?.uid ?: "") }

        // Load cached conversation details to show other user's info in header instantly
        viewModelScope.launch(Dispatchers.IO) {
            val cached = conversationDao.getById(conversationId)
            if (cached != null) {
                _uiState.update {
                    it.copy(
                        otherUserName = cached.otherUserName,
                        otherUserAvatar = cached.otherUserAvatar,
                    )
                }
            }
        }

        // Observe local database messages (Room is single source of truth)
        viewModelScope.launch {
            chatRepository.getMessages(conversationId).collect { messageList ->
                _uiState.update { it.copy(messages = messageList) }
            }
        }

        // Connect WebSocket and collect incoming messages
        viewModelScope.launch {
            try {
                chatRepository.connectWebSocket(conversationId)
                    .catch { e -> Timber.e(e, "Error in WebSocket messages flow") }
                    .launchIn(viewModelScope)

                // Also subscribe to typing indicators for this conversation
                chatWebSocketService.subscribeToTyping(conversationId)
                    .onEach { typingIndicator ->
                        if (typingIndicator.senderId != null) {
                            // Only show typing if it's the other user typing
                            val isOtherUser = typingIndicator.isTyping
                            _uiState.update { it.copy(isTyping = isOtherUser) }
                        }
                    }
                    .catch { e -> Timber.e(e, "Error in WebSocket typing flow") }
                    .launchIn(viewModelScope)
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize WebSocket")
            }
        }

        // Load REST history page 0
        loadHistory()

        // Mark as read on entry
        markAsRead()
    }

    fun loadHistory() {
        val id = conversationId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            chatRepository.loadMessageHistory(id, 0)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    private fun markAsRead() {
        val id = conversationId ?: return
        viewModelScope.launch {
            chatRepository.markAsRead(id)
        }
    }

    fun sendTextMessage(content: String) {
        val id = conversationId ?: return
        if (content.isBlank()) return
        viewModelScope.launch {
            chatRepository.sendMessage(id, MessageType.TEXT, content)
        }
    }

    fun sendVoiceNote(file: File, durationSeconds: Int) {
        val id = conversationId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = withContext(Dispatchers.IO) {
                    val requestFile = file.readBytes().toRequestBody("audio/mp4".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                    val convIdBody = id.toRequestBody("text/plain".toMediaTypeOrNull())
                    mediaApiService.uploadVoiceNote(body, convIdBody)
                }

                if (response.success && response.data != null) {
                    val url = response.data.url
                    val metadata = gson.toJson(mapOf("durationSeconds" to durationSeconds))
                    chatRepository.sendMessage(id, MessageType.VOICE, "Voice note", url, metadata)
                    file.delete()
                } else {
                    _uiState.update { it.copy(error = response.message) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to upload voice note")
                _uiState.update { it.copy(error = e.message ?: "Failed to upload voice note") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun sendImageMessage(imageBytes: ByteArray) {
        val id = conversationId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = withContext(Dispatchers.IO) {
                    val requestFile = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                    val fileName = "chat_img_${System.currentTimeMillis()}.jpg"
                    val body = MultipartBody.Part.createFormData("file", fileName, requestFile)
                    val typeBody = "CHAT_IMAGE".toRequestBody("text/plain".toMediaTypeOrNull())
                    val entityIdBody = id.toRequestBody("text/plain".toMediaTypeOrNull())
                    mediaApiService.uploadImage(body, typeBody, entityIdBody)
                }

                if (response.success && response.data != null) {
                    val url = response.data.url
                    chatRepository.sendMessage(id, MessageType.IMAGE, "", url)
                } else {
                    _uiState.update { it.copy(error = response.message) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to upload chat image")
                _uiState.update { it.copy(error = e.message ?: "Failed to upload chat image") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun sendLocationMessage(lat: Double, lng: Double, label: String) {
        val id = conversationId ?: return
        viewModelScope.launch {
            val metadata = gson.toJson(mapOf("lat" to lat, "lng" to lng, "label" to label))
            chatRepository.sendMessage(
                conversationId = id,
                type = MessageType.LOCATION,
                content = "Location: $label",
                mediaUrl = null,
                metadata = metadata,
            )
        }
    }

    fun sendServiceCardMessage(serviceId: String, title: String, priceRange: String) {
        val id = conversationId ?: return
        viewModelScope.launch {
            val metadata = gson.toJson(mapOf("serviceId" to serviceId, "title" to title, "priceRange" to priceRange))
            chatRepository.sendMessage(
                conversationId = id,
                type = MessageType.SERVICE_CARD,
                content = title,
                mediaUrl = null,
                metadata = metadata,
            )
        }
    }

    fun sendTypingIndicator(isTyping: Boolean) {
        val id = conversationId ?: return
        viewModelScope.launch {
            try {
                chatWebSocketService.sendTypingIndicator(
                    must.kdroiders.hustlehub.ui.features.chat.data.remote.dto.TypingIndicator(
                        conversationId = id,
                        senderId = "", // Filled by server
                        isTyping = isTyping,
                    )
                )
            } catch (e: Exception) {
                // Ignore WebSocket typing errors
            }
        }
    }

    fun playVoice(url: String) {
        voicePlayer.play(url)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        voicePlayer.stop()
        viewModelScope.launch {
            chatRepository.disconnectWebSocket()
        }
    }
}
