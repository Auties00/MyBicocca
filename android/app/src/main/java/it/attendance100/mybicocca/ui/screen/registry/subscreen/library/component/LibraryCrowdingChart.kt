package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import android.content.res.Configuration
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.currentLocale
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.domain.model.library.LibraryCrowdingBucket
import it.attendance100.mybicocca.domain.model.library.LibraryDayCrowdingState
import it.attendance100.mybicocca.domain.model.library.LibraryWeekdayCrowdingPage
import it.attendance100.mybicocca.domain.model.library.occupancyLevelOf
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreview
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreviewDefaults
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryTestTags
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.occupancyLevelLabel
import it.attendance100.mybicocca.ui.theme.PreviewBgDark
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import kotlin.math.roundToInt

private val TimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val ChartCardShape = RoundedCornerShape(24.dp)
private val DayChipShape = RoundedCornerShape(16.dp)
private val PlotHeight = 106.dp

/**
 * Weekly crowding chart card with a weekday selector dropdown, a Monday-to-Sunday [HorizontalPager]
 * opening on today, uniform 30-minute rounded pill bars, interactive slot inspection via tap, live
 * highlight + dotted leader on today while open, cached-date caption, and page indicator dots.
 */
@Composable
internal fun LibraryCrowdingChart(
    pages: List<LibraryWeekdayCrowdingPage>,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(AppClock.clock),
    nowTime: LocalTime = LocalTime.now(AppClock.clock),
    initialPageOverride: Int? = null,
    initialSelectedIndexOverride: Int? = null,
) {
    if (pages.isEmpty()) return

    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    val scope = rememberCoroutineScope()
    val locale = currentLocale()

    val initialPage = remember(pages.size, initialPageOverride) {
        initialPageOverride?.coerceIn(0, pages.lastIndex)
            ?: pages.indexOfFirst { it.isToday }.takeIf { it >= 0 }
            ?: 0
    }
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { pages.size },
    )
    var menuExpanded by remember { mutableStateOf(false) }
    var selection by remember(initialPage, initialSelectedIndexOverride) {
        mutableStateOf(initialSelectedIndexOverride?.let { initialPage to it })
    }

    LaunchedEffect(pagerState.currentPage) {
        if (selection != null && selection?.first != pagerState.currentPage) {
            selection = null
        }
    }

    val currentPage = pages[pagerState.currentPage.coerceIn(0, pages.lastIndex)]
    val captionDate = cachedForecastDate(currentPage, today)?.let { date ->
        date.format(DateTimeFormatter.ofPattern("EEE d MMM", locale)).replace(".", "")
    }
    val selectDayDescription = stringResource(R.string.library_crowding_select_day)

    Surface(
        shape = ChartCardShape,
        color = scheme.surfaceContainerHigh,
        modifier = modifier
            .fillMaxWidth()
            .testTag(LibraryTestTags.DETAIL_CROWDING_CHART),
    ) {
        // The 40 dp day chip sits inside a 48 dp minimum touch target (4 dp invisible vertical margin),
        // so 4 dp top padding yields an 8 dp visual inset matching the end inset and the 16 dp chip radius.
        Column(modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Groups,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    // Holds room for both lines, so large fonts don't resize the card between days.
                    Column(modifier = Modifier.clearAndSetSemantics {}) {
                        Text(" ", style = MaterialTheme.typography.titleMedium, maxLines = 1)
                        Text(" ", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.library_crowding_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (captionDate != null) {
                            Text(
                                text = stringResource(R.string.library_crowding_forecast_of, captionDate),
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                Box {
                    Surface(
                        onClick = { haptic.tap(); menuExpanded = true },
                        shape = DayChipShape,
                        color = scheme.surfaceContainerHighest,
                        contentColor = scheme.onSurface,
                        modifier = Modifier
                            .heightIn(min = 40.dp)
                            .semantics { contentDescription = selectDayDescription }
                            .testTag(LibraryTestTags.DETAIL_CROWDING_DAY_DROPDOWN),
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = currentPage.weekday.localizedFullName(locale),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Icon(
                                imageVector = Icons.Rounded.ArrowDropDown,
                                contentDescription = null,
                                tint = scheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        pages.forEachIndexed { index, page ->
                            val selected = index == pagerState.currentPage
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = page.weekday.localizedFullName(locale),
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (selected) scheme.primary else scheme.onSurface,
                                    )
                                },
                                onClick = {
                                    haptic.tap()
                                    menuExpanded = false
                                    scope.launch { pagerState.animateScrollToPage(index) }
                                },
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
            ) { pageIndex ->
                val page = pages[pageIndex]
                val selectedIndex = selection?.takeIf { it.first == pageIndex }?.second
                CrowdingDayPage(
                    page = page,
                    nowTime = nowTime,
                    selectedIndex = selectedIndex,
                    onSelectSlot = { nextIndex ->
                        selection = nextIndex?.let { pageIndex to it }
                    },
                )
            }

            Spacer(Modifier.height(12.dp))

            CrowdingPageDots(
                pageCount = pages.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

/**
 * Reserves the exact vertical space of a fully populated day plot (two-line callout, plot canvas and
 * time-axis row) so every weekday page and selection state keeps an identical card height.
 */
@Composable
private fun CrowdingPlotHeightReservation(modifier: Modifier = Modifier) {
    Column(modifier = modifier.clearAndSetSemantics {}) {
        Text(" ", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(" ", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, maxLines = 1)
        Spacer(Modifier.height(4.dp + PlotHeight + 4.dp))
        Text(" ", style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

@Composable
private fun CrowdingDayPage(
    page: LibraryWeekdayCrowdingPage,
    nowTime: LocalTime,
    selectedIndex: Int?,
    onSelectSlot: (Int?) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val locale = currentLocale()
    val weekdayName = page.weekday.localizedFullName(locale)

    val a11ySummary = when (val state = page.state) {
        LibraryDayCrowdingState.Closed ->
            stringResource(R.string.library_crowding_cd_closed, weekdayName)
        LibraryDayCrowdingState.Unavailable ->
            stringResource(R.string.library_crowding_cd_unavailable, weekdayName)
        is LibraryDayCrowdingState.Data -> {
            val base = if (state.peakBucket != null) {
                stringResource(
                    R.string.library_crowding_cd_peak,
                    weekdayName,
                    state.peakBucket.start.format(TimeFormat),
                )
            } else {
                stringResource(R.string.library_crowding_cd_day, weekdayName)
            }
            if (page.isToday && state.liveBucketIndex != null && state.liveOccupancy != null) {
                val liveLabel = stringResource(R.string.library_crowding_live, nowTime.format(TimeFormat))
                val tier = occupancyLevelLabel(occupancyLevelOf(state.liveOccupancy))
                "$base. $liveLabel: $tier"
            } else {
                base
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        CrowdingPlotHeightReservation()

        when (val state = page.state) {
            LibraryDayCrowdingState.Closed -> Text(
                text = stringResource(R.string.library_closed),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.clearAndSetSemantics { contentDescription = a11ySummary },
            )

            LibraryDayCrowdingState.Unavailable -> Text(
                text = stringResource(R.string.library_crowding_unavailable),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.clearAndSetSemantics { contentDescription = a11ySummary },
            )

            is LibraryDayCrowdingState.Data -> CrowdingBarsPlot(
                weekday = page.weekday,
                data = state,
                isToday = page.isToday,
                nowTime = nowTime,
                selectedIndex = selectedIndex,
                onSelectSlot = onSelectSlot,
                a11ySummary = a11ySummary,
            )
        }
    }
}

@Composable
private fun CrowdingBarsPlot(
    weekday: DayOfWeek,
    data: LibraryDayCrowdingState.Data,
    isToday: Boolean,
    nowTime: LocalTime,
    selectedIndex: Int?,
    onSelectSlot: (Int?) -> Unit,
    a11ySummary: String,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    val buckets = data.buckets
    if (buckets.isEmpty()) return

    val showLive = isToday && data.liveBucketIndex != null && data.liveOccupancy != null
    val liveIndex = if (showLive) data.liveBucketIndex else null
    val liveOccupancy = if (showLive) data.liveOccupancy else null
    val validSelectedIndex = selectedIndex?.takeIf { idx ->
        idx in buckets.indices && idx != liveIndex && buckets[idx].occupancyPercentage != null
    }
    val activeIndex = validSelectedIndex ?: liveIndex
    val isShowingLive = validSelectedIndex == null && liveIndex != null && liveOccupancy != null

    val tickIndices = remember(buckets) { selectWholeHourTickIndices(buckets) }
    val liveTitle = if (showLive) stringResource(R.string.library_crowding_live, nowTime.format(TimeFormat)) else null

    val normalBarColor = scheme.secondary.copy(alpha = 0.42f)
    val selectedBarColor = scheme.onSurface
    val emptyBarColor = scheme.onSurfaceVariant.copy(alpha = 0.16f)
    val liveBarColor = scheme.primary
    val dottedLineColor = scheme.onSurfaceVariant.copy(alpha = 0.65f)
    val tickColor = scheme.onSurfaceVariant.copy(alpha = 0.6f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
        ) {
            Column(modifier = Modifier.clearAndSetSemantics {}) {
                Text(" ", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(" ", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, maxLines = 1)
            }
            if (activeIndex != null) {
                val activeBucket = buckets[activeIndex]
                val activeOccupancy = if (isShowingLive) liveOccupancy!! else activeBucket.occupancyPercentage!!
                val anchorFraction = (activeIndex + 0.5f) / buckets.size
                val tierLabel = occupancyLevelLabel(occupancyLevelOf(activeOccupancy))
                val firstLine = if (isShowingLive && liveTitle != null) {
                    liveTitle
                } else {
                    formatCrowdingSlotRange(activeBucket)
                }
                AnchoredCallout(
                    anchorFraction = anchorFraction,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.testTag(LibraryTestTags.crowdingCallout(weekday)),
                    ) {
                        Text(
                            text = firstLine,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isShowingLive) scheme.primary else scheme.onSurface,
                            maxLines = 1,
                            softWrap = false,
                        )
                        Text(
                            text = tierLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (isShowingLive) scheme.onSurface else scheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(PlotHeight),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { contentDescription = a11ySummary },
            ) {
                val bucketCount = buckets.size
                val tickZonePx = 8.dp.toPx()
                val topGapPx = 4.dp.toPx()
                val barBottom = size.height - tickZonePx
                val barAreaHeight = (barBottom - topGapPx).coerceAtLeast(1f)

                val slotStep = size.width / bucketCount
                val barGap = (slotStep * 0.28f).coerceIn(1.5.dp.toPx(), 6.dp.toPx())
                val barWidth = (slotStep - barGap).coerceIn(3.dp.toPx(), 16.dp.toPx())
                val corner = CornerRadius(barWidth / 2f, barWidth / 2f)
                val emptyHeight = 3.dp.toPx()
                val minDataHeight = maxOf(barWidth, 6.dp.toPx())

                buckets.forEachIndexed { index, bucket ->
                    val isLiveBucket = index == liveIndex && liveOccupancy != null
                    val value = if (isLiveBucket) liveOccupancy else bucket.occupancyPercentage
                    val cx = (index + 0.5f) * slotStep
                    val left = cx - barWidth / 2f

                    if (value == null) {
                        drawRoundRect(
                            color = emptyBarColor,
                            topLeft = Offset(left, barBottom - emptyHeight),
                            size = Size(barWidth, emptyHeight),
                            cornerRadius = corner,
                        )
                    } else {
                        val fraction = value.coerceIn(0, 100) / 100f
                        val barHeight = (fraction * barAreaHeight).coerceAtLeast(minDataHeight)
                        val barTop = barBottom - barHeight
                        val barColor = when {
                            index == validSelectedIndex -> selectedBarColor
                            isLiveBucket -> liveBarColor
                            else -> normalBarColor
                        }
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(left, barTop),
                            size = Size(barWidth, barHeight),
                            cornerRadius = corner,
                        )

                        if (index == activeIndex) {
                            val lineEndY = (barTop - 4.dp.toPx()).coerceAtLeast(0f)
                            if (lineEndY > 2.dp.toPx()) {
                                drawLine(
                                    color = dottedLineColor,
                                    start = Offset(cx, 0f),
                                    end = Offset(cx, lineEndY),
                                    strokeWidth = 1.5.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    pathEffect = PathEffect.dashPathEffect(
                                        floatArrayOf(1.5.dp.toPx(), 3.5.dp.toPx()),
                                    ),
                                )
                            }
                        }
                    }
                }

                val tickTop = barBottom + 3.dp.toPx()
                val tickBottom = size.height
                tickIndices.forEach { index ->
                    val cx = (index + 0.5f) * slotStep
                    drawLine(
                        color = tickColor,
                        start = Offset(cx, tickTop),
                        end = Offset(cx, tickBottom),
                        strokeWidth = 1.25.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }

            Row(modifier = Modifier.fillMaxSize()) {
                buckets.forEachIndexed { index, bucket ->
                    val isLiveBucket = index == liveIndex && liveOccupancy != null
                    val value = if (isLiveBucket) liveOccupancy else bucket.occupancyPercentage
                    val slotTag = LibraryTestTags.crowdingSlot(weekday, index)
                    if (value != null) {
                        val timeRange = formatCrowdingSlotRange(bucket)
                        val tierLabel = occupancyLevelLabel(occupancyLevelOf(value))
                        val slotDescription = if (isLiveBucket && liveTitle != null) {
                            stringResource(R.string.library_crowding_cd_slot_live, timeRange, liveTitle, tierLabel)
                        } else {
                            stringResource(R.string.library_crowding_cd_slot, timeRange, tierLabel)
                        }
                        val isActive = index == activeIndex
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .testTag(slotTag)
                                .semantics {
                                    contentDescription = slotDescription
                                    selected = isActive
                                }
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    role = Role.Button,
                                    onClick = {
                                        haptic.tap()
                                        onSelectSlot(
                                            toggleCrowdingSelection(
                                                currentSelection = validSelectedIndex,
                                                tappedIndex = index,
                                                hasData = true,
                                                liveIndex = liveIndex,
                                            ),
                                        )
                                    },
                                ),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .testTag(slotTag),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        TimeAxisLabels(
            buckets = buckets,
            tickIndices = tickIndices,
            modifier = Modifier
                .fillMaxWidth()
                .clearAndSetSemantics {},
        )
    }
}

/** Places a callout centered over [anchorFraction] of the width, clamped inside the bounds. */
@Composable
private fun AnchoredCallout(
    anchorFraction: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        content = content,
        modifier = modifier,
    ) { measurables, constraints ->
        val child = measurables.first().measure(constraints.copy(minWidth = 0))
        val targetX = (constraints.maxWidth * anchorFraction - child.width / 2f).roundToInt()
        val maxStart = (constraints.maxWidth - child.width).coerceAtLeast(0)
        val clampedX = targetX.coerceIn(0, maxStart)
        layout(constraints.maxWidth, child.height) {
            child.placeRelative(clampedX, 0)
        }
    }
}

/** Places time axis labels directly under their ticks while skipping any label that would overlap. */
@Composable
private fun TimeAxisLabels(
    buckets: List<LibraryCrowdingBucket>,
    tickIndices: List<Int>,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    if (tickIndices.isEmpty()) {
        Text(
            text = " ",
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            modifier = modifier,
        )
        return
    }

    Layout(
        content = {
            tickIndices.forEach { index ->
                Text(
                    text = buckets[index].start.format(TimeFormat),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0)) }
        val height = placeables.maxOfOrNull { it.height } ?: 0
        val minGapPx = 4.dp.roundToPx()
        val bucketCount = buckets.size
        val slotStep = constraints.maxWidth.toFloat() / bucketCount

        layout(constraints.maxWidth, height) {
            var lastRightEdge = Int.MIN_VALUE
            tickIndices.forEachIndexed { idx, bucketIndex ->
                val placeable = placeables[idx]
                val cx = (bucketIndex + 0.5f) * slotStep
                val maxStart = (constraints.maxWidth - placeable.width).coerceAtLeast(0)
                val x = (cx - placeable.width / 2f).roundToInt().coerceIn(0, maxStart)
                if (x >= lastRightEdge + minGapPx) {
                    placeable.placeRelative(x, 0)
                    lastRightEdge = x + placeable.width
                }
            }
        }
    }
}

@Composable
private fun CrowdingPageDots(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            val width by animateDpAsState(
                targetValue = if (selected) 22.dp else 6.dp,
                label = "crowding_dot_width",
            )
            Box(
                modifier = Modifier
                    .width(width)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) scheme.onSurfaceVariant else scheme.onSurfaceVariant.copy(alpha = 0.3f),
                    ),
            )
        }
    }
}

private fun DayOfWeek.localizedFullName(locale: java.util.Locale): String =
    getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.titlecase(locale) }

/** The date to caption a day's bars with, when they are not today's own forecast. */
internal fun cachedForecastDate(page: LibraryWeekdayCrowdingPage, today: LocalDate): LocalDate? {
    val date = (page.state as? LibraryDayCrowdingState.Data)?.sourceDate ?: return null
    return date.takeIf { !page.isToday || it.isBefore(today) }
}

/** Formats a crowding slot's time span for the callout and accessibility label, e.g. "10:30–11:00". */
internal fun formatCrowdingSlotRange(bucket: LibraryCrowdingBucket): String =
    "${bucket.start.format(TimeFormat)}–${bucket.end.format(TimeFormat)}"

/**
 * Computes the next selected slot index after tapping [tappedIndex]: empty slots leave selection
 * unchanged, tapping the live bar returns to the default live callout, and tapping the currently
 * selected bar clears the selection.
 */
internal fun toggleCrowdingSelection(
    currentSelection: Int?,
    tappedIndex: Int,
    hasData: Boolean,
    liveIndex: Int?,
): Int? = when {
    !hasData -> currentSelection
    liveIndex != null && tappedIndex == liveIndex -> null
    currentSelection == tappedIndex -> null
    else -> tappedIndex
}

/** Picks evenly spaced whole-hour bucket indices (every 1–3 hours depending on day span). */
internal fun selectWholeHourTickIndices(buckets: List<LibraryCrowdingBucket>): List<Int> {
    val wholeHours = buckets.indices.filter { buckets[it].start.minute == 0 }
    if (wholeHours.isEmpty()) return emptyList()
    val step = when {
        wholeHours.size <= 4 -> 1
        wholeHours.size <= 9 -> 2
        else -> 3
    }
    return wholeHours.filterIndexed { index, _ -> index % step == 0 }
}

@Preview(name = "CrowdingChart Live Open · Light", showBackground = true)
@Composable
private fun CrowdingChartLiveOpenLightPreview() {
    LibraryPreview(dark = false) {
        LibraryCrowdingChart(
            pages = LibraryPreviewDefaults.CrowdingWeekOpen,
            today = LibraryPreviewDefaults.Today,
            nowTime = LibraryPreviewDefaults.NowTime,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    name = "CrowdingChart Live Open · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun CrowdingChartLiveOpenDarkPreview() {
    LibraryPreview(dark = true) {
        LibraryCrowdingChart(
            pages = LibraryPreviewDefaults.CrowdingWeekOpen,
            today = LibraryPreviewDefaults.Today,
            nowTime = LibraryPreviewDefaults.NowTime,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "CrowdingChart Bar Selected · Light", showBackground = true)
@Composable
private fun CrowdingChartSelectedLightPreview() {
    LibraryPreview(dark = false) {
        LibraryCrowdingChart(
            pages = LibraryPreviewDefaults.CrowdingWeekOpen,
            today = LibraryPreviewDefaults.Today,
            nowTime = LibraryPreviewDefaults.NowTime,
            initialSelectedIndexOverride = 14,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    name = "CrowdingChart Bar Selected · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun CrowdingChartSelectedDarkPreview() {
    LibraryPreview(dark = true) {
        LibraryCrowdingChart(
            pages = LibraryPreviewDefaults.CrowdingWeekOpen,
            today = LibraryPreviewDefaults.Today,
            nowTime = LibraryPreviewDefaults.NowTime,
            initialSelectedIndexOverride = 14,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "CrowdingChart Closed Now · Light", showBackground = true)
@Composable
private fun CrowdingChartClosedNowLightPreview() {
    LibraryPreview(dark = false) {
        LibraryCrowdingChart(
            pages = LibraryPreviewDefaults.CrowdingWeekClosedNow,
            today = LibraryPreviewDefaults.Today,
            nowTime = LocalTime.of(7, 30),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    name = "CrowdingChart Closed Now · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun CrowdingChartClosedNowDarkPreview() {
    LibraryPreview(dark = true) {
        LibraryCrowdingChart(
            pages = LibraryPreviewDefaults.CrowdingWeekClosedNow,
            today = LibraryPreviewDefaults.Today,
            nowTime = LocalTime.of(7, 30),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "CrowdingChart Other Weekday · Light", showBackground = true)
@Composable
private fun CrowdingChartOtherWeekdayLightPreview() {
    LibraryPreview(dark = false) {
        LibraryCrowdingChart(
            pages = LibraryPreviewDefaults.CrowdingWeekOpen,
            today = LibraryPreviewDefaults.Today,
            nowTime = LibraryPreviewDefaults.NowTime,
            initialPageOverride = DayOfWeek.MONDAY.ordinal,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    name = "CrowdingChart Other Weekday · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun CrowdingChartOtherWeekdayDarkPreview() {
    LibraryPreview(dark = true) {
        LibraryCrowdingChart(
            pages = LibraryPreviewDefaults.CrowdingWeekOpen,
            today = LibraryPreviewDefaults.Today,
            nowTime = LibraryPreviewDefaults.NowTime,
            initialPageOverride = DayOfWeek.MONDAY.ordinal,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "CrowdingChart Missing Day · Light", showBackground = true)
@Composable
private fun CrowdingChartMissingDayLightPreview() {
    LibraryPreview(dark = false) {
        LibraryCrowdingChart(
            pages = LibraryPreviewDefaults.CrowdingWeekOpen,
            today = LibraryPreviewDefaults.Today,
            nowTime = LibraryPreviewDefaults.NowTime,
            initialPageOverride = DayOfWeek.FRIDAY.ordinal,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    name = "CrowdingChart Missing Day · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun CrowdingChartMissingDayDarkPreview() {
    LibraryPreview(dark = true) {
        LibraryCrowdingChart(
            pages = LibraryPreviewDefaults.CrowdingWeekOpen,
            today = LibraryPreviewDefaults.Today,
            nowTime = LibraryPreviewDefaults.NowTime,
            initialPageOverride = DayOfWeek.FRIDAY.ordinal,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "CrowdingChart Closed Day · Light", showBackground = true)
@Composable
private fun CrowdingChartClosedDayLightPreview() {
    LibraryPreview(dark = false) {
        LibraryCrowdingChart(
            pages = LibraryPreviewDefaults.CrowdingWeekOpen,
            today = LibraryPreviewDefaults.Today,
            nowTime = LibraryPreviewDefaults.NowTime,
            initialPageOverride = DayOfWeek.SUNDAY.ordinal,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    name = "CrowdingChart Closed Day · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun CrowdingChartClosedDayDarkPreview() {
    LibraryPreview(dark = true) {
        LibraryCrowdingChart(
            pages = LibraryPreviewDefaults.CrowdingWeekOpen,
            today = LibraryPreviewDefaults.Today,
            nowTime = LibraryPreviewDefaults.NowTime,
            initialPageOverride = DayOfWeek.SUNDAY.ordinal,
            modifier = Modifier.padding(16.dp),
        )
    }
}
