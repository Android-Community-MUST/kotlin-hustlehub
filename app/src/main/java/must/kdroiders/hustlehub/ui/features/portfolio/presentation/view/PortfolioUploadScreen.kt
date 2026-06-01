package must.kdroiders.hustlehub.ui.features.portfolio.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.data.repository.UploadResult
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleButtonVariant
import must.kdroiders.hustlehub.sharedComposables.HustleCard
import must.kdroiders.hustlehub.sharedComposables.HustleCardVariant
import must.kdroiders.hustlehub.sharedComposables.LoadingIndicator
import must.kdroiders.hustlehub.sharedComposables.PortfolioImagePicker
import must.kdroiders.hustlehub.ui.features.portfolio.presentation.viewmodel.PortfolioUploadViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioUploadScreen(
    serviceId: String = "",
    viewModel: PortfolioUploadViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.portfolio_upload_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_navigate_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                // Image picker grid
                HustleCard(
                    modifier = Modifier.fillMaxWidth(),
                    variant = HustleCardVariant.Elevated,
                ) {
                    PortfolioImagePicker(
                        selectedImages = state.selectedUris,
                        onImagesSelected = viewModel::onImagesSelected,
                        onImageRemoved = viewModel::removeImage,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Upload button
                HustleButton(
                    text = if (state.isUploading) stringResource(R.string.portfolio_uploading)
                    else stringResource(R.string.portfolio_upload_button),
                    onClick = { viewModel.uploadPortfolio(context, serviceId) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.selectedUris.isNotEmpty(),
                    loading = state.isUploading,
                )

                // Per-image results
                if (state.uploadResults.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = stringResource(R.string.portfolio_upload_progress_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    state.uploadResults.forEach { (uri, result) ->
                        UploadStatusRow(
                            fileName = uri.lastPathSegment
                                ?: stringResource(R.string.portfolio_unknown_file),
                            result = result,
                            onRetry = { viewModel.retryImage(context, uri, serviceId) },
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }

                    if (state.uploadedUrls.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(
                                R.string.portfolio_upload_success_count,
                                state.uploadedUrls.size,
                                state.selectedUris.size,
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
            }
        }
    }
}

// Sub-composables

@Composable
private fun UploadStatusRow(
    fileName: String,
    result: UploadResult,
    onRetry: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
            )
            // Replaced LinearProgressIndicator with the reusable LoadingIndicator as requested
            if (result is UploadResult.Progress) {
                Spacer(modifier = Modifier.height(8.dp))
                LoadingIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    message = null
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))
        UploadStatusIndicator(result = result, onRetry = onRetry)
    }
}

@Composable
private fun UploadStatusIndicator(result: UploadResult, onRetry: () -> Unit) {
    when (result) {
        is UploadResult.Idle -> Text(
            text = stringResource(R.string.portfolio_status_pending),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        is UploadResult.Progress -> Text(
            text = stringResource(R.string.portfolio_status_percent, (result.percent * 100).toInt()),
            style = MaterialTheme.typography.bodySmall,
        )
        is UploadResult.Success -> Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = stringResource(R.string.portfolio_status_success_cd),
            tint = MaterialTheme.colorScheme.tertiary,
        )
        is UploadResult.Error -> Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = stringResource(R.string.portfolio_status_error_cd),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            HustleButton(
                text = stringResource(R.string.portfolio_retry),
                onClick = onRetry,
                variant = HustleButtonVariant.Outlined,
                icon = Icons.Default.Refresh,
                modifier = Modifier.width(100.dp)
            )
        }
    }
}
