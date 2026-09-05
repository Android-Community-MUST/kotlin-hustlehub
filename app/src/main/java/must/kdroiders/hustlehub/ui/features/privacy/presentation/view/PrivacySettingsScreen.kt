package must.kdroiders.hustlehub.ui.features.privacy.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import must.kdroiders.hustlehub.sharedComposables.HustleBackButton
import must.kdroiders.hustlehub.sharedComposables.HustleScaffold
import must.kdroiders.hustlehub.ui.features.privacy.data.remote.dto.MessagingPermission
import must.kdroiders.hustlehub.ui.features.privacy.presentation.viewmodel.PrivacySettingsViewModel
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsDivider
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsGroup
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsRowNavigate
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsRowToggle
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsSectionLabel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PrivacySettingsScreen(
    onBack: () -> Unit = {},
    viewModel: PrivacySettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    // Messaging permission selection dialog
    if (state.showMessagingDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onMessagingDialogDismissed,
            title = { Text("Who can message me") },
            text = {
                Column {
                    MessagingPermission.entries.forEach { permission ->
                        val label = when (permission) {
                            MessagingPermission.EVERYONE -> "Everyone"
                            MessagingPermission.VERIFIED_ONLY -> "Verified users only"
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(role = Role.RadioButton) {
                                    viewModel.onMessagingPermissionSelected(permission)
                                }.padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = state.messagingPermission == permission,
                                onClick = { viewModel.onMessagingPermissionSelected(permission) },
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = viewModel::onMessagingDialogDismissed) {
                    Text("Cancel")
                }
            },
        )
    }

    HustleScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Privacy Settings",
                        modifier = Modifier.semantics { heading() },
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
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularWavyProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(Modifier.height(16.dp))

                // LOCATION section
                SettingsSectionLabel("LOCATION SHARING")
                Spacer(Modifier.height(8.dp))
                SettingsGroup {
                    SettingsRowToggle(
                        icon = Icons.Default.LocationOn,
                        label = "Show my location on map",
                        checked = state.showLocationOnMap,
                        onCheckedChange = viewModel::onLocationSharingToggled,
                    )
                }

                Spacer(Modifier.height(24.dp))

                // CONTACT PREFERENCES section
                SettingsSectionLabel("CONTACT & MESSAGING")
                Spacer(Modifier.height(8.dp))
                SettingsGroup {
                    SettingsRowNavigate(
                        icon = Icons.Default.Message,
                        label = "Who can message me",
                        trailing = when (state.messagingPermission) {
                            MessagingPermission.EVERYONE -> "Everyone"
                            MessagingPermission.VERIFIED_ONLY -> "Verified users only"
                        },
                        onClick = viewModel::onMessagingClicked,
                    )
                }

                Spacer(Modifier.height(24.dp))

                // PROFILE VISIBILITY section
                SettingsSectionLabel("PROFILE VISIBILITY")
                Spacer(Modifier.height(8.dp))
                SettingsGroup {
                    SettingsRowToggle(
                        icon = Icons.Default.Visibility,
                        label = "Show my online status",
                        checked = state.showOnlineStatus,
                        onCheckedChange = viewModel::onOnlineStatusToggled,
                    )
                    SettingsDivider()
                    SettingsRowToggle(
                        icon = Icons.Default.Schedule,
                        label = "Show last seen",
                        checked = state.showLastSeen,
                        onCheckedChange = viewModel::onLastSeenToggled,
                    )
                    SettingsDivider()
                    SettingsRowToggle(
                        icon = Icons.Default.RateReview,
                        label = "Allow reviews on my profile",
                        checked = state.allowReviews,
                        onCheckedChange = viewModel::onAllowReviewsToggled,
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
