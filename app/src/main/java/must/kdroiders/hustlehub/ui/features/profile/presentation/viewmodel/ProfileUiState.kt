package must.kdroiders.hustlehub.ui.features.profile

import must.kdroiders.hustlehub.data.model.Service
import must.kdroiders.hustlehub.data.model.User

/**
 * Immutable UI state for the Profile screen.
 * The ViewModel is the single source of truth.
 */
data class ProfileUiState(
    val user: User? = null,
    val services: List<Service> = emptyList(),
    val hustleScore: Float = 0f,
    val reviewCount: Int = 0,
    val badges: List<Badge> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * Represents a profile badge / achievement.
 */
data class Badge(
    val label: String,
    val type: BadgeType = BadgeType.DEFAULT
)

enum class BadgeType {
    GOLD,
    GREEN,
    BLUE,
    DEFAULT
}
