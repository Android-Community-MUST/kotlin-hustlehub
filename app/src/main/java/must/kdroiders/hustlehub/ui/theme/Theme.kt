package must.kdroiders.hustlehub.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = HustlePrimaryBlue,
    onPrimary = HustleOnPrimary,
    primaryContainer = HustleSecondaryBlue,
    onPrimaryContainer = HustleOnSecondaryBlue,

    secondary = HustleLinkBlue,
    onSecondary = HustleOnPrimary,
    secondaryContainer = HustleSecondaryBlue,
    onSecondaryContainer = HustleOnSecondaryBlue,

    tertiary = HustleTertiaryTeal,
    onTertiary = HustleOnTertiary,
    tertiaryContainer = Color(0xFFE0F7FA),
    onTertiaryContainer = Color(0xFF006064),

    background = HustleLightBackground,
    onBackground = HustleDarkNavy,
    surface = HustleWhite,
    onSurface = HustleDarkNavy,
    surfaceVariant = HustleLightSurfaceVariant,
    onSurfaceVariant = HustleMediumGrey,

    outline = HustleInputBorder,
    outlineVariant = HustleDividerGrey,

    error = HustleError,
    onError = HustleWhite,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),

    inverseSurface = HustleDarkNavy,
    inverseOnSurface = HustleWhite,
    inversePrimary = HustleSecondaryBlue,
    scrim = Color(0xFF000000).copy(alpha = 0.32f),
)

private val DarkColorScheme = darkColorScheme(
    primary = HustlePrimaryBlue,
    onPrimary = HustleOnDarkPrimary,
    primaryContainer = HustleDarkSecondaryContainer,
    onPrimaryContainer = HustleDarkOnSecondaryContainer,

    secondary = HustleLinkBlue,
    onSecondary = HustleOnPrimary,
    secondaryContainer = HustleDarkSecondaryContainer,
    onSecondaryContainer = HustleDarkOnSecondaryContainer,

    tertiary = HustleDarkTertiary,
    onTertiary = HustleOnTertiary,
    tertiaryContainer = HustleDarkTertiaryContainer,
    onTertiaryContainer = HustleDarkOnTertiaryContainer,

    background = HustleDarkBackground,
    onBackground = HustleDarkOnBackground,
    surface = HustleDarkSurface,
    onSurface = HustleDarkOnBackground,
    surfaceVariant = HustleDarkSurfaceVariant,
    onSurfaceVariant = HustleDarkOnSurfaceVariant,

    outline = HustleDarkOutline,
    outlineVariant = HustleDarkOutlineVariant,

    error = HustleError,
    onError = HustleWhite,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    inverseSurface = HustleDarkOnBackground,
    inverseOnSurface = HustleDarkBackground,
    inversePrimary = HustlePrimaryBlue,
    scrim = Color(0xFF000000).copy(alpha = 0.6f),
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HustleHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // kept false to enforce brand palette
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    val dimensions = when {
        screenWidthDp < 360 -> compactDimensions()
        screenWidthDp >= 600 -> expandedDimensions()
        else -> standardDimensions()
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalDimensions provides dimensions) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = HustleShapes,
            motionScheme = MotionScheme.expressive(),
            content = content,
        )
    }
}
