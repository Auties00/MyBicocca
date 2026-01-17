package it.attendance100.mybicocca.data.api.easystaff

import io.ktor.client.*
import it.attendance100.mybicocca.data.dto.easystaff.*
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
     * @param teachingPeriod Optional teaching period filter
     * @param language The language for labels
     * @return The weekly schedule as a list of schedule cells
     */
    suspend fun getScheduleByProgram(
        academicYear: EasyStaffAcademicYear,
        studyProgram: EasyStaffStudyProgramDetails,
        yearsOfStudy: List<EasyStaffYearOfStudyDetails>,
        weekStartDate: LocalDate,
        teachingPeriod: EasyStaffTeachingPeriod? = null,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): List<EasyStaffScheduleCell> {
        val params = buildMap {
            put("view", listOf("easycourse"))
            put("form-type", listOf("corso"))
            put("include", listOf("corso"))
            put("anno", listOf(academicYear.value))
            put("scuola", listOf(studyProgram.teachingAreaCode))
            put("corso", listOf(studyProgram.code))
            put("date", listOf(formatDate(weekStartDate)))
            put("visualizzazione_orario", listOf("cal"))
            put("_lang", listOf(language.code))
            put("anno2[]", yearsOfStudy.map { it.value })
            teachingPeriod?.let { put("periodo_didattico", listOf(it.code)) }
            put("list", listOf(""))
            put("week_grid_type", listOf("-1"))
            put("ar_codes_", listOf(""))
            put("ar_select_", listOf(""))
            put("col_cells", listOf("0"))
            put("empty_box", listOf("0"))
            put("only_grid", listOf("0"))
            put("highlighted_date", listOf("0"))
            put("all_events", listOf("0"))
        }

        val response = executePostForm<EasyStaffScheduleResponse>(SCHEDULE_ENDPOINT, params)
        return response.cells.sortedWith(compareBy { it.dateTime })
    }

    /**
     * Gets the weekly schedule for a teacher.
     *
     * @param academicYear The academic year
     * @param teacher The teacher
     * @param weekStartDate The starting date for the schedule
     * @param language The language for labels
     * @return The weekly schedule as a list of schedule cells
     */
    suspend fun getScheduleByTeacher(
        academicYear: EasyStaffAcademicYear,
        teacher: EasyStaffTeacher,
        weekStartDate: LocalDate,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): List<EasyStaffScheduleCell> {
        val params = buildMap {
            put("view", listOf("easycourse"))
            put("form-type", listOf("docente"))
            put("include", listOf("docente"))
            put("anno", listOf(academicYear.value))
            put("docente", listOf(teacher.id.toString()))
            put("date", listOf(formatDate(weekStartDate)))
            put("_lang", listOf(language.code))
            put("list", listOf(""))
            put("week_grid_type", listOf("-1"))
            put("ar_codes_", listOf(""))
            put("ar_select_", listOf(""))
            put("col_cells", listOf("0"))
            put("empty_box", listOf("0"))
            put("only_grid", listOf("0"))
            put("highlighted_date", listOf("0"))
            put("all_events", listOf("0"))
        }

        val response = executePostForm<EasyStaffScheduleResponse>(SCHEDULE_ENDPOINT, params)
        return response.cells.sortedWith(compareBy { it.dateTime })
    }

    /**
     * Gets the weekly schedule for a subject.
     *
     * @param academicYear The academic year
     * @param teachingArea The teaching area
     * @param subject The subject
     * @param weekStartDate The starting date for the schedule
     * @param language The language for labels
     * @return The weekly schedule as a list of schedule cells
     */
    suspend fun getScheduleBySubject(
        academicYear: EasyStaffAcademicYear,
        teachingArea: EasyStaffTeachingArea,
        subject: EasyStaffSubject,
        weekStartDate: LocalDate,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): List<EasyStaffScheduleCell> {
        val params = buildMap {
            put("view", listOf("easycourse"))
            put("form-type", listOf("attivita"))
            put("include", listOf("attivita"))
            put("anno", listOf(academicYear.value))
            put("scuola", listOf(teachingArea.code))
            put("attession", listOf(subject.id))
            put("date", listOf(formatDate(weekStartDate)))
            put("_lang", listOf(language.code))
            put("list", listOf(""))
            put("week_grid_type", listOf("-1"))
            put("ar_codes_", listOf(""))
            put("ar_select_", listOf(""))
            put("col_cells", listOf("0"))
            put("empty_box", listOf("0"))
            put("only_grid", listOf("0"))
            put("highlighted_date", listOf("0"))
            put("all_events", listOf("0"))
        }

        val response = executePostForm<EasyStaffScheduleResponse>(SCHEDULE_ENDPOINT, params)
        return response.cells.sortedWith(compareBy { it.dateTime })
    }
}
