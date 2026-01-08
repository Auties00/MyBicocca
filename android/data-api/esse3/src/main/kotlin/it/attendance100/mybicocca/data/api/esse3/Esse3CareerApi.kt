package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import it.attendance100.mybicocca.data.dto.esse3.*

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
            ?: throw IllegalStateException("Cannot get study plan: missing status")
        val status = Esse3PlanStatus.fromString(statusText)
            ?: throw IllegalStateException("Cannot get study plan: invalid status '$statusText'")

        val type = metadataMap["tipo piano"]
            ?: throw IllegalStateException("Cannot get study plan: missing type")

        val lastModifiedText = metadataMap["data ultima variazione"]
            ?: throw IllegalStateException("Cannot get study plan: missing last modified date")
        val lastModified = parseDate(lastModifiedText)
            ?: throw IllegalStateException("Cannot get study plan: invalid date '$lastModifiedText'")

        val offerYearText = metadataMap["anno di offerta"]
            ?: throw IllegalStateException("Cannot get study plan: missing offer year")
        val offerYear = offerYearText.toIntOrNull()
            ?: throw IllegalStateException("Cannot get study plan: invalid offer year '$offerYearText'")

        val regulationYearText = metadataMap["anno del regolamento"]
            ?: throw IllegalStateException("Cannot get study plan: missing regulation year")
        val regulationYear = regulationYearText.toIntOrNull()
            ?: throw IllegalStateException("Cannot get study plan: invalid regulation year '$regulationYearText'")

        val yearTitles = doc.select("td.tplTitolo")
        val tables = doc.select("table.detail_table")

        val courses = yearTitles.zip(tables).flatMap { (titleTd, table) ->
            val year = YEAR_OF_COURSE_REGEX.find(titleTd.text())
                ?.groupValues?.get(1)?.toIntOrNull() ?: 0

            // Build headers list, expanding for colspan
            val headers = table.select("thead tr th").map {
                val node = it.firstChild() ?: it
                node.nodeValue().trim().lowercase()
            }

            table.select("tbody tr").map { row ->
                val rows = row.select("td")
                val rowMap = headers.zip(rows).toMap()

                val code = rowMap["codice"]?.text()?.cleanText()
                    ?: throw IllegalStateException("Cannot get study plan: missing code")

                val description = rowMap["descrizione"]?.text()?.cleanText()
                    ?: throw IllegalStateException("Cannot get study plan: missing description")

                val statusText = rowMap["stato"]?.text()?.cleanText()
                    ?: throw IllegalStateException("Cannot get study plan: missing status")
                val status = Esse3PlannedCourseStatus.fromString(statusText)
                    ?: throw IllegalStateException("Cannot get study plan: invalid status '$statusText'")

                val credits = rowMap["peso"]?.text()?.cleanText()?.toIntOrNull()
                    ?: throw IllegalStateException("Cannot get study plan: missing credits")

                val universityCell = rowMap["ateneo"]
                    ?: throw IllegalStateException("Cannot get study plan: missing university")
                val university = universityCell.selectFirst("span")?.attr("title")
                    ?.takeIf { it.isNotBlank() }
                    ?: universityCell.text().cleanText()

                Esse3PlannedCourse(
                    code = code,
                    description = description,
                    status = status,
                    credits = credits,
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
            ?: throw IllegalStateException("Cannot find print study plan form")

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
            ?: throw IllegalStateException("Cannot get academic record: missing GPA box")
        val unweightedGpa = UNWEIGHTED_GPA_REGEX.find(gpaBox)?.groupValues?.get(1)?.toDoubleOrNull()
            ?: throw IllegalStateException("Cannot get academic record: missing unweighted GPA")
        val weightedGpa = WEIGHTED_GPA_REGEX.find(gpaBox)?.groupValues?.get(1)?.toDoubleOrNull()
            ?: throw IllegalStateException("Cannot get academic record: missing weighted GPA")

        val table = doc.selectFirst("#tableLibretto")
            ?: throw IllegalStateException("Cannot get academic record: missing table")
        val headers = table.select("thead tr th").map {
            val node = it.firstChild() ?: it
            node.nodeValue().trim().lowercase()
        }

        val rows = table.select("tbody tr")
        val courses = rows.mapNotNull { row ->
            val cells = row.select("td")
            val rowMap = headers.zip(cells).toMap()

            val teachingActivityElement = rowMap["attività didattiche"]
                ?: throw IllegalStateException("Cannot get academic record: missing teaching activity")
            val urlPath = teachingActivityElement.selectFirst("a")?.attr("href")
                ?: throw IllegalStateException("Cannot get academic record: missing teaching activity url")
            val teachingActivityParts = teachingActivityElement.text().cleanText().split(" - ", limit = 2)
            val code = teachingActivityParts.elementAtOrNull(0)
                ?: throw IllegalStateException("Cannot get academic record: missing code")
            val name = teachingActivityParts.elementAtOrNull(1)
                ?: throw IllegalStateException("Cannot get academic record: missing name")

            val year = rowMap["anno"]?.text()?.cleanText()?.toIntOrNull()
                ?: throw IllegalStateException("Cannot get academic record: missing year")

            val credits = rowMap["peso in crediti"]?.text()?.cleanText()?.toIntOrNull()
                ?: throw IllegalStateException("Cannot get academic record: missing credits")

            val academicYear = rowMap["aa. freq"]?.text()?.cleanText()
                ?: throw IllegalStateException("Cannot get academic record: missing academic year")

            val gradeAndDate = rowMap["voto - data esame"]?.text()?.cleanText()
                ?: throw IllegalStateException("Cannot get academic record: missing grade and date")
            val gradeAndDateParts = gradeAndDate.split(" - ", limit = 2)
            val grade = gradeAndDateParts.elementAtOrNull(0)?.let { Esse3Grade.parse(it) }
                ?: throw IllegalStateException("Cannot get academic record: missing grade")
            val dateText = gradeAndDateParts.elementAtOrNull(1)
                ?: throw IllegalStateException("Cannot get academic record: missing date")
            val date = parseDate(dateText)
                ?: throw IllegalStateException("Cannot get academic record: invalid date '$dateText'")

            val attemptsUrl = rowMap["prove"]?.text()?.cleanText()
                ?: throw IllegalStateException("Cannot get academic record: missing attempts")

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
        }

        return Esse3AcademicRecord(
            courses = courses,
            unweightedGpa = unweightedGpa,
            weightedGpa = weightedGpa,
        )
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
            ?: throw IllegalStateException("Cannot get course info: missing header")
        val titleText = header.selectFirst("h2")?.text()?.cleanText() ?: ""
        val (name, code) = TEACHING_ACTIVITY_REGEX.find(titleText)?.destructured?.let { (n, c) -> n to c }
            ?: throw IllegalStateException("Cannot match teaching activity in: $titleText")

        val courseAndDegreeText = header.selectFirst("p")?.text()?.cleanText() ?: ""
        val (courseName, courseCode, degreeCode, degreeDesc) = COURSE_AND_DEGREE_REGEX.find(courseAndDegreeText)?.destructured
            ?: throw IllegalStateException("Cannot match course and degree: $courseAndDegreeText")

        val detailsBox = doc.selectFirst("#boxDatiAd")
            ?: throw IllegalStateException("Cannot get course info: missing details box")
        val detailsMap = detailsBox.select("dt").associate { dt ->
            dt.text().trim().removeSuffix(":").lowercase() to dt.nextElementSibling()?.text()?.cleanText()
        }

        val year = detailsMap["anno di corso"]?.toIntOrNull()
            ?: throw IllegalStateException("Cannot get course info: missing or invalid year")

        val statusText = detailsMap["stato"] ?: ""
        val status = PASSED_STATUS_REGEX.find(statusText)?.let {
            Esse3CourseStatus.Passed(it.groupValues[1], it.groupValues[2])
        } ?: ATTENDED_STATUS_REGEX.find(statusText)?.let {
            Esse3CourseStatus.Attended(it.groupValues[1])
        } ?: Esse3CourseStatus.NotAttended

        val examDate = detailsMap["data esame"]?.let { parseDate(it) }
        val grade = detailsMap["voto / giudizio"]?.let { Esse3Grade.parse(it) }
        val notes = detailsMap["note"] ?: ""

        val table = doc.selectFirst("#tableUD")
            ?: throw IllegalStateException("Cannot get course info: missing didactic units table")
        val headers = table.select("thead tr th").map {
            val node = it.firstChild() ?: it
            node.nodeValue().trim().lowercase()
        }
        val units = table.select("tbody tr").map { row ->
            val cells = row.select("td")
            val rowMap = headers.zip(cells).toMap()

            val unitName = rowMap["unità dididattica"]?.text()?.cleanText()
                ?: throw IllegalStateException("Cannot get course unit: missing name")
            val activityType = rowMap["tipo attività"]?.text()?.cleanText() ?: ""
            val formationType = rowMap["tipo formaz."]?.text()?.cleanText() ?: ""
            val sector = rowMap["settore"]?.text()?.cleanText() ?: ""
            val credits = rowMap["cfu"]?.text()?.cleanText()?.toIntOrNull()
                ?: throw IllegalStateException("Cannot get course unit: missing or invalid credits")
            val duration = rowMap["durata"]?.text()?.cleanText()?.toIntOrNull()

            Esse3CourseUnit(
                name = unitName,
                activityType = activityType,
                formationType = formationType,
                sector = sector,
                credits = credits,
                duration = duration
            )
        }

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
        val doc = executeGet(course.examAttemptsUrlPath)

        val table = doc.selectFirst("#tableProve")
            ?: throw IllegalStateException("Cannot get exam attempts: missing table")

        val headers = table.select("thead tr th").map {
            val node = it.firstChild() ?: it
            node.nodeValue().trim().lowercase()
        }

        return table.select("tbody tr").map { row ->
            val cells = row.select("td")
            val rowMap = headers.zip(cells).toMap()

            val examDate = rowMap["data appello"]?.text()?.cleanText()?.let { parseDate(it) }
            val examType = rowMap["tipo d'esame"]?.text()?.cleanText() ?: ""
            val gradeText = rowMap["voto/giudizio"]?.text()?.cleanText() ?: ""
            val status = rowMap["stato"]?.text()?.cleanText() ?: ""
            val verbalizationDate = rowMap["data verb"]?.text()?.cleanText()?.let { parseDate(it) }
            val passedText = rowMap["superato"]?.text()?.cleanText() ?: ""

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
                        throw IllegalStateException("Cannot determine exam outcome for attempt on $examDate with status '$status', grade '$gradeText' and passed '$passedText'")
                    }
                }
            }

            Esse3ExamAttempt(
                examDate = examDate,
                examType = examType,
                outcome = outcome,
                verbalizationDate = verbalizationDate
            )
        }
    }
}
