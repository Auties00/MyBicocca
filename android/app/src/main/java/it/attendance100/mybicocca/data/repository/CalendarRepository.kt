package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.CalendarDao
import it.attendance100.mybicocca.data.database.dao.CareerDao
import it.attendance100.mybicocca.data.database.dao.StudyPlanDao
import it.attendance100.mybicocca.data.datasource.calendar.EasyStaffCalendarDataSource
import it.attendance100.mybicocca.data.datasource.calendar.ElearningCalendarDataSource
import it.attendance100.mybicocca.data.datasource.calendar.Esse3CalendarDataSource
import it.attendance100.mybicocca.data.model.calendar.CalendarEvent
import it.attendance100.mybicocca.data.model.calendar.EventSource
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private const val STATUS_PASSED = "Superata"

@Singleton
class CalendarRepository @Inject constructor(
    private val easyStaffCalendar: EasyStaffCalendarDataSource,
    private val elearningCalendar: ElearningCalendarDataSource,
    private val esse3Calendar: Esse3CalendarDataSource,
    private val dao: CalendarDao,
    private val careerDao: CareerDao,
    private val studyPlanDao: StudyPlanDao,
) {
    fun observeEvents(range: ClosedRange<LocalDate>): Flow<List<CalendarEvent>> =
        dao.observeInRange(range.start, range.endInclusive)

    fun observeEventsForMonth(month: java.time.YearMonth): Flow<List<CalendarEvent>> =
        dao.observeInRange(month.atDay(1), month.atEndOfMonth())

    fun observeFutureEvents(): Flow<List<CalendarEvent>> =
        dao.observeFrom(LocalDate.now())

    fun observeEventsBySource(source: EventSource): Flow<List<CalendarEvent>> =
        dao.observeBySource(source)

    suspend fun refreshAll(range: ClosedRange<LocalDate>): Result<Unit> = runCatching {
        refreshSchedule(range)
    }

    private suspend fun refreshSchedule(range: ClosedRange<LocalDate>) {
        val career = careerDao.getAll().firstOrNull() ?: return
        val programCode = career.courseOfStudyCode ?: return

        val currentAcademicYearStart = computeAcademicYearStart(range.start)
        val maxStudyYear = career.enrollmentYear?.let { enrollment ->
            (currentAcademicYearStart - enrollment + 1).coerceAtLeast(1)
        }

        val planHeader = studyPlanDao.getHeadersByStudentId(career.studentId).firstOrNull()
        val plannedCourses = planHeader?.let { studyPlanDao.getCoursesByPlanId(it.id) } ?: emptyList()

        val unpassedCourseNames = plannedCourses
            .filter { !it.statusDescription.equals(STATUS_PASSED, ignoreCase = true) }
            .filter { maxStudyYear == null || (it.year != null && it.year <= maxStudyYear) }
            .map { it.activityName.lowercase().trim() }
            .toSet()

        val events = easyStaffCalendar.getScheduleEvents(
            range = range,
            programCode = programCode,
            maxStudyYear = maxStudyYear,
            unpassedCourseNames = unpassedCourseNames,
        )

        dao.deleteBySourceAndRange(EventSource.EASYSTAFF, range.start, range.endInclusive)
        dao.upsertAll(events)
    }

    private fun computeAcademicYearStart(date: LocalDate): Int {
        return if (date.monthValue >= 9) date.year else date.year - 1
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
