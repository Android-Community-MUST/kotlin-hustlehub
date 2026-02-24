package must.kdroiders.hustlehub.onboarding

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.ui.theme.HustleHubTheme

// slide data

private data class OnboardingSlide(
    val icon: ImageVector,
    val titleTop: String,
    val titleHighlight: String,
    val description: String
)

private val slides = listOf(
    OnboardingSlide(
        icon = Icons.Filled.Bolt,
        titleTop = "Campus Services,",
        titleHighlight = "Organised",
        description = "Find laundry, salon, tutoring, food " +
            "and more from verified Meru University " +
            "students. No WhatsApp groups."
    ),
    OnboardingSlide(
        icon = Icons.Filled.Forum,
        titleTop = "Discover &",
        titleHighlight = "Chat",
        description = "Find the services you need and " +
            "message providers directly. " +
            "Fast, easy, campus-first."
    ),
    OnboardingSlide(
        icon = Icons.Filled.Build,
        titleTop = "Build Your",
        titleHighlight = "Hustle",
        description = "Create your service profile, " +
            "showcase your skills, and start " +
            "earning on campus."
    )
)

// main composable

@Composable
fun OnboardingScreen(
    onboardingViewModel: OnboardingViewModel = hiltViewModel(),
    onFinished: () -> Unit = {}
) {
    val pagerState = rememberPagerState(
        pageCount = { slides.size }
    )
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == slides.lastIndex
    val isFirstPage = pagerState.currentPage == 0

    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary

    val onComplete: () -> Unit = {
        onboardingViewModel.completeOnboarding()
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (!isFirstPage) {
                    TextButton(onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage - 1
                            )
                        }
                    }) {
                        Text(
                            "Back",
                            color = MaterialTheme.colorScheme
                                .onSurfaceVariant
                        )
                    }
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                if (!isLastPage) {
                    TextButton(onClick = onComplete) {
                        Text(
                            "Skip",
                            color = MaterialTheme.colorScheme
                                .onSurfaceVariant
                        )
                    }
                }
            }

            // hero pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.50f)
            ) { page ->
                HeroSection(slide = slides[page])
            }

            // bottom content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.50f)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(
                            topStart = 28.dp,
                            topEnd = 28.dp
                        )
                    )
                    .padding(horizontal = 28.dp)
                    .padding(top = 32.dp, bottom = 16.dp)
            ) {
                // title + description
                Crossfade(
                    targetState = pagerState.currentPage,
                    label = "slideText"
                ) { page ->
                    val slide = slides[page]
                    Column {
                        Text(
                            text = slide.titleTop,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme
                                .onSurface
                        )
                        Text(
                            text = slide.titleHighlight,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography
                                .headlineMedium.copy(
                                    brush = Brush.linearGradient(
                                        listOf(primary, tertiary)
                                    )
                                )
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = slide.description,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = MaterialTheme.colorScheme
                                .onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                // page dots
                PageIndicator(
                    pagerState = pagerState,
                    pageCount = slides.size
                )

                Spacer(Modifier.height(24.dp))

                // next / get started
                HustleButton(
                    text = if (isLastPage) "Get Started"
                    else "Next",
                    icon = if (!isLastPage)
                        Icons.AutoMirrored.Filled.ArrowForward
                    else null,
                    onClick = {
                        if (isLastPage) {
                            onComplete()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    pagerState.currentPage + 1
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// hero section

@Composable
private fun HeroSection(
    slide: OnboardingSlide,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val background = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // glow behind icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .blur(48.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primary.copy(alpha = 0.45f),
                            tertiary.copy(alpha = 0.15f),
                            background.copy(alpha = 0f)
                        )
                    ),
                    shape = CircleShape
                )
        )

        // icon in rounded square
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = slide.icon,
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = primary
            )
        }
    }
}


// page dots

@Composable
private fun PageIndicator(
    pagerState: PagerState,
    pageCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected =
                pagerState.currentPage == index

            val width by animateDpAsState(
                targetValue = if (isSelected) 24.dp
                else 8.dp,
                label = "dotWidth"
            )
            val color by animateColorAsState(
                targetValue = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                label = "dotColor"
            )

            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

// previews

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingPreview() {
    HustleHubTheme(
        darkTheme = true,
        dynamicColor = false
    ) {
        val slide = slides[0]
        val primary = MaterialTheme.colorScheme.primary
        val tertiary = MaterialTheme.colorScheme.tertiary
        val pagerState = rememberPagerState(
            pageCount = { 3 }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.background
                )
        ) {
            Column(Modifier.fillMaxSize()) {
                Box(
                    Modifier.weight(0.50f),
                    contentAlignment = Alignment.Center
                ) {
                    HeroSection(slide = slide)
                }
                Column(
                    modifier = Modifier
                        .weight(0.50f)
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(
                                topStart = 28.dp,
                                topEnd = 28.dp
                            )
                        )
                        .padding(
                            horizontal = 28.dp,
                            vertical = 32.dp
                        )
                ) {
                    Text(
                        slide.titleTop,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme
                            .onSurface
                    )
                    Text(
                        slide.titleHighlight,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography
                            .headlineMedium.copy(
                                brush = Brush.linearGradient(
                                    listOf(primary, tertiary)
                                )
                            )
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        slide.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme
                            .onSurfaceVariant
                    )
                    Spacer(Modifier.weight(1f))
                    PageIndicator(
                        pagerState = pagerState,
                        pageCount = 3
                    )
                    Spacer(Modifier.height(24.dp))
                    HustleButton(
                        text = "Next",
                        icon = Icons.AutoMirrored.Filled
                            .ArrowForward,
                        onClick = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}
