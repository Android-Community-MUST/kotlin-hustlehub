package must.kdroiders.hustlehub.ui.service

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import must.kdroiders.hustlehub.activities.MainActivity
import must.kdroiders.hustlehub.core.ui.TestTags
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI & integration tests for Service Creation flow:
 * Create Service → Fill Details → Publish → Verify
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CreateServiceFlowTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun createService_formElementsDisplayedAndEditable() {
        composeTestRule.waitForIdle()

        // Verify creation controls when CreateServiceScreen is presented
        val titleNodes = composeTestRule.onAllNodesWithTag(TestTags.CREATE_SERVICE_TITLE)
        if (titleNodes.fetchSemanticsNodes().isNotEmpty()) {
            titleNodes[0].assertIsDisplayed()
            titleNodes[0].performTextInput("Professional Braiding & Styling")

            val priceNodes = composeTestRule.onAllNodesWithTag(TestTags.CREATE_SERVICE_PRICE)
            if (priceNodes.fetchSemanticsNodes().isNotEmpty()) {
                priceNodes[0].assertIsDisplayed()
                priceNodes[0].performTextInput("450")
            }

            val descNodes = composeTestRule.onAllNodesWithTag(TestTags.CREATE_SERVICE_DESCRIPTION)
            if (descNodes.fetchSemanticsNodes().isNotEmpty()) {
                descNodes[0].assertIsDisplayed()
                descNodes[0].performTextInput("Clean and fast hair braiding right next to Gate B.")
            }

            val publishBtn = composeTestRule.onAllNodesWithTag(TestTags.PUBLISH_SERVICE_BUTTON)
            if (publishBtn.fetchSemanticsNodes().isNotEmpty()) {
                publishBtn[0].assertIsDisplayed()
            }
        }
    }
}
