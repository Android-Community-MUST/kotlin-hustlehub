package must.kdroiders.hustlehub.ui.features.profile.presentation.view.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import must.kdroiders.hustlehub.sharedComposables.ErrorView
import must.kdroiders.hustlehub.sharedComposables.LoadingIndicator

/**
 * Profile-screen loading placeholder.
 *
 * Delegates to the shared [LoadingIndicator] from `sharedComposables/` — DRY principle.
 */
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    LoadingIndicator(modifier = modifier)
}

/**
 * Profile-screen error state with a retry action.
 *
 * Delegates to the shared [ErrorView] from `sharedComposables/` which renders
 * the canonical HustleHub error UI (icon, title, message, retry button).
 */
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ErrorView(
        message = message,
        onRetry = onRetry,
        modifier = modifier,
    )
}
