package it.unimib.indoornav.lab.ui.model

import android.graphics.Bitmap
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLExt
import android.opengl.EGLSurface
import android.opengl.GLES30
import android.opengl.GLUtils
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max
import kotlin.math.min

private const val TAG = "GlBeaconRenderer"

private class GlSceneResources(private val mesh: VikingMesh) {
    private var hullProgram = 0
    private var decalProgram = 0
    private var hullVao = 0
    private var hullVbo = 0
    private var hullIbo = 0
    private var decalVao = 0
    private var decalVbo = 0
    private var labelTexId = 0

    private var uHullModel = -1
    private var uHullViewProj = -1
    private var uHullBaseColor = -1
    private var uHullCamPos = -1

    private var uDecalModel = -1
    private var uDecalViewProj = -1
    private var uDecalSampler = -1

    private var cachedLabelKey: String? = null

    fun init() {
        hullProgram = buildProgram(HULL_VERTEX_SHADER, HULL_FRAGMENT_SHADER)
        uHullModel = GLES30.glGetUniformLocation(hullProgram, "uModel")
        uHullViewProj = GLES30.glGetUniformLocation(hullProgram, "uViewProj")
        uHullBaseColor = GLES30.glGetUniformLocation(hullProgram, "uBaseColor")
        uHullCamPos = GLES30.glGetUniformLocation(hullProgram, "uCamPos")

        decalProgram = buildProgram(DECAL_VERTEX_SHADER, DECAL_FRAGMENT_SHADER)
        uDecalModel = GLES30.glGetUniformLocation(decalProgram, "uModel")
        uDecalViewProj = GLES30.glGetUniformLocation(decalProgram, "uViewProj")
        uDecalSampler = GLES30.glGetUniformLocation(decalProgram, "uLabelTex")

        val vaos = IntArray(2)
        GLES30.glGenVertexArrays(2, vaos, 0)
        hullVao = vaos[0]
        decalVao = vaos[1]

        val bufs = IntArray(3)
        GLES30.glGenBuffers(3, bufs, 0)
        hullVbo = bufs[0]
        hullIbo = bufs[1]
        decalVbo = bufs[2]

        GLES30.glBindVertexArray(hullVao)
        val vFloatBuf: FloatBuffer = ByteBuffer
            .allocateDirect(mesh.interleavedPosNorm.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(mesh.interleavedPosNorm)
                position(0)
            }
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, hullVbo)
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            mesh.interleavedPosNorm.size * 4,
            vFloatBuf,
            GLES30.GL_STATIC_DRAW,
        )

        val iShortBuf: ShortBuffer = ByteBuffer
            .allocateDirect(mesh.indices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply {
                put(mesh.indices)
                position(0)
            }
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, hullIbo)
        GLES30.glBufferData(
            GLES30.GL_ELEMENT_ARRAY_BUFFER,
            mesh.indices.size * 2,
            iShortBuf,
            GLES30.GL_STATIC_DRAW,
        )

        val stride = 6 * 4
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, stride, 0)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, stride, 3 * 4)
        GLES30.glBindVertexArray(0)

        val corners = BeaconRenderSpec.plateauCorners(mesh.profile)
        val pTL = corners[0]
        val pTR = corners[1]
        val pBR = corners[2]
        val pBL = corners[3]
        // Two CCW triangles facing +Z matching Bitmap top-left UV (0, 0).
        val decalVerts = floatArrayOf(
            pTL[0], pTL[1], pTL[2], 0f, 0f,
            pBL[0], pBL[1], pBL[2], 0f, 1f,
            pBR[0], pBR[1], pBR[2], 1f, 1f,
            pTL[0], pTL[1], pTL[2], 0f, 0f,
            pBR[0], pBR[1], pBR[2], 1f, 1f,
            pTR[0], pTR[1], pTR[2], 1f, 0f,
        )
        val decalBuf = ByteBuffer
            .allocateDirect(decalVerts.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(decalVerts)
                position(0)
            }

        GLES30.glBindVertexArray(decalVao)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, decalVbo)
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            decalVerts.size * 4,
            decalBuf,
            GLES30.GL_STATIC_DRAW,
        )
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 5 * 4, 0)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 5 * 4, 3 * 4)
        GLES30.glBindVertexArray(0)

        val texIds = IntArray(1)
        GLES30.glGenTextures(1, texIds, 0)
        labelTexId = texIds[0]
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, labelTexId)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
    }

    fun updateLabelIfNeeded(label: String, subLabel: String) {
        val key = "$label|$subLabel"
        if (key == cachedLabelKey) return
        cachedLabelKey = key
        val bmp = BeaconRenderSpec.renderLabelBitmap(label, subLabel)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, labelTexId)
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bmp, 0)
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
        bmp.recycle()
    }

    fun draw(
        widthPx: Int,
        heightPx: Int,
        pose: PoseAngles,
        label: String,
        subLabel: String,
        zoom: Float = 1f,
    ) {
        updateLabelIfNeeded(label, subLabel)

        val rotM = BeaconRenderSpec.makeRotationMatrix(pose.pitchDeg, pose.yawDeg, pose.rollDeg)
        val modelM = BeaconRenderSpec.makeGlModelMatrix(rotM)
        val framing = BeaconRenderSpec.computeFraming(rotM, widthPx.toFloat(), heightPx.toFloat(), zoom)
        val viewProjM = BeaconRenderSpec.makeGlViewProjMatrix(framing, widthPx.toFloat(), heightPx.toFloat())

        GLES30.glViewport(0, 0, widthPx, heightPx)
        GLES30.glClearColor(0f, 0f, 0f, 0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glDepthFunc(GLES30.GL_LEQUAL)
        GLES30.glDepthMask(true)
        GLES30.glEnable(GLES30.GL_CULL_FACE)
        GLES30.glCullFace(GLES30.GL_BACK)
        GLES30.glDisable(GLES30.GL_BLEND)
        GLES30.glDisable(GLES30.GL_POLYGON_OFFSET_FILL)

        GLES30.glUseProgram(hullProgram)
        GLES30.glUniformMatrix4fv(uHullModel, 1, false, modelM, 0)
        GLES30.glUniformMatrix4fv(uHullViewProj, 1, false, viewProjM, 0)
        GLES30.glUniform3f(uHullBaseColor, 243f / 255f, 244f / 255f, 246f / 255f)
        GLES30.glUniform3f(uHullCamPos, 0f, 0f, BeaconRenderSpec.CAM_DIST)

        GLES30.glBindVertexArray(hullVao)
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, mesh.indexCount, GLES30.GL_UNSIGNED_SHORT, 0)

        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFuncSeparate(
            GLES30.GL_SRC_ALPHA,
            GLES30.GL_ONE_MINUS_SRC_ALPHA,
            GLES30.GL_ZERO,
            GLES30.GL_ONE,
        )
        GLES30.glDepthMask(false)
        GLES30.glEnable(GLES30.GL_POLYGON_OFFSET_FILL)
        GLES30.glPolygonOffset(-2f, -2f)

        GLES30.glUseProgram(decalProgram)
        GLES30.glUniformMatrix4fv(uDecalModel, 1, false, modelM, 0)
        GLES30.glUniformMatrix4fv(uDecalViewProj, 1, false, viewProjM, 0)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, labelTexId)
        GLES30.glUniform1i(uDecalSampler, 0)

        GLES30.glBindVertexArray(decalVao)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6)

        GLES30.glBindVertexArray(0)
        GLES30.glDepthMask(true)
        GLES30.glDisable(GLES30.GL_POLYGON_OFFSET_FILL)
        GLES30.glDisable(GLES30.GL_BLEND)
    }

    fun release() {
        if (hullProgram != 0) GLES30.glDeleteProgram(hullProgram)
        if (decalProgram != 0) GLES30.glDeleteProgram(decalProgram)
        val vaos = intArrayOf(hullVao, decalVao)
        GLES30.glDeleteVertexArrays(2, vaos, 0)
        val bufs = intArrayOf(hullVbo, hullIbo, decalVbo)
        GLES30.glDeleteBuffers(3, bufs, 0)
        if (labelTexId != 0) {
            GLES30.glDeleteTextures(1, intArrayOf(labelTexId), 0)
        }
        hullProgram = 0
        decalProgram = 0
        hullVao = 0
        decalVao = 0
        hullVbo = 0
        hullIbo = 0
        decalVbo = 0
        labelTexId = 0
    }

    companion object {
        private const val HULL_VERTEX_SHADER = """#version 300 es
layout(location = 0) in vec3 aPos;
layout(location = 1) in vec3 aNorm;
uniform mat4 uModel;
uniform mat4 uViewProj;
out vec3 vWorldPos;
out vec3 vWorldNorm;
void main() {
    vec4 wp = uModel * vec4(aPos, 1.0);
    vWorldPos = wp.xyz;
    vWorldNorm = normalize(mat3(uModel) * aNorm);
    gl_Position = uViewProj * wp;
}
"""

        private const val HULL_FRAGMENT_SHADER = """#version 300 es
precision highp float;
in vec3 vWorldPos;
in vec3 vWorldNorm;
uniform vec3 uBaseColor;
uniform vec3 uCamPos;
out vec4 fragColor;
void main() {
    vec3 N = normalize(vWorldNorm);
    vec3 V = normalize(uCamPos - vWorldPos);
    vec3 keyDir = normalize(vec3(0.45, 0.62, 0.65));
    vec3 fillDir = normalize(vec3(-0.60, -0.22, 0.48));
    vec3 rimDir = normalize(vec3(-0.30, 0.40, -0.85));
    vec3 H = normalize(keyDir + V);

    float hemiFactor = (0.58 + 0.42 * N.y) * 0.14;
    vec3 hemiCol = mix(vec3(0.847, 0.824, 0.769), vec3(1.0), clamp(0.5 + 0.5 * N.y, 0.0, 1.0));
    vec3 fillCol = vec3(1.0, 0.957, 0.902);

    float nDotK = max(dot(N, keyDir), 0.0);
    float nDotF = max(dot(N, fillDir), 0.0);
    float nDotR = max(dot(N, rimDir), 0.0);
    float spec = pow(max(dot(N, H), 0.0), 26.0) * 0.20;

    vec3 lit = (vec3(0.56) + hemiCol * hemiFactor + vec3(nDotK * 0.34) + fillCol * (nDotF * 0.12) + vec3(nDotR * 0.10)) * 0.94;
    vec3 color = clamp(uBaseColor * lit + vec3(spec), 0.0, 1.0);
    fragColor = vec4(color, 1.0);
}
"""

        private const val DECAL_VERTEX_SHADER = """#version 300 es
layout(location = 0) in vec3 aPos;
layout(location = 1) in vec2 aTexCoord;
uniform mat4 uModel;
uniform mat4 uViewProj;
out vec2 vUv;
void main() {
    vUv = aTexCoord;
    gl_Position = uViewProj * (uModel * vec4(aPos, 1.0));
}
"""

        private const val DECAL_FRAGMENT_SHADER = """#version 300 es
precision highp float;
in vec2 vUv;
uniform sampler2D uLabelTex;
out vec4 fragColor;
void main() {
    vec4 tex = texture(uLabelTex, vUv);
    if (tex.a < 0.01) discard;
    fragColor = tex;
}
"""

        private fun compileShader(type: Int, src: String): Int {
            val shader = GLES30.glCreateShader(type)
            GLES30.glShaderSource(shader, src)
            GLES30.glCompileShader(shader)
            val status = IntArray(1)
            GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, status, 0)
            if (status[0] == 0) {
                val info = GLES30.glGetShaderInfoLog(shader)
                GLES30.glDeleteShader(shader)
                throw IllegalStateException("Shader compile failed: $info")
            }
            return shader
        }

        private fun buildProgram(vsSrc: String, fsSrc: String): Int {
            val vs = compileShader(GLES30.GL_VERTEX_SHADER, vsSrc)
            val fs = compileShader(GLES30.GL_FRAGMENT_SHADER, fsSrc)
            val prog = GLES30.glCreateProgram()
            GLES30.glAttachShader(prog, vs)
            GLES30.glAttachShader(prog, fs)
            GLES30.glLinkProgram(prog)
            GLES30.glDeleteShader(vs)
            GLES30.glDeleteShader(fs)
            val status = IntArray(1)
            GLES30.glGetProgramiv(prog, GLES30.GL_LINK_STATUS, status, 0)
            if (status[0] == 0) {
                val info = GLES30.glGetProgramInfoLog(prog)
                GLES30.glDeleteProgram(prog)
                throw IllegalStateException("Program link failed: $info")
            }
            return prog
        }
    }
}

private class EglHelper {
    var display: EGLDisplay = EGL14.EGL_NO_DISPLAY
        private set
    var context: EGLContext = EGL14.EGL_NO_CONTEXT
        private set
    var config: EGLConfig? = null
        private set

    fun init(surfaceTypeBit: Int, preferMsaa: Boolean) {
        display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        check(display != EGL14.EGL_NO_DISPLAY) { "eglGetDisplay failed" }
        val version = IntArray(2)
        check(EGL14.eglInitialize(display, version, 0, version, 1)) { "eglInitialize failed" }

        val chosen = chooseBestConfig(surfaceTypeBit, preferMsaa)
            ?: chooseBestConfig(surfaceTypeBit, preferMsaa = false)
            ?: throw IllegalStateException("No matching EGLConfig found")
        config = chosen

        val ctxAttribs = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 3, EGL14.EGL_NONE)
        context = EGL14.eglCreateContext(display, chosen, EGL14.EGL_NO_CONTEXT, ctxAttribs, 0)
        check(context != EGL14.EGL_NO_CONTEXT) { "eglCreateContext ES 3.0 failed" }
    }

    private fun chooseBestConfig(surfaceTypeBit: Int, preferMsaa: Boolean): EGLConfig? {
        val renderableFlag = EGLExt.EGL_OPENGL_ES3_BIT_KHR
        return chooseConfigWithFlags(surfaceTypeBit, renderableFlag, preferMsaa)
            ?: chooseConfigWithFlags(surfaceTypeBit, EGL14.EGL_OPENGL_ES2_BIT, preferMsaa)
    }

    private fun chooseConfigWithFlags(
        surfaceTypeBit: Int,
        renderableType: Int,
        msaa: Boolean,
    ): EGLConfig? {
        val attribs = mutableListOf(
            EGL14.EGL_RENDERABLE_TYPE, renderableType,
            EGL14.EGL_SURFACE_TYPE, surfaceTypeBit,
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_DEPTH_SIZE, 16,
        )
        if (msaa) {
            attribs.add(EGL14.EGL_SAMPLE_BUFFERS)
            attribs.add(1)
            attribs.add(EGL14.EGL_SAMPLES)
            attribs.add(4)
        }
        attribs.add(EGL14.EGL_NONE)
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        val ok = EGL14.eglChooseConfig(
            display,
            attribs.toIntArray(),
            0,
            configs,
            0,
            1,
            numConfigs,
            0,
        )
        return if (ok && numConfigs[0] > 0) configs[0] else null
    }

    fun createPbufferSurface(width: Int, height: Int): EGLSurface {
        val cfg = config ?: error("EGL not initialized")
        val surf = EGL14.eglCreatePbufferSurface(
            display,
            cfg,
            intArrayOf(
                EGL14.EGL_WIDTH, width,
                EGL14.EGL_HEIGHT, height,
                EGL14.EGL_NONE,
            ),
            0,
        )
        check(surf != EGL14.EGL_NO_SURFACE) { "eglCreatePbufferSurface failed" }
        return surf
    }

    fun makeCurrent(surface: EGLSurface) {
        check(EGL14.eglMakeCurrent(display, surface, surface, context)) { "eglMakeCurrent failed" }
    }

    fun swapBuffers(surface: EGLSurface) {
        EGL14.eglSwapBuffers(display, surface)
    }

    fun destroySurface(surface: EGLSurface) {
        if (surface != EGL14.EGL_NO_SURFACE) {
            EGL14.eglDestroySurface(display, surface)
        }
    }

    fun release() {
        if (display != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            if (context != EGL14.EGL_NO_CONTEXT) {
                EGL14.eglDestroyContext(display, context)
            }
            EGL14.eglTerminate(display)
        }
        display = EGL14.EGL_NO_DISPLAY
        context = EGL14.EGL_NO_CONTEXT
        config = null
    }
}

object GlBeaconOffscreenRenderer {

    private var thread: HandlerThread? = null
    private var handler: Handler? = null
    private val egl = EglHelper()
    private var pbufferSurface: EGLSurface = EGL14.EGL_NO_SURFACE
    private var pbufferW = 0
    private var pbufferH = 0
    private var scene: GlSceneResources? = null

    private var msaaFbo = 0
    private var msaaColorRb = 0
    private var msaaDepthRb = 0
    private var resolveFbo = 0
    private var resolveColorRb = 0
    private var fboW = 0
    private var fboH = 0

    data class GlRenderResult(
        val bitmap: Bitmap,
        val renderTimeNs: Long,
    )

    @Synchronized
    private fun ensureWorker(): Handler {
        handler?.let { return it }
        val ht = HandlerThread("LabGlOffscreenThread").apply { start() }
        thread = ht
        return Handler(ht.looper).also { handler = it }
    }

    fun renderToBitmap(
        mesh: VikingMesh,
        label: String,
        subLabel: String,
        pose: PoseAngles,
        widthPx: Int,
        heightPx: Int,
        zoom: Float = 1f,
    ): GlRenderResult {
        val w = widthPx.coerceAtLeast(1)
        val h = heightPx.coerceAtLeast(1)
        val worker = ensureWorker()
        val resultRef = AtomicReference<Result<GlRenderResult>>()
        val latch = CountDownLatch(1)

        worker.post {
            resultRef.set(
                runCatching {
                    ensureContextAndFbo(mesh, w, h)
                    val s = scene!!
                    val startNs = System.nanoTime()
                    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, msaaFbo)
                    s.draw(w, h, pose, label, subLabel, zoom)

                    GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, msaaFbo)
                    GLES30.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER, resolveFbo)
                    GLES30.glBlitFramebuffer(
                        0, 0, w, h,
                        0, 0, w, h,
                        GLES30.GL_COLOR_BUFFER_BIT,
                        GLES30.GL_NEAREST,
                    )
                    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, resolveFbo)
                    GLES30.glFinish()
                    val renderNs = System.nanoTime() - startNs

                    val pixelBuf = ByteBuffer.allocateDirect(w * h * 4).order(ByteOrder.nativeOrder())
                    GLES30.glReadPixels(0, 0, w, h, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, pixelBuf)
                    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)

                    val bitmap = rgbaBufferToTopDownBitmap(pixelBuf, w, h)
                    GlRenderResult(bitmap = bitmap, renderTimeNs = renderNs)
                },
            )
            latch.countDown()
        }
        latch.await()
        return resultRef.get().getOrThrow()
    }

    private fun ensureContextAndFbo(mesh: VikingMesh, w: Int, h: Int) {
        if (egl.context == EGL14.EGL_NO_CONTEXT) {
            egl.init(EGL14.EGL_PBUFFER_BIT, preferMsaa = false)
            pbufferSurface = egl.createPbufferSurface(1, 1)
            pbufferW = 1
            pbufferH = 1
            egl.makeCurrent(pbufferSurface)
            scene = GlSceneResources(mesh).also { it.init() }
        } else {
            egl.makeCurrent(pbufferSurface)
        }

        if (fboW == w && fboH == h && msaaFbo != 0 && resolveFbo != 0) return
        deleteFbos()

        val maxSamplesArr = IntArray(1)
        GLES30.glGetIntegerv(GLES30.GL_MAX_SAMPLES, maxSamplesArr, 0)
        val samples = min(4, max(0, maxSamplesArr[0]))

        val fbos = IntArray(2)
        GLES30.glGenFramebuffers(2, fbos, 0)
        msaaFbo = fbos[0]
        resolveFbo = fbos[1]

        val rbs = IntArray(3)
        GLES30.glGenRenderbuffers(3, rbs, 0)
        msaaColorRb = rbs[0]
        msaaDepthRb = rbs[1]
        resolveColorRb = rbs[2]

        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, msaaColorRb)
        if (samples > 1) {
            GLES30.glRenderbufferStorageMultisample(GLES30.GL_RENDERBUFFER, samples, GLES30.GL_RGBA8, w, h)
        } else {
            GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, GLES30.GL_RGBA8, w, h)
        }
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, msaaDepthRb)
        if (samples > 1) {
            GLES30.glRenderbufferStorageMultisample(GLES30.GL_RENDERBUFFER, samples, GLES30.GL_DEPTH_COMPONENT24, w, h)
        } else {
            GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, GLES30.GL_DEPTH_COMPONENT24, w, h)
        }
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, msaaFbo)
        GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_RENDERBUFFER, msaaColorRb)
        GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_RENDERBUFFER, msaaDepthRb)

        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, resolveColorRb)
        GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, GLES30.GL_RGBA8, w, h)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, resolveFbo)
        GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_RENDERBUFFER, resolveColorRb)

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        fboW = w
        fboH = h
    }

    private fun deleteFbos() {
        if (msaaFbo != 0 || resolveFbo != 0) {
            GLES30.glDeleteFramebuffers(2, intArrayOf(msaaFbo, resolveFbo), 0)
            GLES30.glDeleteRenderbuffers(3, intArrayOf(msaaColorRb, msaaDepthRb, resolveColorRb), 0)
        }
        msaaFbo = 0
        resolveFbo = 0
        msaaColorRb = 0
        msaaDepthRb = 0
        resolveColorRb = 0
        fboW = 0
        fboH = 0
    }

    private fun rgbaBufferToTopDownBitmap(buf: ByteBuffer, w: Int, h: Int): Bitmap {
        val rowBytes = w * 4
        val raw = ByteArray(rowBytes * h)
        buf.position(0)
        buf.get(raw)
        val flipped = ByteBuffer.allocateDirect(rowBytes * h).order(ByteOrder.nativeOrder())
        for (y in 0 until h) {
            val srcOffset = (h - 1 - y) * rowBytes
            flipped.put(raw, srcOffset, rowBytes)
        }
        flipped.position(0)
        return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            copyPixelsFromBuffer(flipped)
        }
    }
}
