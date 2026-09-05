package must.kdroiders.hustlehub.ui.features.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import must.kdroiders.hustlehub.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun DateSeparator(
    dateString: String,
    modifier: Modifier = Modifier,
) {
    val todayText = stringResource(R.string.chat_date_today)
    val yesterdayText = stringResource(R.string.chat_date_yesterday)
    val unknownDateText = stringResource(R.string.chat_date_unknown)

    val displayDate = try {
        val instant = Instant.parse(dateString)
        val zonedDateTime = instant.atZone(ZoneId.systemDefault())
        val now = Instant.now().atZone(ZoneId.systemDefault())

        when (ChronoUnit.DAYS.between(zonedDateTime.toLocalDate(), now.toLocalDate())) {
            0L -> todayText
            1L -> yesterdayText
            else -> zonedDateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        }
    } catch (e: Exception) {
        unknownDateText
    }

    val pillShape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .shadow(elevation = 1.dp, shape = pillShape)
                .clip(pillShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f))
                .padding(horizontal = 16.dp, vertical = 6.dp),
        ) {
            Text(
                text = displayDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                modifier = Modifier.semantics { heading() },
            )
        }
    }
}
