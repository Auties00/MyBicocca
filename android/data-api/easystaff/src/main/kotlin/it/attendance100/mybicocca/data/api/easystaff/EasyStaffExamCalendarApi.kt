package it.attendance100.mybicocca.data.api.easystaff

import io.ktor.client.*
import it.attendance100.mybicocca.data.dto.easystaff.*
import kotlinx.serialization.json.Json
import org.jsoup.nodes.Document
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
        private const val EXAM_VIEW = "easytest"
    }

    /**
     * Gets exams for a study program within a date range.
     *
     * @param query The search parameters
     * @param language The language for labels
     * @return The exam search results
     */
    suspend fun getExamsByProgram(
        query: EasyStaffExamsByProgramQuery,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): EasyStaffExamSearchResults {
        val params = buildMap {
            put("view", EXAM_VIEW)
            put("form-type", EasyStaffExamSearchMode.BY_STUDY_PROGRAM.formType)
            put("include", EasyStaffExamSearchMode.BY_STUDY_PROGRAM.includeValue)
            put("anno", query.academicYear.value)
            put("scuola", query.teachingAreaCode)
            put("corso", query.studyProgramCode)
            put("date", formatDate(query.startDate))
            put("date2", formatDate(query.endDate))
            put("_lang", language.code)
        }

        // Add years of study (multiple values)
        val fullParams = params.toMutableMap()
        query.yearsOfStudy.forEachIndexed { index, year ->
            fullParams["anno2[$index]"] = year
        }

        val doc = executeGet(AGENDA_WEB_API, fullParams)
        return parseExamResults(doc)
    }

    /**
     * Gets exams for a teacher within a date range.
     *
     * @param query The search parameters
     * @param language The language for labels
     * @return The exam search results
     */
    suspend fun getExamsByTeacher(
        query: EasyStaffExamsByTeacherQuery,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): EasyStaffExamSearchResults {
        val doc = executeGet(AGENDA_WEB_API, mapOf(
            "view" to EXAM_VIEW,
            "form-type" to EasyStaffExamSearchMode.BY_TEACHER.formType,
            "include" to EasyStaffExamSearchMode.BY_TEACHER.includeValue,
            "anno" to query.academicYear.value,
            "docente" to query.teacherId,
            "date" to formatDate(query.startDate),
            "date2" to formatDate(query.endDate),
            "_lang" to language.code
        ))

        return parseExamResults(doc)
    }

    /**
     * Gets exams for a subject within a date range.
     *
     * @param query The search parameters
     * @param language The language for labels
     * @return The exam search results
     */
    suspend fun getExamsBySubject(
        query: EasyStaffExamsBySubjectQuery,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): EasyStaffExamSearchResults {
        val doc = executeGet(AGENDA_WEB_API, mapOf(
            "view" to EXAM_VIEW,
            "form-type" to EasyStaffExamSearchMode.BY_SUBJECT.formType,
            "include" to EasyStaffExamSearchMode.BY_SUBJECT.includeValue,
            "anno" to query.academicYear.value,
            "scuola" to query.teachingAreaCode,
            "attession" to query.subjectId,
            "date" to formatDate(query.startDate),
            "date2" to formatDate(query.endDate),
            "_lang" to language.code
        ))

        return parseExamResults(doc)
    }

    /**
     * Gets exams for a study program using default date range (next 3 months).
     *
     * @param academicYear The academic year
     * @param teachingAreaCode The teaching area code
     * @param studyProgramCode The study program code
     * @param yearsOfStudy The years of study to include
     * @param language The language for labels
     * @return The exam search results
     */
    suspend fun getUpcomingExamsForProgram(
        academicYear: EasyStaffAcademicYear,
        teachingAreaCode: String,
        studyProgramCode: String,
        yearsOfStudy: List<String>,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): EasyStaffExamSearchResults {
        val today = LocalDate.now()
        val query = EasyStaffExamsByProgramQuery(
            academicYear = academicYear,
            teachingAreaCode = teachingAreaCode,
            studyProgramCode = studyProgramCode,
            yearsOfStudy = yearsOfStudy,
            startDate = today,
            endDate = today.plusMonths(3)
        )
        return getExamsByProgram(query, language)
    }

    /**
     * Parses exam results from the HTML document.
     */
    private fun parseExamResults(doc: Document): EasyStaffExamSearchResults {
        val exams = mutableListOf<EasyStaffScheduledExam>()

        // Get search summary if available
        val searchSummary = doc.selectFirst(".dati-ricerca, .search-summary, #search-info")
            ?.cleanText()
            ?: ""

        // Parse exams from the table
        val table = doc.selectFirst("table.table-bordered, table.detail_table, #tableEsami")
        if (table != null) {
            val headers = table.select("thead th, thead td")
                .map { it.cleanText().lowercase() }

            val rows = table.select("tbody tr")

            for (row in rows) {
                val cells = row.select("td")
                if (cells.isEmpty()) continue

                // Map cells to headers
                val cellMap = headers.zip(cells).toMap()

                // Extract subject name
                val subjectName = findCellValue(cellMap, "descrizione", "insegnamento", "materia", "corso")
                    ?: cells.firstOrNull()?.cleanText()
                    ?: continue

                // Extract date
                val dateText = findCellValue(cellMap, "data", "date")
                val date = dateText?.let { parseDate(it) } ?: continue

                // Extract time
                val timeText = findCellValue(cellMap, "ora", "orario", "time", "orari")
                val (startTime, endTime) = parseTimeRange(timeText)

                // Extract room and building
                val roomText = findCellValue(cellMap, "aula", "room", "sede")
                val (room, building) = parseRoomAndBuilding(roomText)

                // Extract teachers
                val teacherText = findCellValue(cellMap, "docente", "docenti", "teacher", "professore")
                val teachers = teacherText?.split(",", ";", "/")
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    ?: emptyList()

                // Extract notes/info
                val notes = findCellValue(cellMap, "note", "info", "dettagli", "descrizione aggiuntiva")

                // Extract exam type
                val examTypeText = findCellValue(cellMap, "tipo", "type", "modalità")
                val examType = examTypeText?.let { EasyStaffExamType.fromItalian(it) }

                // Extract subject code from link or cell
                val subjectCode = row.selectFirst("a[href*=codice]")?.attr("href")
                    ?.let { """codice=([^&]+)""".toRegex().find(it)?.groupValues?.get(1) }

                exams.add(EasyStaffScheduledExam(
                    subjectName = subjectName,
                    subjectCode = subjectCode,
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    room = room,
                    building = building,
                    teachers = teachers,
                    notes = notes,
                    examType = examType
                ))
            }
        }

        // Also try parsing from list/card format
        val examCards = doc.select(".esame-item, .exam-card, .lista-esami > div")
        for (card in examCards) {
            val subjectName = card.selectFirst(".nome-esame, .exam-title, h4, h5")?.cleanText()
                ?: continue

            val dateText = card.selectFirst(".data-esame, .exam-date, [data-date]")?.cleanText()
                ?: card.attr("data-date").ifBlank { null }
            val date = dateText?.let { parseDate(it) } ?: continue

            val timeText = card.selectFirst(".orario-esame, .exam-time")?.cleanText()
            val (startTime, endTime) = parseTimeRange(timeText)

            val roomText = card.selectFirst(".aula-esame, .exam-room")?.cleanText()
            val (room, building) = parseRoomAndBuilding(roomText)

            val teacherText = card.selectFirst(".docente-esame, .exam-teacher")?.cleanText()
            val teachers = teacherText?.split(",", ";")?.map { it.trim() }?.filter { it.isNotBlank() }
                ?: emptyList()

            exams.add(EasyStaffScheduledExam(
                subjectName = subjectName,
                subjectCode = null,
                date = date,
                startTime = startTime,
                endTime = endTime,
                room = room,
                building = building,
                teachers = teachers,
                notes = null,
                examType = null
            ))
        }

        return EasyStaffExamSearchResults(
            exams = exams.sortedWith(compareBy({ it.date }, { it.startTime })),
            searchSummary = searchSummary
        )
    }
}
