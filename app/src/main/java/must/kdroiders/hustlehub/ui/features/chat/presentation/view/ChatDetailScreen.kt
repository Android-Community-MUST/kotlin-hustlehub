package must.kdroiders.hustlehub.ui.features.chat.presentation.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.core.notification.ActiveConversationTracker
import must.kdroiders.hustlehub.core.utils.ImageCompressor
import must.kdroiders.hustlehub.core.utils.createTempCameraFile
import must.kdroiders.hustlehub.core.utils.saveImageToGallery
import must.kdroiders.hustlehub.sharedComposables.HustleScaffold
import must.kdroiders.hustlehub.ui.features.chat.domain.model.Message
import must.kdroiders.hustlehub.ui.features.chat.domain.model.MessageType
import must.kdroiders.hustlehub.ui.features.chat.presentation.audio.VoiceRecorder
import must.kdroiders.hustlehub.ui.features.chat.presentation.components.ChatLocationPickerSheet
import must.kdroiders.hustlehub.ui.features.chat.presentation.components.DateSeparator
import must.kdroiders.hustlehub.ui.features.chat.presentation.components.MessageBubble
import must.kdroiders.hustlehub.ui.features.chat.presentation.components.ServiceCompletionCard
import must.kdroiders.hustlehub.ui.features.chat.presentation.viewmodel.ChatDetailViewModel
import must.kdroiders.hustlehub.ui.features.report.presentation.ReportDialog
import must.kdroiders.hustlehub.ui.features.service.presentation.view.components.FullScreenImageViewer
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChatDetailScreen(
    conversationId: String,
    onBackClick: () -> Unit,
    onNavigateToServiceDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    serviceId: String? = null,
    serviceTitle: String? = null,
    serviceCategory: String? = null,
    servicePriceRange: String? = null,
    providerName: String? = null,
    onNavigateToWriteReview: ((serviceId: String, providerId: String) -> Unit)? = null,
    chatDetailViewModel: ChatDetailViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by chatDetailViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    var textInput by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var recordingDurationSeconds by remember { mutableIntStateOf(0) }
    var recordingFile by remember { mutableStateOf<File?>(null) }
    var showLocationPickerSheet by remember { mutableStateOf(false) }
    var showAttachmentMenu by remember { mutableStateOf(false) }
    // Fullscreen image viewer
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    // Save-to-gallery bottom sheet
    var imageToSave by remember { mutableStateOf<String?>(null) }
    var messageToReport by remember { mutableStateOf<Message?>(null) }

    val voiceRecorder = remember { VoiceRecorder(context) }

    DisposableEffect(conversationId) {
        ActiveConversationTracker.activeConversationId = conversationId
        onDispose {
            if (ActiveConversationTracker.activeConversationId == conversationId) {
                ActiveConversationTracker.activeConversationId = null
            }
        }
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var currentUserLocation by remember { mutableStateOf<Location?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            showLocationPickerSheet = true
        } else {
            Toast.makeText(context, "Location permission denied. Cannot share location.", Toast.LENGTH_SHORT).show()
        }
    }

    val requestLocationAndShare: () -> Unit = {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation || hasCoarseLocation) {
            showLocationPickerSheet = true
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    LaunchedEffect(Unit) {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation || hasCoarseLocation) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        currentUserLocation = location
                    }
                }
            } catch (e: SecurityException) {
                // Ignore
            }
        }
    }

    // Initialize the conversation when screen loads or ID changes
    LaunchedEffect(conversationId) {
        chatDetailViewModel.initialize(
            conversationId = conversationId,
            serviceId = serviceId,
            serviceTitle = serviceTitle,
            serviceCategory = serviceCategory,
            servicePriceRange = servicePriceRange,
            providerName = providerName,
        )
    }

    // Show errors in snackbar
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            chatDetailViewModel.clearError()
        }
    }

    // Scroll to bottom when list size changes or typing changes
    LaunchedEffect(state.messages.size, state.isTyping) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    // Typing indicator: delegate to ViewModel which owns debounce + auto-clear logic
    // (no LaunchedEffect needed — the screen just forwards raw onChange events)

    val compressAndSendImage: (Uri) -> Unit = { uri ->
        scope.launch {
            try {
                val bytes = ImageCompressor.compressImage(context, uri)
                if (bytes != null) {
                    chatDetailViewModel.sendImageMessage(bytes)
                } else {
                    Toast.makeText(context, "Failed to compress image", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Gallery image picker — compresses before upload
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        uri?.let(compressAndSendImage)
    }

    // Camera capture state — holds the temp file URI until capture completes
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            cameraImageUri?.let(compressAndSendImage)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            val tempFile = createTempCameraFile(context)
            if (tempFile != null) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    tempFile,
                )
                cameraImageUri = uri
                cameraLauncher.launch(uri)
            }
        } else {
            Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT).show()
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

    // Location picker sheet (WhatsApp style)
    if (showLocationPickerSheet) {
        ChatLocationPickerSheet(
            onDismiss = { showLocationPickerSheet = false },
            onLocationSelected = { lat, lng, label ->
                chatDetailViewModel.sendLocationMessage(lat, lng, label)
                showLocationPickerSheet = false
            },
        )
    }

    // Fullscreen image viewer — opens when user taps a chat image bubble
    selectedImageUrl?.let { url ->
        FullScreenImageViewer(
            imageUrls = listOf(url),
            initialIndex = 0,
            onDismiss = { selectedImageUrl = null },
        )
    }

    // Save-to-gallery bottom sheet — opens on long-press of a chat image bubble
    imageToSave?.let { url ->
        ModalBottomSheet(
            onDismissRequest = { imageToSave = null },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp),
            ) {
                Text(
                    text = "Image Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            imageToSave = null
                            scope.launch { saveImageToGallery(context, url) }
                        }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Save to Gallery",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Save to Gallery", fontWeight = FontWeight.Medium)
                        Text(
                            text = "Download this image to your photos",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    HustleScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { heading() },
                    ) {
                        // User Avatar
                        if (!state.otherUserAvatar.isNullOrBlank()) {
                            AsyncImage(
                                model = state.otherUserAvatar,
                                contentDescription = "Avatar of ${state.otherUserName}",
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
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clearAndSetSemantics { },
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

                        // Name and typing/presence status line
                        Column {
                            Text(
                                text = state.otherUserName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            val lastSeenAt = state.otherUserLastSeenAt
                            val subtitle = when {
                                state.isTyping -> "typing..."
                                state.isOtherUserOnline -> "online"
                                lastSeenAt != null ->
                                    "Last seen ${formatLastSeen(lastSeenAt)}"
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
                actions = {
                    if (state.isCurrentUserProvider && !state.isServiceCompleted) {
                        IconButton(onClick = { chatDetailViewModel.markServiceCompleted() }) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Mark as Complete",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    var showMenu by remember { mutableStateOf(false) }
                    var showBlockDialog by remember { mutableStateOf(false) }
                    var showReportDialog by remember { mutableStateOf(false) }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Report User") },
                                onClick = {
                                    showMenu = false
                                    showReportDialog = true
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Block User", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    showBlockDialog = true
                                },
                            )
                        }
                    }

                    if (showReportDialog) {
                        must.kdroiders.hustlehub.ui.features.report.presentation.ReportDialog(
                            targetId = state.otherUserId,
                            targetType = "user",
                            onDismiss = { showReportDialog = false },
                        )
                    }

                    if (showBlockDialog) {
                        AlertDialog(
                            onDismissRequest = { showBlockDialog = false },
                            title = { Text("Block ${state.otherUserName.ifBlank { "User" }}?") },
                            text = { Text("They will no longer be able to message you, view your profile, or see your map pins.") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showBlockDialog = false
                                        chatDetailViewModel.blockUser {
                                            Toast.makeText(context, "User blocked", Toast.LENGTH_SHORT).show()
                                            onBackClick()
                                        }
                                    },
                                ) {
                                    Text("Block", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showBlockDialog = false }) {
                                    Text("Cancel")
                                }
                            },
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
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
        ) {
            // Mark as complete banner for provider when service not yet marked complete
            if (state.isCurrentUserProvider && !state.isServiceCompleted) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                        .clickable(role = Role.Button) { chatDetailViewModel.markServiceCompleted() }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null, // decorative
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Service finished? Mark as complete",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                    Text(
                        text = "Mark Complete",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // Service completion review prompt card for customer
            val activeServiceId = serviceId
            val activeProviderId = state.otherUserId
            if (!state.isCurrentUserProvider &&
                state.isServiceCompleted &&
                !state.hasReviewedService &&
                !activeServiceId.isNullOrBlank() &&
                onNavigateToWriteReview != null
            ) {
                ServiceCompletionCard(
                    providerName = state.otherUserName,
                    serviceTitle = serviceTitle,
                    onWriteReviewClick = { onNavigateToWriteReview(activeServiceId, activeProviderId) },
                    modifier = Modifier.padding(16.dp),
                )
            }

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
                        val isSelf = message.senderId != state.otherUserId || message.id.startsWith("temp_")

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
                            currentUserLocation = currentUserLocation,
                            onVoicePlayClick = chatDetailViewModel::playVoice,
                            onVoiceSpeedToggle = chatDetailViewModel::toggleVoicePlaybackSpeed,
                            onLocationClick = { lat, lng, label ->
                                val mapUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($label)")
                                val intent = Intent(Intent.ACTION_VIEW, mapUri)
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open map app", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onServiceCardClick = onNavigateToServiceDetail,
                            onImageClick = { url -> selectedImageUrl = url },
                            onImageLongClick = { url -> imageToSave = url },
                            onReply = chatDetailViewModel::startReplying,
                            onDeleteForMe = { msg -> chatDetailViewModel.deleteMessageForMe(msg.id) },
                            onDeleteForEveryone = { msg -> chatDetailViewModel.deleteMessageForEveryone(msg.id) },
                            onReportMessage = { msg -> messageToReport = msg },
                            isOtherUserOnline = state.isOtherUserOnline,
                        )
                    }
                }

                if (state.isLoading) {
                    CircularWavyProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    )
                }
            }

            val chatMotionScheme = MaterialTheme.motionScheme
            AnimatedVisibility(
                visible = state.isTyping,
                enter = expandVertically(chatMotionScheme.fastSpatialSpec()) + fadeIn(chatMotionScheme.fastEffectsSpec()),
                exit = shrinkVertically(chatMotionScheme.fastSpatialSpec()) + fadeOut(chatMotionScheme.fastEffectsSpec()),
            ) {
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
                    TypingIndicatorDots(color = MaterialTheme.colorScheme.primary)
                }
            }

            AnimatedVisibility(
                visible = showAttachmentMenu,
                enter = expandVertically(MaterialTheme.motionScheme.fastSpatialSpec()) + fadeIn(MaterialTheme.motionScheme.fastEffectsSpec()),
                exit = shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec()) + fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()),
            ) {
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
                        icon = Icons.Filled.CameraAlt,
                        label = "Camera",
                        onClick = {
                            showAttachmentMenu = false
                            val hasPerm = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA,
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPerm) {
                                val tempFile = createTempCameraFile(context)
                                if (tempFile != null) {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        tempFile,
                                    )
                                    cameraImageUri = uri
                                    cameraLauncher.launch(uri)
                                }
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                    )
                    AttachmentOption(
                        icon = Icons.Default.LocationOn,
                        label = "Location",
                        onClick = {
                            showAttachmentMenu = false
                            requestLocationAndShare()
                        },
                    )
                }
            }

            AnimatedVisibility(
                visible = state.isCurrentUserProvider,
                enter = expandVertically(MaterialTheme.motionScheme.fastSpatialSpec()) + fadeIn(MaterialTheme.motionScheme.fastEffectsSpec()),
                exit = shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec()) + fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()),
            ) {
                val quickReplies = listOf(
                    "Hi! I'm available",
                    "Can we schedule a time?",
                    "What style do you prefer?",
                    "I'm currently busy, back later",
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    quickReplies.forEach { reply ->
                        SuggestionChip(
                            onClick = {
                                chatDetailViewModel.sendTextMessage(reply)
                            },
                            label = {
                                Text(
                                    text = reply,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            ),
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = state.replyingToMessage != null,
                enter = slideInVertically(MaterialTheme.motionScheme.fastSpatialSpec()) { -it } + fadeIn(MaterialTheme.motionScheme.fastEffectsSpec()),
                exit = slideOutVertically(MaterialTheme.motionScheme.fastSpatialSpec()) { -it } + fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()),
            ) {
                val replyingTo = state.replyingToMessage
                if (replyingTo != null) {
                    val replyText = when (replyingTo.type) {
                        MessageType.VOICE -> "[Voice note]"
                        MessageType.IMAGE -> "[Image]"
                        MessageType.LOCATION -> "[Location]"
                        MessageType.SERVICE_CARD -> "[Service Card]"
                        else -> replyingTo.content
                    }
                    val senderName = if (replyingTo.senderId == state.currentUserId) {
                        "You"
                    } else {
                        state.otherUserName
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Left vertical accent line
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(36.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.primary),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = "Replying to $senderName",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = replyText ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 12.sp,
                            )
                        }
                        IconButton(
                            onClick = chatDetailViewModel::cancelReplying,
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel reply",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
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
                                        chatDetailViewModel.sendVoiceNote(file, recordingDurationSeconds)
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
                        onValueChange = { newText ->
                            textInput = newText
                            chatDetailViewModel.onTypingChanged(newText)
                        },
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
                        val sendInteractionSource = remember { MutableInteractionSource() }
                        val isSendPressed by sendInteractionSource.collectIsPressedAsState()
                        val sendScale by animateFloatAsState(
                            targetValue = if (isSendPressed) 0.8f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "send_scale",
                        )

                        IconButton(
                            onClick = {
                                chatDetailViewModel.sendTextMessage(textInput)
                                textInput = ""
                            },
                            interactionSource = sendInteractionSource,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            modifier = Modifier
                                .size(40.dp)
                                .scale(sendScale),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                            )
                        }
                    }
                }
            }

            messageToReport?.let { message ->
                ReportDialog(
                    targetId = message.id,
                    targetType = "message",
                    onDismiss = { messageToReport = null },
                )
            }
        }
    }
}

@Composable
private fun AttachmentOption(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(role = Role.Button, onClick = onClick),
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
                contentDescription = null, // decorative since label announces it
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

private fun formatSeconds(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
}

/**
 * Converts an ISO-8601 last-seen timestamp into a human-readable relative string.
 * Examples: "just now", "5 min ago", "2 hours ago", "3 days ago".
 */
private fun formatLastSeen(isoString: String): String {
    return try {
        val then = Instant.parse(isoString)
        val duration = Duration.between(then, Instant.now())
        when {
            duration.toMinutes() < 1L -> "just now"
            duration.toMinutes() < 60L -> "${duration.toMinutes()} min ago"
            duration.toHours() < 24L -> "${duration.toHours()} hour${if (duration.toHours() > 1L) "s" else ""} ago"
            else -> "${duration.toDays()} day${if (duration.toDays() > 1L) "s" else ""} ago"
        }
    } catch (e: Exception) {
        "a while ago"
    }
}

private const val TYPING_DOT_CYCLE_MS = 600
private const val TYPING_DOT_STAGGER_MS = 200

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TypingIndicatorDots(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val infiniteTransition = androidx.compose.animation.core
        .rememberInfiniteTransition(label = "typing_dots")
    val dotCount = 3

    val dotAnimations = (0 until dotCount).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                animation = tween(durationMillis = TYPING_DOT_CYCLE_MS, delayMillis = index * TYPING_DOT_STAGGER_MS),
                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse,
            ),
            label = "dot_alpha_$index",
        )
    }

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        dotAnimations.forEach { anim ->
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.3f + (0.7f * anim.value)))
                    .offset(y = (-4).dp * anim.value),
            )
        }
    }
}
