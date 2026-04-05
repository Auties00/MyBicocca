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

    /**
     * Recupero degli utenti abilitati presenti a sistema
     *
     * @param name nome dell'utente (se viene utilizzato il carattere * viene applicato il like)
     * @param surname cognome dell'utente (se viene utilizzato il carattere * viene applicato il like)
     * @param fiscalCode codice fiscale dell'utente
     * @param groupId id univoco che consente di individuare il gruppo utente
     * @param email indirizzo email (email personale o di ateneo)
     * @param userId userId
     * @param grants elenco nomi funzioni separati da virgola
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
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

    /**
     * Consente di aggiornare i dati degli utenti
     *
     * @param body file CSV che contiene i valori da importare
     */
    suspend fun postUserSignatureData(
        body: Esse3SignatureImportData
    ): List<Esse3SignatureResponse> {
        return executeJsonPostList<Esse3SignatureResponse>("/utenti/datiFirma", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * Consente di aggiornare i dati degli utenti
     *
     * @param body file CSV che contiene i valori da importare
     * @param delimiter delimitatore del file CSV, se non valorizzato vale ","
     */
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

    /**
     * Recupero delle informazioni relative ad un singolo utente abilitato presente a sistema per identificativo
     *
     * @param userId id univoco che consente di individuare l'account utente
     * @param caseSensitive 1: userId gestito in maniera case sensitive; 0: viene ignorato il case per lo userId. Default 1.
     * @param grants elenco nomi funzioni separati da virgola
     * @param aliasRecoveryAuthorization Indica che l’utente indicato sarà ricercato sia tra gli utenti che tra gli alias validi. 0-la ricerca sarà per il solo utente. 1-la ricerca sarà per utente e alias
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
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

    /**
     * Permette l'associazione delle credenziali CIE ad una utenza, nel caso questa associazione non esista già
     *
     * @param userId id univoco che consente di individuare l'account utente
     * @param cieCode Identificativo credenziali CIE
     */
    suspend fun putCieCode(
        userId: String,
        cieCode: String? = null
    ): Esse3DigitalIdentities {
        return executeJsonPut<Esse3DigitalIdentities>("/utenti/${userId}/cie", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            cieCode?.let { parameter("cieCode", it) }
        }
    }

    /**
     * Recupera lo spid code e il cie code associati ad un'utenza
     *
     * @param userId id univoco che consente di individuare l'account utente
     */
    suspend fun getDigitalIdentity(
        userId: String
    ): Esse3DigitalIdentities {
        return executeJsonGet<Esse3DigitalIdentities>("/utenti/${userId}/identitaDigitale", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Permette l'associazione delle credenziali SPID code ad una utenza, nel caso questa associazione non esista già
     *
     * @param userId id univoco che consente di individuare l'account utente
     * @param spidCode Identificativo credenziali SPID
     */
    suspend fun putSpidCode(
        userId: String,
        spidCode: String? = null
    ): Esse3DigitalIdentities {
        return executeJsonPut<Esse3DigitalIdentities>("/utenti/${userId}/spid", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            spidCode?.let { parameter("spidCode", it) }
        }
    }

    /**
     * Recupero delle carriere degli studenti
     *
     * @param userId id univoco che consente di individuare l'account utente
     * @param studentStatusCode codice dello stato della carriera
     * @param statusReasonCode codice del motivo dello stato della carriera
     * @param fiscalCode codice fiscale dell'utente
     * @param govIdentifier identificativo di U-Gov che permette di ritirare le informazioni dei responsabili.
     * @param studentMatricola matricola dello studente
     * @param externalCareerCode codice esterno carriera
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
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

    /**
     * Recupero dei tratti di carriera di uno studente
     *
     * @param userId id univoco che consente di individuare l'account utente
     * @param studentStatusCode codice dello stato della carriera
     * @param statusReasonCode codice del motivo dello stato della carriera
     * @param fiscalCode codice fiscale dell'utente
     * @param govIdentifier identificativo di U-Gov che permette di ritirare le informazioni dei responsabili.
     * @param studentMatricola matricola dello studente
     * @param externalCareerCode codice esterno carriera
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
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
