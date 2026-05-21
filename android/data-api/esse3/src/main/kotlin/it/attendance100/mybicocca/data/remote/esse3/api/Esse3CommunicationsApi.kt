package it.attendance100.mybicocca.data.remote.esse3.api

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CommunicationInsert
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CommunicationWithRecipients
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PermissionLevel
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Recipient
import kotlinx.serialization.json.Json

class Esse3CommunicationsApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/comunicazioni-service-v1") {

    /**
     * inserisce una nuova comunicazione
     *
     * @param body Oggetto che contiene i dati della comunicazione e relativi destinatari
     */
    suspend fun postCommunication(
        body: Esse3CommunicationInsert
    ) {
        val response = executePost("/comunicazioni") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * recupera i dati di una specifica comunicazione
     *
     * @param municipalityId ID univoco della comunicazione
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
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

    /**
     * recupera i dati dei destinatari di una specifica comunicazione
     *
     * @param municipalityId ID univoco della comunicazione
     * @param dataOrigin tipologia di destinatario (PERSONE=studente/registrato, DOCENTI=docente, SOGG_EST=soggetto esterno, EXTERNAL=recapito email/cellulare)
     * @param personalDataId ID univoco di anagrafica, per la tipologia di destinatario (se origineDato vale PERSONE, DOCENTI o SOGG_EST)
     * @param contact indirizzo email o numero di cellulare del destinatario
     * @param outcomeCode esito di invio comunicazione per il destinatario (SENT=inviata, FAIL=errore nell'invio, CANC=invio annullato, DRAFT=comunicazione ancora in bozza, ACTIVE=da inviare, WAIT=invio in corso)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     */
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
