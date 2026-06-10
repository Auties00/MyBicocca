package it.attendance100.mybicocca.domain.usecase.calendar

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import javax.inject.Inject

/**
 * Streams one month of merged calendar events from the local cache. Backs the calendar tab's
 * month grid and agenda; the screen also collects the adjacent months through it so swiping
 * between months never starts from an empty grid.
 */
class ObserveMonthEventsUseCase @Inject constructor(
    private val repository: CalendarRepository,
) {
    operator fun invoke(careerId: CareerId, yearMonth: YearMonth): Flow<Loadable<List<CalendarEvent>>> =
        repository.observeMonth(careerId, yearMonth)
}
