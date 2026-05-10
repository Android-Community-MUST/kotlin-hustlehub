package must.kdroiders.hustlehub.ui.features.profile.presentation.view.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// User info — name, course · year, campus

@Composable
fun ProfileInfo(
    name: String,
    course: String,
    yearOfStudy: Int,
    campus: String
) {
    Text(
        text = name.ifBlank { "Student" },
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(4.dp))
    Text(
        text = buildString {
            if (course.isNotBlank()) append(course)
            append(" · Year $yearOfStudy")
        },
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(4.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme
                .onSurfaceVariant
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = campus.ifBlank { "Campus" },
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme
                .onSurfaceVariant
        )
    }
}

