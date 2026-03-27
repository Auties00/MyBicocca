package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel
import it.attendance100.mybicocca.data.dto.esse3.Esse3AttachmentExtension
import it.attendance100.mybicocca.data.dto.esse3.Esse3AttachmentMetadata
import it.attendance100.mybicocca.data.dto.esse3.Esse3AttachmentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3AttachmentTypeCode
import it.attendance100.mybicocca.data.dto.esse3.Esse3GenericAttachmentInsertMetadata
import it.attendance100.mybicocca.data.dto.esse3.Esse3PatchAttachmentMetadata
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3UploadMetadata
import kotlinx.serialization.json.Json

class Esse3AttachmentsApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/allegati-service-v1") {

    suspend fun getAttachmentTypologies(): List<Esse3AttachmentType> {
        return executeJsonGetList<Esse3AttachmentType>("/allegati", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getAttachmentTypeCodes(
        sessionLanguageCode: String? = null,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3AttachmentTypeCode> {
        return executeJsonGetList<Esse3AttachmentTypeCode>("/allegati/codiceTipologiaAllegato/", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.EXTERNAL_SUBJECT)) {
            sessionLanguageCode?.let { parameter("sessionLinguaCod", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    suspend fun getAttachmentExtension(
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3AttachmentExtension> {
        return executeJsonGetList<Esse3AttachmentExtension>("/allegati/estensioneAllegato", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.EXTERNAL_SUBJECT)) {
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    suspend fun getAttachmentsByType(
        attachmentType: String,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3AttachmentMetadata> {
        return executeJsonGetList<Esse3AttachmentMetadata>("/allegati/${attachmentType}/", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    suspend fun postGenericAttachmentMetadata(
        attachmentType: String,
        body: Esse3GenericAttachmentInsertMetadata
    ) {
        val response = executePost("/allegati/${attachmentType}/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getAttachmentMetadata(
        attachmentType: String,
        attachmentId: Long
    ): Esse3AttachmentMetadata {
        return executeJsonGet<Esse3AttachmentMetadata>("/allegati/${attachmentType}/${attachmentId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun validateAttachment(
        attachmentType: String,
        attachmentId: Long,
        body: Esse3PatchAttachmentMetadata
    ): Esse3AttachmentMetadata {
        return executeJsonPatch<Esse3AttachmentMetadata>("/allegati/${attachmentType}/${attachmentId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun getAttachmentContent(
        attachmentType: String,
        attachmentId: Long
    ): ByteReadChannel {
        return executeStreamGet("/allegati/${attachmentType}/${attachmentId}/blob", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getUploadedAttachmentMetadata(
        uploadId: Long
    ): Esse3UploadMetadata {
        return executeJsonGet<Esse3UploadMetadata>("/upload/${uploadId}", setOf(Esse3PermissionLevel.ANY, Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun deleteUploadedAttachment(
        uploadId: Long
    ) {
        val response = executeDelete("/upload/${uploadId}")
        ensureSuccess(response, setOf(Esse3PermissionLevel.ANY, Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getUploadedAttachmentState(
        uploadId: Long
    ) {
        val response = executeGet("/upload/${uploadId}/blob")
        ensureSuccess(response, setOf(Esse3PermissionLevel.ANY, Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun uploadAttachment(
        uploadId: Long,
        body: kotlinx.serialization.json.JsonObject
    ) {
        val response = executePut("/upload/${uploadId}/blob") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.ANY, Esse3PermissionLevel.TECHNICAL_USER))
    }
}
