package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3CareerPortion
import it.attendance100.mybicocca.data.dto.esse3.Esse3DigitalIdentities
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExtendedCareerPortion
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3SignatureImportData
import it.attendance100.mybicocca.data.dto.esse3.Esse3SignatureResponse
import it.attendance100.mybicocca.data.dto.esse3.Esse3User
import kotlinx.serialization.json.Json

class Esse3UsersApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/utenti-service-v1") {

    suspend fun getUsers(
        name: String? = null,
        surname: String? = null,
        fiscalCode: String? = null,
        groupId: Long? = null,
        email: String? = null,
        userId: String? = null,
        grants: String? = null,
        optionalFields: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3User> {
        return executeJsonGetList<Esse3User>("/utenti", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            name?.let { parameter("nome", it) }
            surname?.let { parameter("cognome", it) }
            fiscalCode?.let { parameter("codFis", it) }
            groupId?.let { parameter("grpId", it) }
            email?.let { parameter("email", it) }
            userId?.let { parameter("userId", it) }
            grants?.let { parameter("grants", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun postUserSignatureData(
        body: Esse3SignatureImportData
    ): List<Esse3SignatureResponse> {
        return executeJsonGetList<Esse3SignatureResponse>("/utenti/datiFirma", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun postUserSignatureDataCsv(
        body: String,
        delimiter: String? = null
    ): String {
        return executeJsonPost<String>("/utenti/datiFirma/csv", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            delimiter?.let { parameter("delimiter", it) }
        }
    }

    suspend fun getUser(
        userId: String,
        caseSensitive: Int? = null,
        grants: String? = null,
        aliasRecoveryAuthorization: Int? = null,
        optionalFields: String? = null
    ): List<Esse3User> {
        return executeJsonGetList<Esse3User>("/utenti/${userId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            caseSensitive?.let { parameter("caseSensitive", it) }
            grants?.let { parameter("grants", it) }
            aliasRecoveryAuthorization?.let { parameter("abilRecuperoAlias", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun putCieCode(
        userId: String,
        cieCode: String? = null
    ): Esse3DigitalIdentities {
        return executeJsonPut<Esse3DigitalIdentities>("/utenti/${userId}/cie", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            cieCode?.let { parameter("cieCode", it) }
        }
    }

    suspend fun getDigitalIdentity(
        userId: String
    ): Esse3DigitalIdentities {
        return executeJsonGet<Esse3DigitalIdentities>("/utenti/${userId}/identitaDigitale", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun putSpidCode(
        userId: String,
        spidCode: String? = null
    ): Esse3DigitalIdentities {
        return executeJsonPut<Esse3DigitalIdentities>("/utenti/${userId}/spid", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            spidCode?.let { parameter("spidCode", it) }
        }
    }

    suspend fun getExtendedCareer(
        userId: String,
        studentStatusCode: String? = null,
        statusReasonCode: String? = null,
        fiscalCode: String? = null,
        govIdentifier: String? = null,
        studentMatricola: String? = null,
        externalCareerCode: String? = null,
        order: String? = null
    ): List<Esse3ExtendedCareerPortion> {
        return executeJsonGetList<Esse3ExtendedCareerPortion>("/utenti/${userId}/trattiAttivi", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            studentStatusCode?.let { parameter("staStuCod", it) }
            statusReasonCode?.let { parameter("motStastuCod", it) }
            fiscalCode?.let { parameter("codFis", it) }
            govIdentifier?.let { parameter("identificativo U-Gov", it) }
            studentMatricola?.let { parameter("matricolaStudente", it) }
            externalCareerCode?.let { parameter("Codice esterno carriera", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getCareerSegments(
        userId: String,
        studentStatusCode: String? = null,
        statusReasonCode: String? = null,
        fiscalCode: String? = null,
        govIdentifier: String? = null,
        studentMatricola: String? = null,
        externalCareerCode: String? = null,
        order: String? = null
    ): List<Esse3CareerPortion> {
        return executeJsonGetList<Esse3CareerPortion>("/utenti/${userId}/trattiCarriera", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            studentStatusCode?.let { parameter("staStuCod", it) }
            statusReasonCode?.let { parameter("motStastuCod", it) }
            fiscalCode?.let { parameter("codFis", it) }
            govIdentifier?.let { parameter("identificativo U-Gov", it) }
            studentMatricola?.let { parameter("matricolaStudente", it) }
            externalCareerCode?.let { parameter("Codice esterno carriera", it) }
            order?.let { parameter("order", it) }
        }
    }
}
