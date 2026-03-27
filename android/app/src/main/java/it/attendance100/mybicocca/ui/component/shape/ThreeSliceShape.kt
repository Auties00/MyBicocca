package it.attendance100.mybicocca.ui.component.shape

import android.graphics.Matrix
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.graphics.PathParser
import it.attendance100.mybicocca.R

/**
 * A generic shape that has a fixed top and bottom height,
 * but stretches the middle vertical area.
 */
class ThreeSliceShape(
    private val topSliceHeightPx: Float,
    private val bottomSliceHeightPx: Float,
    private val topVector: VectorDefinition?,
    private val bottomVector: VectorDefinition?
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()

        if (topVector != null) {
            val androidPath = PathParser.createPathFromPathData(topVector.pathData)

            val scaleX = (size.width / topVector.viewportWidth)
            val scaleY = (topSliceHeightPx / topVector.viewportHeight)

            val matrix = Matrix()
            matrix.setScale(scaleX, scaleY)
            androidPath.transform(matrix)

            path.addPath(androidPath.asComposePath())
        } else {
            path.moveTo(0f, 0f)
            path.lineTo(size.width, 0f)
            path.lineTo(size.width, topSliceHeightPx)
            path.lineTo(0f, topSliceHeightPx)
        }

        val fullPath = Path()

        if (topVector != null) {
            val p = PathParser.createPathFromPathData(topVector.pathData).asComposePath()

            val scaleX = (size.width / topVector.viewportWidth)
            val scaleY = (topSliceHeightPx / topVector.viewportHeight)

            val matrix = Matrix()
            matrix.setScale(scaleX, scaleY)
            matrix.postTranslate(0f, -1f)
            p.asAndroidPath().transform(matrix)

            fullPath.addPath(p)
        } else {
            fullPath.moveTo(0f, 0f)
            fullPath.lineTo(size.width, 0f)
        }

        if (bottomVector != null) {
            val p = PathParser.createPathFromPathData(bottomVector.pathData).asComposePath()

            val matrix = Matrix()

            val scaleX = (size.width / bottomVector.viewportWidth)
            val scaleY = (bottomSliceHeightPx / bottomVector.viewportHeight)
            matrix.postScale(scaleX, scaleY)

            matrix.postRotate(180f)

            matrix.postTranslate(size.width, size.height)
            matrix.postTranslate(0f, 1f)

            p.asAndroidPath().transform(matrix)

            fullPath.addPath(p)
        } else {
            fullPath.lineTo(size.width, size.height)
            fullPath.lineTo(0f, size.height)
        }

        val middleRect = Path()
        middleRect.moveTo(0f, topSliceHeightPx - 1f)
        middleRect.lineTo(size.width, topSliceHeightPx - 1f)
        middleRect.lineTo(size.width, size.height - bottomSliceHeightPx + 1f)
        middleRect.lineTo(0f, size.height - bottomSliceHeightPx + 1f)
        middleRect.close()

        fullPath.addPath(middleRect)

        return Outline.Generic(fullPath)
    }
}


@Composable
fun ThreeSliceBackground(
    topRes: Int,
    topRes2: Int? = null,
    midRes: Int,
    midRes2: Int? = null,
    botRes: Int,
    botRes2: Int? = null,
    color: Color? = null,
    color2: Color? = null,
    modifier: Modifier = Modifier,
    topHeight: Dp,
    bottomHeight: Dp
) {
    val blendMode: BlendMode = BlendMode.SrcIn
    Column(modifier = modifier) {
        // Top Slice
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(topHeight)
        ) {
            Image(
                painter = painterResource(id = topRes),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth(),
                colorFilter = if (color != null) ColorFilter.tint(
                    color,
                    blendMode = blendMode
                ) else null
            )
            if (topRes2 != null) Image(
                painter = painterResource(id = topRes2),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth(),
                colorFilter = if (color2 != null) ColorFilter.tint(
                    color2,
                    blendMode = blendMode
                ) else null
            )
        }

        // Middle Slice
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
        ) {
            Image(
                painter = painterResource(id = midRes),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
                colorFilter = if (color != null) ColorFilter.tint(
                    color,
                    blendMode = blendMode
                ) else null
            )
            if (midRes2 != null) Image(
                painter = painterResource(id = midRes2),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
                colorFilter = if (color2 != null) ColorFilter.tint(
                    color2,
                    blendMode = blendMode
                ) else null
            )
        }


        // Bottom Slice
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(bottomHeight)
                .rotate(180f)
        ) {
            Image(
                painter = painterResource(id = botRes),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth(),
                colorFilter = if (color != null) ColorFilter.tint(
                    color,
                    blendMode = blendMode
                ) else null
            )
            if (botRes2 != null) Image(
                painter = painterResource(id = botRes2),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth(),
                colorFilter = if (color2 != null) ColorFilter.tint(
                    color2,
                    blendMode = blendMode
                ) else null
            )
        }
    }
}

@Composable
fun DynamicCard(
    topSliceRes: Int,
    midSliceRes: Int,
    bottomSliceRes: Int,
    topSliceRes2: Int? = null,
    midSliceRes2: Int? = null,
    bottomSliceRes2: Int? = null,
    stroke: Color? = null,
    fill: Color? = null,
    sliceTopHeightDp: Dp,
    sliceBottomHeightDp: Dp,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val topVectorDef = remember(topSliceRes) {
        getVectorDefinition(context, topSliceRes)
    }
    val bottomVectorDef = remember(bottomSliceRes) {
        getVectorDefinition(context, bottomSliceRes)
    }
    val customShape =
        remember(density, sliceTopHeightDp, sliceBottomHeightDp, topVectorDef, bottomVectorDef) {
            ThreeSliceShape(
                topSliceHeightPx = with(density) { sliceTopHeightDp.toPx() },
                bottomSliceHeightPx = with(density) { sliceBottomHeightDp.toPx() },
                topVector = topVectorDef,
                bottomVector = bottomVectorDef
            )
        }

    Surface(
        modifier = modifier,
        shape = customShape,
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        color = Color.Transparent
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // The Background Layer
            ThreeSliceBackground(
                topRes = topSliceRes,
                topRes2 = topSliceRes2,
                midRes = midSliceRes,
                midRes2 = midSliceRes2,
                botRes = bottomSliceRes,
                botRes2 = bottomSliceRes2,
                color = stroke,
                color2 = fill,
                topHeight = sliceTopHeightDp,
                bottomHeight = sliceBottomHeightDp,
                modifier = Modifier
                    .matchParentSize()
            )

            // The Content Layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .defaultMinSize(
                        minHeight = sliceTopHeightDp + sliceBottomHeightDp + 25.dp
                    )
                    .padding(top = sliceTopHeightDp, bottom = sliceBottomHeightDp)
            ) {
                content()
            }
        }
    }
}

@Preview(showSystemUi = false)
@Composable
fun DynamicCardPreview() {
    MaterialTheme {
        Box(modifier = Modifier.background(color = MaterialTheme.colorScheme.background)) {
            DynamicCard(
                R.drawable.border_comp_outer,
                R.drawable.body_outer,
                R.drawable.border_comp_outer,
                R.drawable.border_comp_inner,
                R.drawable.body_inner,
                R.drawable.border_comp_inner,
                stroke = Color.Green,
                fill = Color.Red,
                sliceTopHeightDp = 10.dp,
                sliceBottomHeightDp = 10.dp,
                onClick = {},
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                Text(
                    "Fattura",
                    modifier = Modifier.padding(10.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
