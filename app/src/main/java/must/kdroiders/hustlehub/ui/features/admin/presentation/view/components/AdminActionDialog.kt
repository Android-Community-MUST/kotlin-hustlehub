package must.kdroiders.hustlehub.ui.features.admin.presentation.view.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateFlow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import must.kdroiders.hustlehub.ui.features.admin.presentation.viewmodel.AdminActionTarget

@Composable
fun AdminActionDialog(
    target: AdminActionTarget,
    isLoading: Boolean,
    onConfirm: (reason: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var reason by remember { mutableStateOf("") }

    val (title, description, confirmText) = when (target) {
        is AdminActionTarget.SuspendUser -> Triple(
            "Suspend User",
            "Are you sure you want to suspend \"${target.userName}\"? They will be immediately blocked from logging in and accessing features.",
            "Suspend",
        )
        is AdminActionTarget.UnsuspendUser -> Triple(
            "Unsuspend User",
            "Are you sure you want to restore access for \"${target.userName}\"?",
            "Unsuspend",
        )
        is AdminActionTarget.VerifyPro -> Triple(
            "Grant Verified Pro Badge",
            "Grant the official HustleHub Verified PRO badge to \"${target.userName}\"?",
            "Grant Pro",
        )
        is AdminActionTarget.RevokePro -> Triple(
            "Revoke Verified Pro Badge",
            "Revoke the Pro badge from \"${target.userName}\"?",
            "Revoke Pro",
        )
        is AdminActionTarget.DelistService -> Triple(
            "Delist Service Listing",
            "Delist service \"${target.serviceTitle}\"? It will be hidden from search and discovery immediately.",
            "Delist",
        )
        is AdminActionTarget.RelistService -> Triple(
            "Relist Service",
            "Restore service \"${target.serviceTitle}\" back to search and discovery?",
            "Relist",
        )
        is AdminActionTarget.ResolveReport -> Triple(
            "Resolve Report",
            "Mark this report as resolved. Add an administrative note explaining the resolution:",
            "Resolve",
        )
        is AdminActionTarget.DismissReport -> Triple(
            "Dismiss Report",
            "Dismiss this report if no violation occurred. Add an explanation note:",
            "Dismiss",
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Admin reason note (Required)") },
                    placeholder = { Text("e.g., Terms violation, Spam, Verified credential") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(reason) },
                enabled = reason.isNotBlank() && !isLoading,
            ) {
                Text(if (isLoading) "Processing..." else confirmText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading,
            ) {
                Text("Cancel")
            }
        },
    )
}
