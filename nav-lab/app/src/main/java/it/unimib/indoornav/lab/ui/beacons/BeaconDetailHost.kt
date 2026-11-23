package it.unimib.indoornav.lab.ui.beacons

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import it.unimib.indoornav.lab.ui.components.BeaconModelView
import it.unimib.indoornav.lab.ui.components.MorphingCookieShape
import it.unimib.indoornav.lab.ui.components.OrganicBeaconHalo
import it.unimib.indoornav.lab.ui.components.OrganicShapes
import it.unimib.indoornav.lab.ui.components.SmoothCookieShape
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val HERO_DURATION_MS = 450
private const val CARD_HALO_FRACTION = 0.86f

// The model is always laid out at the detail size and scaled, so a frame rendered for one size serves all.
private val MODEL_LAYOUT_SIZE = 240.dp

internal data class HeroLook(
    val label: String,
    val subLabel: String,
    val online: Boolean,
    val cardShape: SmoothCookieShape,
    val stale: Boolean,
)

/**
 * Where each beacon's halo sits on its card and in the detail screen, and the beacon flying between
 * them. While it flies, both resting copies are hidden and the host draws the one moving copy.
 */
class BeaconHeroState internal constructor(
    val key: String?,
    val progress: State<Float>,
) {
    internal val cardRects = mutableStateMapOf<String, Rect>()
    internal val detailRects = mutableStateMapOf<String, Rect>()
    internal val looks = mutableStateMapOf<String, HeroLook>()

    // Written only by the detail copy: the card copy recomposing on the way back must not reset it.
    internal val detailHaloRotations = mutableStateMapOf<String, Float>()

    // Bottom edge, in root coordinates, of the detail screen's top bar.
    internal var detailBarBottom by mutableStateOf<Float?>(null)

    internal fun flightRects(): Pair<Rect, Rect>? {
        val k = key ?: return null
        val p = progress.value
        if (p <= 0f || p >= 1f) return null
        val from = cardRects[k] ?: return null
        val to = detailRects[k] ?: return null
        return from to to
    }

    internal fun isFlying(tileKey: String) = key == tileKey && flightRects() != null
}

val LocalBeaconHero = compositionLocalOf<BeaconHeroState?> { null }

/**
 * Shows [list] or the detail of the beacon [detailKey]. On the way between them the beacon's halo
 * flies from its card to the detail stage, melting from the card's cookie shape into the 12-lobe one,
 * while the model turns from upright to its three-quarter pose. The back gesture scrubs all of it and
 * only closes the detail once released.
 *
 * The list leaves composition while the detail is shown, so callers keep its scroll state outside.
 */
@Composable
fun BeaconDetailHost(
    detailKey: String?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    list: @Composable () -> Unit,
    detail: @Composable (key: String) -> Unit,
) {
    val seekState = remember { SeekableTransitionState(detailKey) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(detailKey) {
        if (seekState.currentState != detailKey || seekState.targetState != detailKey) {
            seekState.animateTo(detailKey)
        }
    }

    PredictiveBackHandler(enabled = detailKey != null) { progress ->
        val openKey = detailKey
        try {
            progress.collect { event -> seekState.seekTo(event.progress, targetState = null) }
            onClose()
        } catch (e: CancellationException) {
            scope.launch { seekState.animateTo(openKey) }
            throw e
        }
    }

    val transition = rememberTransition(seekState, label = "beaconDetail")
    val heroProgress = transition.animateFloat(
        transitionSpec = { tween(HERO_DURATION_MS, easing = FastOutSlowInEasing) },
        label = "heroProgress",
    ) { if (it == null) 0f else 1f }
    val heroKey = transition.targetState ?: transition.currentState
    val previous = remember { mutableStateOf<BeaconHeroState?>(null) }
    val hero = remember(heroKey) {
        BeaconHeroState(heroKey, heroProgress).also { next ->
            previous.value?.let { old ->
                next.cardRects.putAll(old.cardRects)
                next.detailRects.putAll(old.detailRects)
                next.looks.putAll(old.looks)
                next.detailHaloRotations.putAll(old.detailHaloRotations)
                next.detailBarBottom = old.detailBarBottom
            }
        }
    }
    SideEffect { previous.value = hero }
    var hostOrigin by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = modifier.onGloballyPositioned { hostOrigin = it.positionInRoot() }) {
        transition.AnimatedContent(
            transitionSpec = { fadeIn(tween(300, delayMillis = 90)) togetherWith fadeOut(tween(250)) },
        ) { key ->
            CompositionLocalProvider(LocalBeaconHero provides hero) {
                if (key == null) list() else detail(key)
            }
        }

        val flight = hero.flightRects()
        val look = hero.key?.let { hero.looks[it] }
        if (flight != null && look != null) {
            val p = heroProgress.value
            val rect = lerp(flight.first, flight.second, p)
            val density = LocalDensity.current
            // A model scrolled under the detail's top bar comes out from under it instead of covering it:
            // the flying copy is cut at the bar's bottom edge, which slides up to the top as it lands.
            val barBottom = hero.detailBarBottom ?: hostOrigin.y
            val clipEdge = hostOrigin.y + (barBottom - hostOrigin.y) * p
            HeroVisual(
                look = look,
                progress = p,
                detailHaloRotation = hero.detailHaloRotations[hero.key] ?: 0f,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (rect.left - hostOrigin.x).roundToInt(),
                            (rect.top - hostOrigin.y).roundToInt(),
                        )
                    }
                    .size(with(density) { rect.width.toDp() }, with(density) { rect.height.toDp() })
                    .drawWithContent {
                        val far = size.maxDimension * 4
                        clipRect(left = -far, top = clipEdge - rect.top, right = far, bottom = far) {
                            this@drawWithContent.drawContent()
                        }
                    },
            )
        }
    }
}

/** Marks the detail screen's top bar, so a flying beacon never draws over it. */
@Composable
fun Modifier.heroCeiling(): Modifier {
    val hero = LocalBeaconHero.current ?: return this
    return onGloballyPositioned { hero.detailBarBottom = it.positionInRoot().y + it.size.height }
}

/**
 * The halo and model of one beacon, resting on its card ([inDetail] false) or on the detail stage.
 * Its place is reported to the host so the flying copy can start and land exactly on it.
 */
@Composable
fun BeaconHero(
    tileKey: String,
    label: String,
    subLabel: String,
    online: Boolean,
    cardShape: SmoothCookieShape,
    inDetail: Boolean,
    size: Dp,
    modifier: Modifier = Modifier,
    haloRotation: Float = 0f,
    stale: Boolean = false,
) {
    val hero = LocalBeaconHero.current
    val look = HeroLook(label, subLabel, online, cardShape, stale)
    if (hero != null) {
        SideEffect {
            hero.looks[tileKey] = look
            if (inDetail) hero.detailHaloRotations[tileKey] = haloRotation
        }
    }
    val hidden = hero?.isFlying(tileKey) == true
    Box(
        modifier = modifier
            .size(size)
            .onGloballyPositioned { coords ->
                if (hero == null) return@onGloballyPositioned
                val rect = Rect(coords.positionInRoot(), coords.size.toSize())
                if (inDetail) hero.detailRects[tileKey] = rect else hero.cardRects[tileKey] = rect
            }
            .alpha(if (hidden) 0f else 1f),
    ) {
        HeroVisual(
            look = look,
            progress = if (inDetail) 1f else 0f,
            detailHaloRotation = haloRotation,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun HeroVisual(look: HeroLook, progress: Float, detailHaloRotation: Float, modifier: Modifier) {
    val shape = when {
        progress <= 0f -> look.cardShape
        progress >= 1f -> OrganicShapes.SmoothCookie12
        else -> MorphingCookieShape(look.cardShape, OrganicShapes.SmoothCookie12, progress)
    }
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val scale = maxWidth / MODEL_LAYOUT_SIZE
        OrganicBeaconHalo(
            online = look.online,
            stale = look.stale,
            shape = shape,
            modifier = Modifier
                .fillMaxSize(CARD_HALO_FRACTION + (1f - CARD_HALO_FRACTION) * progress)
                // The detail halo turns as the page scrolls; in flight it unwinds towards the card.
                .graphicsLayer { rotationZ = detailHaloRotation * progress },
        )
        BeaconModelView(
            label = look.label,
            subLabel = look.subLabel,
            progress = progress,
            online = look.online,
            modifier = Modifier
                .requiredSize(MODEL_LAYOUT_SIZE)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
        )
    }
}
