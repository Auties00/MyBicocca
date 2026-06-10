package it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import kotlin.math.abs

/**
 * Sheet-scoped palette of deep, muted accents for the catalog's teaching areas — lighter in the
 * dark variant for contrast on dark surfaces. An area's accent colours its tile and everything
 * browsed beneath it; areas with an official brand colour bypass this palette, the rest hash
 * into it.
 */
@Immutable
data class AreaAccentPalette(val accents: List<Color>)

private val LightAccents = listOf(
    Color(0xFF5A2E8E),
    Color(0xFF1F5466),
    Color(0xFF74222B),
    Color(0xFF1F5A3E),
    Color(0xFF7A4A12),
    Color(0xFF1F3A6E),
    Color(0xFF7A2256),
)

private val DarkAccents = listOf(
    Color(0xFF8E5DC4),
    Color(0xFF3F8395),
    Color(0xFFB44A56),
    Color(0xFF3B9772),
    Color(0xFFB87E3D),
    Color(0xFF4A6BAE),
    Color(0xFFB35487),
)

val LightAreaAccentPalette = AreaAccentPalette(LightAccents)
val DarkAreaAccentPalette = AreaAccentPalette(DarkAccents)

val LocalAreaAccentPalette = staticCompositionLocalOf { LightAreaAccentPalette }

/** Assigns a stable accent by hashing the area key into the accent list. */
fun AreaAccentPalette.accentFor(key: String): Color {
    val n = accents.size
    val hash = key.hashCode()
    return accents[abs(hash % n)]
}

/** Installs the light or dark area-accent palette for the sheet's subtree. */
@Composable
fun ProvideAreaAccentPalette(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val palette = if (dark) DarkAreaAccentPalette else LightAreaAccentPalette
    CompositionLocalProvider(LocalAreaAccentPalette provides palette, content = content)
}
