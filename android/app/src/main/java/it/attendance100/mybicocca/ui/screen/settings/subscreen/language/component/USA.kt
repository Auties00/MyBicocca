package it.attendance100.mybicocca.ui.screen.settings.subscreen.language.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

val FlagBlue = Color(0xFF002868)
val FlagRed = Color(0xFFBF0A30)
val FlagWhite = Color.White

/**
 * Stars and Stripes for the language picker: thirteen stripes (red on the even indices over a
 * white ground), the blue canton spanning seven stripe heights, and the star field laid out on
 * the real alternating 6/5-per-row offset grid — row/column parity picks the staggered cells —
 * with each star simplified to a dot, which is all that survives at flag-tile scale.
 */
@Composable
fun UsaFlag(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.background(FlagWhite)) {
        val stripeHeight = size.height / 13f
        for (i in 0 until 13) {
            if (i % 2 == 0) {
                drawRect(
                    color = FlagRed,
                    topLeft = Offset(0f, i * stripeHeight),
                    size = Size(size.width, stripeHeight)
                )
            }
        }

        val cantonWidth = size.width * 0.4f
        val cantonHeight = stripeHeight * 7f
        drawRect(
            color = FlagBlue,
            topLeft = Offset.Zero,
            size = Size(cantonWidth, cantonHeight)
        )

        val rows = 9
        val cols = 11
        val xSpacing = cantonWidth / (cols + 1)
        val ySpacing = cantonHeight / (rows + 1)
        for (row in 1..rows) {
            for (col in 1..cols) {
                if ((row % 2 != 0 && col % 2 != 0) || (row % 2 == 0 && col % 2 == 0)) {
                    drawCircle(
                        color = FlagWhite,
                        radius = stripeHeight * 0.2f,
                        center = Offset(col * xSpacing, row * ySpacing)
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 180)
@Composable
private fun UsaFlagPreview() = FlagFrame { UsaFlag(Modifier.fillMaxSize()) }