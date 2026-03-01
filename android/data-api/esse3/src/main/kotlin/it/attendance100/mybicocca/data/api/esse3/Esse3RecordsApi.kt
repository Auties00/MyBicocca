package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3Batch
import it.attendance100.mybicocca.data.dto.esse3.Esse3BatchRecord
import it.attendance100.mybicocca.data.dto.esse3.Esse3BatchRecordWithDetails
import it.attendance100.mybicocca.data.dto.esse3.Esse3BatchWithDetails
import it.attendance100.mybicocca.data.dto.esse3.Esse3ImportBatch
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3Record
import it.attendance100.mybicocca.data.dto.esse3.Esse3RecordWithDetails
import it.attendance100.mybicocca.data.dto.esse3.Esse3RecordsImportResponse
import kotlinx.serialization.json.Json

class Esse3RecordsApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/verbali-service-v1") {

    suspend fun getBatches(
        activityCode: String? = null,
        courseOfStudyCode: String? = null,
        toDate: String? = null,
        fromDate: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3BatchRecord> {
        return executeJsonGetList<Esse3BatchRecord>("/batches", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            activityCode?.let { parameter("adCod", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            toDate?.let { parameter("aData", it) }
            fromDate?.let { parameter("daData", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun importMinutes(
        body: Esse3ImportBatch
    ): Esse3RecordsImportResponse {
        return executeJsonPost<Esse3RecordsImportResponse>("/batches", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun uploadMinutesBlob(
        uploadId: Long,
        body: kotlinx.serialization.json.JsonObject
    ): String {
        return executeJsonPut<String>("/batches/upload/${uploadId}/blob", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun getBatch(
        batchId: Long
    ): Esse3BatchRecordWithDetails {
        return executeJsonGet<Esse3BatchRecordWithDetails>("/batches/${batchId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun findBatches(
        studentMatricola: String? = null,
        lecturerMatricola: String? = null,
        studentFiscalCode: String? = null,
        lecturerFiscalCode: String? = null,
        callManagementTypeCode: String? = null,
        batchState: String? = null,
        activityCode: String? = null,
        courseOfStudyCode: String? = null,
        fromDate: String? = null,
        toDate: String? = null,
        start: Int? = null,
        limit: Int? = null,
        fields: String? = null,
        order: String? = null
    ): List<Esse3Batch> {
        return executeJsonGetList<Esse3Batch>("/lotti", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)) {
            studentMatricola?.let { parameter("matricolaStu", it) }
            lecturerMatricola?.let { parameter("matricolaDoc", it) }
            studentFiscalCode?.let { parameter("codFisStu", it) }
            lecturerFiscalCode?.let { parameter("codFisDoc", it) }
            callManagementTypeCode?.let { parameter("tipoGestAppCod", it) }
            batchState?.let { parameter("statoLotto", it) }
            activityCode?.let { parameter("adCod", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            fromDate?.let { parameter("daData", it) }
            toDate?.let { parameter("aData", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getBatch(
        lotBatchId: Long,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3BatchWithDetails> {
        return executeJsonGetList<Esse3BatchWithDetails>("/lotti/${lotBatchId}", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getMinutesByBatch(
        lotBatchId: Long,
        filter: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3RecordWithDetails> {
        return executeJsonGetList<Esse3RecordWithDetails>("/lotti/${lotBatchId}/verbali", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)) {
            filter?.let { parameter("filter", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getMinutes(
        lotBatchId: Long,
        minutesId: Long,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3RecordWithDetails {
        return executeJsonGet<Esse3RecordWithDetails>("/lotti/${lotBatchId}/verbali/${minutesId}", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.STUDENT)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun findMinutes(
        studentMatricola: String? = null,
        lecturerMatricola: String? = null,
        studentFiscalCode: String? = null,
        lecturerFiscalCode: String? = null,
        minutesState: String? = null,
        activityCode: String? = null,
        courseOfStudyCode: String? = null,
        studentActivityCode: String? = null,
        courseOfStudyStudentCode: String? = null,
        fromDate: String? = null,
        toDate: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3Record> {
        return executeJsonGetList<Esse3Record>("/verbali", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.STUDENT)) {
            studentMatricola?.let { parameter("matricolaStu", it) }
            lecturerMatricola?.let { parameter("matricolaDoc", it) }
            studentFiscalCode?.let { parameter("codFisStu", it) }
            lecturerFiscalCode?.let { parameter("codFisDoc", it) }
            minutesState?.let { parameter("statoVerbale", it) }
            activityCode?.let { parameter("adCod", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            studentActivityCode?.let { parameter("adStuCod", it) }
            courseOfStudyStudentCode?.let { parameter("cdsStuCod", it) }
            fromDate?.let { parameter("daData", it) }
            toDate?.let { parameter("aData", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }
}
