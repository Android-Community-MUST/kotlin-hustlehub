package must.kdroiders.hustlehub.ui.features.chat.presentation.view

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.ui.features.chat.presentation.audio.VoiceRecorder
import must.kdroiders.hustlehub.ui.features.chat.presentation.components.DateSeparator
import must.kdroiders.hustlehub.ui.features.chat.presentation.components.MessageBubble
import must.kdroiders.hustlehub.ui.features.chat.presentation.viewmodel.ChatDetailViewModel
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversationId: String,
    onBackClick: () -> Unit,
    onNavigateToServiceDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatDetailViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    var textInput by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var recordingDurationSeconds by remember { mutableIntStateOf(0) }
    var recordingFile by remember { mutableStateOf<File?>(null) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showAttachmentMenu by remember { mutableStateOf(false) }

    val voiceRecorder = remember { VoiceRecorder(context) }

    // Initialize the conversation when screen loads or ID changes
    LaunchedEffect(conversationId) {
        viewModel.initialize(conversationId)
    }

    // Show errors in snackbar
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Scroll to bottom when list size changes or typing changes
    LaunchedEffect(state.messages.size, state.isTyping) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    // Typing indicator ticker
    LaunchedEffect(textInput) {
        if (textInput.isNotBlank()) {
            viewModel.sendTypingIndicator(true)
            delay(3000)
            viewModel.sendTypingIndicator(false)
        }
    }

    // Image picker launcher
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                    if (bytes != null) {
                        viewModel.sendImageMessage(bytes)
                    } else {
                        Toast.makeText(context, "Failed to read image data", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error reading image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Audio permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            isRecording = true
            recordingDurationSeconds = 0
            recordingFile = voiceRecorder.startRecording(conversationId)
        } else {
            Toast.makeText(context, "Microphone permission is required to record voice notes", Toast.LENGTH_SHORT).show()
        }
    }

    // Timer for voice note recording
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                delay(1000)
                recordingDurationSeconds++
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isRecording) {
                voiceRecorder.cancelRecording()
            }
        }
    }

    // Dialog to select preset location to share
    if (showLocationDialog) {
        LocationSelectorDialog(
            onDismiss = { showLocationDialog = false },
            onLocationSelected = { lat, lng, label ->
                viewModel.sendLocationMessage(lat, lng, label)
                showLocationDialog = false
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        // User Avatar
                        if (!state.otherUserAvatar.isNullOrBlank()) {
                            AsyncImage(
                                model = state.otherUserAvatar,
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                val firstLetter = state.otherUserName.firstOrNull()?.uppercase() ?: "?"
                                Text(
                                    text = firstLetter,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Name and typing status
                        Column {
                            Text(
                                text = state.otherUserName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            val subtitle = when {
                                state.isTyping -> "typing..."
                                state.isOtherUserOnline -> "online"
                                else -> "offline"
                            }
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (state.isTyping || state.isOtherUserOnline) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                },
                                fontWeight = if (state.isTyping || state.isOtherUserOnline) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Messages list (reversed so it starts at the bottom)
            val reversedMessages = state.messages.reversed()
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = listState,
                    reverseLayout = true,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    itemsIndexed(
                        items = reversedMessages,
                        key = { _, msg -> msg.id },
                    ) { index, message ->
                        val isSelf = message.senderId == state.currentUserId

                        // Because messages are reversed (newest first, index 0),
                        // the previous message chronologically is at index + 1
                        val prevMessage = if (index < reversedMessages.size - 1) reversedMessages[index + 1] else null

                        var showDateSeparator = false
                        if (prevMessage == null) {
                            showDateSeparator = true
                        } else {
                            try {
                                val currentInstant = Instant.parse(message.timestamp)
                                val prevInstant = Instant.parse(prevMessage.timestamp)
                                val currentDate = currentInstant.atZone(ZoneId.systemDefault()).toLocalDate()
                                val prevDate = prevInstant.atZone(ZoneId.systemDefault()).toLocalDate()

                                if (ChronoUnit.DAYS.between(prevDate, currentDate) > 0) {
                                    showDateSeparator = true
                                }
                            } catch (e: Exception) {
                                // Ignore parse errors
                            }
                        }

                        if (showDateSeparator) {
                            DateSeparator(dateString = message.timestamp)
                        }

                        MessageBubble(
                            message = message,
                            isCurrentUser = isSelf,
                            playerState = state.playerState,
                            onVoicePlayClick = viewModel::playVoice,
                            onLocationClick = { lat, lng, label ->
                                // Custom maps action or toast
                                val mapUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($label)")
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, mapUri)
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open map app", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onServiceCardClick = onNavigateToServiceDetail,
                        )
                    }
                }

                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.TopCenter).padding(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp,
                    )
                }
            }

            // Typing Indicator Overlay (Subtle line below messages)
            AnimatedVisibility(visible = state.isTyping) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${state.otherUserName} is typing",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "...",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // Attachment Expanded Menu
            AnimatedVisibility(visible = showAttachmentMenu) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(vertical = 12.dp, horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    AttachmentOption(
                        icon = Icons.Default.Image,
                        label = "Gallery",
                        onClick = {
                            imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            showAttachmentMenu = false
                        },
                    )
                    AttachmentOption(
                        icon = Icons.Default.LocationOn,
                        label = "Location",
                        onClick = {
                            showLocationDialog = true
                            showAttachmentMenu = false
                        },
                    )
                }
            }

            // Bottom input bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isRecording) {
                    // Recording voice note layout
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recording: ${formatSeconds(recordingDurationSeconds)}",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                            )
                        }

                        Row {
                            IconButton(
                                onClick = {
                                    voiceRecorder.cancelRecording()
                                    isRecording = false
                                },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = "Cancel",
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    val file = voiceRecorder.stopRecording()
                                    if (file != null && file.exists()) {
                                        viewModel.sendVoiceNote(file, recordingDurationSeconds)
                                    }
                                    isRecording = false
                                },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Send",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                } else {
                    // Standard message typing layout
                    IconButton(
                        onClick = { showAttachmentMenu = !showAttachmentMenu },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attach",
                        )
                    }

                    TextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Type a message...") },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        singleLine = true,
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    if (textInput.isBlank()) {
                        // Mic button
                        IconButton(
                            onClick = {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO,
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    isRecording = true
                                    recordingDurationSeconds = 0
                                    recordingFile = voiceRecorder.startRecording(conversationId)
                                } else {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Record Voice Note",
                            )
                        }
                    } else {
                        // Send text button
                        IconButton(
                            onClick = {
                                viewModel.sendTextMessage(textInput)
                                textInput = ""
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LocationSelectorDialog(
    onDismiss: () -> Unit,
    onLocationSelected: (Double, Double, String) -> Unit,
) {
    val locations = listOf(
        Triple(0.0515, 37.6456, "Hostel C"),
        Triple(0.0530, 37.6480, "Main Library"),
        Triple(0.0505, 37.6440, "Student Center"),
        Triple(0.0485, 37.6425, "Main Gate"),
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share Location") },
        text = {
            Column {
                Text(
                    text = "Choose a campus location to share:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                locations.forEach { (lat, lng, label) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onLocationSelected(lat, lng, label) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            IconButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.primary)
            }
        },
    )
}

private fun formatSeconds(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
