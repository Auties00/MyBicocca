package it.attendance100.mybicocca.ui.screen.registry.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryTab
import it.attendance100.mybicocca.ui.screen.registry.state.label
import it.attendance100.mybicocca.ui.theme.BicoccaWordmarkAccent

private enum class TabBarSlot { Measure, Content }

// Tabs expand to fill the width when they fit, and fall back to a horizontally
// scrollable strip when they don't. A SubcomposeLayout measures each tab's
// one-line width in a throwaway first pass; if they fit, the leftover space is
// shared out equally so every tab grows (no clipping, no empty trailing gap).
// The selected tab's position is computed in that pass and handed to the content
// composable, which animates a single underline that slides between tabs.
@Composable
fun RegistryTabBar(
    selected: RegistryTab,
    onSelect: (RegistryTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = RegistryTab.entries
    val dividerColor = MaterialTheme.colorScheme.outlineVariant

    SubcomposeLayout(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val stroke = 1.dp.toPx()
                drawLine(
                    color = dividerColor,
                    start = Offset(0f, size.height - stroke / 2f),
                    end = Offset(size.width, size.height - stroke / 2f),
                    strokeWidth = stroke,
                )
            },
    ) { constraints ->
        // Minimum breathing room per tab in fill mode (11dp each side -> 22dp between labels).
        val perTabPadding = 22.dp.roundToPx()
        val edge = 20.dp.roundToPx()
        val gap = 22.dp.roundToPx()
        val selectedIndex = tabs.indexOf(selected)

        val intrinsic = subcompose(TabBarSlot.Measure) {
            tabs.forEach { tab ->
                RegistryTabItem(label = tab.label, selected = tab == selected, onClick = {})
            }
        }.map { it.measure(Constraints()).width }

        val required = intrinsic.sum() + perTabPadding * tabs.size
        val fits = constraints.hasBoundedWidth && required <= constraints.maxWidth
        val extraPerTab = if (fits) (constraints.maxWidth - required) / tabs.size else 0
        val tabWidths: List<Dp> = intrinsic.map { (it + perTabPadding + extraPerTab).toDp() }

        // Underline target for the selected tab, in the content's coordinate space.
        var leftPx = if (fits) 0 else edge
        for (j in 0 until selectedIndex) {
            leftPx += if (fits) intrinsic[j] + perTabPadding + extraPerTab else intrinsic[j] + gap
        }
        if (fits) leftPx += (perTabPadding + extraPerTab) / 2
        val indicatorLeft = leftPx.toDp()
        val indicatorWidth = intrinsic[selectedIndex].toDp()

        val content = subcompose(TabBarSlot.Content) {
            val scrollState = rememberScrollState()
            val animatedLeft by animateDpAsState(indicatorLeft, label = "tab-indicator-left")
            val animatedWidth by animateDpAsState(indicatorWidth, label = "tab-indicator-width")

            // Drawn here (not per tab) so a single line slides, and it lives in the
            // Row's content space so it scrolls together with the tabs.
            val indicator = Modifier.drawBehind {
                val w = animatedWidth.toPx()
                if (w > 0f) {
                    val stroke = 2.5.dp.toPx()
                    val l = animatedLeft.toPx()
                    val y = size.height - stroke / 2f
                    drawLine(
                        color = BicoccaWordmarkAccent,
                        start = Offset(l, y),
                        end = Offset(l + w, y),
                        strokeWidth = stroke,
                    )
                }
            }

            if (fits) {
                Row(modifier = Modifier.fillMaxWidth().then(indicator).padding(top = 6.dp)) {
                    tabs.forEachIndexed { index, tab ->
                        RegistryTabItem(
                            label = tab.label,
                            selected = tab == selected,
                            onClick = { onSelect(tab) },
                            modifier = Modifier.width(tabWidths[index]),
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .horizontalScroll(scrollState)
                        .then(indicator)
                        .padding(start = 20.dp, end = 20.dp, top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(22.dp),
                ) {
                    tabs.forEach { tab ->
                        RegistryTabItem(
                            label = tab.label,
                            selected = tab == selected,
                            onClick = { onSelect(tab) },
                        )
                    }
                }
            }
        }.first().measure(constraints)

        layout(content.width, content.height) {
            content.place(0, 0)
        }
    }
}

@Composable
private fun RegistryTabItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(top = 10.dp, bottom = 12.dp),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) scheme.onSurface else scheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false,
        )
    }
}
