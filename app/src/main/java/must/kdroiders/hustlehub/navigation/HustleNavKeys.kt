package must.kdroiders.hustlehub.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Root-flow keys  (splash → auth → onboarding → main shell)

/** Initial full-screen splash / auth-gate. */
@Serializable
data object Splash : NavKey

/** First-run onboarding carousel. */
@Serializable
data object Onboarding : NavKey

/** Login screen. */
@Serializable
data class Login(val email: String = "") : NavKey

/** Sign-up / registration screen. */
@Serializable
data object SignUp : NavKey

/** Account suspended error screen. */
@Serializable
data class AccountSuspendedKey(val reason: String = "") : NavKey

/**
 * Email OTP verification screen.
 * Carries the [email] so the screen can display it
 * and the ViewModel can pass it to the repository.
 */
@Serializable
data class EmailVerification(val email: String) : NavKey

/** Profile-setup wizard shown after first successful login. */
@Serializable
data object ProfileSetup : NavKey

/**
 * Main shell that contains the bottom navigation bar.
 * Pushed once after auth; bottom-tab navigation lives inside this shell.
 */
@Serializable
data object MainShell : NavKey

// Bottom-tab keys  (used by the inner back-stack inside MainShell)

/** Discovery feed tab. */
@Serializable
data object BottomHome : NavKey

/** Campus map tab. */
@Serializable
data object BottomMap : NavKey

/**
 * Chat / messaging tab (list pane in adaptive layout).
 * On phones: shows the conversation list.
 * On tablets: shown side-by-side with [ChatDetail].
 */
@Serializable
data object BottomChat : NavKey

/** My profile tab. */
@Serializable
data object BottomProfile : NavKey

// Detail / full-screen keys  (pushed over the shell)

/** Change Password screen */
@Serializable
data object ChangePassword : NavKey

/**
 * Individual chat conversation.
 *
 * [chatId] is the conversation ID used to load the thread.
 *
 * The optional service fields are non-null only when the chat is opened directly
 * from a service listing via "Message Provider". They are used to auto-generate
 * a SERVICE_CARD message at the top of the conversation on first open.
 */
@Serializable
data class ChatDetail(
    val chatId: String,
    val serviceId: String? = null,
    val serviceTitle: String? = null,
    val serviceCategory: String? = null,
    val servicePriceRange: String? = null,
    val providerName: String? = null,
) : NavKey

/** App settings screen — pushed from the Profile tab header. */
@Serializable
data object Settings : NavKey

/** Privacy settings screen — pushed from SettingsScreen. */
@Serializable
data object PrivacySettings : NavKey

/** Blocked users screen — pushed from SettingsScreen. */
@Serializable
data object BlockedUsers : NavKey

/**
 * Create or edit a service listing.
 * When [serviceId] is provided the screen loads existing data for editing.
 */
@Serializable
data class CreateService(val serviceId: String? = null) : NavKey

/** Full-screen management list of the current user's own services. */
@Serializable
data object MyServices : NavKey

/** Full-screen search with filters, sort, and recent suggestions. */
@Serializable
data object SearchScreen : NavKey

/** AI-powered natural language search screen. */
@Serializable
data object AiSearchScreen : NavKey

/**
 * Service detail screen — shows a provider's full profile, portfolio, and reviews.
 * Only [serviceId] is passed; the destination ViewModel fetches the full data.
 */
@Serializable
data class ServiceDetail(val serviceId: String) : NavKey

/**
 * Public provider profile screen.
 * Shows all services by [providerId] and their reputation stats.
 * Displays an edit button instead of a message button when the viewer is the provider.
 */
@Serializable
data class ProviderProfile(val providerId: String) : NavKey

/** Edit own profile screen. Pre-fills current user data. */
@Serializable
data object EditProfile : NavKey

/**
 * Write a review for a service.
 * Carries both [serviceId] and [providerId] so the ViewModel can submit and
 * invalidate the correct service detail cache.
 */
@Serializable
data class WriteReview(val serviceId: String, val providerId: String) : NavKey

/** In-app notification center screen. */
@Serializable
data object Notifications : NavKey

/** All reviews screen for a specific service. */
@Serializable
data class AllReviews(val serviceId: String) : NavKey

/** Notification preferences screen — pushed from SettingsScreen. */
@Serializable
data object NotificationPreferences : NavKey

/** Subscription & Pro upgrade screen — navigable from Profile, Settings, and service creation. */
@Serializable
data object Subscription : NavKey

/**
 * Payment status polling screen — pushed after a successful STK push trigger.
 * Carries the [checkoutRequestId] to poll M-Pesa payment confirmation.
 */
@Serializable
data class PaymentStatus(val checkoutRequestId: String) : NavKey

/** Pro Analytics dashboard screen — pushed from Profile screen. */
@Serializable
data class Analytics(val initialTab: String = "OVERVIEW") : NavKey
