package must.kdroiders.hustlehub.ui.features.service.presentation.view.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.sharedComposables.HustleMapLocationPicker
import must.kdroiders.hustlehub.sharedComposables.extractAreaName as sharedExtractAreaName

/** Delegates to the shared [HustleMapLocationPicker]. Kept for backwards-compatibility. */
@Composable
fun MapLocationPickerModal(
    initialLat: Double,
    initialLng: Double,
    onLocationConfirmed: (lat: Double, lng: Double, label: String) -> Unit,
    onDismiss: () -> Unit,
    title: String = stringResource(R.string.map_pin_operating_location),
) {
    HustleMapLocationPicker(
        initialLat = initialLat,
        initialLng = initialLng,
        onLocationConfirmed = onLocationConfirmed,
        onDismiss = onDismiss,
        title = title,
    )
}

/** Delegates to the shared [sharedExtractAreaName]. Kept for backwards-compatibility. */
fun extractAreaName(fullAddress: String): String = sharedExtractAreaName(fullAddress)
