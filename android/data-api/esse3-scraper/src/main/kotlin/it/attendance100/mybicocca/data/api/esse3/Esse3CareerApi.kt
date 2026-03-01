package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import it.attendance100.mybicocca.data.common.exception.HtmlParsingException
import it.attendance100.mybicocca.data.common.util.cleanText
import it.attendance100.mybicocca.data.common.util.parseGrid
import it.attendance100.mybicocca.data.common.util.parseTable
import it.attendance100.mybicocca.data.dto.esse3.*
import java.time.LocalDate

/**
 * API for academic career operations.
 *
 * Provides access to:
 * - Academic record (libretto)
 * - Study plan
 * - Career acts
 * - Course evaluation questionnaires
 */
class Esse3CareerApi(
    client: HttpClient
) : Esse3AbstractApi(client) {
    companion object {
        private const val LIBRETTO_ENTRYPOINT = "/auth/studente/Libretto/LibrettoHome.do?menu_opened_cod=menu_link-navbox_studenti_Carriera"
        private const val STUDY_PLAN_ENTRYPOINT = "/auth/studente/Piani/PianiHome.do?menu_opened_cod=menu_link-navbox_studenti_Carriera"

        private val UNWEIGHTED_GPA_REGEX = "Media Aritmetica degli esami.*?(\\d+\\.\\d+)".toRegex(RegexOption.IGNORE_CASE)
        private val WEIGHTED_GPA_REGEX = "Media Ponderata degli esami.*?(\\d+\\.\\d+)".toRegex(RegexOption.IGNORE_CASE)
        private val TEACHING_ACTIVITY_REGEX = "Attività Didattica: (.+?) \\[([^]]+)]".toRegex(RegexOption.IGNORE_CASE)
        private val COURSE_AND_DEGREE_REGEX = """Offerta nel corso di studio:\s*(.+?)\s*\[(.+?)]\s*.*?Classe di Laurea:\s*(.+?)\s*-\s*(.+)""".toRegex(RegexOption.IGNORE_CASE)
        private val PASSED_STATUS_REGEX = "Superata nell' AA (\\d{4}/\\d{4}) \\(con frequenza nell' AA (\\d{4}/\\d{4})\\)".toRegex(RegexOption.IGNORE_CASE)
        private val ATTENDED_STATUS_REGEX = "Frequentata \\(nell' AA (\\d{4}/\\d{4})\\)".toRegex(RegexOption.IGNORE_CASE)
        private val YEAR_OF_COURSE_REGEX = "Anno di Corso (\\d+)".toRegex(RegexOption.IGNORE_CASE)
    }

    /**
     * Gets the study plan.
     *
     * @return The study plan
     */
    suspend fun getStudyPlan(): Esse3StudyPlan {
        val doc = executeGet(STUDY_PLAN_ENTRYPOINT)

        val metadataMap = doc.select("th.tplMaster").associate { th ->
            val key = th.text().cleanText().removeSuffix(":").lowercase()
            val value = th.nextElementSibling()?.text()?.cleanText() ?: ""
            key to value
        }

        val statusText = metadataMap["stato"]
            ?: throw HtmlParsingException("Cannot get study plan: missing status")
        val status = Esse3PlanStatus.fromString(statusText)
            ?: throw HtmlParsingException("Cannot get study plan: invalid status '$statusText'")

        val type = metadataMap["tipo piano"]
            ?: throw HtmlParsingException("Cannot get study plan: missing type")

        val lastModifiedText = metadataMap["data ultima variazione"]
            ?: throw HtmlParsingException("Cannot get study plan: missing last modified date")
        val lastModified = parseDate(lastModifiedText)
            ?: throw HtmlParsingException("Cannot get study plan: invalid date '$lastModifiedText'")

        val offerYearText = metadataMap["anno di offerta"]
            ?: throw HtmlParsingException("Cannot get study plan: missing offer year")
        val offerYear = offerYearText.toIntOrNull()
            ?: throw HtmlParsingException("Cannot get study plan: invalid offer year '$offerYearText'")

        val regulationYearText = metadataMap["anno del regolamento"]
            ?: throw HtmlParsingException("Cannot get study plan: missing regulation year")
        val regulationYear = regulationYearText.toIntOrNull()
            ?: throw HtmlParsingException("Cannot get study plan: invalid regulation year '$regulationYearText'")

        val yearTitles = doc.select("td.tplTitolo")
        val tables = doc.select("table.detail_table")

        val courses = yearTitles.zip(tables).flatMap { (titleTd, table) ->
            val year = YEAR_OF_COURSE_REGEX.find(titleTd.text())
                ?.groupValues?.get(1)?.toIntOrNull() ?: 0

            // Build headers list, expanding for colspan
            val headers = table.select("tbody tr th").map {
                val node = it.firstChild() ?: it
                node.nodeValue().trim().lowercase()
            }

            table.select("tbody tr").asSequence().drop(1).map { row ->
                val rows = row.select("td").filter {
                    it.text().isNotBlank()
                }

                val rowMap = headers.zip(rows).toMap()

                val code = rowMap["codice"]?.text()?.cleanText()
                    ?: throw HtmlParsingException("Cannot get study plan: missing code")

                val description = rowMap["descrizione"]?.text()?.cleanText()
                    ?: throw HtmlParsingException("Cannot get study plan: missing description")

                val universityCell = rowMap["ateneo"]
                    ?: throw HtmlParsingException("Cannot get study plan: missing university")
                val university = universityCell.selectFirst("span")?.attr("title")
                    ?.takeIf { it.isNotBlank() }
                    ?: universityCell.text().cleanText()

                Esse3PlannedCourse(
                    code = code,
                    description = description,
                    year = year,
                    university = university
                )
            }
        }

        return Esse3StudyPlan(
            status = status,
            type = type,
            lastModified = lastModified,
            offerYear = offerYear,
            regulationYear = regulationYear,
            courses = courses
        )
    }

    /**
     * Prints the study plan as PDF.
     *
     * @return The PDF bytes
     */
    suspend fun printStudyPlan(): ByteReadChannel {
        val doc = executeGet(STUDY_PLAN_ENTRYPOINT)

        val printForm = doc.selectFirst("form[action*='PianiStampaPiano.do']")
            ?: throw HtmlParsingException("Cannot find print study plan form")

        val actionUrl = printForm.attr("action")

        val response = executePostRaw(
            actionUrl,
            Parameters.build {
                append("btnSubmit", "Stampa Piano")
            }
        )
        return response.bodyAsChannel()
    }

    /**
     * Gets the academic record (libretto) with all courses and grades.
     *
     * @return The academic record
     */
    suspend fun getAcademicRecord(): Esse3AcademicRecord {
        val doc = executeGet(LIBRETTO_ENTRYPOINT)

        val gpaBox = doc.selectFirst("#boxMedie")?.text()?.cleanText()
            ?: throw HtmlParsingException("Cannot get academic record: missing GPA box")
        val unweightedGpa = UNWEIGHTED_GPA_REGEX.find(gpaBox)?.groupValues?.get(1)?.toDoubleOrNull()
            ?: throw HtmlParsingException("Cannot get academic record: missing unweighted GPA")
        val weightedGpa = WEIGHTED_GPA_REGEX.find(gpaBox)?.groupValues?.get(1)?.toDoubleOrNull()
            ?: throw HtmlParsingException("Cannot get academic record: missing weighted GPA")

        val table = doc.selectFirst("#tableLibretto")
            ?: throw HtmlParsingException("Cannot get academic record: missing table")

        val courses = table.parseTable().map { row ->
            val teachingActivityElement = row.getElementOrThrow("attività didattiche")
            val urlPath = teachingActivityElement.selectFirst("a")?.attr("href")
                ?: throw HtmlParsingException("Cannot get academic record: missing teaching activity url")
            val teachingActivityParts = teachingActivityElement.cleanText().split(" - ", limit = 2)
            val code = teachingActivityParts.elementAtOrNull(0)
                ?: throw HtmlParsingException("Cannot get academic record: missing code")
            val name = teachingActivityParts.elementAtOrNull(1)
                ?: throw HtmlParsingException("Cannot get academic record: missing name")

            val year = row.getTextAsOrThrow("anno") { it.toIntOrNull() }

            val credits = row.getTextAsOrThrow("peso in crediti") { it.toIntOrNull() }

            val academicYear = row.getTextOrThrow("aa freq.")

            val (grade, date) = parseGradeAndDate(row.getTextOrNull("voto - data esame"))

            val attemptsUrl = row.getTextOrNull("prove")
                ?.takeIf { it.isNotEmpty() }

            Esse3Course(
                code = code,
                name = name,
                urlPath = urlPath,
                year = year,
                credits = credits,
                academicYear = academicYear,
                grade = grade,
                examDate = date,
                examAttemptsUrlPath = attemptsUrl
            )
        }.toList()

        return Esse3AcademicRecord(
            courses = courses,
            unweightedGpa = unweightedGpa,
            weightedGpa = weightedGpa,
        )
    }

    private fun parseGradeAndDate(gradeAndDate: String?): Pair<Esse3Grade?, LocalDate?> {
        if(gradeAndDate.isNullOrEmpty()) {
            return null to null
        } else {
            val gradeAndDateParts = gradeAndDate.split(" - ", limit = 2)
            val gradeText = gradeAndDateParts.elementAtOrNull(0)
                ?: throw HtmlParsingException("Cannot get academic record: missing grade")
            val grade = Esse3Grade.parse(gradeText)
                ?: throw HtmlParsingException("Cannot get academic record: invalid grade '$gradeText'")
            val dateText = gradeAndDateParts.elementAtOrNull(1)
                ?: throw HtmlParsingException("Cannot get academic record: missing date")
            val date = parseDate(dateText)
                ?: throw HtmlParsingException("Cannot get academic record: invalid date '$dateText'")
            return grade to date
        }
    }

    /**
     * Gets detailed information about a course.
     *
     * @param course The course to get details for
     * @return The course details including didactic units
     */
    suspend fun getCourseInfo(course: Esse3Course): Esse3CourseDetails {
        val doc = executeGet(course.urlPath)

        val header = doc.selectFirst("#desAdCds")
            ?: throw HtmlParsingException("Cannot get course info: missing header")
        val titleText = header.selectFirst("h2")?.text()?.cleanText() ?: ""
        val (name, code) = TEACHING_ACTIVITY_REGEX.find(titleText)?.destructured?.let { (n, c) -> n to c }
            ?: throw HtmlParsingException("Cannot match teaching activity in: $titleText")

        val courseAndDegreeText = header.selectFirst("p")?.text()?.cleanText() ?: ""
        val (courseName, courseCode, degreeCode, degreeDesc) = COURSE_AND_DEGREE_REGEX.find(courseAndDegreeText)?.destructured
            ?: throw HtmlParsingException("Cannot match course and degree: $courseAndDegreeText")

        val detailsData = doc.selectFirst("#boxDatiAd")?.parseGrid()
            ?: throw HtmlParsingException("Cannot get course info: missing details box")

        val year = detailsData.getTextAsOrNull("anno di corso") { it.toIntOrNull() }
            ?: throw HtmlParsingException("Cannot get course info: missing or invalid year")

        val statusText = detailsData.getTextOrNull("stato") ?: ""
        val status = PASSED_STATUS_REGEX.find(statusText)?.let {
            Esse3CourseStatus.Passed(it.groupValues[1], it.groupValues[2])
        } ?: ATTENDED_STATUS_REGEX.find(statusText)?.let {
            Esse3CourseStatus.Attended(it.groupValues[1])
        } ?: Esse3CourseStatus.NotAttended

        val examDate = detailsData.getTextAsOrNull("data esame") { parseDate(it) }
        val grade = detailsData.getTextAsOrNull("voto / giudizio") { Esse3Grade.parse(it) }
        val notes = detailsData.getTextOrNull("note") ?: ""

        val table = doc.selectFirst("#tableUD")
            ?: throw HtmlParsingException("Cannot get course info: missing didactic units table")

        val units = table.parseTable().map { row ->
            val unitName = row.getTextOrThrow("unità dididattica")
            val activityType = row.getTextOrNull("tipo attività") ?: ""
            val formationType = row.getTextOrNull("tipo formaz.") ?: ""
            val sector = row.getTextOrNull("settore") ?: ""
            val credits = row.getTextAsOrThrow("cfu") { it.toIntOrNull() }
            val duration = row.getTextAsOrNull("durata") { it.toIntOrNull() }

            Esse3CourseUnit(
                name = unitName,
                activityType = activityType,
                formationType = formationType,
                sector = sector,
                credits = credits,
                duration = duration
            )
        }.toList()

        return Esse3CourseDetails(
            code = code,
            name = name,
            courseCode = courseCode,
            courseName = courseName,
            degreeCode = degreeCode,
            degreeDescription = degreeDesc,
            year = year,
            status = status,
            examDate = examDate,
            grade = grade,
            notes = notes,
            units = units
        )
    }

    /**
     * Gets all exam attempts for a course.
     *
     * @param course The course to get exam attempts for
     * @return The list of exam attempts
     */
    suspend fun getCourseExamAttempts(course: Esse3Course): List<Esse3ExamAttempt> {
        val attemptsUrlPath = course.examAttemptsUrlPath
            ?: return emptyList()

        val doc = executeGet(attemptsUrlPath)

        val table = doc.selectFirst("#tableProve")
            ?: throw HtmlParsingException("Cannot get exam attempts: missing table")

        return table.parseTable().map { row ->
            val examDate = row.getTextAsOrNull("data appello") { parseDate(it) }
            val examType = row.getTextOrNull("tipo d'esame") ?: ""
            val gradeText = row.getTextOrNull("voto/giudizio") ?: ""
            val status = row.getTextOrNull("stato") ?: ""
            val verbalizationDate = row.getTextAsOrNull("data verb") { parseDate(it) }
            val passedText = row.getTextOrNull("superato") ?: ""

            val outcome = when {
                status.equals("Prenotato", ignoreCase = true) -> Esse3ExamAttemptOutcome.Booked
                gradeText.equals("Assente", ignoreCase = true) -> Esse3ExamAttemptOutcome.Absent
                gradeText.equals("Ritirato", ignoreCase = true) -> Esse3ExamAttemptOutcome.Withdrawn
                gradeText.equals("Respinto", ignoreCase = true) ||
                gradeText.equals("Non superato", ignoreCase = true) -> Esse3ExamAttemptOutcome.Failed
                else -> {
                    val grade = Esse3Grade.parse(gradeText)
                    if (grade != null) {
                        Esse3ExamAttemptOutcome.Passed(grade)
                    } else if (gradeText.isEmpty() && passedText.equals("No", ignoreCase = true)) {
                        Esse3ExamAttemptOutcome.Failed
                    } else {
                        throw HtmlParsingException("Cannot determine exam outcome for attempt on $examDate with status '$status', grade '$gradeText' and passed '$passedText'")
                    }
                }
            }

            Esse3ExamAttempt(
                examDate = examDate,
                examType = examType,
                outcome = outcome,
                verbalizationDate = verbalizationDate
            )
        }.toList()
    }
}
