package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3AttendanceProcedureParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3CareerUpdateHeader
import it.attendance100.mybicocca.data.dto.esse3.Esse3CareerUpdateLog
import it.attendance100.mybicocca.data.dto.esse3.Esse3CareerUpdateRow
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3ProcedureUpdateActivityOfferParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3RemoveAttendanceProcedureParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3SegmentProcedureParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3SubstitutionProcedureParameters
import kotlinx.serialization.json.Json

class Esse3CareerUpdateApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/aggcarr-service-v1") {

    suspend fun getCareerUpdateHeaders(): List<Esse3CareerUpdateHeader> {
        return executeJsonGetList<Esse3CareerUpdateHeader>("/aggcarr", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun postAddTeachingActivityOffer(
        body: Esse3ProcedureUpdateActivityOfferParameters
    ) {
        val response = executePost("/aggcarr/AGG_AD_OFF/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun putAddTeachingActivityOfferByStudent(
        matId: Long,
        body: Esse3ProcedureUpdateActivityOfferParameters
    ): List<Esse3CareerUpdateLog> {
        return executeJsonPutList<Esse3CareerUpdateLog>("/aggcarr/AGG_AD_OFF/${matId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun postAttendance(
        body: Esse3AttendanceProcedureParameters
    ) {
        val response = executePost("/aggcarr/FREQ/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun putAttendanceByStudent(
        matId: Long,
        body: Esse3AttendanceProcedureParameters
    ): List<Esse3CareerUpdateLog> {
        return executeJsonPutList<Esse3CareerUpdateLog>("/aggcarr/FREQ/${matId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun postRemoveAttendance(
        body: Esse3RemoveAttendanceProcedureParameters
    ) {
        val response = executePost("/aggcarr/RIMUOVI_FREQ/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun putRemoveAttendanceByStudent(
        matId: Long,
        body: Esse3RemoveAttendanceProcedureParameters
    ): List<Esse3CareerUpdateLog> {
        return executeJsonPutList<Esse3CareerUpdateLog>("/aggcarr/RIMUOVI_FREQ/${matId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun postSEG(
        body: Esse3SegmentProcedureParameters
    ) {
        val response = executePost("/aggcarr/SEG/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun putSEGByStudent(
        matId: Long,
        body: Esse3SegmentProcedureParameters
    ): List<Esse3CareerUpdateLog> {
        return executeJsonPutList<Esse3CareerUpdateLog>("/aggcarr/SEG/${matId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun postSubstitution(
        body: Esse3SubstitutionProcedureParameters
    ) {
        val response = executePost("/aggcarr/SOST/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun putSubstitutionByStudent(
        matId: Long,
        body: Esse3SubstitutionProcedureParameters
    ): List<Esse3CareerUpdateLog> {
        return executeJsonPutList<Esse3CareerUpdateLog>("/aggcarr/SOST/${matId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun getCareerUpdateHeader(
        careerUpdateId: Long
    ): Esse3CareerUpdateHeader {
        return executeJsonGet<Esse3CareerUpdateHeader>("/aggcarr/${careerUpdateId}/", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun deletePreview(
        careerUpdateId: Long
    ) {
        val response = executeDelete("/aggcarr/${careerUpdateId}/")
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getPreviewDetails(
        careerUpdateId: Long,
        fields: String? = null,
        optionalFields: String? = null,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3CareerUpdateRow> {
        return executeJsonGetList<Esse3CareerUpdateRow>("/aggcarr/${careerUpdateId}/dettagli", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    suspend fun getPreviewDetail(
        careerUpdateId: Long,
        careerUpdateDetailId: Long,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3CareerUpdateRow {
        return executeJsonGet<Esse3CareerUpdateRow>("/aggcarr/${careerUpdateId}/dettagli/${careerUpdateDetailId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun setProcessingDetailFlag(
        careerUpdateId: Long,
        careerUpdateDetailId: Long,
        body: kotlinx.serialization.json.JsonObject,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3CareerUpdateRow {
        return executeJsonPut<Esse3CareerUpdateRow>("/aggcarr/${careerUpdateId}/dettagli/${careerUpdateDetailId}/elabora", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun executeUpdateCareers(
        careerUpdateId: Long
    ): List<Esse3CareerUpdateLog> {
        return executeJsonPutList<Esse3CareerUpdateLog>("/aggcarr/${careerUpdateId}/esegui", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getUpdateCareersLog(
        careerUpdateId: Long
    ): List<Esse3CareerUpdateLog> {
        return executeJsonGetList<Esse3CareerUpdateLog>("/aggcarr/${careerUpdateId}/log", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }
}
