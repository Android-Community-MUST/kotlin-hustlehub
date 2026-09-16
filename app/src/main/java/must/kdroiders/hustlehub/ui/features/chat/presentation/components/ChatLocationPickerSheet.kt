package must.kdroiders.hustlehub.ui.features.chat.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.sharedComposables.HustleLocationPickerSheet

/** Delegates to the shared [HustleLocationPickerSheet]. Kept for backwards-compatibility. */
@Composable
fun ChatLocationPickerSheet(
    onDismiss: () -> Unit,
    onLocationSelected: (lat: Double, lng: Double, label: String) -> Unit,
) {
    HustleLocationPickerSheet(
        onDismiss = onDismiss,
        onLocationSelected = onLocationSelected,
        title = stringResource(R.string.chat_share_location_title),
        mapPickerTitle = stringResource(R.string.chat_choose_on_map),
    )
}
