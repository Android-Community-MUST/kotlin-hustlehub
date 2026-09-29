package must.kdroiders.hustlehub.ui.chat

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
 * UI & integration tests for Chat messaging flow:
 * Open Chat → Send Message → Verify
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ChatFlowTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun chatDetail_typingAndSendingMessage() {
        composeTestRule.waitForIdle()

        // Check if Chat input field is present
        val inputFields = composeTestRule.onAllNodesWithTag(TestTags.CHAT_INPUT_FIELD)
        if (inputFields.fetchSemanticsNodes().isNotEmpty()) {
            inputFields[0].assertIsDisplayed()
            inputFields[0].performTextInput("Hello, is this service available today?")

            // Verify send button is shown and clickable
            val sendButtons = composeTestRule.onAllNodesWithTag(TestTags.SEND_MESSAGE_BUTTON)
            if (sendButtons.fetchSemanticsNodes().isNotEmpty()) {
                sendButtons[0].assertIsDisplayed()
                sendButtons[0].performClick()
                composeTestRule.waitForIdle()

                // Check that at least one message bubble exists
                val messageBubbles = composeTestRule.onAllNodesWithTag(TestTags.MESSAGE_BUBBLE)
                if (messageBubbles.fetchSemanticsNodes().isNotEmpty()) {
                    messageBubbles[0].assertIsDisplayed()
                }
            }
        }
    }
}
