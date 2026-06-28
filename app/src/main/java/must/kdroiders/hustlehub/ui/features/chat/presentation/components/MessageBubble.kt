package must.kdroiders.hustlehub.ui.features.chat.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.gson.Gson
import must.kdroiders.hustlehub.ui.features.chat.domain.model.Message
import must.kdroiders.hustlehub.ui.features.chat.domain.model.MessageType
import must.kdroiders.hustlehub.ui.features.chat.presentation.audio.PlayerState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    isCurrentUser: Boolean,
    playerState: PlayerState,
    onVoicePlayClick: (String) -> Unit,
    onVoiceSpeedToggle: () -> Unit,
    onLocationClick: (Double, Double, String) -> Unit,
    onServiceCardClick: (String) -> Unit,
    onImageClick: (url: String) -> Unit = {},
    onImageLongClick: (url: String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val bubbleShape = if (isCurrentUser) {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 16.dp,
            bottomEnd = 2.dp,
        )
    } else {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 2.dp,
            bottomEnd = 16.dp,
        )
    }

    val bubbleBackground = if (isCurrentUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isCurrentUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val alignment = if (isCurrentUser) Alignment.End else Alignment.Start

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = alignment,
    ) {
        Box(
            modifier = Modifier
                .clip(bubbleShape)
                .background(bubbleBackground)
                .padding(12.dp),
        ) {
            Column {
                when (message.type) {
                    MessageType.TEXT -> {
                        Text(
                            text = message.content,
                            color = textColor,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }

                    MessageType.IMAGE -> {
                        val imageUrl = message.mediaUrl ?: ""
                        val isUploading = message.id.startsWith("temp_") || imageUrl.isBlank()

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.72f)
                                .heightIn(min = 120.dp, max = 320.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .combinedClickable(
                                    onClick = { if (!isUploading) onImageClick(imageUrl) },
                                    onLongClick = { if (!isUploading) onImageLongClick(imageUrl) },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isUploading) {
                                // Uploading placeholder
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularWavyProgressIndicator(
                                        modifier = Modifier.size(36.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    )
                                }
                            } else {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Shared Image",
                                    modifier = Modifier.fillMaxWidth(),
                                    contentScale = ContentScale.FillWidth,
                                )
                            }
                        }

                        if (!message.content.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = message.content,
                                color = textColor,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }

                    MessageType.VOICE -> {
                        VoiceMessageContent(
                            message = message,
                            textColor = textColor,
                            playerState = playerState,
                            onPlayClick = onVoicePlayClick,
                            onSpeedToggle = onVoiceSpeedToggle,
                        )
                    }

                    MessageType.LOCATION -> {
                        LocationMessageContent(
                            message = message,
                            textColor = textColor,
                            onClick = onLocationClick,
                        )
                    }

                    MessageType.SERVICE_CARD -> {
                        ServiceCardMessageContent(
                            message = message,
                            onClick = onServiceCardClick,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Time and read receipt
                // States: pending (clock) -> sent (single check) -> delivered (double check, dim)
                //         -> read (double check, highlighted)
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val formattedTime = formatTimestamp(message.timestamp)
                    Text(
                        text = formattedTime,
                        fontSize = 10.sp,
                        color = textColor.copy(alpha = 0.7f),
                    )
                    if (isCurrentUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        val isPending = message.id.startsWith("temp_")
                        val isRead = message.readAt != null
                        val isDelivered = message.deliveredAt != null
                        val receiptIcon = when {
                            isPending -> Icons.Default.Schedule
                            isRead || isDelivered -> Icons.Default.DoneAll
                            else -> Icons.Default.Done
                        }
                        // Blue accent only when read; dim tint for sent/delivered
                        val receiptTint = when {
                            isRead -> MaterialTheme.colorScheme.tertiary
                            else -> textColor.copy(alpha = 0.6f)
                        }
                        val receiptDescription = when {
                            isPending -> "Sending"
                            isRead -> "Read"
                            isDelivered -> "Delivered"
                            else -> "Sent"
                        }
                        Icon(
                            imageVector = receiptIcon,
                            contentDescription = receiptDescription,
                            modifier = Modifier.size(12.dp),
                            tint = receiptTint,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun VoiceMessageContent(
    message: Message,
    textColor: Color,
    playerState: PlayerState,
    onPlayClick: (String) -> Unit,
    onSpeedToggle: () -> Unit,
) {
    val voiceUrl = message.mediaUrl ?: ""
    val isPlayingThis = playerState.isPlaying && playerState.playingUrl == voiceUrl
    val isBufferingThis = playerState.isBuffering && playerState.playingUrl == voiceUrl
    val progress = if (playerState.playingUrl == voiceUrl && playerState.durationMs > 0) {
        playerState.currentPositionMs.toFloat() / playerState.durationMs
    } else {
        0f
    }

    // Stable pseudo-random waveform bars seeded from the URL hash
    val waveformBars = remember(voiceUrl) {
        val seed = voiceUrl.hashCode().toLong()
        val rng = java.util.Random(seed)
        List(WAVEFORM_BAR_COUNT) { 0.2f + rng.nextFloat() * 0.8f }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.width(240.dp),
    ) {
        // Play/Pause or Buffering spinner
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(38.dp),
        ) {
            if (isBufferingThis) {
                CircularWavyProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = textColor.copy(alpha = 0.8f),
                    trackColor = textColor.copy(alpha = 0.15f),
                )
            } else {
                IconButton(
                    onClick = { onPlayClick(voiceUrl) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = textColor.copy(alpha = 0.15f),
                        contentColor = textColor,
                    ),
                    modifier = Modifier.size(38.dp),
                ) {
                    Icon(
                        imageVector = if (isPlayingThis) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlayingThis) "Pause" else "Play",
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Waveform visualization
            WaveformProgress(
                bars = waveformBars,
                progress = progress,
                activeColor = textColor,
                inactiveColor = textColor.copy(alpha = 0.3f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
            )

            // Position / Total duration + speed toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val posText = formatDuration(
                    if (playerState.playingUrl == voiceUrl) playerState.currentPositionMs else 0,
                )
                val totalText = if (playerState.playingUrl == voiceUrl && playerState.durationMs > 0) {
                    formatDuration(playerState.durationMs)
                } else {
                    parseVoiceDurationFromMetadata(message.metadata)
                }
                Text(
                    text = "$posText / $totalText",
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.7f),
                )

                // Speed toggle — only shown while something is loaded
                if (playerState.playingUrl == voiceUrl) {
                    val speedLabel = when (playerState.playbackSpeed) {
                        1.5f -> "1.5x"
                        2.0f -> "2x"
                        else -> "1x"
                    }
                    Text(
                        text = speedLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor.copy(alpha = 0.85f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onSpeedToggle() }
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                    )
                }
            }
        }
    }
}

/**
 * Renders a horizontal row of vertical bars whose fill level reflects [progress].
 * Bars to the left of the playhead use [activeColor]; bars to the right use [inactiveColor].
 */
@Composable
private fun WaveformProgress(
    bars: List<Float>,
    progress: Float,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier,
    barWidthDp: Dp = 3.dp,
    gapDp: Dp = 2.dp,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 150),
        label = "waveformProgress",
    )

    Canvas(modifier = modifier) {
        val totalBars = bars.size
        val barWidth = barWidthDp.toPx()
        val gap = gapDp.toPx()
        val totalWidth = totalBars * (barWidth + gap) - gap
        val startX = (size.width - totalWidth) / 2f
        val midY = size.height / 2f
        val playheadX = startX + totalWidth * animatedProgress

        bars.forEachIndexed { i, amplitude ->
            val barX = startX + i * (barWidth + gap)
            val barHeight = amplitude * size.height
            val color = if (barX <= playheadX) activeColor else inactiveColor

            drawLine(
                color = color,
                start = Offset(barX + barWidth / 2f, midY - barHeight / 2f),
                end = Offset(barX + barWidth / 2f, midY + barHeight / 2f),
                strokeWidth = barWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
        }
    }
}

private const val WAVEFORM_BAR_COUNT = 40

@Composable
private fun LocationMessageContent(
    message: Message,
    textColor: androidx.compose.ui.graphics.Color,
    onClick: (Double, Double, String) -> Unit,
) {
    val gson = remember { Gson() }
    val locationData = remember(message.metadata) {
        try {
            gson.fromJson(message.metadata ?: "", LocationMetadata::class.java)
        } catch (e: Exception) {
            null
        }
    }

    Column(
        modifier = Modifier
            .width(220.dp)
            .clickable {
                if (locationData != null) {
                    onClick(locationData.lat, locationData.lng, locationData.label ?: "Shared Location")
                }
            },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = textColor,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = locationData?.label ?: "Shared Location",
                color = textColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Tap to open in maps",
            color = textColor.copy(alpha = 0.8f),
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun ServiceCardMessageContent(
    message: Message,
    onClick: (String) -> Unit,
) {
    val gson = remember { Gson() }
    val serviceData = remember(message.metadata) {
        try {
            gson.fromJson(message.metadata ?: "", ServiceMetadata::class.java)
        } catch (e: Exception) {
            null
        }
    }

    if (serviceData != null) {
        Card(
            modifier = Modifier
                .width(260.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(12.dp),
                ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header label
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SERVICE REQUEST",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.8.sp,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 0.8.dp,
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Service title
                Text(
                    text = serviceData.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Category · Price range
                Text(
                    text = buildString {
                        if (!serviceData.category.isNullOrBlank()) append("${serviceData.category} · ")
                        append(serviceData.priceRange)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Provider name
                if (!serviceData.providerName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = serviceData.providerName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // "View Service" CTA
                OutlinedButton(
                    onClick = { onClick(serviceData.serviceId) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 12.dp,
                        vertical = 6.dp,
                    ),
                ) {
                    Text(
                        text = "View Service",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    } else {
        Text(
            text = message.content,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

private fun formatTimestamp(isoString: String?): String {
    if (isoString == null) return ""
    return try {
        val parts = isoString.split("T")
        if (parts.size >= 2) {
            parts[1].substring(0, 5) // "HH:MM"
        } else {
            isoString
        }
    } catch (e: Exception) {
        isoString
    }
}

private fun formatDuration(ms: Int): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

private fun parseVoiceDurationFromMetadata(metadata: String?): String {
    if (metadata == null) return "0:00"
    return try {
        val durationSec = Gson().fromJson(metadata, VoiceMetadata::class.java).durationSeconds
        formatDuration(durationSec * 1000)
    } catch (e: Exception) {
        "0:00"
    }
}

// Metadata structures
private data class VoiceMetadata(val durationSeconds: Int)
private data class LocationMetadata(val lat: Double, val lng: Double, val label: String?)
private data class ServiceMetadata(
    val serviceId: String,
    val title: String,
    val priceRange: String,
    val category: String? = null,
    val providerName: String? = null,
)
