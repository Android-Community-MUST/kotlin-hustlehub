package must.kdroiders.hustlehub.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@Composable
fun PortfolioImagePicker(
    selectedImages: List<Uri>,
    onImagesSelected: (List<Uri>) -> Unit,
    onImageRemoved: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    maxImages: Int = 6
) {
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = maxImages)
    ) { uris ->
        // Prevent exceeding maxImages if the user already had some selected and then opened the picker again
        val combined = (selectedImages + uris).distinct().take(maxImages)
        onImagesSelected(combined)
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Portfolio Images (${selectedImages.size}/$maxImages)",
                style = MaterialTheme.typography.titleMedium
            )
            
            Button(
                onClick = {
                    multiplePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                enabled = selectedImages.size < maxImages
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Images")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Images")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        if (selectedImages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No images selected. Showcase your work!",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                items(selectedImages) { uri ->
                    Box(modifier = Modifier.aspectRatio(1f)) {
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Selected Portfolio Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                        )
                        
                        IconButton(
                            onClick = { onImageRemoved(uri) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(32.dp)
                                .padding(4.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Image",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
