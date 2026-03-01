package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3CommunicationInsert
import it.attendance100.mybicocca.data.dto.esse3.Esse3CommunicationWithRecipients
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3Recipient
import kotlinx.serialization.json.Json

class Esse3CommunicationsApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/comunicazioni-service-v1") {

    suspend fun postCommunication(
        body: Esse3CommunicationInsert
    ) {
        val response = executePost("/comunicazioni") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getCommunication(
        municipalityId: Long,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3CommunicationWithRecipients {
        return executeJsonGet<Esse3CommunicationWithRecipients>("/comunicazioni/${municipalityId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getCommonRecipients(
        municipalityId: Long,
        dataOrigin: String? = null,
        personalDataId: Long? = null,
        contact: String? = null,
        outcomeCode: String? = null,
        fields: String? = null,
        optionalFields: String? = null,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3Recipient> {
        return executeJsonGetList<Esse3Recipient>("/comunicazioni/${municipalityId}/destinatari", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            dataOrigin?.let { parameter("origineDato", it) }
            personalDataId?.let { parameter("idAnagrafica", it) }
            contact?.let { parameter("recapito", it) }
            outcomeCode?.let { parameter("esitoCod", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }
}
