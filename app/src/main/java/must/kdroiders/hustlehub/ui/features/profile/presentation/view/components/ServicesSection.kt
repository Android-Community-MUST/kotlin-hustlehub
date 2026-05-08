package must.kdroiders.hustlehub.ui.features.profile.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import must.kdroiders.hustlehub.data.model.Service
import must.kdroiders.hustlehub.ui.theme.HustleActiveGreen
import must.kdroiders.hustlehub.ui.theme.HustleOfflineGray

// ─────────────────────────────────────────────────
// Services section
// ─────────────────────────────────────────────────

@Composable
fun ServicesHeader(
    onAddNewServiceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "My Services",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        TextButton(onClick = onAddNewServiceClick) {
            Text(
                text = "Add New +",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ServiceCard(
    service: Service,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    // Service icon placeholder
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (
                            service.iconUrl.isNotBlank()
                        ) {
                            AsyncImage(
                                model = service.iconUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(
                                        RoundedCornerShape(
                                            8.dp
                                        )
                                    ),
                                contentScale =
                                    ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector =
                                    Icons.Default
                                        .Description,
                                contentDescription =
                                    null,
                                tint = MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant,
                                modifier = Modifier
                                    .size(24.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = service.title,
                            fontSize = 15.sp,
                            fontWeight =
                                FontWeight.SemiBold,
                            color = MaterialTheme
                                .colorScheme.onSurface
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = service.priceRange,
                            fontSize = 13.sp,
                            color = MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                        )
                    }
                }

                Column(
                    horizontalAlignment =
                        Alignment.End
                ) {
                    Switch(
                        checked = service.isActive,
                        onCheckedChange = {
                            onToggle()
                        },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor =
                                MaterialTheme.colorScheme.primary,
                            checkedThumbColor =
                                Color.White,
                            uncheckedTrackColor =
                                HustleOfflineGray
                                    .copy(alpha = 0.4f),
                            uncheckedThumbColor =
                                HustleOfflineGray
                        )
                    )
                    Text(
                        text = if (service.isActive)
                            "Active" else "Offline",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (service.isActive)
                            HustleActiveGreen
                        else HustleOfflineGray
                    )
                }
            }

            // Tags
            if (service.tags.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    service.tags.forEach { tag ->
                        TagChip(label = tag)
                    }
                }
            }
        }
    }
}

@Composable
fun TagChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(
                horizontal = 10.dp,
                vertical = 4.dp
            )
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme
                .onSurfaceVariant
        )
    }
}

