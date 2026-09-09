package must.kdroiders.hustlehub.ui.features.auth.presentation.view

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleButtonVariant
import must.kdroiders.hustlehub.sharedComposables.HustleScaffold
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun AccountSuspendedScreen(
    reason: String = "Violation of campus marketplace terms of service.",
    suspendedUntil: String? = null,
    onContactSupport: (() -> Unit)? = null,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val currentUser = remember {
        runCatching { FirebaseAuth.getInstance().currentUser }.getOrNull()
    }
    val userEmail = currentUser?.email ?: "Unknown Email"
    val userId = currentUser?.uid ?: "Unknown UID"

    val durationInfo = remember(suspendedUntil) {
        formatSuspensionDetails(suspendedUntil)
    }

    HustleScaffold(
        modifier = modifier.testTag("account_suspended_screen"),
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(Modifier.height(16.dp))

            // Glowing / Error Container with Icon
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = stringResource(R.string.cd_account_suspended),
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.auth_account_suspended_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.semantics { heading() },
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.auth_account_suspended_notice),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(16.dp))

            // Suspension Duration Badge Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (durationInfo.isPermanent) Icons.Default.Block else Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = if (durationInfo.isPermanent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = durationInfo.headline,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (durationInfo.subline != null) {
                            Text(
                                text = durationInfo.subline,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Reason Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.admin_dialog_reason_label),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = reason.ifBlank { stringResource(R.string.auth_account_review_pending) },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.auth_account_suspended_appeal_instructions),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(24.dp))

            // Appeal Button
            val appealSubject = stringResource(R.string.auth_account_suspended_appeal_subject)
            HustleButton(
                text = stringResource(R.string.auth_account_suspended_appeal_action),
                onClick = {
                    if (onContactSupport != null) {
                        onContactSupport()
                    } else {
                        val appealBody = buildString {
                            appendLine("Dear HustleHub Moderation Team,")
                            appendLine()
                            appendLine("I am writing to appeal my HustleHub account suspension.")
                            appendLine()
                            appendLine("• Account Email: $userEmail")
                            appendLine("• User ID: $userId")
                            appendLine("• Stated Reason: $reason")
                            appendLine("• Suspension Expiry: ${suspendedUntil ?: "Indefinite / Permanent"}")
                            appendLine()
                            appendLine("Context / Appeal Statement:")
                            appendLine("[Please describe what happened or why this suspension should be reviewed]")
                        }
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:kotlin.hustlehub@gmail.com")
                            putExtra(Intent.EXTRA_SUBJECT, "$appealSubject - $userEmail")
                            putExtra(Intent.EXTRA_TEXT, appealBody)
                        }
                        runCatching { context.startActivity(intent) }
                    }
                },
                icon = Icons.Default.Email,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))

            // Logout Button
            HustleButton(
                text = stringResource(R.string.settings_logout),
                onClick = onLogout,
                variant = HustleButtonVariant.Secondary,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

private data class SuspensionDetails(
    val headline: String,
    val subline: String?,
    val isPermanent: Boolean,
)

private fun formatSuspensionDetails(suspendedUntil: String?): SuspensionDetails {
    if (suspendedUntil.isNullOrBlank()) {
        return SuspensionDetails(
            headline = "Indefinite / Permanent Suspension",
            subline = "Your account has been indefinitely suspended by campus administrators.",
            isPermanent = true,
        )
    }

    return try {
        val until = Instant.parse(suspendedUntil)
        val now = Instant.now()
        val duration = Duration.between(now, until)
        val hours = duration.toHours()

        val formatter =
            DateTimeFormatter
                .ofLocalizedDate(FormatStyle.MEDIUM)
                .withZone(ZoneId.systemDefault())
        val dateStr = formatter.format(until)

        when {
            hours <= 0 -> SuspensionDetails(
                headline = "Suspension Expiring Soon",
                subline = "Access will be restored shortly.",
                isPermanent = false,
            )
            hours < 24 -> SuspensionDetails(
                headline = "Temporary Suspension (${hours}h remaining)",
                subline = "Suspended until $dateStr",
                isPermanent = false,
            )
            else -> {
                val days = (hours + 23) / 24
                SuspensionDetails(
                    headline = "Temporary Suspension ($days days remaining)",
                    subline = "Suspended until $dateStr",
                    isPermanent = false,
                )
            }
        }
    } catch (_: Exception) {
        SuspensionDetails(
            headline = "Account Suspension",
            subline = null,
            isPermanent = false,
        )
    }
}
