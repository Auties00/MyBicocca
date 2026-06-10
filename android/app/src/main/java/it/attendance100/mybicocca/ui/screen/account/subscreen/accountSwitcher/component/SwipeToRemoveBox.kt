package it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import it.attendance100.mybicocca.core.os.rememberHapticManager
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Swipe-left-to-remove gesture with a "seal-break" threshold detent.
 *
 * Pre-pop the drag is modelled as a non-linear spring that loads up against a wall: the
 * finger-to-card ratio starts at [INITIAL_DRAG_RATIO] and decays to zero as the finger
 * approaches the commit threshold (see [resistedOffset]). At the threshold the seal breaks —
 * a short pop animation flings the card forward into a 1:1-with-finger position, the haptic
 * fires, and the gesture switches into a free zone where every subsequent finger pixel moves
 * the card one pixel. Releasing while past the threshold commits; releasing inside the free
 * zone but back-dragged below the threshold snaps the card home and re-seals.
 *
 * Only leftward travel counts: the accumulated drag is clamped at the resting position so
 * the card can never pull right. Once popped, the seal stays broken for the rest of the
 * gesture — pulling back past the threshold doesn't re-stick it, and each threshold crossing
 * in either direction re-taps the haptic as pure boundary feedback, with no visual pop. The
 * one exception is dragging the card all the way back to rest mid-gesture (an exact-equality
 * check, safe because of the rightward clamp), which re-seals it so a fresh forward crossing
 * earns a new pop and haptic.
 *
 * A commit is final: the card slides fully off-screen and [onConfirmRemove] fires once. The
 * caller is expected to remove the row right away — the card never comes back on its own.
 */
@Composable
fun SwipeToRemoveBox(
    onConfirmRemove: () -> Unit,
    background: @Composable BoxScope.(armed: Boolean, revealed: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val haptic = rememberHapticManager()
    val scope = rememberCoroutineScope()
    val motion = MaterialTheme.motionScheme

    BoxWithConstraints(modifier) {
        val widthPx = with(density) { maxWidth.toPx() }
        val thresholdPx = widthPx * COMMIT_FRACTION

        val offset = remember { Animatable(0f) }
        var rawDrag by remember { mutableFloatStateOf(0f) }
        var armed by remember { mutableStateOf(false) }
        var popped by remember { mutableStateOf(false) }

        fun resetState() {
            rawDrag = 0f
            armed = false
            popped = false
        }

        Box(modifier = Modifier.matchParentSize()) {
            background(armed, offset.value < -1f)
        }
        Box(
            modifier = Modifier
                .offset { IntOffset(offset.value.roundToInt(), 0) }
                .pointerInput(widthPx) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val commit = armed
                            scope.launch {
                                if (commit) {
                                    offset.animateTo(-widthPx, tween(durationMillis = 220))
                                    onConfirmRemove()
                                } else {
                                    offset.animateTo(0f, motion.defaultSpatialSpec())
                                    resetState()
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                offset.animateTo(0f, motion.defaultSpatialSpec())
                                resetState()
                            }
                        },
                        onHorizontalDrag = { _, delta ->
                            rawDrag = (rawDrag + delta).coerceAtMost(0f)
                            val abs = -rawDrag

                            if (!popped) {
                                if (abs >= thresholdPx) {
                                    popped = true
                                    armed = true
                                    haptic.tap()
                                    scope.launch {
                                        offset.animateTo(
                                            targetValue = -abs,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioLowBouncy,
                                                stiffness = Spring.StiffnessHigh,
                                            ),
                                        )
                                    }
                                } else {
                                    val mapped = resistedOffset(abs, thresholdPx)
                                    scope.launch { offset.snapTo(-mapped) }
                                }
                            } else {
                                scope.launch { offset.snapTo(-abs) }
                                val nowArmed = abs >= thresholdPx
                                if (nowArmed != armed) {
                                    armed = nowArmed
                                    haptic.tap()
                                }
                                if (rawDrag == 0f) {
                                    popped = false
                                    armed = false
                                }
                            }
                        },
                    )
                },
        ) {
            content()
        }
    }
}

/**
 * Quadratic resistance curve, equivalent to pushing against a spring that stiffens with
 * distance. At `abs = 0` the ratio is [INITIAL_DRAG_RATIO]; at `abs = threshold` the
 * derivative is zero — the card has effectively hit a wall. The integral lands the card at
 * `INITIAL_DRAG_RATIO / 2 * threshold` when the finger has travelled the full threshold,
 * which is what makes the subsequent pop look like a leap.
 */
private fun resistedOffset(abs: Float, thresholdPx: Float): Float {
    if (thresholdPx <= 0f) return 0f
    val clamped = abs.coerceAtMost(thresholdPx)
    return INITIAL_DRAG_RATIO * clamped * (1f - clamped / (2f * thresholdPx))
}

private const val COMMIT_FRACTION = 0.45f
private const val INITIAL_DRAG_RATIO = 0.8f
