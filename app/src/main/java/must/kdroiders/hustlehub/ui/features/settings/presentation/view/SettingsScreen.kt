package must.kdroiders.hustlehub.ui.features.settings.presentation.view

import android.app.Activity
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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.remember
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
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.core.review.findActivity
import must.kdroiders.hustlehub.datastore.AppTheme
import must.kdroiders.hustlehub.sharedComposables.HustleBackButton
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleButtonVariant
import must.kdroiders.hustlehub.sharedComposables.HustleScaffold
import must.kdroiders.hustlehub.sharedComposables.HustleTextField
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.LogOutButton
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.ProfileIdentityCard
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsDivider
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsGroup
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsRowNavigate
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsSectionLabel
import must.kdroiders.hustlehub.ui.features.settings.presentation.viewmodel.DeleteAccountStep
import must.kdroiders.hustlehub.ui.features.settings.presentation.viewmodel.SettingsEvent
import must.kdroiders.hustlehub.ui.features.settings.presentation.viewmodel.SettingsViewModel

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
    onNavigateToCreateService: () -> Unit = {},
    onAccountDeleted: () -> Unit = onBack,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by settingsViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

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
                is SettingsEvent.NavigateToSubscription -> onNavigateToSubscription()
                is SettingsEvent.NavigateToHelp,
                is SettingsEvent.NavigateToTerms,
                is SettingsEvent.NavigateToPrivacyPolicy,
                is SettingsEvent.NavigateToContactUs,
                is SettingsEvent.NavigateToReport,
                -> onNavigateToHelp()
                is SettingsEvent.TriggerRateApp -> {
                    (context as? Activity ?: context.findActivity())?.let { activity ->
                        settingsViewModel.launchReviewFlow(activity)
                    }
                }
            }
        }
    }

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
                                }
                                .padding(vertical = 12.dp),
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

    if (state.showProviderEducationDialog) {
        AlertDialog(
            onDismissRequest = settingsViewModel::onDismissProviderEducationDialog,
            title = { Text(stringResource(R.string.settings_sub_education_title)) },
            text = {
                Text(
                    text = stringResource(R.string.settings_sub_education_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        settingsViewModel.onDismissProviderEducationDialog()
                        onNavigateToCreateService()
                    },
                ) {
                    Text(stringResource(R.string.settings_sub_education_create_service))
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = settingsViewModel::onDismissProviderEducationDialog) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    Spacer(Modifier.width(4.dp))
                    TextButton(onClick = settingsViewModel::onContinueToSubscriptionFromDialog) {
                        Text(stringResource(R.string.settings_sub_education_continue))
                    }
                }
            },
        )
    }

    when (state.deleteAccountStep) {
        DeleteAccountStep.WARNING -> {
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
                            fontWeight = FontWeight.Bold,
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

        DeleteAccountStep.PASSWORD_INPUT -> {
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
                        HustleTextField(
                            value = state.deletePasswordInput,
                            onValueChange = settingsViewModel::onDeletePasswordChanged,
                            label = stringResource(R.string.label_password),
                            singleLine = true,
                            isPassword = true,
                            isError = state.deletePasswordError != null,
                            errorText = state.deletePasswordError,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = settingsViewModel::onDeleteAccountConfirmed,
                        colors = ButtonDefaults.buttonColors(
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

        DeleteAccountStep.NONE -> {}
    }

    if (state.isDeletingAccount) {
        Dialog(onDismissRequest = {}) {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularWavyProgressIndicator(
                        modifier = Modifier.size(44.dp),
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.settings_deleting_account_progress),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
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

            ProfileIdentityCard(
                displayName = state.displayName,
                username = state.username,
                avatarUrl = state.avatarUrl,
            )

            Spacer(Modifier.height(28.dp))

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
                    icon = Icons.Default.Star,
                    label = stringResource(R.string.settings_subscription_billing),
                    subtitle = stringResource(R.string.settings_subscription_subtitle),
                    onClick = settingsViewModel::onSubscriptionClicked,
                )
            }

            Spacer(Modifier.height(24.dp))

            SettingsSectionLabel(stringResource(R.string.settings_section_preferences))
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
                    icon = Icons.Default.Notifications,
                    label = stringResource(R.string.settings_notification_preferences),
                    onClick = settingsViewModel::onNotificationsClicked,
                )
            }

            Spacer(Modifier.height(24.dp))

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

            SettingsSectionLabel(stringResource(R.string.settings_section_support_legal))
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                SettingsRowNavigate(
                    icon = Icons.AutoMirrored.Filled.Help,
                    label = stringResource(R.string.settings_help_faq),
                    subtitle = stringResource(R.string.settings_help_faq_subtitle),
                    onClick = settingsViewModel::onHelpCenterClicked,
                )
                SettingsDivider()
                SettingsRowNavigate(
                    icon = Icons.Default.Star,
                    label = stringResource(R.string.settings_rate_app),
                    subtitle = stringResource(R.string.settings_rate_app_subtitle),
                    onClick = settingsViewModel::onRateAppClicked,
                )
            }

            Spacer(Modifier.height(32.dp))

            LogOutButton(
                isLoading = state.isLoggingOut,
                onClick = settingsViewModel::onLogOutClicked,
            )

            Spacer(Modifier.height(12.dp))

            HustleButton(
                text = stringResource(R.string.settings_delete_account_title),
                onClick = settingsViewModel::onDeleteAccountClicked,
                modifier = Modifier.fillMaxWidth(),
                variant = HustleButtonVariant.Outlined,
                loading = state.isDeletingAccount,
                enabled = !state.isDeletingAccount,
            )

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.settings_version_footer_format, state.appVersion),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
