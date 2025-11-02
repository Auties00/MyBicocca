package it.attendance100.mybicocca.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MyBicoccaDarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    background = BackgroundColor,
    surface = BackgroundColor,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val MyBicoccaLightColorScheme = lightColorScheme(
    primary = PrimaryColorLight,
    background = BackgroundColorLight,
    surface = SurfaceColorLight,
    onPrimary = Color.White,
    onBackground = TextColorLight,
    onSurface = TextColorLight
)

@Composable
fun MyBicoccaTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MyBicoccaDarkColorScheme else MyBicoccaLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = if (darkTheme) BackgroundColor.toArgb() else BackgroundColorLight.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}