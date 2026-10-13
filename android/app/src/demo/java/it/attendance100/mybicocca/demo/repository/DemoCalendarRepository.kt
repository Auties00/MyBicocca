package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.demo.DemoClock
import it.attendance100.mybicocca.demo.DemoCourse
import it.attendance100.mybicocca.demo.DemoCourses
import it.attendance100.mybicocca.demo.DemoElearning
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.calendar.CalendarEventId
import it.attendance100.mybicocca.domain.model.calendar.EventLocation
import it.attendance100.mybicocca.domain.model.calendar.EventSource
import it.attendance100.mybicocca.domain.model.calendar.EventStatus
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A weekly timetable repeated across the semester, plus a handful of one-off exams, deadlines,
 * an appointment and a library seat placed around [DemoClock.now].
 */
@Singleton
class DemoCalendarRepository @Inject constructor() : CalendarRepository {

    override fun observeMonth(careerId: CareerId, yearMonth: YearMonth): Flow<Loadable<List<CalendarEvent>>> =
        loaded(eventsBetween(careerId, yearMonth.atDay(1), yearMonth.atEndOfMonth()))

    override fun observeDay(careerId: CareerId, date: LocalDate): Flow<Loadable<List<CalendarEvent>>> =
        loaded(eventsBetween(careerId, date, date))

    override fun observeUpcoming(careerId: CareerId, from: LocalDate, limit: Int): Flow<Loadable<List<CalendarEvent>>> =
        loaded(eventsBetween(careerId, from, from.plusMonths(3)).take(limit))

    override fun observeMonthHydrated(careerId: CareerId, yearMonth: YearMonth): Flow<Boolean> = flowOf(true)

    override suspend fun isMonthFresh(careerId: CareerId, yearMonth: YearMonth): Boolean = true

    override suspend fun refreshMonth(careerId: CareerId, yearMonth: YearMonth, force: Boolean) = Unit

    override suspend fun prefetchAdjacent(careerId: CareerId, yearMonth: YearMonth) = Unit

    private fun loaded(events: List<CalendarEvent>): Flow<Loadable<List<CalendarEvent>>> =
        flowOf(Loadable.Loaded(events))

    private fun eventsBetween(careerId: CareerId, from: LocalDate, to: LocalDate): List<CalendarEvent> {
        val lessons = generateSequence(from) { it.plusDays(1) }
            .takeWhile { !it.isAfter(to) }
            .filter { !it.isBefore(SEMESTER_START) && !it.isAfter(SEMESTER_END) }
            .flatMap { date -> timetable.filter { it.day == date.dayOfWeek }.map { it.toEvent(careerId, date) } }
            .toList()
        val oneOffs = oneOffs(careerId).filter { !it.date.isBefore(from) && !it.date.isAfter(to) }
        return (lessons + oneOffs).sortedWith(compareBy({ it.date }, { it.start }))
    }

    private data class Slot(
        val day: DayOfWeek,
        val course: DemoCourse,
        val start: LocalTime,
        val end: LocalTime,
        val room: String,
        val building: String,
        val notes: String? = null,
    ) {
        fun toEvent(careerId: CareerId, date: LocalDate) = CalendarEvent.Lesson(
            id = CalendarEventId.of(EventSource.LESSON, "${course.code}-$date-$start"),
            careerId = careerId,
            date = date,
            start = start,
            end = end,
            title = course.name,
            shortLabel = course.shortLabel,
            location = EventLocation(room = room, building = building),
            status = EventStatus.CONFIRMED,
            notes = notes,
            activityCode = course.code,
            subjectCode = course.code,
            teachers = listOf(course.teacher),
            cfu = course.cfu,
        )
    }

    private val timetable = listOf(
        Slot(DayOfWeek.MONDAY, DemoCourses.algorithms, time(8, 30), time(10, 30), "U6-06", "U6"),
        Slot(DayOfWeek.MONDAY, DemoCourses.statistics, time(10, 30), time(12, 30), "U4-08", "U4"),
        Slot(DayOfWeek.MONDAY, DemoCourses.languages, time(14, 30), time(16, 30), "U1-09", "U1"),
        Slot(DayOfWeek.TUESDAY, DemoCourses.operatingSystems, time(9, 30), time(12, 30), "U14-T024", "U14"),
        Slot(DayOfWeek.TUESDAY, DemoCourses.databases, time(14, 30), time(16, 30), "U24-C2", "U24"),
        Slot(DayOfWeek.WEDNESDAY, DemoCourses.algorithms, time(10, 30), time(12, 30), "U6-06", "U6"),
        Slot(DayOfWeek.WEDNESDAY, DemoCourses.languages, time(13, 30), time(15, 30), "U1-09", "U1"),
        Slot(DayOfWeek.THURSDAY, DemoCourses.databases, time(9, 30), time(11, 30), "U24-C2", "U24"),
        Slot(DayOfWeek.THURSDAY, DemoCourses.operatingSystems, time(13, 30), time(15, 30), "LAB 713", "U7", "Laboratorio"),
        Slot(DayOfWeek.FRIDAY, DemoCourses.statistics, time(8, 30), time(10, 30), "U4-08", "U4"),
        Slot(DayOfWeek.FRIDAY, DemoCourses.algorithms, time(10, 30), time(12, 30), "U6-06", "U6", "Esercitazione"),
    )

    private fun oneOffs(careerId: CareerId): List<CalendarEvent> {
        val today = DemoClock.now.toLocalDate()
        return listOf(
            CalendarEvent.LibraryReservation(
                id = CalendarEventId.of(EventSource.LIBRARY, "9001"),
                careerId = careerId,
                date = today,
                start = time(16, 0),
                end = time(18, 30),
                title = "Biblioteca di Ateneo",
                shortLabel = "BIB",
                location = EventLocation(room = "Posto 42", building = "Sede Centrale U6"),
                status = EventStatus.CONFIRMED,
                notes = null,
                seatName = "Posto 42",
            ),
            CalendarEvent.Appointment(
                id = CalendarEventId.of(EventSource.APPOINTMENT, "7001"),
                careerId = careerId,
                date = today.plusDays(1),
                start = time(14, 0),
                end = time(14, 20),
                title = "Segreteria studenti",
                shortLabel = "SEG",
                location = EventLocation(room = "Sportello 3", building = "U17"),
                status = EventStatus.CONFIRMED,
                notes = null,
                serviceGroup = "Segreterie studenti",
                webConferenceUrl = null,
            ),
            exam(careerId, DemoCourses.statistics, today.plusDays(8), time(9, 30), "U4-08", "U4", position = 37),
            exam(careerId, DemoCourses.algorithms, today.plusDays(13), time(14, 0), "U6-06", "U6", position = 12),
        ) + DemoElearning.allAssignments.map { assignment ->
            CalendarEvent.AssignmentDeadline(
                id = CalendarEventId.of(EventSource.DEADLINE, assignment.id.toString()),
                careerId = careerId,
                date = assignment.due.toLocalDate(),
                start = assignment.due.toLocalTime(),
                end = assignment.due.toLocalTime(),
                title = assignment.title,
                shortLabel = assignment.course.shortLabel,
                status = EventStatus.CONFIRMED,
                activityCode = null,
                courseId = assignment.course.moodleCourseId,
                assignmentId = assignment.id,
            )
        }
    }

    private fun exam(
        careerId: CareerId,
        course: DemoCourse,
        date: LocalDate,
        start: LocalTime,
        room: String,
        building: String,
        position: Int,
    ) = CalendarEvent.Exam(
        id = CalendarEventId.of(EventSource.EXAM, "${course.code}-$date"),
        careerId = careerId,
        date = date,
        start = start,
        end = start.plusHours(2),
        title = course.name,
        shortLabel = course.shortLabel,
        location = EventLocation(room = room, building = building),
        status = EventStatus.CONFIRMED,
        notes = null,
        activityCode = course.code,
        examTypeLabel = "Scritto",
        bookingPosition = position,
        bookedAt = DemoClock.now.minusDays(9).withHour(18).withMinute(47),
        cancellableUntil = date.minusDays(3),
    )

    private companion object {
        val SEMESTER_START: LocalDate = LocalDate.of(2026, 3, 2)
        val SEMESTER_END: LocalDate = LocalDate.of(2026, 6, 26)

        fun time(hour: Int, minute: Int): LocalTime = LocalTime.of(hour, minute)
    }
}
