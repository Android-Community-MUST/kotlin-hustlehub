package must.kdroiders.hustlehub.ui.features.service.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.ui.features.service.domain.model.ReviewEligibility
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ReviewRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetReviewEligibilityUseCaseTest {
    private val reviewRepository: ReviewRepository = mockk(relaxed = true)
    private lateinit var useCase: GetReviewEligibilityUseCase

    @Before
    fun setup() {
        useCase = GetReviewEligibilityUseCase(reviewRepository)
    }

    @Test
    fun `invoke delegates serviceId to reviewRepository getReviewEligibility`() =
        runTest {
            val expected = ReviewEligibility(
                canReview = true,
                reason = "ELIGIBLE",
                isVerified = true,
            )
            coEvery { reviewRepository.getReviewEligibility("srv-1") } returns Result.success(expected)

            val result = useCase("srv-1")

            assertTrue(result.isSuccess)
            assertEquals(expected, result.getOrNull())
            coVerify(exactly = 1) { reviewRepository.getReviewEligibility("srv-1") }
        }
}
