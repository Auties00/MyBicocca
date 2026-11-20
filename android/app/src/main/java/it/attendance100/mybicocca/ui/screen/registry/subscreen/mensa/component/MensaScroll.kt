package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListState

/**
 * How long the course strip takes to carry the list or the carousel to a course: 50% longer than
 * the library's default scroll, so the move can be followed.
 */
internal const val MENSA_COURSE_SCROLL_MS = 750

internal fun <T> mensaScrollSpec(): FiniteAnimationSpec<T> = tween(MENSA_COURSE_SCROLL_MS, easing = FastOutSlowInEasing)

/**
 * Scrolls the list so [index] sits at the top, over [MENSA_COURSE_SCROLL_MS]. The library's
 * `animateScrollToItem` takes no animation spec, so the distance is worked out here: exact when the
 * item is on screen, estimated from the items that are when it is not, and settled by the library
 * scroll at the end for the last few pixels.
 */
internal suspend fun LazyListState.animateToItemSlow(index: Int) {
    val visible = layoutInfo.visibleItemsInfo
    if (visible.isEmpty()) {
        scrollToItem(index)
        return
    }
    val onScreen = visible.firstOrNull { it.index == index }
    val distance = if (onScreen != null) {
        onScreen.offset.toFloat()
    } else {
        val first = visible.first()
        val average = visible.sumOf { it.size }.toFloat() / visible.size + layoutInfo.mainAxisItemSpacing
        (index - first.index) * average + first.offset
    }
    animateScrollBy(distance, mensaScrollSpec())
    val settled = firstVisibleItemIndex == index && firstVisibleItemScrollOffset == 0
    if (!settled && canScrollForward) animateScrollToItem(index)
}
