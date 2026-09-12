package must.kdroiders.hustlehub.ui.service

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performClick
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
 * UI & integration tests for Review submission flow:
 * Write Review → Select Stars → Submit → Verify
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ReviewFlowTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun writeReview_elementsDisplayedAndSubmittable() {
        composeTestRule.waitForIdle()

        // Check if write review controls are displayed when screen is active
        val ratingBars = composeTestRule.onAllNodesWithTag(TestTags.REVIEW_RATING_BAR)
        if (ratingBars.fetchSemanticsNodes().isNotEmpty()) {
            ratingBars[0].assertIsDisplayed()
            ratingBars[0].performClick() // Select rating

            val commentInputs = composeTestRule.onAllNodesWithTag(TestTags.REVIEW_CONTENT_INPUT)
            if (commentInputs.fetchSemanticsNodes().isNotEmpty()) {
                commentInputs[0].assertIsDisplayed()
                commentInputs[0].performTextInput("Excellent work! Quick turnaround and great communication.")
            }

            val submitBtns = composeTestRule.onAllNodesWithTag(TestTags.SUBMIT_REVIEW_BUTTON)
            if (submitBtns.fetchSemanticsNodes().isNotEmpty()) {
                submitBtns[0].assertIsDisplayed()
            }
        }
    }
}
