package must.kdroiders.hustlehub.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SplashScreen(
    splashViewModel: SplashViewModel = hiltViewModel(),
    onNavigate: (SplashDestination) -> Unit = {}
) {
    val destination by splashViewModel.destination.collectAsState()
    val motionScheme = MaterialTheme.motionScheme


    // --- Motion specs from MaterialTheme ---
    // Spatial: for scale/position — expressive spring with natural overshoot
    val spatialSpec = motionScheme.defaultSpatialSpec<Float>()
    // Effects: for alpha/color — critically damped spring, no bounce
    val effectsSpec = motionScheme.defaultEffectsSpec<Float>()
    val fastEffectsSpec = motionScheme.fastEffectsSpec<Float>()

    // --- Animation state ---
    val iconAlpha = remember { Animatable(0f) }
    val iconScale = remember { Animatable(0.6f) }
    val textAlpha = remember { Animatable(0f) }
    val barAlpha = remember { Animatable(0f) }
    val footerAlpha = remember { Animatable(0f) }

    // Glow pulse — infinite, so stays as manual tween
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Progress bar fill — infinite, so stays as manual tween
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    // Sequenced entrance using MotionScheme specs
    LaunchedEffect(Unit) {
        // 1. Icon fades in (effects — no bounce on alpha)
        iconAlpha.animateTo(1f, effectsSpec)
        // 2. Icon scales up (spatial — bouncy, expressive spring)
        iconScale.animateTo(1f, spatialSpec)
        // 3. Text fades in
        textAlpha.animateTo(1f, effectsSpec)
        // 4. Progress bar + footer appear quickly
        barAlpha.animateTo(1f, fastEffectsSpec)
        footerAlpha.animateTo(1f, fastEffectsSpec)
    }

    // Navigate when destination resolves
    LaunchedEffect(destination) {
        destination?.let { onNavigate(it) }
    }

    // --- Colors from MaterialTheme ---
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val background = MaterialTheme.colorScheme.background
    val onBackground = MaterialTheme.colorScheme.onBackground
    val outline = MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {
        // Centered content
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon + glow
            Box(contentAlignment = Alignment.Center) {
                // Glow layer behind icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .blur(40.dp)
                        .graphicsLayer { alpha = glowAlpha * iconAlpha.value }
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    primary.copy(alpha = 0.5f),
                                    tertiary.copy(alpha = 0.2f),
                                    background.copy(alpha = 0f)
                                )
                            ),
                            shape = RoundedCornerShape(50)
                        )
                )

                // Lightning bolt icon
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = "HustleHub Logo",
                    modifier = Modifier
                        .size(64.dp)
                        .graphicsLayer {
                            alpha = iconAlpha.value
                            scaleX = iconScale.value
                            scaleY = iconScale.value
                        },
                    tint = primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App name
            Text(
                text = "HustleHub",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer { alpha = textAlpha.value }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Your Campus Marketplace",
                fontSize = 14.sp,
                color = outline,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer { alpha = textAlpha.value }
            )
        }

        // Bottom section
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gradient progress bar
            Box(
                modifier = Modifier
                    .graphicsLayer { alpha = barAlpha.value }
                    .width(200.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(onBackground.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = shimmerProgress.coerceIn(0f, 1f))
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(primary, tertiary)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // University verification footer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.graphicsLayer { alpha = footerAlpha.value }
            ) {
                Icon(
                    imageVector = Icons.Filled.VerifiedUser,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = outline
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "MERU UNIVERSITY · VERIFIED",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = outline,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
