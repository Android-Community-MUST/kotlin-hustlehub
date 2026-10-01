package must.kdroiders.hustlehub.ui.features.profile.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.sharedComposables.ProBadge
import must.kdroiders.hustlehub.ui.theme.success

/** Profile user info section displaying identity, live availability pill, campus location, and bio. */
@Composable
fun ProfileInfo(
    name: String,
    phone: String,
    campusLocation: String,
    bio: String,
    isOnline: Boolean = true,
    allowCalls: Boolean = false,
    isOwnProfile: Boolean = false,
    isProvider: Boolean = true,
    isVerifiedPro: Boolean = false,
    onAvailabilityToggle: ((Boolean) -> Unit)? = null,
) {
    val fallbackName = stringResource(R.string.profile_fallback_name)
    val statusAvailable = stringResource(R.string.profile_status_available)
    val statusOffDuty = stringResource(R.string.profile_status_off_duty)
    val fallbackCampusLoc = stringResource(R.string.profile_fallback_campus_location)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = name.ifBlank { fallbackName },
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        ProBadge(
            isVisible = isVerifiedPro,
            modifier = Modifier.padding(top = 2.dp),
        )
    }

    Spacer(Modifier.height(10.dp))

    if (isProvider) {
        val successColor = MaterialTheme.colorScheme.success
        val errorColor = MaterialTheme.colorScheme.error
        val statusColor = if (isOnline) successColor else errorColor
        val statusText = if (isOnline) statusAvailable else statusOffDuty

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(statusColor.copy(alpha = 0.12f))
                .border(1.dp, statusColor.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                .padding(horizontal = 14.dp, vertical = 4.dp)
                .semantics(mergeDescendants = !isOwnProfile) {
                    if (!isOwnProfile) {
                        contentDescription = statusText
                    }
                },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                        .clearAndSetSemantics {},
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = statusColor,
                )
                if (isOwnProfile && onAvailabilityToggle != null) {
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = isOnline,
                        onCheckedChange = onAvailabilityToggle,
                        modifier = Modifier
                            .scale(0.75f)
                            .semantics {
                                contentDescription = statusText
                                role = Role.Switch
                            },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = successColor,
                            checkedTrackColor = successColor.copy(alpha = 0.3f),
                            uncheckedThumbColor = errorColor,
                            uncheckedTrackColor = errorColor.copy(alpha = 0.3f),
                        ),
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))
    }

    val showPhone = (isOwnProfile || allowCalls) && phone.isNotBlank()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val campusText = campusLocation.ifBlank { fallbackCampusLoc }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = campusText
                },
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = campusText,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (showPhone) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .semantics(mergeDescendants = true) {
                        contentDescription = phone
                    },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = phone,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    if (bio.isNotBlank()) {
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .semantics(mergeDescendants = true) {},
        ) {
            Text(
                text = bio,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
