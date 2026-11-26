package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.mensa.MensaDish
import it.attendance100.mybicocca.ui.component.modal.SheetMotion
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.MensaDishArtwork
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.mensaAnimatedImage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.mensaDishDrawable
import kotlin.math.abs

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

/** Once the bag has landed it opens; the dishes start rising this far in, while it is still opening. */
private const val RISE_START_MS = 250
private const val RISE_MS = 700

/** From the bag starting to open to the last dish in place, when the bag closes again. */
private const val HERO_MS = RISE_START_MS + RISE_MS

/** The two halves of the bag, each its own shared element so the dishes can come out between them. */
private enum class BagLayer { Back, Front }

/**
 * The take-away bag of booking [reservationId]. In the Mensa sheet it is a shared element, so the
 * bag on the hub's card flies to the top of the booking it opens, and back.
 */
@Composable
internal fun MensaBookingBag(reservationId: Int, width: Dp, modifier: Modifier = Modifier) {
    Box(modifier.width(width).aspectRatio(BAG_ASPECT_RATIO)) {
        // The booking draws the bag in two layers; here the front one is the whole bag.
        Spacer(Modifier.matchParentSize().mensaBagElement(reservationId, BagLayer.Back))
        Image(
            painter = painterResource(R.drawable.mensa_art_takeaway_bag_simple),
            contentDescription = null,
            modifier = Modifier.matchParentSize().mensaBagElement(reservationId, BagLayer.Front),
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun Modifier.mensaBagElement(reservationId: Int, layer: BagLayer): Modifier {
    val scopes = LocalMensaShared.current ?: return this
    return with(scopes.transition) {
        this@mensaBagElement.sharedElement(
            sharedContentState = rememberSharedContentState(key = "mensa_bag_${layer.name}_$reservationId"),
            animatedVisibilityScope = scopes.visibility,
            zIndexInOverlay = layer.ordinal.toFloat(),
        )
    }
}

/**
 * The top of a booking: its bag, which once landed opens, lets the ordered dishes rise out of it
 * and closes again. The whole sequence is part of the page change, so the back gesture drops the
 * dishes back into the bag as it goes.
 */
@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
internal fun MensaBookingBagHero(reservationId: Int, dishes: List<MensaDish>, modifier: Modifier = Modifier) {
    val shown = dishes.take(MAX_PLATES)
    val timeline = rememberHeroTimeline()
    val open by remember(shown.isEmpty()) {
        derivedStateOf { shown.isNotEmpty() && timeline.value > 0f && timeline.value < 1f }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (shown.isEmpty()) 130.dp else HeroHeight),
    ) {
        BagLayerImage(R.drawable.mensa_art_takeaway_bag_open_back, open, reservationId, BagLayer.Back)
        // Between the bag's two halves, so a dish still inside is hidden behind the front.
        shown.forEachIndexed { index, dish ->
            val art = remember(dish) { MensaDishArtwork.forDish(dish) }
            MensaPlate(
                art = art,
                size = HeroPlateSize,
                drawable = mensaDishDrawable(dish.id, art.drawable),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .risingOutOfBag(timeline, index, shown.size),
            )
        }
        BagLayerImage(R.drawable.mensa_art_takeaway_bag_open_front, open, reservationId, BagLayer.Front)
    }
}

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
private fun BoxScope.BagLayerImage(@DrawableRes drawable: Int, open: Boolean, reservationId: Int, layer: BagLayer) {
    Image(
        painter = rememberAnimatedVectorPainter(mensaAnimatedImage(drawable), atEnd = open),
        contentDescription = null,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .mensaBagElement(reservationId, layer)
            .width(HeroBagWidth)
            .aspectRatio(BAG_ASPECT_RATIO),
    )
}

/**
 * 0 before the bag opens, 1 once it has closed over the dishes; follows the page's own transition.
 * The bag is open in between, so the back gesture opens it again to take the dishes back.
 */
@Composable
private fun rememberHeroTimeline(): State<Float> {
    val visibility = LocalMensaShared.current?.visibility ?: return remember { mutableFloatStateOf(1f) }
    return visibility.transition.animateFloat(
        transitionSpec = {
            if (targetState == EnterExitState.Visible) {
                tween(durationMillis = HERO_MS, delayMillis = SheetMotion.PAGE_MS, easing = LinearEasing)
            } else {
                tween(durationMillis = SheetMotion.PAGE_MS / 2, easing = LinearEasing)
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
