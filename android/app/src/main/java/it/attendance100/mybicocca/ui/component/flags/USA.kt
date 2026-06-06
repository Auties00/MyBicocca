package it.attendance100.mybicocca.ui.component.flags

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

@Composable
fun UsaFlag(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.background(FlagWhite)) {
        // 13 Stripes
        val stripeHeight = size.height / 13f
        for (i in 0 until 13) {
            if (i % 2 == 0) { // Red stripes on even indices
                drawRect(
                    color = FlagRed,
                    topLeft = Offset(0f, i * stripeHeight),
                    size = Size(size.width, stripeHeight)
                )
            }
        }

        // Canton
        val cantonWidth = size.width * 0.4f
        val cantonHeight = stripeHeight * 7f
        drawRect(
            color = FlagBlue,
            topLeft = Offset.Zero,
            size = Size(cantonWidth, cantonHeight)
        )

        // Stars
        val rows = 9
        val cols = 11
        val xSpacing = cantonWidth / (cols + 1)
        val ySpacing = cantonHeight / (rows + 1)
        for (row in 1..rows) {
            for (col in 1..cols) {
                // USA pattern alternates 6-star and 5-star rows
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