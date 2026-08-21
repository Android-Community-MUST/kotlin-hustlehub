package must.kdroiders.hustlehub.ui.features.service.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ReviewRepository
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CheckDuplicateReviewUseCaseTest {

    private val reviewRepository: ReviewRepository = mockk(relaxed = true)
    private lateinit var useCase: CheckDuplicateReviewUseCase

    @Before
    fun setup() {
        useCase = CheckDuplicateReviewUseCase(reviewRepository)
    }

    @Test
    fun `invoke delegates serviceId to reviewRepository checkDuplicateReview`() = runTest {
        coEvery { reviewRepository.checkDuplicateReview("srv-1") } returns Result.success(true)

        val result = useCase("srv-1")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
        coVerify(exactly = 1) { reviewRepository.checkDuplicateReview("srv-1") }
    }
}
