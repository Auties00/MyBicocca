package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue

// Scroll model for the collapsing hero: the hero + tab bar + pager column is translated up by
// `collapsedPx`, from 0 (hero fully visible) to `collapseRangePx` (tab bar pinned under the
// global top bar). Deltas are fed in from the nested-scroll chain, so the tab pages' own lists
// only scroll once the hero is out of the way.
@Stable
class CollapsingHeaderState(private val morphDistancePx: Float) {

    var collapseRangePx by mutableFloatStateOf(0f)
        private set

    var collapsedPx by mutableFloatStateOf(0f)
        private set

    // 0 while the bar is still a bubble row, 1 once it is pinned; spans the last
    // `morphDistancePx` of header travel so the bubble melts into the tab indicator.
    val pinProgress: Float
        get() {
            if (collapseRangePx <= 0f || morphDistancePx <= 0f) return 0f
            return ((collapsedPx - (collapseRangePx - morphDistancePx)) / morphDistancePx)
                .coerceIn(0f, 1f)
        }

    fun updateCollapseRange(rangePx: Float) {
        collapseRangePx = rangePx.coerceAtLeast(0f)
        collapsedPx = collapsedPx.coerceIn(0f, collapseRangePx)
    }

    // deltaY is in gesture space (negative = finger moving up = collapsing); returns the
    // consumed part in the same space so the nested-scroll chain stays balanced.
    fun drag(deltaY: Float): Float {
        val target = (collapsedPx - deltaY).coerceIn(0f, collapseRangePx)
        val consumed = collapsedPx - target
        collapsedPx = target
        return consumed
    }

    companion object {
        // collapseRangePx is re-measured on restore; only the offset itself is worth saving.
        fun saver(morphDistancePx: Float) = listSaver<CollapsingHeaderState, Float>(
            save = { listOf(it.collapsedPx) },
            restore = { saved ->
                CollapsingHeaderState(morphDistancePx).apply { collapsedPx = saved[0] }
            },
        )
    }
}
