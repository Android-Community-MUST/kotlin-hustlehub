package must.kdroiders.hustlehub.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
        key = BottomBookmarks,
        label = "saved",
        selectedIcon = Icons.Default.Bookmark,
        unselectedIcon = Icons.Outlined.BookmarkBorder,
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
    unreadMessageCount: Int = 0,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp,
    ) {
        bottomTabs.forEach { item ->
            val selected = currentKey == item.key
            val iconScale by animateFloatAsState(
                targetValue = if (selected) 1.15f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
                label = "bottom_tab_icon_scale",
            )

            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(item.key) },
                icon = {
                    val isChatTab = item.key == BottomChat
                    if (isChatTab && unreadMessageCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text(
                                        text = if (unreadMessageCount > 99) "99+" else unreadMessageCount.toString(),
                                    )
                                }
                            },
                        ) {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                modifier = Modifier.scale(iconScale),
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            modifier = Modifier.scale(iconScale),
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
