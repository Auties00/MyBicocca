package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3ActivitiesToInsert
import it.attendance100.mybicocca.data.dto.esse3.Esse3AttendanceReleaseDetail
import it.attendance100.mybicocca.data.dto.esse3.Esse3BulkAttendanceParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3BulkAttendanceResult
import it.attendance100.mybicocca.data.dto.esse3.Esse3BulkPresenceReleaseParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3CareerPortion
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExamSessionEnrollment
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExamSessionTranscript
import it.attendance100.mybicocca.data.dto.esse3.Esse3PatchTranscriptRow
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3PrerequisitesCheck
import it.attendance100.mybicocca.data.dto.esse3.Esse3RecognitionParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3SingleAttendanceParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3SyllabusActivityTranscript
import it.attendance100.mybicocca.data.dto.esse3.Esse3SyllabusTeachingUnitTranscript
import it.attendance100.mybicocca.data.dto.esse3.Esse3TranscriptAverage
import it.attendance100.mybicocca.data.dto.esse3.Esse3TranscriptPartition
import it.attendance100.mybicocca.data.dto.esse3.Esse3TranscriptRow
import it.attendance100.mybicocca.data.dto.esse3.Esse3TranscriptRowPerActivityLog
import it.attendance100.mybicocca.data.dto.esse3.Esse3TranscriptSegment
import it.attendance100.mybicocca.data.dto.esse3.Esse3TranscriptStats
import it.attendance100.mybicocca.data.dto.esse3.Esse3TranscriptTest
import kotlinx.serialization.json.Json

class Esse3TranscriptApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/libretto-service-v2") {

    suspend fun getCareerSegments(
        matricola: String? = null,
        courseOfStudyStudentId: Long? = null,
        courseOfStudyStudentCode: String? = null,
        academicYearOrderStudentId: Int? = null,
        studyPlanStudentId: Long? = null,
        studyPlanStudentCode: String? = null,
        fiscalCode: String? = null,
        studentStatusCode: String? = null,
        optionalFields: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3CareerPortion> {
        return executeJsonGetList<Esse3CareerPortion>("/libretti", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            matricola?.let { parameter("matricola", it) }
            courseOfStudyStudentId?.let { parameter("cdsStuId", it) }
            courseOfStudyStudentCode?.let { parameter("cdsStuCod", it) }
            academicYearOrderStudentId?.let { parameter("aaOrdStuId", it) }
            studyPlanStudentId?.let { parameter("pdsStuId", it) }
            studyPlanStudentCode?.let { parameter("pdsStuCod", it) }
            fiscalCode?.let { parameter("codFis", it) }
            studentStatusCode?.let { parameter("staStuCod", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getStudentClassByTeachingActivityStudyPlanOrderNew(
        academicYearOfferLogId: Int,
        teachingActivityLogCode: String,
        courseOfStudyLogCode: String,
        academicYearOrderLogId: Long,
        studyPlanLogCode: String,
        teachingUnitFreeCode: String? = null,
        studentStatusCode: String? = null,
        matStatusCode: String? = null,
        supFlag: Boolean? = null,
        domicilePartialCode: String? = null,
        allActivityLogs: Boolean? = null,
        studentId: Long? = null,
        start: Int? = null,
        limit: Int? = null,
        fields: String? = null,
        optionalFields: String? = null,
        order: String? = null
    ): List<Esse3TranscriptRowPerActivityLog> {
        return executeJsonGetList<Esse3TranscriptRowPerActivityLog>("/libretti/classe-studenti", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("logAaOffId", academicYearOfferLogId)
            parameter("logAdCod", teachingActivityLogCode)
            parameter("logCdsCod", courseOfStudyLogCode)
            parameter("logAaOrdId", academicYearOrderLogId)
            parameter("logPdsCod", studyPlanLogCode)
            teachingUnitFreeCode?.let { parameter("libUdCod", it) }
            studentStatusCode?.let { parameter("staStuCod", it) }
            matStatusCode?.let { parameter("staMatCod", it) }
            supFlag?.let { parameter("supFlg", it) }
            domicilePartialCode?.let { parameter("domPartCod", it) }
            allActivityLogs?.let { parameter("allAdLog", it) }
            studentId?.let { parameter("stuId", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getStudentClassByTeachingActivityLogId(
        activityLogId: Long,
        activityCode: String? = null,
        courseOfStudyStudentCode: String? = null,
        studentStatusCode: String? = null,
        matStatusCode: String? = null,
        supFlag: Boolean? = null,
        domicilePartialCode: String? = null,
        studentId: Long? = null,
        start: Int? = null,
        limit: Int? = null,
        fields: String? = null,
        optionalFields: String? = null,
        order: String? = null
    ): List<Esse3TranscriptRowPerActivityLog> {
        return executeJsonGetList<Esse3TranscriptRowPerActivityLog>("/libretti/classe-studenti/${activityLogId}", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            activityCode?.let { parameter("adCod", it) }
            courseOfStudyStudentCode?.let { parameter("cdsStuCod", it) }
            studentStatusCode?.let { parameter("staStuCod", it) }
            matStatusCode?.let { parameter("staMatCod", it) }
            supFlag?.let { parameter("supFlg", it) }
            domicilePartialCode?.let { parameter("domPartCod", it) }
            studentId?.let { parameter("stuId", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun putMassiveAttendance(
        body: Esse3BulkAttendanceParameters
    ): Esse3BulkAttendanceResult {
        return executeJsonPut<Esse3BulkAttendanceResult>("/libretti/freq", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun putMassiveDetections(
        body: Esse3BulkPresenceReleaseParameters
    ): Esse3BulkAttendanceResult {
        return executeJsonPut<Esse3BulkAttendanceResult>("/libretti/rilevazioni-freq/", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun getCareerSegment(
        matId: Long,
        optionalFields: String? = null
    ): Esse3CareerPortion {
        return executeJsonGet<Esse3CareerPortion>("/libretti/${matId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getRecordBookExamCalls(
        matId: Long,
        actorCode: String? = null,
        q: String? = null,
        filter: String? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3ExamSessionTranscript> {
        return executeJsonGetList<Esse3ExamSessionTranscript>("/libretti/${matId}/appelli", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            actorCode?.let { parameter("attoreCod", it) }
            q?.let { parameter("q", it) }
            filter?.let { parameter("filter", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getRecordBookAverages(
        matId: Long,
        initialAveragesReferenceDate: String? = null,
        finalAveragesReferenceDate: String? = null
    ): List<Esse3TranscriptAverage> {
        return executeJsonGetList<Esse3TranscriptAverage>("/libretti/${matId}/medie", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            initialAveragesReferenceDate?.let { parameter("dataRifMedieIni", it) }
            finalAveragesReferenceDate?.let { parameter("dataRifMedieFin", it) }
        }
    }

    suspend fun getRecordBookAverage(
        matId: Long,
        base: String,
        type: String,
        initialAveragesReferenceDate: String? = null,
        finalAveragesReferenceDate: String? = null
    ): List<Esse3TranscriptAverage> {
        return executeJsonGetList<Esse3TranscriptAverage>("/libretti/${matId}/medie/${base}/${type}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            initialAveragesReferenceDate?.let { parameter("dataRifMedieIni", it) }
            finalAveragesReferenceDate?.let { parameter("dataRifMedieFin", it) }
        }
    }

    suspend fun getRecordBookPartitions(
        matId: Long,
        fields: String? = null,
        order: String? = null,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3TranscriptPartition> {
        return executeJsonGetList<Esse3TranscriptPartition>("/libretti/${matId}/partizioni", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    suspend fun getBookingsByMatId(
        matId: Long,
        actorCode: String? = null,
        minimumBookingDate: String? = null,
        q: String? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null,
        filter: String? = null
    ): List<Esse3ExamSessionEnrollment> {
        return executeJsonGetList<Esse3ExamSessionEnrollment>("/libretti/${matId}/prenotazioni", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            actorCode?.let { parameter("attoreCod", it) }
            minimumBookingDate?.let { parameter("dataMinPren", it) }
            q?.let { parameter("q", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    suspend fun getRecordBookTests(
        matId: Long,
        fields: String? = null,
        order: String? = null,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3TranscriptTest> {
        return executeJsonGetList<Esse3TranscriptTest>("/libretti/${matId}/prove", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    suspend fun getRecordBookRows(
        matId: Long,
        activityCode: String? = null,
        nonDeletableActivities: Int? = null,
        optionalFields: String? = null,
        fields: String? = null,
        order: String? = null,
        filter: String? = null
    ): List<Esse3TranscriptRow> {
        return executeJsonGetList<Esse3TranscriptRow>("/libretti/${matId}/righe/", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            activityCode?.let { parameter("adCod", it) }
            nonDeletableActivities?.let { parameter("adNonCancellabili", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    suspend fun postRecordBookRow(
        matId: Long,
        body: Esse3ActivitiesToInsert
    ) {
        val response = executePost("/libretti/${matId}/righe/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getRecordBookRow(
        matId: Long,
        activityChoiceId: Long,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3TranscriptRow {
        return executeJsonGet<Esse3TranscriptRow>("/libretti/${matId}/righe/${activityChoiceId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun deleteRecordBookRow(
        matId: Long,
        activityChoiceId: Long
    ) {
        val response = executeDelete("/libretti/${matId}/righe/${activityChoiceId}")
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun patchRecordBookRow(
        matId: Long,
        activityChoiceId: Long,
        body: Esse3PatchTranscriptRow,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3TranscriptRow {
        return executeJsonPatch<Esse3TranscriptRow>("/libretti/${matId}/righe/${activityChoiceId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getExamCallsByRecordBookRow(
        matId: Long,
        activityChoiceId: Long,
        actorCode: String? = null,
        q: String? = null,
        filter: String? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3ExamSessionTranscript> {
        return executeJsonGetList<Esse3ExamSessionTranscript>("/libretti/${matId}/righe/${activityChoiceId}/appelli", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            actorCode?.let { parameter("attoreCod", it) }
            q?.let { parameter("q", it) }
            filter?.let { parameter("filter", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun setManualAttendance(
        matId: Long,
        activityChoiceId: Long,
        body: Esse3SingleAttendanceParameters,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3TranscriptRow {
        return executeJsonPut<Esse3TranscriptRow>("/libretti/${matId}/righe/${activityChoiceId}/freq", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getRecordBookRowPartitions(
        matId: Long,
        activityChoiceId: Long,
        fields: String? = null,
        order: String? = null
    ): List<Esse3TranscriptPartition> {
        return executeJsonGetList<Esse3TranscriptPartition>("/libretti/${matId}/righe/${activityChoiceId}/partizioni", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getRecordBookRowPartition(
        matId: Long,
        activityChoiceId: Long,
        activityPartitionId: Long
    ): Esse3TranscriptPartition {
        return executeJsonGet<Esse3TranscriptPartition>("/libretti/${matId}/righe/${activityChoiceId}/partizioni/${activityPartitionId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getBookingsByTeachingActivityChoiceId(
        matId: Long,
        activityChoiceId: Long,
        actorCode: String? = null,
        q: String? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null,
        filter: String? = null
    ): List<Esse3ExamSessionEnrollment> {
        return executeJsonGetList<Esse3ExamSessionEnrollment>("/libretti/${matId}/righe/${activityChoiceId}/prenotazioni", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            actorCode?.let { parameter("attoreCod", it) }
            q?.let { parameter("q", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    suspend fun getBookingByTeachingActivityChoiceId(
        matId: Long,
        activityChoiceId: Long,
        applicationListId: Long,
        actorCode: String? = null,
        q: String? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null,
        filter: String? = null
    ): Esse3ExamSessionEnrollment {
        return executeJsonGet<Esse3ExamSessionEnrollment>("/libretti/${matId}/righe/${activityChoiceId}/prenotazioni/${applicationListId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            actorCode?.let { parameter("attoreCod", it) }
            q?.let { parameter("q", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    suspend fun getPresenceCertificateByApplicationListId(
        matId: Long,
        activityChoiceId: Long,
        applicationListId: Long
    ): String {
        return executeJsonGet<String>("/libretti/${matId}/righe/${activityChoiceId}/prenotazioni/${applicationListId}/attestato-di-presenza", setOf(Esse3PermissionLevel.STUDENT))
    }

    suspend fun getBookingStatinoByApplicationListId(
        matId: Long,
        activityChoiceId: Long,
        applicationListId: Long
    ): String {
        return executeJsonGet<String>("/libretti/${matId}/righe/${activityChoiceId}/prenotazioni/${applicationListId}/statino-prenotazione", setOf(Esse3PermissionLevel.STUDENT))
    }

    suspend fun getCheckProposalRecordBookRow(
        matId: Long,
        activityChoiceId: Long,
        referenceDate: String? = null
    ): Esse3PrerequisitesCheck {
        return executeJsonGet<Esse3PrerequisitesCheck>("/libretti/${matId}/righe/${activityChoiceId}/prop", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            referenceDate?.let { parameter("dataRif", it) }
        }
    }

    suspend fun getRecordBookRowTests(
        matId: Long,
        activityChoiceId: Long,
        fields: String? = null,
        order: String? = null
    ): List<Esse3TranscriptTest> {
        return executeJsonGetList<Esse3TranscriptTest>("/libretti/${matId}/righe/${activityChoiceId}/prove", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getRecordBookRowTest(
        matId: Long,
        activityChoiceId: Long,
        activityRegulationId: Long
    ): Esse3TranscriptTest {
        return executeJsonGet<Esse3TranscriptTest>("/libretti/${matId}/righe/${activityChoiceId}/prove/${activityRegulationId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER))
    }

    suspend fun putRecordBookRowRecognition(
        matId: Long,
        activityChoiceId: Long,
        body: Esse3RecognitionParameters,
        optionalFields: String? = null,
        type: String? = null
    ): Esse3TranscriptRow {
        return executeJsonPut<Esse3TranscriptRow>("/libretti/${matId}/righe/${activityChoiceId}/riconoscimento", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            optionalFields?.let { parameter("optionalFields", it) }
            type?.let { parameter("type", it) }
        }
    }

    suspend fun deleteRecordBookRecognitionRow(
        matId: Long,
        activityChoiceId: Long,
        optionalFields: String? = null
    ): Esse3TranscriptRow {
        return executeJsonDelete<Esse3TranscriptRow>("/libretti/${matId}/righe/${activityChoiceId}/riconoscimento", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getRecordBookRowDetections(
        matId: Long,
        activityChoiceId: Long,
        choiceReleaseId: Long,
        order: String? = null,
        filter: String? = null
    ): List<Esse3AttendanceReleaseDetail> {
        return executeJsonGetList<Esse3AttendanceReleaseDetail>("/libretti/${matId}/righe/${activityChoiceId}/rilevazioni-in-aula/${choiceReleaseId}/eventi", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            order?.let { parameter("order", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    suspend fun getRecordBookRowSegments(
        matId: Long,
        activityChoiceId: Long,
        fields: String? = null,
        order: String? = null
    ): List<Esse3TranscriptSegment> {
        return executeJsonGetList<Esse3TranscriptSegment>("/libretti/${matId}/righe/${activityChoiceId}/segmenti", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getRecordBookRowSegment(
        matId: Long,
        activityChoiceId: Long,
        segmentChoiceId: Long
    ): Esse3TranscriptSegment {
        return executeJsonGet<Esse3TranscriptSegment>("/libretti/${matId}/righe/${activityChoiceId}/segmenti/${segmentChoiceId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getSyllabusTeachingActivityRecordBookRow(
        matId: Long,
        activityChoiceId: Long,
        fields: String? = null,
        order: String? = null
    ): List<Esse3SyllabusActivityTranscript> {
        return executeJsonGetList<Esse3SyllabusActivityTranscript>("/libretti/${matId}/righe/${activityChoiceId}/syllabus/AD", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getSyllabusTeachingUnitRecordBookRow(
        matId: Long,
        activityChoiceId: Long,
        fields: String? = null,
        order: String? = null
    ): List<Esse3SyllabusTeachingUnitTranscript> {
        return executeJsonGetList<Esse3SyllabusTeachingUnitTranscript>("/libretti/${matId}/righe/${activityChoiceId}/syllabus/UD", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getRecordBookSegments(
        matId: Long,
        nonDeletableActivities: Int? = null,
        fields: String? = null,
        order: String? = null,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3TranscriptSegment> {
        return executeJsonGetList<Esse3TranscriptSegment>("/libretti/${matId}/segmenti", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            nonDeletableActivities?.let { parameter("adNonCancellabili", it) }
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    suspend fun getRecordBookStats(
        matId: Long,
        optionalFields: String? = null,
        initialAveragesReferenceDate: String? = null,
        finalAveragesReferenceDate: String? = null,
        pathRulesReason: String? = null,
        dataOriginReason: String? = null
    ): Esse3TranscriptStats {
        return executeJsonGet<Esse3TranscriptStats>("/libretti/${matId}/stats", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            optionalFields?.let { parameter("optionalFields", it) }
            initialAveragesReferenceDate?.let { parameter("dataRifMedieIni", it) }
            finalAveragesReferenceDate?.let { parameter("dataRifMedieFin", it) }
            pathRulesReason?.let { parameter("motRegolePercorso", it) }
            dataOriginReason?.let { parameter("motOrigineDati", it) }
        }
    }
}
