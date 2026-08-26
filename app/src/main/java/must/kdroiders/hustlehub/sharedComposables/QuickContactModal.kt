package must.kdroiders.hustlehub.sharedComposables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Just-In-Time (JIT) contact info modal.
 *
 * Prompts customer for phone number and campus residence only when they
 * take a transaction action (e.g., DMing or booking a provider).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickContactModal(
    onDismiss: () -> Unit,
    onSaveContactInfo: (phone: String, campusLocation: String) -> Unit,
    initialPhone: String = "",
    initialCampusLocation: String = "",
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var phone by remember { mutableStateOf(initialPhone) }
    var campusLocation by remember { mutableStateOf(initialCampusLocation) }
    var error by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Text(
                text = "Delivery & Contact Info",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Enter your phone number and campus location so the service provider can reach you.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    error = null
                },
                label = { Text("Phone Number") },
                placeholder = { Text("e.g. 0712345678") },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = campusLocation,
                onValueChange = {
                    campusLocation = it
                    error = null
                },
                label = { Text("Campus Residence / Location") },
                placeholder = { Text("e.g. Hostel B Room 204 or Mess 2") },
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(24.dp))

            HustleButton(
                text = "Save & Continue",
                onClick = {
                    if (phone.isBlank()) {
                        error = "Phone number is required"
                        return@HustleButton
                    }
                    if (campusLocation.isBlank()) {
                        error = "Campus location is required"
                        return@HustleButton
                    }
                    onSaveContactInfo(phone.trim(), campusLocation.trim())
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
