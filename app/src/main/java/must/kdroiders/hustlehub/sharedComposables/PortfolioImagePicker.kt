package must.kdroiders.hustlehub.sharedComposables

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import must.kdroiders.hustlehub.R

private const val GRID_COLUMNS = 3
private val GRID_ITEM_SPACING = 8.dp
private val EMPTY_PLACEHOLDER_HEIGHT = 150.dp
private val REMOVE_BUTTON_SIZE = 32.dp
private val REMOVE_ICON_SIZE = 16.dp
private val REMOVE_BUTTON_PADDING = 4.dp

/**
 * Shared composable for picking and previewing portfolio images.
 *
 * - Uses the system photo picker (ActivityResultContracts.PickMultipleVisualMedia).
 * - Enforces a maximum of [maxImages] (default 6, per spec).
 * - Displays selected images in a 3-column grid with per-image remove buttons.
 * - Add button is disabled once [maxImages] is reached.
 *
 * Lives in `sharedComposables/` per CLAUDE.md design-system conventions.
 */
@Composable
fun PortfolioImagePicker(
    selectedImages: List<Uri>,
    onImagesSelected: (List<Uri>) -> Unit,
    onImageRemoved: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    maxImages: Int = 6,
) {
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = maxImages),
    ) { uris ->
        // Merge with existing selections, deduplicate, and respect the cap
        val merged = (selectedImages + uris).distinct().take(maxImages)
        onImagesSelected(merged)
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(
                    R.string.portfolio_picker_count,
                    selectedImages.size,
                    maxImages,
                ),
                style = MaterialTheme.typography.titleMedium,
            )

            Button(
                onClick = {
                    multiplePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
                enabled = selectedImages.size < maxImages,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.portfolio_add_images_cd),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.portfolio_add_images))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedImages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(EMPTY_PLACEHOLDER_HEIGHT)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.portfolio_empty_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(GRID_COLUMNS),
                verticalArrangement = Arrangement.spacedBy(GRID_ITEM_SPACING),
                horizontalArrangement = Arrangement.spacedBy(GRID_ITEM_SPACING),
                modifier = Modifier.heightIn(max = 300.dp),
            ) {
                items(selectedImages, key = { it.toString() }) { uri ->
                    Box(modifier = Modifier.aspectRatio(1f)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = stringResource(R.string.portfolio_image_cd),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                        )

                        IconButton(
                            onClick = { onImageRemoved(uri) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(REMOVE_BUTTON_SIZE)
                                .padding(REMOVE_BUTTON_PADDING)
                                .background(
                                    color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f),
                                    shape = CircleShape,
                                ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.portfolio_remove_image_cd),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(REMOVE_ICON_SIZE),
                            )
                        }
                    }
                }
            }
        }
    }
}
