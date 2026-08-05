package must.kdroiders.hustlehub.ui.features.privacy.presentation.viewmodel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.ui.features.privacy.data.remote.dto.MessagingPermission
import must.kdroiders.hustlehub.ui.features.privacy.data.remote.dto.PrivacySettingsDto
import must.kdroiders.hustlehub.ui.features.privacy.data.remote.dto.UpdatePrivacySettingsRequestDto
import must.kdroiders.hustlehub.ui.features.privacy.domain.repository.PrivacyRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PrivacySettingsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val privacyRepository: PrivacyRepository = mockk(relaxed = true)

    private lateinit var viewModel: PrivacySettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        coEvery { privacyRepository.getPrivacySettings() } returns Result.success(
            PrivacySettingsDto(
                showLocationOnMap = true,
                messagingPermission = MessagingPermission.EVERYONE,
                showOnlineStatus = true,
                showLastSeen = true,
                allowReviews = true,
            ),
        )

        viewModel = PrivacySettingsViewModel(privacyRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadPrivacySettings populates state on init`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.showLocationOnMap)
        assertEquals(MessagingPermission.EVERYONE, state.messagingPermission)
        assertTrue(state.showOnlineStatus)
    }

    @Test
    fun `onLocationSharingToggled updates state and calls repository`() = runTest {
        coEvery { privacyRepository.updatePrivacySettings(any()) } returns Result.success(
            PrivacySettingsDto(showLocationOnMap = false),
        )

        viewModel.onLocationSharingToggled(false)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showLocationOnMap)
        coVerify(exactly = 1) {
            privacyRepository.updatePrivacySettings(UpdatePrivacySettingsRequestDto(showLocationOnMap = false))
        }
    }

    @Test
    fun `onMessagingPermissionSelected updates messaging permission`() = runTest {
        coEvery { privacyRepository.updatePrivacySettings(any()) } returns Result.success(
            PrivacySettingsDto(messagingPermission = MessagingPermission.VERIFIED_ONLY),
        )

        viewModel.onMessagingPermissionSelected(MessagingPermission.VERIFIED_ONLY)
        advanceUntilIdle()

        assertEquals(MessagingPermission.VERIFIED_ONLY, viewModel.uiState.value.messagingPermission)
        coVerify(exactly = 1) {
            privacyRepository.updatePrivacySettings(UpdatePrivacySettingsRequestDto(messagingPermission = MessagingPermission.VERIFIED_ONLY))
        }
    }
}
