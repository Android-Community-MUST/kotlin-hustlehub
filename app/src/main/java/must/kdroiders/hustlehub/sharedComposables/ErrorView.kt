package must.kdroiders.hustlehub.sharedComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import must.kdroiders.hustlehub.ui.theme.HustleHubTheme

@Composable
fun ErrorView(
    message: String,
    modifier: Modifier = Modifier,
    title: String = "Something went wrong",
    icon: ImageVector = Icons.Rounded.ErrorOutline,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon inside an error-tinted circle
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )

        if (onRetry != null) {
            Spacer(Modifier.height(36.dp))
            HustleButton(
                text = "Try Again",
                onClick = onRetry,
                variant = HustleButtonVariant.Outlined
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorViewPreview() {
    HustleHubTheme {
        ErrorView(
            title = "Connection Failed",
            message = "We couldn't reach the server. Check your connection and try again.",
            onRetry = {}
        )
    }
}
