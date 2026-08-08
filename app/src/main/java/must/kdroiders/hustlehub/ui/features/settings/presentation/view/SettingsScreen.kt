package must.kdroiders.hustlehub.ui.features.settings.presentation.view

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import must.kdroiders.hustlehub.datastore.AppTheme
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleButtonVariant
import must.kdroiders.hustlehub.sharedComposables.HustleScaffold
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.LogOutButton
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.ProfileIdentityCard
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsDivider
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsGroup
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsRowNavigate
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsSectionLabel
import must.kdroiders.hustlehub.ui.features.settings.presentation.viewmodel.SettingsEvent
import must.kdroiders.hustlehub.ui.features.settings.presentation.viewmodel.SettingsViewModel
import must.kdroiders.hustlehub.ui.theme.HustleActiveGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToNotificationPreferences: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToBlockedUsers: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by settingsViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Consume navigation events
    LaunchedEffect(Unit) {
        settingsViewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.LoggedOut, is SettingsEvent.AccountDeleted -> onBack()
                is SettingsEvent.NavigateToChangePassword -> onNavigateToChangePassword()
                is SettingsEvent.NavigateToNotifications -> onNavigateToNotificationPreferences()
                is SettingsEvent.NavigateToEditProfile -> onNavigateToEditProfile()
                is SettingsEvent.NavigateToPrivacy -> onNavigateToPrivacy()
                is SettingsEvent.NavigateToBlockedUsers -> onNavigateToBlockedUsers()
                else -> {
                    Toast.makeText(context, "Feature coming soon", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Theme selection dialog
    if (state.showThemeDialog) {
        AlertDialog(
            onDismissRequest = settingsViewModel::onThemeDismissed,
            title = { Text("Choose Theme") },
            text = {
                Column {
                    AppTheme.entries.forEach { theme ->
                        val label = when (theme) {
                            AppTheme.SYSTEM -> "System Default"
                            AppTheme.LIGHT -> "Light"
                            AppTheme.DARK -> "Dark"
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(role = Role.RadioButton) {
                                    settingsViewModel.onThemeSelected(theme)
                                }.padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = state.selectedTheme == theme,
                                onClick = { settingsViewModel.onThemeSelected(theme) },
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = settingsViewModel::onThemeDismissed) {
                    Text("Cancel")
                }
            },
        )
    }

    // Delete Account confirmation dialog
    if (state.showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = settingsViewModel::onDeleteAccountDismissed,
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to delete your account? This action is permanent and cannot be undone.") },
            confirmButton = {
                TextButton(onClick = settingsViewModel::onDeleteAccountConfirmed) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = settingsViewModel::onDeleteAccountDismissed) {
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
                        text = "Settings",
                        modifier = Modifier.semantics { heading() },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
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

            // Profile identity card
            ProfileIdentityCard(
                displayName = state.displayName,
                username = state.username,
                avatarUrl = state.avatarUrl,
                onEditClick = settingsViewModel::onEditProfileClicked,
            )

            Spacer(Modifier.height(28.dp))

            // ACCOUNT section
            SettingsSectionLabel("ACCOUNT")
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                SettingsRowNavigate(
                    icon = Icons.Default.Person,
                    label = "Edit Profile",
                    onClick = settingsViewModel::onEditProfileClicked,
                )
                SettingsDivider()
                SettingsRowNavigate(
                    icon = Icons.Default.Lock,
                    label = "Change Password",
                    onClick = settingsViewModel::onChangePasswordClicked,
                )
                SettingsDivider()
                SettingsRowNavigate(
                    icon = Icons.Default.VerifiedUser,
                    label = "Verify Student ID",
                    subtitle = if (state.isVerified) "Verified Student" else "Not Verified",
                    subtitleColor = if (state.isVerified) HustleActiveGreen else MaterialTheme.colorScheme.error,
                    onClick = settingsViewModel::onVerificationClicked,
                )
                SettingsDivider()
                SettingsRowNavigate(
                    icon = Icons.Default.Star,
                    label = "Subscription & Billing",
                    subtitle = "Manage HustleHub Pro",
                    onClick = onNavigateToSubscription,
                )
            }

            Spacer(Modifier.height(24.dp))

            // NOTIFICATIONS section
            SettingsSectionLabel("NOTIFICATIONS")
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                SettingsRowNavigate(
                    icon = Icons.Default.Notifications,
                    label = "Notification Preferences",
                    onClick = settingsViewModel::onNotificationsClicked,
                )
            }

            Spacer(Modifier.height(24.dp))

            // PRIVACY section
            SettingsSectionLabel("PRIVACY")
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                SettingsRowNavigate(
                    icon = Icons.Default.PrivacyTip,
                    label = "Privacy Settings",
                    onClick = settingsViewModel::onPrivacyClicked,
                )
                SettingsDivider()
                SettingsRowNavigate(
                    icon = Icons.Default.Block,
                    label = "Blocked Users",
                    onClick = settingsViewModel::onBlockedUsersClicked,
                )
            }

            Spacer(Modifier.height(24.dp))

            // APPEARANCE section
            SettingsSectionLabel("APPEARANCE")
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                SettingsRowNavigate(
                    icon = Icons.Default.DarkMode,
                    label = "Theme",
                    trailing = when (state.selectedTheme) {
                        AppTheme.SYSTEM -> "System Default"
                        AppTheme.LIGHT -> "Light"
                        AppTheme.DARK -> "Dark"
                    },
                    onClick = settingsViewModel::onThemeClicked,
                )
                SettingsDivider()
                SettingsRowNavigate(
                    icon = Icons.Default.Language,
                    label = "Language",
                    trailing = state.selectedLanguage,
                    onClick = settingsViewModel::onLanguageClicked,
                )
            }

            Spacer(Modifier.height(24.dp))

            // SUPPORT section
            SettingsSectionLabel("SUPPORT")
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                SettingsRowNavigate(
                    icon = Icons.AutoMirrored.Filled.Help,
                    label = "Help & FAQ",
                    onClick = settingsViewModel::onHelpCenterClicked,
                )
                SettingsDivider()
                SettingsRowNavigate(
                    icon = Icons.AutoMirrored.Filled.ContactSupport,
                    label = "Contact Us",
                    onClick = settingsViewModel::onContactUsClicked,
                )
                SettingsDivider()
                SettingsRowNavigate(
                    icon = Icons.Default.BugReport,
                    label = "Report a Bug",
                    onClick = settingsViewModel::onReportProblemClicked,
                )
            }

            Spacer(Modifier.height(24.dp))

            // LEGAL section
            SettingsSectionLabel("LEGAL")
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                SettingsRowNavigate(
                    icon = Icons.Default.Gavel,
                    label = "Terms of Service",
                    onClick = settingsViewModel::onTermsOfServiceClicked,
                )
                SettingsDivider()
                SettingsRowNavigate(
                    icon = Icons.Default.Policy,
                    label = "Privacy Policy",
                    onClick = settingsViewModel::onPrivacyPolicyClicked,
                )
            }

            Spacer(Modifier.height(24.dp))

            // ABOUT section
            SettingsSectionLabel("ABOUT")
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                SettingsRowNavigate(
                    icon = Icons.Default.Info,
                    label = "App Version",
                    trailing = "v${state.appVersion}",
                    onClick = {},
                )
                SettingsDivider()
                SettingsRowNavigate(
                    icon = Icons.Default.Code,
                    label = "Open Source Licenses",
                    onClick = settingsViewModel::onLicensesClicked,
                )
            }

            Spacer(Modifier.height(32.dp))

            // Log Out button (destructive red)
            LogOutButton(
                isLoading = state.isLoggingOut,
                onClick = settingsViewModel::onLogOutClicked,
            )

            Spacer(Modifier.height(12.dp))

            // Delete Account button (destructive outlined HustleButton)
            HustleButton(
                text = "Delete Account",
                onClick = settingsViewModel::onDeleteAccountClicked,
                modifier = Modifier.fillMaxWidth(),
                variant = HustleButtonVariant.Outlined,
                loading = state.isDeletingAccount,
                enabled = !state.isDeletingAccount,
            )

            Spacer(Modifier.height(24.dp))

            // App version footer
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "v${state.appVersion} · HustleHub",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 11.sp,
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
