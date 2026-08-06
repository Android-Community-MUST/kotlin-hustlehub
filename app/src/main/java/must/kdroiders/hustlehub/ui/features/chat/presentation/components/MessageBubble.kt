package must.kdroiders.hustlehub.ui.features.chat.presentation.components

import android.location.Location
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleButtonVariant
import must.kdroiders.hustlehub.ui.features.chat.domain.model.Message
import must.kdroiders.hustlehub.ui.features.chat.domain.model.MessageType
import must.kdroiders.hustlehub.ui.features.chat.domain.model.isDeleted
import must.kdroiders.hustlehub.ui.features.chat.presentation.audio.PlayerState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

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
    onReply: (Message) -> Unit = {},
    onDeleteForMe: (Message) -> Unit = {},
    onDeleteForEveryone: (Message) -> Unit = {},
    onReportMessage: (Message) -> Unit = {},
    modifier: Modifier = Modifier,
    currentUserLocation: android.location.Location? = null,
    isOtherUserOnline: Boolean = false,
) {
    val haptic = LocalHapticFeedback.current
    var dragAmountX by remember { mutableStateOf(0f) }
    var replyThreshold by remember { mutableStateOf(0f) }
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        // Background layer: Reply Icon (only shown if dragging)
        if (dragAmountX > 0f) {
            val scale = (dragAmountX / replyThreshold).coerceIn(0f, 1.2f)
            val alpha = (dragAmountX / replyThreshold).coerceIn(0f, 1f)
            val iconTint = if (dragAmountX >= replyThreshold) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            }

            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Reply,
                    contentDescription = "Reply",
                    tint = iconTint,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            alpha = alpha,
                        ),
                )
            }
        }

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
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {}
                .then(
                    if (message.isDeleted) {
                        Modifier
                    } else {
                        Modifier.swipeToReply(
                            haptic = haptic,
                            onReply = { onReply(message) },
                            onDragStateChanged = { dragX, thresh ->
                                dragAmountX = dragX
                                replyThreshold = thresh
                            },
                        )
                    },
                ),
            horizontalAlignment = alignment,
        ) {
            Box(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(bubbleBackground)
                    .combinedClickable(
                        enabled = !message.isDeleted,
                        onClick = { /* Child views handle their own click events */ },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showMenu = true
                        },
                    ).padding(12.dp),
            ) {
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete for me") },
                        onClick = {
                            showMenu = false
                            onDeleteForMe(message)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Report Message") },
                        onClick = {
                            showMenu = false
                            onReportMessage(message)
                        },
                    )
                    if (isCurrentUser) {
                        DropdownMenuItem(
                            text = { Text("Delete for everyone") },
                            onClick = {
                                showMenu = false
                                onDeleteForEveryone(message)
                            },
                        )
                    }
                    if (message.type == MessageType.IMAGE && !message.mediaUrl.isNullOrBlank()) {
                        DropdownMenuItem(
                            text = { Text("Save Image") },
                            onClick = {
                                showMenu = false
                                onImageLongClick(message.mediaUrl)
                            },
                        )
                    }
                }

                Column {
                    // Render Reply Quote Box if message has reply details in metadata
                    val replyData = remember(message.metadata) {
                        try {
                            if (message.metadata != null && !message.isDeleted) {
                                val gson = Gson()
                                val obj = gson.fromJson(message.metadata, JsonObject::class.java)
                                if (obj.has("replyToId")) {
                                    ReplyMetadata(
                                        replyToId = obj.get("replyToId")?.asString,
                                        replyToContent = obj.get("replyToContent")?.asString,
                                        replyToSenderName = obj.get("replyToSenderName")?.asString,
                                    )
                                } else {
                                    null
                                }
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            null
                        }
                    }

                    if (replyData != null) {
                        val barColor = if (isCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                        val quoteBg = if (isCurrentUser) {
                            MaterialTheme.colorScheme.onPrimary.copy(
                                alpha = 0.15f,
                            )
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
                        }
                        val replyTextColor = if (isCurrentUser) {
                            MaterialTheme.colorScheme.onPrimary.copy(
                                alpha = 0.9f,
                            )
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Row(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(quoteBg),
                        ) {
                            // Vertical accent bar on the left
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(42.dp)
                                    .background(barColor),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(
                                modifier = Modifier
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                                    .align(Alignment.CenterVertically),
                            ) {
                                Text(
                                    text = replyData.replyToSenderName ?: "User",
                                    fontWeight = FontWeight.Bold,
                                    color = barColor,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = replyData.replyToContent ?: "",
                                    color = replyTextColor,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }

                    if (message.isDeleted) {
                        Text(
                            text = if (isCurrentUser) "You deleted this message" else "This message was deleted",
                            color = textColor.copy(alpha = 0.65f),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic,
                            ),
                        )
                    } else {
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
                                        .run {
                                            if (isUploading) {
                                                this
                                            } else {
                                                clickable(role = Role.Button) { onImageClick(imageUrl) }
                                            }
                                        },
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
                                    currentUserLocation = currentUserLocation,
                                    onClick = onLocationClick,
                                )
                            }

                            MessageType.SERVICE_CARD -> {
                                ServiceCardMessageContent(
                                    message = message,
                                    onClick = onServiceCardClick,
                                )
                            }

                            MessageType.SYSTEM, MessageType.SERVICE_COMPLETED -> {
                                Text(
                                    text = message.content,
                                    color = textColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
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
                            val isDelivered = message.deliveredAt != null && (isOtherUserOnline || isRead)
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
            modifier = Modifier.size(48.dp),
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
                    modifier = Modifier.size(48.dp),
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
                            .clickable(role = Role.Button) { onSpeedToggle() }
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
                cap = StrokeCap.Round,
            )
        }
    }
}

private const val WAVEFORM_BAR_COUNT = 40

@Composable
private fun LocationMessageContent(
    message: Message,
    textColor: Color,
    currentUserLocation: Location?,
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
    val latLng = remember(locationData) {
        if (locationData != null) LatLng(locationData.lat, locationData.lng) else null
    }

    val distanceText = remember(locationData, currentUserLocation) {
        if (locationData != null && currentUserLocation != null) {
            val results = FloatArray(1)
            try {
                Location.distanceBetween(
                    locationData.lat,
                    locationData.lng,
                    currentUserLocation.latitude,
                    currentUserLocation.longitude,
                    results,
                )
                val distanceMeters = results[0]
                if (distanceMeters < 1000f) {
                    "${distanceMeters.toInt()}m away"
                } else {
                    "${String.format("%.1f", distanceMeters / 1000f)}km away"
                }
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    Column(
        modifier = Modifier
            .width(250.dp)
            .clickable(role = Role.Button) {
                if (locationData != null) {
                    onClick(
                        locationData.lat,
                        locationData.lng,
                        locationData.label ?: "Shared Location",
                    )
                }
            }.padding(2.dp),
    ) {
        // Location title & distance badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 6.dp),
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null, // decorative
                tint = textColor,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = locationData?.label ?: "Shared Location",
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (distanceText != null) {
                    Text(
                        text = distanceText,
                        color = textColor.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        // Map Preview Thumbnail — renders native GoogleMap view
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(135.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(textColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            if (latLng != null) {
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(latLng, 16f)
                }
                val markerState = rememberUpdatedMarkerState(position = latLng)
                val mapUiSettings = remember {
                    MapUiSettings(
                        zoomControlsEnabled = false,
                        scrollGesturesEnabled = false,
                        zoomGesturesEnabled = false,
                        tiltGesturesEnabled = false,
                        rotationGesturesEnabled = false,
                        myLocationButtonEnabled = false,
                        compassEnabled = false,
                        mapToolbarEnabled = false,
                    )
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = mapUiSettings,
                    onMapClick = {
                        if (locationData != null) {
                            onClick(
                                locationData.lat,
                                locationData.lng,
                                locationData.label ?: "Shared Location",
                            )
                        }
                    },
                ) {
                    Marker(
                        state = markerState,
                        title = locationData?.label ?: "Shared Location",
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = textColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(36.dp),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Shared Location Pin",
                        fontSize = 11.sp,
                        color = textColor.copy(alpha = 0.7f),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Get Directions CTA line
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Tap to view in Maps",
                color = textColor.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
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

                HustleButton(
                    text = "View Service",
                    onClick = { onClick(serviceData.serviceId) },
                    variant = HustleButtonVariant.Outlined,
                    modifier = Modifier.fillMaxWidth(),
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
    if (isoString.isNullOrBlank()) return ""
    return try {
        val instant = Instant.parse(isoString)
        val zonedDateTime = instant.atZone(ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        zonedDateTime.format(formatter)
    } catch (e: Exception) {
        try {
            val parts = isoString.split("T")
            if (parts.size >= 2) parts[1].substring(0, 5) else isoString
        } catch (_: Exception) {
            isoString
        }
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
private data class ReplyMetadata(
    val replyToId: String?,
    val replyToContent: String?,
    val replyToSenderName: String?,
)

@Composable
fun Modifier.swipeToReply(
    haptic: HapticFeedback,
    onReply: () -> Unit,
    onDragStateChanged: (dragX: Float, thresholdPx: Float) -> Unit,
): Modifier {
    val density = LocalDensity.current
    val maxDragX = 80.dp
    val maxDragPx = with(density) { maxDragX.toPx() }
    val thresholdX = 50.dp
    val thresholdPx = with(density) { thresholdX.toPx() }

    var dragX by remember { mutableStateOf(0f) }
    var hapticTriggered by remember { mutableStateOf(false) }
    val animatedDragX by animateFloatAsState(
        targetValue = dragX,
        animationSpec = tween(durationMillis = 150),
        label = "swipeToReplyDragX",
    )

    // Notify parent of dragging state so it can display the background icon
    onDragStateChanged(animatedDragX, thresholdPx)

    return this
        .offset { IntOffset(animatedDragX.roundToInt(), 0) }
        .pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragStart = {
                    dragX = 0f
                    hapticTriggered = false
                },
                onHorizontalDrag = { _, dragAmount ->
                    // Only allow swiping right (left-to-right) for reply
                    val newDragX = (dragX + dragAmount).coerceIn(0f, maxDragPx)
                    dragX = newDragX

                    if (newDragX >= thresholdPx && !hapticTriggered) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        hapticTriggered = true
                    } else if (newDragX < thresholdPx) {
                        hapticTriggered = false
                    }
                },
                onDragEnd = {
                    if (dragX >= thresholdPx) {
                        onReply()
                    }
                    dragX = 0f
                    hapticTriggered = false
                },
                onDragCancel = {
                    dragX = 0f
                    hapticTriggered = false
                },
            )
        }
}
