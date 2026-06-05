package it.attendance100.mybicocca.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Dynamically selects [onLight] or [onDark] based on the WCAG luminance threshold (~0.179).
 * Ensures readable contrast for custom components (chips, pills) lacking standard Material roles.
 */
fun Color.contrastingContent(
    onLight: Color = Color.Black,
    onDark: Color = Color.White,
): Color = if (luminance() > 0.179f) onLight else onDark
