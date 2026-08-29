package must.kdroiders.hustlehub.navigation

import androidx.activity.ComponentActivity
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import must.kdroiders.hustlehub.navigation.AiSearchScreen
import must.kdroiders.hustlehub.navigation.SearchScreen
import must.kdroiders.hustlehub.navigation.ServiceDetail
import must.kdroiders.hustlehub.ui.features.chat.presentation.view.ChatScreen
import must.kdroiders.hustlehub.ui.features.chat.presentation.viewmodel.UnreadCountViewModel
import must.kdroiders.hustlehub.ui.features.home.presentation.view.HomeScreen
import must.kdroiders.hustlehub.ui.features.map.presentation.view.MapScreen
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.ProfileScreen

/**
 * Main application shell hosting the bottom navigation bar.
 *
 * Owns its own [innerBackstack] (a [rememberNavBackStack] starting on [BottomHome]).
 * Tab switching **replaces** the first element rather than pushing, keeping the stack
 * at depth 1 for tabs — this matches how apps like YouTube and Gmail work.
 *
 * Navigation into detail screens is delegated back up to the
 * root back-stack.
 *
 * Architecture (within this shell):
 * ```
 *  innerBackstack: [BottomHome | BottomMap | BottomChat | BottomProfile]
 *       ↓  observed by ↓
 *  NavDisplay (inner)  →  renders the active tab composable
 * ```
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainShellScreen(
    onNavigateToProfileSetup: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToCreateService: () -> Unit = {},
    onNavigateToMyServices: () -> Unit = {},
    onNavigateToEditService: (serviceId: String) -> Unit = {},
    onNavigateToChatDetail: (String) -> Unit = {},
    onNavigateToServiceDetail: (serviceId: String) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToAiSearch: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    onNavigateToAnalytics: (tab: String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val innerBackstack = rememberNavBackStack(BottomHome)

    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val mainNavigationViewModel: MainNavigationViewModel? = if (activity != null) {
        hiltViewModel<MainNavigationViewModel>(viewModelStoreOwner = activity)
    } else {
        null
    }

    LaunchedEffect(mainNavigationViewModel) {
        mainNavigationViewModel?.deepLinkEvent?.collect { action ->
            when (action) {
                DeepLinkAction.OpenProfile -> {
                    innerBackstack.clear()
                    innerBackstack.add(BottomProfile)
                }
                DeepLinkAction.OpenChatList -> {
                    innerBackstack.clear()
                    innerBackstack.add(BottomChat)
                }
                else -> {
                    // OpenChat, OpenServiceDetail, OpenProviderProfile, OpenWriteReview, OpenNotifications
                    // are handled at the root graph level (HustleHubNavGraph)
                }
            }
        }
    }

    val currentKey = innerBackstack.lastOrNull() ?: BottomHome
    val unreadCountViewModel: UnreadCountViewModel = hiltViewModel()
    val unreadMessageCount by unreadCountViewModel.unreadMessageCount.collectAsState(initial = 0)

    var hasVisitedMap by rememberSaveable { mutableStateOf(false) }
    if (currentKey == BottomMap) {
        hasVisitedMap = true
    }

    val motionScheme = MaterialTheme.motionScheme
    val fastEffectsSpec = motionScheme.fastEffectsSpec<Float>()
    val fastSpatialSpec = motionScheme.fastSpatialSpec<Float>()

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            HustleBottomBar(
                currentKey = currentKey,
                unreadMessageCount = unreadMessageCount,
                onTabSelected = { destination ->
                    // Replace entire stack with the selected tab (no accumulation).
                    innerBackstack.clear()
                    innerBackstack.add(destination)
                },
            )
        },
    ) { innerPadding ->
        NavDisplay(
            backStack = innerBackstack,
            modifier = Modifier.padding(innerPadding),
            onBack = { /* tabs don't back-navigate; system back is handled by root */ },
            transitionSpec = {
                (
                    fadeIn(fastEffectsSpec) + scaleIn(
                        initialScale = 0.96f,
                        animationSpec = fastSpatialSpec,
                    )
                ) togetherWith (
                    fadeOut(fastEffectsSpec) + scaleOut(
                        targetScale = 0.96f,
                        animationSpec = fastSpatialSpec,
                    )
                )
            },
            popTransitionSpec = {
                (
                    fadeIn(fastEffectsSpec) + scaleIn(
                        initialScale = 0.96f,
                        animationSpec = fastSpatialSpec,
                    )
                ) togetherWith (
                    fadeOut(fastEffectsSpec) + scaleOut(
                        targetScale = 0.96f,
                        animationSpec = fastSpatialSpec,
                    )
                )
            },
            entryProvider = entryProvider {
                entry<BottomHome> {
                    HomeScreen(
                        onNavigateToServiceDetail = onNavigateToServiceDetail,
                        onNavigateToSearch = onNavigateToSearch,
                        onNavigateToAiSearch = onNavigateToAiSearch,
                        onNavigateToNotifications = onNavigateToNotifications,
                        onNavigateToCreateService = onNavigateToCreateService,
                    )
                }
                entry<BottomMap> {
                    MapScreen(
                        onNavigateToServiceDetail = onNavigateToServiceDetail,
                        onNavigateToChatDetail = onNavigateToChatDetail,
                        onNavigateToNotifications = onNavigateToNotifications,
                    )
                }
                entry<BottomChat> {
                    ChatScreen(
                        onNavigateToChatDetail = onNavigateToChatDetail,
                    )
                }
                entry<BottomProfile> {
                    ProfileScreen(
                        onEditClick = onNavigateToEditProfile,
                        onAddNewServiceClick = onNavigateToCreateService,
                        onServiceClick = onNavigateToEditService,
                        onNavigateToMyServices = onNavigateToMyServices,
                        onSettingsClick = onNavigateToSettings,
                        onNavigateToSubscription = onNavigateToSubscription,
                        onNavigateToAnalytics = onNavigateToAnalytics,
                    )
                }
            },
        )

        // Keep-Alive offscreen map container so returning to Map tab is 0ms instant
        if (hasVisitedMap && currentKey != BottomMap) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0f),
            ) {
                MapScreen(
                    onNavigateToServiceDetail = onNavigateToServiceDetail,
                    onNavigateToChatDetail = onNavigateToChatDetail,
                    onNavigateToNotifications = onNavigateToNotifications,
                )
            }
        }
    }
}
