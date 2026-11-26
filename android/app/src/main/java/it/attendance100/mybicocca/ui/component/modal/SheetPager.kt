package it.attendance100.mybicocca.ui.component.modal

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.ui.unit.IntOffset
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
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
 * ([backTo] null) it steps aside and the gesture slides the sheet down and dismisses it.
 *
 * @param page the page to show. Changing it animates to it; equal pages (by [key]) update in place.
 * @param backTo where a back step from [page] lands, or null when back is not handled by the pager
 * (first page, or a page that must not be left, e.g. mid-submission).
 * @param onBack commits a back step: the caller moves its state (pops the back stack, clears the
 * selection, …) so that [page] becomes [backTo]. Called after a completed gesture or an arrow tap.
 * @param slide how the body moves between pages; the header always cross-slides.
 * @param header resolves the pinned header (title and subtitle) of every page; null for a page
 * that shows no header (it takes no room, and the header grows in or out on the page change).
 * @param morphHeight whether the sheet's height animates between pages of different heights.
 * Off for a pager whose pages all fill the sheet: their height follows the header's, and
 * animating it chased that header frame by frame and stretched the page change to seconds.
 * @param holdMillis how long a change from one page to another holds still before moving, so the
 * leaving page can finish something of its own first (the Mensa bag taking its dishes back).
 * @param content a page's body; its [AnimatedContentScope] lets it animate parts of itself with
 * the page change.
 */
@Composable
fun <P : Any> SheetPager(
    page: P,
    depth: (P) -> Int,
    backTo: P?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    key: (P) -> Any = { it },
    slide: SheetPageSlide = SheetPageSlide.Horizontal,
    header: @Composable (P) -> SheetHeaderSpec?,
    morphHeight: Boolean = true,
    holdMillis: (from: P, to: P) -> Int = { _, _ -> 0 },
    content: @Composable AnimatedContentScope.(P) -> Unit,
) {
    SheetPagerImpl(page, depth, backTo, onBack, modifier, key, slide, header, morphHeight, holdMillis, content)
}

/** How a [SheetPager]'s body moves to a deeper page (and back). */
enum class SheetPageSlide {
    /** The next page pushes in from the end: navigating into an item. */
    Horizontal,

    /** The next page rises from the bottom: a flow layered over the page (e.g. a sign-in). */
    Vertical,
}

/**
 * A single-page modal body under the standard pinned header ([header]'s title and subtitle) — for
 * the few sheets that live in a screen's local state (pickers) rather than on the back stack, so
 * they look exactly like every navigation sheet.
 */
@Composable
fun SheetPage(
    header: SheetHeaderSpec,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    SheetPager(
        page = Unit,
        depth = { 0 },
        backTo = null,
        onBack = {},
        modifier = modifier,
        header = { header },
    ) { content() }
}

/** [SheetPager], also used by the modal scene for pages that draw their own header (a null spec). */
@Composable
internal fun <P : Any> SheetPagerImpl(
    page: P,
    depth: (P) -> Int,
    backTo: P?,
    onBack: () -> Unit,
    modifier: Modifier,
    key: (P) -> Any,
    slide: SheetPageSlide,
    header: @Composable (P) -> SheetHeaderSpec?,
    morphHeight: Boolean = true,
    holdMillis: (from: P, to: P) -> Int = { _, _ -> 0 },
    content: @Composable AnimatedContentScope.(P) -> Unit,
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
        val pageKey = key(page)
        when {
            key(state.currentState) == pageKey && key(state.targetState) == pageKey -> Unit
            // A predictive back already seeked toward this page: finish that same motion. Page
            // objects can be rebuilt between the seek and the commit (the sheet scene rebuilds them
            // from the live stack), and retargeting to an equal-but-new object would restart the
            // transition — dropping the outgoing page and snapping the height at the end.
            key(state.targetState) == pageKey -> state.animateTo(state.targetState)
            else -> state.animateTo(page)
        }
    }

    PredictiveBackHandler(enabled = backTo != null) { progress ->
        val target = backTo ?: return@PredictiveBackHandler
        try {
            progress.collect { event ->
                // A page that holds before changing lets the finger scrub only that hold: what the
                // leaving page plays first. The page change itself runs once the gesture commits,
                // as an animation, since scrubbing a shared element's flight restarts it each frame.
                val holdNanos = holdMillis(state.currentState, target) * 1_000_000L
                val totalNanos = transition.totalDurationNanos
                val reach = if (holdNanos > 0 && totalNanos > holdNanos) holdNanos.toFloat() / totalNanos else 1f
                state.seekTo(event.progress * reach, targetState = target)
            }
        } catch (cancelled: CancellationException) {
            rewindScope.launch { state.rewindTo(latestPage, transition.totalDurationNanos) }
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
        val specs = rememberHeaderSpecs(transition, key) { header(fresh(it)) }
        SheetPagerHeader(
            transition = transition,
            specs = specs,
            keyOf = key,
            depthOf = depth,
            // Over the page change itself: a page can make the transition longer with motion of its
            // own (held before the change, or played after it), which the header does not wait for.
            progress = {
                val totalMs = transition.totalDurationNanos / 1_000_000f
                val holdMs = holdMillis(transition.currentState, transition.targetState).toFloat()
                val changeMs = minOf(SheetMotion.PAGE_MS.toFloat(), totalMs - holdMs)
                if (changeMs <= 0f) state.fraction else ((state.fraction * totalMs - holdMs) / changeMs).coerceIn(0f, 1f)
            },
            onBack = onBack,
        )
        transition.AnimatedContent(
            transitionSpec = {
                sheetPageTransform(
                    slide = slide,
                    forward = depth(targetState) >= depth(initialState),
                    morphHeight = morphHeight,
                    hold = holdMillis(initialState, targetState),
                )
            },
            contentKey = key,
        ) { shown -> content(fresh(shown)) }
    }
}

/**
 * Forward/back page transition for in-sheet navigation: a soft push with a synchronized
 * sheet-height morph.
 */
private fun AnimatedContentTransitionScope<*>.sheetPageTransform(
    slide: SheetPageSlide,
    forward: Boolean,
    morphHeight: Boolean,
    hold: Int,
): ContentTransform {
    val sign = if (forward) 1 else -1
    val move = tween<IntOffset>(SheetMotion.PAGE_MS, delayMillis = hold)
    val enter = when (slide) {
        SheetPageSlide.Horizontal -> slideInHorizontally(move) { sign * it / 8 }
        SheetPageSlide.Vertical -> slideInVertically(move) { sign * it / 4 }
    }
    val exit = when (slide) {
        SheetPageSlide.Horizontal -> slideOutHorizontally(move) { -sign * it / 8 }
        SheetPageSlide.Vertical -> slideOutVertically(move) { -sign * it / 4 }
    }
    return (fadeIn(tween(SheetMotion.IN_FADE_MS, delayMillis = SheetMotion.IN_FADE_DELAY_MS + hold)) + enter)
        .togetherWith(fadeOut(tween(SheetMotion.OUT_FADE_MS, delayMillis = hold)) + exit)
        .using(SizeTransform(clip = true) { _, _ -> if (morphHeight) tween(SheetMotion.PAGE_MS, delayMillis = hold) else snap() })
}

/**
 * Plays a cancelled back gesture backwards, from where the finger let go to the page it started
 * on, then settles there. Animating to [page] instead would start a new page change from the
 * gesture's target, replaying the page's entrance.
 */
private suspend fun <P : Any> SeekableTransitionState<P>.rewindTo(page: P, totalDurationNanos: Long) {
    val target = targetState
    val from = fraction
    if (target != page && from > 0f) {
        val durationNanos = (totalDurationNanos * from).toLong().coerceIn(1L, MAX_REWIND_NANOS)
        val start = withFrameNanos { it }
        while (true) {
            val elapsed = withFrameNanos { it } - start
            val t = (elapsed.toFloat() / durationNanos).coerceAtMost(1f)
            seekTo(from * (1f - FastOutSlowInEasing.transform(t)), targetState = target)
            if (t >= 1f) break
        }
    }
    snapTo(page)
}

/** The longest a cancelled gesture takes to rewind, however far it went. */
private const val MAX_REWIND_NANOS = 450_000_000L

/**
 * [value], or — while it is null — the last non-null value it had. For a page that is sliding out
 * after the state it renders was cleared (a confirm page whose item was just acted on, a result
 * page whose outcome was consumed): it keeps showing what it showed until it is gone.
 */
@Composable
fun <T : Any> rememberLastNonNull(value: T?): T? {
    val last = remember { LastValue<T>() }
    SideEffect { if (value != null) last.value = value }
    return value ?: last.value
}

private class LastValue<T : Any> {
    var value: T? = null
}
