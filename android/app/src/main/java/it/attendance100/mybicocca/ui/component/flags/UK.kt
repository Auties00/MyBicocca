package it.attendance100.mybicocca.ui.component.flags

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun UkFlag(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.background(FlagBlue)) {
        // Diagonals
        drawDiagonalCross(color = FlagWhite, thickness = size.height * 0.15f)
        drawDiagonalCross(color = FlagRed, thickness = size.height * 0.05f)

        // St. George's Cross
        val mainCrossThickness = size.height * 0.2f
        val borderThickness = size.height * 0.03f

        // Horizontal white border
        drawRect(
            color = FlagWhite,
            topLeft = Offset(0f, (size.height / 2f) - (mainCrossThickness / 2f) - borderThickness),
            size = Size(size.width, mainCrossThickness + (2 * borderThickness))
        )
        // Vertical white border
        drawRect(
            color = FlagWhite,
            topLeft = Offset((size.width / 2f) - (mainCrossThickness / 2f) - borderThickness, 0f),
            size = Size(mainCrossThickness + (2 * borderThickness), size.height)
        )

        // Horizontal main red cross
        drawRect(
            color = FlagRed,
            topLeft = Offset(0f, (size.height / 2f) - (mainCrossThickness / 2f)),
            size = Size(size.width, mainCrossThickness)
        )
        // Vertical main red cross
        drawRect(
            color = FlagRed,
            topLeft = Offset((size.width / 2f) - (mainCrossThickness / 2f), 0f),
            size = Size(mainCrossThickness, size.height)
        )
    }
}

fun DrawScope.drawDiagonalCross(color: Color, thickness: Float) {
    drawLine(
        color = color,
        start = Offset(0f, 0f),
        end = Offset(size.width, size.height),
        strokeWidth = thickness
    )
    drawLine(
        color = color,
        start = Offset(size.width, 0f),
        end = Offset(0f, size.height),
        strokeWidth = thickness
    )
}

@Preview(widthDp = 180)
@Composable
private fun UkFlagPreview() = FlagFrame { UkFlag(Modifier.fillMaxSize()) }