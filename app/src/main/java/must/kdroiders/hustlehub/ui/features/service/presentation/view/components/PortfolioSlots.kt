package must.kdroiders.hustlehub.ui.features.service.presentation.view.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import coil.compose.AsyncImage

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PortfolioSlots(
    existingUrls: List<String>,
    newUris: List<Uri>,
    onAddClick: () -> Unit,
    onRemoveExisting: (Int) -> Unit,
    onRemoveNew: (Int) -> Unit,
    maxSlots: Int = 3,
    modifier: Modifier = Modifier,
) {
    val totalFilled = existingUrls.size + newUris.size
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        maxItemsInEachRow = 3,
    ) {
        // Render existing URLs
        existingUrls.forEachIndexed { index, url ->
            Box(modifier = Modifier.fillMaxWidth(0.31f)) {
                FilledSlot(
                    model = url,
                    index = index,
                    onRemove = { onRemoveExisting(index) },
                )
            }
        }
        // Render new URIs
        newUris.forEachIndexed { index, uri ->
            val globalIndex = existingUrls.size + index
            Box(modifier = Modifier.fillMaxWidth(0.31f)) {
                FilledSlot(
                    model = uri,
                    index = globalIndex,
                    onRemove = { onRemoveNew(index) },
                )
            }
        }
        // Render Add Button slot if under maxSlots (or at least 1 empty slot if empty)
        if (totalFilled < maxSlots) {
            Box(modifier = Modifier.fillMaxWidth(0.31f)) {
                EmptySlot(
                    onClick = onAddClick,
                )
            }
        }
    }
}

@Composable
private fun FilledSlot(
    model: Any,
    index: Int,
    onRemove: () -> Unit,
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .semantics { contentDescription = "Portfolio image ${index + 1}" },
    ) {
        AsyncImage(
            model = model,
            contentDescription = "Portfolio photo ${index + 1}",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        // Remove button
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(28.dp) // Minimum touch area enhancement
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.6f))
                .semantics {
                    contentDescription = "Remove portfolio image ${index + 1}"
                    role = Role.Button
                }
                .clickable { onRemove() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp),
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
                shape = RoundedCornerShape(12.dp),
            ).background(MaterialTheme.colorScheme.surface)
            .semantics {
                contentDescription = "Add portfolio image"
                if (onClick != null) role = Role.Button
            }
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = if (onClick != null) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                },
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Add",
                fontSize = 12.sp,
                color = if (onClick != null) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                },
            )
        }
    }
}
