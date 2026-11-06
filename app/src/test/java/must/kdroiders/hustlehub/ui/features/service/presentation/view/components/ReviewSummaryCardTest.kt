package must.kdroiders.hustlehub.ui.features.service.presentation.view.components

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class ReviewSummaryCardTest {

    @Test
    fun `calculateDummyDistribution with total 0 returns all zeros`() {
        val result = calculateDummyDistribution(average = 5.0f, total = 0)
        assertArrayEquals(intArrayOf(0, 0, 0, 0, 0), result)
    }

    @Test
    fun `calculateDummyDistribution with 1 five-star review returns 1 for 5 stars and 0 for others`() {
        val result = calculateDummyDistribution(average = 5.0f, total = 1)
        assertArrayEquals(intArrayOf(0, 0, 0, 0, 1), result)
    }

    @Test
    fun `calculateDummyDistribution with 1 four-star review returns 1 for 4 stars`() {
        val result = calculateDummyDistribution(average = 4.0f, total = 1)
        assertArrayEquals(intArrayOf(0, 0, 0, 1, 0), result)
    }

    @Test
    fun `calculateDummyDistribution with 1 one-star review returns 1 for 1 star`() {
        val result = calculateDummyDistribution(average = 1.0f, total = 1)
        assertArrayEquals(intArrayOf(1, 0, 0, 0, 0), result)
    }

    @Test
    fun `calculateDummyDistribution with 5 rating and multiple reviews allocates all to 5 stars`() {
        val result = calculateDummyDistribution(average = 5.0f, total = 5)
        assertArrayEquals(intArrayOf(0, 0, 0, 0, 5), result)
    }

    @Test
    fun `calculateDummyDistribution sum always equals total`() {
        val testCases = listOf(
            4.8f to 10,
            4.2f to 7,
            3.6f to 4,
            2.9f to 3,
            1.5f to 2,
            1.0f to 6,
        )
        for ((avg, total) in testCases) {
            val dist = calculateDummyDistribution(average = avg, total = total)
            assertEquals("Sum of distribution should equal total for avg=$avg, total=$total", total, dist.sum())
        }
    }
}

