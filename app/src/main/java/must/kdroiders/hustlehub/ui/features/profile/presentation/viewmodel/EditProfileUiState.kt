package must.kdroiders.hustlehub.ui.features.profile.presentation.viewmodel

import must.kdroiders.hustlehub.ui.features.profile.domain.model.User

data class EditProfileUiState(
    val user: User? = null,
    /** Editable field values — tracked separately so we don't mutate the original User. */
    val name: String = "",
    val bio: String = "",
    val phone: String = "",
    val campusLocation: String = "",
    val allowCalls: Boolean = false,
    /** Set to a new URI string when the user picks a photo; null means unchanged. */
    val pendingAvatarUri: String? = null,
    val isSaving: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val saveSuccess: Boolean = false,
)
