package it.attendance100.mybicocca.data.datasource.calendar

import it.attendance100.mybicocca.data.api.easystaff.EasyStaffApi
import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffAcademicYear
import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffScheduleCell
import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffStudyProgramSubject
import it.attendance100.mybicocca.data.model.calendar.CalendarEvent
import it.attendance100.mybicocca.data.model.calendar.EventSource
import it.attendance100.mybicocca.util.matchesPartition
import it.attendance100.mybicocca.util.normalizeCourseName
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

data class PlannedCourseTarget(
    val activityName: String,
    val partialCode: String?,
)

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
     * @param maxStudyYear furthest year of study (1-based); subjects from later years are dropped
     * @param targets the planned courses to fetch — matched by normalized name with
     *   optional partition tie-break on [PlannedCourseTarget.partialCode]
     */
    suspend fun getScheduleEvents(
        range: ClosedRange<LocalDate>,
        programCode: String,
        maxStudyYear: Int?,
        targets: Set<PlannedCourseTarget>,
    ): List<CalendarEvent> = withContext(ioDispatcher) {
        if (targets.isEmpty()) return@withContext emptyList()

        val academicYears = easyStaffApi.core.getAcademicYears()
        val currentAcademicYear = academicYears.firstOrNull() ?: return@withContext emptyList()

        val programs = easyStaffApi.core.getStudyPrograms(currentAcademicYear)
        val matchedProgram = programs.firstOrNull { it.code == programCode }
            ?: return@withContext emptyList()

        val candidateSubjects = matchedProgram.years
            .filter { maxStudyYear == null || it.year <= maxStudyYear }
            .flatMap { it.subjects }

        val subjectsByName = candidateSubjects.groupBy { it.name.normalizeCourseName() }

        val subjects = targets
            .flatMap { target -> target.pickSubjects(subjectsByName) }
            .distinctBy { it.id }

        if (subjects.isEmpty()) return@withContext emptyList()

        fetchSubjectsParallel(currentAcademicYear, subjects, range)
            .map { it.toCalendarEvent() }
    }

    private fun PlannedCourseTarget.pickSubjects(
        subjectsByName: Map<String, List<EasyStaffStudyProgramSubject>>,
    ): List<EasyStaffStudyProgramSubject> {
        val matches = subjectsByName[activityName.normalizeCourseName()].orEmpty()
        if (matches.size <= 1 || partialCode.isNullOrBlank()) return matches
        val partitioned = matches.filter { matchesPartition(it.name, partialCode) }
        return partitioned.ifEmpty { matches }
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
