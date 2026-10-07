package must.kdroiders.hustlehub.navigation

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.animation.SharedTransitionLayout
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.core.auth.AuthStateViewModel
import must.kdroiders.hustlehub.core.notification.InAppBannerManager
import must.kdroiders.hustlehub.core.notification.InAppNotificationBanner
import must.kdroiders.hustlehub.core.profile.ProfileCompletenessChecker
import must.kdroiders.hustlehub.onboarding.OnboardingScreen
import must.kdroiders.hustlehub.sharedComposables.ProfileGateBottomSheet
import must.kdroiders.hustlehub.sharedComposables.ProfileGateType
import must.kdroiders.hustlehub.splash.SplashDestination
import must.kdroiders.hustlehub.splash.SplashScreen
import must.kdroiders.hustlehub.ui.features.admin.presentation.view.AdminDashboardScreen
import must.kdroiders.hustlehub.ui.features.analytics.presentation.view.AnalyticsScreen
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthState
import must.kdroiders.hustlehub.ui.features.auth.presentation.view.AccountSuspendedScreen
import must.kdroiders.hustlehub.ui.features.auth.presentation.view.ChangePasswordScreen
import must.kdroiders.hustlehub.ui.features.auth.presentation.view.EmailVerificationScreen
import must.kdroiders.hustlehub.ui.features.auth.presentation.view.LoginScreen
import must.kdroiders.hustlehub.ui.features.auth.presentation.view.SignUpScreen
import must.kdroiders.hustlehub.ui.features.auth.presentation.viewmodel.LoginViewModel
import must.kdroiders.hustlehub.ui.features.bookmarks.BookmarkScreen
import must.kdroiders.hustlehub.ui.features.chat.presentation.view.ChatDetailScreen
import must.kdroiders.hustlehub.ui.features.help.presentation.view.HelpScreen
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
                is DeepLinkAction.OpenSubscription -> {
                    backstack.add(Subscription(serviceId = action.serviceId))
                }
                is DeepLinkAction.OpenProfile, is DeepLinkAction.OpenChatList -> {
                    while (backstack.size > 1 && backstack.last() != MainShell) {
                        backstack.remove(backstack.last())
                    }
                }
            }
        }
    }

    val pendingDeepLinkViewModel: PendingDeepLinkViewModel = if (activity != null) {
        hiltViewModel<PendingDeepLinkViewModel>(viewModelStoreOwner = activity)
    } else {
        hiltViewModel()
    }
    val pendingLink by pendingDeepLinkViewModel.pendingLink.collectAsState()
    LaunchedEffect(pendingLink, backstack.lastOrNull()) {
        val link = pendingLink ?: return@LaunchedEffect
        if (backstack.none { it is MainShell }) {
            timber.log.Timber
                .tag("SHARE_LINK")
                .d("[SHARE_LINK] Deferred link waiting for MainShell in backstack: %s", link)
            return@LaunchedEffect
        }
        val (target, id) = link
        timber.log.Timber
            .tag("SHARE_LINK")
            .d("[SHARE_LINK] Routing deferred deep link: target=%s, id=%s", target, id)
        val action = when (target) {
            "profile" -> DeepLinkAction.OpenProviderProfile(id)
            "service" -> DeepLinkAction.OpenServiceDetail(id)
            else -> {
                timber.log.Timber
                    .tag("SHARE_LINK")
                    .w("[SHARE_LINK] Unknown target in deferred deep link: %s", target)
                return@LaunchedEffect
            }
        }
        pendingDeepLinkViewModel.consume()
        timber.log.Timber
            .tag("SHARE_LINK")
            .d("[SHARE_LINK] Consumed pending link and triggering navigation action: %s", action)
        mainNavigationViewModel?.triggerDeepLink(action)
    }

    val activeBanner by InAppBannerManager.activeBanner.collectAsState()

    val profileGateViewModel: ProfileGateViewModel = if (activity != null) {
        hiltViewModel<ProfileGateViewModel>(viewModelStoreOwner = activity)
    } else {
        hiltViewModel()
    }
    val cachedUser by profileGateViewModel.cachedUser.collectAsState()
    val navScope = rememberCoroutineScope()

    var showBookingGate by remember { mutableStateOf(false) }
    var pendingChatArgs by remember { mutableStateOf<ChatDetail?>(null) }
    var showListingGate by remember { mutableStateOf(false) }
    var isSavingGate by remember { mutableStateOf(false) }
    var gateError by remember { mutableStateOf<String?>(null) }

    if (showBookingGate) {
        ProfileGateBottomSheet(
            gateType = ProfileGateType.BOOKING,
            initialCampusLocation = cachedUser.campusLocation,
            isSaving = isSavingGate,
            errorMessage = gateError,
            onDismiss = {
                showBookingGate = false
                pendingChatArgs = null
                gateError = null
            },
            onSave = { campusLocation, phone, bio ->
                isSavingGate = true
                gateError = null
                navScope.launch {
                    profileGateViewModel
                        .saveLocation(campusLocation, phone, bio)
                        .onSuccess {
                            isSavingGate = false
                            showBookingGate = false
                            pendingChatArgs?.let { backstack.add(it) }
                            pendingChatArgs = null
                        }.onFailure {
                            isSavingGate = false
                            gateError = "Could not save location. Please try again."
                        }
                }
            },
        )
    }

    if (showListingGate) {
        ProfileGateBottomSheet(
            gateType = ProfileGateType.LISTING,
            initialCampusLocation = cachedUser.campusLocation,
            initialPhone = cachedUser.phone,
            initialBio = cachedUser.bio,
            isSaving = isSavingGate,
            errorMessage = gateError,
            onDismiss = {
                showListingGate = false
                gateError = null
            },
            onSave = { campusLocation, phone, bio ->
                isSavingGate = true
                gateError = null
                navScope.launch {
                    profileGateViewModel
                        .saveLocation(campusLocation, phone, bio)
                        .onSuccess {
                            isSavingGate = false
                            showListingGate = false
                            backstack.add(CreateService())
                        }.onFailure {
                            isSavingGate = false
                            gateError = "Could not save profile. Please try again."
                        }
                }
            },
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SharedTransitionLayout {
            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
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
                                        is SplashDestination.AccountSuspended -> AccountSuspendedKey(
                                            reason = destination.reason,
                                            suspendedUntil = destination.suspendedUntil,
                                        )
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
                                    if (hasProfile) {
                                        backstack.add(MainShell)
                                    } else {
                                        backstack.add(ProfileSetup)
                                    }
                                }
                            }

                            LoginScreen(
                                prefilledEmail = key.email,
                                onLoginSuccess = { hasProfile ->
                                    backstack.clear()
                                    if (hasProfile) {
                                        backstack.add(MainShell)
                                    } else {
                                        backstack.add(ProfileSetup)
                                    }
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
                                    backstack.add(ProfileSetup)
                                },
                            )
                        }

                        entry<SignUp> {
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
                                    if (hasProfile) {
                                        backstack.add(MainShell)
                                    } else {
                                        backstack.add(ProfileSetup)
                                    }
                                }
                            }

                            SignUpScreen(
                                onNavigateToLogin = { email ->
                                    if (backstack.isNotEmpty()) backstack.remove(backstack.last())
                                    if (backstack.isEmpty()) backstack.add(Login(email = email))
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

                        // Account Suspended
                        entry<AccountSuspendedKey> { key ->
                            AccountSuspendedScreen(
                                reason = key.reason.ifBlank { "Violation of terms of service." },
                                suspendedUntil = key.suspendedUntil,
                                onLogout = {
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
                                onNavigateToCreateService = {
                                    if (ProfileCompletenessChecker.needsProfileForListing(cachedUser)) {
                                        showListingGate = true
                                    } else {
                                        backstack.add(CreateService())
                                    }
                                },
                                onNavigateToMyServices = { backstack.add(MyServices) },
                                onNavigateToEditService = { serviceId ->
                                    backstack.add(
                                        CreateService(
                                            serviceId = serviceId,
                                        ),
                                    )
                                },
                                onNavigateToChatDetail = { chatId -> backstack.add(ChatDetail(chatId = chatId)) },
                                onNavigateToServiceDetail = { serviceId ->
                                    backstack.add(
                                        ServiceDetail(
                                            serviceId = serviceId,
                                        ),
                                    )
                                },
                                onNavigateToSearch = { backstack.add(SearchScreen) },
                                onNavigateToAiSearch = { backstack.add(AiSearchScreen) },
                                onNavigateToEditProfile = { backstack.add(EditProfile) },
                                onNavigateToNotifications = { backstack.add(Notifications) },
                                onNavigateToSubscription = { backstack.add(Subscription()) },
                                onNavigateToAnalytics = { tab -> backstack.add(Analytics(initialTab = tab)) },
                                onNavigateToAdminDashboard = { backstack.add(AdminDashboard) },
                            )
                        }

                        entry<Settings> {
                            SettingsScreen(
                                onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                                onNavigateToEditProfile = { backstack.add(EditProfile) },
                                onNavigateToChangePassword = { backstack.add(ChangePassword) },
                                onNavigateToNotificationPreferences = { backstack.add(NotificationPreferences) },
                                onNavigateToPrivacy = { backstack.add(PrivacySettings) },
                                onNavigateToBlockedUsers = { backstack.add(BlockedUsers) },
                                onNavigateToSubscription = { backstack.add(Subscription()) },
                                onNavigateToHelp = { backstack.add(HelpFaq) },
                                onAccountDeleted = {
                                    backstack.clear()
                                    backstack.add(Onboarding)
                                },
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

                        entry<HelpFaq> {
                            HelpScreen(
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
                                onNavigateToSubscription = { backstack.add(Subscription()) },
                            )
                        }

                        // My services management
                        entry<MyServices> {
                            MyServicesScreen(
                                onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                                onCreateService = { backstack.add(CreateService()) },
                                onEditService = { serviceId -> backstack.add(CreateService(serviceId = serviceId)) },
                                onBoostService = { serviceId -> backstack.add(Subscription(serviceId = serviceId)) },
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
                                onNavigateToServiceDetail = { serviceId ->
                                    backstack.add(
                                        ServiceDetail(
                                            serviceId = serviceId,
                                        ),
                                    )
                                },
                                onNavigateToWriteReview = { serviceId, providerId ->
                                    backstack.add(
                                        WriteReview(
                                            serviceId = serviceId,
                                            providerId = providerId,
                                        ),
                                    )
                                },
                            )
                        }

                        // Service detail — full provider profile, portfolio and reviews.
                        entry<ServiceDetail> { key ->
                            ServiceDetailScreen(
                                serviceId = key.serviceId,
                                onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                                onNavigateToChat = { providerId, serviceId, title, category, priceRange, providerName ->
                                    val chatDest = ChatDetail(
                                        chatId = providerId,
                                        serviceId = serviceId,
                                        serviceTitle = title,
                                        serviceCategory = category,
                                        servicePriceRange = priceRange,
                                        providerName = providerName,
                                    )
                                    if (ProfileCompletenessChecker.needsLocationForBooking(cachedUser)) {
                                        pendingChatArgs = chatDest
                                        showBookingGate = true
                                    } else {
                                        backstack.add(chatDest)
                                    }
                                },
                                onNavigateToProviderProfile = { providerId ->
                                    backstack.add(
                                        ProviderProfile(providerId = providerId),
                                    )
                                },
                                onNavigateToWriteReview = { serviceId, providerId ->
                                    backstack.add(
                                        WriteReview(
                                            serviceId = serviceId,
                                            providerId = providerId,
                                        ),
                                    )
                                },
                                onNavigateToAllReviews = { serviceId ->
                                    backstack.add(AllReviews(serviceId = serviceId))
                                },
                                onNavigateToEditService = { serviceId ->
                                    backstack.add(CreateService(serviceId = serviceId))
                                },
                            )
                        }

                        // All reviews
                        entry<AllReviews> { key ->
                            AllReviewsScreen(
                                serviceId = key.serviceId,
                                onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                                onNavigateToWriteReview = { serviceId, providerId, reviewId, initialRating, initialComment, initialIsAnonymous ->
                                    backstack.add(
                                        WriteReview(
                                            serviceId = serviceId,
                                            providerId = providerId,
                                            reviewId = reviewId,
                                            initialRating = initialRating,
                                            initialComment = initialComment,
                                            initialIsAnonymous = initialIsAnonymous,
                                        ),
                                    )
                                },
                            )
                        }

                        // Provider public profile
                        entry<ProviderProfile> { key ->
                            ProviderProfileScreen(
                                providerId = key.providerId,
                                onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                                onNavigateToChat = { providerId -> backstack.add(ChatDetail(chatId = providerId)) },
                                onNavigateToEditProfile = { backstack.add(EditProfile) },
                                onNavigateToMyServices = { backstack.add(MyServices) },
                                onNavigateToServiceDetail = { serviceId ->
                                    backstack.add(
                                        ServiceDetail(
                                            serviceId = serviceId,
                                        ),
                                    )
                                },
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
                                reviewId = key.reviewId,
                                initialRating = key.initialRating,
                                initialComment = key.initialComment,
                                initialIsAnonymous = key.initialIsAnonymous,
                                onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                                onSubmitSuccess = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                                onNavigateToChat = { providerId, serviceId, title, category, priceRange, providerName ->
                                    val chatDest = ChatDetail(
                                        chatId = providerId,
                                        serviceId = serviceId,
                                        serviceTitle = title,
                                        serviceCategory = category,
                                        servicePriceRange = priceRange,
                                        providerName = providerName,
                                    )
                                    if (ProfileCompletenessChecker.needsLocationForBooking(cachedUser)) {
                                        pendingChatArgs = chatDest
                                        showBookingGate = true
                                    } else {
                                        backstack.add(chatDest)
                                    }
                                },
                            )
                        }

                        entry<SearchScreen> {
                            SearchScreen(
                                onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                                onNavigateToServiceDetail = { serviceId ->
                                    backstack.add(
                                        ServiceDetail(
                                            serviceId = serviceId,
                                        ),
                                    )
                                },
                                onNavigateToChat = { providerId -> backstack.add(ChatDetail(chatId = providerId)) },
                            )
                        }

                        entry<AiSearchScreen> {
                            AiSearchScreen(
                                onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                                onNavigateToServiceDetail = { serviceId ->
                                    backstack.add(
                                        ServiceDetail(
                                            serviceId = serviceId,
                                        ),
                                    )
                                },
                            )
                        }

                        entry<Notifications> {
                            NotificationScreen(
                                onBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                            )
                        }

                        // Subscription & Pro upgrade
                        entry<Subscription> { key ->
                            SubscriptionScreen(
                                onNavigateBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                                onNavigateToPaymentStatus = { checkoutRequestId ->
                                    backstack.add(PaymentStatus(checkoutRequestId = checkoutRequestId))
                                },
                                serviceId = key.serviceId,
                            )
                        }

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
                        entry<BottomBookmarks> {
                            BookmarkScreen(
                                onBack = {
                                    if (backstack.size > 1) backstack.remove(backstack.last())
                                },
                                onItemClick = { serviceId ->
                                    backstack.add(ServiceDetail(serviceId))
                                },
                            )
                        }

                        // In-app Admin Dashboard
                        entry<AdminDashboard> {
                            AdminDashboardScreen(
                                onNavigateBack = { if (backstack.size > 1) backstack.remove(backstack.last()) },
                            )
                        }
                    },
                )
            }
        }

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
