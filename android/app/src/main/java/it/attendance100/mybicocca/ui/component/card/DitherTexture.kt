package it.attendance100.mybicocca.ui.component.card

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.tooling.preview.*
import kotlin.math.*

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
  shapeProvider: (size: Float) -> Path = { size -> RhombusPath(size) },
) {
  // We remember the path based on size so we don't rebuild it every frame unless the size changes
  val shapePath = remember(dotSize) { shapeProvider(dotSize) }

  Canvas(modifier = modifier) {
    // Rotate globally based on globalRotation
    rotate(degrees = globalRotation) {

      val buffer = size.maxDimension // Safety buffer to draw outside of bounds
      val gridWidth = size.width + buffer
      val gridHeight = size.height + buffer

      // Start drawing from negative coordinates to center the pattern
      val startX = -buffer / 2
      val startY = -buffer / 2

      val cols = (gridWidth / spacing).toInt()
      val rows = (gridHeight / spacing).toInt()

      for (row in 0..rows) {
        for (col in 0..cols) {
          // Position
          var x = startX + (col * spacing)
          val y = startY + (row * spacing)

          // Offset
          if (row % 2 != 0) x += spacing / 2


          // Calculate dithering probability
          val screenRelativeX = x - startX - (buffer / 2) + (size.width / 2)

          // Normalize position between 0 and 1 relative to canvas width
          val normalizedX = (screenRelativeX / size.width).coerceIn(0f, 1f)

          // 1.0 -> always draw
          // 0.0 -> never draw
          // Interpolate based on fadeStart and fadeEnd
          val drawProbability = when {
            normalizedX < fadeStart -> 1f
            normalizedX > fadeEnd -> 0f
            else -> 1f - ((normalizedX - fadeStart) / (fadeEnd - fadeStart))
          }

          val randomValue = pseudoRandom(row, col)

          if (randomValue < drawProbability) {
            translate(left = x, top = y) {
              // We rotate the individual dot around its own center
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

fun RhombusPath(size: Float): Path {
  val half = size / 2
  return Path().apply {
    moveTo(0f, -half) // Top
    lineTo(half, 0f)  // Right
    lineTo(0f, half)  // Bottom
    lineTo(-half, 0f) // Left
    close()
  }
}

fun pseudoRandom(x: Int, y: Int): Float {
  // A simple hash logic often used in shaders for noise
  val value = sin(x * 12.9898 + y * 78.233) * 43758.5453
  return (abs(value) % 1.0).toFloat()
}


@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Composable
fun TexturePreview() {
  Box(
	  modifier = Modifier
	    .fillMaxSize()
	    .background(Color.LightGray),
  ) {
    DitheredTexture(
      modifier = Modifier.fillMaxSize(),
      color = Color.Black,
      spacing = 50f,          // Distance between dots
      dotSize = 25f,          // Size of the dot
      globalRotation = 15f,   // Rotate the whole wall of dots
      dotRotation = 30f,       // Rotate the rhombus itself
      fadeStart = 0.2f,       // Fade starts at 10% width
      fadeEnd = 1f          // Fade ends at 90% width
    )
  }
}
