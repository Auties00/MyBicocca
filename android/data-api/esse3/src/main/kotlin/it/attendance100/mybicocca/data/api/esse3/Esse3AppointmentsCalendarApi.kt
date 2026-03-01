package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3Appointment
import it.attendance100.mybicocca.data.dto.esse3.Esse3Booking
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExamSessionCalendarTypesList
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExamSessionShiftCalendar
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3ShiftId
import kotlinx.serialization.json.Json

class Esse3AppointmentsCalendarApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/calendario-app-service-v1") {

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

    suspend fun cancelAppointment(
        callId: Long,
        personId: Long
    ) {
        val response = executeDelete("/calendari/appuntamenti/${personId}/${callId}")
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

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
