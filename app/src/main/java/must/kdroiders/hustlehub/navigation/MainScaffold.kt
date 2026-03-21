package must.kdroiders.hustlehub.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import must.kdroiders.hustlehub.ui.features.chat.ChatScreen
import must.kdroiders.hustlehub.ui.features.home.HomeScreen
import must.kdroiders.hustlehub.ui.features.map.MapScreen
import must.kdroiders.hustlehub.ui.features.profile.presentation.view.ProfileScreen

/**
 * Main application shell.
 *
 * Hosts its own inner [NavHostController] for the bottom-navigation graph and
 * renders a [Scaffold] whose bottom bar is the shared [BottomNavigationBar].
 *
 * The outer [rootNavController] (splash/auth graph) is kept as a parameter for any
 * future cross-graph navigation (e.g., signing out → Login screen).
 *
 * Architecture:
 * ```
 * HustleHubNavGraph (outer, splash/auth routes)
 *   └── MainScaffold (inner, bottom-nav routes)
 *         ├── HomeScreen
 *         ├── MapScreen
 *         ├── ChatScreen
 *         └── ProfileScreen
 * ```
 */
@Composable
fun MainScaffold(
    rootNavController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val bottomNavController = rememberNavController()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            BottomNavigationBar(navController = bottomNavController)
        },
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavDestination.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(BottomNavDestination.Home.route) {
                HomeScreen()
            }

            composable(BottomNavDestination.Map.route) {
                MapScreen()
            }

            composable(BottomNavDestination.Chat.route) {
                ChatScreen()
            }

            composable(BottomNavDestination.Profile.route) {
                ProfileScreen(
                    onEditClick = { /* TODO: deep-link to Edit Profile screen */ },
                    onAddNewServiceClick = {
                        rootNavController.navigate(Routes.PORTFOLIO_UPLOAD)
                    },
                )
            }
        }
    }
}
