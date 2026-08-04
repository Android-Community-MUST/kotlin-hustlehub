package must.kdroiders.hustlehub.ui.features.settings.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.LogOutButton
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.ProfileIdentityCard
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsDivider
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsGroup
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsRowExternalLink
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsRowNavigate
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsRowToggle
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsSectionLabel
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsTopBar
import must.kdroiders.hustlehub.ui.features.settings.presentation.viewmodel.SettingsEvent
import must.kdroiders.hustlehub.ui.features.settings.presentation.viewmodel.SettingsViewModel
import must.kdroiders.hustlehub.ui.theme.HustleActiveGreen
import must.kdroiders.hustlehub.ui.theme.HustleHubTheme

/**
 * Settings screen — full-screen destination pushed over MainShell.
 *
 * NavKey: [Settings]
 * Entry point: gear icon in ProfileHeader → onSettingsClick callback.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current

    // Consume one-shot navigation events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.LoggedOut -> onBack() // parent nav graph pops to Login
                is SettingsEvent.NavigateToChangePassword -> onNavigateToChangePassword()
                else -> {
                    android.widget.Toast
                        .makeText(context, "Coming soon", android.widget.Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        // Top bar
        SettingsTopBar(onBack = onBack)

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            // Profile identity card
            ProfileIdentityCard(
                displayName = state.displayName,
                username = state.username,
                avatarUrl = state.avatarUrl,
                onEditClick = viewModel::onEditProfileClicked,
            )

            Spacer(Modifier.height(28.dp))

            // ACCOUNT section
            SettingsSectionLabel("ACCOUNT")
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                SettingsRowNavigate(
                    icon = Icons.Default.Shield,
                    label = "Verification Status",
                    subtitle = if (state.isVerified) "Verified Student" else "Not Verified",
                    subtitleColor = if (state.isVerified) HustleActiveGreen else MaterialTheme.colorScheme.error,
                    onClick = viewModel::onVerificationClicked,
                )
                SettingsDivider()
                SettingsRowNavigate(
                    icon = Icons.Default.CreditCard,
                    label = "Payment Methods",
                    trailing = state.paymentMethod,
                    onClick = viewModel::onPaymentMethodsClicked,
                )
            }

            Spacer(Modifier.height(24.dp))

            // PREFERENCES section
            SettingsSectionLabel("PREFERENCES")
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                SettingsRowNavigate(
                    icon = Icons.Default.Notifications,
                    label = "Notifications",
                    onClick = viewModel::onNotificationsClicked,
                )
                SettingsDivider()
                SettingsRowNavigate(
                    icon = Icons.Default.Lock,
                    label = "Privacy & Security",
                    onClick = viewModel::onPrivacyClicked,
                )
                SettingsDivider()
                SettingsRowNavigate(
                    icon = Icons.Default.Lock,
                    label = "Change Password",
                    onClick = viewModel::onChangePasswordClicked,
                )
                SettingsDivider()
                SettingsRowToggle(
                    icon = Icons.Default.DarkMode,
                    label = "Dark Mode",
                    checked = state.isDarkMode,
                    onCheckedChange = viewModel::onDarkModeToggled,
                )
            }

            Spacer(Modifier.height(24.dp))

            // SUPPORT section
            SettingsSectionLabel("SUPPORT")
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                SettingsRowExternalLink(
                    icon = Icons.AutoMirrored.Filled.Help,
                    label = "Help Center",
                    onClick = viewModel::onHelpCenterClicked,
                )
                SettingsDivider()
                SettingsRowNavigate(
                    icon = Icons.Default.ReportProblem,
                    label = "Report a Problem",
                    onClick = viewModel::onReportProblemClicked,
                )
            }

            Spacer(Modifier.height(32.dp))

            // Log Out button
            LogOutButton(
                isLoading = state.isLoggingOut,
                onClick = viewModel::onLogOutClicked,
            )

            Spacer(Modifier.height(16.dp))

            // Delete Account link
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Delete Account",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(role = Role.Button) { viewModel.onDeleteAccountClicked() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }

            Spacer(Modifier.height(16.dp))

            // App version footer
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "HustleHub v${state.appVersion} (Build ${state.buildNumber})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 11.sp,
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SettingsScreenPreview() {
    HustleHubTheme(darkTheme = false) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding(),
        ) {
            SettingsTopBar(onBack = {})
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(Modifier.height(16.dp))
                ProfileIdentityCard(
                    displayName = "Juma Kamau",
                    username = "@UoN_Hustler",
                    avatarUrl = "",
                    onEditClick = {},
                )
                Spacer(Modifier.height(28.dp))
                SettingsSectionLabel("ACCOUNT")
                Spacer(Modifier.height(8.dp))
                SettingsGroup {
                    SettingsRowNavigate(
                        icon = Icons.Default.Shield,
                        label = "Verification Status",
                        subtitle = "Verified Student",
                        subtitleColor = HustleActiveGreen,
                        onClick = {},
                    )
                    SettingsDivider()
                    SettingsRowNavigate(
                        icon = Icons.Default.CreditCard,
                        label = "Payment Methods",
                        trailing = "M-Pesa",
                        onClick = {},
                    )
                }
                Spacer(Modifier.height(24.dp))
                SettingsSectionLabel("PREFERENCES")
                Spacer(Modifier.height(8.dp))
                SettingsGroup {
                    SettingsRowNavigate(
                        icon = Icons.Default.Notifications,
                        label = "Notifications",
                        onClick = {},
                    )
                    SettingsDivider()
                    SettingsRowNavigate(
                        icon = Icons.Default.Lock,
                        label = "Privacy & Security",
                        onClick = {},
                    )
                    SettingsDivider()
                    SettingsRowToggle(
                        icon = Icons.Default.DarkMode,
                        label = "Dark Mode",
                        checked = true,
                        onCheckedChange = {},
                    )
                }
                Spacer(Modifier.height(24.dp))
                SettingsSectionLabel("SUPPORT")
                Spacer(Modifier.height(8.dp))
                SettingsGroup {
                    SettingsRowExternalLink(
                        icon = Icons.AutoMirrored.Filled.Help,
                        label = "Help Center",
                        onClick = {},
                    )
                    SettingsDivider()
                    SettingsRowNavigate(
                        icon = Icons.Default.ReportProblem,
                        label = "Report a Problem",
                        onClick = {},
                    )
                }
                Spacer(Modifier.height(32.dp))
                LogOutButton(isLoading = false, onClick = {})
                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        "Delete Account",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.Underline,
                    )
                }
                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        "HustleHub v2.4.0 (Build 2045)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 11.sp,
                    )
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
