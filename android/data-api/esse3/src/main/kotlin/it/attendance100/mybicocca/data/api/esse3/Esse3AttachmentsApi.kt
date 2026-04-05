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

    /**
     * tipologie di allegato
     */
    suspend fun getAttachmentTypologies(): List<Esse3AttachmentType> {
        return executeJsonGetList<Esse3AttachmentType>("/allegati", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Recupera le tipologie di allegato ed i relativi codici gestiti da Esse3
     *
     * @param sessionLanguageCode codice ISO_6392 della lingua con la quale recuperare le descrizioni, nel caso non sia passato in ingresso viene utilizzata la lingua di default del sistema, e nel caso la lingua richiesta non sia disponibile, viene restituita la descrizione nella lingua di default
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     */
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

    /**
     * Recupera le estensioni degli allegati ed i relativi codici gestiti da Esse3
     *
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     */
    suspend fun getAttachmentExtension(
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3AttachmentExtension> {
        return executeJsonGetList<Esse3AttachmentExtension>("/allegati/estensioneAllegato", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.EXTERNAL_SUBJECT)) {
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    /**
     * lista degli allegati di una determinata tipologia
     *
     * @param attachmentType codice del tipo allegato da recuperare
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     */
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

    /**
     * inserimento metadati allegato
     *
     * @param attachmentType codice del tipo allegato da recuperare
     * @param body Oggetto che contiene i metadati dell'allegato da inserire
     */
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

    /**
     * metadati di uno specifico allegato
     *
     * @param attachmentType codice del tipo allegato da recuperare
     * @param attachmentId id di upload per caricare un allegato
     */
    suspend fun getAttachmentMetadata(
        attachmentType: String,
        attachmentId: Long
    ): Esse3AttachmentMetadata {
        return executeJsonGet<Esse3AttachmentMetadata>("/allegati/${attachmentType}/${attachmentId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * modifica metadati di un allegato
     *
     * @param attachmentType codice del tipo allegato da recuperare
     * @param attachmentId id di upload per caricare un allegato
     * @param body Proprietà modificabili dell'allegato
     */
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

    /**
     * download dell'allegato collegato ai metadati
     *
     * @param attachmentType codice del tipo allegato da recuperare
     * @param attachmentId id di upload per caricare un allegato
     */
    suspend fun getAttachmentContent(
        attachmentType: String,
        attachmentId: Long
    ): ByteReadChannel {
        return executeStreamGet("/allegati/${attachmentType}/${attachmentId}/blob", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * metadati di upload dell'allegato
     *
     * @param uploadId id di upload per caricare un allegato
     */
    suspend fun getUploadedAttachmentMetadata(
        uploadId: Long
    ): Esse3UploadMetadata {
        return executeJsonGet<Esse3UploadMetadata>("/upload/${uploadId}", setOf(Esse3PermissionLevel.ANY, Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * cancellazione dei metadati di upload
     *
     * @param uploadId id di upload per caricare un allegato
     */
    suspend fun deleteUploadedAttachment(
        uploadId: Long
    ) {
        val response = executeDelete("/upload/${uploadId}")
        ensureSuccess(response, setOf(Esse3PermissionLevel.ANY, Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * monitora lo stato di upload dell'allegato
     *
     * @param uploadId id di upload per caricare un allegato
     */
    suspend fun getUploadedAttachmentState(
        uploadId: Long
    ) {
        val response = executeGet("/upload/${uploadId}/blob")
        ensureSuccess(response, setOf(Esse3PermissionLevel.ANY, Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * operazione di upload dell'allegato
     *
     * @param uploadId id di upload per caricare un allegato
     */
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
