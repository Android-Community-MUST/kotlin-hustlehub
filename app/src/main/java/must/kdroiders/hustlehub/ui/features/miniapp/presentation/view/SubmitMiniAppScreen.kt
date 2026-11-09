package must.kdroiders.hustlehub.ui.features.miniapp.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import must.kdroiders.hustlehub.sharedComposables.HustleBackButton
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleScaffold
import must.kdroiders.hustlehub.sharedComposables.HustleTextField
import must.kdroiders.hustlehub.ui.features.miniapp.presentation.viewmodel.SubmitMiniAppViewModel

private val CATEGORIES = listOf(
    "SERVICES",
    "FOOD",
    "TECH",
    "FASHION",
    "DESIGN",
    "ENTERTAINMENT",
    "OTHER",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitMiniAppScreen(
    onBack: () -> Unit,
    onSubmitSuccess: () -> Unit,
    viewModel: SubmitMiniAppViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.error) {
        state.error?.let { err ->
            snackbarHostState.showSnackbar(err)
            viewModel.clearError()
        }
    }

    if (state.isSuccess) {
        AlertDialog(
            onDismissRequest = onSubmitSuccess,
            title = {
                Text(
                    text = "Application Submitted!",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = "Your mini-app has been submitted to the HustleHub admin team. Once reviewed and approved, it will appear dynamically on the campus feed.",
                )
            },
            confirmButton = {
                TextButton(onClick = onSubmitSuccess) {
                    Text("Done")
                }
            },
        )
    }

    HustleScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "List Your App",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    HustleBackButton(onClick = onBack)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.AddBusiness,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "HustleHub Mini-App Platform",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Deploy your website and let thousands of students access it instantly with seamless native M-Pesa payments.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            HustleTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = "Mini-App Name *",
                placeholder = "e.g. MUST Canteen Online",
                modifier = Modifier.fillMaxWidth(),
            )

            HustleTextField(
                value = state.appId,
                onValueChange = viewModel::onAppIdChange,
                label = "Unique App ID *",
                placeholder = "e.g. must-canteen",
                modifier = Modifier.fillMaxWidth(),
            )

            HustleTextField(
                value = state.entryUrl,
                onValueChange = viewModel::onEntryUrlChange,
                label = "HTTPS Web URL *",
                placeholder = "https://your-website.com",
                modifier = Modifier.fillMaxWidth(),
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Category *",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(12.dp),
                            ).clickable { categoryDropdownExpanded = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = state.category,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false },
                    ) {
                        CATEGORIES.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    viewModel.onCategoryChange(category)
                                    categoryDropdownExpanded = false
                                },
                            )
                        }
                    }
                }
            }

            HustleTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                label = "Description *",
                placeholder = "Briefly describe your services, food menu, or products...",
                singleLine = false,
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
            )

            HustleTextField(
                value = state.iconUrl,
                onValueChange = viewModel::onIconUrlChange,
                label = "Icon URL (Optional)",
                placeholder = "https://example.com/icon.png",
                modifier = Modifier.fillMaxWidth(),
            )

            HustleTextField(
                value = state.coverPhotoUrl,
                onValueChange = viewModel::onCoverPhotoUrlChange,
                label = "Cover Photo URL (Optional)",
                placeholder = "https://example.com/banner.jpg",
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            HustleButton(
                text = "Submit Application",
                onClick = viewModel::submit,
                loading = state.isLoading,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
