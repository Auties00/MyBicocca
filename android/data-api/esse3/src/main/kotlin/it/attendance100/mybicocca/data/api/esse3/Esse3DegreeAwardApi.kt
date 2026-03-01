package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3AntiplagiarismDataInsert
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExamSession
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExamSessionLocation
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExternalSubject
import it.attendance100.mybicocca.data.dto.esse3.Esse3ImportSupervisorsResponse
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3SupervisorTeacher
import it.attendance100.mybicocca.data.dto.esse3.Esse3SupervisorTypeRegulation
import it.attendance100.mybicocca.data.dto.esse3.Esse3SupervisorsInsert
import it.attendance100.mybicocca.data.dto.esse3.Esse3TeachingDomainCancellation
import it.attendance100.mybicocca.data.dto.esse3.Esse3TeachingDomainInsert
import it.attendance100.mybicocca.data.dto.esse3.Esse3TeachingDomainSummary
import it.attendance100.mybicocca.data.dto.esse3.Esse3ThesisAttachmentInsertMetadata
import it.attendance100.mybicocca.data.dto.esse3.Esse3ThesisConsultationMode
import it.attendance100.mybicocca.data.dto.esse3.Esse3ThesisDiscussionModeCodeInsert
import it.attendance100.mybicocca.data.dto.esse3.Esse3ThesisIntoTeachingDomainInsert
import it.attendance100.mybicocca.data.dto.esse3.Esse3ThesisSummary
import it.attendance100.mybicocca.data.dto.esse3.Esse3ThesisSupervisorsDepartments
import it.attendance100.mybicocca.data.dto.esse3.Esse3ThesisTeachingDomainSummary
import it.attendance100.mybicocca.data.dto.esse3.Esse3ThesisTypes
import kotlinx.serialization.json.Json

class Esse3DegreeAwardApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/consTit-service-v1") {

    suspend fun postThesisAttachmentMetadata(
        body: Esse3ThesisAttachmentInsertMetadata
    ) {
        val response = executePost("/allegati/tesi/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun putAntiplagiarismData(
        attachmentId: Long? = null,
        antiplagiarismIndex: String? = null,
        antiplagiarismLink: String? = null,
        antiplagiarismNote: String? = null,
        attachmentApprovalFlag: Long? = null
    ): Esse3AntiplagiarismDataInsert {
        return executeJsonPut<Esse3AntiplagiarismDataInsert>("/allegati/tesi/antiplagio", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            attachmentId?.let { parameter("allegatoId", it) }
            antiplagiarismIndex?.let { parameter("indxAntiplagio", it) }
            antiplagiarismLink?.let { parameter("linkAntiplagio", it) }
            antiplagiarismNote?.let { parameter("notaAntiplagio", it) }
            attachmentApprovalFlag?.let { parameter("approvazioneAllegFlg", it) }
        }
    }

    suspend fun getExamCalls(
        courseTypeCode: String? = null,
        courseOfStudyCode: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3ExamSession> {
        return executeJsonGetList<Esse3ExamSession>("/appelliCt", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getCommitteeCall(
        callCommitteeId: Long
    ): List<Esse3ExamSession> {
        return executeJsonGetList<Esse3ExamSession>("/appelliCt/${callCommitteeId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun postCommitteeApplication(
        body: Esse3TeachingDomainInsert
    ) {
        val response = executePost("/domandeCt") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun putCancelCommitteeApplication(
        domicileTitleDeliveryId: Long? = null,
        studentId: Long? = null,
        deadlinesCheck: Boolean? = null
    ): Esse3TeachingDomainCancellation {
        return executeJsonPut<Esse3TeachingDomainCancellation>("/domandeCt/annullaDomandaCt", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            domicileTitleDeliveryId?.let { parameter("domConsTitId", it) }
            studentId?.let { parameter("studId", it) }
            deadlinesCheck?.let { parameter("checkScadenze", it) }
        }
    }

    suspend fun getThesesByCommitteeCallId(
        callCommitteeId: Long,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3ThesisTeachingDomainSummary> {
        return executeJsonGetList<Esse3ThesisTeachingDomainSummary>("/domandeCt/appCtId/${callCommitteeId}/tesi", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    suspend fun getCommitteeApplicationByMatId(
        matId: Long
    ): List<Esse3TeachingDomainSummary> {
        return executeJsonGetList<Esse3TeachingDomainSummary>("/domandeCt/matId/${matId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getCommitteeApplicationByStudentId(
        studentId: Long
    ): List<Esse3TeachingDomainSummary> {
        return executeJsonGetList<Esse3TeachingDomainSummary>("/domandeCt/stuId/${studentId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun postThesisIntoCommitteeApplication(
        body: Esse3ThesisIntoTeachingDomainInsert
    ) {
        val response = executePost("/domandeCt/tesi") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getCommitteeApplication(
        domicileCommitteeId: Long
    ): Esse3TeachingDomainSummary {
        return executeJsonGet<Esse3TeachingDomainSummary>("/domandeCt/${domicileCommitteeId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getTheses(
        domicileCommitteeId: Long
    ): Esse3ThesisTeachingDomainSummary {
        return executeJsonGet<Esse3ThesisTeachingDomainSummary>("/domandeCt/${domicileCommitteeId}/tesi", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getThesisDiscussionMode(
        authorizationFlag: Long? = null
    ): List<Esse3ThesisConsultationMode> {
        return executeJsonGetList<Esse3ThesisConsultationMode>("/modConsTesi", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            authorizationFlag?.let { parameter("abilFlg", it) }
        }
    }

    suspend fun getThesisRelatedDocuments(
        surname: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3SupervisorTeacher> {
        return executeJsonGetList<Esse3SupervisorTeacher>("/relatori/docenti", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            surname?.let { parameter("cognome", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getExternalSubject(
        surname: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3ExternalSubject> {
        return executeJsonGetList<Esse3ExternalSubject>("/relatori/soggEst/", setOf(Esse3PermissionLevel.STUDENT)) {
            surname?.let { parameter("cognome", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getThesisSupervisorsReport(
        departmentCode: String,
        startDate: String,
        endDate: String,
        lecturerId: Long? = null,
        externalSubjectId: Long? = null,
        relationTypeCode: String? = null
    ): Esse3ThesisSupervisorsDepartments {
        return executeJsonGet<Esse3ThesisSupervisorsDepartments>("/report/tesi/relatori/${departmentCode}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("dataIni", startDate)
            parameter("dataFine", endDate)
            lecturerId?.let { parameter("docenteId", it) }
            externalSubjectId?.let { parameter("soggEstId", it) }
            relationTypeCode?.let { parameter("tipoRelCod", it) }
        }
    }

    suspend fun getCommitteeCallSession(
        academicYearId: Long? = null,
        department: String? = null
    ): List<Esse3ExamSessionLocation> {
        return executeJsonGetList<Esse3ExamSessionLocation>("/sedAppCt", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            academicYearId?.let { parameter("aaId", it) }
            department?.let { parameter("dipartimento", it) }
        }
    }

    suspend fun getThesisByThesisId(
        thesisId: Long
    ): Esse3ThesisSummary {
        return executeJsonGet<Esse3ThesisSummary>("/tesi/${thesisId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun putThesisDiscussionMode(
        thesisId: Long,
        thesisDiscussionModeCode: String
    ): Esse3ThesisDiscussionModeCodeInsert {
        return executeJsonPut<Esse3ThesisDiscussionModeCodeInsert>("/tesi/${thesisId}/modConsTesi", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("modConsTesiCod", thesisDiscussionModeCode)
        }
    }

    suspend fun putThesisRelation(
        thesisId: Long,
        body: List<Esse3SupervisorsInsert>
    ): Esse3ImportSupervisorsResponse {
        return executeJsonPut<Esse3ImportSupervisorsResponse>("/tesi/${thesisId}/relatori", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun getThesisRelationTypes(
        studentId: Long
    ): List<Esse3SupervisorTypeRegulation> {
        return executeJsonGetList<Esse3SupervisorTypeRegulation>("/tipiRelTesi/${studentId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getThesisType(
        committeeRegulationId: Long? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3ThesisTypes> {
        return executeJsonGetList<Esse3ThesisTypes>("/tipiTesiStu", setOf(Esse3PermissionLevel.STUDENT)) {
            committeeRegulationId?.let { parameter("regCtId", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }
}
