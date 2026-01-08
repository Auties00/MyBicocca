package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import it.attendance100.mybicocca.data.dto.esse3.*
import org.jsoup.Jsoup
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
            ?: throw IllegalStateException("Cannot get available exam sessions: missing 'app-tabella_appelli' table")

        val headers = table.select("thead tr th").map {
            val node = it.firstChild() ?: it
            node.nodeValue().trim().lowercase()
        }

        val rows = table.select("tbody tr")

        return rows.map { row ->
            val cells = row.select("td")
            val rowMap = headers.zip(cells).toMap()

            val infoLinkCell = rowMap[""] ?: row
            val infoPath = infoLinkCell.selectFirst("a[href*='APP_ID=']")?.attr("href")
                ?: throw IllegalStateException("Cannot get available exam sessions: mmissing APP_ID link")

            val courseName = rowMap["attività didattica"]?.text()?.cleanText()
                ?: throw IllegalStateException("Cannot get available exam sessions: missing course name")

            val examDateText = rowMap["appello"]?.text()?.cleanText()
                ?: throw IllegalStateException("Cannot get available exam sessions: missing description")
            val examDate = parseDate(examDateText)
                ?: throw IllegalArgumentException("Cannot get available exam sessions: invalid exam date '$examDateText'")

            val registrationNodes = rowMap["iscrizione"]?.childNodes()
                ?: throw IllegalStateException("Cannot get available exam sessions: missing registration column")
            if (registrationNodes.isEmpty()) throw IllegalArgumentException("Expected non-empty registration cell")
            val registrationStartDate = parseDate(registrationNodes.first().nodeValue())
                ?: throw IllegalArgumentException("Cannot get available exam sessions: invalid registration start date")
            val registrationEndDate = parseDate(registrationNodes.last().nodeValue()) ?: throw IllegalArgumentException(
                "Cannot get available exam sessions: invalid registration end date"
            )

            val description = rowMap["descrizione"]?.text()?.cleanText()
                ?: throw IllegalStateException("Cannot get available exam sessions: missing description column")

            val modeText = rowMap["svolg. esame"]?.text()?.cleanText() ?: ""
            val examMode = Esse3ExamSessionMode.fromString(modeText)
                ?: throw IllegalArgumentException("Cannot get available exam sessions: invalid exam mode code '$modeText'")

            val academicYears = rowMap["sessioni"]?.childNodes()?.asSequence()?.map { it.nodeValue().cleanText() }
                ?.filter { it.isNotBlank() }?.toList()
                ?: throw IllegalArgumentException("Cannot get available exam sessions: missing academic years")

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
        }
    }

    /**
     * Gets detailed information for an exam session.
     *
     * @param session the exam session
     * @return the detailed exam session
     */
    suspend fun getExamSessionInfo(session: Esse3ExamSession): Esse3ExamSessionInformation {
        val doc = executeGet(session.infoPath)

        val dl = doc.selectFirst(".record-riga")
            ?: throw IllegalArgumentException("Cannot get exam session info: missing 'record-riga' table")
        val dataMap = dl.select("dt").associate { dt ->
            val key = dt.text().trim().removeSuffix(":").lowercase()
            val value = dt.nextElementSibling()
            key to value
        }

        val teachingActivity = dataMap["attività didattica"]?.nodeValue()?.cleanText()
            ?: throw IllegalStateException("Cannot get exam session info: missing teaching activity")

        val description = dataMap["appello"]?.nodeValue()?.cleanText()
            ?: throw IllegalStateException("Cannot get exam session info: missing description")

        val sessions =
            dataMap["sessioni"]?.nodeValue()?.splitToSequence(",")?.map { it.cleanText() }?.filter { it.isNotBlank() }
                ?.distinct()?.toList() ?: emptyList()

        val typeCode = dataMap["tipo esame"]?.nodeValue()?.cleanText()
            ?: throw IllegalStateException("Cannot get exam session info: missing exam type")
        val type = Esse3ExamType.fromString(typeCode)

        val verbalization = dataMap["verbalizzazione"]?.nodeValue()?.cleanText()
            ?: throw IllegalStateException("Cannot get exam session info: missing verbalization")

        val teachersNodes = dataMap["docenti"]?.childNodes()
            ?: throw IllegalStateException("Cannot get exam session info: missing teachers")
        val teachers = teachersNodes.map { teacherNode ->
            val teacherText = teacherNode.nodeValue().cleanText()
            teacherText.lastIndexOf('(').takeIf { it != -1 }?.let { teacherText.take(it) }
                ?: teacherText
        }

        val notes = dataMap["note"]?.nodeValue()?.cleanText()

        val shiftsTable = doc.selectFirst("#app-tabella_turni")
            ?: throw IllegalStateException("Cannot reserve exam session: missing shifts table")
        val shiftHeaders = shiftsTable.select("thead tr th").map {
            it.text().cleanText().lowercase()
        }
        // #app-tabella_turni is a table so it could contain more than one entry
        // But then when you go to the exams reservations page, there is only one field for the date/building/room
        // Plus I've never seen this table with more than one entry, so I assume only one entry can be here even though they used a table
        val shiftRows = shiftsTable.selectFirst("tbody tr")
            ?: throw IllegalStateException("Cannot reserve exam session: missing shift")
        val shiftCells = shiftRows.select("td")
        val shiftData = shiftHeaders.zip(shiftCells).toMap()

        val datetimeText = shiftData["data - ora"]?.text()?.cleanText()
            ?: throw IllegalStateException("Cannot reserve exam session: missing shift date")
        val datetime = parseDateTime(datetimeText)
            ?: throw IllegalStateException("Cannot reserve exam session: invalid datetime '$datetimeText'")

        val registrationNumber = shiftData["# iscr"]?.text()?.cleanText()?.toIntOrNull()

        val locationInfo = shiftData["edificio e aula"]?.text()?.cleanText()
            ?: throw IllegalStateException("Cannot reserve exam session: missing shift location")
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
        if (form !is FormElement) throw IllegalStateException("Missing form")

        val formUrl = "$BASE_URL/${form.absUrl("action")}"
        val formParameters = Parameters.build {
            form.formData().forEach {
                append(it.key(), it.value())
            }
            append("NOTE_STU", notes)
        }

        val response = executePostRaw(formUrl, formParameters)
        if (response.status.value != 200) {
            throw IllegalStateException("Cannot reserve exam session: invalid response status ${response.status.value}")
        }

        val document = Jsoup.parse(response.bodyAsChannel().toInputStream(), "UTF-8", BASE_URL)
        val errorMessage = document.selectFirst("#app-text_esito_pren_msg")?.text()
        if (errorMessage != null && errorMessage.contains("Attenzione")) {
            throw IllegalStateException(errorMessage)
        }
    }

    /**
     * Cancels an exam reservation.
     *
     * @param reservation The reservation to cancel
     * @throws IllegalStateException if cancellation is not possible or reservation not found
     */
    suspend fun cancelExamReservation(reservation: Esse3ExamReservation) {
        val doc = executeGet(BOOKED_EXAMS_ENTRYPOINT)
        val cancelUrl = getReservationActionUrlOrNull(doc, reservation, "btnCancella")
            ?: throw IllegalStateException("Cannot cancel exam reservation: registration is closed or reservation not found")

        val confirmDoc = executeGet(cancelUrl)

        val errorMsg = confirmDoc.selectFirst(".alert-danger, .errore, #error")?.text()
        if (errorMsg != null && errorMsg.isNotBlank()) {
            throw IllegalStateException(errorMsg)
        }

        val form = confirmDoc.selectFirst("#formCancellazioneAppello") as? FormElement
            ?: throw IllegalStateException("Cannot cancel exam reservation: confirmation form 'formCancellazioneAppello' not found")

        val formUrl = form.attr("action")

        val formParameters = Parameters.build {
            form.formData().forEach {
                append(it.key(), it.value())
            }
        }

        val response = executePostRaw(formUrl, formParameters)
        if (response.status != HttpStatusCode.OK) {
            throw IllegalStateException("Cannot cancel exam reservation: stats code ${response.status.value}")
        }

        val resultDoc = Jsoup.parse(response.bodyAsChannel().toInputStream(), "UTF-8", BASE_URL)
        val resultError = resultDoc.selectFirst(".alert-danger, .errore, #error")?.text()
        if (resultError != null && resultError.contains("Attenzione", ignoreCase = true)) {
            throw IllegalStateException(resultError)
        }
    }

    /**
     * Prints an exam reservation as PDF.
     *
     * @param reservation The reservation to print
     * @return The PDF content as a ByteReadChannel
     * @throws IllegalStateException if print link not found
     */
    suspend fun printExamReservation(reservation: Esse3ExamReservation): ByteReadChannel {
        val doc = executeGet(BOOKED_EXAMS_ENTRYPOINT)

        val printUrl = getReservationActionUrlOrNull(doc, reservation, "btnStampa")
            ?: throw IllegalStateException("Cannot print exam reservation: print link 'btnStampa' not found")

        val response = executeGetRaw(printUrl)
        if (response.status != HttpStatusCode.OK) {
            throw IllegalStateException("Cannot print exam reservation: status code ${response.status.value}")
        }

        return response.bodyAsChannel()
    }

    private fun getReservationActionUrlOrNull(
        doc: Document, reservation: Esse3ExamReservation, buttonId: String
    ): String? {
        val actionLinks = doc.select("a#$buttonId, a[id=$buttonId]")
        for (link in actionLinks) {
            val href = link.attr("href")
            val linkParams = parseQueryString(href.substringAfter("?", ""))
            if (linkParams["APP_ID"] == reservation.sessionId && linkParams["ADSCE_ID"] == reservation.teachingActivityId) {
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
                ?: throw IllegalStateException("Cannot get exam reservations: missing toolbar")
            val printButton = toolbar.selectFirst("#btnStampa")
                ?: throw IllegalStateException("Cannot get exam reservations: missing print button")
            val printButtonHrefParams = parseQueryString(printButton.attr("href").substringAfter("?"))
            val sessionId = printButtonHrefParams["APP_ID"]
                ?: throw IllegalStateException("Cannot get exam reservations: missing session id in print button url")
            val teachingActivityId = printButtonHrefParams["ADSCE_ID"]
                ?: throw IllegalStateException("Cannot get exam reservations: missing teaching activity id in print button url")

            val teachingActivity = reservation.selectFirst("h2")?.text()?.cleanText()
                ?: throw IllegalStateException("Cannot get exam reservations: missing teaching activity")

            val dl = reservation.selectFirst("dl.record-riga")
                ?: throw IllegalStateException("Cannot get exam reservations: missing details list")

            val examDateDt = dl.selectFirst("dt.app-box_dati_data_esame")?.text()?.cleanText()
                ?: throw IllegalStateException("Cannot get exam reservations: missing 'dt.app-box_dati_data_esame' element")
            val examDateTime = parseDateTime(examDateDt)
                ?: throw IllegalStateException("Cannot get exam reservations: cannot parse date '${examDateDt}'")

            val examDateDd = dl.selectFirst("dd.app-box_dati_data_esame")
                ?: throw IllegalStateException("Cannot get exam reservations: missing 'dd.app-box_dati_data_esame' element")
            val examIsPartial = examDateDd.text().contains("Prova parziale", ignoreCase = true)

            val dataMap = dl.select("dt").associate { dt ->
                val key = dt.text().trim().removeSuffix(":").lowercase()
                val value = dt.nextElementSibling()
                key to value
            }

            val description = dataMap["appello"]?.text()?.cleanText()
                ?: throw IllegalStateException("Cannot get exam reservations: missing description")

            val type = if (examIsPartial) {
                Esse3ExamType.Partial
            } else {
                val typeCode = dataMap["tipo prova"]?.text()?.cleanText()
                    ?: throw IllegalStateException("Cannot get exam reservations: missing type")
                Esse3ExamType.fromString(typeCode)
            }

            val examBuilding = dataMap["edificio"]?.text()?.cleanText()
                ?: throw IllegalStateException("Cannot get exam reservations: missing building")

            val examRoom = dataMap["aula"]?.text()?.cleanText()
                ?: throw IllegalStateException("Cannot get exam reservations: missing room")

            val teacherElements =
                dataMap["docenti"] ?: throw IllegalStateException("Cannot get exam reservations: missing teachers")
            val teachers =
                teacherElements.childNodes().asSequence().map { it.nodeValue().cleanText() }.filter { it.isNotBlank() }
                    .toList()

            val reservationNumberText = dataMap["numero iscrizione"]?.nodeValue()?.cleanText()
                ?: throw IllegalStateException("Cannot reserve exam session: missing reservation number")
            val reservationNumberParts = reservationNumberText.split(" su ", limit = 2)
            val reservationNumber = reservationNumberParts[0].trim().toInt()
            val maxReservationsCount = reservationNumberParts[1].trim().toInt()

            val modeText = dataMap["svolgimento esame"]?.nodeValue()?.cleanText()
                ?: throw IllegalStateException("Cannot reserve exam session: missing exam mode")
            val mode = Esse3ExamSessionMode.fromString(modeText)
                ?: throw IllegalStateException("Cannot reserve exam session: invalid mode $modeText")

            val notes = dataMap["note"]?.nodeValue()?.cleanText()

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
                ?: throw IllegalStateException("Cannot get exam reservations: missing caption")

            val headers = table.select("thead tr th").map {
                val node = it.firstChild() ?: it
                node.nodeValue().trim().lowercase()
            }
            val rows = table.select("tbody tr")
            val entries = rows.map { row ->
                val cells = row.select("td")

                val rowMap = headers.zip(cells).toMap()

                val operationDateTime = rowMap["data"]?.text()?.cleanText()?.let { parseDateTime(it) }
                    ?: throw IllegalStateException("Cannot get exam reservations history: missing date")

                val (examDescription, examDate) = rowMap["appello (descrizione - data)"]?.text()?.cleanText()
                    ?.let { it.substringBeforeLast("-") to parseDate(it.substringAfterLast("-")) }
                    ?: throw IllegalStateException("Cannot get exam reservations history: missing session data")

                val operationText = rowMap["operazione"]?.text()?.cleanText()
                    ?: throw IllegalStateException("Cannot get exam reservations history: missing operation")

                val operation = Esse3ReservationOperation.fromString(operationText)
                    ?: throw IllegalStateException("Cannot get exam reservations history: invalid operation '$operationText'")

                val performedBy = rowMap["effettuato da"]?.text()?.cleanText()
                    ?: throw IllegalStateException("Cannot get exam reservations history: missing performed by")

                Esse3ReservationHistoryEntry(
                    operationDateTime = operationDateTime,
                    examDescription = examDescription,
                    examDate = examDate,
                    operation = operation,
                    performedBy = performedBy
                )
            }

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
