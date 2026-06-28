package it.attendance100.mybicocca.ui.component.shape

import android.graphics.Matrix
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.graphics.PathParser
import it.attendance100.mybicocca.core.os.rememberHapticManager

/**
 * A shape with a fixed-height top and bottom slice (driven by a vector path so the
 * edges can be scalloped/perforated like a ticket) and a stretched middle.
 */
class ThreeSliceShape(
    private val topSliceHeightPx: Float,
    private val bottomSliceHeightPx: Float,
    private val topVector: VectorDefinition?,
    private val bottomVector: VectorDefinition?,
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val fullPath = Path()

        if (topVector != null) {
            val p = PathParser.createPathFromPathData(topVector.pathData).asComposePath()
            val matrix = Matrix()
            matrix.setScale(size.width / topVector.viewportWidth, topSliceHeightPx / topVector.viewportHeight)
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
            matrix.postScale(size.width / bottomVector.viewportWidth, bottomSliceHeightPx / bottomVector.viewportHeight)
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
private fun ThreeSliceBackground(
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
    bottomHeight: Dp,
) {
    val blendMode = BlendMode.SrcIn
    Column(modifier = modifier) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(topHeight)) {
            Image(
                painter = painterResource(id = topRes),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth(),
                colorFilter = color?.let { ColorFilter.tint(it, blendMode) },
            )
            if (topRes2 != null) Image(
                painter = painterResource(id = topRes2),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth(),
                colorFilter = color2?.let { ColorFilter.tint(it, blendMode) },
            )
        }

        Box(modifier = Modifier
            .fillMaxSize()
            .weight(1f)) {
            Image(
                painter = painterResource(id = midRes),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
                colorFilter = color?.let { ColorFilter.tint(it, blendMode) },
            )
            if (midRes2 != null) Image(
                painter = painterResource(id = midRes2),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
                colorFilter = color2?.let { ColorFilter.tint(it, blendMode) },
            )
        }

        Box(modifier = Modifier
            .fillMaxWidth()
            .height(bottomHeight)
            .rotate(180f)) {
            Image(
                painter = painterResource(id = botRes),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth(),
                colorFilter = color?.let { ColorFilter.tint(it, blendMode) },
            )
            if (botRes2 != null) Image(
                painter = painterResource(id = botRes2),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth(),
                colorFilter = color2?.let { ColorFilter.tint(it, blendMode) },
            )
        }
    }
}

/**
 * A ticket/receipt-style card: scalloped top and bottom edges, a tinted [stroke] + [fill]
 * drawn from layered vector slices, and a [content] area inset past the slices. The clip
 * shape is rebuilt from the same top/bottom drawables that paint the background, so the
 * touch ripple and the artwork always share one silhouette.
 */
@Composable
fun DynamicCard(
    topSliceRes: Int,
    midSliceRes: Int,
    bottomSliceRes: Int,
    sliceTopHeightDp: Dp,
    sliceBottomHeightDp: Dp,
    modifier: Modifier = Modifier,
    topSliceRes2: Int? = null,
    midSliceRes2: Int? = null,
    bottomSliceRes2: Int? = null,
    stroke: Color? = null,
    fill: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val haptic = rememberHapticManager()
    val topVectorDef = remember(topSliceRes) { getVectorDefinition(context, topSliceRes) }
    val bottomVectorDef = remember(bottomSliceRes) { getVectorDefinition(context, bottomSliceRes) }
    val customShape = remember(density, sliceTopHeightDp, sliceBottomHeightDp, topVectorDef, bottomVectorDef) {
        ThreeSliceShape(
            topSliceHeightPx = with(density) { sliceTopHeightDp.toPx() },
            bottomSliceHeightPx = with(density) { sliceBottomHeightDp.toPx() },
            topVector = topVectorDef,
            bottomVector = bottomVectorDef,
        )
    }

    Surface(
        modifier = modifier,
        shape = customShape,
        onClick = {
            haptic.tap()
            onClick?.invoke()
        },
        enabled = onClick != null,
        color = Color.Transparent,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                modifier = Modifier.matchParentSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .defaultMinSize(minHeight = sliceTopHeightDp + sliceBottomHeightDp + 25.dp)
                    .padding(top = sliceTopHeightDp, bottom = sliceBottomHeightDp),
            ) {
                content()
            }
        }
    }
}
