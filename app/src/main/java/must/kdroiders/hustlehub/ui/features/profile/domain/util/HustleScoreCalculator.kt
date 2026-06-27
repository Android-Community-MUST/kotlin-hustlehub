package must.kdroiders.hustlehub.ui.features.profile.domain.util

import must.kdroiders.hustlehub.ui.features.service.domain.model.Service

/**
 * Calculates the Hustle Score using a Bayesian Average model.
 * This ensures that a provider must "earn" their score by consistently providing
 * great service over multiple reviews, rather than getting a perfect score after 1 review.
 */
object HustleScoreCalculator {
    private const val MIN_REVIEWS_TO_ESTABLISH = 5f
    private const val PLATFORM_AVERAGE_RATING = 3.5f

    fun calculate(services: List<Service>): Float {
        val totalReviews = services.sumOf { it.reviewCount }.toFloat()
        if (totalReviews == 0f) return 0f

        val avgRating = services.map { it.averageRating }.average().toFloat()

        // Bayesian Average Formula
        val bayesianRating = ((totalReviews / (totalReviews + MIN_REVIEWS_TO_ESTABLISH)) * avgRating) +
            ((MIN_REVIEWS_TO_ESTABLISH / (totalReviews + MIN_REVIEWS_TO_ESTABLISH)) * PLATFORM_AVERAGE_RATING)

        // Convert the 1.0 - 5.0 Bayesian rating to a percentage out of 100
        val finalPercentage = (bayesianRating / 5f) * 100f

        // Round to 1 decimal place (e.g. 83.3)
        return kotlin.math.round(finalPercentage * 10f) / 10f
    }

    /**
     * Calculates the Hustle Score for a single service independently.
     * Used for ranking individual services (e.g., Top Hustlers row).
     */
    fun calculateForService(service: Service): Float {
        val totalReviews = service.reviewCount.toFloat()
        if (totalReviews == 0f) return 0f

        val avgRating = service.averageRating.toFloat()

        val bayesianRating = ((totalReviews / (totalReviews + MIN_REVIEWS_TO_ESTABLISH)) * avgRating) +
            ((MIN_REVIEWS_TO_ESTABLISH / (totalReviews + MIN_REVIEWS_TO_ESTABLISH)) * PLATFORM_AVERAGE_RATING)

        val finalPercentage = (bayesianRating / 5f) * 100f
        return kotlin.math.round(finalPercentage * 10f) / 10f
    }
}
