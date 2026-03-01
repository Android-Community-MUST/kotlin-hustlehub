package must.kdroiders.hustlehub.ui.features.profilesetup.presentation.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.data.model.UserRole
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleTextField
import must.kdroiders.hustlehub.ui.features.profilesetup.presentation.viewModel.ProfileSetupEvent
import must.kdroiders.hustlehub.ui.features.profilesetup.presentation.viewModel.ProfileSetupViewModel

/**
 * List of courses for the dropdown.
 * Add/remove as needed for your university.
 */
private val courseOptions = listOf(
    "Computer Science",
    "Information Technology",
    "Software Engineering",
    "Business Administration",
    "Education",
    "Agriculture",
    "Nursing",
    "Engineering",
    "Economics",
    "Other"
)

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
@Composable
fun ProfileSetupScreen(
    viewModel: ProfileSetupViewModel = hiltViewModel(),
    onSetupComplete: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary

    // ---- Observe one-shot events ----
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ProfileSetupEvent.ProfileSaved ->
                    onSetupComplete()
            }
        }
    }

    // ---- Photo picker launchers ----
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onPhotoSelected(it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { _ ->
        // TakePicturePreview returns a thumbnail Bitmap.
        // For production, use TakePicture with a
        // FileProvider URI for full-res photos.
        // For now we use gallery which gives a URI.
    }

    // ---- Bottom sheet state ----
    var showPhotoSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // ---- Course dropdown state ----
    var courseExpanded by remember { mutableStateOf(false) }

    // ---- Main layout ----
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ---- Header ----
            Text(
                text = "Set Up Your",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Profile",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography
                    .headlineMedium.copy(
                        brush = Brush.linearGradient(
                            listOf(primary, tertiary)
                        )
                    )
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Tell us a bit about yourself",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme
                    .onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            // ---- Profile Photo ----
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme
                            .surfaceVariant
                    )
                    .border(
                        width = 3.dp,
                        brush = Brush.linearGradient(
                            listOf(primary, tertiary)
                        ),
                        shape = CircleShape
                    )
                    .clickable { showPhotoSheet = true },
                contentAlignment = Alignment.Center
            ) {
                if (state.photoUri != null) {
                    AsyncImage(
                        model = state.photoUri,
                        contentDescription = "Profile photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Add photo",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme
                            .onSurfaceVariant
                    )
                }

                // Upload loading overlay
                if (state.isUploadingPhoto) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                MaterialTheme.colorScheme
                                    .scrim.copy(alpha = 0.5f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme
                                .colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                    }
                }
            }

            Text(
                text = "Tap to add photo",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme
                    .onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(Modifier.height(28.dp))

            // ---- Name ----
            HustleTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = "Full Name",
                leadingIcon = Icons.Default.Person,
                placeholder = "Enter your name",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // ---- Course Dropdown ----
            ExposedDropdownMenuBox(
                expanded = courseExpanded,
                onExpandedChange = {
                    courseExpanded = it
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                HustleTextField(
                    value = state.course,
                    onValueChange = {},
                    label = "Course",
                    leadingIcon = Icons.Default.School,
                    placeholder = "Select your course",
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            MenuAnchorType
                                .PrimaryNotEditable
                        )
                )

                ExposedDropdownMenu(
                    expanded = courseExpanded,
                    onDismissRequest = {
                        courseExpanded = false
                    }
                ) {
                    courseOptions.forEach { course ->
                        DropdownMenuItem(
                            text = { Text(course) },
                            onClick = {
                                viewModel
                                    .onCourseChange(course)
                                courseExpanded = false
                            },
                            contentPadding =
                                ExposedDropdownMenuDefaults
                                    .ItemContentPadding
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Year of Study ----
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Year of Study",
                    style = MaterialTheme.typography
                        .bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme
                        .onSurfaceVariant,
                    modifier = Modifier.padding(
                        start = 4.dp,
                        bottom = 8.dp
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    (1..5).forEach { year ->
                        val isSelected =
                            state.yearOfStudy == year

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.onYearChange(year)
                            },
                            label = {
                                Text(
                                    "Y$year",
                                    fontWeight =
                                        if (isSelected)
                                            FontWeight.Bold
                                        else
                                            FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults
                                .filterChipColors(
                                    selectedContainerColor =
                                        primary,
                                    selectedLabelColor =
                                        MaterialTheme
                                            .colorScheme
                                            .onPrimary
                                ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Hostel / Residence ----
            HustleTextField(
                value = state.hostel,
                onValueChange = viewModel::onHostelChange,
                label = "Hostel / Residence",
                leadingIcon = Icons.Default.Home,
                placeholder = "e.g. Hostel A, Off-campus",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            // ---- Role Selection ----
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "I want to...",
                    style = MaterialTheme.typography
                        .bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme
                        .onSurfaceVariant,
                    modifier = Modifier.padding(
                        start = 4.dp,
                        bottom = 8.dp
                    )
                )

                FlowRow(
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RoleChip(
                        label = "Find Services",
                        description = "Customer",
                        isSelected =
                            state.role == UserRole.CUSTOMER,
                        onClick = {
                            viewModel.onRoleChange(
                                UserRole.CUSTOMER
                            )
                        }
                    )
                    RoleChip(
                        label = "Offer Services",
                        description = "Provider",
                        isSelected =
                            state.role == UserRole.PROVIDER,
                        onClick = {
                            viewModel.onRoleChange(
                                UserRole.PROVIDER
                            )
                        }
                    )
                    RoleChip(
                        label = "Both",
                        description = "Provide & Find",
                        isSelected =
                            state.role == UserRole.BOTH,
                        onClick = {
                            viewModel.onRoleChange(
                                UserRole.BOTH
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ---- Error Message ----
            AnimatedVisibility(
                visible = state.errorMessage != null
            ) {
                Text(
                    text = state.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography
                        .bodySmall,
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                )
            }

            // ---- Save Button ----
            HustleButton(
                text = if (state.isSaving) "Saving..."
                else "Complete Setup",
                onClick = { viewModel.saveProfile() },
                loading = state.isSaving,
                enabled = !state.isSaving
                    && !state.isUploadingPhoto,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // ---- Photo Picker Bottom Sheet ----
    if (showPhotoSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showPhotoSheet = false
            },
            sheetState = sheetState,
            containerColor = MaterialTheme
                .colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Choose Photo",
                    style = MaterialTheme.typography
                        .titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(20.dp))

                // Gallery option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            galleryLauncher
                                .launch("image/*")
                            scope.launch {
                                sheetState.hide()
                                showPhotoSheet = false
                            }
                        }
                        .padding(16.dp),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector =
                            Icons.Default.PhotoLibrary,
                        contentDescription = "Gallery",
                        tint = primary
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "Gallery",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Pick from your photos",
                            fontSize = 12.sp,
                            color = MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Camera option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            cameraLauncher.launch(null)
                            scope.launch {
                                sheetState.hide()
                                showPhotoSheet = false
                            }
                        }
                        .padding(16.dp),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector =
                            Icons.Default.CameraAlt,
                        contentDescription = "Camera",
                        tint = primary
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "Camera",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Take a new photo",
                            fontSize = 12.sp,
                            color = MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Reusable chip for role selection.
 */
@Composable
private fun RoleChip(
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Column(
                modifier = Modifier
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = label,
                    fontWeight = if (isSelected)
                        FontWeight.Bold
                    else FontWeight.Normal,
                    fontSize = 13.sp
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = if (isSelected)
                        MaterialTheme.colorScheme
                            .onPrimary
                            .copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme
                        .onSurfaceVariant
                )
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor =
                MaterialTheme.colorScheme.primary,
            selectedLabelColor =
                MaterialTheme.colorScheme.onPrimary
        ),
        shape = RoundedCornerShape(12.dp)
    )
}
