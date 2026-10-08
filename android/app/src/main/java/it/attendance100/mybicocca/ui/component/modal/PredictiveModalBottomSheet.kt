package it.attendance100.mybicocca.ui.component.modal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import it.attendance100.mybicocca.ui.component.feedback.SnackbarScope
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Handle on a mounted [PredictiveModalBottomSheet] for whoever owns its lifetime — the modal scene
 * plays the sheet's exit through it when the sheet is closed by navigation rather than by a user
 * gesture, so programmatic closes animate out instead of vanishing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Stable
class ModalSheetController {
    internal var state: SheetState? by mutableStateOf(null)

    /** True while a programmatic hide runs: it bypasses the content's dismiss veto. */
    internal var closing: Boolean by mutableStateOf(false)

    /** Set once a programmatic hide started, so only that hide is undone by [show]. */
    private var hiddenByController: Boolean = false

    /**
     * Animates the sheet out and returns once it is hidden. Returns immediately if it is not mounted
     * or already hidden, and early if [show] brings the sheet back meanwhile.
     *
     * Another sheet animation can cancel the slide out halfway: a scrim tap or back press while
     * the sheet is leaving starts Material's own hide, and a finger can grab the sheet. That
     * cancellation must not reach the caller. The modal scene waits for this hide before it
     * removes the sheet, and a sheet that is never removed leaves its invisible, full-screen window
     * over the app, where it swallows every touch. Instead the interruption is waited out and
     * the hide repeated until the sheet is really gone.
     */
    suspend fun hide() {
        val sheet = state ?: return
        if (!sheet.isVisible) return
        hiddenByController = true
        closing = true
        try {
            while (closing && sheet.isVisible) {
                try {
                    sheet.hide()
                } catch (interrupted: CancellationException) {
                    // Our own cancellation (the scene left composition) still propagates.
                    currentCoroutineContext().ensureActive()
                    snapshotFlow { sheet.isAnimationRunning }.first { !it }
                    // A drag outranks animations and refuses a new one outright: retry at most
                    // once a frame until the finger lets go.
                    withFrameNanos {}
                }
            }
        } finally {
            closing = false
        }
    }

    /**
     * Animates the sheet back in after a programmatic [hide] (the sheet came back on the stack while
     * it was hiding), ending that hide. The first show is left to Material's own entry animation.
     */
    suspend fun show() {
        val sheet = state ?: return
        if (!hiddenByController) return
        hiddenByController = false
        closing = false
        sheet.show()
    }
}

/**
 * The app's one modal bottom sheet container: a Material3 [ModalBottomSheet] (its own window,
 * scrim, drag handle, accessibility actions, IME and edge-to-edge insets) with the app's
 * conventions on top. Every modal — navigation sheets and the few local ones — goes through it so
 * they all behave the same.
 *
 * Back is fully native: on the sheet's first page the system back gesture runs Material's own
 * predictive-back animation (the sheet scales with the finger, springs back on cancel, slides out
 * on commit); deeper pages are handled by the [SheetPager] inside, whose handler is registered
 * later and therefore wins while it is enabled.
 *
 * Dismissal can be vetoed via [confirmDismiss]. The veto runs before the hide settles: a refused
 * swipe or scrim dismissal springs the sheet straight back (and the content can morph into a
 * confirm page) rather than fully hiding and visibly re-opening. While [gesturesEnabled] is false
 * back is routed through the veto as well, without Material's predictive scale. A programmatic
 * close through [controller] bypasses the veto — by then navigation already decided.
 *
 * The sheet is its own window, so the app-root snackbar host can never draw over it; content is
 * wrapped in a sheet-local snackbar scope instead, so messages raised inside the sheet render at
 * the bottom of the sheet's own content.
 *
 * @param onDismiss called once the user dismissed the sheet (swipe, scrim tap, back). Must be
 * idempotent: Material can report the same dismissal from more than one path.
 * @param gesturesEnabled while false the sheet cannot be dragged — for content where dismissal
 * must stay an explicit act (e.g. mid-wizard). The drag handle stays visible but muted.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictiveModalBottomSheet(
    onDismiss: () -> Unit,
    modalColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    confirmDismiss: () -> Boolean = { true },
    gesturesEnabled: Boolean = true,
    controller: ModalSheetController? = null,
    contentWindowInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.modalWindowInsets },
    dragHandle: @Composable (() -> Unit)? = { SheetDragHandle(gesturesEnabled) },
    content: @Composable () -> Unit,
) {
    val latestConfirmDismiss by rememberUpdatedState(confirmDismiss)
    val latestOnDismiss by rememberUpdatedState(onDismiss)
    val sheetController = controller ?: remember { ModalSheetController() }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { target ->
            target != SheetValue.Hidden || sheetController.closing || latestConfirmDismiss()
        },
    )
    DisposableEffect(sheetController, sheetState) {
        sheetController.state = sheetState
        onDispose { if (sheetController.state === sheetState) sheetController.state = null }
    }
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = {
            when {
                sheetController.closing -> Unit
                latestConfirmDismiss() -> latestOnDismiss()
                else -> scope.launch { sheetState.show() }
            }
        },
        sheetState = sheetState,
        sheetGesturesEnabled = gesturesEnabled,
        dragHandle = dragHandle,
        shape = shape,
        scrimColor = scrimColor,
        contentWindowInsets = contentWindowInsets,
        containerColor = modalColor,
    ) {
        // While the content locks the sheet, back must not reach Material's handler: its predictive
        // scale would play, the vetoed hide would not, and the sheet would stay shrunk. Swallow it
        // here and let the veto decide (it may morph into a confirm page instead). Registered before
        // the content, so in-sheet pagers and page handlers still take precedence.
        BackHandler(enabled = !gesturesEnabled) {
            if (latestConfirmDismiss()) latestOnDismiss()
        }
        val bodyScroll = remember { SheetBodyScrollConnection() }
        Box(Modifier.nestedScroll(bodyScroll)) {
            SnackbarScope { content() }
        }
    }
}

/** The standard drag handle, muted (but kept) while the sheet's gestures are locked. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetDragHandle(gesturesEnabled: Boolean) {
    if (gesturesEnabled) {
        BottomSheetDefaults.DragHandle()
    } else {
        BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))
    }
}

/**
 * Sits between the sheet's scrollable content and Material's sheet nested-scroll handling, so a
 * list in a sheet behaves like on stock Android:
 * - scroll leftovers pass through unchanged, so pulling down at the top of a list drags the sheet
 *   (and a long or fast enough pull dismisses it);
 * - leftover fling velocity reaches the sheet only when this gesture actually dragged the sheet.
 *   Material's sheet otherwise runs its settle animation on every fling the list hands up — an
 *   over-scroll at the end of a list then kept the stretch stuck until that animation finished,
 *   and a fling reaching the top of a list could move or even dismiss a sheet nobody dragged.
 */
private class SheetBodyScrollConnection : NestedScrollConnection {
    private var draggedSheet = false

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        // A downward drag left over by the content (it is at its top) moves the sheet.
        if (source == NestedScrollSource.UserInput && available.y > 0f) draggedSheet = true
        return Offset.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        val handOver = draggedSheet
        draggedSheet = false
        return if (handOver) Velocity.Zero else available
    }
}
