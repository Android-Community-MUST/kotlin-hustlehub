package must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel

import must.kdroiders.hustlehub.ui.features.profile.domain.model.User
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service

data class WriteReviewUiState(
    val service: Service? = null,
    val provider: User? = null,
    val isLoadingInfo: Boolean = true,

    val rating: Int = 0,
    val comment: String = "",
    val selectedTags: Set<String> = emptySet(),

    val isAnonymous: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val hasAlreadyReviewed: Boolean = false,
    val error: String? = null,
) {
    val canSubmit: Boolean get() = rating in 1..5 && !isSubmitting && !isLoadingInfo && !hasAlreadyReviewed
    val commentLength: Int get() = comment.length
    val maxCommentLength: Int get() = 200
}
