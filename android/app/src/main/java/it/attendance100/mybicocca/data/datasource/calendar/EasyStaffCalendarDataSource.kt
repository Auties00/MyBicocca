package it.attendance100.mybicocca.data.datasource.calendar

import it.attendance100.mybicocca.data.api.easystaff.EasyStaffApi
import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffAcademicYear
import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffScheduleCell
import it.attendance100.mybicocca.data.model.calendar.CalendarEvent
import it.attendance100.mybicocca.data.model.calendar.EventSource
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EasyStaffCalendarDataSource @Inject constructor(
    private val easyStaffApi: EasyStaffApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getScheduleEvents(
        range: ClosedRange<LocalDate>,
        programCodes: List<String>,
    ): List<CalendarEvent> = withContext(ioDispatcher) {
        if (programCodes.isEmpty()) return@withContext emptyList()

        val academicYears = easyStaffApi.core.getAcademicYears()
        val currentAcademicYear = academicYears.firstOrNull() ?: return@withContext emptyList()

        val programs = easyStaffApi.core.getStudyPrograms(currentAcademicYear)
        val matchedPrograms = programs.filter { it.code in programCodes }
        if (matchedPrograms.isEmpty()) return@withContext emptyList()

        val allCells = mutableListOf<EasyStaffScheduleCell>()
        for (program in matchedPrograms) {
            var weekStart = range.start.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val rangeEnd = range.endInclusive

            while (!weekStart.isAfter(rangeEnd)) {
                val cells = easyStaffApi.schedule.getScheduleByProgram(
                    academicYear = currentAcademicYear,
                    studyProgram = program,
                    yearsOfStudy = program.years,
                    weekStartDate = weekStart,
                )
                allCells.addAll(cells)
                weekStart = weekStart.plusWeeks(1)
            }
        }

        allCells.filterIsInstance<EasyStaffScheduleCell.Lesson>()
            .filter { it.date in range }
            .map { lesson ->
                CalendarEvent(
                    id = "EASYSTAFF_${lesson.id}",
                    title = lesson.name ?: "",
                    date = lesson.date,
                    startTime = lesson.startTime,
                    endTime = lesson.endTime,
                    location = lesson.roomName,
                    source = EventSource.EASYSTAFF,
                    teacherName = lesson.teacherNames.firstOrNull(),
                    buildingCode = lesson.buildingCode,
                    subjectCode = lesson.subjectCode,
                )
            }
    }
}
