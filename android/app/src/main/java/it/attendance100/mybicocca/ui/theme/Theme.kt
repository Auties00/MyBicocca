package it.attendance100.mybicocca.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val MyBicoccaDarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    onPrimary = OnPrimaryColor,
    secondary = SecondaryColor,
    onSecondary = OnSecondaryColor,
    tertiary = TertiaryColor,
    onTertiary = OnTertiaryColor,
    error = ErrorColor,
    onError = OnErrorColor,
    primaryContainer = PrimaryContainerColor,
    onPrimaryContainer = OnPrimaryContainerColor,
    secondaryContainer = SecondaryContainerColor,
    onSecondaryContainer = OnSecondaryContainerColor,
    tertiaryContainer = TertiaryContainerColor,
    onTertiaryContainer = OnTertiaryContainerColor,
    errorContainer = ErrorContainerColor,
    onErrorContainer = OnErrorContainerColor,
    surfaceDim = SurfaceDimColor,
    surface = BackgroundColor,
    surfaceBright = SurfaceBrightColor,
    surfaceContainerLowest = SurfaceContainerLowestColor,
    surfaceContainerLow = SurfaceContainerLowColor,
    surfaceContainer = SurfaceContainerColor,
    surfaceContainerHigh = SurfaceContainerHighColor,
    surfaceContainerHighest = SurfaceContainerHighestColor,
    onSurfaceVariant = OnSurfaceColor,
    surfaceVariant = OnSurfaceVariantColor,
    outline = OutlineColor,
    outlineVariant = OutlineVariantColor,
    inverseSurface = InverseSurfaceColor,
    inverseOnSurface = InverseOnSurfaceColor,
    inversePrimary = InversePrimaryColor,
    background = BackgroundColor,
    onBackground = OnBackgroundColor,
)

val MyBicoccaLightColorScheme = lightColorScheme(
    primary = PrimaryColorLight,
    onPrimary = OnPrimaryColorLight,
    secondary = SecondaryColorLight,
    onSecondary = OnSecondaryColorLight,
    tertiary = TertiaryColorLight,
    onTertiary = OnTertiaryColorLight,
    error = ErrorColorLight,
    onError = OnErrorColorLight,
    primaryContainer = PrimaryContainerColorLight,
    onPrimaryContainer = OnPrimaryContainerColorLight,
    secondaryContainer = SecondaryContainerColorLight,
    onSecondaryContainer = OnSecondaryContainerColorLight,
    tertiaryContainer = TertiaryContainerColorLight,
    onTertiaryContainer = OnTertiaryContainerColorLight,
    errorContainer = ErrorContainerColorLight,
    onErrorContainer = OnErrorContainerColorLight,
    surfaceDim = SurfaceDimColorLight,
    surface = SurfaceColorLight,
    surfaceBright = SurfaceBrightColorLight,
    surfaceContainerLowest = SurfaceContainerLowestColorLight,
    surfaceContainerLow = SurfaceContainerLowColorLight,
    surfaceContainer = SurfaceContainerColorLight,
    surfaceContainerHigh = SurfaceContainerHighColorLight,
    surfaceContainerHighest = SurfaceContainerHighestColorLight,
    onSurfaceVariant = OnSurfaceColorLight,
    surfaceVariant = OnSurfaceVariantColorLight,
    outline = OutlineColorLight,
    outlineVariant = OutlineVariantColorLight,
    inverseSurface = InverseSurfaceColorLight,
    inverseOnSurface = InverseOnSurfaceColorLight,
    inversePrimary = InversePrimaryColorLight,
    background = BackgroundColorLight,
    onBackground = OnBackgroundColorLight,
)

// PagoPA brand colors
val PagoPaColor = Color(0xFF006DCA)
val PagoPaSecondaryColor = Color(0xFF61E7FF)
private val PagoPaBackgroundDark = Color(0xFF0B1016)
private val PagoPaBackgroundLight = Color(0xFFB4D7FF)

@Composable
fun pagoPaBackgroundColor(): Color {
    val isDark = MaterialTheme.colorScheme.background == BackgroundColor
    return if (isDark) PagoPaBackgroundDark else PagoPaBackgroundLight
}

@Composable
fun MyBicoccaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) MyBicoccaDarkColorScheme else MyBicoccaLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val statusBarColor = if (darkTheme) BackgroundColor.toArgb() else BackgroundColorLight.toArgb()
            @Suppress("DEPRECATION")
            window.statusBarColor = statusBarColor
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
