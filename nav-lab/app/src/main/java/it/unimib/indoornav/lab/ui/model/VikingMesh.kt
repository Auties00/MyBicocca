package it.unimib.indoornav.lab.ui.model

import android.content.Context
import it.unimib.indoornav.lab.R
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class VikingProfile(
    val widthMm: Float = 32.0f,
    val heightMm: Float = 74.63f,
    val thicknessMm: Float = 14.37f,
    val plateauZ: Float = 6.79f,
    val plateauHalfW: Float = 13.85f,
    val midZ: Float = 3.57f,
    val midHalfW: Float = 15.36f,
    val baseZ: Float = -7.19f,
    val baseHalfW: Float = 16.0f,
    val bandHalfH: Float = 16.5f,
    val holeY: Float = 31.34f,
    val holeR: Float = 3.12f,
)

class VikingMesh(
    val vertexCount: Int,
    val indexCount: Int,
    val profile: VikingProfile,
    val positions: FloatArray,
    val normals: FloatArray,
    val interleavedPosNorm: FloatArray,
    val indices: ShortArray,
) {
    companion object {
        private const val MAGIC = "VKSM"
        private const val SUPPORTED_VERSION = 1

        @Volatile
        private var cached: VikingMesh? = null

        fun load(context: Context): VikingMesh {
            cached?.let { return it }
            synchronized(this) {
                cached?.let { return it }
                val bytes = context.resources.openRawResource(R.raw.viking_slim).use { it.readBytes() }
                return parse(bytes).also { cached = it }
            }
        }

        fun parse(bytes: ByteArray): VikingMesh {
            require(bytes.size >= 20) { "viking_slim.mesh truncated (${bytes.size} bytes)" }
            val magic = String(bytes, 0, 4, Charsets.US_ASCII)
            require(magic == MAGIC) { "Invalid mesh magic: $magic" }

            val buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            buf.position(4)
            val version = buf.int
            require(version == SUPPORTED_VERSION) { "Unsupported mesh version: $version" }

            val vertexCount = buf.int
            val indexCount = buf.int
            val profileCount = buf.int
            require(vertexCount > 0 && indexCount > 0 && profileCount >= 0) {
                "Invalid mesh counts: v=$vertexCount i=$indexCount p=$profileCount"
            }

            val expectedSize = 20 + profileCount * 4 + vertexCount * 6 * 4 + indexCount * 2
            require(bytes.size >= expectedSize) {
                "viking_slim.mesh truncated: expected $expectedSize bytes, got ${bytes.size}"
            }

            val profileValues = FloatArray(profileCount) { buf.float }
            val profile = VikingProfile(
                widthMm = profileValues.getOrElse(0) { 32.0f },
                heightMm = profileValues.getOrElse(1) { 74.63f },
                thicknessMm = profileValues.getOrElse(2) { 14.37f },
                plateauZ = profileValues.getOrElse(3) { 6.79f },
                plateauHalfW = profileValues.getOrElse(4) { 13.85f },
                midZ = profileValues.getOrElse(5) { 3.57f },
                midHalfW = profileValues.getOrElse(6) { 15.36f },
                baseZ = profileValues.getOrElse(7) { -7.19f },
                baseHalfW = profileValues.getOrElse(8) { 16.0f },
                bandHalfH = profileValues.getOrElse(9) { 16.5f },
                holeY = profileValues.getOrElse(10) { 31.34f },
                holeR = profileValues.getOrElse(11) { 3.12f },
            )

            val positions = FloatArray(vertexCount * 3)
            val normals = FloatArray(vertexCount * 3)
            val interleaved = FloatArray(vertexCount * 6)
            for (i in 0 until vertexCount) {
                val x = buf.float
                val y = buf.float
                val z = buf.float
                val nx = buf.float
                val ny = buf.float
                val nz = buf.float
                val i3 = i * 3
                positions[i3] = x
                positions[i3 + 1] = y
                positions[i3 + 2] = z
                normals[i3] = nx
                normals[i3 + 1] = ny
                normals[i3 + 2] = nz
                val i6 = i * 6
                interleaved[i6] = x
                interleaved[i6 + 1] = y
                interleaved[i6 + 2] = z
                interleaved[i6 + 3] = nx
                interleaved[i6 + 4] = ny
                interleaved[i6 + 5] = nz
            }

            val indices = ShortArray(indexCount) { buf.short }
            return VikingMesh(
                vertexCount = vertexCount,
                indexCount = indexCount,
                profile = profile,
                positions = positions,
                normals = normals,
                interleavedPosNorm = interleaved,
                indices = indices,
            )
        }
    }
}
