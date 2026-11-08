package com.canvotes.design.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.canvotes.core.model.CanvasThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = PureBlack,
    primaryContainer = Neutral800,
    onPrimaryContainer = PureWhite,
    secondary = Neutral400,
    onSecondary = PureBlack,
    background = Neutral950,
    onBackground = PureWhite,
    surface = Neutral900,
    onSurface = PureWhite,
    surfaceVariant = Neutral800,
    onSurfaceVariant = Neutral300,
    outline = Neutral700,
    outlineVariant = Neutral800
)

private val LightColorScheme = lightColorScheme(
    primary = PureBlack,
    onPrimary = PureWhite,
    primaryContainer = Neutral200,
    onPrimaryContainer = PureBlack,
    secondary = Neutral600,
    onSecondary = PureWhite,
    background = PureWhite,
    onBackground = PureBlack,
    surface = Neutral50,
    onSurface = PureBlack,
    surfaceVariant = Neutral100,
    onSurfaceVariant = Neutral700,
    outline = Neutral300,
    outlineVariant = Neutral200
)

@Composable
fun CanvotesTheme(
    themeMode: CanvasThemeMode = CanvasThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        CanvasThemeMode.SYSTEM -> isSystemInDarkTheme()
        CanvasThemeMode.DARK -> true
        CanvasThemeMode.LIGHT -> false
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
