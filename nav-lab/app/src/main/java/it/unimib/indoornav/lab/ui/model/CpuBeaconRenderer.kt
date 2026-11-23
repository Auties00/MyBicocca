package it.unimib.indoornav.lab.ui.model

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

object CpuBeaconRenderer {

    private const val SUPERSAMPLE = 2

    private const val BASE_R = 243f
    private const val BASE_G = 244f
    private const val BASE_B = 246f
    private const val SPEC_STRENGTH = 0.20f

    private val KEY_DIR = norm3(0.45f, 0.62f, 0.65f)
    private val FILL_DIR = norm3(-0.60f, -0.22f, 0.48f)
    private val RIM_DIR = norm3(-0.30f, 0.40f, -0.85f)
    private val HALF_VEC = norm3(KEY_DIR[0], KEY_DIR[1], KEY_DIR[2] + 1f)

    // Warm studio fill/ground colors matching three-d-stage.js.
    private const val GROUND_R = 0.847f
    private const val GROUND_G = 0.824f
    private const val GROUND_B = 0.769f
    private const val FILL_R = 1.0f
    private const val FILL_G = 0.957f
    private const val FILL_B = 0.902f

    class ScratchBuffers {
        private var vertexCap = 0
        var screenX = FloatArray(0)
            private set
        var screenY = FloatArray(0)
            private set
        var invZ = FloatArray(0)
            private set
        var colR = IntArray(0)
            private set
        var colG = IntArray(0)
            private set
        var colB = IntArray(0)
            private set

        var rasW = 0
            private set
        var rasH = 0
            private set
        var pixels = IntArray(0)
            private set
        var zBuf = FloatArray(0)
            private set
        var rasBitmap: Bitmap? = null
            private set

        fun ensureVertices(count: Int) {
            if (vertexCap < count) {
                vertexCap = count
                screenX = FloatArray(count)
                screenY = FloatArray(count)
                invZ = FloatArray(count)
                colR = IntArray(count)
                colG = IntArray(count)
                colB = IntArray(count)
            }
        }

        fun ensureRaster(w: Int, h: Int) {
            val size = w * h
            if (rasW != w || rasH != h) {
                rasW = w
                rasH = h
                if (pixels.size < size) {
                    pixels = IntArray(size)
                    zBuf = FloatArray(size)
                }
                rasBitmap?.recycle()
                rasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            }
            pixels.fill(0, 0, size)
            zBuf.fill(0f, 0, size)
        }
    }

    data class CpuRenderResult(
        val bitmap: Bitmap,
        val imageBitmap: ImageBitmap,
        val renderTimeNs: Long,
    )

    suspend fun renderToImageBitmap(
        mesh: VikingMesh,
        label: String,
        subLabel: String,
        pose: PoseAngles,
        widthPx: Int,
        heightPx: Int,
        zoom: Float = 1f,
        scratch: ScratchBuffers? = null,
    ): CpuRenderResult = withContext(Dispatchers.Default) {
        val res = renderBitmap(
            mesh = mesh,
            label = label,
            subLabel = subLabel,
            pose = pose,
            widthPx = widthPx,
            heightPx = heightPx,
            zoom = zoom,
            scratch = scratch ?: ScratchBuffers(),
        )
        res
    }

    fun renderBitmap(
        mesh: VikingMesh,
        label: String,
        subLabel: String,
        pose: PoseAngles,
        widthPx: Int,
        heightPx: Int,
        zoom: Float = 1f,
        scratch: ScratchBuffers = ScratchBuffers(),
        recycleTarget: Bitmap? = null,
    ): CpuRenderResult {
        val outW = widthPx.coerceAtLeast(1)
        val outH = heightPx.coerceAtLeast(1)
        val rasW = (outW * SUPERSAMPLE).coerceAtLeast(2)
        val rasH = (outH * SUPERSAMPLE).coerceAtLeast(2)

        val startNs = System.nanoTime()

        val rotM = BeaconRenderSpec.makeRotationMatrix(pose.pitchDeg, pose.yawDeg, pose.rollDeg)
        val framingOut = BeaconRenderSpec.computeFraming(rotM, outW.toFloat(), outH.toFloat(), zoom)
        val fovScaleRas = framingOut.fovScale * SUPERSAMPLE
        val cxRas = framingOut.cx * SUPERSAMPLE
        val cyRas = framingOut.cy * SUPERSAMPLE

        val vCount = mesh.vertexCount
        scratch.ensureVertices(vCount)
        scratch.ensureRaster(rasW, rasH)

        val pos = mesh.positions
        val norm = mesh.normals
        val sx = scratch.screenX
        val sy = scratch.screenY
        val invZ = scratch.invZ
        val colR = scratch.colR
        val colG = scratch.colG
        val colB = scratch.colB

        val m0 = rotM[0]
        val m1 = rotM[1]
        val m2 = rotM[2]
        val m3 = rotM[3]
        val m4 = rotM[4]
        val m5 = rotM[5]
        val m6 = rotM[6]
        val m7 = rotM[7]
        val m8 = rotM[8]

        for (i in 0 until vCount) {
            val i3 = i * 3
            val x = pos[i3]
            val y = pos[i3 + 1]
            val z = pos[i3 + 2]
            val tx = m0 * x + m1 * y + m2 * z
            val ty = m3 * x + m4 * y + m5 * z
            val tz = m6 * x + m7 * y + m8 * z
            val zCam = max(BeaconRenderSpec.CAM_DIST - tz, 20f)
            val iz = 1f / zCam
            val k = fovScaleRas * iz
            sx[i] = cxRas + tx * k
            sy[i] = cyRas - ty * k
            invZ[i] = iz

            val lnx = norm[i3]
            val lny = norm[i3 + 1]
            val lnz = norm[i3 + 2]
            val nx = m0 * lnx + m1 * lny + m2 * lnz
            val ny = m3 * lnx + m4 * lny + m5 * lnz
            val nz = m6 * lnx + m7 * lny + m8 * lnz

            val nDotK = nx * KEY_DIR[0] + ny * KEY_DIR[1] + nz * KEY_DIR[2]
            val nDotF = nx * FILL_DIR[0] + ny * FILL_DIR[1] + nz * FILL_DIR[2]
            val nDotR = nx * RIM_DIR[0] + ny * RIM_DIR[1] + nz * RIM_DIR[2]
            val nDotH = nx * HALF_VEC[0] + ny * HALF_VEC[1] + nz * HALF_VEC[2]

            val hemiFactor = (0.58f + 0.42f * ny) * 0.14f
            val skyMix = (0.5f + 0.5f * ny).coerceIn(0f, 1f)
            val hemiR = GROUND_R + (1f - GROUND_R) * skyMix
            val hemiG = GROUND_G + (1f - GROUND_G) * skyMix
            val hemiB = GROUND_B + (1f - GROUND_B) * skyMix

            val kTerm = if (nDotK > 0f) nDotK * 0.34f else 0f
            val fTerm = if (nDotF > 0f) nDotF * 0.12f else 0f
            val rTerm = if (nDotR > 0f) nDotR * 0.10f else 0f
            val spec = if (nDotH > 0f) nDotH.pow(26f) * SPEC_STRENGTH * 255f else 0f

            val litR = (0.56f + hemiR * hemiFactor + kTerm + FILL_R * fTerm + rTerm) * 0.94f
            val litG = (0.56f + hemiG * hemiFactor + kTerm + FILL_G * fTerm + rTerm) * 0.94f
            val litB = (0.56f + hemiB * hemiFactor + kTerm + FILL_B * fTerm + rTerm) * 0.94f

            colR[i] = (BASE_R * litR + spec + 0.5f).toInt().coerceIn(0, 255)
            colG[i] = (BASE_G * litG + spec + 0.5f).toInt().coerceIn(0, 255)
            colB[i] = (BASE_B * litB + spec + 0.5f).toInt().coerceIn(0, 255)
        }

        val pixels = scratch.pixels
        val zBuf = scratch.zBuf
        val indices = mesh.indices
        val iLen = indices.size
        var t = 0
        while (t < iLen) {
            val i0 = indices[t].toInt() and 0xFFFF
            val i1 = indices[t + 1].toInt() and 0xFFFF
            val i2 = indices[t + 2].toInt() and 0xFFFF
            rasterizeGouraudTriangle(
                rasW, rasH, pixels, zBuf,
                sx[i0], sy[i0], invZ[i0], colR[i0], colG[i0], colB[i0],
                sx[i1], sy[i1], invZ[i1], colR[i1], colG[i1], colB[i1],
                sx[i2], sy[i2], invZ[i2], colR[i2], colG[i2], colB[i2],
            )
            t += 3
        }

        val rasBitmap = scratch.rasBitmap!!
        rasBitmap.setPixels(pixels, 0, rasW, 0, 0, rasW, rasH)

        val outBitmap = if (recycleTarget != null &&
            recycleTarget.width == outW &&
            recycleTarget.height == outH &&
            recycleTarget.isMutable
        ) {
            recycleTarget.eraseColor(0)
            recycleTarget
        } else {
            Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
        }

        val outCanvas = Canvas(outBitmap)
        val blitPaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
        outCanvas.drawBitmap(
            rasBitmap,
            Rect(0, 0, rasW, rasH),
            Rect(0, 0, outW, outH),
            blitPaint,
        )

        val plateauModelCorners = BeaconRenderSpec.plateauCorners(mesh.profile)
        val projectedCorners = Array(4) { idx ->
            val c = plateauModelCorners[idx]
            BeaconRenderSpec.projectPoint(c[0], c[1], c[2], rotM, framingOut)
        }
        BeaconRenderSpec.drawProjectedLabel(outCanvas, label, subLabel, projectedCorners)

        val elapsedNs = System.nanoTime() - startNs
        return CpuRenderResult(
            bitmap = outBitmap,
            imageBitmap = outBitmap.asImageBitmap(),
            renderTimeNs = elapsedNs,
        )
    }

    private fun rasterizeGouraudTriangle(
        w: Int,
        h: Int,
        pixels: IntArray,
        zBuf: FloatArray,
        x0: Float, y0: Float, z0: Float, r0: Int, g0: Int, b0: Int,
        x1: Float, y1: Float, z1: Float, r1: Int, g1: Int, b1: Int,
        x2: Float, y2: Float, z2: Float, r2: Int, g2: Int, b2: Int,
    ) {
        val area = (x1 - x0) * (y2 - y0) - (y1 - y0) * (x2 - x0)
        // Standard 3D CCW triangles have negative signed area in Y-down screen coordinates.
        if (area >= -1e-3f) return
        val invArea = 1.0f / area

        val minX = max(0, floor(min(x0, min(x1, x2))).toInt())
        val maxX = min(w - 1, ceil(max(x0, max(x1, x2))).toInt())
        val minY = max(0, floor(min(y0, min(y1, y2))).toInt())
        val maxY = min(h - 1, ceil(max(y0, max(y1, y2))).toInt())
        if (minX > maxX || minY > maxY) return

        val e0dx = -(y2 - y1) * invArea
        val e0dy = (x2 - x1) * invArea
        val e1dx = -(y0 - y2) * invArea
        val e1dy = (x0 - x2) * invArea

        val startPx = minX + 0.5f
        val startPy = minY + 0.5f
        var w0Row = ((x1 - startPx) * (y2 - startPy) - (y1 - startPy) * (x2 - startPx)) * invArea
        var w1Row = ((x2 - startPx) * (y0 - startPy) - (y2 - startPy) * (x0 - startPx)) * invArea

        for (y in minY..maxY) {
            var w0 = w0Row
            var w1 = w1Row
            var rowIdx = y * w + minX
            for (x in minX..maxX) {
                val w2 = 1.0f - w0 - w1
                if (w0 >= -1e-4f && w1 >= -1e-4f && w2 >= -1e-4f) {
                    val z = w0 * z0 + w1 * z1 + w2 * z2
                    if (z > zBuf[rowIdx]) {
                        zBuf[rowIdx] = z
                        val r = (w0 * r0 + w1 * r1 + w2 * r2 + 0.5f).toInt().coerceIn(0, 255)
                        val g = (w0 * g0 + w1 * g1 + w2 * g2 + 0.5f).toInt().coerceIn(0, 255)
                        val b = (w0 * b0 + w1 * b1 + w2 * b2 + 0.5f).toInt().coerceIn(0, 255)
                        pixels[rowIdx] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
                    }
                }
                w0 += e0dx
                w1 += e1dx
                rowIdx++
            }
            w0Row += e0dy
            w1Row += e1dy
        }
    }

    private fun norm3(x: Float, y: Float, z: Float): FloatArray {
        val len = sqrt(x * x + y * y + z * z).coerceAtLeast(1e-6f)
        return floatArrayOf(x / len, y / len, z / len)
    }
}
