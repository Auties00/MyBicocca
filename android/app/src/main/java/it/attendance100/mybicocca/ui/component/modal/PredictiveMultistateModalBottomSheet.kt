package it.attendance100.mybicocca.ui.component.modal

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import kotlin.coroutines.cancellation.CancellationException

/**
 * A [ModalBottomSheet] that hosts exactly two states — a [rootState] and one sub-state — and wires
 * up predictive back for both levels:
 *
 *  - From the root, a back gesture scrubs the sheet closed (same machinery as [PredictiveModalBottomSheet]).
 *  - From the sub-state, a back gesture scrubs the inner transition back to the root rather than closing,
 *    then calls [onNavigateToRoot] so the caller can reset [targetState].
 *
 * The inner transition is seekable, so the back gesture drags it 1:1. [content] receives a
 * `subStateProgress` (0 at root, 1 at sub) that follows the scrub — use it to drive sub-state-only
 * affordances (e.g. a cancel button fading/sliding in).
 *
 * Generic over the state type [T] so the sub-state can carry data (e.g. `ExamCall?`). [AnimatedContent]
 * keeps the outgoing value alive through the exit animation, so [content] can safely render it without
 * guarding against the caller having already reset [targetState] to [rootState].
 *
 * @param targetState the current state; equal to [rootState] means "at root", anything else is the sub-state.
 * @param rootState the value that represents the root level.
 * @param onNavigateToRoot invoked when a back gesture (or scrim tap) from the sub-state completes; the
 *   caller should reset [targetState] to [rootState] here.
 * @param canInteract gates both back handlers and swipe/scrim dismissal — pass `false` while a blocking
 *   operation is in flight (e.g. submitting) to lock the sheet.
 * @param contentModifier applied to the inner [AnimatedContent]; use it to fix the sheet height (to avoid
 *   resizing between states) or to inset/clip the content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> PredictiveMultistateModalBottomSheet(
    targetState: T,
    rootState: T,
    onNavigateToRoot: () -> Unit,
    onDismiss: () -> Unit,
    modalColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    canInteract: Boolean = true,
    duration: Int = 400,
    sizeDuration: Int = duration,
    contentModifier: Modifier = Modifier.fillMaxWidth(),
    content: @Composable (state: T, subStateProgress: Float) -> Unit,
) {
    val inSubState = targetState != rootState

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        // Block swipe/programmatic hiding while in the sub-state (back returns to root first) or locked.
        confirmValueChange = { newValue ->
            if (newValue == SheetValue.Hidden) !inSubState && canInteract else true
        },
    )

    val closeSeekableState = remember { SeekableTransitionState(true) }
    val closeTransition = rememberTransition(closeSeekableState, label = "closeTransition")

    val innerSeekableState = remember { SeekableTransitionState(targetState) }
    val innerTransition = rememberTransition(innerSeekableState, label = "innerTransition")

    // Drive the inner transition to follow the caller's state for non-gesture changes (e.g. tapping
    // into the sub-state, or a scrim-tap return handled below). The guard avoids re-animating over a
    // gesture-driven animateTo we just kicked off in the back handler.
    LaunchedEffect(targetState) {
        if (innerSeekableState.targetState != targetState) {
            innerSeekableState.animateTo(targetState, tween(durationMillis = duration))
        }
    }

    // 0 at root, 1 at the sub-state; follows the seekable scrub so callers can track the gesture.
    val subStateProgress by innerTransition.animateFloat(
        transitionSpec = { tween(durationMillis = duration) },
        label = "subStateProgress",
    ) { state -> if (state == rootState) 0f else 1f }

    ModalBottomSheet(
        onDismissRequest = {
            when {
                inSubState -> onNavigateToRoot()
                canInteract -> onDismiss()
            }
        },
        sheetState = sheetState,
        shape = shape,
        containerColor = modalColor,
    ) {
        // Root level: back scrubs the sheet closed.
        PredictiveBackHandler(enabled = !inSubState && canInteract) { progress ->
            try {
                progress.collect { backEvent ->
                    closeSeekableState.seekTo(backEvent.progress, targetState = false)
                }
                closeSeekableState.animateTo(false)
                onDismiss()
            } catch (_: CancellationException) {
                closeSeekableState.animateTo(true)
            }
        }

        // Sub-state: back scrubs the inner transition back to the root rather than closing.
        PredictiveBackHandler(enabled = inSubState && canInteract) { progress ->
            try {
                progress.collect { backEvent ->
                    innerSeekableState.seekTo(backEvent.progress, targetState = rootState)
                }
                onNavigateToRoot()
                innerSeekableState.animateTo(rootState)
            } catch (_: CancellationException) {
                innerSeekableState.animateTo(targetState)
            }
        }

        closeTransition.AnimatedContent(
            modifier = Modifier.fillMaxWidth(),
            transitionSpec = {
                ContentTransform(
                    targetContentEnter = fadeIn(tween(durationMillis = duration)),
                    initialContentExit = fadeOut(tween(durationMillis = duration)),
                    sizeTransform = SizeTransform(clip = true) { _, _ ->
                        tween(durationMillis = sizeDuration)
                    },
                )
            },
            contentKey = { it },
        ) { isVisible ->
            if (isVisible) {
                innerTransition.AnimatedContent(
                    modifier = contentModifier,
                    transitionSpec = {
                        val enteringSubState = targetState != rootState
                        val enter = slideInVertically(tween(durationMillis = duration)) { h ->
                            if (enteringSubState) h else -h
                        } + fadeIn(tween(durationMillis = duration), initialAlpha = 0.2f)
                        val exit = slideOutVertically(tween(durationMillis = duration)) { h ->
                            if (enteringSubState) -h else h
                        } + fadeOut(tween(durationMillis = duration), targetAlpha = 0.2f)
                        ContentTransform(
                            targetContentEnter = enter,
                            initialContentExit = exit,
                            sizeTransform = SizeTransform(clip = true) { _, _ ->
                                tween(durationMillis = sizeDuration)
                            },
                        )
                    },
                    // Collapse all sub-state values to one key so we only animate on root <-> sub crossings.
                    contentKey = { it == rootState },
                ) { state ->
                    content(state, subStateProgress)
                }
            } else {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.dp)
                )
            }
        }
    }
}
