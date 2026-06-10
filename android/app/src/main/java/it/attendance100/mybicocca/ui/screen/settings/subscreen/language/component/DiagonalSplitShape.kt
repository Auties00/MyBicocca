package it.attendance100.mybicocca.ui.screen.settings.subscreen.language.component

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * Triangular half of the bounds, cut along the diagonal running from the top-right corner to
 * the bottom-left corner: true keeps the top-left triangle, false the bottom-right one. Used
 * to layer two flags into a single split tile.
 */
class DiagonalSplitShape(private val splitTopLeftToBottomRight: Boolean) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            if (splitTopLeftToBottomRight) {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(0f, size.height)
            } else {
                moveTo(size.width, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
            }
            close()
        }
        return Outline.Generic(path)
    }
}