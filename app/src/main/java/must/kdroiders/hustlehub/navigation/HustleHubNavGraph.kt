package must.kdroiders.hustlehub.navigation

import androidx.activity.ComponentActivity
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import must.kdroiders.hustlehub.core.auth.AuthStateViewModel
import must.kdroiders.hustlehub.onboarding.OnboardingScreen
import must.kdroiders.hustlehub.splash.SplashDestination
import must.kdroiders.hustlehub.splash.SplashScreen
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthState
import must.kdroiders.hustlehub.ui.features.auth.presentation.view.EmailVerificationScreen
import must.kdroiders.hustlehub.ui.features.auth.presentation.view.LoginScreen
import must.kdroiders.hustlehub.ui.features.auth.presentation.view.SignUpScreen

import must.kdroiders.hustlehub.ui.features.auth.presentation.viewmodel.LoginViewModel
import must.kdroiders.hustlehub.ui.features.portfolio.PortfolioUploadScreen
import must.kdroiders.hustlehub.ui.features.profilesetup.presentation.view.ProfileSetupScreen
import must.kdroiders.hustlehub.ui.features.service.presentation.view.CreateServiceScreen
import must.kdroiders.hustlehub.ui.features.service.presentation.view.MyServicesScreen
import must.kdroiders.hustlehub.ui.features.settings.presentation.view.SettingsScreen

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
fun HustleHubNav(
    onGoogleSignInClick: () -> Unit
) {
    val backstack = rememberNavBackStack(Splash)
    val motionScheme = MaterialTheme.motionScheme
    val slideSpec = motionScheme.defaultSpatialSpec<IntOffset>()
    val fadeSpec = motionScheme.defaultEffectsSpec<Float>()

    // Observe global auth state — auto-navigate to Login if Firebase signs the user out
    // (token expiry, forced signout, account deletion, etc.)
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val authStateViewModel: AuthStateViewModel? = if (activity != null) {
        hiltViewModel<AuthStateViewModel>(viewModelStoreOwner = activity)
    } else null

    authStateViewModel?.let { vm ->
        val authState by vm.authState.collectAsState()
        LaunchedEffect(authState) {
            // Only react after Splash has completed (don't interrupt the splash auth check)
            val currentTop = backstack.lastOrNull()
            val isInAuthFlow = currentTop is Splash || currentTop is Login ||
                currentTop is SignUp || currentTop is EmailVerification ||
                currentTop is Onboarding

            if (authState == AuthState.Unauthenticated && !isInAuthFlow) {
                backstack.clear()
                backstack.add(Login())
            }
        }
    }


    NavDisplay(
        backStack = backstack,
        onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
        transitionSpec = {
            (slideInHorizontally(slideSpec) { it } + fadeIn(fadeSpec)) togetherWith
                (slideOutHorizontally(slideSpec) { -it } + fadeOut(fadeSpec))
        },
        popTransitionSpec = {
            (slideInHorizontally(slideSpec) { -it } + fadeIn(fadeSpec)) togetherWith
                (slideOutHorizontally(slideSpec) { it } + fadeOut(fadeSpec))
        },
        entryProvider = entryProvider {

            // Splash
            entry<Splash> {
                SplashScreen(
                    onNavigate = { destination ->
                        val key: NavKey = when (destination) {
                            SplashDestination.Home -> MainShell
                            SplashDestination.Login -> Login()
                            SplashDestination.Onboarding -> Onboarding
                            SplashDestination.ProfileSetup -> ProfileSetup
                        }
                        backstack.clear()
                        backstack.add(key)
                    },
                )
            }

            // Auth
            entry<Login> { key ->
                val context = LocalContext.current
                val activity = context as? ComponentActivity
                val loginViewModel: LoginViewModel = if (activity != null) {
                    hiltViewModel(viewModelStoreOwner = activity)
                } else {
                    hiltViewModel()
                }

                // Observe Google sign-in navigation events from the shared ViewModel
                LaunchedEffect(loginViewModel) {
                    loginViewModel.navigateToHome.collect { hasProfile ->
                        backstack.clear()
                        backstack.add(if (hasProfile) MainShell else ProfileSetup)
                    }
                }

                LoginScreen(
                    prefilledEmail = key.email,
                    onLoginSuccess = { hasProfile ->
                        backstack.clear()
                        backstack.add(if (hasProfile) MainShell else ProfileSetup)
                    },
                    onNavigateToSignUp = {
                        backstack.add(SignUp)
                    },
                    onNavigateToEmailVerification = { email ->
                        backstack.add(EmailVerification(email = email))
                    },
                    onGoogleSignInClick = onGoogleSignInClick,
                    loginViewModel = loginViewModel
                )
            }

            entry<EmailVerification> { key ->
                EmailVerificationScreen(
                    email = key.email,
                    onVerified = {
                        backstack.clear()
                        backstack.add(Login(email = key.email))
                    }
                )
            }

            entry<SignUp> {
                SignUpScreen(
                    onNavigateToLogin = {
                        if (backstack.isNotEmpty()) backstack.remove(backstack.last())
                        if (backstack.isEmpty()) backstack.add(Login())
                    },
                    onSignUpSuccess = { email ->
                        backstack.add(EmailVerification(email = email))
                    }
                )
            }

            // Onboarding
            entry<Onboarding> {
                OnboardingScreen(
                    onFinished = {
                        backstack.clear()
                        backstack.add(Login())
                    },
                )
            }

            // Profile setup
            entry<ProfileSetup> {
                ProfileSetupScreen(
                    onSetupComplete = {
                        backstack.clear()
                        backstack.add(MainShell)
                    },
                )
            }

            // Main shell
            entry<MainShell> {
                MainShellScreen(
                    onNavigateToPortfolio = { serviceId -> backstack.add(PortfolioUpload(serviceId)) },
                    onNavigateToProfileSetup = { backstack.add(ProfileSetup) },
                    onNavigateToSettings = { backstack.add(Settings) },
                    onNavigateToCreateService = { backstack.add(CreateService()) },
                    onNavigateToMyServices = { backstack.add(MyServices) }
                )
            }

            // Standalone screens
            entry<PortfolioUpload> { key ->
                PortfolioUploadScreen(
                    serviceId = key.serviceId,
                    onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) }
                )
            }

            entry<Settings> {
                SettingsScreen(
                    onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) }
                )
            }

            // Create / Edit service
            entry<CreateService> { key ->
                CreateServiceScreen(
                    serviceId = key.serviceId,
                    onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                    onSuccess = { if (backstack.size > 1) backstack.remove(backstack.last()) }
                )
            }

            // My services management
            entry<MyServices> {
                MyServicesScreen(
                    onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                    onCreateService = { backstack.add(CreateService()) },
                    onEditService = { serviceId -> backstack.add(CreateService(serviceId = serviceId)) }
                )
            }

            entry<ChatDetail> { key ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Chat – ${key.chatId}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        },
    )
}
