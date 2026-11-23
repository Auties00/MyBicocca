package it.unimib.indoornav.lab.ui.components

import android.util.LruCache
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.unimib.indoornav.lab.ui.model.BeaconRenderSpec
import it.unimib.indoornav.lab.ui.model.GlBeaconOffscreenRenderer
import it.unimib.indoornav.lab.ui.model.PoseAngles
import it.unimib.indoornav.lab.ui.model.VikingMesh
import it.unimib.indoornav.lab.ui.theme.BeaconHaloTokens
import it.unimib.indoornav.lab.ui.theme.LabTheme
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

// Frames between card and detail are rendered square at this size and scaled to the box.
private const val TRANSITION_RENDER_PX = 640
private const val TRANSITION_STEPS = 90

private data class BeaconBitmapKey(
    val step: Int,
    val label: String,
    val subLabel: String,
    val size: IntSize,
)

private object BeaconBitmapCache {
    private val cache = object : LruCache<BeaconBitmapKey, ImageBitmap>(64 * 1024 * 1024) {
        override fun sizeOf(key: BeaconBitmapKey, value: ImageBitmap): Int = value.width * value.height * 4
    }

    fun get(key: BeaconBitmapKey): ImageBitmap? = synchronized(cache) { cache.get(key) }

    fun put(key: BeaconBitmapKey, bitmap: ImageBitmap) {
        synchronized(cache) { cache.put(key, bitmap) }
    }
}

/** Renders each frame once, even when the card and the detail copy of a beacon ask for it together. */
private object FrameStore {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val inFlight = ConcurrentHashMap<BeaconBitmapKey, Deferred<ImageBitmap?>>()

    suspend fun get(mesh: VikingMesh, key: BeaconBitmapKey): ImageBitmap? {
        BeaconBitmapCache.get(key)?.let { return it }
        return inFlight.computeIfAbsent(key) {
            scope.async {
                render(mesh, key.label, key.subLabel, key.step, key.size)
                    ?.also { BeaconBitmapCache.put(key, it) }
                    .also { inFlight.remove(key) }
            }
        }.await()
    }
}

private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

private fun poseAt(progress: Float): PoseAngles {
    val c = BeaconRenderSpec.CARD_POSE
    val d = BeaconRenderSpec.DETAIL_POSE
    return PoseAngles(
        pitchDeg = lerp(c.pitchDeg, d.pitchDeg, progress),
        yawDeg = lerp(c.yawDeg, d.yawDeg, progress),
        rollDeg = lerp(c.rollDeg, d.rollDeg, progress),
    )
}

private fun fitPxPerMm(pose: PoseAngles, sizePx: Float): Float {
    val rot = BeaconRenderSpec.makeRotationMatrix(pose.pitchDeg, pose.yawDeg, pose.rollDeg)
    return BeaconRenderSpec.computeFraming(rot, sizePx, sizePx).pxPerMm
}

/**
 * Zoom that makes the model's scale move linearly from its fitted card size to its fitted detail size.
 * Fitting each in-between pose on its own would make the model seem to zoom while it turns.
 */
private fun zoomAt(progress: Float, sizePx: Float): Float {
    val card = fitPxPerMm(BeaconRenderSpec.CARD_POSE, sizePx)
    val detail = fitPxPerMm(BeaconRenderSpec.DETAIL_POSE, sizePx)
    return lerp(card, detail, progress) / fitPxPerMm(poseAt(progress), sizePx)
}

/**
 * The beacon rendered by OpenGL. [progress] 0 is the upright card pose and 1 the three-quarter detail
 * pose; values in between are rendered on demand, so the model turns as a transition runs. The two
 * ends are rendered at the box's own size and cached. The box should be square.
 */
@Composable
fun BeaconModelView(
    label: String,
    subLabel: String,
    progress: Float,
    online: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val inPreview = LocalInspectionMode.current
    val modelAlpha by animateFloatAsState(
        targetValue = if (online) BeaconHaloTokens.ONLINE_MODEL_ALPHA else BeaconHaloTokens.OFFLINE_MODEL_ALPHA,
        animationSpec = tween(durationMillis = 300),
        label = "beaconModelAlpha",
    )
    val mesh = remember(context) { if (inPreview) null else runCatching { VikingMesh.load(context) }.getOrNull() }
    var sizePx by remember { mutableStateOf(IntSize.Zero) }
    val restStep = if (progress >= 0.5f) TRANSITION_STEPS else 0
    var restBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var liveBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val latestProgress by rememberUpdatedState(progress)

    LaunchedEffect(mesh, label, subLabel, sizePx, restStep) {
        if (mesh == null || sizePx.width <= 0 || sizePx.height <= 0) return@LaunchedEffect
        restBitmap = FrameStore.get(mesh, BeaconBitmapKey(restStep, label, subLabel, sizePx))
    }

    LaunchedEffect(mesh, label, subLabel) {
        if (mesh == null) return@LaunchedEffect
        val size = IntSize(TRANSITION_RENDER_PX, TRANSITION_RENDER_PX)
        snapshotFlow { (latestProgress * TRANSITION_STEPS).roundToInt() }
            .filter { it in 1 until TRANSITION_STEPS }
            .conflate()
            .collect { step ->
                FrameStore.get(mesh, BeaconBitmapKey(step, label, subLabel, size))?.let { liveBitmap = it }
            }
    }

    Box(
        modifier = modifier
            .alpha(modelAlpha)
            .onSizeChanged { sizePx = it },
        contentAlignment = Alignment.Center,
    ) {
        // At either end the sharp render at the box's size wins; in between, the nearest end stands in
        // until the first frame of the transition arrives.
        val atEnd = progress <= 0f || progress >= 1f
        val shown = if (atEnd) restBitmap ?: liveBitmap else liveBitmap ?: restBitmap
        if (shown != null) {
            Image(
                bitmap = shown,
                contentDescription = "Beacon $label",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        } else if (inPreview || mesh == null) {
            FallbackTag(label = label, modifier = Modifier.fillMaxSize())
        }
    }
}

private suspend fun render(mesh: VikingMesh, label: String, subLabel: String, step: Int, size: IntSize): ImageBitmap? {
    val progress = step.toFloat() / TRANSITION_STEPS
    return withContext(Dispatchers.Default) {
        runCatching {
            GlBeaconOffscreenRenderer.renderToBitmap(
                mesh = mesh,
                label = label,
                subLabel = subLabel,
                pose = poseAt(progress),
                widthPx = size.width,
                heightPx = size.height,
                zoom = zoomAt(progress, minOf(size.width, size.height).toFloat()),
            ).bitmap.asImageBitmap()
        }.getOrNull()
    }
}

// Shown in previews, where OpenGL is unavailable, and if the mesh fails to load.
@Composable
private fun FallbackTag(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFD0C7BE), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label.ifBlank { "—" },
            color = Color(0xFF171518),
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BeaconModelViewPreview() {
    LabTheme {
        Surface {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BeaconModelView("B07", "U14 - Floor 1", progress = 0f, online = true, Modifier.size(88.dp))
                BeaconModelView("B16", "U14 - Floor 1", progress = 0f, online = false, Modifier.size(88.dp))
                BeaconModelView("B07", "U14 - Floor 1", progress = 1f, online = true, Modifier.size(160.dp))
            }
        }
    }
}
