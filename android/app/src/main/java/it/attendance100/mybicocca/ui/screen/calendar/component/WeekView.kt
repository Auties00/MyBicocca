package it.attendance100.mybicocca.ui.screen.calendar.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.ui.screen.calendar.ext.weekStartFor
import it.attendance100.mybicocca.ui.screen.calendar.state.timelineWindowFor
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

private const val PAGER_ANCHOR = 5000
private const val PAGER_COUNT = 10000

private val StripRowHeight = 60.dp
private val StripBottomSpacing = 12.dp

/**
 * Week mode of the calendar tab: an hour gutter beside a virtually unbounded horizontal
 * pager of weeks, each page stacking its day strip over the seven-column events layer.
 * The whole stack scrolls vertically as one, and a vertical two-finger pinch rescales the
 * timeline around the gesture's focal point, compensating the scroll offset so the
 * content under the fingers stays anchored.
 *
 * Settling on a new page moves the selection — today when the page is the current week,
 * its Monday otherwise — while external week changes animate the pager into place.
 *
 * The pinch handler reads zoom through a mutable ref that is rewritten on every
 * composition and synchronously during the gesture, because `pointerInput(Unit)` captures
 * its lambda once and would otherwise see stale values between frames.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WeekView(
    weekStart: LocalDate,
    eventsByDay: Map<LocalDate, List<CalendarEvent>>,
    onSelectDay: (LocalDate) -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier,
    zoom: Float = TIMELINE_ZOOM_DEFAULT,
    onZoomChange: (Float) -> Unit = {},
) {
    val anchorWeekStart = remember { weekStart }
    val pagerState = rememberPagerState(initialPage = PAGER_ANCHOR) { PAGER_COUNT }

    LaunchedEffect(weekStart) {
        val target = PAGER_ANCHOR + ChronoUnit.WEEKS.between(anchorWeekStart, weekStart).toInt()
        if (pagerState.settledPage != target && !pagerState.isScrollInProgress) {
            pagerState.animateScrollToPage(target)
        }
    }

    LaunchedEffect(pagerState.settledPage) {
        val pageWeekStart = anchorWeekStart.plusWeeks((pagerState.settledPage - PAGER_ANCHOR).toLong())
        if (pageWeekStart != weekStart) {
            val today = LocalDate.now()
            val target = if (weekStartFor(today) == pageWeekStart) today else pageWeekStart
            onSelectDay(target)
        }
    }

    val zoomRef = remember { mutableFloatStateOf(zoom) }
    SideEffect { zoomRef.floatValue = zoom }

    val minuteHeight = minuteHeightFor(zoom)
    val window = remember(eventsByDay) { timelineWindowFor(eventsByDay.values) }
    val timelineH = timelineHeightFor(minuteHeight, window)
    val pageContentHeight = StripRowHeight + StripBottomSpacing + timelineH
    val scroll = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = modifier
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
        Column {
            Spacer(Modifier.height(StripRowHeight + StripBottomSpacing))
            HourGutterColumn(
                minuteHeight = minuteHeight,
                window = window,
                modifier = Modifier.height(timelineH),
            )
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .height(pageContentHeight),
            beyondViewportPageCount = 1,
        ) { page ->
            val pageWeekStart = anchorWeekStart.plusWeeks((page - PAGER_ANCHOR).toLong())
            Column {
                DayStrip(
                    weekStart = pageWeekStart,
                    onSelect = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(StripRowHeight),
                    contentPadding = PaddingValues(0.dp),
                    leadingSpacerWidth = 0.dp,
                )
                Spacer(Modifier.height(StripBottomSpacing))
                WeekEventsLayer(
                    weekStart = pageWeekStart,
                    eventsByDay = eventsByDay,
                    onEventClick = onEventClick,
                    minuteHeight = minuteHeight,
                    window = window,
                )
            }
        }
    }
}
