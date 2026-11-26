package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
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
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.mensa.MensaDish
import it.attendance100.mybicocca.ui.component.modal.SheetMotion
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.MensaDishArtwork
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.mensaDishDrawable
import kotlin.math.abs

/** The bag drawing's width over its height. */
private const val BAG_ASPECT_RATIO = 1077.8f / 847.1f

/** Most plates shown coming out of the bag; a bigger order still lists every dish below. */
private const val MAX_PLATES = 6
private val HeroPlateSize = 64.dp
private val HeroBagWidth = 150.dp

/** Each plate starts this share of the whole rise after the one before it. */
private const val PLATE_STAGGER = 0.12f

/**
 * The take-away bag of booking [reservationId]. In the Mensa sheet it is a shared element, so the
 * bag on the hub's card flies to the top of the booking it opens, and back.
 */
@Composable
internal fun MensaBookingBag(reservationId: Int, width: Dp, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.mensa_art_takeaway_bag),
        contentDescription = null,
        modifier = modifier
            .mensaBagElement(reservationId)
            .width(width)
            .aspectRatio(BAG_ASPECT_RATIO),
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun Modifier.mensaBagElement(reservationId: Int): Modifier {
    val scopes = LocalMensaShared.current ?: return this
    return with(scopes.transition) {
        this@mensaBagElement.sharedElement(
            sharedContentState = rememberSharedContentState(key = "mensa_bag_$reservationId"),
            animatedVisibilityScope = scopes.visibility,
        )
    }
}

/**
 * The top of a booking: its bag, with the ordered dishes rising out of it once the bag has landed.
 * The rise is part of the page change, so the back gesture drops the dishes back into the bag as
 * it goes.
 */
@Composable
internal fun MensaBookingBagHero(reservationId: Int, dishes: List<MensaDish>, modifier: Modifier = Modifier) {
    val shown = dishes.take(MAX_PLATES)
    val rise = rememberDishRise()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (shown.isEmpty()) 130.dp else 184.dp),
    ) {
        // Drawn before the bag, so a dish still inside it is hidden behind its front.
        shown.forEachIndexed { index, dish ->
            val art = remember(dish) { MensaDishArtwork.forDish(dish) }
            MensaPlate(
                art = art,
                size = HeroPlateSize,
                drawable = mensaDishDrawable(dish.id, art.drawable),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .risingOutOfBag(rise, index, shown.size),
            )
        }
        MensaBookingBag(
            reservationId = reservationId,
            width = HeroBagWidth,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/** 0 with every dish in the bag, 1 with all of them out; follows the page's own transition. */
@Composable
private fun rememberDishRise(): State<Float> {
    val visibility = LocalMensaShared.current?.visibility ?: return remember { mutableFloatStateOf(1f) }
    return visibility.transition.animateFloat(
        transitionSpec = {
            if (targetState == EnterExitState.Visible) {
                // Out once the bag has landed, one dish after the other.
                tween(durationMillis = 700, delayMillis = SheetMotion.PAGE_MS + 100, easing = LinearEasing)
            } else {
                tween(durationMillis = SheetMotion.PAGE_MS / 2, easing = LinearEasing)
            }
        },
        label = "mensa_bag_dishes",
    ) { if (it == EnterExitState.Visible) 1f else 0f }
}

/**
 * Moves plate [index] of [count] from inside the bag to its place in a fan above it: the middle
 * ones highest, the outer ones lower and tilted outwards.
 */
private fun Modifier.risingOutOfBag(rise: State<Float>, index: Int, count: Int): Modifier = graphicsLayer {
    val window = 1f - PLATE_STAGGER * (count - 1)
    val own = ((rise.value - PLATE_STAGGER * index) / window).coerceIn(0f, 1f)
    val eased = EaseOutBack.transform(own)
    val fromCentre = index - (count - 1) / 2f
    val spacing = if (count > 1) minOf(62.dp, 250.dp / (count - 1)) else 0.dp
    val endX = spacing * fromCentre
    val endTop = 18.dp + 10.dp * abs(fromCentre)
    val startX = 8.dp * fromCentre
    val startTop = 104.dp
    translationX = lerp(startX, endX, eased).toPx()
    translationY = lerp(startTop, endTop, eased).toPx()
    val scale = lerp(0.6f, 1f, eased)
    scaleX = scale
    scaleY = scale
    rotationZ = 7f * fromCentre * eased
    // Hidden at the very start, while the bag may still be flying in over the empty spot.
    alpha = (own * 4f).coerceAtMost(1f)
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
