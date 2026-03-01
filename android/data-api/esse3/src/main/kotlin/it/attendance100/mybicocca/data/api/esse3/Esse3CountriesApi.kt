package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3Country
import it.attendance100.mybicocca.data.dto.esse3.Esse3Municipality
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3PostalCode
import it.attendance100.mybicocca.data.dto.esse3.Esse3Province
import kotlinx.serialization.json.Json

class Esse3CountriesApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/nazioni-service-v1") {

    suspend fun getNations(
        iso6392Code: String? = null
    ): List<Esse3Country> {
        return executeJsonGetList<Esse3Country>("/nazioni", setOf(Esse3PermissionLevel.ANY)) {
            iso6392Code?.let { parameter("iso6392Cod", it) }
        }
    }

    suspend fun getPostalCode(
        nationId: Long,
        nationFiscalCode: String? = null,
        regionCode: String? = null,
        abbreviation: String? = null,
        municipalityId: Long? = null,
        municipalityCode: String? = null
    ): List<Esse3PostalCode> {
        return executeJsonGetList<Esse3PostalCode>("/nazioni/${nationId}/cap", setOf(Esse3PermissionLevel.ANY)) {
            nationFiscalCode?.let { parameter("nazioneCodFisc", it) }
            regionCode?.let { parameter("regioneCod", it) }
            abbreviation?.let { parameter("sigla", it) }
            municipalityId?.let { parameter("comuneId", it) }
            municipalityCode?.let { parameter("comuneCod", it) }
        }
    }

    suspend fun getMunicipalities(
        nationId: Long,
        iso6392Code: String? = null,
        nationFiscalCode: String? = null,
        regionCode: String? = null,
        abbreviation: String? = null,
        municipalityCode: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3Municipality> {
        return executeJsonGetList<Esse3Municipality>("/nazioni/${nationId}/comuni", setOf(Esse3PermissionLevel.ANY)) {
            iso6392Code?.let { parameter("iso6392Cod", it) }
            nationFiscalCode?.let { parameter("nazioneCodFisc", it) }
            regionCode?.let { parameter("regioneCod", it) }
            abbreviation?.let { parameter("sigla", it) }
            municipalityCode?.let { parameter("comuneCod", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getProvinces(
        nationId: Long,
        nationFiscalCode: String? = null
    ): List<Esse3Province> {
        return executeJsonGetList<Esse3Province>("/nazioni/${nationId}/province", setOf(Esse3PermissionLevel.ANY)) {
            nationFiscalCode?.let { parameter("nazioneCodFisc", it) }
        }
    }
}
