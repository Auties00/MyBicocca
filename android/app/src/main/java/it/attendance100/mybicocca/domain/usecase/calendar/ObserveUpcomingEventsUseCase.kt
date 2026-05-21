package it.attendance100.mybicocca.domain.usecase.calendar

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class ObserveUpcomingEventsUseCase @Inject constructor(
    private val repository: CalendarRepository,
) {
    operator fun invoke(
        careerId: CareerId,
        from: LocalDate,
        limit: Int,
    ): Flow<Loadable<List<CalendarEvent>>> = repository.observeUpcoming(careerId, from, limit)
}
