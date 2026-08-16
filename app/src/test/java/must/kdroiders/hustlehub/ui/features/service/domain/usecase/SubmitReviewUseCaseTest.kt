package must.kdroiders.hustlehub.ui.features.service.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.ui.features.service.domain.model.Review
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ReviewRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SubmitReviewUseCaseTest {

    private lateinit var repository: ReviewRepository
    private lateinit var useCase: SubmitReviewUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = SubmitReviewUseCase(repository)
    }

    @Test
    fun `submitReview forwards parameters to repository`() = runTest {
        val mockReview = Review(
            id = "rev-1",
            serviceId = "service-123",
            providerId = "provider-1",
            customerId = "customer-1",
            customerName = "Anonymous",
            customerAvatarUrl = "",
            rating = 5,
            comment = "Great service!",
            isAnonymous = true,
            createdAt = System.currentTimeMillis(),
        )

        coEvery {
            repository.submitReview("service-123", 5, "Great service!", true)
        } returns Result.success(mockReview)

        val result = useCase("service-123", 5, "Great service!", true)

        assertTrue(result.isSuccess)
        assertEquals(mockReview, result.getOrNull())
        coVerify(exactly = 1) { repository.submitReview("service-123", 5, "Great service!", true) }
    }

    @Test
    fun `submitReview failure returns failure result`() = runTest {
        coEvery {
            repository.submitReview(any(), any(), any(), any())
        } returns Result.failure(RuntimeException("Already reviewed"))

        val result = useCase("service-123", 4, null, false)

        assertTrue(result.isFailure)
        assertEquals("Already reviewed", result.exceptionOrNull()?.message)
    }
}
