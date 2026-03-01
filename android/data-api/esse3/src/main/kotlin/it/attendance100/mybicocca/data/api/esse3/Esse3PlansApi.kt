package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3CareerPortion
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3PostPlanBody
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudyPlan
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudyPlanHeader
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudyPlansStatistics
import kotlinx.serialization.json.Json

class Esse3PlansApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/piani-service-v1") {

    suspend fun getCareerSegments(
        matricola: String? = null,
        courseOfStudyStudentId: Long? = null,
        courseOfStudyStudentCode: String? = null,
        academicYearOrderStudentId: Int? = null,
        studyPlanStudentId: Long? = null,
        studyPlanStudentCode: String? = null,
        cohort: Int? = null,
        fiscalCode: String? = null,
        studentStatusCode: String? = null,
        optionalFields: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null
    ): List<Esse3CareerPortion> {
        return executeJsonGetList<Esse3CareerPortion>("/piani", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            matricola?.let { parameter("matricola", it) }
            courseOfStudyStudentId?.let { parameter("cdsStuId", it) }
            courseOfStudyStudentCode?.let { parameter("cdsStuCod", it) }
            academicYearOrderStudentId?.let { parameter("aaOrdStuId", it) }
            studyPlanStudentId?.let { parameter("pdsStuId", it) }
            studyPlanStudentCode?.let { parameter("pdsStuCod", it) }
            cohort?.let { parameter("coorte", it) }
            fiscalCode?.let { parameter("codiceFiscale", it) }
            studentStatusCode?.let { parameter("staStuCod", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    suspend fun getPlansStatistics(
        facultyCode: String? = null,
        cohort: Int? = null,
        minimumCohort: Int? = null,
        courseTypeCode: String? = null,
        courseOfStudyCode: String? = null
    ): List<Esse3StudyPlansStatistics> {
        return executeJsonGetList<Esse3StudyPlansStatistics>("/piani/stats", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            facultyCode?.let { parameter("facCod", it) }
            cohort?.let { parameter("coorte", it) }
            minimumCohort?.let { parameter("coorteMin", it) }
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
        }
    }

    suspend fun getStudentPlanHeaders(
        studentId: Long,
        planState: List<String>? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3StudyPlanHeader> {
        return executeJsonGetList<Esse3StudyPlanHeader>("/piani/${studentId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            planState?.let { parameter("statoPiano", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun postStudentPlan(
        studentId: Long,
        body: Esse3PostPlanBody
    ) {
        val response = executePost("/piani/${studentId}") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getStudentPlan(
        studentId: Long,
        planId: Long,
        fields: String? = null,
        optionalFields: String? = null,
        filter: String? = null
    ): Esse3StudyPlan {
        return executeJsonGet<Esse3StudyPlan>("/piani/${studentId}/${planId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    suspend fun getPlanPrint(
        studentId: Long,
        planId: Long
    ): String {
        return executeJsonGet<String>("/piani/${studentId}/${planId}/stampa", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER))
    }
}
