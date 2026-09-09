package must.kdroiders.hustlehub.ui.features.admin.presentation.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleButtonVariant
import must.kdroiders.hustlehub.sharedComposables.HustleTextField
import must.kdroiders.hustlehub.ui.features.admin.presentation.viewmodel.AdminActionTarget

@Composable
fun AdminServicesTab(
    onActionClick: (AdminActionTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    var serviceIdInput by remember { mutableStateOf("") }
    val serviceTargetLabel = stringResource(R.string.admin_services_target_format, serviceIdInput)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.admin_services_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(R.string.admin_services_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {},
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.admin_services_card_title),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                HustleTextField(
                    value = serviceIdInput,
                    onValueChange = { serviceIdInput = it },
                    label = stringResource(R.string.admin_services_id_label),
                    placeholder = stringResource(R.string.admin_services_id_placeholder),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    HustleButton(
                        text = stringResource(R.string.admin_services_action_relist),
                        onClick = {
                            if (serviceIdInput.isNotBlank()) {
                                onActionClick(
                                    AdminActionTarget.RelistService(
                                        serviceId = serviceIdInput.trim(),
                                        serviceTitle = serviceTargetLabel,
                                    ),
                                )
                            }
                        },
                        enabled = serviceIdInput.isNotBlank(),
                        variant = HustleButtonVariant.Outlined,
                        modifier = Modifier.height(44.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    HustleButton(
                        text = stringResource(R.string.admin_services_action_delist),
                        onClick = {
                            if (serviceIdInput.isNotBlank()) {
                                onActionClick(
                                    AdminActionTarget.DelistService(
                                        serviceId = serviceIdInput.trim(),
                                        serviceTitle = serviceTargetLabel,
                                    ),
                                )
                            }
                        },
                        enabled = serviceIdInput.isNotBlank(),
                        variant = HustleButtonVariant.Primary,
                        modifier = Modifier.height(44.dp),
                    )
                }
            }
        }
    }
}
