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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.datastore.AppTheme
import must.kdroiders.hustlehub.sharedComposables.HustleBackButton
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
    onNavigateToHelp: () -> Unit = {},
    onAccountDeleted: () -> Unit = onBack,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by settingsViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val featureComingSoonToast = stringResource(R.string.toast_feature_coming_soon)
    var showLicensesDialog by remember { mutableStateOf(false) }
    var showVersionDialog by remember { mutableStateOf(false) }

    // Consume navigation events
    LaunchedEffect(Unit) {
        settingsViewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.LoggedOut -> onBack()
                is SettingsEvent.AccountDeleted -> onAccountDeleted()
                is SettingsEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Long,
                    )
                }
                is SettingsEvent.NavigateToChangePassword -> onNavigateToChangePassword()
                is SettingsEvent.NavigateToNotifications -> onNavigateToNotificationPreferences()
                is SettingsEvent.NavigateToEditProfile -> onNavigateToEditProfile()
                is SettingsEvent.NavigateToPrivacy -> onNavigateToPrivacy()
                is SettingsEvent.NavigateToBlockedUsers -> onNavigateToBlockedUsers()
                is SettingsEvent.NavigateToHelp,
                is SettingsEvent.NavigateToTerms,
                is SettingsEvent.NavigateToPrivacyPolicy,
                is SettingsEvent.NavigateToContactUs,
                is SettingsEvent.NavigateToReport,
                -> onNavigateToHelp()
                is SettingsEvent.NavigateToLicenses -> showLicensesDialog = true
                else -> {
                    Toast.makeText(context, featureComingSoonToast, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Theme selection dialog
    if (state.showThemeDialog) {
        AlertDialog(
            onDismissRequest = settingsViewModel::onThemeDismissed,
            title = { Text(stringResource(R.string.settings_choose_theme)) },
            text = {
                Column {
                    AppTheme.entries.forEach { theme ->
                        val label = when (theme) {
                            AppTheme.SYSTEM -> stringResource(R.string.settings_theme_system)
                            AppTheme.LIGHT -> stringResource(R.string.settings_theme_light)
                            AppTheme.DARK -> stringResource(R.string.settings_theme_dark)
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
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    // Delete Account 2-Step Confirmation Dialog
    when (state.deleteAccountStep) {
        must.kdroiders.hustlehub.ui.features.settings.presentation.viewmodel.DeleteAccountStep.WARNING -> {
            AlertDialog(
                onDismissRequest = settingsViewModel::onDeleteAccountDismissed,
                title = { Text(stringResource(R.string.settings_delete_account_title)) },
                text = {
                    Text(
                        stringResource(R.string.settings_delete_account_body),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                confirmButton = {
                    TextButton(onClick = settingsViewModel::onDeleteWarningConfirmed) {
                        Text(
                            stringResource(R.string.action_continue),
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = settingsViewModel::onDeleteAccountDismissed) {
                        Text(stringResource(R.string.action_cancel))
                    }
                },
            )
        }

        must.kdroiders.hustlehub.ui.features.settings.presentation.viewmodel.DeleteAccountStep.PASSWORD_INPUT -> {
            var passwordVisible by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = settingsViewModel::onDeleteAccountDismissed,
                title = { Text(stringResource(R.string.settings_delete_confirm_password_title)) },
                text = {
                    Column {
                        Text(
                            stringResource(R.string.settings_delete_confirm_password_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(12.dp))
                        androidx.compose.material3.OutlinedTextField(
                            value = state.deletePasswordInput,
                            onValueChange = settingsViewModel::onDeletePasswordChanged,
                            label = { Text(stringResource(R.string.label_password)) },
                            singleLine = true,
                            isError = state.deletePasswordError != null,
                            visualTransformation = if (passwordVisible) {
                                androidx.compose.ui.text.input.VisualTransformation.None
                            } else {
                                androidx.compose.ui.text.input
                                    .PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) {
                                            Icons.Default.Visibility
                                        } else {
                                            Icons.Default.VisibilityOff
                                        },
                                        contentDescription = if (passwordVisible) {
                                            stringResource(R.string.cd_hide_password)
                                        } else {
                                            stringResource(R.string.cd_show_password)
                                        },
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (state.deletePasswordError != null) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = state.deletePasswordError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                },
                confirmButton = {
                    androidx.compose.material3.Button(
                        onClick = settingsViewModel::onDeleteAccountConfirmed,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text(stringResource(R.string.settings_delete_my_account_btn))
                    }
                },
                dismissButton = {
                    TextButton(onClick = settingsViewModel::onDeleteAccountDismissed) {
                        Text(stringResource(R.string.action_cancel))
                    }
                },
            )
        }

        must.kdroiders.hustlehub.ui.features.settings.presentation.viewmodel.DeleteAccountStep.NONE -> {}
    }

    // Loading overlay during deletion
    if (state.isDeletingAccount) {
        androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = androidx.compose.foundation.shape
                            .RoundedCornerShape(16.dp),
                    ).padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    androidx.compose.material3.CircularWavyProgressIndicator(
                        modifier = Modifier.size(44.dp),
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.settings_deleting_account_progress),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    )
                }
            }
        }
    }

    // Open Source Licenses dialog
    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            title = { Text(stringResource(R.string.settings_licenses_dialog_title)) },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.settings_licenses_dialog_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    val libraries = listOf(
                        "AndroidX & Jetpack Compose" to "Apache License 2.0",
                        "Kotlin & Coroutines" to "Apache License 2.0",
                        "Hilt / Dagger" to "Apache License 2.0",
                        "Firebase Android SDK" to "Apache License 2.0",
                        "Room Database" to "Apache License 2.0",
                        "Retrofit & OkHttp" to "Apache License 2.0",
                        "Coil" to "Apache License 2.0",
                        "Timber" to "Apache License 2.0",
                    )
                    libraries.forEach { (lib, license) ->
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Text(
                                text = lib,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = license,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLicensesDialog = false }) {
                    Text(stringResource(R.string.action_close))
                }
            },
        )
    }

    // App Version Info dialog
    if (showVersionDialog) {
        AlertDialog(
            onDismissRequest = { showVersionDialog = false },
            title = { Text(stringResource(R.string.settings_about_dialog_title)) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.settings_about_dialog_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.settings_about_version_format, state.appVersion),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.settings_about_build_format, state.buildNumber),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.settings_about_status),
                        style = MaterialTheme.typography.bodySmall,
                        color = HustleActiveGreen,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = stringResource(R.string.settings_about_copyright),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showVersionDialog = false }) {
                    Text(stringResource(R.string.action_ok))
                }
            },
        )
    }

    HustleScaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        modifier = Modifier.semantics { heading() },
                    )
                },
                navigationIcon = {
                    HustleBackButton(
                        onClick = onBack,
                        contentDescription = stringResource(R.string.cd_navigate_back),
                    )
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
            )

            Spacer(Modifier.height(28.dp))

            // ACCOUNT section
            SettingsSectionLabel(stringResource(R.string.settings_section_account))
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                SettingsRowNavigate(
                    icon = Icons.Default.Person,
                    label = stringResource(R.string.settings_edit_profile),
                    onClick = settingsViewModel::onEditProfileClicked,
                )
                SettingsDivider()
                SettingsRowNavigate(
                    icon = Icons.Default.Lock,
                    label = stringResource(R.string.settings_change_password),
                    onClick = settingsViewModel::onChangePasswordClicked,
                )
                SettingsDivider()
                SettingsRowNavigate(
                    icon = Icons.Default.VerifiedUser,
                    label = stringResource(R.string.settings_verify_student_id),
                    subtitle = if (state.isVerified) stringResource(R.string.settings_verified_student) else stringResource(R.string.settings_not_verified),
                    subtitleColor = if (state.isVerified) HustleActiveGreen else MaterialTheme.colorScheme.error,
                    onClick = settingsViewModel::onVerificationClicked,
                )
                SettingsDivider()
                SettingsRowNavigate(
                    icon = Icons.Default.Star,
                    label = stringResource(R.string.settings_subscription_billing),
                    subtitle = stringResource(R.string.settings_subscription_subtitle),
                    onClick = onNavigateToSubscription,
                )
            }

            Spacer(Modifier.height(24.dp))

            // NOTIFICATIONS section
            SettingsSectionLabel(stringResource(R.string.settings_section_notifications))
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                SettingsRowNavigate(
                    icon = Icons.Default.Notifications,
                    label = stringResource(R.string.settings_notification_preferences),
                    onClick = settingsViewModel::onNotificationsClicked,
                )
            }

            Spacer(Modifier.height(24.dp))

            // PRIVACY section
            SettingsSectionLabel(stringResource(R.string.settings_section_privacy))
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                SettingsRowNavigate(
                    icon = Icons.Default.PrivacyTip,
                    label = stringResource(R.string.settings_privacy_settings),
                    onClick = settingsViewModel::onPrivacyClicked,
                )
                SettingsDivider()
                SettingsRowNavigate(
                    icon = Icons.Default.Block,
                    label = stringResource(R.string.settings_blocked_users),
                    onClick = settingsViewModel::onBlockedUsersClicked,
                )
            }

            Spacer(Modifier.height(24.dp))

            // APPEARANCE section
            SettingsSectionLabel(stringResource(R.string.settings_section_appearance))
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                SettingsRowNavigate(
                    icon = Icons.Default.DarkMode,
                    label = stringResource(R.string.settings_theme),
                    trailing = when (state.selectedTheme) {
                        AppTheme.SYSTEM -> stringResource(R.string.settings_theme_system)
                        AppTheme.LIGHT -> stringResource(R.string.settings_theme_light)
                        AppTheme.DARK -> stringResource(R.string.settings_theme_dark)
                    },
                    onClick = settingsViewModel::onThemeClicked,
                )
                SettingsDivider()
                SettingsRowNavigate(
                    icon = Icons.Default.Language,
                    label = stringResource(R.string.settings_language),
                    trailing = state.selectedLanguage,
                    onClick = settingsViewModel::onLanguageClicked,
                )
            }

            Spacer(Modifier.height(24.dp))

            // SUPPORT & LEGAL section
            SettingsSectionLabel(stringResource(R.string.settings_section_support_legal))
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                SettingsRowNavigate(
                    icon = Icons.AutoMirrored.Filled.Help,
                    label = stringResource(R.string.settings_help_faq),
                    subtitle = stringResource(R.string.settings_help_faq_subtitle),
                    onClick = settingsViewModel::onHelpCenterClicked,
                )
            }

            Spacer(Modifier.height(24.dp))

            // ABOUT section
            SettingsSectionLabel(stringResource(R.string.settings_section_about))
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                SettingsRowNavigate(
                    icon = Icons.Default.Info,
                    label = stringResource(R.string.settings_app_version),
                    trailing = stringResource(R.string.version_prefix_format, state.appVersion),
                    onClick = { showVersionDialog = true },
                )
                SettingsDivider()
                SettingsRowNavigate(
                    icon = Icons.Default.Code,
                    label = stringResource(R.string.settings_open_source_licenses),
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
                text = stringResource(R.string.settings_delete_account_title),
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
                    text = stringResource(R.string.settings_version_footer_format, state.appVersion),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 11.sp,
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
