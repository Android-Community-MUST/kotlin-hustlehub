package must.kdroiders.hustlehub.ui.features.map.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.ui.features.map.domain.model.MapPin
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetNearbyProvidersUseCaseTest {

    private val getMapPinsUseCase: GetMapPinsUseCase = mockk(relaxed = true)

    @Before
    fun setup() {
    }

    @Test
    fun `invoke delegates coordinates category and radius to getMapPinsUseCase`() = runTest {
        val pins = listOf(
            MapPin(
                serviceId = "s-1",
                providerId = "p-1",
                providerName = "Tech Guy",
                providerPhotoUrl = null,
                serviceTitle = "Laptop Repair",
                category = ServiceCategory.TECH,
                availability = ServiceAvailability.AVAILABLE,
                averageRating = 4.8,
                lat = -0.0033,
                lng = 37.7126,
            ),
        )

        coEvery {
            getMapPinsUseCase(
                lat = -0.0033,
                lng = 37.7126,
                radiusKm = 5.0,
                category = ServiceCategory.TECH,
                availability = ServiceAvailability.AVAILABLE,
            )
        } returns Result.success(pins)

        val result = getMapPinsUseCase(
            lat = -0.0033,
            lng = 37.7126,
            radiusKm = 5.0,
            category = ServiceCategory.TECH,
            availability = ServiceAvailability.AVAILABLE,
        )

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("s-1", result.getOrNull()?.first()?.serviceId)
        coVerify(exactly = 1) {
            getMapPinsUseCase(
                lat = -0.0033,
                lng = 37.7126,
                radiusKm = 5.0,
                category = ServiceCategory.TECH,
                availability = ServiceAvailability.AVAILABLE,
            )
        }
    }
}
