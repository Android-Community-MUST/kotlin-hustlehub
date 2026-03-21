package must.kdroiders.hustlehub.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import must.kdroiders.hustlehub.BuildConfig
import must.kdroiders.hustlehub.onboarding.OnboardingScreen
import must.kdroiders.hustlehub.splash.SplashDestination
import must.kdroiders.hustlehub.splash.SplashScreen
import must.kdroiders.hustlehub.ui.auth.presentation.view.SignUpScreen
import must.kdroiders.hustlehub.ui.features.profilesetup.presentation.view.ProfileSetupScreen
import must.kdroiders.hustlehub.ui.portfolio.PortfolioUploadScreen

/**
 * Top-level route strings for the outer (splash/auth) navigation graph.
 *
 * Bottom navigation tab routes live in [BottomNavDestination].
 */
object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val ONBOARDING = "onboarding"
    const val PROFILE_SETUP = "profile_setup"
    const val PORTFOLIO_UPLOAD = "portfolio_upload"
}

/**
 * Root navigation graph.
 *
 * Handles splash, auth, and onboarding flow. Once a user is authenticated and
 * their profile is set up, navigation lands on [Routes.HOME] which hosts the
 * [MainScaffold] (bottom-navigation shell).
 */
@Composable
fun HustleHubNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier,
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigate = { destination ->
                    val route = when (destination) {
                        SplashDestination.Home -> Routes.HOME
                        SplashDestination.Login -> Routes.LOGIN
                        SplashDestination.Onboarding -> Routes.ONBOARDING
                        SplashDestination.ProfileSetup -> Routes.PROFILE_SETUP
                    }
                    navController.navigate(route) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }

        // ── Main shell (bottom navigation) ──────────────────────────────────
        composable(Routes.HOME) {
            MainScaffold(rootNavController = navController)
        }

        // ── Auth screens ─────────────────────────────────────────────────────
        composable(Routes.LOGIN) {
            PlaceholderScreen(
                title = "Login (Teammate Task)",
                showDeveloperShortcuts = BuildConfig.DEBUG,
                onDeveloperShortcut = { navController.navigate(it) },
            )
        }

        composable(Routes.SIGN_UP) {
            SignUpScreen(
                onNavigateToLogin = { navController.navigate(Routes.LOGIN) },
            )
        }

        // ── Onboarding ────────────────────────────────────────────────────────
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Routes.SPLASH) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.PROFILE_SETUP) {
            ProfileSetupScreen(
                onSetupComplete = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.PROFILE_SETUP) { inclusive = true }
                    }
                },
            )
        }

        // ── Standalone screens reachable from within the shell ────────────────
        composable(Routes.PORTFOLIO_UPLOAD) {
            PortfolioUploadScreen()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dev utility – used for auth placeholder until teammate completes Login screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PlaceholderScreen(
    title: String,
    showDeveloperShortcuts: Boolean = false,
    onDeveloperShortcut: (String) -> Unit = {},
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
                    androidx.compose.material3.Button(
                        onClick = { onDeveloperShortcut(Routes.HOME) },
                    ) { Text("Home") }
                    androidx.compose.material3.Button(
                        onClick = { onDeveloperShortcut(Routes.SIGN_UP) },
                    ) { Text("Sign Up") }
                    androidx.compose.material3.Button(
                        onClick = { onDeveloperShortcut(Routes.PORTFOLIO_UPLOAD) },
                    ) { Text("Upload") }
                }
            }
        }
    }
}
