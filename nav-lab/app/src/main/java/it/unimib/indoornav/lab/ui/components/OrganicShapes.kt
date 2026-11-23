package it.unimib.indoornav.lab.ui.components

import androidx.compose.ui.graphics.lerp
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import it.unimib.indoornav.lab.ui.theme.BeaconHaloTokens
import it.unimib.indoornav.lab.ui.theme.LabTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class SmoothCookieShape(
    private val lobes: Int,
    private val dip: Float,
    private val samples: Int = 144,
    private val rotationRadians: Float = 0f,
) : Shape {
    /** Radius at absolute angle [a], as a fraction of the half size. */
    fun radiusAt(a: Float): Float = 1f - dip * (1f - cos(lobes * (a - rotationRadians))) / 2f

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        cookieOutline(size, samples, ::radiusAt)
}

/** Blends two cookie shapes radius by radius, so the lobes melt into each other as [fraction] goes 0 to 1. */
class MorphingCookieShape(
    private val from: SmoothCookieShape,
    private val to: SmoothCookieShape,
    private val fraction: Float,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        cookieOutline(size, 192) { a -> from.radiusAt(a) + (to.radiusAt(a) - from.radiusAt(a)) * fraction }
}

private fun cookieOutline(size: Size, samples: Int, radiusAt: (Float) -> Float): Outline {
    val path = Path()
    val cx = size.width / 2f
    val cy = size.height / 2f
    for (i in 0 until samples) {
        val a = (i.toFloat() / samples) * (2 * PI.toFloat())
        val r = radiusAt(a)
        val x = cx + cos(a) * cx * r
        val y = cy + sin(a) * cy * r
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return Outline.Generic(path)
}

object OrganicShapes {
    val SmoothCookie6 = SmoothCookieShape(lobes = 6, dip = 0.20f, rotationRadians = (PI / 2).toFloat())
    val SmoothCookie8 = SmoothCookieShape(lobes = 8, dip = 0.16f, rotationRadians = 0.35f)
    val SmoothCookie12 = SmoothCookieShape(lobes = 12, dip = 0.10f, samples = 192)

    /** A beacon's card halo, fixed per beacon so its card and its detail transition agree. */
    fun cardShapeFor(tileKey: String): SmoothCookieShape =
        if (tileKey.hashCode() % 2 == 0) SmoothCookie6 else SmoothCookie8
}

@Composable
fun OrganicBeaconHalo(
    online: Boolean,
    shape: Shape,
    modifier: Modifier = Modifier,
    stale: Boolean = false,
) {
    val settledFill by animateColorAsState(
        targetValue = if (online) BeaconHaloTokens.OnlineFill else BeaconHaloTokens.OfflineFill,
        animationSpec = tween(durationMillis = 300),
        label = "haloFill",
    )
    // A beacon gone quiet breathes towards grey until it is declared offline.
    val breath by animateFloatAsState(
        targetValue = if (stale) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "haloStale",
    )
    val reduceMotion = rememberReduceMotion()
    val pulse = if (reduceMotion) {
        if (stale) 0.5f else 0f
    } else if (stale || breath > 0f) {
        rememberInfiniteTransition(label = "haloPulse").animateFloat(
            initialValue = 0f,
            targetValue = 0.85f,
            animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "haloPulseFraction",
        ).value
    } else {
        0f
    }
    val fillColor = lerp(settledFill, BeaconHaloTokens.OfflineFill, pulse * breath)
    val strokeColor by animateColorAsState(
        targetValue = if (online) BeaconHaloTokens.OnlineStroke else BeaconHaloTokens.OfflineStroke,
        animationSpec = tween(durationMillis = 300),
        label = "haloStroke",
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(fillColor, shape)
            .border(1.dp, strokeColor, shape),
    )
}

@Preview(showBackground = true)
@Composable
private fun OrganicBeaconHaloPreview() {
    LabTheme {
        Surface {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OrganicBeaconHalo(
                    online = true,
                    shape = OrganicShapes.SmoothCookie6,
                    modifier = Modifier.size(64.dp),
                )
                OrganicBeaconHalo(
                    online = true,
                    stale = true,
                    shape = OrganicShapes.SmoothCookie8,
                    modifier = Modifier.size(64.dp),
                )
                OrganicBeaconHalo(
                    online = false,
                    shape = OrganicShapes.SmoothCookie12,
                    modifier = Modifier.size(64.dp),
                )
            }
        }
    }
}
