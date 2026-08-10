package must.kdroiders.hustlehub.ui.features.analytics.presentation.view.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import must.kdroiders.hustlehub.ui.features.analytics.data.remote.dto.DailyCountDto

@Composable
fun BarChart(
    data: List<DailyCountDto>,
    title: String,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(800))
    }

    val textMeasurer = rememberTextMeasurer()
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = TextStyle(fontSize = 10.sp, color = labelColor, textAlign = TextAlign.Center)
    val valueStyle = TextStyle(fontSize = 9.sp, color = barColor, textAlign = TextAlign.Center)

    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        val chartData = if (data.isEmpty()) {
            val today = java.time.LocalDate.now()
            (6 downTo 0).map { daysAgo ->
                DailyCountDto(date = today.minusDays(daysAgo.toLong()).toString(), count = 0L)
            }
        } else {
            data
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .semantics {
                    contentDescription = "$title bar chart"
                },
        ) {
            val maxVal = chartData.maxOf { it.count }.coerceAtLeast(1)
            val barCount = chartData.size
            val gap = 12.dp.toPx()
            val labelHeight = 20.dp.toPx()
            val valueHeight = 16.dp.toPx()
            val chartHeight = size.height - labelHeight - valueHeight
            val totalGaps = (barCount + 1) * gap
            val barWidth = (size.width - totalGaps) / barCount

            // Draw horizontal baseline
            val baselineY = valueHeight + chartHeight
            drawLine(
                color = labelColor.copy(alpha = 0.2f),
                start = Offset(0f, baselineY),
                end = Offset(size.width, baselineY),
                strokeWidth = 1.dp.toPx(),
            )

            chartData.forEachIndexed { i, item ->
                val x = gap + i * (barWidth + gap)
                val rawH = (item.count.toFloat() / maxVal) * chartHeight * animProgress.value
                val barH = if (item.count > 0) rawH.coerceAtLeast(6.dp.toPx()) else 3.dp.toPx()
                val y = baselineY - barH
                val color = if (item.count > 0) barColor else barColor.copy(alpha = 0.25f)

                // bar
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barH),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                )

                // value on top
                val valText = item.count.toString()
                val valResult = textMeasurer.measure(valText, valueStyle)
                drawText(
                    valResult,
                    topLeft = Offset(
                        x + (barWidth - valResult.size.width) / 2,
                        y - valResult.size.height - 2.dp.toPx(),
                    ),
                )

                // day label below
                val dayLabel = item.date.takeLast(5) // MM-DD
                val labelResult = textMeasurer.measure(dayLabel, labelStyle)
                drawText(
                    labelResult,
                    topLeft = Offset(
                        x + (barWidth - labelResult.size.width) / 2,
                        size.height - labelHeight + 4.dp.toPx(),
                    ),
                )
            }
        }
    }
}
