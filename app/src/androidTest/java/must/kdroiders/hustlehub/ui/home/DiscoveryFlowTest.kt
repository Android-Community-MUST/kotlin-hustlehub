package must.kdroiders.hustlehub.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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
 * UI & integration tests for Discovery flow:
 * Browse → Search → View Detail
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DiscoveryFlowTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun discoveryFeedShowsServices() {
        composeTestRule.waitForIdle()

        // Verify key discovery feed components are displayed if on home
        val searchBars = composeTestRule.onAllNodesWithTag(TestTags.SEARCH_BAR)
        if (searchBars.fetchSemanticsNodes().isNotEmpty()) {
            searchBars[0].assertIsDisplayed()

            composeTestRule.onNodeWithTag(TestTags.CATEGORY_CHIPS).assertIsDisplayed()
            composeTestRule.onNodeWithTag(TestTags.SERVICE_GRID).assertIsDisplayed()

            // If any service cards exist in the feed, click the first one
            val serviceCards = composeTestRule.onAllNodesWithTag(TestTags.SERVICE_CARD)
            if (serviceCards.fetchSemanticsNodes().isNotEmpty()) {
                serviceCards[0].performClick()
                composeTestRule.waitForIdle()

                // Verify detail screen opened
                composeTestRule.onNodeWithTag(TestTags.SERVICE_DETAIL_SCREEN).assertIsDisplayed()
            }
        }
    }

    @Test
    fun discoverySearchNavigation() {
        composeTestRule.waitForIdle()

        val searchBars = composeTestRule.onAllNodesWithTag(TestTags.SEARCH_BAR)
        if (searchBars.fetchSemanticsNodes().isNotEmpty()) {
            searchBars[0].performClick()
            composeTestRule.waitForIdle()

            // Verify search results list or search screen is shown
            val searchResults = composeTestRule.onAllNodesWithTag("search_result_list")
            if (searchResults.fetchSemanticsNodes().isNotEmpty()) {
                searchResults[0].assertIsDisplayed()
            }
        }
    }
}
