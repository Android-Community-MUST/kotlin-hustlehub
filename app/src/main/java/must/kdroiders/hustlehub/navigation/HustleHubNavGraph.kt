package must.kdroiders.hustlehub.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import must.kdroiders.hustlehub.onboarding.OnboardingScreen
import must.kdroiders.hustlehub.splash.SplashDestination
import must.kdroiders.hustlehub.splash.SplashScreen
import must.kdroiders.hustlehub.ui.auth.LoginScreen
import must.kdroiders.hustlehub.ui.auth.SignUpScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import must.kdroiders.hustlehub.ui.auth.EmailVerificationScreen


/**
 * Navigation route definitions.
 */
object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val LOGIN = "login"

    const val SIGN_UP = "sign_up"

    const val EMAIL_VERIFICATION = "email_verification/{email}"
    const val ONBOARDING = "onboarding"
}

@Composable
fun HustleHubNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigate = { destination ->
                    val route = when (destination) {
                        SplashDestination.Home -> Routes.HOME
                        SplashDestination.Login -> Routes.LOGIN
                        SplashDestination.Onboarding -> Routes.ONBOARDING
                    }
                    navController.navigate(route) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            PlaceholderScreen(title = "Home")
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToSignUp = {
                    navController.navigate(Routes.SIGN_UP)
                },
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SIGN_UP) {
            SignUpScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SIGN_UP) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.EMAIL_VERIFICATION,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            EmailVerificationScreen(
                email = email,
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SIGN_UP) { inclusive = true }
                    }
                },
                onResendEmail = { /* handled inside ViewModel */ }
            )
        }


        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinished = {
                    // navigate to splash to re-evaluate
                    // auth state (Home vs Login)
                    navController.navigate(Routes.SPLASH) {
                        popUpTo(Routes.ONBOARDING) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}

/**
 * Temporary placeholder screen — will be replaced
 * when actual feature screens are implemented.
 */
@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
