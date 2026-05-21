package it.attendance100.mybicocca.ui.component.shape

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Star-of-petals polygon: alternates between an outer and inner radius around the bbox.
// Mirrors petal(n, inner, outer) from the Botanical course design.
class PetalShape(
    private val petals: Int,
    private val inner: Float,
    private val outer: Float = 1f,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val rx = size.width / 2f
        val ry = size.height / 2f
        val total = petals * 2
        for (i in 0 until total) {
            val a = (i.toFloat() / total) * (2 * PI.toFloat()) - PI.toFloat() / 2f
            val r = if (i % 2 == 0) outer else inner
            val x = cx + cos(a) * rx * r
            val y = cy + sin(a) * ry * r
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return Outline.Generic(path)
    }
}

// Smoothed cookie: r(θ) = 1 − dip · (1 − cos(n·θ)) / 2 — gentle lobes, no spikes.
class SmoothCookieShape(
    private val lobes: Int,
    private val dip: Float,
    private val samples: Int = 144,
    private val rotationRadians: Float = 0f,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val rx = size.width / 2f
        val ry = size.height / 2f
        for (i in 0 until samples) {
            val a = (i.toFloat() / samples) * (2 * PI.toFloat()) - PI.toFloat() / 2f + rotationRadians
            val r = 1f - dip * (1f - cos(lobes * (a - rotationRadians))) / 2f
            val x = cx + cos(a) * rx * r
            val y = cy + sin(a) * ry * r
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return Outline.Generic(path)
    }
}

// Catalog of named shapes used across the elearning surfaces. Names match the JSX so
// porting individual elements stays mechanical.
object OrganicShapes {
    val Cookie: Shape = PetalShape(petals = 12, inner = 0.86f)
    val Sunny: Shape = PetalShape(petals = 8, inner = 0.78f)
    val Puffy: Shape = PetalShape(petals = 6, inner = 0.85f)
    val Burst: Shape = PetalShape(petals = 15, inner = 0.84f)
    val Leaf: Shape = PetalShape(petals = 4, inner = 0.40f)

    val SmoothCookie6: Shape = SmoothCookieShape(lobes = 6, dip = 0.20f, rotationRadians = (PI / 2).toFloat())
    val SmoothCookie4: Shape = SmoothCookieShape(lobes = 4, dip = 0.18f)
    val SmoothCookie12: Shape = SmoothCookieShape(lobes = 12, dip = 0.10f, samples = 192)
}
