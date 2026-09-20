package must.kdroiders.hustlehub.core.network

import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.random.Random

object RetryPolicy {
    suspend fun <T> withExponentialBackoff(
        maxAttempts: Int = 3,
        initialDelayMs: Long = 300L,
        maxDelayMs: Long = 8_000L,
        jitterFactor: Double = 0.2,
        shouldRetry: (Throwable) -> Boolean = { true },
        block: suspend () -> T,
    ): T {
        var delayMs = initialDelayMs
        var lastException: Throwable? = null
        repeat(maxAttempts) { attempt ->
            try {
                return block()
            } catch (e: Throwable) {
                lastException = e
                if (attempt == maxAttempts - 1 || !shouldRetry(e)) throw e
                val jitter = (delayMs * jitterFactor * Random.nextDouble()).toLong()
                delay(min(delayMs + jitter, maxDelayMs))
                delayMs = min(delayMs * 2, maxDelayMs)
            }
        }
        throw lastException!!
    }
}
