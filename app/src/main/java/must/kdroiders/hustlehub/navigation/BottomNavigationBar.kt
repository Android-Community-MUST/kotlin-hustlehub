package must.kdroiders.hustlehub.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Bottom navigation bar composable shared across all main-shell destinations.
 *
 * It reads the current route from [navController]'s back stack and highlights the
 * matching [BottomNavDestination]. Navigation is performed with `saveState = true` /
 * `restoreState = true` so that each tab preserves its own scroll / state across switches.
 *
 * @param navController the *inner* NavController that drives the bottom-nav graph.
 * @param modifier optional modifier forwarded to [NavigationBar].
 */
@Composable
fun BottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route

    NavigationBar(modifier = modifier) {
        BottomNavDestination.all.forEach { destination ->
            val selected = currentRoute == destination.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        // Pop up to the start destination of the graph to avoid
                        // building up a large stack of the same destination on
                        // repeated taps of the same item.
                        popUpTo(
                            navController.graph.startDestinationId
                        ) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // re-selecting the same item.
                        launchSingleTop = true
                        // Restore state when re-selecting a previously selected item.
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = destination.label,
                    )
                },
                label = { Text(text = destination.label) },
                alwaysShowLabel = true,
            )
        }
    }
}
