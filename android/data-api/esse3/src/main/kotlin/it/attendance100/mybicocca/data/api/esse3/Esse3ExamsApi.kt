package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.*
import io.ktor.client.call.*
import it.attendance100.mybicocca.data.dto.esse3.*
import org.jsoup.nodes.Document

/**
 * API for exam operations.
 *
 * Provides access to:
 * - Available exam sessions
 * - Exam reservations
 * - Exam results
 * - Reservation printing
 */
class Esse3ExamsApi(
    client: HttpClient
) : Esse3AbstractApi(client) {

    /**
     * Gets available exam sessions for the student.
     *
     * @return List of available exam sessions
     */
    suspend fun getAvailableExamSessions(): List<Esse3ExamSession> {
        val doc = executeGet(
            "/auth/studente/Appelli/Appelli.do",
            mapOf("menu_opened_cod" to "menu_link-navbox_studenti_Esami")
        )
        return parseExamSessions(doc)
    }

    /**
     * Gets the student's exam reservations.
     *
     * @return List of exam reservations
     */
    suspend fun getExamReservations(): List<Esse3ExamReservation> {
        val doc =  executeGet(
            "/auth/studente/Appelli/BachecaPrenotazioni.do",
            mapOf("menu_opened_cod" to "menu_link-navbox_studenti_Esami")
        )
        return parseExamReservations(doc)
    }

    /**
     * Gets exam results.
     *
     * @return List of exam results
     */
    suspend fun getExamResults(): List<Esse3ExamResult> {
        val doc = executeGet(
            "/auth/studente/Appelli/BachecaEsiti.do",
            mapOf("menu_opened_cod" to "menu_link-navbox_studenti_Esami")
        )
        return parseExamResults(doc)
    }

    /**
     * Prints an exam reservation as PDF.
     *
     * @param reservation The reservation to print
     * @return The PDF bytes
     */
    suspend fun printReservation(
        reservation: Esse3ExamReservation
    ): ByteArray {
        executeGet(
            "/auth/studente/Appelli/BachecaPrenotazioni.do",
            mapOf("menu_opened_cod" to "menu_link-navbox_studenti_Esami")
        )
        val response = executeGetRaw(
            "/auth/studente/Appelli/StampaStatino.do",
            reservation.toPrintParams()
        )
        return response.body<ByteArray>()
    }

    private fun parseExamSessions(doc: Document): List<Esse3ExamSession> {
        val sessions = mutableListOf<Esse3ExamSession>()

        // Select all exam reservation boxes
        val examBoxes = doc.select("div#boxPrenotazione, div.breaks3.record")

        for (box in examBoxes) {
            // Extract course name and code from h2
            val headerText = box.selectFirst("h2.record-h2")?.text()?.cleanText() ?: continue
            val codeMatch = "\\[([A-Z0-9]+)\\]".toRegex().find(headerText)
            val code = codeMatch?.groupValues?.get(1) ?: ""
            val name = headerText.replace("\\[.*?\\]".toRegex(), "").trim()

            // Parse the definition list
            val dl = box.selectFirst("dl.record-riga") ?: continue
            val dtElements = dl.select("dt")
            val ddElements = dl.select("dd")

            // Build a map of label -> value
            val dataMap = mutableMapOf<String, String>()
            for (i in dtElements.indices) {
                val label = dtElements.getOrNull(i)?.text()?.cleanText()?.lowercase() ?: continue
                val value = ddElements.getOrNull(i)?.text()?.cleanText() ?: ""
                dataMap[label] = value
            }

            // Extract date/time from the first dt (has special class)
            val dateTimeText = box.selectFirst("dt.app-box_dati_data_esame")?.text()?.cleanText() ?: ""
            val date = parseDateTime(dateTimeText)

            // Extract other fields from the map
            val typeText = dataMap["tipo prova"] ?: ""
            val type = Esse3ExamType.fromString(typeText)

            val building = dataMap["edificio"]?.takeIf { it.isNotBlank() }
            val room = dataMap["aula"]?.takeIf { it.isNotBlank() }
            val location = listOfNotNull(building, room).joinToString(" - ").takeIf { it.isNotBlank() }

            val professor = dataMap["docenti"]?.takeIf { it.isNotBlank() }

            // Find the toolbar that follows this box
            val toolbar = box.nextElementSibling()?.takeIf {
                it.hasClass("tool-bar") || it.id() == "toolbarAzioni"
            } ?: box.parent()?.selectFirst("div#toolbarAzioni")

            // Check reservation status
            val cannotCancel = dataMap["cancella prenotazione"]?.contains("Impossibile", ignoreCase = true) == true

            // Extract exam ID from cancel or print link
            val idMatch = toolbar?.selectFirst("a[href*=APP_ID]")
                ?.attr("href")
                ?.let { "APP_ID=(\\d+)".toRegex().find(it) }
            val id = idMatch?.groupValues?.get(1)?.toLongOrNull() ?: 0L

            // Extract registration number
            val registrationInfo = dataMap["numero iscrizione"] // e.g., "12 su 49"

            sessions.add(
                Esse3ExamSession(
                    id = id,
                    courseCode = code,
                    courseName = name,
                    date = date,
                    type = type,
                    location = location,
                    professor = professor,
                    closed = cannotCancel,
                    notes = registrationInfo
                )
            )
        }

        return sessions
    }

    private fun parseExamReservations(doc: Document): List<Esse3ExamReservation> {
        val reservations = mutableListOf<Esse3ExamReservation>()

        // Look for reservation cards
        val cards = doc.select("div.record, div.breaks3.record")
        for (card in cards) {
            val text = card.text()

            // Extract course info
            val courseMatch = "([A-Z0-9]+)\\s*-?\\s*(.+?)(?:\\d{2}/\\d{2}/\\d{4}|$)".toRegex()
                .find(text)
            val courseCode = courseMatch?.groupValues?.get(1)?.trim() ?: ""
            val courseName = courseMatch?.groupValues?.get(2)?.trim() ?: text.substringBefore("\\d".toRegex().pattern)

            // Extract date
            val dateMatch = "(\\d{2}/\\d{2}/\\d{4}\\s*\\d{2}:\\d{2})".toRegex().find(text)
            val date = dateMatch?.groupValues?.get(1)?.let { parseDateTime(it) }

            // Extract type
            val typeMatch = "(Prova parziale|Scritto|Orale|Laboratorio)".toRegex(RegexOption.IGNORE_CASE)
                .find(text)
            val type = typeMatch?.value?.let { Esse3ExamType.fromString(it) } ?: Esse3ExamType.OTHER

            // Extract IDs from print link
            val printLink = card.selectFirst("a[href*=StampaStatino]")?.attr("href") ?: ""
            val appId = "APP_ID=(\\d+)".toRegex().find(printLink)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
            val adsceId = "ADSCE_ID=(\\d+)".toRegex().find(printLink)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
            val attDidId = "ATT_DID_ESA_ID=(\\d+)".toRegex().find(printLink)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
            val cdsId = "CDS_ESA_ID=(\\d+)".toRegex().find(printLink)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
            val aaFreqId = "AA_FREQ_ID=(\\d+)".toRegex().find(printLink)?.groupValues?.get(1)?.toLongOrNull() ?: 0L

            // Extract location
            val location = "(?:Aula|Luogo|Location)\\s*:?\\s*(.+?)(?:\\s{2,}|Docente|Tipo|$)".toRegex()
                .find(text)?.groupValues?.get(1)?.trim()
                ?: card.selectFirst("span.location, div.location")?.text()?.cleanText()

            // Extract status
            val status = when {
                text.contains("Confermata", ignoreCase = true) -> "Confermata"
                text.contains("In attesa", ignoreCase = true) -> "In attesa"
                text.contains("Annullata", ignoreCase = true) -> "Annullata"
                text.contains("Prenotata", ignoreCase = true) -> "Prenotata"
                else -> card.selectFirst("span.status, div.status")?.text()?.cleanText()
            }

            if (appId > 0 || courseCode.isNotBlank()) {
                reservations.add(
                    Esse3ExamReservation(
                        appId = appId,
                        adsceId = adsceId,
                        attDidEsaId = attDidId,
                        cdsEsaId = cdsId,
                        aaFreqId = aaFreqId,
                        date = date,
                        courseCode = courseCode,
                        courseName = courseName,
                        type = type,
                        location = location,
                        status = status
                    )
                )
            }
        }

        return reservations
    }

    private fun parseExamResults(doc: Document): List<Esse3ExamResult> {
        val results = mutableListOf<Esse3ExamResult>()

        val table = doc.selectFirst("table.table-1")
        if (table != null) {
            val rows = table.select("tbody tr, tr:has(td)")
            for (row in rows) {
                val cells = row.select("td")
                if (cells.size >= 3) {
                    val courseText = cells[0].text().cleanText()
                    val codeMatch = "\\[([A-Z0-9]+)\\]".toRegex().find(courseText)
                    val code = codeMatch?.groupValues?.get(1) ?: courseText.substringBefore(" ")
                    val name = courseText.replace("\\[.*?\\]".toRegex(), "").trim()

                    val dateText = cells.getOrNull(1)?.text()?.cleanText() ?: ""
                    val date = parseDate(dateText)

                    val gradeText = cells.getOrNull(2)?.text()?.cleanText() ?: ""
                    val grade = Esse3Grade.parse(gradeText)

                    val statusText = cells.getOrNull(3)?.text()?.cleanText() ?: ""
                    val status = Esse3ResultStatus.fromString(statusText)

                    val professor = cells.getOrNull(4)?.text()?.cleanText()?.takeIf { it.isNotBlank() }

                    // Extract notes from dedicated column or title attribute
                    val notes = cells.getOrNull(5)?.text()?.cleanText()?.takeIf { it.isNotBlank() }
                        ?: row.attr("title")?.takeIf { it.isNotBlank() }
                        ?: row.selectFirst("td.note, span.note")?.text()?.cleanText()

                    results.add(
                        Esse3ExamResult(
                            courseCode = code,
                            courseName = name,
                            date = date,
                            grade = grade,
                            status = status,
                            professor = professor,
                            notes = notes
                        )
                    )
                }
            }
        }

        return results
    }
}
