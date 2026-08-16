package must.kdroiders.hustlehub.ui.features.chat.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.core.network.ConnectivityObserver
import must.kdroiders.hustlehub.core.security.KeyExchangeHandler
import must.kdroiders.hustlehub.core.telemetry.HustleAnalytics
import must.kdroiders.hustlehub.core.telemetry.HustleCrashlytics
import must.kdroiders.hustlehub.ui.features.chat.data.local.dao.ConversationDao
import must.kdroiders.hustlehub.ui.features.chat.data.remote.ChatWebSocketService
import must.kdroiders.hustlehub.ui.features.chat.domain.repository.ChatRepository
import must.kdroiders.hustlehub.ui.features.media.data.remote.MediaApiService
import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ServiceRepository
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.CheckDuplicateReviewUseCase
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var context: Context
    private lateinit var chatRepository: ChatRepository
    private lateinit var chatWebSocketService: ChatWebSocketService
    private lateinit var mediaApiService: MediaApiService
    private lateinit var conversationDao: ConversationDao
    private lateinit var serviceRepository: ServiceRepository
    private lateinit var checkDuplicateReviewUseCase: CheckDuplicateReviewUseCase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var userRepository: UserRepository
    private lateinit var keyExchangeHandler: KeyExchangeHandler
    private lateinit var connectivityObserver: ConnectivityObserver
    private lateinit var hustleAnalytics: HustleAnalytics
    private lateinit var hustleCrashlytics: HustleCrashlytics
    private lateinit var viewModel: ChatDetailViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        context = mockk(relaxed = true)
        chatRepository = mockk(relaxed = true)
        coEvery { chatRepository.loadMessageHistory(any(), any()) } returns Result.success(Unit)
        coEvery { chatRepository.getOrCreateConversation(any(), any()) } returns Result.failure(RuntimeException("Not stubbed"))
        chatWebSocketService = mockk(relaxed = true)
        mediaApiService = mockk(relaxed = true)
        conversationDao = mockk(relaxed = true)
        serviceRepository = mockk(relaxed = true)
        coEvery { serviceRepository.getServiceById(any()) } returns Result.failure(RuntimeException("Not stubbed"))
        checkDuplicateReviewUseCase = mockk(relaxed = true)
        firebaseAuth = mockk(relaxed = true)
        firebaseUser = mockk(relaxed = true) {
            every { uid } returns "current-user-1"
        }
        every { firebaseAuth.currentUser } returns firebaseUser
        userRepository = mockk(relaxed = true)
        keyExchangeHandler = mockk(relaxed = true)
        connectivityObserver = mockk(relaxed = true) {
            every { isConnected } returns flowOf(true)
        }
        hustleAnalytics = mockk(relaxed = true)
        hustleCrashlytics = mockk(relaxed = true)

        viewModel = ChatDetailViewModel(
            context = context,
            chatRepository = chatRepository,
            chatWebSocketService = chatWebSocketService,
            mediaApiService = mediaApiService,
            conversationDao = conversationDao,
            serviceRepository = serviceRepository,
            checkDuplicateReviewUseCase = checkDuplicateReviewUseCase,
            firebaseAuth = firebaseAuth,
            userRepository = userRepository,
            keyExchangeHandler = keyExchangeHandler,
            connectivityObserver = connectivityObserver,
            hustleAnalytics = hustleAnalytics,
            hustleCrashlytics = hustleCrashlytics,
        )
    }

    @After
    fun tearDown() {
        viewModel.viewModelScope.coroutineContext.cancelChildren()
        testDispatcher.scheduler.advanceUntilIdle()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState has default values`() {
        val state = viewModel.uiState.value
        assertTrue(state.messages.isEmpty())
        assertFalse(state.isTyping)
        assertFalse(state.isLoading)
    }

    @Test
    fun `blockUser delegates target user ID to userRepository`() = runTest {
        coEvery { userRepository.blockUser("conv-1") } returns Result.success(Unit)

        viewModel.initialize(
            conversationId = "conv-1",
        )

        var callbackCalled = false
        viewModel.blockUser {
            callbackCalled = true
        }

        coVerify { userRepository.blockUser("conv-1") }
        assertTrue(callbackCalled)
    }
}
