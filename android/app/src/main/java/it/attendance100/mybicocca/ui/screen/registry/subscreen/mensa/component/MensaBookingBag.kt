package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.SharedTransitionScope
import androidx.annotation.DrawableRes
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.vectordrawable.graphics.drawable.SeekableAnimatedVectorDrawable
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.mensa.MensaDish
import it.attendance100.mybicocca.ui.component.modal.LocalSheetBackScrub
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
 * Going back, the booking page holds still this long while the bag opens and takes the dishes
 * back; it closes again on its way home, as the page changes.
 */
internal const val MENSA_BAG_RETURN_MS = 240

/** Leaving with no gesture to follow, the dishes fade out this fast instead of going back in. */
private const val QUICK_DISHES_OUT_MS = 150

/** Where the timeline is once every dish is back in the bag: the start of their rise. */
private const val DISHES_IN = RISE_START_MS.toFloat() / HERO_MS

/** The two halves of the bag, each its own shared element so the dishes can come out between them. */
private enum class BagLayer { Back, Front }

/**
 * The bags of the Mensa sheet's pages, by reservation id. A booking page on screen puts in how
 * open its bag is; a card with a shut bag puts in where that bag sits on screen, which is where the
 * booking's bag flies when its page is left.
 */
internal class MensaBagStage {
    val bookings = mutableStateMapOf<Int, State<Float>>()
    val homes = mutableStateMapOf<Int, Rect>()
}

internal val LocalMensaBagStage = staticCompositionLocalOf<MensaBagStage?> { null }

/**
 * The take-away bag of booking [reservationId] on a card. In the Mensa sheet it is a shared element,
 * so it flies to the top of the booking it opens; coming back, that booking's own bag flies here.
 */
@Composable
internal fun MensaBookingBag(reservationId: Int, width: Dp, modifier: Modifier = Modifier) {
    val stage = LocalMensaBagStage.current
    DisposableEffect(stage, reservationId) {
        onDispose { stage?.homes?.remove(reservationId) }
    }
    Box(
        modifier
            .width(width)
            .aspectRatio(BAG_ASPECT_RATIO)
            .onGloballyPositioned { stage?.homes?.set(reservationId, it.boundsInWindow()) }
            // While the booking's page is on screen its bag is the one to see, also on its way back
            // here. Read while drawing, so this one shows in the very frame that one is gone.
            .graphicsLayer { alpha = if (stage?.bookings?.containsKey(reservationId) == true) 0f else 1f },
    ) {
        // A shut bag is one drawing; the back half's place is kept for the booking's two halves.
        Spacer(Modifier.matchParentSize().mensaBagElement(reservationId, BagLayer.Back))
        Image(
            painter = painterResource(R.drawable.mensa_art_takeaway_bag_simple),
            contentDescription = null,
            modifier = Modifier.matchParentSize().mensaBagElement(reservationId, BagLayer.Front),
        )
    }
}

/**
 * The bag as a shared element, flying from its card into the booking. [shared] turns it off: the
 * booking's bag makes its own way back, following the page change, so a back gesture can scrub it.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun Modifier.mensaBagElement(reservationId: Int, layer: BagLayer, shared: () -> Boolean = { true }): Modifier {
    val scopes = LocalMensaShared.current ?: return this
    val config = remember(shared) { BagShareConfig(shared) }
    return with(scopes.transition) {
        this@mensaBagElement.sharedElement(
            sharedContentState = rememberSharedContentState(key = "mensa_bag_${layer.name}_$reservationId", config = config),
            animatedVisibilityScope = scopes.visibility,
            zIndexInOverlay = layer.ordinal.toFloat(),
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
private class BagShareConfig(private val shared: () -> Boolean) : SharedTransitionScope.SharedContentConfig {
    override val SharedTransitionScope.SharedContentState.isEnabled: Boolean get() = shared()

    // Off at once, even mid-flight: the bag's own way back takes over from there.
    override val shouldKeepEnabledForOngoingAnimation: Boolean get() = false
}

/**
 * The top of a booking: its bag, which once landed opens, lets the ordered dishes rise out of it
 * and closes again. Leaving the page it plays backwards, quicker: the page holds while the bag
 * opens and takes the dishes back, then the bag closes as it flies home. All of it is part of the
 * page change, so a back gesture scrubs it as the finger moves, and a cancelled one plays it back.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun MensaBookingBagHero(reservationId: Int, dishes: List<MensaDish>, modifier: Modifier = Modifier) {
    val shown = dishes.take(MAX_PLATES)
    val scrubbed = LocalSheetBackScrub.current
    val timeline = rememberHeroTimeline(shown.isNotEmpty(), scrubbed)
    val dishesShown = rememberDishesShown(scrubbed)
    val openness = remember(timeline) { derivedStateOf { bagOpenness(timeline.value) } }
    val stage = LocalMensaBagStage.current
    DisposableEffect(stage, reservationId, openness) {
        stage?.bookings?.set(reservationId, openness)
        onDispose { if (stage?.bookings?.get(reservationId) === openness) stage.bookings.remove(reservationId) }
    }
    val scopes = LocalMensaShared.current
    val visibility = scopes?.visibility
    val leaving = remember(visibility) { { visibility?.transition?.targetState == EnterExitState.PostExit } }
    val flight = rememberBagFlight(scrubbed)
    val flightOf = remember(stage, reservationId) { BagFlight(stage, reservationId) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (shown.isEmpty()) 130.dp else HeroHeight),
    ) {
        BagLayerImage(
            R.drawable.mensa_art_takeaway_bag_open_back, openness, reservationId, BagLayer.Back,
            Modifier
                .align(Alignment.BottomCenter)
                .width(HeroBagWidth)
                .flyingHome(flightOf, flight, leaving, BagLayer.Back, reportStart = true),
            shared = { !leaving() },
        )
        // Between the bag's two halves, so a dish still inside is hidden behind the front; during a
        // page change too, when the halves are drawn above the pages.
        shown.forEachIndexed { index, dish ->
            val art = remember(dish) { MensaDishArtwork.forDish(dish) }
            val betweenHalves = if (scopes != null) {
                with(scopes.transition) {
                    Modifier.renderInSharedTransitionScopeOverlay(renderInOverlay = { isTransitionActive || leaving() }, zIndexInOverlay = 0.5f)
                }
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
                    .risingOutOfBag(timeline, index, shown.size)
                    .graphicsLayer { alpha = dishesShown.value },
            )
        }
        BagLayerImage(
            R.drawable.mensa_art_takeaway_bag_open_front, openness, reservationId, BagLayer.Front,
            Modifier
                .align(Alignment.BottomCenter)
                .width(HeroBagWidth)
                .flyingHome(flightOf, flight, leaving, BagLayer.Front, reportStart = false),
            shared = { !leaving() },
        )
    }
}

/** Where a booking's bag starts its way home from, and the card it flies to. */
private class BagFlight(private val stage: MensaBagStage?, private val reservationId: Int) {
    var start by mutableStateOf<Rect?>(null)
    val home: Rect? get() = stage?.homes?.get(reservationId)
}

/**
 * Takes a leaving booking's bag half to its card's bag, as far as [flight] says, drawn above the
 * pages so it does not fade with the one it leaves. With no card to land on it stays and fades.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun Modifier.flyingHome(
    bag: BagFlight,
    flight: State<Float>,
    leaving: () -> Boolean,
    layer: BagLayer,
    reportStart: Boolean,
): Modifier {
    val scopes = LocalMensaShared.current ?: return this
    val measured = if (reportStart) onGloballyPositioned { bag.start = it.boundsInWindow() } else this
    return with(scopes.transition) {
        measured
            .renderInSharedTransitionScopeOverlay(
                // Its own flight, not a shared element's, so this is not counted as a transition in progress.
                renderInOverlay = { leaving() && bag.home != null },
                zIndexInOverlay = layer.ordinal.toFloat(),
            )
            .graphicsLayer {
                val from = bag.start
                val home = bag.home
                val t = flight.value
                if (from == null || home == null || t <= 0f || from.width <= 0f) return@graphicsLayer
                transformOrigin = TransformOrigin(0f, 0f)
                val scale = lerp(1f, home.width / from.width, t)
                scaleX = scale
                scaleY = scale
                translationX = (home.left - from.left) * t
                translationY = (home.top - from.top) * t
            }
    }
}

/** 1 while the dishes show, fading to 0 when the page is left with no gesture to follow. */
@Composable
private fun rememberDishesShown(scrubbed: () -> Boolean): State<Float> {
    val visibility = LocalMensaShared.current?.visibility ?: return remember { mutableFloatStateOf(1f) }
    return visibility.transition.animateFloat(
        transitionSpec = {
            if (targetState == EnterExitState.PostExit && !scrubbed()) tween(QUICK_DISHES_OUT_MS) else snap()
        },
        label = "mensa_bag_dishes_shown",
    ) { if (it == EnterExitState.PostExit && !scrubbed()) 0f else 1f }
}

/** 0 until the page leaves, then 1 once the bag is home: its flight, after the page's hold. */
@Composable
private fun rememberBagFlight(scrubbed: () -> Boolean): State<Float> {
    val visibility = LocalMensaShared.current?.visibility ?: return remember { mutableFloatStateOf(0f) }
    return visibility.transition.animateFloat(
        transitionSpec = {
            if (targetState == EnterExitState.PostExit) {
                tween(SheetMotion.PAGE_MS, delayMillis = if (scrubbed()) MENSA_BAG_RETURN_MS else 0, easing = FastOutSlowInEasing)
            } else {
                snap()
            }
        },
        label = "mensa_bag_flight",
    ) { if (it == EnterExitState.PostExit) 1f else 0f }
}

/** One half of the bag, drawn at the frame [openness] gives it. */
@Composable
private fun BoxScope.BagLayerImage(
    @DrawableRes drawable: Int,
    openness: State<Float>,
    reservationId: Int,
    layer: BagLayer,
    modifier: Modifier = Modifier.matchParentSize(),
    shared: () -> Boolean = { true },
) {
    val context = LocalContext.current
    val painter = remember(drawable, openness) { BagLayerPainter(BagLayers.get(context, drawable)) { openness.value } }
    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier
            .mensaBagElement(reservationId, layer, shared)
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
 * where the dishes are in while the page holds still, and on to 0 as the bag flies home.
 */
@Composable
private fun rememberHeroTimeline(hasDishes: Boolean, scrubbed: () -> Boolean): State<Float> {
    val visibility = LocalMensaShared.current?.visibility
    if (visibility == null || !hasDishes) return remember { mutableFloatStateOf(1f) }
    return visibility.transition.animateFloat(
        transitionSpec = {
            if (targetState == EnterExitState.Visible) {
                // Once the bag has landed.
                tween(durationMillis = HERO_MS, delayMillis = SheetMotion.PAGE_MS, easing = LinearEasing)
            } else if (!scrubbed()) {
                // No finger to follow: the bag stays shut and the dishes fade out where they are.
                snap(delayMillis = QUICK_DISHES_OUT_MS)
            } else {
                // The bag opens and the dishes go in while the page holds; it shuts on its way home.
                keyframes {
                    durationMillis = MENSA_BAG_RETURN_MS + SheetMotion.PAGE_MS / 2
                    1f at 0
                    DISHES_IN at MENSA_BAG_RETURN_MS
                }
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
