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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.ui.screen.calendar.ext.highlightedDayFor
import it.attendance100.mybicocca.ui.screen.calendar.ext.weekStartFor
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private const val PAGER_ANCHOR = 5000
private const val PAGER_COUNT = 10000

private val StripRowHeight = 60.dp
private val StripBottomSpacing = 12.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WeekView(
    selectedDay: LocalDate,
    weekStart: LocalDate,
    eventsByDay: Map<LocalDate, List<CalendarEvent>>,
    onSelectDay: (LocalDate) -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier,
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

    val pageContentHeight = StripRowHeight + StripBottomSpacing + TimelineHeight

    Row(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
    ) {
        Column {
            Spacer(Modifier.height(StripRowHeight + StripBottomSpacing))
            HourGutterColumn(modifier = Modifier.height(TimelineHeight))
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).height(pageContentHeight),
            beyondViewportPageCount = 1,
        ) { page ->
            val pageWeekStart = anchorWeekStart.plusWeeks((page - PAGER_ANCHOR).toLong())
            val highlightedDay = highlightedDayFor(pageWeekStart, selectedDay)
            Column {
                DayStrip(
                    weekStart = pageWeekStart,
                    selectedDay = highlightedDay,
                    onSelect = onSelectDay,
                    modifier = Modifier.fillMaxWidth().height(StripRowHeight),
                    contentPadding = PaddingValues(0.dp),
                    leadingSpacerWidth = 0.dp,
                )
                Spacer(Modifier.height(StripBottomSpacing))
                WeekEventsLayer(
                    weekStart = pageWeekStart,
                    selectedDay = highlightedDay,
                    eventsByDay = eventsByDay,
                    onEventClick = onEventClick,
                )
            }
        }
    }
}
