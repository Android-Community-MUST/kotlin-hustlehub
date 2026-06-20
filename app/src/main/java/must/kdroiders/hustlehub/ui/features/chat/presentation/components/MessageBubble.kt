package must.kdroiders.hustlehub.ui.features.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.gson.Gson
import must.kdroiders.hustlehub.ui.features.chat.domain.model.Message
import must.kdroiders.hustlehub.ui.features.chat.domain.model.MessageType
import must.kdroiders.hustlehub.ui.features.chat.presentation.audio.PlayerState

@Composable
fun MessageBubble(
    message: Message,
    isCurrentUser: Boolean,
    playerState: PlayerState,
    onVoicePlayClick: (String) -> Unit,
    onLocationClick: (Double, Double, String) -> Unit,
    onServiceCardClick: (String) -> Unit,
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
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Shared Image",
                            modifier = Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                        )
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
                        val receiptIcon = if (message.readAt != null) {
                            Icons.Default.DoneAll
                        } else {
                            Icons.Default.Done
                        }
                        Icon(
                            imageVector = receiptIcon,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = if (message.readAt != null) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceMessageContent(
    message: Message,
    textColor: androidx.compose.ui.graphics.Color,
    playerState: PlayerState,
    onPlayClick: (String) -> Unit,
) {
    val voiceUrl = message.mediaUrl ?: ""
    val isPlayingThis = playerState.isPlaying && playerState.playingUrl == voiceUrl
    val progress = if (isPlayingThis && playerState.durationMs > 0) {
        playerState.currentPositionMs.toFloat() / playerState.durationMs
    } else {
        0f
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.width(240.dp),
    ) {
        IconButton(
            onClick = { onPlayClick(voiceUrl) },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector = if (isPlayingThis) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlayingThis) "Pause" else "Play",
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Slider(
                value = progress,
                onValueChange = {},
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = textColor,
                    activeTrackColor = textColor,
                    inactiveTrackColor = textColor.copy(alpha = 0.3f),
                ),
                modifier = Modifier.height(16.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val durationText = formatDuration(
                    if (isPlayingThis) playerState.currentPositionMs else 0
                )
                val totalDurationText = parseVoiceDurationFromMetadata(message.metadata)
                Text(
                    text = durationText,
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.7f),
                )
                Text(
                    text = totalDurationText,
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.7f),
                )
            }
        }
    }
}

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
                .width(240.dp)
                .clickable { onClick(serviceData.serviceId) },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = "Service",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = serviceData.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = serviceData.priceRange,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tap to view service detail",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
private data class ServiceMetadata(val serviceId: String, val title: String, val priceRange: String)
