package it.attendance100.mybicocca.ui.theme

import android.app.*
import android.os.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.core.view.*

private val MyBicoccaDarkColorScheme = darkColorScheme(
  primary = PrimaryColor,
  onPrimary = OnPrimaryColor,
  secondary = SecondaryColor,
  onSecondary = onSecondaryColor,
  tertiary = TertiaryColor,
  onTertiary = onTertiaryColor,
  error = ErrorColor,
  onError = onErrorColor,
  primaryContainer = primaryContainerColor,
  onPrimaryContainer = onPrimaryContainerColor,
  secondaryContainer = secondaryContainerColor,
  onSecondaryContainer = onSecondaryContainerColor,
  tertiaryContainer = tertiaryContainerColor,
  onTertiaryContainer = onTertiaryContainerColor,
  errorContainer = errorContainerColor,
  onErrorContainer = onErrorContainerColor,
  surfaceDim = surfaceDimColor,
  surface = surfaceColor,
  surfaceBright = surfaceBrightColor,
  surfaceContainerLowest = surfaceContainerLowestColor,
  surfaceContainerLow = surfaceContainerLowColor,
  surfaceContainer = surfaceContainerColor,
  surfaceContainerHigh = surfaceContainerHighColor,
  surfaceContainerHighest = surfaceContainerHighestColor,
  onSurfaceVariant = onSurfaceColor,
  surfaceVariant = onSurfaceVarColor,
  outline = outlineColor,
  outlineVariant = outlineVariantColor,
  inverseOnSurface = inverseOnSurfaceColor,
  inverseSurface = inverseSurfaceColor,
  inversePrimary = inversePrimaryColor,
  background = BackgroundColor,
)

private val MyBicoccaLightColorScheme = lightColorScheme(
  primary = PrimaryColorLight,
  onPrimary = OnPrimaryColorLight,
  secondary = SecondaryColorLight,
  onSecondary = onSecondaryColorLight,
  tertiary = TertiaryColorLight,
  onTertiary = onTertiaryColorLight,
  error = ErrorColorLight,
  onError = onErrorColorLight,
  primaryContainer = primaryContainerColorLight,
  onPrimaryContainer = onPrimaryContainerColorLight,
  secondaryContainer = secondaryContainerColorLight,
  onSecondaryContainer = onSecondaryContainerColorLight,
  tertiaryContainer = tertiaryContainerColorLight,
  onTertiaryContainer = onTertiaryContainerColorLight,
  errorContainer = errorContainerColorLight,
  onErrorContainer = onErrorContainerColorLight,
  surfaceDim = surfaceDimColorLight,
  surface = surfaceColorLight,
  surfaceBright = surfaceBrightColorLight,
  surfaceContainerLowest = surfaceContainerLowestColorLight,
  surfaceContainerLow = surfaceContainerLowColorLight,
  surfaceContainer = surfaceContainerColorLight,
  surfaceContainerHigh = surfaceContainerHighColorLight,
  surfaceContainerHighest = surfaceContainerHighestColorLight,
  onSurfaceVariant = onSurfaceColorLight,
  surfaceVariant = onSurfaceVarColorLight,
  outline = outlineColorLight,
  outlineVariant = outlineVariantColorLight,
  inverseOnSurface = inverseOnSurfaceColorLight,
  inverseSurface = inverseSurfaceColorLight,
  inversePrimary = inversePrimaryColorLight,
  background = BackgroundColorLight,
)

@Composable
fun MyBicoccaTheme(
  darkTheme: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) MyBicoccaDarkColorScheme else MyBicoccaLightColorScheme

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      val color = if (darkTheme) BackgroundColor.toArgb() else BackgroundColorLight.toArgb()
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) { // Android 15+
        window.decorView.setOnApplyWindowInsetsListener { view, insets ->
          // val statusBarInsets = insets.getInsets(WindowInsets.Type.statusBars())
          view.setBackgroundColor(color)

          // Adjust padding to avoid overlap
          view.setPadding(0, 0, 0, 0)
          insets
        }
      } else {
        // For Android 14 and below
        window.statusBarColor = color
      }
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = MyBicoccaTypography,
    content = content
  )
}