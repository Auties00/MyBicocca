package it.attendance100.mybicocca.ui.component.card

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.abs
import kotlin.math.sin

// Decorative dithered dot field. Dots are laid on a hex-offset grid and stochastically
// dropped toward the right edge (fadeStart..fadeEnd) using a cheap deterministic hash, so
// the pattern is stable across recompositions without allocating per-frame.
@Composable
fun DitheredTexture(
    modifier: Modifier = Modifier,
    color: Color = Color.Black,
    spacing: Float = 60f,
    dotSize: Float = 20f,
    globalRotation: Float = 0f,
    dotRotation: Float = 0f,
    fadeStart: Float = 0.2f,
    fadeEnd: Float = 0.9f,
    alpha: Float = 1f,
    shapeProvider: (size: Float) -> Path = { size -> rhombusPath(size) },
) {
    val shapePath = remember(dotSize) { shapeProvider(dotSize) }

    Canvas(modifier = modifier) {
        rotate(degrees = globalRotation) {
            val buffer = size.maxDimension
            val gridWidth = size.width + buffer
            val gridHeight = size.height + buffer

            val startX = -buffer / 2
            val startY = -buffer / 2

            val cols = (gridWidth / spacing).toInt()
            val rows = (gridHeight / spacing).toInt()

            for (row in 0..rows) {
                for (col in 0..cols) {
                    var x = startX + (col * spacing)
                    val y = startY + (row * spacing)

                    if (row % 2 != 0) x += spacing / 2

                    val screenRelativeX = x - startX - (buffer / 2) + (size.width / 2)
                    val normalizedX = (screenRelativeX / size.width).coerceIn(0f, 1f)

                    val drawProbability = when {
                        normalizedX < fadeStart -> 1f
                        normalizedX > fadeEnd -> 0f
                        else -> 1f - ((normalizedX - fadeStart) / (fadeEnd - fadeStart))
                    }

                    val randomValue = pseudoRandom(row, col)

                    if (randomValue < drawProbability) {
                        translate(left = x, top = y) {
                            rotate(degrees = dotRotation) {
                                drawPath(
                                    path = shapePath,
                                    color = color.copy(alpha = (drawProbability - 0.4f) * alpha),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun rhombusPath(size: Float): Path {
    val half = size / 2
    return Path().apply {
        moveTo(0f, -half)
        lineTo(half, 0f)
        lineTo(0f, half)
        lineTo(-half, 0f)
        close()
    }
}

// Deterministic value-noise hash (sin-fract), the shader idiom for cheap pseudo-randomness.
private fun pseudoRandom(x: Int, y: Int): Float {
    val value = sin(x * 12.9898 + y * 78.233) * 43758.5453
    return (abs(value) % 1.0).toFloat()
}
