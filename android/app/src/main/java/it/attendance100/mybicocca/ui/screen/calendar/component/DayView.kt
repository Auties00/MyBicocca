package it.attendance100.mybicocca.ui.screen.calendar.component

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.ui.screen.calendar.ext.weekStartFor
import it.attendance100.mybicocca.ui.screen.calendar.state.timelineWindowFor
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.floor
import kotlin.math.roundToInt

private const val PAGER_ANCHOR = 5000
private const val PAGER_COUNT = 10000

private const val STRIP_HALF_WINDOW = 5
private const val STRIP_WINDOW_SIZE = 2 * STRIP_HALF_WINDOW + 1

private val StripRowHeight = 60.dp
private val CellGapDp = 2.dp

/**
 * Day mode of the calendar tab: a week-strip carousel header over an hour-gutter timeline
 * whose day pages swipe through a virtually unbounded horizontal pager. Dragging a day
 * page pulls the header strip along fractionally — crossing a week boundary slides the
 * whole strip to the adjacent week — and tapping a strip day jumps the pager there.
 * A vertical two-finger pinch rescales the timeline around the gesture's focal point,
 * compensating the scroll offset so the content under the fingers stays anchored; [zoom]
 * is hoisted so day and week modes share one scale.
 *
 * The pinch handler reads zoom through a mutable ref that is rewritten on every
 * composition and synchronously during the gesture, because `pointerInput(Unit)` captures
 * its lambda once and would otherwise see stale values between frames.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DayView(
    selectedDay: LocalDate,
    eventsByDay: Map<LocalDate, List<CalendarEvent>>,
    onSelectDay: (LocalDate) -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier,
    zoom: Float = TIMELINE_ZOOM_DEFAULT,
    onZoomChange: (Float) -> Unit = {},
) {
    val anchorDay = remember { selectedDay }
    val anchorWeekStart = remember { weekStartFor(anchorDay) }
    val eventsPagerState = rememberPagerState(initialPage = PAGER_ANCHOR) { PAGER_COUNT }

    LaunchedEffect(selectedDay) {
        val target = PAGER_ANCHOR + (selectedDay.toEpochDay() - anchorDay.toEpochDay()).toInt()
        if (eventsPagerState.settledPage != target && !eventsPagerState.isScrollInProgress) {
            eventsPagerState.animateScrollToPage(target)
        }
    }

    LaunchedEffect(eventsPagerState.settledPage) {
        val pageDay = anchorDay.plusDays((eventsPagerState.settledPage - PAGER_ANCHOR).toLong())
        if (pageDay != selectedDay) onSelectDay(pageDay)
    }

    val zoomRef = remember { mutableFloatStateOf(zoom) }
    SideEffect { zoomRef.floatValue = zoom }

    val minuteHeight = minuteHeightFor(zoom)
    val window = remember(eventsByDay) { timelineWindowFor(eventsByDay.values) }
    val timelineH = timelineHeightFor(minuteHeight, window)
    val scroll = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp, end = 14.dp)) {
            Spacer(Modifier.width(TimelineGutterWidth))
            DayStripCarousel(
                eventsPagerState = eventsPagerState,
                anchorDay = anchorDay,
                anchorWeekStart = anchorWeekStart,
                onSelect = onSelectDay,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalPinchZoom(currentZoom = { zoomRef.floatValue }) { newZoom, focalY, factor ->
                    zoomRef.floatValue = newZoom
                    onZoomChange(newZoom)
                    val newOffset = ((scroll.value + focalY) * factor - focalY)
                        .roundToInt().coerceIn(0, scroll.maxValue)
                    coroutineScope.launch { scroll.scrollTo(newOffset) }
                }
                .verticalScroll(scroll)
                .padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
        ) {
            HourGutterColumn(
                minuteHeight = minuteHeight,
                window = window,
                modifier = Modifier.height(timelineH),
            )
            HorizontalPager(
                state = eventsPagerState,
                modifier = Modifier
                    .weight(1f)
                    .height(timelineH),
                beyondViewportPageCount = 1,
            ) { page ->
                val day = anchorDay.plusDays((page - PAGER_ANCHOR).toLong())
                DayEventsLayer(
                    selectedDay = day,
                    events = eventsByDay[day].orEmpty(),
                    onEventClick = onEventClick,
                    minuteHeight = minuteHeight,
                    window = window,
                )
            }
        }
    }
}

/**
 * Header strip that tracks the day pager continuously. A window of week strips around the
 * current week renders unbounded inside a row translated so exactly one week fills the
 * viewport; the week position interpolates only while the dragged day crosses a week
 * boundary, so mid-week swipes keep the strip still. The selection pill travels
 * cell-to-cell with the fractional page position, wrapping across week rows.
 */
@Composable
private fun DayStripCarousel(
    eventsPagerState: PagerState,
    anchorDay: LocalDate,
    anchorWeekStart: LocalDate,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dayPos by remember(eventsPagerState) {
        derivedStateOf {
            eventsPagerState.currentPage + eventsPagerState.currentPageOffsetFraction
        }
    }

    val weekPos by remember(anchorDay, anchorWeekStart) {
        derivedStateOf {
            val floorOffset = floor(dayPos.toDouble()).toInt()
            val frac = (dayPos - floorOffset).coerceIn(0f, 1f)
            val floorDay = anchorDay.plusDays((floorOffset - PAGER_ANCHOR).toLong())
            val ceilDay = floorDay.plusDays(1)
            val weekFloor = ChronoUnit.WEEKS.between(anchorWeekStart, weekStartFor(floorDay)).toInt()
            val weekCeil = ChronoUnit.WEEKS.between(anchorWeekStart, weekStartFor(ceilDay)).toInt()
            if (weekFloor == weekCeil) weekFloor.toFloat()
            else weekFloor.toFloat() + frac
        }
    }

    val windowStart by remember {
        derivedStateOf { floor(weekPos + 0.5f).toInt() - STRIP_HALF_WINDOW }
    }

    val anchorDayOffsetInWeek = remember(anchorDay, anchorWeekStart) {
        (anchorDay.toEpochDay() - anchorWeekStart.toEpochDay()).toInt()
    }

    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val highlightColor = if (dark) scheme.primaryContainer else scheme.primary

    BoxWithConstraints(
        modifier = modifier
            .height(StripRowHeight)
            .clipToBounds()
    ) {
        val density = LocalDensity.current
        val stripPxWidth = constraints.maxWidth.toFloat()
        val cellGapPx = with(density) { CellGapDp.toPx() }
        val cellPxWidth = (stripPxWidth - 6 * cellGapPx) / 7
        val stripDpWidth = with(density) { stripPxWidth.toDp() }
        val cellDpWidth = with(density) { cellPxWidth.toDp() }

        Box(
            modifier = Modifier
                .wrapContentSize(unbounded = true, align = Alignment.CenterStart)
                .graphicsLayer {
                    translationX = -(weekPos - windowStart) * stripPxWidth
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            HighlightPill(
                color = highlightColor,
                modifier = Modifier
                    .width(cellDpWidth)
                    .graphicsLayer {
                        val dayIndexInCarousel =
                            anchorDayOffsetInWeek + (dayPos - PAGER_ANCHOR) - windowStart * 7
                        val floorN = floor(dayIndexInCarousel.toDouble()).toInt()
                        val frac = dayIndexInCarousel - floorN

                        val weekN = floorN / 7
                        val dayN = floorN - weekN * 7
                        val xN = weekN * stripPxWidth + dayN * (cellPxWidth + cellGapPx)

                        val nextN = floorN + 1
                        val weekN2 = nextN / 7
                        val dayN2 = nextN - weekN2 * 7
                        val xN2 = weekN2 * stripPxWidth + dayN2 * (cellPxWidth + cellGapPx)

                        translationX = xN + frac * (xN2 - xN)
                    },
            )

            Row {
                for (offset in 0 until STRIP_WINDOW_SIZE) {
                    val weekIndex = windowStart + offset
                    key(weekIndex) {
                        val weekStart = anchorWeekStart.plusWeeks(weekIndex.toLong())
                        DayStrip(
                            weekStart = weekStart,
                            onSelect = onSelect,
                            modifier = Modifier
                                .width(stripDpWidth)
                                .height(StripRowHeight),
                            contentPadding = PaddingValues(0.dp),
                            leadingSpacerWidth = 0.dp,
                        )
                    }
                }
            }
        }
    }
}

/**
 * The sliding selection highlight, deliberately drawn outside [DayStrip] so a single pill
 * can glide continuously across week boundaries during a swipe — per-strip highlights
 * would show two half-visible pills mid-transition. Its content is transparent text
 * mirroring a strip cell's column, so the pill's natural size matches the cells exactly
 * regardless of font metrics.
 */
@Composable
private fun HighlightPill(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(CircleShape)
            .background(color)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "M",
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.3.sp,
            color = Color.Transparent,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "0",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Transparent,
        )
    }
}
