package must.kdroiders.hustlehub.ui.features.home.presentation.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import must.kdroiders.hustlehub.ui.theme.CategoryNeonPurple

@Composable
fun HomeTopBar(
    initials: String,
    notificationCount: Int,
    onNotificationClick: () -> Unit,
    onProfileClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Logo + wordmark
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.semantics(mergeDescendants = true) {
                heading()
            },
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, CategoryNeonPurple),
                        ),
                    ).clearAndSetSemantics { }, // Hides "Capital H" from TalkBack,
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "H",
                    fontSize = 18.sp,
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = "HustleHub",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )
        }

        // Right icons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Notification bell with badge
            BadgedBox(
                badge = {
                    if (notificationCount > 0) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ) {
                            Text(
                                text = notificationCount.toString(),
                                fontSize = 9.sp,
                            )
                        }
                    }
                },
            ) {
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications${if (notificationCount > 0) ", $notificationCount unread" else ""}",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // User avatar with gradient ring
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, CategoryNeonPurple),
                        ),
                        shape = CircleShape,
                    ).background(MaterialTheme.colorScheme.surfaceVariant)
                    .semantics {
                        role = Role.Button
                        contentDescription = "User profile, initials $initials"
                    }.clickable {
                        if (onProfileClick != null) {
                            onProfileClick()
                        } else {
                            Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clearAndSetSemantics { }, // Suppresses raw text reading
                )
            }
        }
    }
}
