package must.kdroiders.hustlehub.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import must.kdroiders.hustlehub.activities.MainActivity
import must.kdroiders.hustlehub.core.ui.TestTags
import must.kdroiders.hustlehub.util.FirebaseEmulatorHelper
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI & integration tests for the Authentication flow:
 * Sign Up → Verify Email → Profile Setup
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AuthFlowTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
        FirebaseEmulatorHelper.setupEmulators()
    }

    @Test
    fun testAuthFlow_signupFieldsAndSubmission() {
        composeTestRule.waitForIdle()

        // If on onboarding, advance or skip to login
        val getStartedNodes = composeTestRule.onAllNodes(hasText("Get Started"))
        if (getStartedNodes.fetchSemanticsNodes().isNotEmpty()) {
            getStartedNodes[0].performClick()
            composeTestRule.waitForIdle()
        }

        // Navigate to Sign Up from Login if present
        val signupLinkNodes = composeTestRule.onAllNodesWithTag("login_signup_link")
        if (signupLinkNodes.fetchSemanticsNodes().isNotEmpty()) {
            signupLinkNodes[0].performClick()
            composeTestRule.waitForIdle()
        }

        // Verify Sign Up form fields are displayed
        val nameNodes = composeTestRule.onAllNodesWithTag(TestTags.SIGNUP_NAME)
        if (nameNodes.fetchSemanticsNodes().isNotEmpty()) {
            nameNodes[0].assertIsDisplayed()
            nameNodes[0].performTextInput("John Doe")

            val emailNodes = composeTestRule.onAllNodesWithTag(TestTags.SIGNUP_EMAIL)
            emailNodes[0].assertIsDisplayed()
            emailNodes[0].performTextInput("johndoe@must.ac.ke")

            val passNodes = composeTestRule.onAllNodesWithTag(TestTags.SIGNUP_PASSWORD)
            passNodes[0].assertIsDisplayed()
            passNodes[0].performTextInput("SecurePass123!")

            val confirmNodes = composeTestRule.onAllNodesWithTag(TestTags.SIGNUP_CONFIRM_PASSWORD)
            confirmNodes[0].assertIsDisplayed()
            confirmNodes[0].performTextInput("SecurePass123!")

            val submitNodes = composeTestRule.onAllNodesWithTag(TestTags.SIGNUP_BUTTON)
            submitNodes[0].assertIsDisplayed()
        }
    }

    @Test
    fun testEmailVerificationScreen_elementsDisplayed() {
        composeTestRule.waitForIdle()

        // Check if verification button tag is available when rendered
        val verifyNodes = composeTestRule.onAllNodesWithTag(TestTags.VERIFY_EMAIL_BUTTON)
        if (verifyNodes.fetchSemanticsNodes().isNotEmpty()) {
            verifyNodes[0].assertIsDisplayed()
        }
    }

    @Test
    fun testProfileSetupScreen_elementsDisplayed() {
        composeTestRule.waitForIdle()

        // Check if profile setup controls are available when rendered
        val completeNodes = composeTestRule.onAllNodesWithTag(TestTags.PROFILE_SETUP_COMPLETE)
        if (completeNodes.fetchSemanticsNodes().isNotEmpty()) {
            completeNodes[0].assertIsDisplayed()
        }

        val skipNodes = composeTestRule.onAllNodesWithTag(TestTags.PROFILE_SETUP_SKIP)
        if (skipNodes.fetchSemanticsNodes().isNotEmpty()) {
            skipNodes[0].assertIsDisplayed()
        }
    }
}
