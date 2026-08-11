package must.kdroiders.hustlehub.navigation

import android.content.Intent
import android.net.Uri
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import must.kdroiders.hustlehub.core.auth.AuthStateViewModel
import must.kdroiders.hustlehub.core.notification.InAppBannerManager
import must.kdroiders.hustlehub.core.notification.InAppNotificationBanner
import must.kdroiders.hustlehub.navigation.AllReviews
import must.kdroiders.hustlehub.onboarding.OnboardingScreen
import must.kdroiders.hustlehub.splash.SplashDestination
import must.kdroiders.hustlehub.splash.SplashScreen
import must.kdroiders.hustlehub.ui.features.analytics.presentation.view.AnalyticsScreen
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthState
import must.kdroiders.hustlehub.ui.features.auth.presentation.view.ChangePasswordScreen
import must.kdroiders.hustlehub.ui.features.auth.presentation.view.EmailVerificationScreen
import must.kdroiders.hustlehub.ui.features.auth.presentation.view.LoginScreen
import must.kdroiders.hustlehub.ui.features.auth.presentation.view.SignUpScreen
import must.kdroiders.hustlehub.ui.features.auth.presentation.viewmodel.LoginViewModel
import must.kdroiders.hustlehub.ui.features.chat.presentation.view.ChatDetailScreen
import must.kdroiders.hustlehub.ui.features.home.presentation.view.AiSearchScreen
import must.kdroiders.hustlehub.ui.features.home.presentation.view.SearchScreen
import must.kdroiders.hustlehub.ui.features.monetization.presentation.PaymentStatusScreen
import must.kdroiders.hustlehub.ui.features.monetization.presentation.SubscriptionScreen
import must.kdroiders.hustlehub.ui.features.notification.presentation.view.NotificationPreferencesScreen
import must.kdroiders.hustlehub.ui.features.notification.presentation.view.NotificationScreen
import must.kdroiders.hustlehub.ui.features.privacy.presentation.view.PrivacySettingsScreen
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.EditProfileScreen
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.ProviderProfileScreen
import must.kdroiders.hustlehub.ui.features.profilesetup.presentation.view.ProfileSetupScreen
import must.kdroiders.hustlehub.ui.features.service.presentation.view.AllReviewsScreen
import must.kdroiders.hustlehub.ui.features.service.presentation.view.CreateServiceScreen
import must.kdroiders.hustlehub.ui.features.service.presentation.view.MyServicesScreen
import must.kdroiders.hustlehub.ui.features.service.presentation.view.ServiceDetailScreen
import must.kdroiders.hustlehub.ui.features.service.presentation.view.WriteReviewScreen
import must.kdroiders.hustlehub.ui.features.settings.presentation.view.BlockedUsersScreen
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
fun HustleHubNav(onGoogleSignInClick: () -> Unit) {
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
    } else {
        null
    }

    authStateViewModel?.let { vm ->
        val authState by vm.authState.collectAsState()
        LaunchedEffect(authState) {
            // Only react after Splash has completed (don't interrupt the splash auth check)
            val currentTop = backstack.lastOrNull()
            val isInAuthFlow = currentTop is Splash ||
                currentTop is Login ||
                currentTop is SignUp ||
                currentTop is EmailVerification ||
                currentTop is Onboarding

            if (authState == AuthState.Unauthenticated && !isInAuthFlow) {
                backstack.clear()
                backstack.add(Login())
            }
        }
    }

    val mainNavigationViewModel: MainNavigationViewModel? = if (activity != null) {
        hiltViewModel<MainNavigationViewModel>(viewModelStoreOwner = activity)
    } else {
        null
    }

    LaunchedEffect(mainNavigationViewModel) {
        mainNavigationViewModel?.deepLinkEvent?.collect { action ->
            if (backstack.none { it is MainShell }) {
                backstack.clear()
                backstack.add(MainShell)
            }
            when (action) {
                is DeepLinkAction.OpenChat -> {
                    val currentTop = backstack.lastOrNull()
                    if (currentTop is ChatDetail && currentTop.chatId == action.conversationId) return@collect
                    backstack.add(ChatDetail(chatId = action.conversationId))
                }
                is DeepLinkAction.OpenServiceDetail -> {
                    val currentTop = backstack.lastOrNull()
                    if (currentTop is ServiceDetail && currentTop.serviceId == action.serviceId) return@collect
                    backstack.add(ServiceDetail(serviceId = action.serviceId))
                }
                is DeepLinkAction.OpenProviderProfile -> {
                    val currentTop = backstack.lastOrNull()
                    if (currentTop is ProviderProfile && currentTop.providerId == action.providerId) return@collect
                    backstack.add(ProviderProfile(providerId = action.providerId))
                }
                is DeepLinkAction.OpenWriteReview -> {
                    backstack.add(WriteReview(serviceId = action.serviceId, providerId = action.providerId))
                }
                is DeepLinkAction.OpenNotifications -> {
                    if (backstack.lastOrNull() !is Notifications) {
                        backstack.add(Notifications)
                    }
                }
                is DeepLinkAction.OpenProfile, is DeepLinkAction.OpenChatList -> {
                    while (backstack.size > 1 && backstack.last() != MainShell) {
                        backstack.remove(backstack.last())
                    }
                }
            }
        }
    }

    val activeBanner by InAppBannerManager.activeBanner.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
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
                        loginViewModel.navigateToHome.collect { _ ->
                            backstack.clear()
                            backstack.add(MainShell)
                        }
                    }

                    LoginScreen(
                        prefilledEmail = key.email,
                        onLoginSuccess = { _ ->
                            backstack.clear()
                            backstack.add(MainShell)
                        },
                        onNavigateToSignUp = {
                            backstack.add(SignUp)
                        },
                        onNavigateToEmailVerification = { email ->
                            backstack.add(EmailVerification(email = email))
                        },
                        onGoogleSignInClick = onGoogleSignInClick,
                        loginViewModel = loginViewModel,
                    )
                }

                entry<EmailVerification> { key ->
                    EmailVerificationScreen(
                        email = key.email,
                        onVerified = {
                            backstack.clear()
                            backstack.add(Login(email = key.email))
                        },
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
                        },
                        onGoogleSignInClick = onGoogleSignInClick,
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
                        onNavigateToProfileSetup = { backstack.add(ProfileSetup) },
                        onNavigateToSettings = { backstack.add(Settings) },
                        onNavigateToCreateService = { backstack.add(CreateService()) },
                        onNavigateToMyServices = { backstack.add(MyServices) },
                        onNavigateToEditService = { serviceId -> backstack.add(CreateService(serviceId = serviceId)) },
                        onNavigateToChatDetail = { chatId -> backstack.add(ChatDetail(chatId = chatId)) },
                        onNavigateToServiceDetail = { serviceId -> backstack.add(ServiceDetail(serviceId = serviceId)) },
                        onNavigateToSearch = { backstack.add(SearchScreen) },
                        onNavigateToAiSearch = { backstack.add(AiSearchScreen) },
                        onNavigateToEditProfile = { backstack.add(EditProfile) },
                        onNavigateToNotifications = { backstack.add(Notifications) },
                        onNavigateToSubscription = { backstack.add(Subscription) },
                        onNavigateToAnalytics = { tab -> backstack.add(Analytics(initialTab = tab)) },
                    )
                }

                entry<Settings> {
                    SettingsScreen(
                        onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                        onNavigateToChangePassword = { backstack.add(ChangePassword) },
                        onNavigateToNotificationPreferences = { backstack.add(NotificationPreferences) },
                        onNavigateToPrivacy = { backstack.add(PrivacySettings) },
                        onNavigateToBlockedUsers = { backstack.add(BlockedUsers) },
                        onNavigateToSubscription = { backstack.add(Subscription) },
                    )
                }

                entry<PrivacySettings> {
                    PrivacySettingsScreen(
                        onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                    )
                }

                entry<BlockedUsers> {
                    BlockedUsersScreen(
                        onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                    )
                }

                entry<ChangePassword> {
                    ChangePasswordScreen(
                        onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                    )
                }

                entry<NotificationPreferences> {
                    NotificationPreferencesScreen(
                        onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                    )
                }

                // Create / Edit service
                entry<CreateService> { key ->
                    CreateServiceScreen(
                        serviceId = key.serviceId,
                        onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                        onSuccess = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                        onNavigateToSubscription = { backstack.add(Subscription) },
                    )
                }

                // My services management
                entry<MyServices> {
                    MyServicesScreen(
                        onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                        onCreateService = { backstack.add(CreateService()) },
                        onEditService = { serviceId -> backstack.add(CreateService(serviceId = serviceId)) },
                    )
                }

                entry<ChatDetail> { key ->
                    ChatDetailScreen(
                        conversationId = key.chatId,
                        serviceId = key.serviceId,
                        serviceTitle = key.serviceTitle,
                        serviceCategory = key.serviceCategory,
                        servicePriceRange = key.servicePriceRange,
                        providerName = key.providerName,
                        onBackClick = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                        onNavigateToServiceDetail = { serviceId -> backstack.add(ServiceDetail(serviceId = serviceId)) },
                        onNavigateToWriteReview = { serviceId, providerId ->
                            backstack.add(WriteReview(serviceId = serviceId, providerId = providerId))
                        },
                    )
                }

                // Service detail — full provider profile, portfolio and reviews.
                entry<ServiceDetail> { key ->
                    ServiceDetailScreen(
                        serviceId = key.serviceId,
                        onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                        onNavigateToChat = { providerId, serviceId, title, category, priceRange, providerName ->
                            backstack.add(
                                ChatDetail(
                                    chatId = providerId,
                                    serviceId = serviceId,
                                    serviceTitle = title,
                                    serviceCategory = category,
                                    servicePriceRange = priceRange,
                                    providerName = providerName,
                                ),
                            )
                        },
                        onNavigateToProviderProfile = { providerId -> backstack.add(ProviderProfile(providerId = providerId)) },
                        onNavigateToWriteReview = { serviceId, providerId ->
                            backstack.add(WriteReview(serviceId = serviceId, providerId = providerId))
                        },
                        onNavigateToAllReviews = { serviceId ->
                            backstack.add(AllReviews(serviceId = serviceId))
                        },
                    )
                }

                // All reviews
                entry<AllReviews> { key ->
                    AllReviewsScreen(
                        serviceId = key.serviceId,
                        onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                    )
                }

                // Provider public profile
                entry<ProviderProfile> { key ->
                    ProviderProfileScreen(
                        providerId = key.providerId,
                        onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                        onNavigateToChat = { providerId -> backstack.add(ChatDetail(chatId = providerId)) },
                        onNavigateToEditProfile = { backstack.add(EditProfile) },
                        onNavigateToServiceDetail = { serviceId -> backstack.add(ServiceDetail(serviceId = serviceId)) },
                    )
                }

                // Edit own profile
                entry<EditProfile> {
                    EditProfileScreen(
                        onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                        onSaveSuccess = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                    )
                }

                // Write review
                entry<WriteReview> { key ->
                    WriteReviewScreen(
                        serviceId = key.serviceId,
                        onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                        onSubmitSuccess = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                    )
                }

                entry<SearchScreen> {
                    SearchScreen(
                        onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                        onNavigateToServiceDetail = { serviceId -> backstack.add(ServiceDetail(serviceId = serviceId)) },
                        onNavigateToChat = { providerId -> backstack.add(ChatDetail(chatId = providerId)) },
                    )
                }

                entry<AiSearchScreen> {
                    AiSearchScreen(
                        onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                        onNavigateToServiceDetail = { serviceId -> backstack.add(ServiceDetail(serviceId = serviceId)) },
                    )
                }

                entry<Notifications> {
                    NotificationScreen(
                        onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                    )
                }

                // Subscription & Pro upgrade
                entry<Subscription> {
                    SubscriptionScreen(
                        onNavigateBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                        onNavigateToPaymentStatus = { checkoutRequestId ->
                            backstack.add(PaymentStatus(checkoutRequestId = checkoutRequestId))
                        },
                    )
                }

                // Subscription from a specific service (Boost This Service)
                // Handled via SubscriptionScreen's serviceId parameter — pass it through

                // M-Pesa payment status polling
                entry<PaymentStatus> { key ->
                    PaymentStatusScreen(
                        checkoutRequestId = key.checkoutRequestId,
                        onNavigateBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                        onNavigateToProfile = {
                            // Pop back to MainShell
                            while (backstack.size > 1 && backstack.last() !is MainShell) {
                                backstack.remove(backstack.last())
                            }
                        },
                        onRetryPayment = {
                            // Pop PaymentStatus and go back to Subscription
                            if (backstack.size > 1) backstack.remove(backstack.last())
                        },
                    )
                }

                // Pro Analytics dashboard
                entry<Analytics> {
                    AnalyticsScreen(
                        onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                    )
                }
            },
        )

        InAppNotificationBanner(
            banner = activeBanner,
            onTap = { banner ->
                InAppBannerManager.dismissCurrentBanner()
                if (!banner.conversationId.isNullOrBlank()) {
                    mainNavigationViewModel?.triggerDeepLink(DeepLinkAction.OpenChat(banner.conversationId))
                } else if (!banner.deepLinkUri.isNullOrBlank()) {
                    val uri = Uri.parse(banner.deepLinkUri)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    activity?.let {
                        it.intent = intent
                    }
                }
            },
            onDismiss = {
                InAppBannerManager.dismissCurrentBanner()
            },
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}
