package must.kdroiders.hustlehub.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewPreferencesTest {
    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var reviewPreferences: ReviewPreferences

    @Before
    fun setUp() {
        val testDataStore =
            PreferenceDataStoreFactory.create(
                scope = testScope,
                produceFile = { tmpFolder.newFile("test_review_prefs.preferences_pb") },
            )
        reviewPreferences = ReviewPreferences(testDataStore)
    }

    @Test
    fun `initial reviewMetrics has default empty values`() =
        runTest {
            val metrics = reviewPreferences.reviewMetrics.first()
            assertEquals(0L, metrics.firstInstallTime)
            assertEquals(0, metrics.appOpenCount)
            assertEquals(0, metrics.significantActionsCount)
            assertEquals(0L, metrics.lastPromptTime)
            assertFalse(metrics.hasRatedApp)
        }

    @Test
    fun `recordAppOpen sets firstInstallTime and increments appOpenCount`() =
        runTest {
            reviewPreferences.recordAppOpen()
            var metrics = reviewPreferences.reviewMetrics.first()
            assertTrue(metrics.firstInstallTime > 0L)
            assertEquals(1, metrics.appOpenCount)

            val firstInstall = metrics.firstInstallTime
            reviewPreferences.recordAppOpen()
            metrics = reviewPreferences.reviewMetrics.first()
            assertEquals(firstInstall, metrics.firstInstallTime)
            assertEquals(2, metrics.appOpenCount)
        }

    @Test
    fun `recordSignificantAction increments action counter`() =
        runTest {
            reviewPreferences.recordSignificantAction()
            reviewPreferences.recordSignificantAction()

            val metrics = reviewPreferences.reviewMetrics.first()
            assertEquals(2, metrics.significantActionsCount)
        }

    @Test
    fun `recordPromptShown updates lastPromptTime`() =
        runTest {
            reviewPreferences.recordPromptShown()
            val metrics = reviewPreferences.reviewMetrics.first()
            assertTrue(metrics.lastPromptTime > 0L)
        }

    @Test
    fun `markHasRatedApp sets hasRatedApp to true`() =
        runTest {
            reviewPreferences.markHasRatedApp()
            val metrics = reviewPreferences.reviewMetrics.first()
            assertTrue(metrics.hasRatedApp)
        }

    @Test
    fun `resetForTesting clears all review preferences`() =
        runTest {
            reviewPreferences.recordAppOpen()
            reviewPreferences.recordSignificantAction()
            reviewPreferences.markHasRatedApp()
            reviewPreferences.recordPromptShown()

            reviewPreferences.resetForTesting()

            val metrics = reviewPreferences.reviewMetrics.first()
            assertEquals(0L, metrics.firstInstallTime)
            assertEquals(0, metrics.appOpenCount)
            assertEquals(0, metrics.significantActionsCount)
            assertEquals(0L, metrics.lastPromptTime)
            assertFalse(metrics.hasRatedApp)
        }
}
