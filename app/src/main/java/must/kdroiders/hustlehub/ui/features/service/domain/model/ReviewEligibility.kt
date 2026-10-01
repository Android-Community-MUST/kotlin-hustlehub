package must.kdroiders.hustlehub.ui.features.service.domain.model

data class ReviewEligibility(
    val canReview: Boolean,
    val reason: String,
    val isVerified: Boolean = false,
) {
    val isNoInteraction: Boolean get() = reason.equals("NO_INTERACTION", ignoreCase = true)
    val isAlreadyReviewed: Boolean get() = reason.equals("ALREADY_REVIEWED", ignoreCase = true)
    val isOwnService: Boolean get() = reason.equals("OWN_SERVICE", ignoreCase = true)
}
