package it.attendance100.mybicocca.ui.screen.settings.subscreen.language.component

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

/**
 * Union Jack for the language picker, drawn as stacked layers on a blue field: the diagonal
 * saltires (a wide white cross under a thin red one), then St. George's cross built from
 * slightly oversized white rectangles under the red arms so the cross reads bordered without
 * any path math.
 */
@Composable
fun UkFlag(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.background(FlagBlue)) {
        drawDiagonalCross(color = FlagWhite, thickness = size.height * 0.15f)
        drawDiagonalCross(color = FlagRed, thickness = size.height * 0.05f)

        val mainCrossThickness = size.height * 0.2f
        val borderThickness = size.height * 0.03f

        drawRect(
            color = FlagWhite,
            topLeft = Offset(0f, (size.height / 2f) - (mainCrossThickness / 2f) - borderThickness),
            size = Size(size.width, mainCrossThickness + (2 * borderThickness))
        )
        drawRect(
            color = FlagWhite,
            topLeft = Offset((size.width / 2f) - (mainCrossThickness / 2f) - borderThickness, 0f),
            size = Size(mainCrossThickness + (2 * borderThickness), size.height)
        )

        drawRect(
            color = FlagRed,
            topLeft = Offset(0f, (size.height / 2f) - (mainCrossThickness / 2f)),
            size = Size(size.width, mainCrossThickness)
        )
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