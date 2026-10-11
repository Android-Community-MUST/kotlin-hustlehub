package must.kdroiders.hustlehub.sharedComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import must.kdroiders.hustlehub.R

@Composable
fun ProBadge(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!isVisible) return
    val cd = stringResource(R.string.badge_pro_cd)
    AssistChip(
        onClick = {},
        enabled = false,
        modifier = modifier.semantics { contentDescription = cd },
        label = {
            Text(
                text = stringResource(R.string.badge_pro_text),
                style = MaterialTheme.typography.labelSmall,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.padding(start = 4.dp),
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            disabledContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
            disabledLabelColor = MaterialTheme.colorScheme.onTertiaryContainer,
            disabledLeadingIconContentColor = MaterialTheme.colorScheme.tertiary,
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = false,
            borderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
            disabledBorderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
        ),
    )
}

@Composable
fun CompactProBadge(
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
) {
    if (!isVisible) return
    val cd = stringResource(R.string.badge_pro_cd)
    Row(
        modifier = modifier
            .semantics { contentDescription = cd }
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp),
            ).padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = stringResource(R.string.badge_pro_text),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = 10.sp,
        )
    }
}
