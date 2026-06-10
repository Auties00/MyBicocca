package it.attendance100.mybicocca.ui.component.modal

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.ui.component.feedback.SnackbarScope
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * The app's standard modal bottom sheet: a [ModalBottomSheet] wired for predictive back.
 * The back gesture drives a seekable close transition, so content shrinks/fades in step with
 * the user's finger and springs back if the gesture is cancelled; [content] receives that
 * transition plus its 0→1 progress to stage its own exit choreography.
 *
 * Dismissal can be vetoed via [confirmDismiss]. The veto runs before the hide settles: a
 * refused swipe/scrim dismissal springs the sheet straight back (and the caller can morph its
 * content into a confirm page) rather than fully hiding and visibly re-opening. The same veto
 * also guards [ModalBottomSheet]'s own dismiss request, which covers paths that bypass
 * `confirmValueChange` (e.g. the dialog's back handling when no content-level back handler is
 * enabled). Back gestures do not route through the veto while a content-level back handler is
 * active.
 *
 * The sheet is its own window, so the app-root snackbar host could never draw over it; content
 * is wrapped in a sheet-local snackbar scope instead, making feedback contextual — messages
 * raised inside the sheet render at the bottom of the sheet's own content, within its bounds.
 *
 * @param confirmDismiss veto for swipe/scrim dismissal: return false to keep the sheet open,
 * e.g. to ask for confirmation first.
 * @param gesturesEnabled while false the sheet cannot be dragged — for content where dismissal
 * must stay an explicit act (e.g. mid-wizard). The drag handle stays visible but muted to the
 * disabled tone, so it reads as inert rather than vanishing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictiveModalBottomSheet(
    onDismiss: () -> Unit,
    modalColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    duration: Int = 400,
    sizeDuration: Int = duration,
    confirmDismiss: () -> Boolean = { true },
    gesturesEnabled: Boolean = true,
    content: @Composable (closeTransition: Transition<Boolean>, progress: Float) -> Unit
) {
    val latestConfirmDismiss by rememberUpdatedState(confirmDismiss)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { target ->
            target != SheetValue.Hidden || latestConfirmDismiss()
        },
    )
    val scope = rememberCoroutineScope()
    val closeSeekableState = remember { SeekableTransitionState(true) }
    val closeTransition = rememberTransition(closeSeekableState, label = "closeTransition")

    ModalBottomSheet(
        onDismissRequest = {
            if (confirmDismiss()) onDismiss() else scope.launch { sheetState.show() }
        },
        sheetState = sheetState,
        sheetGesturesEnabled = gesturesEnabled,
        dragHandle = {
            if (gesturesEnabled) {
                BottomSheetDefaults.DragHandle()
            } else {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
                )
            }
        },
        shape = shape,
        containerColor = modalColor,
        scrimColor = scrimColor,
    ) {
        PredictiveBackHandler { progress ->
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

        SnackbarScope {
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
                contentKey = { it }
            ) { isVisible ->
                if (isVisible) {
                    val progress by this@AnimatedContent.transition.animateFloat(
                        transitionSpec = { tween(durationMillis = duration) },
                        label = "progress"
                    ) { state ->
                        if (state == EnterExitState.Visible) 0f else 1f
                    }

                    content(closeTransition, progress)
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
}
