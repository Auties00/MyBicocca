package it.unimib.indoornav.lab.ui.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class BeaconModelMathTest {

    private fun loadMeshFile(): ByteArray {
        val candidates = listOf(
            File("src/main/res/raw/viking_slim.mesh"),
            File("app/src/main/res/raw/viking_slim.mesh"),
        )
        val file = candidates.firstOrNull { it.exists() }
            ?: error("Could not locate viking_slim.mesh from ${File(".").absolutePath}")
        return file.readBytes()
    }

    @Test
    fun `viking mesh parses expected vertex count index count and profile`() {
        val mesh = VikingMesh.parse(loadMeshFile())
        assertEquals(6741, mesh.vertexCount)
        assertEquals(24732, mesh.indexCount)
        assertEquals(32.0f, mesh.profile.widthMm, 0.01f)
        assertEquals(6.79f, mesh.profile.plateauZ, 0.01f)
        assertEquals(16.5f, mesh.profile.bandHalfH, 0.01f)
        assertEquals(mesh.vertexCount * 3, mesh.positions.size)
        assertEquals(mesh.vertexCount * 3, mesh.normals.size)
        assertEquals(mesh.vertexCount * 6, mesh.interleavedPosNorm.size)
    }

    @Test
    fun `projected points and the gl viewProj matrix produce matching screen coordinates`() {
        val width = 288f
        val height = 528f
        val pose = BeaconRenderSpec.DETAIL_POSE
        val rotM = BeaconRenderSpec.makeRotationMatrix(pose.pitchDeg, pose.yawDeg, pose.rollDeg)
        val framing = BeaconRenderSpec.computeFraming(rotM, width, height)
        val glModel = BeaconRenderSpec.makeGlModelMatrix(rotM)
        val glViewProj = BeaconRenderSpec.makeGlViewProjMatrix(framing, width, height)

        val testPt = floatArrayOf(11.5f, -14.2f, 6.87f)
        val cpuProj = BeaconRenderSpec.projectPoint(testPt[0], testPt[1], testPt[2], rotM, framing)

        val wx = glModel[0] * testPt[0] + glModel[4] * testPt[1] + glModel[8] * testPt[2] + glModel[12]
        val wy = glModel[1] * testPt[0] + glModel[5] * testPt[1] + glModel[9] * testPt[2] + glModel[13]
        val wz = glModel[2] * testPt[0] + glModel[6] * testPt[1] + glModel[10] * testPt[2] + glModel[14]

        val cx = glViewProj[0] * wx + glViewProj[4] * wy + glViewProj[8] * wz + glViewProj[12]
        val cy = glViewProj[1] * wx + glViewProj[5] * wy + glViewProj[9] * wz + glViewProj[13]
        val cw = glViewProj[3] * wx + glViewProj[7] * wy + glViewProj[11] * wz + glViewProj[15]

        val glScreenX = (cx / cw + 1f) * 0.5f * width
        val glScreenY = (1f - cy / cw) * 0.5f * height

        assertEquals(cpuProj.x, glScreenX, 0.02f)
        assertEquals(cpuProj.y, glScreenY, 0.02f)
    }

    @Test
    fun `plateau signed area is positive when facing camera and negative when facing away`() {
        val mesh = VikingMesh.parse(loadMeshFile())
        val corners = BeaconRenderSpec.plateauCorners(mesh.profile)

        fun areaFor(pose: PoseAngles): Float {
            val rotM = BeaconRenderSpec.makeRotationMatrix(pose.pitchDeg, pose.yawDeg, pose.rollDeg)
            val framing = BeaconRenderSpec.computeFraming(rotM, 200f, 360f)
            val projected = Array(4) { i ->
                val c = corners[i]
                BeaconRenderSpec.projectPoint(c[0], c[1], c[2], rotM, framing)
            }
            return BeaconRenderSpec.signedArea2D(projected)
        }

        assertTrue(areaFor(BeaconRenderSpec.CARD_POSE) > 0f)
        assertTrue(areaFor(BeaconRenderSpec.DETAIL_POSE) > 0f)
        assertTrue(areaFor(PoseAngles(0f, 180f, 0f)) < 0f)
    }
}
