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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

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
    val reasons = listOf("Spam", "Inappropriate", "Fake", "Harassment", "Other")

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
                    tint = must.kdroiders.hustlehub.ui.theme.HustleSuccess,
                    modifier = Modifier.padding(8.dp),
                )
            },
            title = {
                Text(
                    text = "Report Submitted",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = "Thank you. Our moderators will review this reported $targetType shortly.",
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
                    Text("OK")
                }
            },
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Report $targetType",
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
                        text = "Why are you reporting this?",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    reasons.forEach { reason ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedReason = reason }
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                                .semantics {
                                    role = Role.RadioButton
                                    selected = selectedReason == reason
                                },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = selectedReason == reason,
                                onClick = null,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = reason,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = {
                            Text(
                                text = if (selectedReason == "Other") "Please specify (required)" else "Additional context (optional)",
                            )
                        },
                        placeholder = { Text("Provide details...") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )

                    state.error?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            },
            confirmButton = {
                val isReasonValid = selectedReason != null
                val isDescriptionValid = selectedReason != "Other" || description.isNotBlank()

                Button(
                    enabled = isReasonValid && isDescriptionValid && !state.isSubmitting,
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
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text("Submit Report")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !state.isSubmitting,
                    onClick = onDismiss,
                ) {
                    Text("Cancel")
                }
            },
        )
    }
}
