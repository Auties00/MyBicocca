package it.attendance100.mybicocca.ui.component.tax

import android.graphics.Matrix
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.PathParser
import android.graphics.Path as AndroidPath

const val BorderPathData =
	"M505,13c-5.52,0-10-4.48-10-10h-12c0,5.52-4.48,10-10,10s-10-4.48-10-10h-12c0,5.52-4.48,10-10,10s-10-4.48-10-10h-12c0,5.52-4.48,10-10,10s-10-4.48-10-10h-12c0,5.52-4.48,10-10,10s-10-4.48-10-10h-12c0,5.52-4.48,10-10,10s-10-4.48-10-10h-12c0,5.52-4.48,10-10,10s-10-4.48-10-10h-12c0,5.52-4.48,10-10,10s-10-4.48-10-10h-12c0,5.52-4.48,10-10,10s-10-4.48-10-10h-12c0,5.52-4.48,10-10,10s-10-4.48-10-10h-12c0,5.52-4.48,10-10,10s-10-4.48-10-10h-12c0,5.52-4.48,10-10,10s-10-4.48-10-10h-12c0,5.52-4.48,10-10,10s-10-4.48-10-10h-12c0,5.52-4.48,10-10,10s-10-4.48-10-10h-12c0,5.52-4.48,10-10,10s-10-4.48-10-10h-12c0,5.52-4.48,10-10,10S15,8.52,15,3H3v14h524V3h-12c0,5.52-4.48,10-10,10Z"


@Composable
@Preview
fun TicketSurfacePreview() {
	val ticketShape = remember { createTicketShape(BorderPathData) }

	Box(
		modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .padding(4.dp)
            .scale(1.25f),
		contentAlignment = Alignment.Center,
	) {
		Surface(
			modifier = Modifier
                .width(300.dp)
                .height(500.dp),
			shape = ticketShape,
			color = Color(0xFF3f3f3f),
			shadowElevation = 8.dp,
			onClick = { },
		) {
			Box(contentAlignment = Alignment.Center) {
				Text(
					text = "Custom Surface",
					color = Color.White,
				)
			}
		}
	}
}

fun createTicketShape(svgPathData: String): GenericShape {
	return GenericShape { size, _ ->
		val originalSvgWidth = 530f // from svg
		val borderHeight = 17f // from svg

		val scaleX = size.width / originalSvgWidth

		// Create Top Path
		val pathTop = PathParser.createPathFromPathData(svgPathData)
		val matrixTop = Matrix()
		matrixTop.setScale(scaleX, scaleX)
		pathTop.transform(matrixTop)

		// Create Bottom Path
		val pathBottom = PathParser.createPathFromPathData(svgPathData)
		val matrixBottom = Matrix()
		matrixBottom.setScale(scaleX, -scaleX) // Flip vertically
		matrixBottom.postTranslate(0f, size.height) // Move to bottom
		pathBottom.transform(matrixBottom)

		// Create Body Path
		val sideMargin = 3f * scaleX

		val pathBody = AndroidPath()
		val overlap = 1f

		pathBody.addRect(
			sideMargin,
			borderHeight - overlap,
			size.width - sideMargin,
			size.height - borderHeight + overlap,
			AndroidPath.Direction.CW,
		)

		// Merge body into top
		pathTop.op(pathBody, AndroidPath.Op.UNION)
		// Merge bottom into result
		pathTop.op(pathBottom, AndroidPath.Op.UNION)

		// Convert final unified Android Path to a Compose Path
		this.addPath(pathTop.asComposePath())
	}
}
