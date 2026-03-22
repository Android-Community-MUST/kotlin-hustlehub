package must.kdroiders.hustlehub.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import must.kdroiders.hustlehub.BuildConfig
import must.kdroiders.hustlehub.onboarding.OnboardingScreen
import must.kdroiders.hustlehub.splash.SplashDestination
import must.kdroiders.hustlehub.splash.SplashScreen
import must.kdroiders.hustlehub.ui.auth.presentation.view.SignUpScreen
import must.kdroiders.hustlehub.ui.features.profilesetup.presentation.view.ProfileSetupScreen
import must.kdroiders.hustlehub.ui.portfolio.PortfolioUploadScreen

/**
 * Root Navigation 3 navigator for HustleHub.
 *
 * Uses a single [NavDisplay] that owns the entire root back-stack. Each destination
 * is a serializable [NavKey] from [HustleNavKeys], ensuring type-safety and state
 * restoration across configuration changes and process death.
 *
 * Architecture:
 * ```
 * HustleHubNav  (root NavDisplay – splash/auth/shell)
 *   └── MainShellScreen  (inner NavDisplay – bottom-tab destinations)
 *         ├── HomeScreen
 *         ├── MapScreen
 *         ├── ChatScreen
 *         └── ProfileScreen
 * ```
 *
 * Transitions: horizontal slide + crossfade (applied globally via [transitionSpec]).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HustleHubNav() {
    val backstack = rememberNavBackStack(Splash)
    val motionScheme = MaterialTheme.motionScheme
    // IntOffset spec for slide animations, Float spec for fade animations
    val slideSpec = motionScheme.defaultSpatialSpec<IntOffset>()
    val fadeSpec = motionScheme.defaultEffectsSpec<Float>()

    NavDisplay(
        backStack = backstack,
        onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
        // Global transitions using MotionScheme
        transitionSpec = {
            (slideInHorizontally(slideSpec) { it } + fadeIn(fadeSpec)) togetherWith
                (slideOutHorizontally(slideSpec) { -it } + fadeOut(fadeSpec))
        },
        popTransitionSpec = {
            (slideInHorizontally(slideSpec) { -it } + fadeIn(fadeSpec)) togetherWith
                (slideOutHorizontally(slideSpec) { it } + fadeOut(fadeSpec))
        },
        // Screen routing via entryProvider DSL
        entryProvider = entryProvider {

            // Splash
            entry<Splash> {
                SplashScreen(
                    onNavigate = { destination ->
                        val key: NavKey = when (destination) {
                            SplashDestination.Home -> MainShell
                            SplashDestination.Login -> Login
                            SplashDestination.Onboarding -> Onboarding
                            SplashDestination.ProfileSetup -> ProfileSetup
                        }
                        backstack.clear()
                        backstack.add(key)
                    },
                )
            }

            // Auth
            entry<Login> {
                NavPlaceholderScreen(
                    title = "Login (Teammate Task)",
                    showDeveloperShortcuts = BuildConfig.DEBUG,
                    onDeveloperShortcut = { key -> backstack.add(key) },
                )
            }

            entry<SignUp> {
                SignUpScreen(
                    onNavigateToLogin = {
                        if (backstack.isNotEmpty()) { backstack.remove(backstack.last()) }
                        if (backstack.isEmpty()) backstack.add(Login)
                    },
                )
            }

            //Onboarding
            entry<Onboarding> {
                OnboardingScreen(
                    onFinished = {
                        backstack.clear()
                        backstack.add(Login)
                    },
                )
            }

            // Profile setup (post first-login wizard)
            entry<ProfileSetup> {
                ProfileSetupScreen(
                    onSetupComplete = {
                        backstack.clear()
                        backstack.add(MainShell)
                    },
                )
            }

            // Main shell (bottom-nav host)
            entry<MainShell> {
                MainShellScreen(
                    onNavigateToPortfolio = { backstack.add(PortfolioUpload) },
                )
            }

            // Standalone screens reachable from within the shell
            entry<PortfolioUpload> {
                PortfolioUploadScreen()
            }

            // Chat detail (also used as adaptive detail pane on tablets)
            entry<ChatDetail> { key ->
                // TODO: replace with real ChatDetailScreen(key.chatId) in a later sprint
                NavPlaceholderScreen(title = "Chat – ${key.chatId}")
            }
        },
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Dev utility – placeholder until teammates complete their screens
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NavPlaceholderScreen(
    title: String,
    showDeveloperShortcuts: Boolean = false,
    onDeveloperShortcut: (NavKey) -> Unit = {},
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            if (showDeveloperShortcuts) {
                Spacer(Modifier.height(32.dp))
                Text(
                    text = "Developer Shortcuts",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onDeveloperShortcut(MainShell) }) { Text("Home") }
                    Button(onClick = { onDeveloperShortcut(SignUp) })    { Text("Sign Up") }
                    Button(onClick = { onDeveloperShortcut(PortfolioUpload) }) { Text("Upload") }
                }
            }
        }
    }
}
