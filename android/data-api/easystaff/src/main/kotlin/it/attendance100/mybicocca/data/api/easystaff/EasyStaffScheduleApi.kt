package it.attendance100.mybicocca.data.api.easystaff

import io.ktor.client.*
import it.attendance100.mybicocca.data.api.cleanText
import it.attendance100.mybicocca.data.dto.easystaff.*
import kotlinx.serialization.json.*
import java.time.LocalDate
import java.time.LocalTime

/**
 * API for lesson schedule operations (Orario delle lezioni).
 *
 * Provides access to:
 * - Lesson schedules by study program, teacher, or subject
 * - Available teaching areas and study programs
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
        private const val SCHEDULE_VIEW = "easycourse"
        private const val COMBO_ENDPOINT = "/PortaleStudentiUnimib/combo.php"
    }

    /**
     * Gets the available search options for schedules.
     *
     * This includes available academic years and teaching areas.
     *
     * @return The available search options
     */
    suspend fun getSearchOptions(): EasyStaffScheduleSearchOptions {
        val academicYearsResponse = executeGetText(COMBO_ENDPOINT, mapOf(
            "sw" to "ec_",
            "aa" to "1"
        ))

        val academicYearsJson = extractJsonFromJsVariable(academicYearsResponse, "anni_accademici_ec")
            ?: throw IllegalStateException("Failed to parse academic years response")

        val academicYearsObject = json.parseToJsonElement(academicYearsJson).jsonObject
        val academicYears = academicYearsObject.values.mapNotNull { yearElement ->
            val year = yearElement.jsonObject
            val value = year["valore"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            EasyStaffAcademicYear(value.toIntOrNull() ?: return@mapNotNull null)
        }.sortedByDescending { it.startYear }

        val latestYear = academicYears.firstOrNull()
            ?: return EasyStaffScheduleSearchOptions(academicYears = emptyList(), teachingAreas = emptyList())

        val programsResponse = executeGetText(COMBO_ENDPOINT, mapOf(
            "sw" to "ec_",
            "aa" to latestYear.value,
            "page" to "corsi"
        ))

        val programsJson = extractJsonFromJsVariable(programsResponse, "elenco_corsi")
            ?: throw IllegalStateException("Failed to parse study programs response")

        val programsArray = json.parseToJsonElement(programsJson).jsonArray
        val teachingAreas = programsArray
            .mapNotNull { courseElement ->
                val course = courseElement.jsonObject
                val areaCode = course["scuola"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                val areaName = course["facolta_code"]?.jsonPrimitive?.contentOrNull ?: areaCode
                EasyStaffTeachingArea(code = areaCode, name = areaName)
            }
            .distinctBy { it.code }
            .sortedBy { it.name }

        return EasyStaffScheduleSearchOptions(
            academicYears = academicYears,
            teachingAreas = teachingAreas
        )
    }

    /**
     * Gets the study programs available for a teaching area in a given academic year.
     *
     * This loads detailed information including years of study and subjects.
     *
     * @param academicYear The academic year
     * @param teachingAreaCode The teaching area code (optional, returns all if null)
     * @return The available study programs with details
     */
    suspend fun getStudyPrograms(
        academicYear: EasyStaffAcademicYear,
        teachingAreaCode: String? = null
    ): EasyStaffStudyProgramsForArea {
        val response = executeGetText(COMBO_ENDPOINT, mapOf(
            "sw" to "ec_",
            "aa" to academicYear.value,
            "page" to "corsi"
        ))

        // Extract JSON from JavaScript variable assignment
        val jsonString = extractJsonFromJsVariable(response, "elenco_corsi")
            ?: throw IllegalStateException("Failed to parse study programs response")

        val coursesArray = json.parseToJsonElement(jsonString).jsonArray

        val studyPrograms = coursesArray.mapNotNull { courseElement ->
            val course = courseElement.jsonObject
            val areaCode = course["scuola"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null

            // Filter by teaching area if specified
            if (teachingAreaCode != null && areaCode != teachingAreaCode) {
                return@mapNotNull null
            }

            val code = course["valore"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val name = course["label"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val degreeTypeText = course["tipo"]?.jsonPrimitive?.contentOrNull ?: ""
            val internalId = course["cdl_id"]?.jsonPrimitive?.contentOrNull ?: ""
            val defaultDisplayMode = course["pub_type"]?.jsonPrimitive?.contentOrNull ?: "cal"

            // Parse teaching periods
            val periods = course["periodi"]?.jsonArray?.mapNotNull { periodElement ->
                val period = periodElement.jsonObject
                EasyStaffTeachingPeriod(
                    id = period["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
                    code = period["valore"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
                    label = period["label"]?.jsonPrimitive?.contentOrNull ?: ""
                )
            } ?: emptyList()

            // Parse years of study
            val years = course["elenco_anni"]?.jsonArray?.mapNotNull { yearElement ->
                val year = yearElement.jsonObject
                val yearValue = year["valore"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                val yearLabel = year["label"]?.jsonPrimitive?.contentOrNull ?: ""

                // Extract year number from value (e.g., "GGG|1" -> 1)
                val yearNumber = yearValue.split("|").lastOrNull()?.toIntOrNull() ?: 0
                val trackName = year["order_lbl"]?.jsonPrimitive?.contentOrNull ?: yearLabel

                // Parse subjects for this year
                val subjects = year["elenco_insegnamenti"]?.jsonArray?.mapNotNull { subjectElement ->
                    val subject = subjectElement.jsonObject
                    EasyStaffSubject(
                        id = subject["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
                        code = subject["valore"]?.jsonPrimitive?.contentOrNull ?: "",
                        name = subject["label"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
                        teacherName = subject["docente"]?.jsonPrimitive?.contentOrNull ?: "",
                        periodId = subject["id_periodo"]?.jsonPrimitive?.contentOrNull ?: ""
                    )
                } ?: emptyList()

                EasyStaffYearOfStudyDetails(
                    value = yearValue,
                    year = yearNumber,
                    label = yearLabel,
                    trackName = trackName,
                    subjects = subjects
                )
            } ?: emptyList()

            EasyStaffStudyProgramDetails(
                code = code,
                name = name,
                degreeType = EasyStaffDegreeType.fromItalian(degreeTypeText),
                internalId = internalId,
                teachingAreaCode = areaCode,
                years = years,
                teachingPeriods = periods,
                defaultDisplayMode = EasyStaffScheduleDisplayMode.fromValue(defaultDisplayMode)
            )
        }

        return EasyStaffStudyProgramsForArea(studyPrograms)
    }

    /**
     * Gets the list of all teachers for a given academic year.
     *
     * @param academicYear The academic year
     * @return List of teachers
     */
    suspend fun getTeachers(academicYear: EasyStaffAcademicYear): List<EasyStaffTeacher> {
        val response = executeGetText(COMBO_ENDPOINT, mapOf(
            "sw" to "ec_",
            "aa" to academicYear.value,
            "page" to "docenti"
        ))

        // Extract JSON from JavaScript variable assignment
        val jsonString = extractJsonFromJsVariable(response, "elenco_docenti")
            ?: throw IllegalStateException("Failed to parse teachers response")

        val teachersArray = json.parseToJsonElement(jsonString).jsonArray

        return teachersArray.mapNotNull { teacherElement ->
            val teacher = teacherElement.jsonObject
            EasyStaffTeacher(
                id = teacher["valore"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
                name = teacher["label"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            )
        }
    }

    /**
     * Gets the weekly schedule for a study program.
     *
     * @param query The search parameters
     * @param language The language for labels
     * @return The weekly schedule
     */
    suspend fun getScheduleByProgram(
        query: EasyStaffScheduleByProgramQuery,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): EasyStaffWeeklySchedule {
        val params = buildMap {
            put("view", SCHEDULE_VIEW)
            put("form-type", EasyStaffScheduleSearchMode.BY_STUDY_PROGRAM.formType)
            put("include", EasyStaffScheduleSearchMode.BY_STUDY_PROGRAM.includeValue)
            put("anno", query.academicYear.value)
            put("scuola", query.teachingAreaCode)
            put("corso", query.studyProgramCode)
            put("date", formatDate(query.weekStartDate))
            put("visualizzazione_orario", query.displayMode.value)
            put("_lang", language.code)
            query.teachingPeriodCode?.let { put("periodo_didattico", it) }
        }

        // Add years of study (multiple values)
        val fullParams = params.toMutableMap()
        query.yearsOfStudy.forEachIndexed { index, year ->
            fullParams["anno2[$index]"] = year
        }

        val doc = executeGet(AGENDA_WEB_API, fullParams)
        return parseWeeklySchedule(doc, query.weekStartDate)
    }

    /**
     * Gets the weekly schedule for a teacher.
     *
     * @param query The search parameters
     * @param language The language for labels
     * @return The weekly schedule
     */
    suspend fun getScheduleByTeacher(
        query: EasyStaffScheduleByTeacherQuery,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): EasyStaffWeeklySchedule {
        val doc = executeGet(AGENDA_WEB_API, mapOf(
            "view" to SCHEDULE_VIEW,
            "form-type" to EasyStaffScheduleSearchMode.BY_TEACHER.formType,
            "include" to EasyStaffScheduleSearchMode.BY_TEACHER.includeValue,
            "anno" to query.academicYear.value,
            "docente" to query.teacherId,
            "date" to formatDate(query.weekStartDate),
            "_lang" to language.code
        ))

        return parseWeeklySchedule(doc, query.weekStartDate)
    }

    /**
     * Gets the weekly schedule for a subject.
     *
     * @param query The search parameters
     * @param language The language for labels
     * @return The weekly schedule
     */
    suspend fun getScheduleBySubject(
        query: EasyStaffScheduleBySubjectQuery,
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): EasyStaffWeeklySchedule {
        val doc = executeGet(AGENDA_WEB_API, mapOf(
            "view" to SCHEDULE_VIEW,
            "form-type" to EasyStaffScheduleSearchMode.BY_SUBJECT.formType,
            "include" to EasyStaffScheduleSearchMode.BY_SUBJECT.includeValue,
            "anno" to query.academicYear.value,
            "scuola" to query.teachingAreaCode,
            "attession" to query.subjectId,
            "date" to formatDate(query.weekStartDate),
            "_lang" to language.code
        ))

        return parseWeeklySchedule(doc, query.weekStartDate)
    }

    /**
     * Parses a weekly schedule from the HTML document.
     */
    private fun parseWeeklySchedule(
        doc: org.jsoup.nodes.Document,
        requestedDate: LocalDate
    ): EasyStaffWeeklySchedule {
        val lessons = mutableListOf<EasyStaffScheduledLesson>()

        // Parse lessons from the calendar grid
        // Each lesson is in a cell with class "cella-lezione" or similar
        val eventCells = doc.select(".cella-codice, .cella-codice-small, .event-item, [data-codice]")

        for (cell in eventCells) {
            // Try to extract lesson information
            val title = cell.selectFirst(".nome-insegnamento, .codice_insegnamento, .titolo")
                ?.cleanText()
                ?: cell.attr("data-nome").ifBlank { null }
                ?: continue

            // Get date from parent cell or data attribute
            val dateStr = cell.attr("data-date").ifBlank {
                cell.parents().firstOrNull { it.hasAttr("data-date") }?.attr("data-date")
            } ?: continue

            val date = parseDate(dateStr) ?: continue

            // Get time range
            val timeText = cell.selectFirst(".orario, .time")?.cleanText()
                ?: cell.attr("data-orario").ifBlank { null }

            val (startTime, endTime) = parseTimeRange(timeText)

            // Get room and building
            val roomText = cell.selectFirst(".aula, .room")?.cleanText()
                ?: cell.attr("data-aula").ifBlank { null }
            val (room, building) = parseRoomAndBuilding(roomText)

            // Get teacher(s)
            val teacherText = cell.selectFirst(".docente, .docenti, .teacher")?.cleanText()
                ?: cell.attr("data-docente").ifBlank { null }
            val teachers = teacherText?.split(",", ";")?.map { it.trim() }?.filter { it.isNotBlank() }
                ?: emptyList()

            // Get event type
            val eventTypeText = cell.selectFirst(".tipo, .type")?.cleanText()
                ?: cell.attr("data-tipo").ifBlank { "Lezione" }
            val eventType = EasyStaffLessonEventType.fromItalian(eventTypeText)

            // Get subject code
            val subjectCode = cell.attr("data-codice").ifBlank {
                cell.selectFirst(".codice")?.cleanText()
            }

            lessons.add(EasyStaffScheduledLesson(
                title = title,
                date = date,
                startTime = startTime ?: LocalTime.of(8, 0),
                endTime = endTime ?: LocalTime.of(9, 0),
                room = room,
                building = building,
                teachers = teachers,
                eventType = eventType,
                notes = null,
                subjectCode = subjectCode
            ))
        }

        // Calculate week bounds
        val weekStart = requestedDate.with(java.time.DayOfWeek.MONDAY)
        val weekEnd = weekStart.plusDays(6)

        return EasyStaffWeeklySchedule(
            weekStartDate = weekStart,
            weekEndDate = weekEnd,
            studyProgram = null, // Would need to parse from page context
            yearOfStudy = null,
            lessons = lessons
        )
    }
}
