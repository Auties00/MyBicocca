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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
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
import it.unimib.indoornav.lab.ui.model.VikingMesh
import it.unimib.indoornav.lab.ui.theme.BeaconHaloTokens
import it.unimib.indoornav.lab.ui.theme.LabTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class BeaconPose {
    CARD,
    DETAIL,
}

private data class BeaconBitmapKey(
    val pose: BeaconPose,
    val label: String,
    val subLabel: String,
    val size: IntSize,
)

private object BeaconBitmapCache {
    private val cache = object : LruCache<BeaconBitmapKey, ImageBitmap>(16 * 1024 * 1024) {
        override fun sizeOf(key: BeaconBitmapKey, value: ImageBitmap): Int = value.width * value.height * 4
    }

    fun get(key: BeaconBitmapKey): ImageBitmap? = synchronized(cache) { cache.get(key) }

    fun put(key: BeaconBitmapKey, bitmap: ImageBitmap) {
        synchronized(cache) { cache.put(key, bitmap) }
    }
}

/** The beacon rendered by OpenGL in a fixed pose, cached as a bitmap per pose, label and size. */
@Composable
fun BeaconModelView(
    label: String,
    subLabel: String,
    pose: BeaconPose,
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
    val key = BeaconBitmapKey(pose, label, subLabel, sizePx)
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(key, mesh) {
        if (mesh == null || sizePx.width <= 0 || sizePx.height <= 0) return@LaunchedEffect
        BeaconBitmapCache.get(key)?.let {
            bitmap = it
            return@LaunchedEffect
        }
        val angles = if (pose == BeaconPose.CARD) BeaconRenderSpec.CARD_POSE else BeaconRenderSpec.DETAIL_POSE
        val rendered = withContext(Dispatchers.Default) {
            runCatching {
                GlBeaconOffscreenRenderer.renderToBitmap(
                    mesh = mesh,
                    label = label,
                    subLabel = subLabel,
                    pose = angles,
                    widthPx = sizePx.width,
                    heightPx = sizePx.height,
                ).bitmap.asImageBitmap()
            }.getOrNull()
        } ?: return@LaunchedEffect
        BeaconBitmapCache.put(key, rendered)
        bitmap = rendered
    }

    Box(
        modifier = modifier
            .alpha(modelAlpha)
            .onSizeChanged { sizePx = it },
        contentAlignment = Alignment.Center,
    ) {
        val bmp = bitmap
        if (bmp != null) {
            Image(bitmap = bmp, contentDescription = "Beacon $label", modifier = Modifier.fillMaxSize())
        } else if (inPreview || mesh == null) {
            FallbackTag(label = label, modifier = Modifier.fillMaxSize())
        }
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
                BeaconModelView("B07", "U14 - Floor 1", BeaconPose.CARD, online = true, Modifier.size(44.dp, 88.dp))
                BeaconModelView("B16", "U14 - Floor 1", BeaconPose.CARD, online = false, Modifier.size(44.dp, 88.dp))
                BeaconModelView("B07", "U14 - Floor 1", BeaconPose.DETAIL, online = true, Modifier.size(160.dp, 120.dp))
            }
        }
    }
}
