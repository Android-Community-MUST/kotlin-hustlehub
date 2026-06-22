package must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel

data class WriteReviewUiState(
    val rating: Int = 0,
    val comment: String = "",
    val isAnonymous: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val error: String? = null,
) {
    val canSubmit: Boolean get() = rating in 1..5 && !isSubmitting
    val commentLength: Int get() = comment.length
    val maxCommentLength: Int get() = 200
}
