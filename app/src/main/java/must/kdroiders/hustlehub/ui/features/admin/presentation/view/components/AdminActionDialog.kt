package must.kdroiders.hustlehub.ui.features.admin.presentation.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.sharedComposables.HustleTextField
import must.kdroiders.hustlehub.ui.features.admin.presentation.viewmodel.AdminActionTarget

@Composable
fun AdminActionDialog(
    target: AdminActionTarget,
    isLoading: Boolean,
    onConfirm: (reason: String, durationHours: Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    var reason by remember { mutableStateOf("") }
    var selectedDurationHours by remember { mutableStateOf<Long?>(72L) } // default 3 days

    val durationPresets = listOf(
        24L to stringResource(R.string.admin_dialog_duration_24h),
        72L to stringResource(R.string.admin_dialog_duration_3d),
        168L to stringResource(R.string.admin_dialog_duration_7d),
        720L to stringResource(R.string.admin_dialog_duration_30d),
        null to stringResource(R.string.admin_dialog_duration_permanent),
    )

    val (title, description, confirmText) = when (target) {
        is AdminActionTarget.SuspendUser -> Triple(
            stringResource(R.string.admin_dialog_suspend_title),
            stringResource(R.string.admin_dialog_suspend_desc, target.userName),
            stringResource(R.string.admin_dialog_suspend_confirm),
        )
        is AdminActionTarget.UnsuspendUser -> Triple(
            stringResource(R.string.admin_dialog_unsuspend_title),
            stringResource(R.string.admin_dialog_unsuspend_desc, target.userName),
            stringResource(R.string.admin_dialog_unsuspend_confirm),
        )
        is AdminActionTarget.VerifyPro -> Triple(
            stringResource(R.string.admin_dialog_verify_pro_title),
            stringResource(R.string.admin_dialog_verify_pro_desc, target.userName),
            stringResource(R.string.admin_dialog_verify_pro_confirm),
        )
        is AdminActionTarget.RevokePro -> Triple(
            stringResource(R.string.admin_dialog_revoke_pro_title),
            stringResource(R.string.admin_dialog_revoke_pro_desc, target.userName),
            stringResource(R.string.admin_dialog_revoke_pro_confirm),
        )
        is AdminActionTarget.DelistService -> Triple(
            stringResource(R.string.admin_dialog_delist_title),
            stringResource(R.string.admin_dialog_delist_desc, target.serviceTitle),
            stringResource(R.string.admin_dialog_delist_confirm),
        )
        is AdminActionTarget.RelistService -> Triple(
            stringResource(R.string.admin_dialog_relist_title),
            stringResource(R.string.admin_dialog_relist_desc, target.serviceTitle),
            stringResource(R.string.admin_dialog_relist_confirm),
        )
        is AdminActionTarget.ResolveReport -> Triple(
            stringResource(R.string.admin_dialog_resolve_title),
            stringResource(R.string.admin_dialog_resolve_desc),
            stringResource(R.string.admin_dialog_resolve_confirm),
        )
        is AdminActionTarget.DismissReport -> Triple(
            stringResource(R.string.admin_dialog_dismiss_title),
            stringResource(R.string.admin_dialog_dismiss_desc),
            stringResource(R.string.admin_dialog_dismiss_confirm),
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() },
            )
        },
        text = {
            Column {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (target is AdminActionTarget.SuspendUser) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.admin_dialog_duration_label),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp),
                    ) {
                        items(durationPresets) { (duration, label) ->
                            FilterChip(
                                selected = selectedDurationHours == duration,
                                onClick = { selectedDurationHours = duration },
                                label = { Text(label) },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HustleTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = stringResource(R.string.admin_dialog_reason_label),
                    placeholder = stringResource(R.string.admin_dialog_reason_placeholder),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    minLines = 2,
                    maxLines = 4,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        reason,
                        if (target is AdminActionTarget.SuspendUser) selectedDurationHours else null,
                    )
                },
                enabled = reason.isNotBlank() && !isLoading,
            ) {
                Text(if (isLoading) stringResource(R.string.admin_dialog_processing) else confirmText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading,
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}
