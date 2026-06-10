package it.attendance100.mybicocca.data.remote.easystaff.api

import io.ktor.client.*
import it.attendance100.mybicocca.data.remote.easystaff.dto.*
import kotlinx.serialization.json.Json
import java.time.LocalDate

/**
 * API for lesson schedule operations (Orario delle lezioni).
 *
 * Provides access to:
 * - Lesson schedules by study program, teacher, or subject
 * - Weekly schedule grid with lesson details
 *
 * The schedule can be searched in three modes:
 * - By study program (corso di studio): Most common, shows all lessons for a degree program
 * - By teacher (docente): Shows all lessons taught by a specific teacher
 * - By subject (insegnamento): Shows lessons for a specific course/subject
 */
class EasyStaffScheduleApi(
    client: HttpClient,
    json: Json
) : EasyStaffAbstractApi(client, json) {
    companion object {
        private const val SCHEDULE_ENDPOINT = "/PortaleStudentiUnimib/grid_call.php"
    }

    /**
     * Gets the weekly schedule for a study program.
     *
     * @param academicYear The academic year
     * @param studyProgram The study program
     * @param yearsOfStudy The years of study to include
     * @param weekStartDate The starting date for the schedule
     * @param language The language for labels
     * @return The weekly schedule as a list of schedule cells
     */
    suspend fun getScheduleByProgram(
        academicYear: EasyStaffAcademicYear,
        studyProgram: EasyStaffStudyProgram,
        yearsOfStudy: List<EasyStaffStudyProgramYear>,
        weekStartDate: LocalDate,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): List<EasyStaffScheduleCell> {
        val params = buildMap {
            put("view", listOf("easycourse"))
            put("form-type", listOf("corso"))
            put("include", listOf("corso"))
            put("anno", listOf(academicYear.value))
            put("corso", listOf(studyProgram.code))
            put("anno2[]", yearsOfStudy.map { it.code })
            put("_lang", listOf(language.code))
            put("empty_box", listOf("1"))
            put("highlighted_date", listOf("0"))
            put("all_events", listOf("1"))
            put("date", listOf(formatDate(weekStartDate)))
            put("ar_codes_", listOf(""))
            put("ar_select_", listOf(""))
        }
        val response = executePostForm<EasyStaffScheduleResponse>(SCHEDULE_ENDPOINT, params)
        return response.cells.sorted()
    }

    /**
     * Gets the weekly schedule for a teacher.
     *
     * @param academicYear The academic year
     * @param teacher The teacher
     * @param course The study course
     * @param weekStartDate The starting date for the schedule
     * @param language The language for labels
     * @return The weekly schedule as a list of schedule cells
     */
    suspend fun getScheduleByTeacher(
        academicYear: EasyStaffAcademicYear,
        teacher: EasyStaffTeacher,
        course: EasyStaffTeacherCourse,
        yearsOfStudy: List<EasyStaffTeacherCourseYearOfStudy>,
        weekStartDate: LocalDate,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): List<EasyStaffScheduleCell> {
        val params = buildMap {
            put("view", listOf("easycourse"))
            put("form-type", listOf("docente"))
            put("include", listOf("docente"))
            put("anno", listOf(academicYear.value))
            put("docente", listOf(teacher.code))
            put("corso", listOf(course.code))
            put("anno2[]", yearsOfStudy.map { it.code })
            put("visualizzazione_orario", listOf("cal"))
            put("date", listOf(formatDate(weekStartDate)))
            put("periodo_didattico", listOf(""))
            put("_lang", listOf(language.code))
            put("list", listOf(""))
            put("week_grid_type", listOf("-1"))
            put("ar_codes_", listOf(""))
            put("ar_select_", listOf(""))
            put("col_cells", listOf("0"))
            put("empty_box", listOf("1"))
            put("only_grid", listOf("0"))
            put("highlighted_date", listOf("0"))
            put("all_events", listOf("1"))
            put("faculty_group", listOf("0"))
            put("txtcurr", listOf(yearsOfStudy.joinToString(",") { it.name }))
        }

        val response = executePostForm<EasyStaffScheduleResponse>(SCHEDULE_ENDPOINT, params)
        return response.cells.sorted()
    }

    /**
     * Gets the weekly schedule for a subject.
     *
     * @param academicYear The academic year
     * @param subject The subject
     * @param weekStartDate The starting date for the schedule
     * @param language The language for labels
     * @return The weekly schedule as a list of schedule cells
     */
    suspend fun getScheduleBySubject(
        academicYear: EasyStaffAcademicYear,
        subject: EasyStaffStudyProgramSubject,
        weekStartDate: LocalDate,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): List<EasyStaffScheduleCell> =
        getScheduleBySubjects(academicYear, listOf(subject), weekStartDate, language)

    /**
     * Gets the weekly schedule for multiple subjects in a single request.
     *
     * The `attivita[]` form parameter is an array on the EasyStaff side (the web portal
     * multi-selects subjects into one grid), so one request returns the merged weekly
     * grid for all the requested subjects. Verified against the live server: the merged
     * cell set is identical to the union of per-subject requests, at a fraction of the
     * round-trips. Each returned cell carries its subject code, so callers can attribute
     * cells back to the requested subjects.
     *
     * @param academicYear The academic year
     * @param subjects The subjects to include in the merged schedule
     * @param weekStartDate The starting date for the schedule
     * @param language The language for labels
     * @return The merged weekly schedule as a list of schedule cells
     */
    suspend fun getScheduleBySubjects(
        academicYear: EasyStaffAcademicYear,
        subjects: List<EasyStaffStudyProgramSubject>,
        weekStartDate: LocalDate,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): List<EasyStaffScheduleCell> {
        if (subjects.isEmpty()) return emptyList()
        val params = buildMap {
            put("view", listOf("easycourse"))
            put("form-type", listOf("attivita"))
            put("include", listOf("attivita"))
            put("anno", listOf(academicYear.value))
            put("attivita[]", subjects.map { it.code })
            put("visualizzazione_orario", listOf("cal"))
            put("periodo_didattico", listOf(""))
            put("date", listOf(formatDate(weekStartDate)))
            put("_lang", listOf(language.code))
            put("list", listOf(""))
            put("week_grid_type", listOf("-1"))
            put("ar_codes_", listOf(""))
            put("ar_select_", listOf(""))
            put("col_cells", listOf("0"))
            put("empty_box", listOf("1"))
            put("only_grid", listOf("0"))
            put("highlighted_date", listOf("0"))
            put("all_events", listOf("1"))
            put("faculty_group", listOf("0"))
        }

        val response = executePostForm<EasyStaffScheduleResponse>(SCHEDULE_ENDPOINT, params)
        return response.cells.sorted()
    }
}
