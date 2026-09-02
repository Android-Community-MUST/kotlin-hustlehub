package must.kdroiders.hustlehub.ui.features.notification.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.HourglassFull
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import must.kdroiders.hustlehub.sharedComposables.HustleBackButton
import must.kdroiders.hustlehub.sharedComposables.HustleScaffold
import must.kdroiders.hustlehub.ui.features.notification.presentation.viewmodel.NotificationPreferencesViewModel
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsDivider
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsGroup
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsRowToggle
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsSectionLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPreferencesScreen(
    onBack: () -> Unit = {},
    notificationPreferencesViewModel: NotificationPreferencesViewModel = hiltViewModel(),
) {
    val state by notificationPreferencesViewModel.uiState.collectAsState()
    val prefs = state.preferences

    HustleScaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Settings", modifier = Modifier.semantics { heading() }) },
                navigationIcon = {
                    HustleBackButton(onClick = onBack)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            // Notification types
            SettingsSectionLabel("NOTIFICATION TYPES")
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                SettingsRowToggle(
                    icon = Icons.AutoMirrored.Filled.Chat,
                    label = "New Messages",
                    checked = prefs.newMessages,
                    onCheckedChange = notificationPreferencesViewModel::onNewMessagesToggled,
                )
                SettingsDivider()
                SettingsRowToggle(
                    icon = Icons.Default.RateReview,
                    label = "New Reviews",
                    checked = prefs.newReviews,
                    onCheckedChange = notificationPreferencesViewModel::onNewReviewsToggled,
                )
                SettingsDivider()
                SettingsRowToggle(
                    icon = Icons.Default.Work,
                    label = "Service Inquiries",
                    checked = prefs.serviceInquiries,
                    onCheckedChange = notificationPreferencesViewModel::onServiceInquiriesToggled,
                )
                SettingsDivider()
                SettingsRowToggle(
                    icon = Icons.Default.Campaign,
                    label = "Marketing & Promotions",
                    checked = prefs.marketing,
                    onCheckedChange = notificationPreferencesViewModel::onMarketingToggled,
                )
            }

            Spacer(Modifier.height(24.dp))

            // Quiet hours
            SettingsSectionLabel("QUIET HOURS")
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                HourPickerDropdownRow(
                    label = "Quiet Hours Start",
                    icon = Icons.Default.HourglassFull,
                    selectedHour = prefs.quietHoursStart,
                    onHourSelected = notificationPreferencesViewModel::onQuietHoursStartChanged,
                )
                SettingsDivider()
                HourPickerDropdownRow(
                    label = "Quiet Hours End",
                    icon = Icons.Default.HourglassFull,
                    selectedHour = prefs.quietHoursEnd,
                    onHourSelected = notificationPreferencesViewModel::onQuietHoursEndChanged,
                )
            }

            Spacer(Modifier.height(24.dp))

            // Preferences
            SettingsSectionLabel("PREFERENCES")
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                SettingsRowToggle(
                    icon = Icons.Default.Vibration,
                    label = "Vibration",
                    checked = prefs.vibrationEnabled,
                    onCheckedChange = notificationPreferencesViewModel::onVibrationToggled,
                )
                SettingsDivider()
                SettingsRowToggle(
                    icon = Icons.Default.MusicNote,
                    label = "Notification Sound",
                    checked = prefs.soundEnabled,
                    onCheckedChange = notificationPreferencesViewModel::onSoundToggled,
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HourPickerDropdownRow(
    label: String,
    icon: ImageVector,
    selectedHour: Int,
    onHourSelected: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.size(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                value = formatHour(selectedHour),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .padding(0.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                (0..23).forEach { hour ->
                    DropdownMenuItem(
                        text = { Text(formatHour(hour)) },
                        onClick = {
                            onHourSelected(hour)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

private fun formatHour(hour: Int): String {
    val period = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "%d:00 %s".format(displayHour, period)
}
