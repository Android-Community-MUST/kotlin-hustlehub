package must.kdroiders.hustlehub.ui.features.profile.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import must.kdroiders.hustlehub.ui.theme.HustleSuccess

import must.kdroiders.hustlehub.sharedComposables.ServiceProviderBadge

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
    ServiceProviderBadge(
        name = name.ifBlank { "Hustler Provider" },
        isVerifiedPro = isVerifiedPro,
    )

    Spacer(Modifier.height(8.dp))

    // Live Availability Status Pill & Toggle (Shown ONLY for Providers)
    if (isProvider) {
        val successColor = HustleSuccess
        val errorColor = MaterialTheme.colorScheme.error
        val statusColor = if (isOnline) successColor else errorColor
        val statusText = if (isOnline) "Available on Campus" else "Off Duty"

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(statusColor.copy(alpha = 0.12f))
                    .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor,
                    )
                }
            }

            if (isOwnProfile && onAvailabilityToggle != null) {
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = isOnline,
                    onCheckedChange = onAvailabilityToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = successColor,
                        checkedTrackColor = successColor.copy(alpha = 0.2f),
                        uncheckedThumbColor = errorColor,
                        uncheckedTrackColor = errorColor.copy(alpha = 0.2f),
                    ),
                )
            }
        }
    }

    // Phone Privacy Guard: Only display phone if it's user's own profile OR provider explicitly allowed direct calls
    val showPhone = (isOwnProfile || allowCalls) && phone.isNotBlank()
    if (showPhone) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = phone,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    Spacer(Modifier.height(6.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = campusLocation.ifBlank { "MUST Main Campus" },
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    if (bio.isNotBlank()) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = bio,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
            lineHeight = 20.sp,
        )
    }
}
