package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import it.attendance100.mybicocca.data.common.exception.ApiRequestException
import it.attendance100.mybicocca.data.common.exception.HtmlParsingException
import it.attendance100.mybicocca.data.common.util.cleanText
import it.attendance100.mybicocca.data.common.util.extractQueryParam
import it.attendance100.mybicocca.data.common.util.parseGrid
import it.attendance100.mybicocca.data.common.util.toHtml
import it.attendance100.mybicocca.data.common.util.parseTable
import it.attendance100.mybicocca.data.dto.esse3.*
import org.jsoup.nodes.Document
import org.jsoup.nodes.FormElement

/**
 * API for exam operations.
 */
class Esse3ExamsApi(
    client: HttpClient
) : Esse3AbstractApi(client) {
    companion object {
        private const val EXAMS_SESSIONS_ENTRYPOINT = "/auth/studente/Appelli/Appelli.do?menu_opened_cod=menu_link-navbox_studenti_Esami"
        private const val BOOKED_EXAMS_ENTRYPOINT = "/auth/studente/Appelli/BachecaPrenotazioni.do?menu_opened_cod=menu_link-navbox_studenti_Esami"
        private const val RESERVATIONS_HISTORY_ENTRYPOINT = "/auth/studente/Appelli/LogPrenotazioni.do"
        private const val EXAMS_RESULTS_ENTRYPOINT = "/auth/studente/Appelli/BachecaEsiti.do?menu_opened_cod=menu_link-navbox_studenti_Esami"
    }

    /**
     * Gets available exam sessions for the student.
     *
     * @return List of available exam sessions
     */
    suspend fun getAvailableExamSessions(): List<Esse3ExamSession> {
        val doc = executeGet(EXAMS_SESSIONS_ENTRYPOINT)

        val table = doc.selectFirst("#app-tabella_appelli")
            ?: return emptyList()

        return table.parseTable().map { row ->
            val infoLinkCell = row.getElementOrNull("") ?: row.getElementOrThrow("attività didattica")
            val infoPath = infoLinkCell.selectFirst("a[href*='APP_ID=']")?.attr("href")
                ?: throw HtmlParsingException("Cannot get available exam sessions: missing APP_ID link")

            val courseName = row.getTextOrThrow("attività didattica")

            val examDate = row.getTextAsOrThrow("appello") {
                parseDate(it)
            }

            val (registrationStartDate, registrationEndDate) = row.getElementAsOrThrow("iscrizione") {
                val registrationNodes = it.childNodes()
                val registrationStartDateText = registrationNodes.firstOrNull()?.nodeValue()
                    ?: return@getElementAsOrThrow null
                val registrationStartDate = parseDate(registrationStartDateText)
                    ?: return@getElementAsOrThrow null
                val registrationEndDateText = registrationNodes.lastOrNull()?.nodeValue()
                    ?: return@getElementAsOrThrow null
                val registrationEndDate = parseDate(registrationEndDateText)
                    ?: return@getElementAsOrThrow null
                registrationStartDate to registrationEndDate
            }

            val description = row.getTextOrThrow("descrizione")

            val examMode = row.getTextAsOrThrow("svolg. esame (d = distanza, p = presenza)") {
                Esse3ExamSessionMode.fromString(it)
            }

            val sessionsCell = row.getElementOrThrow("sessioni")
            val academicYears = sessionsCell.childNodes().asSequence()
                .map { it.nodeValue().cleanText() }
                .filter { it.isNotBlank() }
                .toList()

            Esse3ExamSession(
                courseName = courseName,
                examDate = examDate,
                registrationStartDate = registrationStartDate,
                registrationEndDate = registrationEndDate,
                description = description,
                examMode = examMode,
                academicYears = academicYears,
                infoPath = infoPath
            )
        }.toList()
    }

    /**
     * Gets detailed information for an exam session.
     *
     * @param session the exam session
     * @return the detailed exam session
     */
    suspend fun getExamSessionInfo(session: Esse3ExamSession): Esse3ExamSessionInformation {
        val doc = executeGet(session.infoPath)

        val data = doc.selectFirst(".record-riga")?.parseGrid()
            ?: throw HtmlParsingException("Cannot get exam session info: missing 'record-riga' table")

        val teachingActivity = data.getTextOrThrow("attività didattica")
        val description = data.getTextOrThrow("appello")

        val sessions = data.getTextOrNull("sessioni")
            ?.splitToSequence(",")
            ?.map { it.cleanText() }
            ?.filter { it.isNotBlank() }
            ?.distinct()
            ?.toList()
            ?: emptyList()

        val type = data.getTextAsOrThrow("tipo esame") { Esse3ExamType.fromString(it) }
        val verbalization = data.getTextOrThrow("verbalizzazione")

        val teachersElement = data.getElementOrThrow("docenti")
        val teachers = teachersElement.childNodes().mapNotNull { node ->
            node.nodeValue()
            // Get text content from text nodes, skip other node types
            node.nodeValue().cleanText().takeIf { it.isNotBlank() }
        }.map { teacherText ->
            // Remove the parenthetical part (e.g., "(Docente Responsabile)")
            teacherText.lastIndexOf('(').takeIf { it != -1 }?.let { teacherText.take(it).trim() }
                ?: teacherText
        }

        val notes = data.getTextOrNull("note")

        val shiftsTable = doc.selectFirst("#app-tabella_turni")
            ?: throw HtmlParsingException("Cannot reserve exam session: missing shifts table")
        val shiftHeaders = shiftsTable.select("thead tr th").map {
            it.text().cleanText().lowercase()
        }
        // #app-tabella_turni is a table so it could contain more than one entry
        // But then when you go to the exams reservations page, there is only one field for the date/building/room
        // Plus I've never seen this table with more than one entry, so I assume only one entry can be here even though they used a table
        val shiftRows = shiftsTable.selectFirst("tbody tr")
            ?: throw HtmlParsingException("Cannot reserve exam session: missing shift")
        val shiftCells = shiftRows.select("td")
        val shiftData = shiftHeaders.zip(shiftCells).toMap()

        val datetimeText = shiftData["data - ora"]?.text()?.cleanText()?.split("-", limit = 2)[0]
            ?: throw HtmlParsingException("Cannot reserve exam session: missing shift date")
        val datetime = parseDateTime(datetimeText)
            ?: throw HtmlParsingException("Cannot reserve exam session: invalid datetime '$datetimeText'")

        val registrationNumber = shiftData["# iscr"]?.text()?.cleanText()?.toIntOrNull()

        val locationInfo = shiftData["edificio e aula"]?.text()?.cleanText()
            ?: throw HtmlParsingException("Cannot reserve exam session: missing shift location")
        val (building, room) = when {
            locationInfo.contains(" - ") -> {
                val index = locationInfo.lastIndexOf(" - ")
                locationInfo.take(index).trim() to locationInfo.substring(index + 3).trim()
            }

            locationInfo.contains("-") -> {
                val index = locationInfo.lastIndexOf("-")
                locationInfo.take(index).trim() to locationInfo.substring(index + 1).trim()
            }

            else -> locationInfo.trim() to ""
        }

        return Esse3ExamSessionInformation(
            examSession = session,
            teachingActivity = teachingActivity,
            description = description,
            sessions = sessions,
            type = type,
            verbalization = verbalization,
            teachers = teachers,
            notes = notes,
            datetime = datetime,
            building = building,
            room = room,
            registrationNumber = registrationNumber
        )
    }

    /**
     * Reserves an exam session.
     *
     * @param session the exam session to reserve
     * @param notes notes for the teacher, empty by default
     * @return the detailed exam session
     */
    suspend fun reserveExamSession(session: Esse3ExamSession, notes: String = "") {
        executeGet(EXAMS_SESSIONS_ENTRYPOINT)
        val doc = executeGet(session.infoPath)

        val form = doc.selectFirst("#app-form_dati_pren")
        if (form !is FormElement) throw HtmlParsingException("Missing form")

        val formUrl = "$BASE_URL/${form.absUrl("action")}"
        val formParameters = Parameters.build {
            form.formData().forEach {
                append(it.key(), it.value())
            }
            append("NOTE_STU", notes)
        }

        val response = executePostRaw(formUrl, formParameters)
        if (response.status.value != 200) {
            throw ApiRequestException(response.status.value, "Cannot reserve exam session: invalid response status ${response.status.value}")
        }

        val document = response.toHtml()
        val errorMessage = document.selectFirst("#app-text_esito_pren_msg")?.text()
        if (errorMessage != null && errorMessage.contains("Attenzione")) {
            throw HtmlParsingException(errorMessage)
        }
    }

    /**
     * Cancels an exam reservation.
     *
     * @param reservation The reservation to cancel
     * @throws HtmlParsingException if cancellation is not possible or reservation not found
     */
    suspend fun cancelExamReservation(reservation: Esse3ExamReservation) {
        val doc = executeGet(BOOKED_EXAMS_ENTRYPOINT)
        val cancelUrl = getReservationActionUrlOrNull(doc, reservation, "btnCancella")
            ?: throw HtmlParsingException("Cannot cancel exam reservation: registration is closed or reservation not found")

        val confirmDoc = executeGet(cancelUrl)

        val errorMsg = confirmDoc.selectFirst(".alert-danger, .errore, #error")?.text()
        if (errorMsg != null && errorMsg.isNotBlank()) {
            throw HtmlParsingException(errorMsg)
        }

        val form = confirmDoc.selectFirst("#formCancellazioneAppello") as? FormElement
            ?: throw HtmlParsingException("Cannot cancel exam reservation: confirmation form 'formCancellazioneAppello' not found")

        val formUrl = form.attr("action")

        val formParameters = Parameters.build {
            form.formData().forEach {
                append(it.key(), it.value())
            }
        }

        val response = executePostRaw(formUrl, formParameters)
        if (response.status != HttpStatusCode.OK) {
            throw ApiRequestException(response.status.value, "Cannot cancel exam reservation: status code ${response.status.value}")
        }

        val resultDoc = response.toHtml()
        val resultError = resultDoc.selectFirst(".alert-danger, .errore, #error")?.text()
        if (resultError != null && resultError.contains("Attenzione", ignoreCase = true)) {
            throw HtmlParsingException(resultError)
        }
    }

    /**
     * Prints an exam reservation as PDF.
     *
     * @param reservation The reservation to print
     * @return The PDF content as a ByteReadChannel
     * @throws HtmlParsingException if print link not found
     */
    suspend fun printExamReservation(reservation: Esse3ExamReservation): ByteReadChannel {
        val doc = executeGet(BOOKED_EXAMS_ENTRYPOINT)

        val printUrl = getReservationActionUrlOrNull(doc, reservation, "btnStampa")
            ?: throw HtmlParsingException("Cannot print exam reservation: print link 'btnStampa' not found")

        val response = executeGetRaw(printUrl)
        if (response.status != HttpStatusCode.OK) {
            throw ApiRequestException(response.status.value, "Cannot print exam reservation: status code ${response.status.value}")
        }

        return response.bodyAsChannel()
    }

    private fun getReservationActionUrlOrNull(
        doc: Document, reservation: Esse3ExamReservation, buttonId: String
    ): String? {
        val actionLinks = doc.select("a#$buttonId, a[id=$buttonId]")
        for (link in actionLinks) {
            val href = link.attr("href")
            val appId = extractQueryParam(href, "APP_ID")
            val adsceId = extractQueryParam(href, "ADSCE_ID")
            if (appId == reservation.sessionId && adsceId == reservation.teachingActivityId) {
                return href
            }
        }
        return null
    }

    /**
     * Gets the student's exam reservations.
     *
     * @return List of exam reservations
     */
    suspend fun getExamReservations(): List<Esse3ExamReservation> {
        val doc = executeGet(BOOKED_EXAMS_ENTRYPOINT)

        return doc.select("#boxPrenotazione").map { reservation ->
            val toolbar = reservation.nextElementSiblings().selectFirst("#toolbarAzioni")
                ?: throw HtmlParsingException("Cannot get exam reservations: missing toolbar")
            val printButton = toolbar.selectFirst("#btnStampa")
                ?: throw HtmlParsingException("Cannot get exam reservations: missing print button")
            val printButtonHref = printButton.attr("href")
            val sessionId = extractQueryParam(printButtonHref, "APP_ID")
                ?: throw HtmlParsingException("Cannot get exam reservations: missing session id in print button url")
            val teachingActivityId = extractQueryParam(printButtonHref, "ADSCE_ID")
                ?: throw HtmlParsingException("Cannot get exam reservations: missing teaching activity id in print button url")

            val teachingActivity = reservation.selectFirst("h2")?.text()?.cleanText()
                ?: throw HtmlParsingException("Cannot get exam reservations: missing teaching activity")

            val dl = reservation.selectFirst("dl.record-riga")
                ?: throw HtmlParsingException("Cannot get exam reservations: missing details list")

            val examDateDt = dl.selectFirst("dt.app-box_dati_data_esame")?.text()?.cleanText()
                ?: throw HtmlParsingException("Cannot get exam reservations: missing 'dt.app-box_dati_data_esame' element")
            val examDateTime = parseDateTime(examDateDt)
                ?: throw HtmlParsingException("Cannot get exam reservations: cannot parse date '${examDateDt}'")

            val examDateDd = dl.selectFirst("dd.app-box_dati_data_esame")
                ?: throw HtmlParsingException("Cannot get exam reservations: missing 'dd.app-box_dati_data_esame' element")
            val examIsPartial = examDateDd.text().contains("Prova parziale", ignoreCase = true)

            val data = dl.parseGrid()

            val description = data.getTextOrThrow("appello")

            val type = if (examIsPartial) {
                Esse3ExamType.Partial
            } else {
                data.getTextAsOrThrow("tipo prova") { Esse3ExamType.fromString(it) }
            }

            val examBuilding = data.getTextOrThrow("edificio")
            val examRoom = data.getTextOrThrow("aula")

            val teachersElement = data.getElementOrThrow("docenti")
            val teachers = teachersElement.childNodes().mapNotNull { node ->
                node.nodeValue().cleanText().takeIf { it.isNotBlank() }
            }

            val reservationNumberText = data.getTextOrThrow("numero iscrizione")
            val reservationNumberParts = reservationNumberText.split(" su ", limit = 2)
            val reservationNumber = reservationNumberParts[0].trim().toInt()
            val maxReservationsCount = reservationNumberParts[1].trim().toInt()

            val mode = data.getTextAsOrThrow("svolgimento esame") { Esse3ExamSessionMode.fromString(it) }

            val notes = data.getTextOrNull("note")

            Esse3ExamReservation(
                teachingActivity = teachingActivity,
                description = description,
                type = type,
                teachers = teachers,
                notes = notes,
                datetime = examDateTime,
                building = examBuilding,
                room = examRoom,
                sessionId = sessionId,
                teachingActivityId = teachingActivityId,
                reservationNumber = reservationNumber,
                maxReservationsCount = maxReservationsCount,
                examMode = mode
            )
        }
    }

    /**
     * Gets the complete history of exam reservation operations.
     *
     * @return List of course reservation histories, each containing all operations for that course
     */
    suspend fun getExamReservationsHistory(): List<Esse3CourseReservationHistory> {
        val doc = executeGet(RESERVATIONS_HISTORY_ENTRYPOINT)

        return doc.select("table.app-table_pren_log").map { table ->
            val caption = table.selectFirst("caption")?.text()?.cleanText()
                ?: throw HtmlParsingException("Cannot get exam reservations: missing caption")

            val entries = table.parseTable().map { row ->
                val operationDateTime = row.getTextAsOrNull("data") { parseDateTime(it) }
                    ?: throw HtmlParsingException("Cannot get exam reservations history: missing date")

                val sessionData = row.getTextOrThrow("appello (descrizione - data)")
                val examDescription = sessionData.substringBeforeLast("-")
                val examDate = parseDate(sessionData.substringAfterLast("-"))

                val operationText = row.getTextOrThrow("operazione")
                val operation = Esse3ReservationOperation.fromString(operationText)
                    ?: throw HtmlParsingException("Cannot get exam reservations history: invalid operation '$operationText'")

                val performedBy = row.getTextOrThrow("effettuata da")

                Esse3ReservationHistoryEntry(
                    operationDateTime = operationDateTime,
                    examDescription = examDescription,
                    examDate = examDate,
                    operation = operation,
                    performedBy = performedBy
                )
            }.toList()

            Esse3CourseReservationHistory(
                course = caption, entries = entries
            )
        }
    }

    /**
     * Gets exam results.
     *
     * @return List of exam results
     */
    suspend fun getExamResults(): List<Esse3ExamResult> {
        val doc = executeGet(EXAMS_RESULTS_ENTRYPOINT)
        TODO()
    }
}
