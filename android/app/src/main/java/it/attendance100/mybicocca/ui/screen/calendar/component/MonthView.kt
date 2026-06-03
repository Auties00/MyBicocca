package it.attendance100.mybicocca.ui.screen.calendar.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

private const val PAGER_ANCHOR = 5000
private const val PAGER_COUNT = 10000

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MonthView(
    yearMonth: YearMonth,
    selectedDay: LocalDate,
    eventsByDay: Map<LocalDate, List<CalendarEvent>>,
    onSelectDay: (LocalDate) -> Unit,
    onSelectMonth: (YearMonth) -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
    onMonthSheetSizeChanged: (IntSize) -> Unit,
    modifier: Modifier = Modifier,
) {
    val anchorMonth = remember { yearMonth }
    val pagerState = rememberPagerState(initialPage = PAGER_ANCHOR) { PAGER_COUNT }

    LaunchedEffect(yearMonth) {
        val target = PAGER_ANCHOR + ChronoUnit.MONTHS.between(anchorMonth, yearMonth).toInt()
        if (pagerState.settledPage != target && !pagerState.isScrollInProgress) {
            pagerState.animateScrollToPage(target)
        }
    }

    LaunchedEffect(pagerState.settledPage) {
        val pageMonth = anchorMonth.plusMonths((pagerState.settledPage - PAGER_ANCHOR).toLong())
        if (pageMonth != yearMonth) onSelectMonth(pageMonth)
    }

    Column(modifier = modifier.fillMaxSize()) {
        Spacer(Modifier.height(6.dp))
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            beyondViewportPageCount = 1,
        ) { page ->
            val ym = anchorMonth.plusMonths((page - PAGER_ANCHOR).toLong())
            MonthGrid(
                yearMonth = ym,
                selectedDay = selectedDay,
                eventsByDay = eventsByDay,
                onDayClick = onSelectDay,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged(onMonthSheetSizeChanged),
        )
    }
}
