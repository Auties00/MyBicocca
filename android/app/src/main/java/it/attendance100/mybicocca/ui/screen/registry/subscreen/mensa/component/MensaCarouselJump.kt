package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.math.abs

/**
 * Jumps across the dish carousel without flashing the dishes in between, and without swapping the
 * plate on screen. A jump longer than two dishes plays over the two pages next to the current one:
 * while the carousel slides, they show the dish just before the target and then the target itself.
 * Once it lands, the pager moves to the target's real page in the same frame and the pages show
 * their own dishes again.
 */
@Stable
internal class MensaCarouselJumper {
    private class Jump(val from: Int, val to: Int) {
        val step = if (to > from) 1 else -1
    }

    private var jump by mutableStateOf<Jump?>(null)

    /** The index of the dish shown on [page]: the page's own, unless a jump is borrowing it. */
    fun dishIndex(page: Int): Int = jump?.let { dishIndexDuring(it, page) } ?: page

    /** Slides to dish [target], through at most one other dish. */
    suspend fun jumpTo(pagerState: PagerState, target: Int, animationSpec: AnimationSpec<Float>) {
        val from = pagerState.currentPage
        if (abs(target - from) <= 2) {
            pagerState.animateScrollToPage(target, animationSpec = animationSpec)
            return
        }
        val next = Jump(from, target)
        jump = next
        try {
            pagerState.animateScrollToPage(from + 2 * next.step, animationSpec = animationSpec)
        } finally {
            // Also when a swipe cuts the jump short: wherever it stopped, stay on that dish.
            val landedOn = dishIndexDuring(next, pagerState.currentPage)
            val fraction = pagerState.currentPageOffsetFraction
            jump = null
            pagerState.requestScrollToPage(landedOn, fraction)
        }
    }

    private fun dishIndexDuring(jump: Jump, page: Int): Int = when (page) {
        jump.from + jump.step -> jump.to - jump.step
        jump.from + 2 * jump.step -> jump.to
        else -> page
    }
}

@Composable
internal fun rememberMensaCarouselJumper(): MensaCarouselJumper = remember { MensaCarouselJumper() }
