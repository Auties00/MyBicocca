package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import it.attendance100.mybicocca.domain.model.library.LibraryZoneColor

/**
 * Accent swatch for a seating zone. The Bicocca zones are literally named by color, so the UI
 * honors that: a solid accent for the zone glyph and a low-alpha container for its surface.
 */
data class ZoneSwatch(
    val accent: Color,
    val container: Color,
    val onContainer: Color,
)

@Composable
fun zoneSwatch(color: LibraryZoneColor): ZoneSwatch {
    val accent = when (color) {
        LibraryZoneColor.Rossa -> Color(0xFFD3302F)
        LibraryZoneColor.Gialla -> Color(0xFFE0A100)
        LibraryZoneColor.Verde -> Color(0xFF2E9E55)
        LibraryZoneColor.Lilla -> Color(0xFF9A6BD8)
        LibraryZoneColor.Blu -> Color(0xFF2274D6)
        LibraryZoneColor.Arancio -> Color(0xFFEC6A1E)
        LibraryZoneColor.Carrels -> Color(0xFF00897B)
        LibraryZoneColor.Other -> MaterialTheme.colorScheme.primary
    }
    return ZoneSwatch(
        accent = accent,
        container = accent.copy(alpha = 0.14f),
        onContainer = MaterialTheme.colorScheme.onSurface,
    )
}
