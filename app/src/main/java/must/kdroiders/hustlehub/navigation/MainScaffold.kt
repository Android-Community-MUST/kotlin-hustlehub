package must.kdroiders.hustlehub.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import must.kdroiders.hustlehub.navigation.AiSearchScreen
import must.kdroiders.hustlehub.navigation.SearchScreen
import must.kdroiders.hustlehub.navigation.ServiceDetail
import must.kdroiders.hustlehub.ui.features.chat.presentation.view.ChatScreen
import must.kdroiders.hustlehub.ui.features.chat.presentation.viewmodel.UnreadCountViewModel
import must.kdroiders.hustlehub.ui.features.home.presentation.view.HomeScreen
import must.kdroiders.hustlehub.ui.features.map.MapScreen
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
    modifier: Modifier = Modifier,
) {
    val innerBackstack = rememberNavBackStack(BottomHome)

    // The currently active tab key is always the last element.
    val currentKey = innerBackstack.lastOrNull() ?: BottomHome

    // Observe the total unread count reactively — zero allocation per recomposition.
    val unreadCountViewModel: UnreadCountViewModel = hiltViewModel()
    val totalUnreadCount by unreadCountViewModel.totalUnreadCount.collectAsState(initial = 0)

    Scaffold(
        modifier = modifier,
        bottomBar = {
            HustleBottomBar(
                currentKey = currentKey,
                totalUnreadCount = totalUnreadCount,
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
            // Subtle crossfade between tabs — feels native and doesn't "slide" sideways
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            popTransitionSpec = { fadeIn() togetherWith fadeOut() },
            entryProvider = entryProvider {
                entry<BottomHome> {
                    HomeScreen(
                        onNavigateToServiceDetail = onNavigateToServiceDetail,
                        onNavigateToSearch = onNavigateToSearch,
                        onNavigateToAiSearch = onNavigateToAiSearch,
                    )
                }
                entry<BottomMap> { MapScreen() }
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
                    )
                }
            },
        )
    }
}
