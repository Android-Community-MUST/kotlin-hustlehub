package must.kdroiders.hustlehub.navigation

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocal that provides the [SharedTransitionScope] created in [HustleHubNav]
 * to any composable in the tree without explicit parameter threading.
 *
 * Usage in a consuming composable:
 * ```kotlin
 * val sharedTransitionScope = LocalSharedTransitionScope.current
 * // Use sharedTransitionScope?.run { Modifier.sharedElement(...) } on shared elements.
 * ```
 */
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
