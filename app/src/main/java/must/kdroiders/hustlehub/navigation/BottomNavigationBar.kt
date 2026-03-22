package must.kdroiders.hustlehub.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey

/**
 * Metadata record for a single bottom-navigation tab.
 *
 * Each item carries its corresponding [NavKey], display [label], and
 * selected / unselected icon pair.
 */
private data class BottomTabItem(
    val key: NavKey,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val bottomTabs = listOf(
    BottomTabItem(
        key = BottomHome,
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
    ),
    BottomTabItem(
        key = BottomMap,
        label = "Map",
        selectedIcon = Icons.Filled.Map,
        unselectedIcon = Icons.Outlined.Map,
    ),
    BottomTabItem(
        key = BottomChat,
        label = "Chat",
        selectedIcon = Icons.AutoMirrored.Filled.Chat,
        unselectedIcon = Icons.AutoMirrored.Outlined.Chat,
    ),
    BottomTabItem(
        key = BottomProfile,
        label = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.PersonOutline,
    ),
)

/**
 * Navigation 3–aware bottom bar.
 *
 * Completely decoupled from [NavController]. It receives the active [currentKey]
 * directly from the inner back-stack and calls [onTabSelected] when a tab is tapped.
 * The hosting composable ([MainShellScreen]) is responsible for actually mutating
 * the back-stack.
 *
 * @param currentKey  the [NavKey] of the currently displayed tab.
 * @param onTabSelected  invoked with the [NavKey] of the tapped tab.
 * @param modifier  optional modifier forwarded to [NavigationBar].
 */
@Composable
fun HustleBottomBar(
    currentKey: NavKey,
    onTabSelected: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        bottomTabs.forEach { item ->
            val selected = currentKey == item.key
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(item.key) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                    )
                },
                label = { Text(text = item.label) },
                alwaysShowLabel = true,
            )
        }
    }
}
