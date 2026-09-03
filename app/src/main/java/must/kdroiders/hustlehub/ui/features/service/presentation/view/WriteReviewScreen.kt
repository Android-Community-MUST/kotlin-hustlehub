package must.kdroiders.hustlehub.ui.features.service.presentation.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.sharedComposables.HustleBackButton
import must.kdroiders.hustlehub.sharedComposables.HustleScaffold
import must.kdroiders.hustlehub.sharedComposables.HustleTextField
import must.kdroiders.hustlehub.sharedComposables.LoadingIndicator
import must.kdroiders.hustlehub.sharedComposables.StarRatingBar
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
    val reviewSubmittedMsg = stringResource(R.string.review_submitted_snackbar)

    LaunchedEffect(serviceId) {
        viewModel.initialize(serviceId)
    }

    LaunchedEffect(state.submitSuccess) {
        if (state.submitSuccess) {
            snackbarHostState.showSnackbar(reviewSubmittedMsg)
            delay(1200)
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
                        text = stringResource(R.string.review_write_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { heading() },
                    )
                },
                navigationIcon = {
                    HustleBackButton(onClick = onBack)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                                contentDescription = stringResource(R.string.cd_provider_avatar),
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
                                text = state.provider?.name ?: stringResource(R.string.label_unknown),
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
                                    text = stringResource(R.string.settings_verified_student),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }

                if (state.hasAlreadyReviewed) {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.review_already_submitted),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Interactive Star Rating (Tap / Drag)
                StarRatingBar(
                    rating = state.rating,
                    starSize = 44.dp,
                    onRatingChanged = viewModel::onRatingChanged,
                )

                Spacer(Modifier.height(16.dp))

                // Rating Feedback Text
                val feedbackText = when (state.rating) {
                    5 -> stringResource(R.string.review_rate_5)
                    4 -> stringResource(R.string.review_rate_4)
                    3 -> stringResource(R.string.review_rate_3)
                    2 -> stringResource(R.string.review_rate_2)
                    1 -> stringResource(R.string.review_rate_1)
                    else -> stringResource(R.string.review_rate_hint)
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
                        text = stringResource(
                            if (state.rating > 1) R.string.review_rated_stars_plural else R.string.review_rated_stars_singular,
                            state.rating,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(Modifier.height(32.dp))

                // Review Input Area
                val defaultProviderName = stringResource(R.string.profile_fallback_name)
                val providerFirstName = state.provider
                    ?.name
                    ?.split(" ")
                    ?.firstOrNull() ?: defaultProviderName

                HustleTextField(
                    value = state.comment,
                    onValueChange = viewModel::onCommentChanged,
                    placeholder = stringResource(R.string.review_comment_placeholder, providerFirstName),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    minLines = 5,
                    maxLines = 5,
                )

                Spacer(Modifier.height(8.dp))

                // Live Character Count Indicator
                Text(
                    text = stringResource(R.string.review_char_count_format, state.commentLength, state.maxCommentLength),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (state.commentLength >=
                        state.maxCommentLength
                    ) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
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
                        text = stringResource(R.string.review_post_anonymously),
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
                        LoadingIndicator(
                            modifier = Modifier.size(24.dp),
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = if (state.hasAlreadyReviewed) {
                                    stringResource(R.string.review_already_reviewed_btn)
                                } else {
                                    stringResource(R.string.review_submit_btn)
                                },
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

            // Animated Submission Success Overlay
            AnimatedVisibility(
                visible = state.submitSuccess,
                enter = fadeIn() + scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                modifier = Modifier.align(Alignment.Center),
            ) {
                val scale by animateFloatAsState(
                    targetValue = if (state.submitSuccess) 1f else 0.5f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "successScale",
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.scale(scale),
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = stringResource(R.string.cd_success),
                            tint = must.kdroiders.hustlehub.ui.theme.HustleSuccess,
                            modifier = Modifier.size(96.dp),
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.review_success_title),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.review_success_subtitle),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
