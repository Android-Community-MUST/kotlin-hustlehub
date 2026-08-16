package must.kdroiders.hustlehub.ui.features.home.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.ui.features.home.data.remote.AiSearchRequest
import must.kdroiders.hustlehub.ui.features.home.data.remote.AiSearchResponse
import must.kdroiders.hustlehub.ui.features.home.data.remote.QueryUnderstanding
import must.kdroiders.hustlehub.ui.features.home.domain.repository.AiSearchRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AiSearchUseCaseTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: AiSearchRepository
    private lateinit var useCase: AiSearchUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        useCase = AiSearchUseCase(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `aiSearch delegates query location and maxResults to repository`() = runTest {
        val mockLocation = AiSearchRequest.UserLocationDto(lat = 0.0515, lng = 37.6456)
        val expectedResponse = AiSearchResponse(
            matches = emptyList(),
            queryUnderstanding = QueryUnderstanding(service = null, location = null, maxPrice = null, category = null),
        )

        coEvery { repository.aiSearch("I need a haircut", mockLocation, 10) } returns Result.success(expectedResponse)

        val result = useCase("I need a haircut", mockLocation, 10)

        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
        coVerify(exactly = 1) { repository.aiSearch("I need a haircut", mockLocation, 10) }
    }

    @Test
    fun `aiSearch failure returns failure result`() = runTest {
        coEvery { repository.aiSearch(any(), any(), any()) } returns Result.failure(RuntimeException("AI service unavailable"))

        val result = useCase("I need a tutor", null, 5)

        assertTrue(result.isFailure)
        assertEquals("AI service unavailable", result.exceptionOrNull()?.message)
    }
}
