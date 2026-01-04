package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.http.Parameters
import it.attendance100.mybicocca.data.dto.esse3.*
import org.jsoup.nodes.Document

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

    /**
     * Gets the academic record (libretto) with all courses and grades.
     *
     * @return The academic record
     */
    suspend fun getAcademicRecord(): Esse3AcademicRecord {
        val doc = executeGet(
            "/auth/studente/Libretto/LibrettoHome.do",
            mapOf("menu_opened_cod" to "menu_link-navbox_studenti_Carriera")
        )
        return parseAcademicRecord(doc)
    }

    /**
     * Gets the study plan.
     *
     * @return The study plan or null if not available
     */
    suspend fun getStudyPlan(): Esse3StudyPlan? {
        val doc = executeGet(
            "/auth/studente/Piani/PianiHome.do",
            mapOf("menu_opened_cod" to "menu_link-navbox_studenti_Carriera")
        )
        return parseStudyPlan(doc)
    }

    /**
     * Prints the study plan as PDF.
     *
     * @param planId The plan ID
     * @return The PDF bytes
     */
    suspend fun printStudyPlan(planId: Long): ByteArray {
        val response = executePostRaw(
            "/auth/studente/Piani/PianiStampaPiano.do",
            Parameters.build {
                append("btnSubmit", "")
            },
            mapOf("PIANO_ID" to planId.toString())
        )
        return response.call.body()
    }

    /**
     * Gets course evaluation questionnaires that need to be filled.
     *
     * @return List of courses with pending questionnaires
     */
    suspend fun getCourseEvaluations(): List<Esse3CourseEvaluation> {
        val doc = executeGet(
            "/auth/studente/QuestAdLibrettoValDid.do",
            mapOf("menu_opened_cod" to "menu_link-navbox_studenti_Carriera")
        )
        return parseCourseEvaluations(doc)
    }

    private fun parseAcademicRecord(doc: Document): Esse3AcademicRecord {
        val courses = mutableListOf<Esse3CourseEntry>()

        // Find the main courses table
        val table = doc.selectFirst("table.breaks3.table-1") ?: doc.selectFirst("table.table-1")

        if (table != null) {
            val rows = table.select("tbody tr, tr:has(td)")
            for (row in rows) {
                val cells = row.select("td")
                if (cells.size >= 5) {
                    val courseEntry = parseCourseRow(cells)
                    if (courseEntry != null) {
                        courses.add(courseEntry)
                    }
                }
            }
        }

        // Parse statistics
        val statistics = parseRecordStatistics(doc)

        return Esse3AcademicRecord(
            courses = courses,
            statistics = statistics
        )
    }

    private fun parseCourseRow(cells: org.jsoup.select.Elements): Esse3CourseEntry? {
        if (cells.isEmpty()) return null

        // Column order: Attività Didattiche, Anno, Peso in crediti, Stato, AA Freq., Voto - Data Esame, Ric., Prove, Appelli
        val activityCell = cells[0]
        val activityText = activityCell.text().cleanText()

        // Parse course code and name
        val codeNameMatch = "^([A-Z0-9]+)\\s*-\\s*(.+)$".toRegex().find(activityText)
        val code = codeNameMatch?.groupValues?.get(1) ?: activityText.substringBefore(" - ")
        val name = codeNameMatch?.groupValues?.get(2) ?: activityText.substringAfter(" - ")

        val year = cells.getOrNull(1)?.text()?.trim()?.toIntOrNull() ?: 0
        val credits = cells.getOrNull(2)?.text()?.trim()?.toIntOrNull() ?: 0
        val statusText = cells.getOrNull(3)?.text()?.cleanText() ?: ""
        val academicYear = cells.getOrNull(4)?.text()?.cleanText()

        // Parse grade and exam date (format: "29 - 28/06/2024" or "IDO - 17/10/2024")
        val gradeCell = cells.getOrNull(5)?.text()?.cleanText() ?: ""
        val (grade, examDate) = parseGradeAndDate(gradeCell)

        val isRecognized = cells.getOrNull(6)?.text()?.isNotBlank() == true
        val attempts = cells.getOrNull(7)?.text()?.trim()?.toIntOrNull() ?: 0
        val hasAvailableExams = cells.getOrNull(8)?.selectFirst("a") != null

        // Determine status
        val status = when {
            grade != null -> Esse3CourseStatus.PASSED
            statusText.isNotBlank() -> Esse3CourseStatus.fromString(statusText)
            else -> Esse3CourseStatus.PLANNED
        }

        return Esse3CourseEntry(
            code = code,
            name = name,
            year = year,
            credits = credits,
            status = status,
            academicYear = academicYear,
            grade = grade,
            examDate = examDate,
            isRecognized = isRecognized,
            attempts = attempts,
            hasAvailableExams = hasAvailableExams
        )
    }

    private fun parseGradeAndDate(text: String): Pair<Esse3Grade?, java.time.LocalDate?> {
        if (text.isBlank()) return null to null

        // Split by " - " or similar separator
        val parts = text.split(Regex("\\s*[-–]\\s*"))
        if (parts.isEmpty()) return null to null

        val gradeText = parts[0].trim()
        val dateText = parts.getOrNull(1)?.trim()

        val grade = Esse3Grade.parse(gradeText)
        val date = dateText?.let { parseDate(it) }

        return grade to date
    }

    private fun parseRecordStatistics(doc: Document): Esse3RecordStatistics {
        // Look for statistics box
        val statsBox = doc.selectFirst("div.box-2") ?: doc.selectFirst("div:contains(Media)")

        var arithmeticAvg: Double? = null
        var weightedAvg: Double? = null
        var totalCredits = 0
        var earnedCredits = 0

        if (statsBox != null) {
            val text = statsBox.text()

            // Parse arithmetic average
            "Media Aritmetica[^\\d]*(\\d+[.,]\\d+)".toRegex()
                .find(text)?.groupValues?.get(1)?.let {
                    arithmeticAvg = it.replace(",", ".").toDoubleOrNull()
                }

            // Parse weighted average
            "Media Ponderata[^\\d]*(\\d+[.,]\\d+)".toRegex()
                .find(text)?.groupValues?.get(1)?.let {
                    weightedAvg = it.replace(",", ".").toDoubleOrNull()
                }

            // Parse credits
            "Crediti totali[^\\d]*(\\d+)".toRegex()
                .find(text)?.groupValues?.get(1)?.let {
                    totalCredits = it.toIntOrNull() ?: 0
                }

            "Crediti acquisiti[^\\d]*(\\d+)".toRegex()
                .find(text)?.groupValues?.get(1)?.let {
                    earnedCredits = it.toIntOrNull() ?: 0
                }
        }

        return Esse3RecordStatistics(
            arithmeticAverage = arithmeticAvg,
            weightedAverage = weightedAvg,
            totalCredits = totalCredits,
            earnedCredits = earnedCredits
        )
    }

    private fun parseStudyPlan(doc: Document): Esse3StudyPlan? {
        // Parse plan metadata
        val text = doc.text()

        val statusMatch = "Stato:\\s*(\\w+)".toRegex().find(text)
        val status = statusMatch?.groupValues?.get(1)?.let { Esse3PlanStatus.fromString(it) }
            ?: Esse3PlanStatus.DRAFT

        val typeMatch = "Tipo Piano:\\s*([^\\n]+)".toRegex().find(text)
        val type = typeMatch?.groupValues?.get(1)?.trim() ?: "Unknown"

        // Try to extract plan ID from forms
        val planIdMatch = "PIANO_ID=(\\d+)".toRegex().find(doc.html())
        val planId = planIdMatch?.groupValues?.get(1)?.toLongOrNull() ?: 0L

        // Extract offer year and regulation year from page text
        val offerYearMatch = "(?:Anno Offerta|A\\.A\\. Offerta)\\s*:?\\s*(\\d{4})".toRegex().find(text)
        val offerYear = offerYearMatch?.groupValues?.get(1)?.toIntOrNull()

        val regulationYearMatch = "(?:Anno Regolamento|Regolamento)\\s*:?\\s*(\\d{4})".toRegex().find(text)
        val regulationYear = regulationYearMatch?.groupValues?.get(1)?.toIntOrNull()

        // Extract last modified date
        val lastModifiedMatch = "(?:Ultima modifica|Modificato il)\\s*:?\\s*(\\d{2}/\\d{2}/\\d{4})".toRegex().find(text)
        val lastModified = lastModifiedMatch?.groupValues?.get(1)?.let { parseDate(it) }

        // Parse courses from table
        val courses = mutableListOf<Esse3PlannedCourse>()
        val tables = doc.select("table")
        for (table in tables) {
            val headers = table.select("th").map { it.text().trim() }
            if (headers.contains("Codice") || headers.contains("Descrizione")) {
                // Find year column index if present
                val yearColIndex = headers.indexOfFirst { it.contains("Anno", ignoreCase = true) }

                val rows = table.select("tbody tr, tr:has(td)")
                for (row in rows) {
                    val cells = row.select("td")
                    if (cells.size >= 2) {
                        // Extract year from the dedicated column or from course text
                        val year = if (yearColIndex >= 0) {
                            cells.getOrNull(yearColIndex)?.text()?.trim()?.toIntOrNull()
                        } else {
                            "(?:Anno|Year)\\s*(\\d+)".toRegex().find(row.text())?.groupValues?.get(1)?.toIntOrNull()
                        }

                        courses.add(
                            Esse3PlannedCourse(
                                code = cells.getOrNull(0)?.text()?.cleanText() ?: "",
                                description = cells.getOrNull(1)?.text()?.cleanText() ?: "",
                                status = cells.getOrNull(2)?.text()?.cleanText(),
                                credits = cells.getOrNull(3)?.text()?.toIntOrNull() ?: 0,
                                year = year,
                                university = cells.getOrNull(4)?.text()?.cleanText()
                            )
                        )
                    }
                }
            }
        }

        return Esse3StudyPlan(
            id = planId,
            status = status,
            type = type,
            lastModified = lastModified,
            offerYear = offerYear,
            regulationYear = regulationYear,
            courses = courses
        )
    }

    private fun parseCourseEvaluations(doc: Document): List<Esse3CourseEvaluation> {
        val evaluations = mutableListOf<Esse3CourseEvaluation>()

        // Table columns: Anno di corso | Attività Didattiche | Peso in crediti | Stato | AA Freq. | Ric. | Q.Val.
        val table = doc.selectFirst("table.table-1")
        if (table != null) {
            val rows = table.select("tbody tr, tr:has(td)")
            for (row in rows) {
                val cells = row.select("td")
                if (cells.size >= 6) {
                    val courseYear = cells.getOrNull(0)?.text()?.trim()?.toIntOrNull() ?: 0
                    val courseText = cells.getOrNull(1)?.text()?.cleanText() ?: ""
                    val codeMatch = "^([A-Z0-9]+)\\s*-\\s*(.+)$".toRegex().find(courseText)
                    val credits = cells.getOrNull(2)?.text()?.trim()?.toIntOrNull() ?: 0
                    val status = cells.getOrNull(3)?.text()?.cleanText()?.takeIf { it.isNotBlank() }
                    val academicYear = cells.getOrNull(4)?.text()?.cleanText()?.takeIf { it.isNotBlank() }
                    val isRecognized = cells.getOrNull(5)?.text()?.isNotBlank() == true
                    val questionnaireLink = cells.getOrNull(6)?.selectFirst("a")?.attr("href")

                    evaluations.add(
                        Esse3CourseEvaluation(
                            courseCode = codeMatch?.groupValues?.get(1) ?: courseText.substringBefore(" "),
                            courseName = codeMatch?.groupValues?.get(2) ?: courseText,
                            courseYear = courseYear,
                            credits = credits,
                            academicYear = academicYear,
                            status = status,
                            isRecognized = isRecognized,
                            questionnaireUrl = questionnaireLink?.let { "$BASE_URL/$it" }
                        )
                    )
                }
            }
        }

        return evaluations
    }
}
