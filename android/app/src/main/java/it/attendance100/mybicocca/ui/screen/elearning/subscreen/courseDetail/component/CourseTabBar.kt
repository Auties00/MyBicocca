package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.CourseTab
import kotlin.math.roundToInt

// Tab strip that morphs with `pinProgress`: at 0 it is a row of full-width filled buttons
// (each cell weighted by its label so nothing truncates on narrow screens); at 1 the active
// button's pill has melted into an M3-expressive indicator under the label. The selection
// layer is a single rounded rect drawn behind the cells, positioned from the pager's
// continuous page+fraction so it tracks swipes; reading the pager inside drawBehind keeps
// per-frame swipe updates in the draw phase instead of recomposing the row.
@Composable
fun CourseTabBar(
    pagerState: PagerState,
    onSelect: (CourseTab) -> Unit,
    modifier: Modifier = Modifier,
    pinProgress: () -> Float = { 0f },
) {
    val scheme = MaterialTheme.colorScheme
    val tabs = CourseTab.entries
    val p = pinProgress().coerceIn(0f, 1f)

    // Labels flip selection as the pill crosses the midpoint of a swipe, not on settle.
    val activeIndex by remember(pagerState) {
        derivedStateOf {
            (pagerState.currentPage + pagerState.currentPageOffsetFraction)
                .roundToInt()
                .coerceIn(0, tabs.size - 1)
        }
    }

    val selectionColor = scheme.primary
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(scheme.surfaceContainer)
            .padding(horizontal = 16.dp)
            .padding(top = lerp(14.dp, 0.dp, p), bottom = 8.dp)
            .drawBehind {
                val spacing = CellSpacing.toPx()
                val weightSum = tabs.sumOf { tabWeight(it).toDouble() }.toFloat()
                val available = size.width - spacing * (tabs.size - 1)
                val widths = FloatArray(tabs.size)
                val lefts = FloatArray(tabs.size)
                var x = 0f
                tabs.forEachIndexed { i, tab ->
                    widths[i] = available * tabWeight(tab) / weightSum
                    lefts[i] = x
                    x += widths[i] + spacing
                }

                val position = (pagerState.currentPage + pagerState.currentPageOffsetFraction)
                    .coerceIn(0f, (tabs.size - 1).toFloat())
                val from = position.toInt().coerceAtMost(tabs.size - 2)
                val fraction = position - from
                val cellLeft = lerp(lefts[from], lefts[from + 1], fraction)
                val cellWidth = lerp(widths[from], widths[from + 1], fraction)

                val indicatorWidth = cellWidth * IndicatorWidthFraction
                val indicatorHeight = IndicatorHeight.toPx()
                val left = lerp(cellLeft, cellLeft + (cellWidth - indicatorWidth) / 2f, p)
                val top = lerp(0f, size.height - indicatorHeight, p)
                val width = lerp(cellWidth, indicatorWidth, p)
                val height = lerp(size.height, indicatorHeight, p)
                drawRoundRect(
                    color = selectionColor,
                    topLeft = Offset(left, top),
                    size = Size(width, height),
                    cornerRadius = CornerRadius(lerp(16.dp.toPx(), height / 2f, p)),
                )
            },
        horizontalArrangement = Arrangement.spacedBy(CellSpacing),
    ) {
        tabs.forEach { tab ->
            val isActive = tab.ordinal == activeIndex
            // The active cell stays transparent so the moving selection layer shows through.
            val bg by animateColorAsState(
                if (isActive) Color.Transparent else scheme.surfaceContainerHigh.copy(alpha = 1f - p),
                label = "tab-bg",
            )
            val fg by animateColorAsState(
                if (isActive) lerp(scheme.onPrimary, scheme.primary, p) else scheme.onSurfaceVariant,
                label = "tab-fg",
            )
            Box(
                modifier = Modifier
                    .weight(tabWeight(tab))
                    .clip(RoundedCornerShape(999.dp))
                    .background(bg)
                    .clickable { onSelect(tab) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tabLabel(tab),
                    color = fg,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

private fun tabLabel(tab: CourseTab): String = when (tab) {
    CourseTab.Syllabus -> "Scheda"
    CourseTab.Content -> "Contenuti"
    CourseTab.Quiz -> "Quiz"
    CourseTab.Assignments -> "Compiti"
    CourseTab.Forum -> "Forum"
}

// Width share proportional to the label (plus constant slack) so the row fills the screen
// while long labels like "Contenuti" still fit on compact widths.
private fun tabWeight(tab: CourseTab): Float = tabLabel(tab).length + 4f

private val CellSpacing = 6.dp
private val IndicatorHeight = 4.dp
private const val IndicatorWidthFraction = 0.6f
