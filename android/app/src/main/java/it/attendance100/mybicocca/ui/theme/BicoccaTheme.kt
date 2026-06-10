package it.attendance100.mybicocca.ui.theme

import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import it.attendance100.mybicocca.domain.model.settings.AppTheme

/**
 * App-wide Material theme wrapper: applies the [appTheme] palette resolved for [dark] (the flag
 * comes from the in-app ThemeMode setting, resolved upstream) under MaterialExpressiveTheme with
 * the standard motion scheme. It deliberately stays this narrow — screen-scoped palettes and
 * CompositionLocals are installed by their owning screens, so a screen can override the whole
 * MaterialTheme without competing palette state hanging around.
 */
@Composable
fun BicoccaTheme(
    dark: Boolean,
    appTheme: AppTheme = AppTheme.Default,
    content: @Composable () -> Unit,
) {
    MaterialExpressiveTheme(
        colorScheme = appTheme.colorScheme(dark),
        motionScheme = MotionScheme.standard(),
        content = content,
    )
}
