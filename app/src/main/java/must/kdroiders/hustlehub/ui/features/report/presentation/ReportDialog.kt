package must.kdroiders.hustlehub.ui.features.report.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleTextField
import must.kdroiders.hustlehub.ui.theme.HustleSuccess

private data class ReportReasonItem(
    val key: String,
    val labelResId: Int,
)

private val REPORT_REASONS = listOf(
    ReportReasonItem("Spam", R.string.report_reason_spam),
    ReportReasonItem("Inappropriate", R.string.report_reason_inappropriate),
    ReportReasonItem("Fake", R.string.report_reason_fake),
    ReportReasonItem("Harassment", R.string.report_reason_harassment),
    ReportReasonItem("Other", R.string.report_reason_other),
)

@Composable
fun ReportDialog(
    targetId: String,
    targetType: String, // "user" or "service" or "message"
    onDismiss: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    var selectedReason by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    if (state.isSuccess) {
        AlertDialog(
            onDismissRequest = {
                onDismiss()
                viewModel.resetState()
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = HustleSuccess,
                    modifier = Modifier.padding(8.dp),
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.report_submitted_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.report_submitted_message, targetType),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDismiss()
                        viewModel.resetState()
                    },
                ) {
                    Text(stringResource(R.string.action_ok))
                }
            },
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = stringResource(R.string.report_target_title, targetType),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() },
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.report_why_prompt),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    REPORT_REASONS.forEach { reasonItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedReason = reasonItem.key }
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                                .semantics {
                                    role = Role.RadioButton
                                    selected = selectedReason == reasonItem.key
                                },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = selectedReason == reasonItem.key,
                                onClick = null,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(reasonItem.labelResId),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    HustleTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = if (selectedReason == "Other") {
                            stringResource(R.string.report_specify_required)
                        } else {
                            stringResource(R.string.report_context_optional)
                        },
                        placeholder = stringResource(R.string.report_details_placeholder),
                        singleLine = false,
                        minLines = 3,
                        maxLines = 4,
                        isError = state.error != null,
                        errorText = state.error,
                        enabled = !state.isSubmitting,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                val isReasonValid = selectedReason != null
                val isDescriptionValid = selectedReason != "Other" || description.isNotBlank()

                HustleButton(
                    text = stringResource(R.string.report_submit_action),
                    enabled = isReasonValid && isDescriptionValid && !state.isSubmitting,
                    loading = state.isSubmitting,
                    onClick = {
                        selectedReason?.let { reason ->
                            viewModel.submitReport(
                                targetId = targetId,
                                targetType = targetType,
                                reason = reason,
                                description = description.ifBlank { null },
                            )
                        }
                    },
                )
            },
            dismissButton = {
                TextButton(
                    enabled = !state.isSubmitting,
                    onClick = onDismiss,
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}
