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

/**
 * Individual chat conversation.
 * Carries the [chatId] so the detail pane can load the correct thread.
 */
@Serializable
data class ChatDetail(val chatId: String) : NavKey

/** App settings screen — pushed from the Profile tab header. */
@Serializable
data object Settings : NavKey

/**
 * Create or edit a service listing.
 * When [serviceId] is provided the screen loads existing data for editing.
 */
@Serializable
data class CreateService(val serviceId: String? = null) : NavKey

/** Full-screen management list of the current user's own services. */
@Serializable
data object MyServices : NavKey
