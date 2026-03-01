package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3ActivitiesPerExamSession
import it.attendance100.mybicocca.data.dto.esse3.Esse3BookingModificationParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3EnrollmentTag
import it.attendance100.mybicocca.data.dto.esse3.Esse3Esse3SystemLogCommitment
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExamSession
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExamSessionCommissionTeacher
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExamSessionEnrollment
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExamSessionEnrollmentParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExamSessionInsert
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExamSessionSession
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExamSessionShift
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExamSessionTeacherAuthorization
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExamSessionWithDetails
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExamType
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3PublicationParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3Session
import it.attendance100.mybicocca.data.dto.esse3.Esse3SharedExam
import it.attendance100.mybicocca.data.dto.esse3.Esse3SharedExamInsert
import it.attendance100.mybicocca.data.dto.esse3.Esse3SharedExamResult
import it.attendance100.mybicocca.data.dto.esse3.Esse3SharedExamSession
import it.attendance100.mybicocca.data.dto.esse3.Esse3ShiftCommissionTeacher
import it.attendance100.mybicocca.data.dto.esse3.Esse3SystemLogEventTestExport
import it.attendance100.mybicocca.data.dto.esse3.Esse3SystemLogExport
import it.attendance100.mybicocca.data.dto.esse3.Esse3SystemLogImport
import it.attendance100.mybicocca.data.dto.esse3.Esse3SystemLogImportResult
import it.attendance100.mybicocca.data.dto.esse3.Esse3SystemLogSessionsExport
import it.attendance100.mybicocca.data.dto.esse3.Esse3TeacherAuthorizations
import it.attendance100.mybicocca.data.dto.esse3.Esse3UpdateExamSession
import it.attendance100.mybicocca.data.dto.esse3.Esse3UpdateResult
import it.attendance100.mybicocca.data.dto.esse3.Esse3UpdateSystemLogCommitments
import kotlinx.serialization.json.Json

class Esse3CalesaApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/calesa-service-v1") {

    suspend fun getLecturerAuthorizations(
        courseOfStudyEnableId: Long? = null,
        courseOfStudyEnableCode: String? = null,
        activityAuthorizationId: Long? = null,
        activityAuthorizationCode: String? = null,
        academicYearOfferAuthorizationId: Int? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3TeacherAuthorizations> {
        return executeJsonGetList<Esse3TeacherAuthorizations>("/abilitazioni", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            courseOfStudyEnableId?.let { parameter("cdsAbilId", it) }
            courseOfStudyEnableCode?.let { parameter("cdsAbilCod", it) }
            activityAuthorizationId?.let { parameter("adAbilId", it) }
            activityAuthorizationCode?.let { parameter("adAbilCod", it) }
            academicYearOfferAuthorizationId?.let { parameter("aaOffAbilId", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getLecturerAuthorizationsByLecturer(
        lecturerId: Long,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3TeacherAuthorizations> {
        return executeJsonGetList<Esse3TeacherAuthorizations>("/abilitazioni/${lecturerId}/", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getExamCallsByLecturerAuthorization(
        lecturerId: Long,
        minCallDate: String,
        maxCallDate: String,
        academicYearCalendarId: Int? = null,
        state: String? = null,
        outcomesInsertionState: String? = null,
        outcomesPublicationState: String? = null,
        minutesState: String? = null,
        recentFlag: Int? = null,
        q: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3ExamSessionTeacherAuthorization> {
        return executeJsonGetList<Esse3ExamSessionTeacherAuthorization>("/abilitazioni/${lecturerId}/appelli", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("minDataApp", minCallDate)
            parameter("maxDataApp", maxCallDate)
            academicYearCalendarId?.let { parameter("aaCalId", it) }
            state?.let { parameter("stato", it) }
            outcomesInsertionState?.let { parameter("statoInsEsiti", it) }
            outcomesPublicationState?.let { parameter("statoPubblEsiti", it) }
            minutesState?.let { parameter("statoVerb", it) }
            recentFlag?.let { parameter("recenteFlg", it) }
            q?.let { parameter("q", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getLecturerAuthorizationsCourseTeachingActivity(
        courseOfStudyId: Long,
        activityId: Long,
        lecturerId: Long,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3TeacherAuthorizations> {
        return executeJsonGetList<Esse3TeacherAuthorizations>("/abilitazioni/${lecturerId}/${courseOfStudyId}/${activityId}/", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getLecturerAuthorization(
        courseOfStudyId: Long,
        activityId: Long,
        lecturerId: Long,
        academicYearOfferTeacherAuthorizationId: Long,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3TeacherAuthorizations {
        return executeJsonGet<Esse3TeacherAuthorizations>("/abilitazioni/${lecturerId}/${courseOfStudyId}/${activityId}/${academicYearOfferTeacherAuthorizationId}", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getActivitiesForExamCalls(
        courseOfStudyEnableId: Long? = null,
        courseOfStudyEnableCode: String? = null,
        activityAuthorizationId: Long? = null,
        activityAuthorizationCode: String? = null,
        academicYearOfferAuthorizationId: Int? = null,
        matricola: String? = null,
        matId: Long? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3ActivitiesPerExamSession> {
        return executeJsonGetList<Esse3ActivitiesPerExamSession>("/appelli", setOf(Esse3PermissionLevel.STUDENT)) {
            courseOfStudyEnableId?.let { parameter("cdsAbilId", it) }
            courseOfStudyEnableCode?.let { parameter("cdsAbilCod", it) }
            activityAuthorizationId?.let { parameter("adAbilId", it) }
            activityAuthorizationCode?.let { parameter("adAbilCod", it) }
            academicYearOfferAuthorizationId?.let { parameter("aaOffAbilId", it) }
            matricola?.let { parameter("matricola", it) }
            matId?.let { parameter("matId", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getExamCalls(
        courseOfStudyId: Long,
        activityId: Long,
        academicYearCalendarId: Int? = null,
        minCallDate: String? = null,
        maxCallDate: String? = null,
        state: String? = null,
        outcomesInsertionState: String? = null,
        outcomesPublicationState: String? = null,
        minutesState: String? = null,
        recentFlag: Int? = null,
        personId: Long? = null,
        actorCode: String? = null,
        q: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3ExamSession> {
        return executeJsonGetList<Esse3ExamSession>("/appelli/${courseOfStudyId}/${activityId}/", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            academicYearCalendarId?.let { parameter("aaCalId", it) }
            minCallDate?.let { parameter("minDataApp", it) }
            maxCallDate?.let { parameter("maxDataApp", it) }
            state?.let { parameter("stato", it) }
            outcomesInsertionState?.let { parameter("statoInsEsiti", it) }
            outcomesPublicationState?.let { parameter("statoPubblEsiti", it) }
            minutesState?.let { parameter("statoVerb", it) }
            recentFlag?.let { parameter("recenteFlg", it) }
            personId?.let { parameter("persId", it) }
            actorCode?.let { parameter("attoreCod", it) }
            q?.let { parameter("q", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun postExamCall(
        courseOfStudyId: Long,
        activityId: Long,
        body: Esse3ExamSessionInsert
    ) {
        val response = executePost("/appelli/${courseOfStudyId}/${activityId}/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getExamCall(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        personId: Long? = null,
        actorCode: String? = null,
        actorCodeExamType: String? = null,
        q: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3ExamSessionWithDetails {
        return executeJsonGet<Esse3ExamSessionWithDetails>("/appelli/${courseOfStudyId}/${activityId}/${callId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            personId?.let { parameter("persId", it) }
            actorCode?.let { parameter("attoreCod", it) }
            actorCodeExamType?.let { parameter("attoreCodTipoSvolgEsame", it) }
            q?.let { parameter("q", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun putExamCall(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        body: Esse3UpdateExamSession,
        actorCodeExamType: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3ExamSessionWithDetails {
        return executeJsonPut<Esse3ExamSessionWithDetails>("/appelli/${courseOfStudyId}/${activityId}/${callId}", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            actorCodeExamType?.let { parameter("attoreCodTipoSvolgEsame", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun deleteExamCall(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        forceCancellation: Long
    ) {
        val response = executeDelete("/appelli/${courseOfStudyId}/${activityId}/${callId}") {
            parameter("forzaCanc", forceCancellation)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getLecturersExamCall(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        order: String? = null,
        fields: String? = null
    ): List<Esse3ExamSessionCommissionTeacher> {
        return executeJsonGetList<Esse3ExamSessionCommissionTeacher>("/appelli/${courseOfStudyId}/${activityId}/${callId}/comm", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    suspend fun getLecturerExamCall(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        lecturerId: Long,
        fields: String? = null
    ): Esse3ExamSessionCommissionTeacher {
        return executeJsonGet<Esse3ExamSessionCommissionTeacher>("/appelli/${courseOfStudyId}/${activityId}/${callId}/comm/${lecturerId}", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
        }
    }

    suspend fun getCommonExamsExamCall(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        order: String? = null,
        fields: String? = null
    ): List<Esse3SharedExamSession> {
        return executeJsonGetList<Esse3SharedExamSession>("/appelli/${courseOfStudyId}/${activityId}/${callId}/esacom", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    suspend fun getExamCallEnrolledList(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        callLogId: Int? = null,
        actorCode: String? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null,
        filter: String? = null
    ): List<Esse3ExamSessionEnrollment> {
        return executeJsonGetList<Esse3ExamSessionEnrollment>("/appelli/${courseOfStudyId}/${activityId}/${callId}/iscritti", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            callLogId?.let { parameter("appLogId", it) }
            actorCode?.let { parameter("attoreCod", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    suspend fun postExamCallEnrolledList(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        body: Esse3ExamSessionEnrollmentParameters
    ) {
        val response = executePost("/appelli/${courseOfStudyId}/${activityId}/${callId}/iscritti") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getEnrolledExamCall(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        studentId: Long,
        actorCode: String? = null,
        q: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3ExamSessionEnrollment {
        return executeJsonGet<Esse3ExamSessionEnrollment>("/appelli/${courseOfStudyId}/${activityId}/${callId}/iscritti/${studentId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            actorCode?.let { parameter("attoreCod", it) }
            q?.let { parameter("q", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun putModifyBooking(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        studentId: Long,
        body: Esse3BookingModificationParameters,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3ExamSessionEnrollment {
        return executeJsonPut<Esse3ExamSessionEnrollment>("/appelli/${courseOfStudyId}/${activityId}/${callId}/iscritti/${studentId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun deleteExamCallEnrolledList(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        studentId: Long
    ) {
        val response = executeDelete("/appelli/${courseOfStudyId}/${activityId}/${callId}/iscritti/${studentId}")
        ensureSuccess(response, setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getPresenceCertificate(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        studentId: Long
    ): String {
        return executeJsonGet<String>("/appelli/${courseOfStudyId}/${activityId}/${callId}/iscritti/${studentId}/attestato-di-presenza", setOf(Esse3PermissionLevel.STUDENT))
    }

    suspend fun putApplicationListOutcome(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        studentId: Long,
        body: Esse3UpdateResult,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3ExamSessionEnrollment {
        return executeJsonPut<Esse3ExamSessionEnrollment>("/appelli/${courseOfStudyId}/${activityId}/${callId}/iscritti/${studentId}/esito", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun putAcknowledgmentOfReceipt(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        studentId: Long,
        body: kotlinx.serialization.json.JsonObject,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3ExamSessionEnrollment {
        return executeJsonPut<Esse3ExamSessionEnrollment>("/appelli/${courseOfStudyId}/${activityId}/${callId}/iscritti/${studentId}/presaVisione", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getBookingStatino(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        studentId: Long
    ): String {
        return executeJsonGet<String>("/appelli/${courseOfStudyId}/${activityId}/${callId}/iscritti/${studentId}/statino-prenotazione", setOf(Esse3PermissionLevel.STUDENT))
    }

    suspend fun putExamCallPublication(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        body: Esse3PublicationParameters
    ) {
        val response = executePut("/appelli/${courseOfStudyId}/${activityId}/${callId}/pubblicazione") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TEACHER))
    }

    suspend fun getExamCallSessions(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        order: String? = null,
        fields: String? = null
    ): List<Esse3ExamSessionSession> {
        return executeJsonGetList<Esse3ExamSessionSession>("/appelli/${courseOfStudyId}/${activityId}/${callId}/sessioni", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    suspend fun getExamCallSession(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        academicYearSessionId: Long,
        sessionId: Long,
        fields: String? = null
    ): Esse3ExamSessionSession {
        return executeJsonGet<Esse3ExamSessionSession>("/appelli/${courseOfStudyId}/${activityId}/${callId}/sessioni/${academicYearSessionId}/${sessionId}", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
        }
    }

    suspend fun getTagsByBooking(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        activityChoiceId: Long,
        order: String? = null
    ): List<Esse3EnrollmentTag> {
        return executeJsonGetList<Esse3EnrollmentTag>("/appelli/${courseOfStudyId}/${activityId}/${callId}/tags/${activityChoiceId}", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getExamCallTypes(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        actorCodeExamType: String? = null,
        order: String? = null,
        fields: String? = null,
        filter: String? = null
    ): List<Esse3ExamType> {
        return executeJsonGetList<Esse3ExamType>("/appelli/${courseOfStudyId}/${activityId}/${callId}/tipi-svolgimento-esame", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            actorCodeExamType?.let { parameter("attoreCodTipoSvolgEsame", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    suspend fun getExamCallShifts(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3ExamSessionShift> {
        return executeJsonGetList<Esse3ExamSessionShift>("/appelli/${courseOfStudyId}/${activityId}/${callId}/turni", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getExamCallShift(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        callLogId: Long,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3ExamSessionShift {
        return executeJsonGet<Esse3ExamSessionShift>("/appelli/${courseOfStudyId}/${activityId}/${callId}/turni/${callLogId}", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getLecturersShift(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        callLogId: Long,
        order: String? = null,
        fields: String? = null
    ): List<Esse3ShiftCommissionTeacher> {
        return executeJsonGetList<Esse3ShiftCommissionTeacher>("/appelli/${courseOfStudyId}/${activityId}/${callId}/turni/${callLogId}/comm", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    suspend fun getLecturerShift(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        callLogId: Long,
        lecturerId: Long,
        fields: String? = null
    ): Esse3ShiftCommissionTeacher {
        return executeJsonGet<Esse3ShiftCommissionTeacher>("/appelli/${courseOfStudyId}/${activityId}/${callId}/turni/${callLogId}/comm/${lecturerId}", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
        }
    }

    suspend fun putShiftPublication(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        callLogId: Long,
        body: Esse3PublicationParameters
    ) {
        val response = executePut("/appelli/${courseOfStudyId}/${activityId}/${callId}/turni/${callLogId}/pubblicazione") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TEACHER))
    }

    suspend fun getEsacomByAcademicYearId(
        academicYearId: Int,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null
    ): List<Esse3SharedExam> {
        return executeJsonGetList<Esse3SharedExam>("/esacom/${academicYearId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    suspend fun getEsacomParent(
        academicYearId: Int,
        courseOfStudyGraduationId: Long,
        activityExamId: Long,
        order: String? = null,
        fields: String? = null
    ): List<Esse3SharedExam> {
        return executeJsonGetList<Esse3SharedExam>("/esacom/${academicYearId}/${courseOfStudyGraduationId}/${activityExamId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    suspend fun getEsacomChild(
        academicYearId: Int,
        courseOfStudyGraduationId: Long,
        activityExamId: Long,
        childCourseOfStudyId: Long,
        childActivityId: Long
    ): Esse3SharedExam {
        return executeJsonGet<Esse3SharedExam>("/esacom/${academicYearId}/${courseOfStudyGraduationId}/${activityExamId}/figli/${childCourseOfStudyId}/${childActivityId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun putEsacomChild(
        academicYearId: Int,
        courseOfStudyGraduationId: Long,
        activityExamId: Long,
        childCourseOfStudyId: Long,
        childActivityId: Long,
        body: Esse3SharedExamInsert,
        forceFlag: Boolean? = null
    ): Esse3SharedExamResult {
        return executeJsonPut<Esse3SharedExamResult>("/esacom/${academicYearId}/${courseOfStudyGraduationId}/${activityExamId}/figli/${childCourseOfStudyId}/${childActivityId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            forceFlag?.let { parameter("forceFlg", it) }
        }
    }

    suspend fun deleteEsacomChild(
        academicYearId: Int,
        courseOfStudyGraduationId: Long,
        activityExamId: Long,
        childCourseOfStudyId: Long,
        childActivityId: Long,
        forceFlag: Boolean? = null
    ): Esse3SharedExamResult {
        return executeJsonDelete<Esse3SharedExamResult>("/esacom/${academicYearId}/${courseOfStudyGraduationId}/${activityExamId}/figli/${childCourseOfStudyId}/${childActivityId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            forceFlag?.let { parameter("forceFlg", it) }
        }
    }

    suspend fun getBookingsByMatId(
        matId: Long,
        actorCode: String? = null,
        q: String? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null,
        filter: String? = null
    ): List<Esse3ExamSessionEnrollment> {
        return executeJsonGetList<Esse3ExamSessionEnrollment>("/prenotazioni/${matId}/", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            actorCode?.let { parameter("attoreCod", it) }
            q?.let { parameter("q", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    suspend fun getBooking(
        matId: Long,
        applicationListId: Long,
        actorCode: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3ExamSessionEnrollment {
        return executeJsonGet<Esse3ExamSessionEnrollment>("/prenotazioni/${matId}/${applicationListId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            actorCode?.let { parameter("attoreCod", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun putAcknowledgmentOfReceiptApplicationList(
        matId: Long,
        applicationListId: Long,
        body: kotlinx.serialization.json.JsonObject,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3ExamSessionEnrollment {
        return executeJsonPut<Esse3ExamSessionEnrollment>("/prenotazioni/${matId}/${applicationListId}/presaVisione", setOf(Esse3PermissionLevel.STUDENT)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getSessions(
        facultyId: Long? = null,
        courseOfStudyId: Long? = null,
        facultyCode: String? = null,
        courseOfStudyCode: String? = null,
        minStartDate: String? = null,
        maxEndDate: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3Session> {
        return executeJsonGetList<Esse3Session>("/sessioni", setOf(Esse3PermissionLevel.ANY)) {
            facultyId?.let { parameter("facId", it) }
            courseOfStudyId?.let { parameter("cdsId", it) }
            facultyCode?.let { parameter("facCod", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            minStartDate?.let { parameter("minDataInizio", it) }
            maxEndDate?.let { parameter("maxDataFine", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getSessionsByAcademicYearSession(
        academicYearSessionId: Long,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3Session> {
        return executeJsonGetList<Esse3Session>("/sessioni/${academicYearSessionId}", setOf(Esse3PermissionLevel.ANY)) {
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getSessionsByAcademicYearSessionAndCourseOfStudy(
        academicYearSessionId: Long,
        courseOfStudyId: Long,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3Session> {
        return executeJsonGetList<Esse3Session>("/sessioni/${academicYearSessionId}/${courseOfStudyId}", setOf(Esse3PermissionLevel.ANY)) {
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun exportSystemLog(
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3SystemLogExport> {
        return executeJsonGetList<Esse3SystemLogExport>("/sistLogExt/export", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun exportSystemLogByProcessingId(
        processingId: Long
    ): Esse3SystemLogExport {
        return executeJsonGet<Esse3SystemLogExport>("/sistLogExt/export/${processingId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun exportSystemLogEvents(
        processingId: Long,
        start: Int? = null,
        limit: Int? = null,
        excludeIdenticalPackages: Boolean? = null
    ): List<Esse3SystemLogEventTestExport> {
        return executeJsonGetList<Esse3SystemLogEventTestExport>("/sistLogExt/export/${processingId}/eventi", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            excludeIdenticalPackages?.let { parameter("escludiPacchettiUguali", it) }
        }
    }

    suspend fun exportSystemLogSessions(
        processingId: Long,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3SystemLogSessionsExport> {
        return executeJsonGetList<Esse3SystemLogSessionsExport>("/sistLogExt/export/${processingId}/sessioni", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    suspend fun getCommitmentsByDate(
        referenceDate: String? = null
    ): List<Esse3Esse3SystemLogCommitment> {
        return executeJsonGetList<Esse3Esse3SystemLogCommitment>("/sistLogExt/impegni", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            referenceDate?.let { parameter("dataRif", it) }
        }
    }

    suspend fun importSystemLog(
        body: Esse3SystemLogImport,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3SystemLogImportResult {
        return executeJsonPut<Esse3SystemLogImportResult>("/sistLogExt/import/", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun updateCommitment(
        body: Esse3UpdateSystemLogCommitments
    ): Esse3SystemLogImportResult {
        return executeJsonPut<Esse3SystemLogImportResult>("/sistLogExt/update/", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}
