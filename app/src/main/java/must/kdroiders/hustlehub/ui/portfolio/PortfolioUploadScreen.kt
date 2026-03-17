package must.kdroiders.hustlehub.ui.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import must.kdroiders.hustlehub.data.repository.UploadResult
import must.kdroiders.hustlehub.ui.components.PortfolioImagePicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioUploadScreen(
    viewModel: PortfolioUploadViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val vertigoTag = "vertiGO-0628"


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Portfolio Upload") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PortfolioImagePicker(
                        selectedImages = state.selectedUris,
                        onImagesSelected = { viewModel.onImagesSelected(it) },
                        onImageRemoved = { viewModel.removeImage(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.uploadPortfolio(context, "test-service-id") },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.selectedUris.isNotEmpty() && !state.isUploading,
                shape = RoundedCornerShape(8.dp)
            ) {
                if (state.isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Upload Portfolio")
                }
            }

            if (state.uploadResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Upload Progress",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                state.uploadResults.forEach { (uri, result) ->
                    UploadStatusItem(uri.lastPathSegment ?: "Image", result)
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
fun UploadStatusItem(fileName: String, result: UploadResult) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1
            )
            
            when (result) {
                is UploadResult.Progress -> {
                    LinearProgressIndicator(
                        progress = { result.percent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .height(4.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
                else -> Unit
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        StatusIndicator(result)
    }
}

@Composable
fun StatusIndicator(result: UploadResult) {
    when (result) {
        is UploadResult.Idle -> Text("Pending", style = MaterialTheme.typography.bodySmall)
        is UploadResult.Progress -> Text("${(result.percent * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
        is UploadResult.Success -> Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success",
            tint = Color(0xFF4CAF50)
        )
        is UploadResult.Error -> Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error
        )
    }
}
