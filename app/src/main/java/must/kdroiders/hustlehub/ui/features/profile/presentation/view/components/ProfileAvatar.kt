package must.kdroiders.hustlehub.ui.features.profile.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

// Avatar — circular photo with gradient border

@Composable
fun ProfileAvatar(photoUrl: String) {
    val gradientBorder = Brush.linearGradient(
        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.surfaceVariant),
    )

    Box(
        modifier = Modifier
            .size(110.dp)
            .border(
                width = 3.dp,
                brush = gradientBorder,
                shape = CircleShape,
            ).padding(4.dp)
            .clip(CircleShape)
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (photoUrl.isNotBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Profile photo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "No photo",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme
                    .onSurfaceVariant,
            )
        }
    }
}
