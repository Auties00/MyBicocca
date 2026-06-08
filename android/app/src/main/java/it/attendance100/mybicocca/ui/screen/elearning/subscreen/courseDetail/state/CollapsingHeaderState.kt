package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue

// Scroll model for the collapsing hero: the hero + tab bar + pager column is translated up by
// `offsetPx`, from 0 (hero fully visible) to `collapseRangePx` (tab bar pinned under the
// global top bar). Deltas are fed in from the nested-scroll chain, so the tab pages' own lists
// only scroll once the hero is out of the way. The range is set by the scaffold's measure pass
// from real child sizes.
@Stable
class CollapsingHeaderState(private val morphDistancePx: Float) {

    var collapseRangePx by mutableFloatStateOf(0f)
        private set

    // Raw desired offset. It may exceed the range while geometry is still settling (a restored
    // offset before the first measure pass, content that shrank); reads go through `offsetPx`,
    // so stale geometry can never destroy the persisted value — it snaps back into place as
    // soon as the range is real again.
    var collapsedPx by mutableFloatStateOf(0f)

    val offsetPx: Float
        get() = collapsedPx.coerceIn(0f, collapseRangePx)

    // 0 while the bar is still a bubble row, 1 once it is pinned; spans the last
    // `morphDistancePx` of header travel so the bubble melts into the tab indicator.
    val pinProgress: Float
        get() {
            if (collapseRangePx <= 0f || morphDistancePx <= 0f) return 0f
            return ((offsetPx - (collapseRangePx - morphDistancePx)) / morphDistancePx)
                .coerceIn(0f, 1f)
        }

    suspend fun collapse(durationMs: Int = 500) {
        val from = offsetPx
        val to = collapseRangePx
        if (from >= to) return
        Animatable(from).animateTo(to, animationSpec = tween(durationMs)) {
            collapsedPx = value
        }
    }

    // Snaps to fully open or fully collapsed. velocityY is in px/s (positive = downward);
    // near-zero velocity snaps to whichever end is closer.
    suspend fun snap(velocityY: Float = 0f, durationMs: Int = 250) {
        val target = when {
            velocityY > 100f -> 0f
            velocityY < -100f -> collapseRangePx
            else -> if (offsetPx < collapseRangePx / 2f) 0f else collapseRangePx
        }
        val from = collapsedPx
        if (from == target) return
        Animatable(from).animateTo(target, animationSpec = tween(durationMs)) {
            collapsedPx = value
        }
    }

    fun updateCollapseRange(rangePx: Float) {
        collapseRangePx = rangePx.coerceAtLeast(0f)
    }

    // deltaY is in gesture space (negative = finger moving up = collapsing); returns the
    // consumed part in the same space so the nested-scroll chain stays balanced. The first
    // user drag collapses any stale raw value down to the effective offset.
    fun drag(deltaY: Float): Float {
        val current = offsetPx
        val target = (current - deltaY).coerceIn(0f, collapseRangePx)
        collapsedPx = target
        return current - target
    }

    companion object {
        // The range is saved alongside the offset so `offsetPx` is already meaningful during
        // the first composition back (title/cover logic reads it before the first measure
        // pass re-derives the real range from live geometry).
        fun saver(morphDistancePx: Float) = listSaver<CollapsingHeaderState, Float>(
            save = { listOf(it.collapsedPx, it.collapseRangePx) },
            restore = { saved ->
                CollapsingHeaderState(morphDistancePx).apply {
                    collapsedPx = saved[0]
                    updateCollapseRange(saved.getOrElse(1) { 0f })
                }
            },
        )
    }
}
