package must.kdroiders.hustlehub.ui.features.monetization.domain.usecase

import must.kdroiders.hustlehub.ui.features.monetization.data.remote.dto.StkPushResponseDto
import must.kdroiders.hustlehub.ui.features.monetization.domain.repository.PaymentRepository
import javax.inject.Inject

/**
 * Validates and normalizes a Kenyan M-Pesa phone number, then triggers the STK push.
 *
 * Accepted input formats:
 *  - 07XXXXXXXX   → 2547XXXXXXXX
 *  - 01XXXXXXXX   → 2541XXXXXXXX
 *  - +254XXXXXXXX → 254XXXXXXXX
 *  - 254XXXXXXXX  → unchanged (already valid)
 */
class InitiateStkPushUseCase
    @Inject
    constructor(
        private val paymentRepository: PaymentRepository,
    ) {
    companion object {
        private const val EXPECTED_NORMALIZED_LENGTH = 12
    }

    suspend operator fun invoke(
        rawPhone: String,
        planType: String,
        serviceId: String? = null,
    ): Result<StkPushResponseDto> {
        val normalized = normalizePhone(rawPhone.trim())
            ?: return Result.failure(IllegalArgumentException("Invalid phone number. Use format 07XXXXXXXX or 01XXXXXXXX."))

        return paymentRepository.initiateStkPush(
            phoneNumber = normalized,
            planType = planType,
            serviceId = serviceId,
        )
    }

    /**
     * Converts common Kenyan phone input formats to the 254XXXXXXXXX format required
     * by the Safaricom Daraja API (and validated by the Spring Boot backend).
     *
     * Returns null if the number cannot be normalized to a valid 12-digit Kenyan number.
     */
    internal fun normalizePhone(phone: String): String? {
        val normalized = when {
            // Already in international format without +
            phone.startsWith("254") && phone.length == EXPECTED_NORMALIZED_LENGTH -> phone
            // International format with +
            phone.startsWith("+254") -> phone.removePrefix("+")
            // Local Safaricom format: 07XX...
            phone.startsWith("07") && phone.length == 10 -> "254" + phone.substring(1)
            // Local Airtel format: 01XX...
            phone.startsWith("01") && phone.length == 10 -> "254" + phone.substring(1)
            // Short format without leading zero: 7XXXXXXXX
            phone.startsWith("7") && phone.length == 9 -> "254$phone"
            // Short format without leading zero: 1XXXXXXXX
            phone.startsWith("1") && phone.length == 9 -> "254$phone"
            else -> null
        }
        // Final validation: must be exactly 12 digits, all numeric
        return normalized?.takeIf { it.length == EXPECTED_NORMALIZED_LENGTH && it.all { c -> c.isDigit() } }
    }
}
