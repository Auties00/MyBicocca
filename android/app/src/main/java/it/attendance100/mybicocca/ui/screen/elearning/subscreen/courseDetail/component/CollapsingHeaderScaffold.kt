package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.CollapsingHeaderState
import kotlin.math.roundToInt

// Collapsing-header scaffold for the course page: hero + tab bar + pager laid out as one
// column that slides up by the header's offset. A custom Layout instead of measured-after-the-
// fact composition state (onSizeChanged + BoxWithConstraints) buys three guarantees:
//  - the pager is always sized for the pinned state (the viewport below the pinned bar) from
//    child sizes measured in the same pass, so the bar pins purely off header travel — a
//    short or empty tab can never unpin it, and the bottom edge can't lag a frame behind;
//  - the collapse range is derived from real geometry before placement ever reads the offset,
//    so a restored offset survives the first frame back instead of being clamped against
//    not-yet-measured children;
//  - the offset is read only inside the placement block, so scrolling re-runs placement
//    alone — no recomposition, no remeasure.
// Each slot must emit exactly one layout node; bar and pages come and go together.
@Composable
fun CollapsingHeaderScaffold(
    header: CollapsingHeaderState,
    topInset: Dp,
    spacing: Dp,
    // How much of the bar's own top padding slides under the global bar at full pin, instead
    // of the bar animating that padding away (which would remeasure the pager every frame of
    // the morph).
    pinnedBarOverlap: Dp,
    hero: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    bar: (@Composable () -> Unit)? = null,
    pages: (@Composable () -> Unit)? = null,
) {
    Layout(
        content = {
            hero()
            if (bar != null && pages != null) {
                bar()
                pages()
            }
        },
        // While the hero is expanded the pager overflows the bottom edge by the collapse
        // range; clip so the surplus can't bleed outside the page during nav transitions.
        modifier = modifier.clipToBounds(),
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val heightUnbounded = constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)
        val heroPlaceable = measurables[0].measure(heightUnbounded)
        val topInsetPx = topInset.roundToPx()
        val spacingPx = spacing.roundToPx()

        if (measurables.size == 1) {
            // Nothing below the hero to scroll: no collapsing, pull-to-refresh still works
            // through the (otherwise idle) root scrollable.
            header.updateCollapseRange(0f)
            layout(width, constraints.maxHeight) {
                heroPlaceable.place(0, topInsetPx)
            }
        } else {
            val barPlaceable = measurables[1].measure(heightUnbounded)
            val overlapPx = pinnedBarOverlap.roundToPx()
            // heroHeight + spacing brings the bar's top edge to the inset; the extra overlap
            // then tucks the bar's breathing room under the global bar. Publish the range
            // before measuring the pager so pages (visiblePageSlice) read fresh geometry.
            header.updateCollapseRange((heroPlaceable.height + spacingPx + overlapPx).toFloat())
            val pagerHeight =
                (constraints.maxHeight - topInsetPx - spacingPx - barPlaceable.height + overlapPx)
                    .coerceAtLeast(0)
            val pagerPlaceable = measurables[2].measure(Constraints.fixed(width, pagerHeight))

            layout(width, constraints.maxHeight) {
                val heroY = topInsetPx - header.offsetPx.roundToInt()
                heroPlaceable.place(0, heroY)
                val barY = heroY + heroPlaceable.height + spacingPx
                barPlaceable.place(0, barY)
                pagerPlaceable.place(0, barY + barPlaceable.height + spacingPx)
            }
        }
    }
}

// Sizes an empty/loading page to the slice of the pager that is actually on screen, so its
// centered content tracks the header: low while the hero is expanded, centered in the full
// page once the bar is pinned. Must sit on a direct page child of the scaffold's pager (the
// incoming max height is the pager height); header state is read in the layout phase only.
fun Modifier.visiblePageSlice(
    header: CollapsingHeaderState,
    minHeight: Dp = 240.dp,
): Modifier = layout { measurable, constraints ->
    val hiddenPx = (header.collapseRangePx - header.offsetPx).coerceAtLeast(0f).roundToInt()
    val height = (constraints.maxHeight - hiddenPx).coerceAtLeast(minHeight.roundToPx())
    val placeable = measurable.measure(constraints.copy(minHeight = height, maxHeight = height))
    layout(placeable.width, height) { placeable.place(0, 0) }
}
