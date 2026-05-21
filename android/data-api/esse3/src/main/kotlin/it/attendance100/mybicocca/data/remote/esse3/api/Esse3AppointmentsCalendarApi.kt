package it.attendance100.mybicocca.data.remote.esse3.api

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Appointment
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Booking
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExamSessionCalendarTypesList
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExamSessionShiftCalendar
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PermissionLevel
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ShiftId
import kotlinx.serialization.json.Json

class Esse3AppointmentsCalendarApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/calendario-app-service-v1") {

    /**
     * Prenota un appuntamento
     *
     * @param personId Identificativo persona
     * @param body Parametri
     */
    suspend fun insertAppointment(
        personId: Long,
        body: Esse3Appointment
    ) {
        val response = executePost("/calendari/appuntamenti/${personId}") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Modifica una prenotazione
     *
     * @param callId Identificativo dell'appuntamento da eliminare
     * @param personId Identificativo persona
     * @param body turnoId      Identificativo turno da prenotare nota         Nota notaId       Identificativo nota
     */
    suspend fun updateAppointment(
        callId: Long,
        personId: Long,
        body: Esse3Appointment
    ): Esse3ShiftId {
        return executeJsonPut<Esse3ShiftId>("/calendari/appuntamenti/${personId}/${callId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * Annulla una prenotazione
     *
     * @param callId Identificativo dell'appuntamento da eliminare
     * @param personId Identificativo persona
     */
    suspend fun cancelAppointment(
        callId: Long,
        personId: Long
    ) {
        val response = executeDelete("/calendari/appuntamenti/${personId}/${callId}")
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Lista calendari
     *
     * @param calendarContext Contesto del calendario
     * @param language Codice della lingua
     * @param siteId Identificativo della sede
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getCalendarList(
        calendarContext: String,
        language: String? = null,
        siteId: Long? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3ExamSessionCalendarTypesList> {
        return executeJsonGetList<Esse3ExamSessionCalendarTypesList>("/calendari/contesti/${calendarContext}", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.AUTHENTICATED_USER)) {
            language?.let { parameter("lingua", it) }
            siteId?.let { parameter("sedeId", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * Lista prenotazioni di una persona
     *
     * @param calendarContext Contesto del calendario
     * @param personId Identificativo persona
     * @param language Codice della lingua
     * @param studentId Identificativo dello studente.
     * @param courseOfStudyId Identificativo del corso di studio
     * @param academicYearOrderId Identificativo dell'ordinamento di corso di studio
     * @param studyPlanId Identificativo del percorso di studio
     * @param academicYearEnrollmentId Identificativo dell'anno d'iscrizione.
     * @param enrollmentId Identificativo dell'iscrizione
     * @param siteId Identificativo della sede
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getBookingsList(
        calendarContext: String,
        personId: Long,
        language: String? = null,
        studentId: Long? = null,
        courseOfStudyId: Long? = null,
        academicYearOrderId: Long? = null,
        studyPlanId: Long? = null,
        academicYearEnrollmentId: Long? = null,
        enrollmentId: Long? = null,
        siteId: Long? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3Booking> {
        return executeJsonGetList<Esse3Booking>("/calendari/contesti/${calendarContext}/appuntamenti/${personId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            language?.let { parameter("lingua", it) }
            studentId?.let { parameter("stuId", it) }
            courseOfStudyId?.let { parameter("cdsId", it) }
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanId?.let { parameter("pdsId", it) }
            academicYearEnrollmentId?.let { parameter("aaIscrId", it) }
            enrollmentId?.let { parameter("iscrId", it) }
            siteId?.let { parameter("sedeId", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * Lista turni
     *
     * @param calendarTypeCode Codice tipologia calendario
     * @param language Codice della lingua
     * @param siteId Identificativo della sede
     * @param administrativeStructureId Identificativo della struttura amministrativa
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getShiftsList(
        calendarTypeCode: String,
        language: String? = null,
        siteId: Long? = null,
        administrativeStructureId: Long? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3ExamSessionShiftCalendar> {
        return executeJsonGetList<Esse3ExamSessionShiftCalendar>("/calendari/turni/${calendarTypeCode}", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.AUTHENTICATED_USER)) {
            language?.let { parameter("lingua", it) }
            siteId?.let { parameter("sedeId", it) }
            administrativeStructureId?.let { parameter("StrutAmmId", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }
}
