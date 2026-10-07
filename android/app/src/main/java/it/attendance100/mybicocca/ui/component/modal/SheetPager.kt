package it.attendance100.mybicocca.ui.component.modal

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * The one implementation of in-sheet navigation, shared by every multi-page modal: sheets whose
 * pages are back-stack entries (hosted by the modal scene) and sheets that derive their page from
 * their own state machine (wizards, outcome pages) alike.
 *
 * A single [SeekableTransitionState] drives everything that moves on a page change — the pinned
 * header (back arrow, inset, title and subtitle), the body's horizontal push and the sheet's
 * height (the body's [SizeTransform] re-measures the sheet every frame). That gives every sheet the
 * same behaviour:
 * - forward/back page changes animate header, body and height in one motion (issue #14 — some
 *   sheets used to snap their height because each one wired its own AnimatedContent);
 * - the system back gesture is predictive: it seeks that transition toward [backTo] while the
 *   finger moves, commits through [onBack] on release and rewinds on cancel, with the header in
 *   lockstep (issue #9);
 * - the header back arrow and the gesture share one code path.
 *
 * The back handler is registered inside the sheet's window, after Material's own sheet handler, so
 * on deeper pages it takes precedence over the sheet's predictive dismiss; on the first page
 * ([backTo] null) it steps aside and the gesture scales and dismisses the sheet natively.
 *
 * @param page the page to show. Changing it animates to it; equal pages (by [key]) update in place.
 * @param backTo where a back step from [page] lands, or null when back is not handled by the pager
 * (first page, or a page that must not be left, e.g. mid-submission).
 * @param onBack commits a back step: the caller moves its state (pops the back stack, clears the
 * selection, …) so that [page] becomes [backTo]. Called after a completed gesture or an arrow tap.
 * @param header resolves the pinned header of a page; null draws no pinned header (pages that
 * render their own).
 */
@Composable
fun <P : Any> SheetPager(
    page: P,
    depth: (P) -> Int,
    backTo: P?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    key: (P) -> Any = { it },
    header: (@Composable (P) -> SheetHeaderSpec?)? = null,
    content: @Composable (P) -> Unit,
) {
    val state = remember { SeekableTransitionState(page) }
    val transition = rememberTransition(state, label = "sheet_pager")
    val latestPage by rememberUpdatedState(page)
    val latestOnBack by rememberUpdatedState(onBack)
    // A cancelled gesture cancels the handler's own job, so the rewind must run elsewhere.
    val rewindScope = rememberCoroutineScope()

    // Programmatic navigation (a tap, a ViewModel result, a popped back-stack entry): animate to
    // the new page. A page that is already the settled state (e.g. right after a predictive back
    // commit) is a no-op. Keyed by page identity, not data: a page whose data refreshes keeps its
    // place and simply renders the fresh value (see [fresh]).
    LaunchedEffect(key(page)) {
        if (key(state.currentState) != key(page) || key(state.targetState) != key(page)) {
            state.animateTo(page)
        }
    }

    PredictiveBackHandler(enabled = backTo != null) { progress ->
        val target = backTo ?: return@PredictiveBackHandler
        try {
            progress.collect { event -> state.seekTo(event.progress, targetState = target) }
        } catch (cancelled: CancellationException) {
            rewindScope.launch { state.animateTo(latestPage) }
            throw cancelled
        }
        // Commit first, then let the page change finish the motion from where the finger left it
        // (the key-change effect above animates to [target]). Doing the commit after a suspending
        // finish would lose it when a second back press arrives mid-animation: a new gesture
        // cancels this handler's job before it gets to commit.
        latestOnBack()
        // If the owner vetoed the step, return to wherever it is now instead of leaving the pager
        // parked on a page it is not on.
        rewindScope.launch {
            withFrameNanos { }
            if (key(latestPage) != key(state.targetState)) state.animateTo(latestPage)
        }
    }

    // The transition holds the page objects it was animated with; while a page stays on screen its
    // data may refresh under the same key, so render the latest value for it.
    fun fresh(shown: P): P = if (key(shown) == key(latestPage)) latestPage else shown

    Column(modifier) {
        if (header != null) {
            val specs = rememberHeaderSpecs(transition, key) { header(fresh(it)) }
            SheetPagerHeader(
                transition = transition,
                specs = specs,
                keyOf = key,
                depthOf = depth,
                onBack = onBack,
            )
        }
        transition.AnimatedContent(
            transitionSpec = { sheetPageTransform(forward = depth(targetState) >= depth(initialState)) },
            contentKey = key,
        ) { shown -> content(fresh(shown)) }
    }
}

/**
 * Forward/back page transition for in-sheet navigation: a soft horizontal push with a
 * synchronized sheet-height morph. Scoped to [AnimatedContentTransitionScope] because `using` is a
 * member of the transitionSpec scope.
 */
fun AnimatedContentTransitionScope<*>.sheetPageTransform(forward: Boolean): ContentTransform =
    (fadeIn(tween(280, delayMillis = 40)) + slideInHorizontally(tween(SheetMotion.PAGE_MS)) { if (forward) it / 8 else -it / 8 })
        .togetherWith(fadeOut(tween(180)) + slideOutHorizontally(tween(SheetMotion.PAGE_MS)) { if (forward) -it / 8 else it / 8 })
        .using(SizeTransform(clip = true) { _, _ -> tween(SheetMotion.PAGE_MS) })
