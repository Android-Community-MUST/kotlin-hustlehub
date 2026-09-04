package must.kdroiders.hustlehub.core.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

class ErrorSanitizerTest {

    @Test
    fun `should_sanitize_ssl_exception`() {
        val ex = SSLException("SSL handshake failed with cert chain")
        val result = ErrorSanitizer.sanitize(ex)
        assertEquals("Secure connection failed. Please check your network or try again.", result)
    }

    @Test
    fun `should_sanitize_network_io_exceptions`() {
        val exceptions = listOf(
            UnknownHostException("Unable to resolve host hustlehub-backend-xyz.a.run.app: No address associated with hostname"),
            ConnectException("failed to connect to hustlehub-backend-xyz.a.run.app/127.0.0.1:443"),
            SocketTimeoutException("timeout"),
            IOException("broken pipe"),
        )

        for (ex in exceptions) {
            val result = ErrorSanitizer.sanitize(ex)
            assertEquals(
                "Network connection error. Please check your internet connection and try again.",
                result,
            )
        }
    }

    @Test
    fun `should_redact_sensitive_cloud_run_hostnames_and_certificate_pins`() {
        val sensitiveMessages = listOf(
            "Certificate pinning failure! Peer certificate chain: sha256/8jVhONRfoLxp9xEO7Gc/HdRfHqtEqkqd44YdfeZq5Wo=",
            "Failed to reach https://hustlehub-backend-418465133649.us-central1.run.app/api/profile",
            "Exception occurred while parsing chain: sha256/...",
            "connection reset by peer",
        )

        for (msg in sensitiveMessages) {
            val ex = Exception(msg)
            val result = ErrorSanitizer.sanitize(ex, fallback = "Unable to process request.")
            assertEquals("Unable to process request.", result)
        }
    }

    @Test
    fun `should_preserve_clean_user_facing_messages`() {
        val cleanMsg = "Invalid username or password"
        val ex = Exception(cleanMsg)
        val result = ErrorSanitizer.sanitize(ex)
        assertEquals(cleanMsg, result)
    }

    @Test
    fun `should_use_fallback_when_message_is_blank_or_null`() {
        val nullEx = Exception(null as String?)
        val blankEx = Exception("   ")

        assertEquals("Something went wrong. Please try again.", ErrorSanitizer.sanitize(nullEx))
        assertEquals("Custom fallback", ErrorSanitizer.sanitize(blankEx, fallback = "Custom fallback"))
    }

    @Test
    fun `should_identify_sensitive_tokens`() {
        assertTrue(ErrorSanitizer.isSensitiveOrTechnical("hustlehub-api.a.run.app"))
        assertTrue(ErrorSanitizer.isSensitiveOrTechnical("Certificate pinning failure"))
        assertTrue(ErrorSanitizer.isSensitiveOrTechnical("Failed to connect to host"))
        assertTrue(ErrorSanitizer.isSensitiveOrTechnical("broken pipe"))
        assertFalse(ErrorSanitizer.isSensitiveOrTechnical("Invalid email address"))
    }

    @Test
    fun `userFriendlyMessage_extension_returns_sanitized_message`() {
        val ex = Exception("https://api.run.app/internal error")
        assertEquals("Custom error", ex.userFriendlyMessage("Custom error"))
    }
}
