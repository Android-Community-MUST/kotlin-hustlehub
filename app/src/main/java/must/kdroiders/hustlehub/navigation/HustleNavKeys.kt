package must.kdroiders.hustlehub.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────────────────────────────────────
// Root-flow keys  (splash → auth → onboarding → main shell)
// ─────────────────────────────────────────────────────────────────────────────

/** Initial full-screen splash / auth-gate. */
@Serializable
data object Splash : NavKey

/** First-run onboarding carousel. */
@Serializable
data object Onboarding : NavKey

/** Login placeholder (teammate's screen). */
@Serializable
data object Login : NavKey

/** Sign-up / registration screen. */
@Serializable
data object SignUp : NavKey

/** Profile-setup wizard shown after first successful login. */
@Serializable
data object ProfileSetup : NavKey

/**
 * Main shell that contains the bottom navigation bar.
 * Pushed once after auth; bottom-tab navigation lives inside this shell.
 */
@Serializable
data object MainShell : NavKey

// ─────────────────────────────────────────────────────────────────────────────
// Bottom-tab keys  (used by the inner back-stack inside MainShell)
// ─────────────────────────────────────────────────────────────────────────────

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

// ─────────────────────────────────────────────────────────────────────────────
// Detail / full-screen keys  (pushed over the shell)
// ─────────────────────────────────────────────────────────────────────────────

/** Portfolio / gig upload screen reached from Profile tab. */
@Serializable
data object PortfolioUpload : NavKey

/**
 * Individual chat conversation.
 * Carries the [chatId] so the detail pane can load the correct thread.
 */
@Serializable
data class ChatDetail(val chatId: String) : NavKey
