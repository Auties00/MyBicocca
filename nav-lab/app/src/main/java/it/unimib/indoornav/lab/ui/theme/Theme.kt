package it.unimib.indoornav.lab.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme

private val LightColors = lightColorScheme(
    primary = Color(0xFF0F4C81),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E6F7),
    onPrimaryContainer = Color(0xFF062542),
    secondary = Color(0xFF2D6A4F),
    onSecondary = Color.White,
    error = Color(0xFFC62828),
    background = Color(0xFFF7F9FC),
    surface = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8EC5FC),
    onPrimary = Color(0xFF042645),
    primaryContainer = Color(0xFF153D63),
    onPrimaryContainer = Color(0xFFD6E6F7),
    secondary = Color(0xFF74C69D),
    onSecondary = Color(0xFF082D1C),
    error = Color(0xFFEF5350),
)

@Composable
fun LabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
