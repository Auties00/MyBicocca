package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.toSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import it.attendance100.mybicocca.ui.component.modal.LocalSheetBackProgress
import it.attendance100.mybicocca.ui.component.modal.SheetMotion
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.MENSA_MENU_CARD_ASPECT
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.mensaMenuCardFanDrawables
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.mensaMenuCardShadow
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

/**
 * Displays the seven campus menu cards fanned symmetrically above the "Menù e ordini" tile.
 *
 * When [playIntro] is true, the deck enters stacked vertically in the Layered Wallet pose and
 * springs open like a card fan into the resting Poker Hand spread (folding gently back during
 * predictive-back dismissal).
 */
@Composable
internal fun MensaMenuCardFan(
    playIntro: Boolean = false,
    onIntroPlayed: () -> Unit = {},
    modifier: Modifier = Modifier,
    cards: List<Int> = mensaMenuCardFanDrawables,
) {
    val spread = remember { Animatable(if (playIntro) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (spread.value < 1f) {
            onIntroPlayed()
            delay(75L)
            spread.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.78f,
                    stiffness = 240f,
                    visibilityThreshold = 0.0004f,
                ),
            )
        }
    }

    val sheetBack = LocalSheetBackProgress.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(106.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        if (cards.isEmpty()) return@BoxWithConstraints

        val cardWidth = (maxWidth * 0.275f).coerceIn(42.dp, 50.dp)
        val cardHeight = cardWidth / MENSA_MENU_CARD_ASPECT
        val mid = (cards.size - 1) / 2f
        val maxRank = cards.size - 1

        // Read Animatable and back progress inside draw/layout phase where possible; compute once for slot poses.
        val progress = (spread.value * (1f - (sheetBack() * 1.25f).coerceAtMost(1f))).coerceIn(0f, 1f)

        cards.forEachIndexed { index, resId ->
            val offsetFromCenter = index - mid
            val rank = fanRank(index, cards.size)
            val walletDepth = (maxRank - rank).coerceAtLeast(0)

            FanCard(
                drawableRes = resId,
                cardWidth = cardWidth,
                cardHeight = cardHeight,
                index = index,
                offsetFromCenter = offsetFromCenter,
                walletDepth = walletDepth,
                zOrder = rank.toFloat(),
                progress = progress,
            )
        }
    }
}

@Composable
private fun FanCard(
    @DrawableRes drawableRes: Int,
    cardWidth: Dp,
    cardHeight: Dp,
    index: Int,
    offsetFromCenter: Float,
    walletDepth: Int,
    zOrder: Float,
    progress: Float,
) {
    // Resting state (progress = 1f): symmetrical Poker Hand Spread anchored at each card's bottom center.
    val fanScale = 1f
    val fanTopY = 8.dp + 4.5.dp * abs(offsetFromCenter)
    val fanOffsetX = cardWidth * 0.23f * offsetFromCenter
    val fanRotation = offsetFromCenter * FAN_LEAN

    // Entrance state (progress = 0f): vertical Layered Wallet Deck with back cards peeking cleanly above.
    val walletScale = (1.06f - walletDepth * 0.036f).coerceAtLeast(0.75f)
    val walletTopY = 20.dp - (3.0.dp * walletDepth)
    val walletOffsetX = if (walletDepth == 0) 0.dp else if (index % 2 == 0) (-1.2).dp else 1.2.dp
    val walletBrightness = if (walletDepth == 0) 1f else (1f - walletDepth * 0.045f).coerceAtLeast(0.73f)

    val curScale = lerp(walletScale, fanScale, progress)
    val curTopY = lerp(walletTopY, fanTopY, progress)
    val curBottomY = curTopY + cardHeight * curScale
    val curOffsetX = lerp(walletOffsetX, fanOffsetX, progress)
    val curRotation = lerp(0f, fanRotation, progress)
    val curBrightness = lerp(walletBrightness, 1f, progress)

    val colorFilter = if (curBrightness < 0.999f) {
        ColorFilter.colorMatrix(ColorMatrix().apply { setToScale(curBrightness, curBrightness, curBrightness, 1f) })
    } else {
        null
    }

    Image(
        painter = painterResource(drawableRes),
        contentDescription = null,
        colorFilter = colorFilter,
        modifier = Modifier
            .zIndex(zOrder)
            // Placed rather than translated, so the card flies from where it is drawn.
            .offset { IntOffset(curOffsetX.roundToPx(), (curBottomY - cardHeight).toPx().roundToInt()) }
            .mensaMenuCardHero(drawableRes, lean = { curRotation })
            .size(cardWidth, cardHeight)
            .graphicsLayer {
                transformOrigin = TransformOrigin(0.5f, 1f)
                scaleX = curScale
                scaleY = curScale
            }
            .mensaMenuCardShadow(offsetY = 2.dp, doubleLayer = true),
    )
}

/** How far apart, in degrees, neighbouring cards of the fan lean. */
private const val FAN_LEAN = 8f

/** Where a card stacks in the fan: the middle card on top, then outwards, right over left. */
private fun fanRank(index: Int, size: Int): Int {
    val mid = (size - 1) / 2f
    val sideRank = if (index > mid) 1 else 0
    return ((size - 1) / 2 - abs(index - mid).toInt()) * 2 + sideRank
}

/** Where each menu card is drawn in the window, on the hub's fan and on the place picker. */
internal class MensaMenuCardStage {
    val fan = mutableStateMapOf<Int, Rect>()
    val picker = mutableStateMapOf<Int, Rect>()
}

internal val LocalMensaMenuCardStage = staticCompositionLocalOf<MensaMenuCardStage?> { null }

/**
 * Flies a menu card between the hub's fan and the place picker: the card on the page coming in
 * starts where its twin on the other page is and lands in its own place, while the twin hides.
 * It is a flight of its own rather than a shared element, so a back gesture that is let go
 * rewinds it with the page. The fan's cards lean by [lean]; the picker's (no [lean]) start from
 * their card's lean in the fan and straighten as they land.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Modifier.mensaMenuCardHero(@DrawableRes card: Int, lean: (() -> Float)? = null): Modifier {
    val onFan = lean != null
    val fanIndex = mensaMenuCardFanDrawables.indexOf(card)
    val fanLean = if (fanIndex < 0) 0f else (fanIndex - (mensaMenuCardFanDrawables.size - 1) / 2f) * FAN_LEAN
    // The overlay ignores the fan's zIndex, so flying cards stack by their place in the fan.
    val fanZ = if (fanIndex < 0) 0f else 1f + fanRank(fanIndex, mensaMenuCardFanDrawables.size)
    val scopes = LocalMensaShared.current
    val stage = LocalMensaMenuCardStage.current
    if (scopes == null || stage == null) {
        return graphicsLayer {
            transformOrigin = TransformOrigin(0.5f, 1f)
            rotationZ = lean?.invoke() ?: 0f
        }
    }
    val own = if (onFan) stage.fan else stage.picker
    val twin = if (onFan) stage.picker else stage.fan
    DisposableEffect(own, card) { onDispose { own.remove(card) } }
    val transition = scopes.visibility.transition
    // 0 as its page comes in, 1 once it is there: how far a flying card has landed.
    val landed = transition.animateFloat(
        transitionSpec = { tween(SheetMotion.PAGE_MS, easing = FastOutSlowInEasing) },
        label = "mensa_menu_card_landed",
    ) { if (it == EnterExitState.PreEnter) 0f else 1f }
    val flying = remember(transition, twin, card) {
        { transition.currentState == EnterExitState.PreEnter && transition.targetState == EnterExitState.Visible && card in twin }
    }
    val handedOver = remember(transition, twin, card) {
        { transition.targetState == EnterExitState.PostExit && card in twin }
    }
    return with(scopes.transition) {
        this@mensaMenuCardHero
            // Its position, not its bounds, which a page sliding out of the sheet would clip.
            .onGloballyPositioned { own[card] = Rect(it.positionInWindow(), it.size.toSize()) }
            .renderInSharedTransitionScopeOverlay(renderInOverlay = flying, zIndexInOverlay = fanZ)
    }.graphicsLayer {
        if (handedOver()) {
            alpha = 0f
            return@graphicsLayer
        }
        if (!flying()) return@graphicsLayer
        val from = twin[card]
        val self = own[card]
        if (from == null || self == null || self.width <= 0f) return@graphicsLayer
        val t = landed.value
        transformOrigin = TransformOrigin(0f, 0f)
        val scale = lerp(from.width / self.width, 1f, t)
        scaleX = scale
        scaleY = scale
        translationX = (from.left - self.left) * (1f - t)
        translationY = (from.top - self.top) * (1f - t)
    }.graphicsLayer {
        transformOrigin = TransformOrigin(0.5f, 1f)
        val t = if (flying()) landed.value else 1f
        rotationZ = if (lean != null) lean() * t else fanLean * (1f - t)
    }
}

@Preview(name = "Menu card fan · hub tile")
@Composable
private fun MensaMenuCardFanPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        Box(
            modifier = Modifier
                .width(174.dp)
                .height(192.dp)
                .background(MaterialTheme.colorScheme.surface),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 52.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {}
            MensaMenuCardFan(modifier = Modifier.align(Alignment.TopCenter))
        }
    }
}
