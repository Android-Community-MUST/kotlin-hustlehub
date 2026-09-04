package must.kdroiders.hustlehub.core.api

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * Sanitizes exceptions and error messages before presenting them to the user interface.
 *
 * Prevents sensitive diagnostic data (such as backend Cloud Run hostnames, certificate
 * pinning fingerprints, IP addresses, or low-level socket stack traces) from being
 * exposed to end users.
 */
object ErrorSanitizer {

    /**
     * Converts a [Throwable] into a safe, human-friendly message.
     */
    fun sanitize(throwable: Throwable, fallback: String = "Something went wrong. Please try again."): String {
        return when (throwable) {
            is SSLException -> "Secure connection failed. Please check your network or try again."
            is UnknownHostException,
            is ConnectException,
            is SocketTimeoutException,
            is IOException -> "Network connection error. Please check your internet connection and try again."
            else -> {
                val msg = throwable.message?.trim()
                if (msg.isNullOrBlank() || isSensitiveOrTechnical(msg)) {
                    fallback
                } else {
                    msg
                }
            }
        }
    }

    /**
     * Checks if a message string contains internal hostnames, URLs, certificate hashes,
     * or technical diagnostic tokens.
     */
    fun isSensitiveOrTechnical(msg: String): Boolean {
        val lower = msg.lowercase()
        return lower.contains("run.app") ||
            lower.contains("http://") ||
            lower.contains("https://") ||
            lower.contains("pinning") ||
            lower.contains("certificate") ||
            lower.contains("exception") ||
            lower.contains("failed to connect") ||
            lower.contains("unable to resolve host") ||
            lower.contains("chain:") ||
            lower.contains("sha256/") ||
            lower.contains("broken pipe") ||
            lower.contains("connection reset")
    }
}

/**
 * Convenience extension to safely extract a user-facing error message from a [Throwable].
 */
fun Throwable.userFriendlyMessage(fallback: String = "Something went wrong. Please try again."): String {
    return ErrorSanitizer.sanitize(this, fallback)
}
