package must.kdroiders.hustlehub.ui.features.chat.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import must.kdroiders.hustlehub.core.notification.NotificationHelper
import must.kdroiders.hustlehub.ui.features.chat.data.local.dao.ConversationDao
import must.kdroiders.hustlehub.ui.features.chat.data.remote.ChatWebSocketService
import must.kdroiders.hustlehub.ui.features.chat.domain.model.Message
import must.kdroiders.hustlehub.ui.features.chat.domain.model.MessageType
import must.kdroiders.hustlehub.ui.features.chat.domain.repository.ChatRepository
import must.kdroiders.hustlehub.ui.features.chat.presentation.audio.PlayerState
import must.kdroiders.hustlehub.ui.features.chat.presentation.audio.VoicePlayer
import must.kdroiders.hustlehub.ui.features.media.data.remote.MediaApiService
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ServiceRepository
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.CheckDuplicateReviewUseCase
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.File
import javax.inject.Inject

data class ChatDetailUiState(
    val messages: List<Message> = emptyList(),
    val currentUserId: String = "",
    val otherUserId: String = "",
    val otherUserName: String = "",
    val otherUserAvatar: String? = null,
    val isTyping: Boolean = false,
    val isOtherUserOnline: Boolean = false,
    /** ISO-8601 string; null when user is currently online. */
    val otherUserLastSeenAt: String? = null,
    val isLoading: Boolean = false,
    val playerState: PlayerState = PlayerState(),
    val error: String? = null,
    /** True when the signed-in user is the service provider of this conversation. */
    val isCurrentUserProvider: Boolean = false,
    /** Prevents the auto-generated service card from being re-sent on every re-open. */
    val serviceCardSent: Boolean = false,
    val replyingToMessage: Message? = null,
    val isServiceCompleted: Boolean = false,
    val hasReviewedService: Boolean = false,
)

@HiltViewModel
class ChatDetailViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val chatRepository: ChatRepository,
        private val chatWebSocketService: ChatWebSocketService,
        private val mediaApiService: MediaApiService,
        private val conversationDao: ConversationDao,
        private val serviceRepository: ServiceRepository,
        private val checkDuplicateReviewUseCase: CheckDuplicateReviewUseCase,
        private val firebaseAuth: FirebaseAuth?,
        private val userRepository: must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ChatDetailUiState())
        val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()

        fun blockUser(onSuccess: () -> Unit) {
            val targetId = _uiState.value.otherUserId
            if (targetId.isBlank()) return
            viewModelScope.launch {
                userRepository
                    .blockUser(targetId)
                    .onSuccess {
                        withContext(Dispatchers.Main) {
                            onSuccess()
                        }
                    }.onFailure { e ->
                        Timber.e(e, "Failed to block user $targetId")
                    }
            }
        }

        private var conversationId: String? = null
        private val voicePlayer = VoicePlayer(context)
        private val gson = Gson()

        // Raw typing events from the keyboard — debounced before sending over WebSocket
        private val typingEvents = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)

        // Cancels the auto-clear job when a new typing event arrives
        private var typingClearJob: Job? = null

        private var messagesJob: Job? = null
        private var webSocketJob: Job? = null
        private var presenceJob: Job? = null

        init {
            // Observe voice player state changes
            viewModelScope.launch {
                voicePlayer.playerState.collect { pState ->
                    _uiState.update { it.copy(playerState = pState) }
                }
            }

            // Debounce raw typing events before sending over WebSocket
            typingEvents
                .debounce(TYPING_DEBOUNCE_MS)
                .onEach { isTyping ->
                    sendTypingIndicator(isTyping)
                }.launchIn(viewModelScope)
        }

        /**
         * Initialises the chat conversation.
         *
         * The optional [serviceId] / [serviceTitle] / [serviceCategory] / [servicePriceRange] /
         * [providerName] fields are non-null only when the screen is opened from a service listing.
         * When present and the conversation is brand new (empty history), a SERVICE_CARD message
         * is auto-sent once so the provider sees the request context at a glance.
         */
        fun initialize(
            conversationId: String,
            serviceId: String? = null,
            serviceTitle: String? = null,
            serviceCategory: String? = null,
            servicePriceRange: String? = null,
            providerName: String? = null,
        ) {
            if (this.conversationId == conversationId) return
            this.conversationId = conversationId

            messagesJob?.cancel()
            webSocketJob?.cancel()
            presenceJob?.cancel()

            val currentUid = firebaseAuth?.currentUser?.uid ?: ""
            _uiState.update { it.copy(currentUserId = currentUid, messages = emptyList(), replyingToMessage = null) }

            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }

                // 1. Resolve conversation ID (the input could be a conversation ID or a provider/user ID)
                val cached = withContext(Dispatchers.IO) { conversationDao.getById(conversationId) }
                val resolvedId = if (cached != null) {
                    conversationId
                } else {
                    val result = chatRepository.getOrCreateConversation(
                        otherUserId = conversationId,
                        serviceId = serviceId,
                    )
                    result.fold(
                        onSuccess = { conversation ->
                            this@ChatDetailViewModel.conversationId = conversation.id
                            conversation.id
                        },
                        onFailure = { e ->
                            Timber.e(e, "Failed to resolve conversation, using original ID")
                            conversationId
                        },
                    )
                }

                chatRepository.setActiveConversation(resolvedId)

                // 2. Load cached conversation details to show other user's info in header instantly
                val finalCached = withContext(Dispatchers.IO) { conversationDao.getById(resolvedId) }
                if (finalCached != null) {
                    val sId = finalCached.serviceId
                    var isProvider = false
                    if (sId != null) {
                        val svcResult = serviceRepository.getServiceById(sId)
                        svcResult.onSuccess { svc ->
                            isProvider = svc.providerId == currentUid
                        }
                    }
                    _uiState.update {
                        it.copy(
                            otherUserId = finalCached.otherUserId,
                            otherUserName = finalCached.otherUserName,
                            otherUserAvatar = finalCached.otherUserAvatar,
                            isCurrentUserProvider = isProvider,
                        )
                    }
                } else {
                    // Fallback to providerName if conversation isn't cached yet
                    var isProvider = false
                    if (serviceId != null) {
                        val svcResult = serviceRepository.getServiceById(serviceId)
                        svcResult.onSuccess { svc ->
                            isProvider = svc.providerId == currentUid
                        }
                    }
                    _uiState.update {
                        it.copy(
                            otherUserId = conversationId,
                            otherUserName = providerName ?: "User",
                            otherUserAvatar = null,
                            isCurrentUserProvider = isProvider,
                        )
                    }
                }

                // 3. Cancel the notification for this conversation
                withContext(Dispatchers.IO) {
                    NotificationHelper.cancelConversationNotification(context, resolvedId)
                }

                // 4. Observe local database messages & completion status
                messagesJob = launch {
                    chatRepository.getMessages(resolvedId).collect { messageList ->
                        val hasCompletionMsg = messageList.any { it.type == MessageType.SERVICE_COMPLETED }
                        _uiState.update { state ->
                            state.copy(
                                messages = messageList,
                                isServiceCompleted = state.isServiceCompleted || hasCompletionMsg,
                            )
                        }
                    }
                }

                val activeSvcId = serviceId ?: finalCached?.serviceId
                if (!activeSvcId.isNullOrBlank()) {
                    launch {
                        val hasReviewed = checkDuplicateReviewUseCase(activeSvcId).getOrDefault(false)
                        _uiState.update { it.copy(hasReviewedService = hasReviewed) }
                    }
                }

                // 5. Connect WebSocket and collect incoming messages with structured auto-reconnection
                webSocketJob = launch {
                    var attempt = 0
                    while (isActive) {
                        try {
                            chatWebSocketService.connect()
                            attempt = 0 // Reset attempt count on successful connection

                            coroutineScope {
                                chatRepository
                                    .connectWebSocket(resolvedId)
                                    .catch { e -> Timber.e(e, "Error in WebSocket messages flow") }
                                    .launchIn(this)

                                chatWebSocketService
                                    .subscribeToTyping(resolvedId)
                                    .onEach { typingIndicator ->
                                        if (typingIndicator.senderId != null) {
                                            val isOtherUser = typingIndicator.isTyping
                                            _uiState.update { it.copy(isTyping = isOtherUser) }
                                        }
                                    }.catch { e -> Timber.e(e, "Error in WebSocket typing flow") }
                                    .launchIn(this)

                                val otherUid = finalCached?.otherUserId
                                    ?: if (resolvedId != conversationId) conversationId else null

                                if (otherUid != null) {
                                    chatRepository
                                        .subscribeToPresence(otherUid)
                                        .onEach { presence ->
                                            _uiState.update {
                                                it.copy(
                                                    isOtherUserOnline = presence.online,
                                                    otherUserLastSeenAt = if (presence.online) null else presence.lastSeenAt,
                                                )
                                            }
                                        }.catch { e -> Timber.e(e, "Error in WebSocket presence flow") }
                                        .launchIn(this)
                                }
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "WebSocket connection failed or disconnected, retrying...")
                            chatWebSocketService.disconnect()
                            attempt++
                            kotlinx.coroutines.delay(kotlin.math.min(2000L * attempt, 10000L))
                        }
                    }
                }

                // 6. Load REST history page 0, then auto-send service card if this is a brand-new thread
                chatRepository
                    .loadMessageHistory(resolvedId, 0)
                    .onSuccess {
                        _uiState.update { it.copy(isLoading = false) }
                        val noMessages = _uiState.value.messages.isEmpty()
                        val cardNotYetSent = !_uiState.value.serviceCardSent
                        if (noMessages && !serviceId.isNullOrBlank() && !serviceTitle.isNullOrBlank() && cardNotYetSent) {
                            _uiState.update { it.copy(serviceCardSent = true) }
                            sendServiceCardMessage(
                                serviceId = serviceId,
                                title = serviceTitle,
                                priceRange = "KES ${servicePriceRange ?: ""}",
                                providerName = providerName ?: "",
                                category = serviceCategory ?: "",
                            )
                        }
                    }.onFailure { error ->
                        _uiState.update { it.copy(isLoading = false, error = error.message) }
                    }

                // 7. Mark as read on entry
                chatRepository.markAsRead(resolvedId)
            }
        }

        private fun loadHistoryAndAutoCard(
            serviceId: String? = null,
            serviceTitle: String? = null,
            serviceCategory: String? = null,
            servicePriceRange: String? = null,
            providerName: String? = null,
        ) {
            val id = conversationId ?: return
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }
                chatRepository
                    .loadMessageHistory(id, 0)
                    .onSuccess {
                        _uiState.update { it.copy(isLoading = false) }
                        val noMessages = _uiState.value.messages.isEmpty()
                        val cardNotYetSent = !_uiState.value.serviceCardSent
                        if (noMessages && !serviceId.isNullOrBlank() && !serviceTitle.isNullOrBlank() && cardNotYetSent) {
                            _uiState.update { it.copy(serviceCardSent = true) }
                            sendServiceCardMessage(
                                serviceId = serviceId,
                                title = serviceTitle,
                                priceRange = "KES ${servicePriceRange ?: ""}",
                                providerName = providerName ?: "",
                                category = serviceCategory ?: "",
                            )
                        }
                    }.onFailure { error ->
                        _uiState.update { it.copy(isLoading = false, error = error.message) }
                    }
            }
        }

        fun loadHistory() {
            loadHistoryAndAutoCard()
        }

        private fun markAsRead() {
            val id = conversationId ?: return
            viewModelScope.launch {
                chatRepository.markAsRead(id)
            }
        }

        fun startReplying(message: Message) {
            _uiState.update { it.copy(replyingToMessage = message) }
        }

        fun cancelReplying() {
            _uiState.update { it.copy(replyingToMessage = null) }
        }

        fun deleteMessageForMe(messageId: String) {
            viewModelScope.launch {
                chatRepository
                    .deleteMessageForMe(messageId)
                    .onFailure { error ->
                        _uiState.update { it.copy(error = error.message) }
                    }
            }
        }

        fun deleteMessageForEveryone(messageId: String) {
            viewModelScope.launch {
                chatRepository
                    .deleteMessageForEveryone(messageId)
                    .onFailure { error ->
                        _uiState.update { it.copy(error = error.message) }
                    }
            }
        }

        fun sendTextMessage(content: String) {
            val id = conversationId ?: return
            if (content.isBlank()) return
            val currentReply = uiState.value.replyingToMessage
            viewModelScope.launch {
                // Clear the typing indicator immediately when the message is sent
                sendTypingIndicator(false)
                typingClearJob?.cancel()

                val metadata = if (currentReply != null) {
                    val replyText = when (currentReply.type) {
                        MessageType.VOICE -> "[Voice note]"
                        MessageType.IMAGE -> "[Image]"
                        MessageType.LOCATION -> "[Location]"
                        MessageType.SERVICE_CARD -> "[Service Card]"
                        else -> currentReply.content
                    }
                    val senderName = if (currentReply.senderId == uiState.value.currentUserId) {
                        "You"
                    } else {
                        uiState.value.otherUserName
                    }
                    gson.toJson(
                        mapOf(
                            "replyToId" to currentReply.id,
                            "replyToContent" to replyText,
                            "replyToSenderName" to senderName,
                        ),
                    )
                } else {
                    null
                }

                chatRepository.sendMessage(
                    conversationId = id,
                    type = MessageType.TEXT,
                    content = content,
                    mediaUrl = null,
                    metadata = metadata,
                )
                cancelReplying()
            }
        }

        fun markServiceCompleted() {
            val id = conversationId ?: return
            viewModelScope.launch {
                chatRepository.sendMessage(
                    conversationId = id,
                    type = MessageType.SERVICE_COMPLETED,
                    content = "Service marked as complete [+]",
                )
                _uiState.update { it.copy(isServiceCompleted = true) }
            }
        }

        fun sendVoiceNote(
            file: File,
            durationSeconds: Int,
        ) {
            val id = conversationId ?: return
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                try {
                    val response = withContext(Dispatchers.IO) {
                        val requestFile = file.readBytes().toRequestBody("audio/m4a".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                        val convIdBody = MultipartBody.Part.createFormData("conversationId", id)
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
                        val typeBody = MultipartBody.Part.createFormData("type", "chat")
                        val entityIdBody = MultipartBody.Part.createFormData("entityId", id)
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

        fun sendLocationMessage(
            lat: Double,
            lng: Double,
            label: String,
        ) {
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

        fun sendServiceCardMessage(
            serviceId: String,
            title: String,
            priceRange: String,
            category: String = "",
            providerName: String = "",
        ) {
            val id = conversationId ?: return
            viewModelScope.launch {
                val metadata = gson.toJson(
                    mapOf(
                        "serviceId" to serviceId,
                        "title" to title,
                        "priceRange" to priceRange,
                        "category" to category,
                        "providerName" to providerName,
                    ),
                )
                chatRepository.sendMessage(
                    conversationId = id,
                    type = MessageType.SERVICE_CARD,
                    content = title,
                    mediaUrl = null,
                    metadata = metadata,
                )
            }
        }

        /**
         * Called from the screen whenever the text input changes.
         * Emits to the debounce pipeline — 500ms after the last keystroke it sends
         * isTyping=true. A 3-second auto-clear is also scheduled.
         */
        fun onTypingChanged(text: String) {
            val isTyping = text.isNotBlank()
            typingClearJob?.cancel()
            viewModelScope.launch {
                typingEvents.emit(isTyping)
            }
            if (isTyping) {
                typingClearJob = viewModelScope.launch {
                    delay(TYPING_CLEAR_TIMEOUT_MS)
                    sendTypingIndicator(false)
                }
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
                        ),
                    )
                } catch (e: Exception) {
                    // Ignore WebSocket typing errors
                }
            }
        }

        fun playVoice(url: String) {
            voicePlayer.play(url)
        }

        /** Cycles the playback speed: 1.0x → 1.5x → 2.0x → 1.0x. */
        fun toggleVoicePlaybackSpeed() {
            voicePlayer.toggleSpeed()
        }

        fun clearError() {
            _uiState.update { it.copy(error = null) }
        }

        override fun onCleared() {
            super.onCleared()
            chatRepository.setActiveConversation(null)
            voicePlayer.release()
            viewModelScope.launch {
                chatRepository.disconnectWebSocket()
            }
        }

        private companion object {
            /** Minimum quiet period before a typing=true event is sent to the server. */
            const val TYPING_DEBOUNCE_MS = 500L

            /** How long after the last keystroke the typing indicator is auto-cleared. */
            const val TYPING_CLEAR_TIMEOUT_MS = 3_000L
        }
    }
