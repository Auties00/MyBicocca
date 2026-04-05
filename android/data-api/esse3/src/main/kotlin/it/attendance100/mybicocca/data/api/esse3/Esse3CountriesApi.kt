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

    /**
     * recupero delle nazioni
     *
     * @param iso6392Code Codice ISO lingua
     */
    suspend fun getNations(
        iso6392Code: String? = null
    ): List<Esse3Country> {
        return executeJsonGetList<Esse3Country>("/nazioni", setOf(Esse3PermissionLevel.ANY)) {
            iso6392Code?.let { parameter("iso6392Cod", it) }
        }
    }

    /**
     * recupero dei CAP
     *
     * @param nationId identificativo della nazione
     * @param nationFiscalCode Codice fiscale della nazione
     * @param regionCode Codice regione
     * @param abbreviation Sigla della provincia
     * @param municipalityId id univoco comune
     * @param municipalityCode Codice di 4 cifre (Lettera + 3 numeri) che è utilizzato nel codice fiscale per indicare il comune di nascita.
     */
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

    /**
     * recupero dei comuni
     *
     * @param nationId identificativo della nazione
     * @param iso6392Code Codice ISO lingua
     * @param nationFiscalCode Codice fiscale della nazione
     * @param regionCode Codice regione
     * @param abbreviation Sigla della provincia
     * @param municipalityCode Codice di 4 cifre (Lettera + 3 numeri) che è utilizzato nel codice fiscale per indicare il comune di nascita.
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
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

    /**
     * recupero delle province
     *
     * @param nationId identificativo della nazione
     * @param nationFiscalCode Codice fiscale della nazione
     */
    suspend fun getProvinces(
        nationId: Long,
        nationFiscalCode: String? = null
    ): List<Esse3Province> {
        return executeJsonGetList<Esse3Province>("/nazioni/${nationId}/province", setOf(Esse3PermissionLevel.ANY)) {
            nationFiscalCode?.let { parameter("nazioneCodFisc", it) }
        }
    }
}
