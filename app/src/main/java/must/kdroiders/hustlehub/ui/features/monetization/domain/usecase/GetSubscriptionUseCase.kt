package must.kdroiders.hustlehub.ui.features.monetization.domain.usecase

import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.ui.features.monetization.data.remote.dto.SubscriptionResponseDto
import must.kdroiders.hustlehub.ui.features.monetization.domain.repository.PaymentRepository
import javax.inject.Inject

/**
 * Fetches the current user's active subscription from the backend and caches
 * the Pro status in [UserPreferences] DataStore for fast offline rendering.
 *
 * Returns null data when the user has no active subscription (backend returns null).
 */
class GetSubscriptionUseCase
    @Inject
    constructor(
        private val paymentRepository: PaymentRepository,
        private val userPreferences: UserPreferences,
    ) {
    suspend operator fun invoke(): Result<SubscriptionResponseDto?> {
        val result = paymentRepository.getMySubscription()
        result.onSuccess { subscription ->
            // Cache Pro status so badge renders immediately on next launch
            userPreferences.saveProStatus(
                isActive = subscription?.isActive == true,
                expiresAt = subscription?.endDate,
            )
        }
        return result
    }
}
