package it.attendance100.mybicocca.ui.theme

import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable

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
