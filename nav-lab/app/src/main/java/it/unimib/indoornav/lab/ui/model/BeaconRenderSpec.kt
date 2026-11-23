package it.unimib.indoornav.lab.ui.model

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

data class PoseAngles(
    val pitchDeg: Float,
    val yawDeg: Float,
    val rollDeg: Float,
)

data class Framing(
    val pxPerMm: Float,
    val fovScale: Float,
    val cx: Float,
    val cy: Float,
    val midProjX: Float,
    val midProjY: Float,
)

data class ProjectedPoint(
    val x: Float,
    val y: Float,
    val z: Float,
    val invZCam: Float,
)

object BeaconRenderSpec {
    val CARD_POSE = PoseAngles(pitchDeg = 0f, yawDeg = 0f, rollDeg = 0f)
    val DETAIL_POSE = PoseAngles(pitchDeg = -58f, yawDeg = -12f, rollDeg = 56f)

    const val CAM_DIST = 250f
    const val DECAL_VIRTUAL_W = 380f
    const val DECAL_VIRTUAL_H = 440f

    private const val DEG_TO_RAD = (PI / 180.0).toFloat()

    // Bounds of the Confidex Viking Slim hull in mm used for consistent camera auto-framing.
    private val BBOX_CORNERS = arrayOf(
        floatArrayOf(-16.2f, -37.5f, -7.3f),
        floatArrayOf(16.2f, -37.5f, -7.3f),
        floatArrayOf(-16.2f, 37.5f, -7.3f),
        floatArrayOf(16.2f, 37.5f, -7.3f),
        floatArrayOf(-16.2f, -37.5f, 7.2f),
        floatArrayOf(16.2f, -37.5f, 7.2f),
        floatArrayOf(-16.2f, 37.5f, 7.2f),
        floatArrayOf(16.2f, 37.5f, 7.2f),
    )

    // Row-major Ry(yaw) * Rx(pitch) * Rz(roll) matching beacon-3d.js.
    fun makeRotationMatrix(pitchDeg: Float, yawDeg: Float, rollDeg: Float): FloatArray {
        val px = pitchDeg * DEG_TO_RAD
        val py = yawDeg * DEG_TO_RAD
        val pz = rollDeg * DEG_TO_RAD
        val cx = cos(px)
        val sx = sin(px)
        val cy = cos(py)
        val sy = sin(py)
        val cz = cos(pz)
        val sz = sin(pz)
        return floatArrayOf(
            cy * cz + sy * sx * sz, -cy * sz + sy * sx * cz, sy * cx,
            cx * sz, cx * cz, -sx,
            -sy * cz + cy * sx * sz, sy * sz + cy * sx * cz, cy * cx,
        )
    }

    fun makeGlModelMatrix(rotM: FloatArray): FloatArray {
        return floatArrayOf(
            rotM[0], rotM[3], rotM[6], 0f,
            rotM[1], rotM[4], rotM[7], 0f,
            rotM[2], rotM[5], rotM[8], 0f,
            0f, 0f, 0f, 1f,
        )
    }

    fun computeFraming(
        rotM: FloatArray,
        widthPx: Float,
        heightPx: Float,
        zoom: Float = 1f,
    ): Framing {
        var minX = Float.POSITIVE_INFINITY
        var maxX = Float.NEGATIVE_INFINITY
        var minY = Float.POSITIVE_INFINITY
        var maxY = Float.NEGATIVE_INFINITY
        val m0 = rotM[0]
        val m1 = rotM[1]
        val m2 = rotM[2]
        val m3 = rotM[3]
        val m4 = rotM[4]
        val m5 = rotM[5]
        val m6 = rotM[6]
        val m7 = rotM[7]
        val m8 = rotM[8]

        for (pt in BBOX_CORNERS) {
            val tx = m0 * pt[0] + m1 * pt[1] + m2 * pt[2]
            val ty = m3 * pt[0] + m4 * pt[1] + m5 * pt[2]
            val tz = m6 * pt[0] + m7 * pt[1] + m8 * pt[2]
            val k = CAM_DIST / max(CAM_DIST - tz, 30f)
            val px = tx * k
            val py = -ty * k
            if (px < minX) minX = px
            if (px > maxX) maxX = px
            if (py < minY) minY = py
            if (py > maxY) maxY = py
        }
        val spanX = max(10f, maxX - minX)
        val spanY = max(10f, maxY - minY)
        val pxPerMm = min((widthPx * 0.90f) / spanX, (heightPx * 0.88f) / spanY) * zoom
        val fovScale = pxPerMm * CAM_DIST
        val midProjX = ((minX + maxX) * 0.5f) * pxPerMm
        val midProjY = ((minY + maxY) * 0.5f) * pxPerMm
        return Framing(
            pxPerMm = pxPerMm,
            fovScale = fovScale,
            cx = widthPx * 0.5f - midProjX,
            cy = heightPx * 0.5f - midProjY,
            midProjX = midProjX,
            midProjY = midProjY,
        )
    }

    // Column-major view-projection matrix placing the camera at (0, 0, CAM_DIST) looking down -Z.
    fun makeGlViewProjMatrix(
        framing: Framing,
        widthPx: Float,
        heightPx: Float,
        near: Float = 50f,
        far: Float = 450f,
    ): FloatArray {
        val sx = (2f * framing.fovScale) / widthPx
        val sy = (2f * framing.fovScale) / heightPx
        val ox = (2f * framing.midProjX) / widthPx
        val oy = (2f * framing.midProjY) / heightPx
        val a = (far + near) / (far - near)
        val b = (2f * far * near) / (far - near)
        return floatArrayOf(
            sx, 0f, 0f, 0f,
            0f, sy, 0f, 0f,
            ox, -oy, -a, -1f,
            -ox * CAM_DIST, oy * CAM_DIST, a * CAM_DIST - b, CAM_DIST,
        )
    }

    fun projectPoint(
        x: Float,
        y: Float,
        z: Float,
        rotM: FloatArray,
        framing: Framing,
    ): ProjectedPoint {
        val tx = rotM[0] * x + rotM[1] * y + rotM[2] * z
        val ty = rotM[3] * x + rotM[4] * y + rotM[5] * z
        val tz = rotM[6] * x + rotM[7] * y + rotM[8] * z
        val zCam = max(CAM_DIST - tz, 20f)
        val invZ = 1f / zCam
        val k = framing.fovScale * invZ
        return ProjectedPoint(
            x = framing.cx + tx * k,
            y = framing.cy - ty * k,
            z = tz,
            invZCam = invZ,
        )
    }

    fun plateauCorners(profile: VikingProfile): Array<FloatArray> {
        val zDecal = profile.plateauZ + 0.08f
        val halfW = profile.plateauHalfW - 0.10f
        val halfH = profile.bandHalfH - 0.20f
        return arrayOf(
            floatArrayOf(-halfW, halfH, zDecal),
            floatArrayOf(halfW, halfH, zDecal),
            floatArrayOf(halfW, -halfH, zDecal),
            floatArrayOf(-halfW, -halfH, zDecal),
        )
    }

    fun signedArea2D(corners: Array<ProjectedPoint>): Float {
        var area2 = 0f
        for (i in corners.indices) {
            val a = corners[i]
            val b = corners[(i + 1) % corners.size]
            area2 += a.x * b.y - b.x * a.y
        }
        return area2
    }

    fun drawLabelContent(
        canvas: Canvas,
        width: Float,
        height: Float,
        label: String,
        subLabel: String,
    ) {
        val mainText = label.trim().ifEmpty { "—" }
        val subText = subLabel.trim()

        val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3.0f
            color = Color.argb(77, 24, 22, 26)
        }
        val ticks = Path().apply {
            moveTo(14f, 34f); lineTo(14f, 16f); lineTo(32f, 16f)
            moveTo(width - 14f, 34f); lineTo(width - 14f, 16f); lineTo(width - 32f, 16f)
            moveTo(14f, height - 34f); lineTo(14f, height - 16f); lineTo(32f, height - 16f)
            moveTo(width - 14f, height - 34f); lineTo(width - 14f, height - 16f); lineTo(width - 32f, height - 16f)
        }
        canvas.drawPath(ticks, tickPaint)

        val boldTypeface = Typeface.create(Typeface.SANS_SERIF, 800, false)
        val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = boldTypeface
            textAlign = Paint.Align.CENTER
            color = Color.rgb(23, 21, 24)
            textSize = 180f
        }
        val measuredMain = max(1f, mainPaint.measureText(mainText))
        mainPaint.textSize = min(200f, 180f * ((width * 0.92f) / measuredMain))
        val mainYOffset = if (subText.isNotEmpty()) -28f else 0f
        val mainFm = mainPaint.fontMetrics
        val mainBaseline = height * 0.5f + mainYOffset - (mainFm.ascent + mainFm.descent) * 0.5f
        canvas.drawText(mainText, width * 0.5f, mainBaseline, mainPaint)

        if (subText.isNotEmpty()) {
            val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = boldTypeface
                textAlign = Paint.Align.CENTER
                color = Color.argb(219, 23, 21, 24)
                textSize = 48f
            }
            val measuredSub = max(1f, subPaint.measureText(subText))
            subPaint.textSize = (48f * ((width * 0.72f) / measuredSub)).coerceIn(24f, 40f)
            val subFm = subPaint.fontMetrics
            val subBaseline = height - 56f - (subFm.ascent + subFm.descent) * 0.5f
            canvas.drawText(subText, width * 0.5f, subBaseline, subPaint)
        }
    }

    fun renderLabelBitmap(
        label: String,
        subLabel: String,
        width: Int = 512,
        height: Int = 592,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.scale(width / DECAL_VIRTUAL_W, height / DECAL_VIRTUAL_H)
        drawLabelContent(canvas, DECAL_VIRTUAL_W, DECAL_VIRTUAL_H, label, subLabel)
        return bitmap
    }
}
