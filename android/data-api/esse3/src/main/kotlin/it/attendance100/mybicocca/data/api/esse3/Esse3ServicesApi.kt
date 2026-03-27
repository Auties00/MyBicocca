package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3AddressType
import it.attendance100.mybicocca.data.dto.esse3.Esse3Alias
import it.attendance100.mybicocca.data.dto.esse3.Esse3CodeToIdTranslatorRequest
import it.attendance100.mybicocca.data.dto.esse3.Esse3CodeToIdTranslatorResponseObject
import it.attendance100.mybicocca.data.dto.esse3.Esse3ConfigurationParameter
import it.attendance100.mybicocca.data.dto.esse3.Esse3IdentityDocumentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3Languages
import it.attendance100.mybicocca.data.dto.esse3.Esse3MaritalStatusType
import it.attendance100.mybicocca.data.dto.esse3.Esse3PaymentRefundType
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3ProcessedLists
import it.attendance100.mybicocca.data.dto.esse3.Esse3RecognitionType
import it.attendance100.mybicocca.data.dto.esse3.Esse3ReferenceYear
import it.attendance100.mybicocca.data.dto.esse3.Esse3UserGroup
import it.attendance100.mybicocca.data.dto.esse3.Esse3Users
import it.attendance100.mybicocca.data.dto.esse3.Esse3VersionInfo
import kotlinx.serialization.json.Json

class Esse3ServicesApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/servizi-service-v1") {

    suspend fun getAlias(
        userId: String
    ): Esse3Alias {
        return executeJsonGet<Esse3Alias>("/alias/${userId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun insertUpdateAlias(
        userId: String,
        alias: String,
        expirationDate: String? = null
    ): Esse3Alias {
        return executeJsonPost<Esse3Alias>("/alias/${userId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("alias", alias)
            expirationDate?.let { parameter("dataScadenza", it) }
        }
    }

    suspend fun deleteAlias(
        userId: String,
        alias: String
    ) {
        val response = executeDelete("/alias/${userId}") {
            parameter("alias", alias)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getAcademicYear(
        referenceDateTypeCode: String,
        courseTypeCode: String? = null,
        referenceDate: String? = null
    ): Esse3ReferenceYear {
        return executeJsonGet<Esse3ReferenceYear>("/annoRif/${referenceDateTypeCode}", setOf(Esse3PermissionLevel.ANY)) {
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            referenceDate?.let { parameter("dataRif", it) }
        }
    }

    suspend fun getLanguages(): List<Esse3Languages> {
        return executeJsonGetList<Esse3Languages>("/dati-strutturali/lingue", setOf(Esse3PermissionLevel.ANY))
    }

    suspend fun getLists(
        testId: Long,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3ProcessedLists> {
        return executeJsonGetList<Esse3ProcessedLists>("/elenchi/${testId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getUserGroups(): List<Esse3UserGroup> {
        return executeJsonGetList<Esse3UserGroup>("/grp-utenti", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getUserGroup(
        groupId: Int
    ): Esse3UserGroup {
        return executeJsonGet<Esse3UserGroup>("/grp-utenti/${groupId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getParameters(
        module: String? = null,
        product: String? = null,
        description: String? = null,
        note: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3ConfigurationParameter> {
        return executeJsonGetList<Esse3ConfigurationParameter>("/par-conf", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.EXTERNAL_SUBJECT)) {
            module?.let { parameter("modulo", it) }
            product?.let { parameter("prodotto", it) }
            description?.let { parameter("des", it) }
            note?.let { parameter("nota", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getParameter(
        parameterCode: String
    ): Esse3ConfigurationParameter {
        return executeJsonGet<Esse3ConfigurationParameter>("/par-conf/${parameterCode}/", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.EXTERNAL_SUBJECT))
    }

    suspend fun getIdentityDocumentTypes(
        iso6392Code: String? = null
    ): List<Esse3IdentityDocumentType> {
        return executeJsonGetList<Esse3IdentityDocumentType>("/tipologie/docIdent", setOf(Esse3PermissionLevel.ANY)) {
            iso6392Code?.let { parameter("iso6392Cod", it) }
        }
    }

    suspend fun getAddressTypes(): List<Esse3AddressType> {
        return executeJsonGetList<Esse3AddressType>("/tipologie/indirizzi", setOf(Esse3PermissionLevel.ANY))
    }

    suspend fun getRequestTypes(): List<Esse3RecognitionType> {
        return executeJsonGetList<Esse3RecognitionType>("/tipologie/riconoscimento-ad", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getPaymentRefundTypes(
        iso6392Code: String? = null
    ): List<Esse3PaymentRefundType> {
        return executeJsonGetList<Esse3PaymentRefundType>("/tipologie/rimborsi", setOf(Esse3PermissionLevel.ANY)) {
            iso6392Code?.let { parameter("iso6392Cod", it) }
        }
    }

    suspend fun getMaritalStatusTypes(
        iso6392Code: String? = null
    ): List<Esse3MaritalStatusType> {
        return executeJsonGetList<Esse3MaritalStatusType>("/tipologie/statiCivili", setOf(Esse3PermissionLevel.ANY)) {
            iso6392Code?.let { parameter("iso6392Cod", it) }
        }
    }

    suspend fun getCodeFromId(
        body: Esse3CodeToIdTranslatorRequest
    ): List<Esse3CodeToIdTranslatorResponseObject> {
        return executeJsonPutList<Esse3CodeToIdTranslatorResponseObject>("/translator/cod-to-id", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun getUser(
        userId: String,
        aliasRecoveryAuthorization: Int? = null
    ): Esse3Users {
        return executeJsonGet<Esse3Users>("/utente-info/${userId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            aliasRecoveryAuthorization?.let { parameter("abilRecuperoAlias", it) }
        }
    }

    suspend fun getVersionInfo(): Esse3VersionInfo {
        return executeJsonGet<Esse3VersionInfo>("/version-info", setOf(Esse3PermissionLevel.ANY))
    }
}
