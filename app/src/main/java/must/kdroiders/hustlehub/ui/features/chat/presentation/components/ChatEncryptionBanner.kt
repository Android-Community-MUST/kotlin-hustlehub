package must.kdroiders.hustlehub.ui.features.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.ui.theme.chatE2EeBannerContainer
import must.kdroiders.hustlehub.ui.theme.chatE2EeBannerContent

private const val LOCK_INLINE_TAG = "lock_icon"
private val BANNER_CORNER_RADIUS = 8.dp

/**
 * WhatsApp-style security notice displayed at the very beginning of every chat.
 * Informs the user that messages are end-to-end encrypted.
 */
@Composable
fun ChatEncryptionBanner(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val bannerText = stringResource(R.string.chat_e2ee_banner_text)
    val contentColor = MaterialTheme.colorScheme.chatE2EeBannerContent
    val containerColor = MaterialTheme.colorScheme.chatE2EeBannerContainer

    val annotatedString = buildAnnotatedString {
        appendInlineContent(id = LOCK_INLINE_TAG)
        append(" ")
        append(bannerText)
    }

    val inlineContent = mapOf(
        LOCK_INLINE_TAG to InlineTextContent(
            Placeholder(
                width = 13.sp,
                height = 13.sp,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(12.dp),
            )
        },
    )

    val shape = RoundedCornerShape(BANNER_CORNER_RADIUS)
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(containerColor)
                .then(clickModifier)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = annotatedString,
                inlineContent = inlineContent,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp,
                    textAlign = TextAlign.Center,
                ),
                color = contentColor,
            )
        }
    }
}
