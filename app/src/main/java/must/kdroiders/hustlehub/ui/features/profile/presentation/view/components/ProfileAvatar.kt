package must.kdroiders.hustlehub.ui.features.profile.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import must.kdroiders.hustlehub.R

/** Profile avatar with gradient ring, verified badge, and optional camera edit button. */
@Composable
fun ProfileAvatar(
    photoUrl: String?,
    modifier: Modifier = Modifier,
    isVerified: Boolean = false,
    avatarSize: Dp = 100.dp,
    onEditPhotoClick: (() -> Unit)? = null,
    contentDescription: String? = null,
) {
    val defaultCd = stringResource(R.string.cd_profile_photo)
    val ringBorder = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.primary,
        ),
    )

    Box(contentAlignment = Alignment.BottomEnd, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(
                    width = 3.dp,
                    brush = ringBorder,
                    shape = CircleShape,
                ).padding(3.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (photoUrl.isNullOrEmpty()) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = contentDescription ?: defaultCd,
                    modifier = Modifier.size(avatarSize * 0.52f),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                )
            } else {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = contentDescription ?: defaultCd,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }

        if (onEditPhotoClick != null) {
            val changePhotoCd = stringResource(R.string.cd_change_profile_photo)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(
                        onClick = onEditPhotoClick,
                        role = Role.Button,
                    ).semantics {
                        this.contentDescription = changePhotoCd
                    },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        } else if (isVerified) {
            val verifiedCd = stringResource(R.string.cd_verified)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .semantics {
                        this.contentDescription = verifiedCd
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
