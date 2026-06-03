package it.attendance100.mybicocca.ui.screen.map.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import kotlin.math.acos

// Classic map-pin silhouette built as ONE path: a circular head whose sides run tangentially
// into the tip at the bottom center, so head and pointer read as a single seamless shape.
// The head fills the full width; whatever height exceeds the head becomes the pointer.
private object PinShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val radius = size.width / 2f
        val tipDistance = size.height - radius
        // Angular offset of each tangent touch point from straight-down, seen from the center.
        val tangentDegrees = Math.toDegrees(acos((radius / tipDistance).toDouble())).toFloat()
        val path = Path().apply {
            arcTo(
                rect = Rect(0f, 0f, size.width, size.width),
                startAngleDegrees = 90f + tangentDegrees,
                sweepAngleDegrees = 360f - 2f * tangentDegrees,
                forceMoveTo = true,
            )
            lineTo(size.width / 2f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

// Rasterized into a Google Maps marker via MarkerComposable. The pin tip lands on the marker's
// default bottom-center anchor. selectionProgress (0 = idle, 1 = selected) is animated by the
// caller and must also be passed as a MarkerComposable key — the marker is a bitmap, so each
// animation frame needs a re-rasterization; lerping here is what makes the growth smooth.
// The head shows the building's legacy U-number when it has one, otherwise a generic icon.
@Composable
fun BuildingMarker(selectionProgress: Float, code: String? = null) {
    val scheme = MaterialTheme.colorScheme
    // Springs can overshoot past 1: sizes may extrapolate (bouncy growth) but colors must clamp.
    val colorFraction = selectionProgress.coerceIn(0f, 1f)
    val container = lerp(scheme.primary, scheme.primaryContainer, colorFraction)
    val content = lerp(scheme.onPrimary, scheme.onPrimaryContainer, colorFraction)
    val headDiameter = lerp(36.dp, 46.dp, selectionProgress)
    val tailHeight = lerp(9.dp, 12.dp, selectionProgress)

    Surface(
        shape = PinShape,
        color = container,
        contentColor = content,
        border = BorderStroke(2.dp, scheme.surface),
        shadowElevation = 4.dp,
    ) {
        Box(
            modifier = Modifier
                .width(headDiameter)
                .height(headDiameter + tailHeight),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier.size(headDiameter),
                contentAlignment = Alignment.Center,
            ) {
                if (code != null) {
                    Text(
                        text = code,
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = lerp(
                            MaterialTheme.typography.labelMedium.fontSize,
                            MaterialTheme.typography.labelLarge.fontSize,
                            colorFraction,
                        ),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Apartment,
                        contentDescription = null,
                        modifier = Modifier.size(lerp(20.dp, 26.dp, selectionProgress)),
                    )
                }
            }
        }
    }
}
