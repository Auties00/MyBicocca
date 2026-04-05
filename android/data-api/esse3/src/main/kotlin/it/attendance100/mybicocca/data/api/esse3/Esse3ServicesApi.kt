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

    /**
     * @param userId identificativo univoco che permette di estrarre i dati di utenza dalla tabella P18_USER
     */
    suspend fun getAlias(
        userId: String
    ): Esse3Alias {
        return executeJsonGet<Esse3Alias>("/alias/${userId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Inserisce gli alias
     *
     * @param userId identificativo univoco che permette di estrarre i dati di utenza dalla tabella P18_USER
     * @param alias alias dell utente
     * @param expirationDate data di scadenza
     */
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

    /**
     * Elimina l'alias associato ad un utente
     *
     * @param userId identificativo univoco che permette di estrarre i dati di utenza dalla tabella P18_USER
     * @param alias alias dell utente
     */
    suspend fun deleteAlias(
        userId: String,
        alias: String
    ) {
        val response = executeDelete("/alias/${userId}") {
            parameter("alias", alias)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * @param referenceDateTypeCode codice del tipo data di riferimento
     * @param courseTypeCode codice del tipo di corso di studio
     * @param referenceDate data di riferimento
     */
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

    /**
     * Recupera le lista delle lingue
     */
    suspend fun getLanguages(): List<Esse3Languages> {
        return executeJsonGetList<Esse3Languages>("/dati-strutturali/lingue", setOf(Esse3PermissionLevel.ANY))
    }

    /**
     * @param testId identificativo univoco che permette di estrarre i dati dalle tabelle P04_ELENCO_TST e P04_ELENCO_DETT
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
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

    /**
     * gruppi utente gestiti su e3rest
     */
    suspend fun getUserGroups(): List<Esse3UserGroup> {
        return executeJsonGetList<Esse3UserGroup>("/grp-utenti", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * gruppi utente gestiti su e3rest
     *
     * @param groupId identificativo univoco che permette di estrarre i dati del gruppo di un utente
     */
    suspend fun getUserGroup(
        groupId: Int
    ): Esse3UserGroup {
        return executeJsonGet<Esse3UserGroup>("/grp-utenti/${groupId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * @param module modulo di appartenenza del parametro di configurazione
     * @param product prodotto di appartenenza del parametro di configurazione
     * @param description descrizione del parametro di configurazione  (se viene utilizzato il carattere * viene applicato il like)
     * @param note nota associata al parametro di configurazione  (se viene utilizzato il carattere * viene applicato il like)
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
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

    /**
     * @param parameterCode codice univoco che consente di individuare il parametro di configurazione
     */
    suspend fun getParameter(
        parameterCode: String
    ): Esse3ConfigurationParameter {
        return executeJsonGet<Esse3ConfigurationParameter>("/par-conf/${parameterCode}/", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.EXTERNAL_SUBJECT))
    }

    /**
     * Recupera le tipologie dei documenti di identità
     *
     * @param iso6392Code Codice ISO lingua
     */
    suspend fun getIdentityDocumentTypes(
        iso6392Code: String? = null
    ): List<Esse3IdentityDocumentType> {
        return executeJsonGetList<Esse3IdentityDocumentType>("/tipologie/docIdent", setOf(Esse3PermissionLevel.ANY)) {
            iso6392Code?.let { parameter("iso6392Cod", it) }
        }
    }

    /**
     * Recupera le tipologie di indirizzo
     */
    suspend fun getAddressTypes(): List<Esse3AddressType> {
        return executeJsonGetList<Esse3AddressType>("/tipologie/indirizzi", setOf(Esse3PermissionLevel.ANY))
    }

    /**
     * Recupera le tipologie di riconoscimento delle attività didattiche
     */
    suspend fun getRequestTypes(): List<Esse3RecognitionType> {
        return executeJsonGetList<Esse3RecognitionType>("/tipologie/riconoscimento-ad", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Recupera le tipologie dei rimborsi di pagamento
     *
     * @param iso6392Code Codice ISO lingua
     */
    suspend fun getPaymentRefundTypes(
        iso6392Code: String? = null
    ): List<Esse3PaymentRefundType> {
        return executeJsonGetList<Esse3PaymentRefundType>("/tipologie/rimborsi", setOf(Esse3PermissionLevel.ANY)) {
            iso6392Code?.let { parameter("iso6392Cod", it) }
        }
    }

    /**
     * Recupera le tipologie di stato civile
     *
     * @param iso6392Code Codice ISO lingua
     */
    suspend fun getMaritalStatusTypes(
        iso6392Code: String? = null
    ): List<Esse3MaritalStatusType> {
        return executeJsonGetList<Esse3MaritalStatusType>("/tipologie/statiCivili", setOf(Esse3PermissionLevel.ANY)) {
            iso6392Code?.let { parameter("iso6392Cod", it) }
        }
    }

    /**
     * fornisce l'id relativo ad un determinato codice
     *
     * @param body Oggetto che contiene i codici da tradurre
     */
    suspend fun getCodeFromId(
        body: Esse3CodeToIdTranslatorRequest
    ): List<Esse3CodeToIdTranslatorResponseObject> {
        return executeJsonPutList<Esse3CodeToIdTranslatorResponseObject>("/translator/cod-to-id", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * @param userId identificativo univoco che permette di estrarre i dati di utenza dalla tabella P18_USER
     * @param aliasRecoveryAuthorization Indica che l’utente indicato sarà ricercato sia tra gli utenti che tra gli alias validi. 0-la ricerca sarà per il solo utente. 1-la ricerca sarà per utente e alias
     */
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
