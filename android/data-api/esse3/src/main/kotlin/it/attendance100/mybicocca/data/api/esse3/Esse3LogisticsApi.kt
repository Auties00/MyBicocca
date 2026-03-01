package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3ActivityLog
import it.attendance100.mybicocca.data.dto.esse3.Esse3ActivityLogWithSyllabus
import it.attendance100.mybicocca.data.dto.esse3.Esse3Building
import it.attendance100.mybicocca.data.dto.esse3.Esse3Classroom
import it.attendance100.mybicocca.data.dto.esse3.Esse3CoverageDeletable
import it.attendance100.mybicocca.data.dto.esse3.Esse3DeletedLogistics
import it.attendance100.mybicocca.data.dto.esse3.Esse3EasystaffActivityLogWithDetails
import it.attendance100.mybicocca.data.dto.esse3.Esse3EasystaffCourseOrderWithDetails
import it.attendance100.mybicocca.data.dto.esse3.Esse3LogisticsPerTeacher
import it.attendance100.mybicocca.data.dto.esse3.Esse3LogisticsWithDetails
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3SyllabusActivityPatch
import it.attendance100.mybicocca.data.dto.esse3.Esse3SyllabusActivityPatchResult
import it.attendance100.mybicocca.data.dto.esse3.Esse3SystemLogImportResult
import it.attendance100.mybicocca.data.dto.esse3.Esse3TeachingUnitLogWithDetails
import kotlinx.serialization.json.Json

class Esse3LogisticsApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/logistica-service-v1") {

    suspend fun getEasystaffLogistics(
        academicYearOfferId: Int,
        departmentCode: String? = null,
        courseOfStudyList: String? = null,
        optionalFields: String? = null
    ): List<Esse3EasystaffActivityLogWithDetails> {
        return executeJsonGetList<Esse3EasystaffActivityLogWithDetails>("/easystaff/logistica/${academicYearOfferId}", setOf(Esse3PermissionLevel.ANY)) {
            departmentCode?.let { parameter("dipCod", it) }
            courseOfStudyList?.let { parameter("listaCds", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getEasystaffStructure(
        academicYearOfferId: Int,
        departmentCode: String? = null,
        courseOfStudyList: String? = null,
        optionalFields: String? = null
    ): List<Esse3EasystaffCourseOrderWithDetails> {
        return executeJsonGetList<Esse3EasystaffCourseOrderWithDetails>("/easystaff/struttura/${academicYearOfferId}", setOf(Esse3PermissionLevel.ANY)) {
            departmentCode?.let { parameter("dipCod", it) }
            courseOfStudyList?.let { parameter("listaCds", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getLogistics(
        academicYearOfferId: Int? = null,
        courseOfStudyCode: String? = null,
        courseOfStudyDescription: String? = null,
        academicYearOrderId: Int? = null,
        studyPlanId: Long? = null,
        studyPlanCode: String? = null,
        studyPlanDescription: String? = null,
        activityId: Long? = null,
        activityCode: String? = null,
        activityDescription: String? = null,
        teachingLanguageCode: String? = null,
        siteDescription: String? = null,
        logModificationDate: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3ActivityLog> {
        return executeJsonGetList<Esse3ActivityLog>("/logistica", setOf(Esse3PermissionLevel.ANY)) {
            academicYearOfferId?.let { parameter("aaOffId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseOfStudyDescription?.let { parameter("cdsDes", it) }
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanId?.let { parameter("pdsId", it) }
            studyPlanCode?.let { parameter("pdsCod", it) }
            studyPlanDescription?.let { parameter("pdsDes", it) }
            activityId?.let { parameter("adId", it) }
            activityCode?.let { parameter("adCod", it) }
            activityDescription?.let { parameter("adDes", it) }
            teachingLanguageCode?.let { parameter("linguaDidCod", it) }
            siteDescription?.let { parameter("sedeDes", it) }
            logModificationDate?.let { parameter("dataModLog", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getCancellableCoverage(
        coverageId: Long? = null,
        matricola: String? = null,
        academicYearOfferId: Int? = null,
        courseOfStudyCode: String? = null,
        academicYearOrderId: Int? = null,
        studyPlanCode: String? = null,
        activityCode: String? = null,
        teachingUnitCode: String? = null,
        domicilePartialCode: String? = null
    ): Esse3CoverageDeletable {
        return executeJsonGet<Esse3CoverageDeletable>("/logistica/copertura/cancellabile", setOf(Esse3PermissionLevel.ANY)) {
            coverageId?.let { parameter("coperId", it) }
            matricola?.let { parameter("matricola", it) }
            academicYearOfferId?.let { parameter("aaOffId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanCode?.let { parameter("pdsCod", it) }
            activityCode?.let { parameter("adCod", it) }
            teachingUnitCode?.let { parameter("udCod", it) }
            domicilePartialCode?.let { parameter("domPartCod", it) }
        }
    }

    suspend fun putSyllabusTeachingActivity(
        body: Esse3SyllabusActivityPatch,
        lecturerMatricola: String? = null
    ): Esse3SyllabusActivityPatchResult {
        return executeJsonPut<Esse3SyllabusActivityPatchResult>("/logistica/syllabusAD", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            lecturerMatricola?.let { parameter("docenteMatricola", it) }
        }
    }

    suspend fun getTeachingActivityLogWithSyllabus(
        activityLogId: Long,
        academicYearOfferId: Int? = null,
        courseOfStudyCode: String? = null,
        courseOfStudyDescription: String? = null,
        academicYearOrderId: Int? = null,
        studyPlanId: Long? = null,
        studyPlanCode: String? = null,
        studyPlanDescription: String? = null,
        activityCode: String? = null,
        activityDescription: String? = null,
        order: String? = null,
        optionalFields: String? = null
    ): List<Esse3ActivityLogWithSyllabus> {
        return executeJsonGetList<Esse3ActivityLogWithSyllabus>("/logistica/${activityLogId}/adLogConSyllabus", setOf(Esse3PermissionLevel.ANY)) {
            academicYearOfferId?.let { parameter("aaOffId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseOfStudyDescription?.let { parameter("cdsDes", it) }
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanId?.let { parameter("pdsId", it) }
            studyPlanCode?.let { parameter("pdsCod", it) }
            studyPlanDescription?.let { parameter("pdsDes", it) }
            activityCode?.let { parameter("adCod", it) }
            activityDescription?.let { parameter("adDes", it) }
            order?.let { parameter("order", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getTeachingUnitLogWithDetails(
        activityLogId: Long,
        academicYearOfferId: Int? = null,
        courseOfStudyCode: String? = null,
        courseOfStudyDescription: String? = null,
        academicYearOrderId: Int? = null,
        studyPlanId: Long? = null,
        studyPlanCode: String? = null,
        studyPlanDescription: String? = null,
        activityCode: String? = null,
        activityDescription: String? = null,
        teachingUnitCode: String? = null,
        teachingUnitDescription: String? = null,
        lecturerMatricola: String? = null,
        lecturerSurname: String? = null,
        lecturerName: String? = null,
        order: String? = null,
        optionalFields: String? = null
    ): List<Esse3TeachingUnitLogWithDetails> {
        return executeJsonGetList<Esse3TeachingUnitLogWithDetails>("/logistica/${activityLogId}/udLogConDettagli", setOf(Esse3PermissionLevel.ANY)) {
            academicYearOfferId?.let { parameter("aaOffId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseOfStudyDescription?.let { parameter("cdsDes", it) }
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanId?.let { parameter("pdsId", it) }
            studyPlanCode?.let { parameter("pdsCod", it) }
            studyPlanDescription?.let { parameter("pdsDes", it) }
            activityCode?.let { parameter("adCod", it) }
            activityDescription?.let { parameter("adDes", it) }
            teachingUnitCode?.let { parameter("udCod", it) }
            teachingUnitDescription?.let { parameter("udDes", it) }
            lecturerMatricola?.let { parameter("docenteMatricola", it) }
            lecturerSurname?.let { parameter("docenteCognome", it) }
            lecturerName?.let { parameter("docenteNome", it) }
            order?.let { parameter("order", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getFullLogistics(
        activityLogId: Long,
        academicYearOfferId: Int? = null,
        courseOfStudyCode: String? = null,
        courseOfStudyDescription: String? = null,
        academicYearOrderId: Int? = null,
        studyPlanId: Long? = null,
        studyPlanCode: String? = null,
        studyPlanDescription: String? = null,
        activityCode: String? = null,
        activityDescription: String? = null,
        order: String? = null,
        optionalFields: String? = null
    ): List<Esse3LogisticsWithDetails> {
        return executeJsonGetList<Esse3LogisticsWithDetails>("/logisticaFull/${activityLogId}/", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            academicYearOfferId?.let { parameter("aaOffId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseOfStudyDescription?.let { parameter("cdsDes", it) }
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanId?.let { parameter("pdsId", it) }
            studyPlanCode?.let { parameter("pdsCod", it) }
            studyPlanDescription?.let { parameter("pdsDes", it) }
            activityCode?.let { parameter("adCod", it) }
            activityDescription?.let { parameter("adDes", it) }
            order?.let { parameter("order", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getFullLogisticsByTeachingActivity(
        academicYearOfferId: Int,
        courseOfStudyId: Long? = null,
        courseOfStudyCode: String? = null,
        courseOfStudyDescription: String? = null,
        academicYearOrderId: Int? = null,
        studyPlanId: Long? = null,
        studyPlanCode: String? = null,
        studyPlanDescription: String? = null,
        activityCode: String? = null,
        activityDescription: String? = null,
        logModificationDate: String? = null,
        teachingActivityPublicationFlag: Int? = null,
        order: String? = null,
        optionalFields: String? = null
    ): List<Esse3LogisticsWithDetails> {
        return executeJsonGetList<Esse3LogisticsWithDetails>("/logisticaPerAdFull/${academicYearOfferId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            courseOfStudyId?.let { parameter("cdsId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseOfStudyDescription?.let { parameter("cdsDes", it) }
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanId?.let { parameter("pdsId", it) }
            studyPlanCode?.let { parameter("pdsCod", it) }
            studyPlanDescription?.let { parameter("pdsDes", it) }
            activityCode?.let { parameter("adCod", it) }
            activityDescription?.let { parameter("adDes", it) }
            logModificationDate?.let { parameter("dataModLog", it) }
            teachingActivityPublicationFlag?.let { parameter("desAdPubblFlg", it) }
            order?.let { parameter("order", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getLogisticsByLecturer(
        lecturerId: Long? = null,
        lecturerMatricola: String? = null,
        lecturerSurname: String? = null,
        userId: String? = null,
        academicYearOfferId: Int? = null,
        activityLogId: Long? = null,
        tcDocumentModificationDate: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3LogisticsPerTeacher> {
        return executeJsonGetList<Esse3LogisticsPerTeacher>("/logisticaPerDocente", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            lecturerId?.let { parameter("docenteId", it) }
            lecturerMatricola?.let { parameter("docenteMatricola", it) }
            lecturerSurname?.let { parameter("docenteCognome", it) }
            userId?.let { parameter("userId", it) }
            academicYearOfferId?.let { parameter("aaOffId", it) }
            activityLogId?.let { parameter("adLogId", it) }
            tcDocumentModificationDate?.let { parameter("dataModTCDoc", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getFullLogisticsByOdStreamed(
        academicYearOfferId: Int,
        courseOfStudyOfferId: Long,
        courseOfStudyCode: String? = null,
        courseOfStudyDescription: String? = null,
        academicYearOrderId: Int? = null,
        studyPlanId: Long? = null,
        studyPlanCode: String? = null,
        studyPlanDescription: String? = null,
        activityCode: String? = null,
        activityDescription: String? = null,
        order: String? = null,
        optionalFields: String? = null
    ): List<Esse3LogisticsWithDetails> {
        return executeJsonGetList<Esse3LogisticsWithDetails>("/logisticaPerOdFull/streamed/${academicYearOfferId}/${courseOfStudyOfferId}/", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseOfStudyDescription?.let { parameter("cdsDes", it) }
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanId?.let { parameter("pdsId", it) }
            studyPlanCode?.let { parameter("pdsCod", it) }
            studyPlanDescription?.let { parameter("pdsDes", it) }
            activityCode?.let { parameter("adCod", it) }
            activityDescription?.let { parameter("adDes", it) }
            order?.let { parameter("order", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getFullLogisticsByOd(
        academicYearOfferId: Int,
        courseOfStudyOfferId: Long,
        courseOfStudyCode: String? = null,
        courseOfStudyDescription: String? = null,
        academicYearOrderId: Int? = null,
        studyPlanId: Long? = null,
        studyPlanCode: String? = null,
        studyPlanDescription: String? = null,
        activityCode: String? = null,
        activityDescription: String? = null,
        order: String? = null,
        optionalFields: String? = null
    ): List<Esse3LogisticsWithDetails> {
        return executeJsonGetList<Esse3LogisticsWithDetails>("/logisticaPerOdFull/${academicYearOfferId}/${courseOfStudyOfferId}/", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseOfStudyDescription?.let { parameter("cdsDes", it) }
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanId?.let { parameter("pdsId", it) }
            studyPlanCode?.let { parameter("pdsCod", it) }
            studyPlanDescription?.let { parameter("pdsDes", it) }
            activityCode?.let { parameter("adCod", it) }
            activityDescription?.let { parameter("adDes", it) }
            order?.let { parameter("order", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getDeletedLogistics(
        logModificationDate: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3DeletedLogistics> {
        return executeJsonGetList<Esse3DeletedLogistics>("/logisticheEliminate", setOf(Esse3PermissionLevel.ANY)) {
            logModificationDate?.let { parameter("dataModLog", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getAllClassrooms(
        classroomCode: String? = null,
        externalClassroomCode: String? = null,
        optionalFields: String? = null,
        fields: String? = null,
        order: String? = null
    ): List<Esse3Classroom> {
        return executeJsonGetList<Esse3Classroom>("/risFisse/aule", setOf(Esse3PermissionLevel.ANY)) {
            classroomCode?.let { parameter("aulaCod", it) }
            externalClassroomCode?.let { parameter("extAulaCod", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getBuildings(
        filter: String? = null,
        optionalFields: String? = null,
        fields: String? = null,
        order: String? = null
    ): List<Esse3Building> {
        return executeJsonGetList<Esse3Building>("/risFisse/edifici", setOf(Esse3PermissionLevel.ANY)) {
            filter?.let { parameter("filter", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getBuilding(
        buildingId: Long,
        optionalFields: String? = null,
        fields: String? = null
    ): Esse3Building {
        return executeJsonGet<Esse3Building>("/risFisse/edifici/${buildingId}", setOf(Esse3PermissionLevel.ANY)) {
            optionalFields?.let { parameter("optionalFields", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    suspend fun getClassrooms(
        buildingId: Long,
        filter: String? = null,
        optionalFields: String? = null,
        fields: String? = null,
        order: String? = null
    ): List<Esse3Classroom> {
        return executeJsonGetList<Esse3Classroom>("/risFisse/edifici/${buildingId}/aule", setOf(Esse3PermissionLevel.ANY)) {
            filter?.let { parameter("filter", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getClassroom(
        classroomId: Long,
        buildingId: Long,
        optionalFields: String? = null,
        fields: String? = null
    ): List<Esse3Classroom> {
        return executeJsonGetList<Esse3Classroom>("/risFisse/edifici/${buildingId}/aule/${classroomId}", setOf(Esse3PermissionLevel.ANY)) {
            optionalFields?.let { parameter("optionalFields", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    suspend fun syncSystemLog(): Esse3SystemLogImportResult {
        return executeJsonPut<Esse3SystemLogImportResult>("/risFisse/syncConSistLog", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }
}
