@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package must.kdroiders.hustlehub.sharedComposables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

enum class ProfileGateType {
    BOOKING,
    LISTING,
}

@Composable
fun ProfileGateBottomSheet(
    gateType: ProfileGateType,
    initialCampusLocation: String = "",
    initialPhone: String = "",
    initialBio: String = "",
    isSaving: Boolean = false,
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onSave: (campusLocation: String, phone: String, bio: String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var campusLocation by remember { mutableStateOf(initialCampusLocation) }
    var phone by remember { mutableStateOf(initialPhone) }
    var bio by remember { mutableStateOf(initialBio) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = when (gateType) {
                    ProfileGateType.BOOKING -> "Almost there!"
                    ProfileGateType.LISTING -> "Complete your provider profile"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = when (gateType) {
                    ProfileGateType.BOOKING -> "Add your campus location so the provider knows where you are."
                    ProfileGateType.LISTING -> "Clients want to know who they're hiring. Complete your profile to list a service."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HustleTextField(
                value = campusLocation,
                onValueChange = { campusLocation = it },
                label = "Campus Location",
                placeholder = "e.g. Hostels Area, Room 14B",
            )

            if (gateType == ProfileGateType.LISTING) {
                HustleTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone Number",
                    placeholder = "e.g. 0712345678",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                )

                HustleTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = "Short Bio",
                    placeholder = "Tell clients about your skills...",
                    singleLine = false,
                    maxLines = 4,
                )
            }

            errorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            HustleButton(
                text = if (isSaving) "Saving..." else "Save & Continue",
                loading = isSaving,
                enabled = campusLocation.isNotBlank(),
                onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        onSave(campusLocation, phone, bio)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
