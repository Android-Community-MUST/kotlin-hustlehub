package must.kdroiders.hustlehub.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.compose.animation.togetherWith
import must.kdroiders.hustlehub.ui.features.chat.ChatScreen
import must.kdroiders.hustlehub.ui.features.home.HomeScreen
import must.kdroiders.hustlehub.ui.features.map.MapScreen
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.ProfileScreen

/**
 * Main application shell hosting the bottom navigation bar.
 *
 * Owns its own [innerBackstack] (a [rememberNavBackStack] starting on [BottomHome]).
 * Tab switching **replaces** the first element rather than pushing, keeping the stack
 * at depth 1 for tabs — this matches how apps like YouTube and Gmail work.
 *
 * Navigation into detail screens (e.g. [PortfolioUpload]) is delegated back up to the
 * root back-stack via [onNavigateToPortfolio].
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
    onNavigateToPortfolio: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val innerBackstack = rememberNavBackStack(BottomHome)

    // The currently active tab key is always the last element.
    val currentKey = innerBackstack.lastOrNull() ?: BottomHome

    Scaffold(
        modifier = modifier,
        bottomBar = {
            HustleBottomBar(
                currentKey = currentKey,
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
                entry<BottomHome>    { HomeScreen() }
                entry<BottomMap>     { MapScreen() }
                entry<BottomChat>    { ChatScreen() }
                entry<BottomProfile> {
                    ProfileScreen(
                        onEditClick = { /* TODO: deep-link to Edit Profile screen */ },
                        onAddNewServiceClick = onNavigateToPortfolio,
                    )
                }
            },
        )
    }
}
