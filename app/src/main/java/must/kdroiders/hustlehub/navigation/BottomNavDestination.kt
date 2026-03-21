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
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Defines every bottom-navigation tab in the main shell.
 *
 * Each subclass carries:
 *  - [route]            the NavHost destination route string
 *  - [label]            human-readable tab label
 *  - [selectedIcon]     icon shown when the tab is active
 *  - [unselectedIcon]   icon shown when the tab is inactive
 */
sealed class BottomNavDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    /** 🏠 Discovery feed */
    data object Home : BottomNavDestination(
        route = "bottom_home",
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
    )

    /** 🗺️ Campus map */
    data object Map : BottomNavDestination(
        route = "bottom_map",
        label = "Map",
        selectedIcon = Icons.Filled.Map,
        unselectedIcon = Icons.Outlined.Map,
    )

    /** 💬 Messages / chat */
    data object Chat : BottomNavDestination(
        route = "bottom_chat",
        label = "Chat",
        selectedIcon = Icons.AutoMirrored.Filled.Chat,
        unselectedIcon = Icons.AutoMirrored.Outlined.Chat,
    )

    /** 👤 My profile */
    data object Profile : BottomNavDestination(
        route = "bottom_profile",
        label = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.PersonOutline,
    )

    companion object {
        /** Ordered list used to build the bottom bar items. */
        val all: List<BottomNavDestination> = listOf(Home, Map, Chat, Profile)
    }
}
