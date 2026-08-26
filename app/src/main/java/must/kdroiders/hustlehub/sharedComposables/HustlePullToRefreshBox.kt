package must.kdroiders.hustlehub.sharedComposables

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared HustleHub Pull-to-Refresh container.
 *
 * Wraps Material 3 [PullToRefreshBox] with HustleHub's signature gradient "H" monogram
 * indicator featuring spring bounce motion and dynamic drag rotation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HustlePullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        state = pullToRefreshState,
        enabled = enabled,
        indicator = {
            val progress = pullToRefreshState.distanceFraction.coerceIn(0f, 1f)
            val scale by animateFloatAsState(
                targetValue = if (isRefreshing) 1f else progress,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "logo_pull_scale",
            )
            val rotation by animateFloatAsState(
                targetValue = if (isRefreshing) 360f else progress * 180f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "logo_pull_rotation",
            )

            if (progress > 0f || isRefreshing) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .size(38.dp)
                        .scale(scale)
                        .graphicsLayer(rotationZ = rotation)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary,
                                ),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "H",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
        content = content,
    )
}
