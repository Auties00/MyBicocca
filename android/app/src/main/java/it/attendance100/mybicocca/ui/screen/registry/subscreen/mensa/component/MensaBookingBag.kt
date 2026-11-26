package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.vectordrawable.graphics.drawable.SeekableAnimatedVectorDrawable
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.mensa.MensaDish
import it.attendance100.mybicocca.ui.component.modal.SheetMotion
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.MensaDishArtwork
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.mensaDishDrawable
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/** The bag drawing's width over its height. */
private const val BAG_ASPECT_RATIO = 1077.8f / 847.1f

/** Most plates shown coming out of the bag; a bigger order still lists every dish below. */
private const val MAX_PLATES = 6
private val HeroPlateSize = 64.dp
private val HeroBagWidth = 150.dp

/** The hero's height when dishes come out of the bag: room for the fan of plates above it. */
private val HeroHeight = 214.dp

/** Where the bag's open top is, from the hero's top: the bag sits at the bottom, its mouth ~15% down. */
private val BagMouthTop = HeroHeight - HeroBagWidth / BAG_ASPECT_RATIO + HeroBagWidth * 0.15f

/** Each plate starts this share of the whole rise after the one before it. */
private const val PLATE_STAGGER = 0.12f

/** How long the bag takes to open, or to close: the length of its drawing's animation. */
private const val BAG_OPEN_MS = 450

/** Once the bag has landed it opens; the dishes start rising this far in, while it is still opening. */
private const val RISE_START_MS = 250
private const val RISE_MS = 700

/** The whole sequence: the bag opens, the dishes rise out of it, and it closes once they are in place. */
private const val HERO_MS = RISE_START_MS + RISE_MS + BAG_OPEN_MS

/**
 * Going back, the booking page holds still this long while the same sequence plays backwards: the
 * bag opens, takes the dishes back and closes. Only then does it fly home.
 */
internal const val MENSA_BAG_RETURN_MS = 560

/** The two halves of the bag, each its own shared element so the dishes can come out between them. */
private enum class BagLayer { Back, Front }

/**
 * How open each booking's bag is in the booking page on screen (0 shut, 1 open), by reservation id.
 * While that page leaves, the bag flying home is drawn by the page it lands on, which reads the
 * same value so the bag keeps opening and closing on the way.
 */
internal val LocalMensaBagOpenness = staticCompositionLocalOf<SnapshotStateMap<Int, State<Float>>?> { null }

/**
 * The take-away bag of booking [reservationId]. In the Mensa sheet it is a shared element, so the
 * bag on the hub's card flies to the top of the booking it opens, and back.
 */
@Composable
internal fun MensaBookingBag(reservationId: Int, width: Dp, modifier: Modifier = Modifier) {
    val openness = LocalMensaBagOpenness.current?.get(reservationId)
    Box(modifier.width(width).aspectRatio(BAG_ASPECT_RATIO)) {
        if (openness != null) {
            // Standing in for the booking's bag on its way home: open as much as that one is.
            BagLayerImage(R.drawable.mensa_art_takeaway_bag_open_back, openness, reservationId, BagLayer.Back)
            BagLayerImage(R.drawable.mensa_art_takeaway_bag_open_front, openness, reservationId, BagLayer.Front)
        } else {
            // A shut bag is one drawing; the back half's place is kept for the booking's two halves.
            Spacer(Modifier.matchParentSize().mensaBagElement(reservationId, BagLayer.Back))
            Image(
                painter = painterResource(R.drawable.mensa_art_takeaway_bag_simple),
                contentDescription = null,
                modifier = Modifier.matchParentSize().mensaBagElement(reservationId, BagLayer.Front),
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun Modifier.mensaBagElement(reservationId: Int, layer: BagLayer): Modifier {
    val scopes = LocalMensaShared.current ?: return this
    val heroWidthPx = with(LocalDensity.current) { HeroBagWidth.toPx() }
    val boundsTransform = remember(heroWidthPx) { bagBoundsTransform(heroWidthPx) }
    return with(scopes.transition) {
        this@mensaBagElement.sharedElement(
            sharedContentState = rememberSharedContentState(key = "mensa_bag_${layer.name}_$reservationId"),
            animatedVisibilityScope = scopes.visibility,
            boundsTransform = boundsTransform,
            zIndexInOverlay = layer.ordinal.toFloat(),
        )
    }
}

/** Leaving a booking, the bag waits for its dishes to go back in before it flies home. */
@OptIn(ExperimentalSharedTransitionApi::class)
private fun bagBoundsTransform(heroWidthPx: Float) = BoundsTransform { initial, target ->
    val leavingBooking = abs(initial.width - heroWidthPx) < 1f && target.width < initial.width
    if (leavingBooking) {
        tween(SheetMotion.PAGE_MS, delayMillis = MENSA_BAG_RETURN_MS, easing = FastOutSlowInEasing)
    } else {
        spring(stiffness = Spring.StiffnessMediumLow, visibilityThreshold = Rect.VisibilityThreshold)
    }
}

/**
 * The top of a booking: its bag, which once landed opens, lets the ordered dishes rise out of it
 * and closes again. The whole sequence is part of the page change, so a back gesture plays it
 * backwards as the finger moves, and a cancelled one plays it forwards again.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun MensaBookingBagHero(reservationId: Int, dishes: List<MensaDish>, modifier: Modifier = Modifier) {
    val shown = dishes.take(MAX_PLATES)
    val timeline = rememberHeroTimeline(shown.isNotEmpty())
    val openness = remember(timeline) { derivedStateOf { bagOpenness(timeline.value) } }
    val registry = LocalMensaBagOpenness.current
    DisposableEffect(registry, reservationId, openness) {
        registry?.set(reservationId, openness)
        onDispose { if (registry?.get(reservationId) === openness) registry.remove(reservationId) }
    }
    val scopes = LocalMensaShared.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (shown.isEmpty()) 130.dp else HeroHeight),
    ) {
        BagLayerImage(
            R.drawable.mensa_art_takeaway_bag_open_back, openness, reservationId, BagLayer.Back,
            Modifier.align(Alignment.BottomCenter).width(HeroBagWidth),
        )
        // Between the bag's two halves, so a dish still inside is hidden behind the front; during a
        // page change too, when the halves are drawn above the pages.
        shown.forEachIndexed { index, dish ->
            val art = remember(dish) { MensaDishArtwork.forDish(dish) }
            val betweenHalves = if (scopes != null) {
                with(scopes.transition) { Modifier.renderInSharedTransitionScopeOverlay(zIndexInOverlay = 0.5f) }
            } else {
                Modifier
            }
            MensaPlate(
                art = art,
                size = HeroPlateSize,
                drawable = mensaDishDrawable(dish.id, art.drawable),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .then(betweenHalves)
                    .risingOutOfBag(timeline, index, shown.size),
            )
        }
        BagLayerImage(
            R.drawable.mensa_art_takeaway_bag_open_front, openness, reservationId, BagLayer.Front,
            Modifier.align(Alignment.BottomCenter).width(HeroBagWidth),
        )
    }
}

/** One half of the bag, drawn at the frame [openness] gives it. */
@Composable
private fun BoxScope.BagLayerImage(
    @DrawableRes drawable: Int,
    openness: State<Float>,
    reservationId: Int,
    layer: BagLayer,
    modifier: Modifier = Modifier.matchParentSize(),
) {
    val context = LocalContext.current
    val painter = remember(drawable, openness) { BagLayerPainter(BagLayers.get(context, drawable)) { openness.value } }
    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier
            .mensaBagElement(reservationId, layer)
            .aspectRatio(BAG_ASPECT_RATIO),
    )
}

/**
 * The bag's two halves, inflated once for the whole app. A bag sets the frame it wants right
 * before drawing, so every bag on screen can share them.
 */
private object BagLayers {
    private val drawables = HashMap<Int, SeekableAnimatedVectorDrawable>()

    fun get(context: Context, @DrawableRes drawable: Int): SeekableAnimatedVectorDrawable = synchronized(this) {
        drawables.getOrPut(drawable) {
            checkNotNull(SeekableAnimatedVectorDrawable.create(context.applicationContext, drawable)) {
                "Bag layer $drawable is not an animated vector"
            }
        }
    }
}

/** Draws [drawable] at the point of its animation [openness] gives: 0 its first frame, 1 its last. */
private class BagLayerPainter(
    private val drawable: SeekableAnimatedVectorDrawable,
    private val openness: () -> Float,
) : Painter() {
    override val intrinsicSize: Size = Size(drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())

    override fun DrawScope.onDraw() {
        drawable.currentPlayTime = (openness().coerceIn(0f, 1f) * drawable.totalDuration).roundToLong()
        drawable.setBounds(0, 0, size.width.roundToInt(), size.height.roundToInt())
        drawIntoCanvas { drawable.draw(it.nativeCanvas) }
    }
}

/** How open the bag is at [timeline]: it opens at the start, stays open while the dishes rise, closes at the end. */
private fun bagOpenness(timeline: Float): Float {
    val ms = timeline * HERO_MS
    return (minOf(ms, HERO_MS - ms) / BAG_OPEN_MS).coerceIn(0f, 1f)
}

/**
 * 0 with the bag shut and every dish inside, 1 once they are all out and it has shut again;
 * follows the page's own transition, so it is seekable with it. Leaving the page it runs back to
 * 0 while the page holds still, before the bag flies home.
 */
@Composable
private fun rememberHeroTimeline(hasDishes: Boolean): State<Float> {
    val visibility = LocalMensaShared.current?.visibility
    if (visibility == null || !hasDishes) return remember { mutableFloatStateOf(1f) }
    return visibility.transition.animateFloat(
        transitionSpec = {
            if (targetState == EnterExitState.Visible) {
                // Once the bag has landed.
                tween(durationMillis = HERO_MS, delayMillis = SheetMotion.PAGE_MS, easing = LinearEasing)
            } else {
                tween(durationMillis = MENSA_BAG_RETURN_MS, easing = LinearEasing)
            }
        },
        label = "mensa_bag_hero",
    ) { if (it == EnterExitState.Visible) 1f else 0f }
}

/**
 * Moves plate [index] of [count] from inside the bag to its place in a fan above it: the middle
 * ones highest, the outer ones lower and tilted outwards. It rises straight up out of the bag's
 * top, then arcs over and down into place: a curve from inside the bag, through a point high above
 * its own start, to its place.
 */
private fun Modifier.risingOutOfBag(timeline: State<Float>, index: Int, count: Int): Modifier = graphicsLayer {
    val rise = ((timeline.value * HERO_MS - RISE_START_MS) / RISE_MS).coerceIn(0f, 1f)
    val window = 1f - PLATE_STAGGER * (count - 1)
    val own = ((rise - PLATE_STAGGER * index) / window).coerceIn(0f, 1f)
    val eased = EaseOutBack.transform(own)
    val fromCentre = index - (count - 1) / 2f
    val spacing = if (count > 1) minOf(62.dp, 250.dp / (count - 1)) else 0.dp
    val endX = spacing * fromCentre
    val endTop = 18.dp + 10.dp * abs(fromCentre)
    val startX = 6.dp * fromCentre
    // Inside the bag, behind its front, so it shows only once it clears the top.
    val startTop = BagMouthTop + 10.dp
    val peakTop = endTop - 34.dp
    translationX = bend(startX, startX, endX, eased).toPx()
    translationY = bend(startTop, peakTop, endTop, eased).toPx()
    val scale = lerp(0.6f, 1f, eased)
    scaleX = scale
    scaleY = scale
    rotationZ = 7f * fromCentre * eased
    // Hidden at the very start, while the bag may still be flying in over the empty spot.
    alpha = (own * 4f).coerceAtMost(1f)
}

/** A point along the quadratic curve from [from] to [to], pulled towards [via], at [t]. */
private fun bend(from: Dp, via: Dp, to: Dp, t: Float): Dp {
    val rest = 1f - t
    return from * (rest * rest) + via * (2f * rest * t) + to * (t * t)
}

@Preview(name = "Booking bag", widthDp = MENSA_PREVIEW_WIDTH, heightDp = 240)
@Composable
private fun BookingBagHeroPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        MensaBookingBagHero(
            reservationId = MensaPreviewOrders.reservation.id,
            dishes = MensaPreviewOrders.reservation.dishes,
        )
    }
}
