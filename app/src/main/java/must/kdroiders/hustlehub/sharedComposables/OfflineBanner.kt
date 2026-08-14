package must.kdroiders.hustlehub.sharedComposables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import must.kdroiders.hustlehub.ui.theme.HustleActiveGreen
import must.kdroiders.hustlehub.ui.theme.HustleWarningAmber
import must.kdroiders.hustlehub.ui.theme.HustleWhite

@Composable
fun OfflineBanner(
    isOnline: Boolean,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var showReconnected by remember { mutableStateOf(false) }

    LaunchedEffect(isOnline) {
        if (isOnline) {
            showReconnected = true
            delay(3_000L)
            showReconnected = false
        }
    }

    val visible = !isOnline || showReconnected

    AnimatedVisibility(
        visible = visible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier.fillMaxWidth(),
    ) {
        val isOffline = !isOnline

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isOffline) HustleWarningAmber else HustleActiveGreen)
                .padding(horizontal = 16.dp, vertical = 2.dp)
                .semantics {
                    liveRegion = LiveRegionMode.Polite
                }.testTag("offline_banner"),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = if (isOffline) Icons.Default.WifiOff else Icons.Default.Wifi,
                    contentDescription = null,
                    tint = HustleWhite,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = if (isOffline) "You're offline. Browsing cached data." else "Back online",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = HustleWhite,
                    fontSize = 12.sp,
                )
                if (isOffline && onRetry != null) {
                    IconButton(
                        onClick = onRetry,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry connection",
                            tint = HustleWhite,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}
