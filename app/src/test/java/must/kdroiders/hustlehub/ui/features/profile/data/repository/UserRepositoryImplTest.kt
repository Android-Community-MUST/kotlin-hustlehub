package must.kdroiders.hustlehub.ui.features.profile.data.repository

import android.content.Context
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.ui.features.auth.data.remote.AuthApiService
import must.kdroiders.hustlehub.ui.features.media.data.remote.MediaApiService
import must.kdroiders.hustlehub.ui.features.profile.data.remote.FcmTokenRequest
import must.kdroiders.hustlehub.ui.features.profile.data.remote.UserApiService
import must.kdroiders.hustlehub.ui.features.service.data.remote.ServiceApiService
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class UserRepositoryImplTest {
    private lateinit var context: Context
    private lateinit var authApiService: AuthApiService
    private lateinit var userApiService: UserApiService
    private lateinit var mediaApiService: MediaApiService
    private lateinit var serviceApiService: ServiceApiService
    private lateinit var userRepository: UserRepositoryImpl

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        authApiService = mockk(relaxed = true)
        userApiService = mockk(relaxed = true)
        mediaApiService = mockk(relaxed = true)
        serviceApiService = mockk(relaxed = true)
        val userDao: must.kdroiders.hustlehub.ui.features.profile.data.local.dao.UserDao = mockk(relaxed = true)
        userRepository = UserRepositoryImpl(
            context = context,
            authApiService = authApiService,
            userApiService = userApiService,
            mediaApiService = mediaApiService,
            serviceApiService = serviceApiService,
            userDao = userDao,
        )
    }

    @Test
    fun `updateFcmToken calls userApiService updateFcmToken`() =
        runTest {
            coEvery { userApiService.updateFcmToken(FcmTokenRequest("test-token")) } returns Response.success(Unit)

            val result = userRepository.updateFcmToken("test-token")

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { userApiService.updateFcmToken(FcmTokenRequest("test-token")) }
        }

    @Test
    fun `removeFcmToken calls userApiService removeFcmToken`() =
        runTest {
            coEvery { userApiService.removeFcmToken("test-token") } returns Response.success(Unit)

            val result = userRepository.removeFcmToken("test-token")

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { userApiService.removeFcmToken("test-token") }
        }
}
