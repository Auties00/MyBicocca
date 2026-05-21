package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.career.CareerId
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

interface CalendarRepository {
    fun observeMonth(careerId: CareerId, yearMonth: YearMonth): Flow<Loadable<List<CalendarEvent>>>
    fun observeDay(careerId: CareerId, date: LocalDate): Flow<Loadable<List<CalendarEvent>>>
    fun observeUpcoming(careerId: CareerId, from: LocalDate, limit: Int): Flow<Loadable<List<CalendarEvent>>>
    suspend fun refreshMonth(careerId: CareerId, yearMonth: YearMonth, force: Boolean = false)
    suspend fun prefetchAdjacent(careerId: CareerId, yearMonth: YearMonth)
}
