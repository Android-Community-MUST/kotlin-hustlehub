package must.kdroiders.hustlehub.ui.features.service.presentation.view.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun PortfolioSlots(
    existingUrls: List<String>,
    newUris: List<Uri>,
    onAddClick: () -> Unit,
    onRemoveExisting: (Int) -> Unit,
    onRemoveNew: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalFilled = existingUrls.size + newUris.size
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Render 3 slots
        for (slotIndex in 0 until 3) {
            Box(modifier = Modifier.weight(1f)) {
                when {
                    slotIndex < existingUrls.size -> {
                        FilledSlot(
                            model = existingUrls[slotIndex],
                            onRemove = { onRemoveExisting(slotIndex) }
                        )
                    }
                    slotIndex < existingUrls.size + newUris.size -> {
                        val uriIndex = slotIndex - existingUrls.size
                        FilledSlot(
                            model = newUris[uriIndex],
                            onRemove = { onRemoveNew(uriIndex) }
                        )
                    }
                    else -> {
                        EmptySlot(
                            onClick = if (totalFilled < 3) onAddClick else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilledSlot(model: Any, onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
    ) {
        AsyncImage(
            model = model,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Remove button
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onRemove() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun EmptySlot(onClick: (() -> Unit)?) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .background(MaterialTheme.colorScheme.surface)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add image",
                tint = if (onClick != null)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Add",
                fontSize = 12.sp,
                color = if (onClick != null)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
        }
    }
}
