package it.attendance100.mybicocca.ui.component.modal

import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Velocity
import it.attendance100.mybicocca.ui.component.feedback.SnackbarScope
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt

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

    /** How far a back gesture on the sheet's first page has slid it down, 0 to 1. */
    internal val backProgress = Animatable(0f)

    /** True once a back gesture committed: the sheet is sliding out and further back is ignored. */
    internal var dismissingByBack: Boolean = false

    /** The content's height before the back gesture started; see [backSlide]. */
    internal var restingHeight: Int = -1

    /** Animates the sheet out. Returns immediately if it is not mounted or already hidden. */
    suspend fun hide() {
        val sheet = state ?: return
        if (!sheet.isVisible) return
        hiddenByController = true
        closing = true
        try {
            sheet.hide()
        } finally {
            closing = false
        }
    }

    /**
     * Animates the sheet back in after a programmatic [hide] (the sheet came back on the stack while
     * it was hiding). The first show is left to Material's own entry animation.
     */
    suspend fun show() {
        val sheet = state ?: return
        if (!hiddenByController) return
        hiddenByController = false
        dismissingByBack = false
        backProgress.snapTo(0f)
        sheet.show()
    }
}

/**
 * The app's one modal bottom sheet container: a Material3 [ModalBottomSheet] (its own window,
 * scrim, drag handle, accessibility actions, IME and edge-to-edge insets) with the app's
 * conventions on top. Every modal — navigation sheets and the few local ones — goes through it so
 * they all behave the same.
 *
 * Back is predictive: on the sheet's first page the system back gesture slides the sheet down
 * with the finger (its content collapses from the bottom and fades), springs it back on cancel and
 * slides it out on commit. This replaces Material's own predictive scale, which shrinks the sheet
 * in place. Deeper pages are handled by the [SheetPager] inside, whose handler is registered later
 * and therefore wins while it is enabled.
 *
 * Dismissal can be vetoed via [confirmDismiss]. The veto runs before the hide settles: a refused
 * swipe or scrim dismissal springs the sheet straight back (and the content can morph into a
 * confirm page) rather than fully hiding and visibly re-opening. While [gesturesEnabled] is false
 * back is routed through the veto as well, without the slide. A programmatic
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
        // Takes the gesture from Material's handler (registered earlier), and is itself overridden
        // by in-sheet pagers (registered later).
        PredictiveBackHandler(enabled = gesturesEnabled) { events ->
            val progress = sheetController.backProgress
            if (sheetController.dismissingByBack) {
                events.collect { }
                return@PredictiveBackHandler
            }
            try {
                events.collect { event -> progress.snapTo(event.progress) }
            } catch (cancelled: CancellationException) {
                // A cancelled gesture cancels this handler's own job, so the rewind runs elsewhere.
                scope.launch { progress.animateTo(0f) }
                throw cancelled
            }
            if (!latestConfirmDismiss()) {
                scope.launch { progress.animateTo(0f) }
                return@PredictiveBackHandler
            }
            // Finish the slide outside this job, so a second back press cannot cut the dismissal.
            sheetController.dismissingByBack = true
            scope.launch {
                sheetController.hide()
                latestOnDismiss()
            }
        }
        val bodyScroll = remember { SheetBodyScrollConnection() }
        Box(
            Modifier
                .nestedScroll(bodyScroll)
                .backSlide(sheetController) { sheetController.backProgress.value },
        ) {
            SnackbarScope { content() }
        }
    }
}

/**
 * Collapses the sheet's content from the bottom and fades it as [progress] goes from 0 to 1. The
 * sheet wraps its content, so its top edge follows: the sheet reads as sliding down.
 *
 * During the gesture the content keeps the height it had at rest ([ModalSheetController.restingHeight]).
 * The sheet gives a shorter sheet less room, and content that fills the sheet (About) would shrink
 * with it and feed back into the next frame, the sheet jumping between two heights.
 */
private fun Modifier.backSlide(controller: ModalSheetController, progress: () -> Float): Modifier =
    clipToBounds().layout { measurable, constraints ->
        val eased = FastOutSlowInEasing.transform(progress().coerceIn(0f, 1f))
        val resting = controller.restingHeight
        val placeable = if (eased > 0f && resting > 0) {
            measurable.measure(constraints.copy(minHeight = resting, maxHeight = resting))
        } else {
            measurable.measure(constraints).also { controller.restingHeight = it.height }
        }
        val height = (placeable.height * (1f - eased)).roundToInt()
            .coerceIn(constraints.minHeight, constraints.maxHeight)
        layout(placeable.width, height) {
            placeable.placeWithLayer(0, 0) { alpha = 1f - eased }
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
