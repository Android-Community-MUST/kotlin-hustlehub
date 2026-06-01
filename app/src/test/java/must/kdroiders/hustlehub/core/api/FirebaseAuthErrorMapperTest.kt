package must.kdroiders.hustlehub.core.api

import com.google.firebase.auth.FirebaseAuthException
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies that [FirebaseAuthErrorMapper] maps every required error code to a
 * user-friendly message and that non-Firebase exceptions fall through to their
 * own message.
 */
class FirebaseAuthErrorMapperTest {

    private fun fakeFirebaseException(errorCode: String, message: String? = null): FirebaseAuthException {
        val ex = mockk<FirebaseAuthException>(relaxed = true)
        every { ex.errorCode } returns errorCode
        every { ex.message } returns message
        return ex
    }

    // ── Sign-up errors ────────────────────────────────────────────────────────

    @Test
    fun `should_return_friendly_message_when_email_already_in_use`() {
        val result = FirebaseAuthErrorMapper.map(
            fakeFirebaseException("ERROR_EMAIL_ALREADY_IN_USE")
        )
        assertTrue(result.contains("already exists", ignoreCase = true))
    }

    @Test
    fun `should_return_friendly_message_when_email_is_invalid`() {
        val result = FirebaseAuthErrorMapper.map(
            fakeFirebaseException("ERROR_INVALID_EMAIL")
        )
        assertTrue(result.contains("valid email", ignoreCase = true))
    }

    @Test
    fun `should_return_friendly_message_when_password_is_weak`() {
        val result = FirebaseAuthErrorMapper.map(
            fakeFirebaseException("ERROR_WEAK_PASSWORD")
        )
        assertTrue(result.contains("weak", ignoreCase = true) || result.contains("password", ignoreCase = true))
    }

    // ── Sign-in errors ────────────────────────────────────────────────────────

    @Test
    fun `should_return_friendly_message_when_user_not_found`() {
        val result = FirebaseAuthErrorMapper.map(
            fakeFirebaseException("ERROR_USER_NOT_FOUND")
        )
        assertTrue(result.contains("No account found", ignoreCase = true))
    }

    @Test
    fun `should_return_friendly_message_when_password_is_wrong`() {
        val result = FirebaseAuthErrorMapper.map(
            fakeFirebaseException("ERROR_WRONG_PASSWORD")
        )
        assertTrue(result.contains("Incorrect password", ignoreCase = true))
    }

    @Test
    fun `should_return_friendly_message_when_credential_is_invalid`() {
        val result = FirebaseAuthErrorMapper.map(
            fakeFirebaseException("ERROR_INVALID_CREDENTIAL")
        )
        assertTrue(result.contains("Incorrect password", ignoreCase = true))
    }

    @Test
    fun `should_return_friendly_message_when_user_is_disabled`() {
        val result = FirebaseAuthErrorMapper.map(
            fakeFirebaseException("ERROR_USER_DISABLED")
        )
        assertTrue(result.contains("disabled", ignoreCase = true))
    }

    // ── Network / rate limit ─────────────────────────────────────────────────

    @Test
    fun `should_return_friendly_message_when_network_request_failed`() {
        val result = FirebaseAuthErrorMapper.map(
            fakeFirebaseException("ERROR_NETWORK_REQUEST_FAILED")
        )
        assertTrue(result.contains("Network error", ignoreCase = true))
    }

    @Test
    fun `should_return_friendly_message_when_too_many_requests`() {
        val result = FirebaseAuthErrorMapper.map(
            fakeFirebaseException("ERROR_TOO_MANY_REQUESTS")
        )
        assertTrue(result.contains("Too many", ignoreCase = true))
    }

    // ── Fallbacks ─────────────────────────────────────────────────────────────

    @Test
    fun `should_return_generic_message_for_unknown_firebase_error_code`() {
        val ex = fakeFirebaseException("SOME_UNKNOWN_CODE", null)
        val result = FirebaseAuthErrorMapper.map(ex)
        // Falls through to the `else` branch which tries ex.message first
        assertEquals("Authentication failed. Please try again.", result)
    }

    @Test
    fun `should_return_exception_message_for_non_firebase_exceptions`() {
        val result = FirebaseAuthErrorMapper.map(Exception("Network timeout"))
        assertEquals("Network timeout", result)
    }

    @Test
    fun `should_return_generic_fallback_for_non_firebase_exception_with_null_message`() {
        val result = FirebaseAuthErrorMapper.map(Exception())
        assertEquals("Something went wrong. Please try again.", result)
    }
}
