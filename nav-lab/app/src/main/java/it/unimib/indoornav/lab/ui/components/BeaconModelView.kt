package it.unimib.indoornav.lab.ui.components

import android.graphics.Bitmap
import android.util.LruCache
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import it.unimib.indoornav.lab.ui.model.BeaconRenderSpec
import it.unimib.indoornav.lab.ui.model.CpuBeaconRenderer
import it.unimib.indoornav.lab.ui.model.GlBeaconOffscreenRenderer
import it.unimib.indoornav.lab.ui.model.GlBeaconTextureView
import it.unimib.indoornav.lab.ui.model.LocalModelRenderer
import it.unimib.indoornav.lab.ui.model.ModelRenderer
import it.unimib.indoornav.lab.ui.model.PoseAngles
import it.unimib.indoornav.lab.ui.model.VikingMesh
import it.unimib.indoornav.lab.ui.theme.BeaconHaloTokens
import it.unimib.indoornav.lab.ui.theme.LabTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.withContext

enum class BeaconPose {
    CARD,
    DETAIL,
}

private data class BeaconBitmapCacheKey(
    val renderer: ModelRenderer,
    val pose: BeaconPose,
    val label: String,
    val subLabel: String,
    val widthPx: Int,
    val heightPx: Int,
)

object BeaconBitmapCache {
    private const val MAX_BYTES = 16 * 1024 * 1024

    private val cache = object : LruCache<BeaconBitmapCacheKey, ImageBitmap>(MAX_BYTES) {
        override fun sizeOf(key: BeaconBitmapCacheKey, value: ImageBitmap): Int {
            return (value.width * value.height * 4).coerceAtLeast(1)
        }
    }

    fun get(
        renderer: ModelRenderer,
        pose: BeaconPose,
        label: String,
        subLabel: String,
        widthPx: Int,
        heightPx: Int,
    ): ImageBitmap? {
        return synchronized(cache) {
            cache.get(BeaconBitmapCacheKey(renderer, pose, label, subLabel, widthPx, heightPx))
        }
    }

    fun put(
        renderer: ModelRenderer,
        pose: BeaconPose,
        label: String,
        subLabel: String,
        widthPx: Int,
        heightPx: Int,
        bitmap: ImageBitmap,
    ) {
        synchronized(cache) {
            cache.put(BeaconBitmapCacheKey(renderer, pose, label, subLabel, widthPx, heightPx), bitmap)
        }
    }

    fun clear() {
        synchronized(cache) {
            cache.evictAll()
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun BeaconModelView(
    label: String,
    subLabel: String,
    pose: BeaconPose,
    online: Boolean,
    modifier: Modifier = Modifier,
    renderer: ModelRenderer = LocalModelRenderer.current,
    interactive: Boolean = (pose == BeaconPose.DETAIL),
    overrideAngles: PoseAngles? = null,
    onFrameTimeNs: ((Long) -> Unit)? = null,
) {
    val context = LocalContext.current
    val inPreview = LocalInspectionMode.current
    val modelAlpha by animateFloatAsState(
        targetValue = if (online) BeaconHaloTokens.ONLINE_MODEL_ALPHA else BeaconHaloTokens.OFFLINE_MODEL_ALPHA,
        animationSpec = tween(durationMillis = 300),
        label = "beaconModelAlpha",
    )

    val mesh = remember(context) { runCatching { VikingMesh.load(context) }.getOrNull() }
    var sizePx by remember { mutableStateOf(IntSize.Zero) }

    if (pose == BeaconPose.CARD) {
        val cachedBitmap = remember(renderer, pose, label, subLabel, sizePx) {
            if (sizePx.width > 0 && sizePx.height > 0) {
                BeaconBitmapCache.get(renderer, pose, label, subLabel, sizePx.width, sizePx.height)
            } else {
                null
            }
        }
        var renderedBitmap by remember { mutableStateOf(cachedBitmap) }
        if (cachedBitmap != null && renderedBitmap !== cachedBitmap) {
            renderedBitmap = cachedBitmap
        }

        if (inPreview && renderedBitmap == null && mesh != null && sizePx.width > 0 && sizePx.height > 0) {
            renderedBitmap = remember(label, subLabel, sizePx) {
                CpuBeaconRenderer.renderBitmap(
                    mesh = mesh,
                    label = label,
                    subLabel = subLabel,
                    pose = BeaconRenderSpec.CARD_POSE,
                    widthPx = sizePx.width,
                    heightPx = sizePx.height,
                ).imageBitmap
            }
        }

        LaunchedEffect(mesh, renderer, label, subLabel, sizePx, inPreview) {
            if (inPreview || mesh == null || sizePx.width <= 0 || sizePx.height <= 0) return@LaunchedEffect
            val existing = BeaconBitmapCache.get(renderer, pose, label, subLabel, sizePx.width, sizePx.height)
            if (existing != null) {
                renderedBitmap = existing
                return@LaunchedEffect
            }
            val result = withContext(Dispatchers.Default) {
                if (renderer == ModelRenderer.GL) {
                    runCatching {
                        val glRes = GlBeaconOffscreenRenderer.renderToBitmap(
                            mesh = mesh,
                            label = label,
                            subLabel = subLabel,
                            pose = BeaconRenderSpec.CARD_POSE,
                            widthPx = sizePx.width,
                            heightPx = sizePx.height,
                        )
                        glRes.bitmap.asImageBitmap() to glRes.renderTimeNs
                    }.getOrElse {
                        val cpuRes = CpuBeaconRenderer.renderBitmap(
                            mesh = mesh,
                            label = label,
                            subLabel = subLabel,
                            pose = BeaconRenderSpec.CARD_POSE,
                            widthPx = sizePx.width,
                            heightPx = sizePx.height,
                        )
                        cpuRes.imageBitmap to cpuRes.renderTimeNs
                    }
                } else {
                    val cpuRes = CpuBeaconRenderer.renderBitmap(
                        mesh = mesh,
                        label = label,
                        subLabel = subLabel,
                        pose = BeaconRenderSpec.CARD_POSE,
                        widthPx = sizePx.width,
                        heightPx = sizePx.height,
                    )
                    cpuRes.imageBitmap to cpuRes.renderTimeNs
                }
            }
            BeaconBitmapCache.put(renderer, pose, label, subLabel, sizePx.width, sizePx.height, result.first)
            renderedBitmap = result.first
            onFrameTimeNs?.invoke(result.second)
        }

        Box(
            modifier = modifier
                .alpha(modelAlpha)
                .onSizeChanged { sizePx = it },
            contentAlignment = Alignment.Center,
        ) {
            val bmp = renderedBitmap
            if (bmp != null) {
                Image(
                    bitmap = bmp,
                    contentDescription = "Beacon $label",
                    modifier = Modifier.fillMaxSize(),
                )
            } else if (inPreview) {
                PreviewFallbackTag(label = label, modifier = Modifier.fillMaxSize())
            }
        }
        return
    }

    val transition = remember(pose) { Animatable(if (overrideAngles != null) 1f else 0f) }
    LaunchedEffect(pose, overrideAngles) {
        if (overrideAngles == null) {
            transition.snapTo(0f)
            transition.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
            )
        } else {
            transition.snapTo(1f)
        }
    }

    var orbitYaw by remember { mutableFloatStateOf(0f) }
    var orbitPitch by remember { mutableFloatStateOf(0f) }

    val activeAngles = overrideAngles ?: run {
        val p = transition.value
        val target = BeaconRenderSpec.DETAIL_POSE
        PoseAngles(
            pitchDeg = lerp(BeaconRenderSpec.CARD_POSE.pitchDeg, target.pitchDeg, p) + orbitPitch,
            yawDeg = lerp(BeaconRenderSpec.CARD_POSE.yawDeg, target.yawDeg, p) + orbitYaw,
            rollDeg = lerp(BeaconRenderSpec.CARD_POSE.rollDeg, target.rollDeg, p),
        )
    }

    val dragModifier = if (interactive) {
        Modifier.pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                orbitYaw += dragAmount.x * 0.45f
                orbitPitch += dragAmount.y * 0.45f
            }
        }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .alpha(modelAlpha)
            .onSizeChanged { sizePx = it }
            .then(dragModifier),
        contentAlignment = Alignment.Center,
    ) {
        if (inPreview || mesh == null) {
            if (mesh != null && sizePx.width > 0 && sizePx.height > 0) {
                val previewBmp = remember(label, subLabel, activeAngles, sizePx) {
                    CpuBeaconRenderer.renderBitmap(
                        mesh = mesh,
                        label = label,
                        subLabel = subLabel,
                        pose = activeAngles,
                        widthPx = sizePx.width,
                        heightPx = sizePx.height,
                    ).imageBitmap
                }
                Image(
                    bitmap = previewBmp,
                    contentDescription = "Beacon $label",
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                PreviewFallbackTag(label = label, modifier = Modifier.fillMaxSize())
            }
        } else if (renderer == ModelRenderer.GL) {
            AndroidView(
                factory = { ctx -> GlBeaconTextureView(ctx, mesh) },
                update = { view ->
                    view.updateScene(
                        label = label,
                        subLabel = subLabel,
                        pose = activeAngles,
                        onFrameTimeNs = onFrameTimeNs,
                    )
                },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            var liveFrame by remember { mutableStateOf<Pair<ImageBitmap, Int>?>(null) }
            val latestParams by rememberUpdatedState(Triple(label, subLabel, activeAngles))
            LaunchedEffect(mesh, sizePx) {
                if (sizePx.width <= 0 || sizePx.height <= 0) return@LaunchedEffect
                val cpuDispatcher = Dispatchers.Default.limitedParallelism(1)
                val scratch = CpuBeaconRenderer.ScratchBuffers()
                var backBufferA: Bitmap? = null
                var backBufferB: Bitmap? = null
                var seq = 0
                snapshotFlow { latestParams }
                    .conflate()
                    .collect { (lbl, sub, angles) ->
                        val target = if (seq % 2 == 0) backBufferA else backBufferB
                        val res = withContext(cpuDispatcher) {
                            CpuBeaconRenderer.renderBitmap(
                                mesh = mesh,
                                label = lbl,
                                subLabel = sub,
                                pose = angles,
                                widthPx = sizePx.width,
                                heightPx = sizePx.height,
                                scratch = scratch,
                                recycleTarget = target,
                            )
                        }
                        if (seq % 2 == 0) backBufferA = res.bitmap else backBufferB = res.bitmap
                        seq++
                        liveFrame = res.imageBitmap to seq
                        onFrameTimeNs?.invoke(res.renderTimeNs)
                    }
            }
            liveFrame?.let { (bmp, _) ->
                Image(
                    bitmap = bmp,
                    contentDescription = "Beacon $label",
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun PreviewFallbackTag(label: String, modifier: Modifier = Modifier) {
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

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

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
                BeaconModelView(
                    label = "B07",
                    subLabel = "U14 - Floor 1",
                    pose = BeaconPose.CARD,
                    online = true,
                    renderer = ModelRenderer.CPU,
                    modifier = Modifier.size(width = 44.dp, height = 88.dp),
                )
                BeaconModelView(
                    label = "B16",
                    subLabel = "U14 - Floor 1",
                    pose = BeaconPose.CARD,
                    online = false,
                    renderer = ModelRenderer.CPU,
                    modifier = Modifier.size(width = 44.dp, height = 88.dp),
                )
                BeaconModelView(
                    label = "B07",
                    subLabel = "U14 - Floor 1",
                    pose = BeaconPose.DETAIL,
                    online = true,
                    renderer = ModelRenderer.CPU,
                    modifier = Modifier.size(width = 88.dp, height = 160.dp),
                )
            }
        }
    }
}
