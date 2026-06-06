package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.CourseTab
import kotlin.math.abs

// The bar's resting top padding. It stays constant in layout; at full pin the scaffold slides
// exactly this much under the global bar instead of the bar animating its padding away, which
// would remeasure the bar — and through it the pager — on every frame of the morph.
val CourseTabBarBreathingRoom = 14.dp

// Tab strip that morphs with `pinProgress`: at 0 it is a row of full-width filled pills (each
// cell weighted by its label so nothing truncates on narrow screens); at 1 the selection has
// melted into an M3-expressive indicator under the active label. Every selection visual — the
// resting pills, the moving selection, the label tints — derives from the pager's continuous
// position and the pin progress, and is evaluated in the draw phase: swipes and the collapse
// morph redraw the bar without recomposing or remeasuring it. The selection is drawn after
// the resting pills, so it glides over them instead of vanishing underneath a neighbour cell
// mid-swipe, and the labels tint by how much the selection covers them, so colors track the
// finger instead of flipping on a timer at the midpoint.
@Composable
fun CourseTabBar(
    pagerState: PagerState,
    onSelect: (CourseTab) -> Unit,
    modifier: Modifier = Modifier,
    pinProgress: () -> Float = { 0f },
) {
    val scheme = MaterialTheme.colorScheme
    val tabs = CourseTab.entries

    val restingColor = scheme.surfaceContainerHigh
    val selectionColor = scheme.primary
    val inactiveLabel = scheme.onSurfaceVariant
    val activeLabelOnPill = scheme.onPrimary
    val activeLabelPinned = scheme.primary

    val typography = MaterialTheme.typography
    val labelStyle = remember(typography) {
        typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(scheme.surfaceContainer)
            .padding(horizontal = 16.dp)
            .padding(top = CourseTabBarBreathingRoom, bottom = 8.dp)
            .drawWithCache {
                // Cell geometry only depends on the bar's size — resolve it once per measure,
                // not on every swipe frame.
                val spacingPx = CellSpacing.toPx()
                val weightSum = tabs.sumOf { tabWeight(it).toDouble() }.toFloat()
                val available = size.width - spacingPx * (tabs.size - 1)
                val widths = FloatArray(tabs.size)
                val lefts = FloatArray(tabs.size)
                var x = 0f
                for (i in tabs.indices) {
                    widths[i] = available * tabWeight(tabs[i]) / weightSum
                    lefts[i] = x
                    x += widths[i] + spacingPx
                }
                val indicatorHeightPx = IndicatorHeight.toPx()
                onDrawBehind {
                    val p = pinProgress().coerceIn(0f, 1f)
                    if (p < 1f) {
                        val restAlpha = 1f - p
                        for (i in tabs.indices) {
                            drawRoundRect(
                                color = restingColor,
                                topLeft = Offset(lefts[i], 0f),
                                size = Size(widths[i], size.height),
                                cornerRadius = CornerRadius(size.height / 2f),
                                alpha = restAlpha,
                            )
                        }
                    }

                    val position = (pagerState.currentPage + pagerState.currentPageOffsetFraction)
                        .coerceIn(0f, (tabs.size - 1).toFloat())
                    val from = position.toInt().coerceAtMost(tabs.size - 2)
                    val fraction = position - from
                    val cellLeft = lerp(lefts[from], lefts[from + 1], fraction)
                    val cellWidth = lerp(widths[from], widths[from + 1], fraction)

                    val indicatorWidth = cellWidth * IndicatorWidthFraction
                    val left = lerp(cellLeft, cellLeft + (cellWidth - indicatorWidth) / 2f, p)
                    val top = lerp(0f, size.height - indicatorHeightPx, p)
                    val width = lerp(cellWidth, indicatorWidth, p)
                    val height = lerp(size.height, indicatorHeightPx, p)
                    drawRoundRect(
                        color = selectionColor,
                        topLeft = Offset(left, top),
                        size = Size(width, height),
                        cornerRadius = CornerRadius(lerp(size.height / 2f, height / 2f, p)),
                    )
                }
            },
        horizontalArrangement = Arrangement.spacedBy(CellSpacing),
    ) {
        tabs.forEachIndexed { index, tab ->
            // settledPage only changes once per landed page, so the selection semantics don't
            // recompose anything mid-swipe.
            val selected = pagerState.settledPage == index
            val labelColor = remember(
                pagerState, pinProgress, inactiveLabel, activeLabelOnPill, activeLabelPinned,
            ) {
                ColorProducer {
                    val p = pinProgress().coerceIn(0f, 1f)
                    val position = pagerState.currentPage + pagerState.currentPageOffsetFraction
                    val coverage = (1f - abs(position - index)).coerceIn(0f, 1f)
                    lerp(inactiveLabel, lerp(activeLabelOnPill, activeLabelPinned, p), coverage)
                }
            }
            Box(
                modifier = Modifier
                    .weight(tabWeight(tab))
                    .clip(CellShape)
                    .selectable(selected = selected, role = Role.Tab) { onSelect(tab) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = tabLabel(tab),
                    style = labelStyle,
                    color = labelColor,
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

private val CellShape = RoundedCornerShape(999.dp)
private val CellSpacing = 6.dp
private val IndicatorHeight = 4.dp
private const val IndicatorWidthFraction = 0.6f
