package must.kdroiders.hustlehub.ui.features.service.presentation.view

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import must.kdroiders.hustlehub.sharedComposables.HustleScaffold
import must.kdroiders.hustlehub.sharedComposables.HustleTextField
import must.kdroiders.hustlehub.sharedComposables.RatingBar
import must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel.WriteReviewViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WriteReviewScreen(
    serviceId: String,
    viewModel: WriteReviewViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(serviceId) {
        viewModel.initialize(serviceId)
    }

    LaunchedEffect(state.submitSuccess) {
        if (state.submitSuccess) {
            snackbarHostState.showSnackbar("Review submitted! Thank you.")
            onSubmitSuccess()
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    HustleScaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Text(
                        text = "Write a Review",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (state.isLoadingInfo) {
                CircularProgressIndicator()
            } else if (state.provider != null && state.service != null) {
                // Provider Info Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.size(56.dp)) {
                        AsyncImage(
                            model = state.provider?.profilePhotoUrl,
                            contentDescription = "Provider Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Crop,
                        )
                        // Online indicator (green dot)
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(must.kdroiders.hustlehub.ui.theme.HustleSuccess)
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = state.provider?.name ?: "Unknown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = state.service?.title ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = "Verified Student",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Star Rating
            RatingBar(
                rating = state.rating.toFloat(),
                starSize = 48.dp,
                onRatingChanged = viewModel::onRatingChanged,
            )

            Spacer(Modifier.height(16.dp))

            // Rating Feedback Text
            val feedbackText = when (state.rating) {
                5 -> "Outstanding!"
                4 -> "Great!"
                3 -> "Good"
                2 -> "Could be better"
                1 -> "Poor"
                else -> "Tap a star to rate"
            }
            Text(
                text = feedbackText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (state.rating > 0) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "You rated this service ${state.rating} stars",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(32.dp))

            // Review Input Area
            // Review Input Area
            val providerFirstName = state.provider
                ?.name
                ?.split(" ")
                ?.firstOrNull() ?: "the provider"

            HustleTextField(
                value = state.comment,
                onValueChange = viewModel::onCommentChanged,
                placeholder = "Share your experience with $providerFirstName...\nWas the delivery on time? How was the quality?",
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                minLines = 5,
                maxLines = 5,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Min 10 chars",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End),
            )

            Spacer(Modifier.height(24.dp))

            // Predefined Tags
            val availableTags = listOf("Fast Delivery", "Great Communication", "Creative")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                availableTags.forEach { tag ->
                    val isSelected = state.selectedTags.contains(tag)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                            ).border(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(20.dp),
                            ).clickable { viewModel.onTagToggled(tag) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Checkbox(
                    checked = state.isAnonymous,
                    onCheckedChange = viewModel::onAnonymousToggled,
                )
                Text(
                    text = "Post anonymously",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(32.dp))

            // Submit Button
            Button(
                onClick = viewModel::submit,
                enabled = state.canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "Submit Review",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}
