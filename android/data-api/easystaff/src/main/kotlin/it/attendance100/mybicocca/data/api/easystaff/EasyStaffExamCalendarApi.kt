package it.attendance100.mybicocca.data.api.easystaff

import io.ktor.client.*
import it.attendance100.mybicocca.data.common.exception.HtmlParsingException
import it.attendance100.mybicocca.data.dto.easystaff.*
import kotlinx.serialization.json.Json
import java.time.LocalDate

/**
 * API for exam calendar operations (Calendario esami).
 *
 * Provides access to:
 * - Exam schedules by study program, teacher, or subject
 * - Available exam sessions within a date range
 * - Exam details including room, teachers, and type
 *
 * The exam calendar can be searched in three modes:
 * - By study program (corso di studio): Shows all exams for a degree program
 * - By teacher (docente): Shows all exams for a specific teacher
 * - By subject (insegnamento): Shows exams for a specific course/subject
 */
class EasyStaffExamCalendarApi(
    client: HttpClient,
    json: Json
) : EasyStaffAbstractApi(client, json) {
    companion object {
        private const val EXAM_CALENDAR_ENDPOINT = "/PortaleStudentiUnimib/test_call.php"
    }

    /**
     * Gets exams for a study program within a date range.
     *
     * @param studyProgram The study program
     * @param yearsOfStudy The years of study to include
     * @param startDate The start date for the search range
     * @param endDate The end date for the search range
     * @param language The language for labels
     * @return The exam search results
     */
    suspend fun getExamsByProgram(
        studyProgram: EasyStaffStudyProgram,
        yearsOfStudy: List<EasyStaffStudyProgramYear>,
        startDate: LocalDate,
        endDate: LocalDate,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): List<EasyStaffScheduledExam> {
        val params = buildMap {
            put("view", listOf("easytest"))
            put("form-type", listOf("et_cdl"))
            put("include", listOf("et_cdl"))
            put("et_er", listOf("1"))
            put("scuola", listOf(studyProgram.teachingAreaCode))
            put("esami_cdl", listOf(studyProgram.code))
            put("anno2[]", yearsOfStudy.map { it.year.toString() })
            put("datefrom", listOf(formatDate(startDate)))
            put("dateto", listOf(formatDate(endDate)))
            put("_lang", listOf(language.code))
            put("list", listOf())
            put("week_grid_type", listOf("-1"))
            put("ar_codes_", listOf())
            put("ar_select_", listOf())
            put("col_cells", listOf("0"))
            put("empty_box", listOf("1"))
            put("only_grid", listOf("0"))
            put("highlighted_date", listOf("0"))
            put("all_events", listOf("0"))
        }
        val response = executePostForm<EasyStaffExamCalendarResponse>(EXAM_CALENDAR_ENDPOINT, params)
        return response.subjects.flatMap { it.exams }.sortedWith(compareBy({ it.date }, { it.startTime }))
    }

    /**
     * Gets exams for a teacher within a date range.
     *
     * @param teacher The teacher
     * @param startDate The start date for the search range
     * @param endDate The end date for the search range
     * @param language The language for labels
     * @return The exam search results
     */
    suspend fun getExamsByTeacher(
        teacher: EasyStaffTeacher,
        startDate: LocalDate,
        endDate: LocalDate,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): List<EasyStaffScheduledExam> {
        val params = buildMap {
            put("view", listOf("easytest"))
            put("form-type", listOf("et_docente"))
            put("include", listOf("et_docente"))
            put("et_er", listOf("1"))
            put("esami_docente", listOf(teacher.id.toString()))
            put("datefrom", listOf(formatDate(startDate)))
            put("dateto", listOf(formatDate(endDate)))
            put("_lang", listOf(language.code))
            put("list", listOf())
            put("week_grid_type", listOf("-1"))
            put("ar_codes_", listOf())
            put("ar_select_", listOf())
            put("col_cells", listOf("0"))
            put("empty_box", listOf("1"))
            put("only_grid", listOf("0"))
            put("highlighted_date", listOf("0"))
            put("all_events", listOf("0"))
        }
        val response = executePostForm<EasyStaffExamCalendarResponse>(EXAM_CALENDAR_ENDPOINT, params)
        return response.subjects.flatMap { it.exams }.sortedWith(compareBy({ it.date }, { it.startTime }))
    }

    /**
     * Gets exams for a subject within a date range.
     *
     * @param subject The subject
     * @param startDate The start date for the search range
     * @param endDate The end date for the search range
     * @param language The language for labels
     * @return The exam search results
     */
    suspend fun getExamsBySubject(
        subject: EasyStaffStudyProgramSubject,
        startDate: LocalDate,
        endDate: LocalDate,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): List<EasyStaffScheduledExam> {
        val now = System.currentTimeMillis()
        val subjectsResponse = executeGetText(
            COMBO_ENDPOINT,
            mapOf(
                "sw" to listOf("et_"),
                "page" to listOf("insegnamento"),
                "_" to listOf(System.currentTimeMillis().toString())
            )
        )
        val subjectsJsonString = extractJsonFromJsVariable(subjectsResponse, "esami_insegnamento")
            ?: throw HtmlParsingException("Failed to parse subjects response: missing 'esami_insegnamento'")
        val bookableExamSubject = json.decodeFromString<List<EasyStaffBookableExamSubject>>(subjectsJsonString)
            .firstOrNull { subject.code.endsWith(it.code) }
            ?: return emptyList()
        val params = buildMap {
            put("view", listOf("easytest"))
            put("form-type", listOf("et_insegnamento"))
            put("include", listOf("et_insegnamento"))
            put("et_er", listOf("1"))
            put("text_ins", listOf(bookableExamSubject.name))
            put("esami_insegnamento", listOf(bookableExamSubject.id))
            put("datefrom", listOf(formatDate(startDate)))
            put("dateto", listOf(formatDate(endDate)))
            put("_lang", listOf(language.code))
            put("list", listOf())
            put("week_grid_type", listOf("-1"))
            put("ar_codes_", listOf())
            put("ar_select_", listOf())
            put("col_cells", listOf("0"))
            put("empty_box", listOf("1"))
            put("only_grid", listOf("0"))
            put("highlighted_date", listOf("0"))
            put("all_events", listOf("0"))
        }
        val response = executePostForm<EasyStaffExamCalendarResponse>(EXAM_CALENDAR_ENDPOINT, params)
        return response.subjects.flatMap { it.exams }.sortedWith(compareBy({ it.date }, { it.startTime }))
    }
}
