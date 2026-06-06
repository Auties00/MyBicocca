package it.attendance100.mybicocca.ui.component.shape

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class DiagonalSplitShape(private val splitTopLeftToBottomRight: Boolean) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            if (splitTopLeftToBottomRight) {
                // Draws the top left triangle half
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(0f, size.height)
            } else {
                // Draws the bottom right triangle half
                moveTo(size.width, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
            }
            close()
        }
        return Outline.Generic(path)
    }
}