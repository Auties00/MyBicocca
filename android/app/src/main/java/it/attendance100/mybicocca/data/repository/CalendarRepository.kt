package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.CalendarDao
import it.attendance100.mybicocca.data.database.dao.CareerDao
import it.attendance100.mybicocca.data.datasource.calendar.EasyStaffCalendarDataSource
import it.attendance100.mybicocca.data.datasource.calendar.ElearningCalendarDataSource
import it.attendance100.mybicocca.data.datasource.calendar.Esse3CalendarDataSource
import it.attendance100.mybicocca.data.model.calendar.CalendarEvent
import it.attendance100.mybicocca.data.model.calendar.EventSource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRepository @Inject constructor(
    private val easyStaffCalendar: EasyStaffCalendarDataSource,
    private val elearningCalendar: ElearningCalendarDataSource,
    private val esse3Calendar: Esse3CalendarDataSource,
    private val dao: CalendarDao,
    private val careerDao: CareerDao,
) {
    fun observeEvents(range: ClosedRange<LocalDate>): Flow<List<CalendarEvent>> =
        dao.observeInRange(range.start, range.endInclusive)

    fun observeEventsForMonth(month: java.time.YearMonth): Flow<List<CalendarEvent>> =
        dao.observeInRange(month.atDay(1), month.atEndOfMonth())

    fun observeFutureEvents(): Flow<List<CalendarEvent>> =
        dao.observeFrom(LocalDate.now())

    fun observeEventsBySource(source: EventSource): Flow<List<CalendarEvent>> =
        dao.observeBySource(source)

    suspend fun refreshAll(range: ClosedRange<LocalDate>): Result<Unit> {
        val results = coroutineScope {
            listOf(
                async { runCatching { refreshSchedule(range) } },
                async { runCatching { refreshDeadlines(range) } },
                async { runCatching { refreshAppointments(range) } },
            ).awaitAll()
        }
        return if (results.any { it.isSuccess }) Result.success(Unit)
        else Result.failure(results.first { it.isFailure }.exceptionOrNull()!!)
    }

    private suspend fun refreshSchedule(range: ClosedRange<LocalDate>) {
        val programCodes = careerDao.getAll().mapNotNull { it.courseOfStudyCode }
        val events = easyStaffCalendar.getScheduleEvents(range, programCodes)
        dao.deleteBySourceAndRange(EventSource.EASYSTAFF, range.start, range.endInclusive)
        dao.upsertAll(events)
    }

    private suspend fun refreshDeadlines(range: ClosedRange<LocalDate>) {
        val events = elearningCalendar.getActionEvents(range)
        dao.deleteBySourceAndRange(EventSource.ELEARNING, range.start, range.endInclusive)
        dao.upsertAll(events)
    }

    private suspend fun refreshAppointments(range: ClosedRange<LocalDate>) {
        val events = esse3Calendar.getAppointmentEvents(range)
        dao.deleteBySourceAndRange(EventSource.ESSE3, range.start, range.endInclusive)
        dao.upsertAll(events)
    }
}
