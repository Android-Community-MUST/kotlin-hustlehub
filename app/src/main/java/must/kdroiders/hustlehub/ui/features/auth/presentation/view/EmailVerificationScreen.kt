package must.kdroiders.hustlehub.ui.features.auth.presentation.view

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.coroutines.delay
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.core.ui.TestTags
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleButtonVariant
import must.kdroiders.hustlehub.sharedComposables.HustleCard
import must.kdroiders.hustlehub.sharedComposables.HustleCardVariant
import must.kdroiders.hustlehub.ui.features.auth.presentation.viewmodel.EmailVerificationViewModel

@Composable
fun EmailVerificationScreen(
    email: String,
    onVerified: () -> Unit,
    onNavigateToLogin: () -> Unit = {},
    emailVerificationViewModel: EmailVerificationViewModel = hiltViewModel(),
) {
    val uiState by emailVerificationViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(email) {
        emailVerificationViewModel.setEmail(email)
    }

    // Auto-check on resume and auto-polling — updates isVerified state automatically
    LifecycleResumeEffect(Unit) {
        emailVerificationViewModel.checkVerificationStatus()
        emailVerificationViewModel.startAutoPolling()
        onPauseOrDispose {
            emailVerificationViewModel.stopAutoPolling()
        }
    }

    // When verified, display the success state briefly and transition to Profile Setup
    LaunchedEffect(uiState.isVerified) {
        if (uiState.isVerified) {
            delay(1200)
            onVerified()
        }
    }

    val openEmailApp = {
        val emailIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_EMAIL)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(emailIntent)
        } catch (_: Exception) {
            val mailtoIntent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"))
            try {
                context.startActivity(mailtoIntent)
            } catch (_: Exception) {
                Toast
                    .makeText(
                        context,
                        context.getString(R.string.auth_email_app_not_found),
                        Toast.LENGTH_LONG,
                    ).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (uiState.isVerified) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 16.dp),
            )

            Text(
                text = stringResource(R.string.auth_verify_complete_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .semantics { heading() },
            )

            Text(
                text = stringResource(R.string.auth_verify_complete_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp),
            )

            HustleCard(
                variant = HustleCardVariant.Elevated,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    HustleButton(
                        text = stringResource(R.string.auth_btn_continue_to_profile),
                        onClick = onVerified,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(TestTags.VERIFY_EMAIL_BUTTON),
                    )
                }
            }
        } else {
            Text(
                text = stringResource(R.string.auth_verify_email_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .semantics { heading() },
            )

            Text(
                text = stringResource(R.string.auth_verify_email_desc_format, email),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp),
            )

            HustleCard(
                variant = HustleCardVariant.Elevated,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    uiState.errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.semantics {
                                liveRegion = LiveRegionMode.Polite
                            },
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    HustleButton(
                        text = stringResource(R.string.auth_btn_open_email_app),
                        onClick = { openEmailApp() },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    HustleButton(
                        text = if (uiState.isLoading) {
                            stringResource(R.string.auth_btn_verifying)
                        } else {
                            stringResource(R.string.auth_btn_verify_status)
                        },
                        onClick = {
                            emailVerificationViewModel.verifyOtp("")
                        },
                        loading = uiState.isLoading,
                        variant = HustleButtonVariant.Outlined,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(TestTags.VERIFY_EMAIL_BUTTON),
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (uiState.resendCooldown > 0) {
                        Text(
                            text = stringResource(
                                R.string.auth_resend_cooldown_format,
                                uiState.resendCooldown,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    } else {
                        HustleButton(
                            text = stringResource(R.string.auth_btn_resend_email),
                            onClick = { emailVerificationViewModel.resendOtp() },
                            variant = HustleButtonVariant.Outlined,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(TestTags.RESEND_EMAIL_BUTTON),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = {
                    emailVerificationViewModel.signOut(onComplete = onNavigateToLogin)
                },
            ) {
                Text(
                    text = stringResource(R.string.auth_btn_change_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
