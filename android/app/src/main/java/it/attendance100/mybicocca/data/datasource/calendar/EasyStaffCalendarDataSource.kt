package it.attendance100.mybicocca.data.datasource.calendar

import it.attendance100.mybicocca.data.api.easystaff.EasyStaffApi
import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffAcademicYear
import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffScheduleCell
import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffStudyProgramSubject
import it.attendance100.mybicocca.data.model.calendar.CalendarEvent
import it.attendance100.mybicocca.data.model.calendar.EventSource
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EasyStaffCalendarDataSource @Inject constructor(
    private val easyStaffApi: EasyStaffApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    /**
     * Fetches schedule events for the student's unpassed courses using per-subject queries.
     *
     * @param range date range to fetch
     * @param programCode the student's EasyStaff study program code (from Career.courseOfStudyCode)
     * @param maxStudyYear the student's furthest year of study (1-based), or null to fetch all years
     * @param unpassedCourseNames normalized names of courses the student still needs to attend
     */
    suspend fun getScheduleEvents(
        range: ClosedRange<LocalDate>,
        programCode: String,
        maxStudyYear: Int?,
        unpassedCourseNames: Set<String>,
    ): List<CalendarEvent> = withContext(ioDispatcher) {
        val academicYears = easyStaffApi.core.getAcademicYears()
        val currentAcademicYear = academicYears.firstOrNull() ?: return@withContext emptyList()

        val programs = easyStaffApi.core.getStudyPrograms(currentAcademicYear)
        val matchedProgram = programs.firstOrNull { it.code == programCode }
            ?: return@withContext emptyList()

        val subjects = matchedProgram.years
            .let { years -> if (maxStudyYear != null) years.filter { it.year <= maxStudyYear } else years }
            .flatMap { it.subjects }
            .filter { it.name.normalize() in unpassedCourseNames }

        if (subjects.isEmpty()) return@withContext emptyList()

        fetchSubjectsParallel(currentAcademicYear, subjects, range)
            .map { it.toCalendarEvent() }
    }

    private suspend fun fetchSubjectsParallel(
        academicYear: EasyStaffAcademicYear,
        subjects: List<EasyStaffStudyProgramSubject>,
        range: ClosedRange<LocalDate>,
    ): List<EasyStaffScheduleCell.Lesson> = coroutineScope {
        subjects
            .map { subject ->
                async {
                    runCatching {
                        easyStaffApi.schedule.getScheduleBySubject(
                            academicYear = academicYear,
                            subject = subject,
                            weekStartDate = range.start,
                        )
                    }.getOrDefault(emptyList())
                }
            }
            .awaitAll()
            .flatten()
            .filterIsInstance<EasyStaffScheduleCell.Lesson>()
            .filter { it.date in range }
            .distinctBy { it.id }
    }

    private fun String.normalize(): String = lowercase().trim()

    private fun EasyStaffScheduleCell.Lesson.toCalendarEvent() = CalendarEvent(
        id = "EASYSTAFF_$id",
        title = name,
        date = date,
        startTime = startTime,
        endTime = endTime,
        location = roomName,
        source = EventSource.EASYSTAFF,
        teacherName = teacherNames.firstOrNull(),
        buildingCode = buildingCode,
        subjectCode = subjectCode,
    )
}
