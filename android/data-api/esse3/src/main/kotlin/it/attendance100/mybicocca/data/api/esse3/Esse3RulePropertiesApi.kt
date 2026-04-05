package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3PrerequisiteConstraint
import it.attendance100.mybicocca.data.dto.esse3.Esse3PrerequisitesRegulation
import it.attendance100.mybicocca.data.dto.esse3.Esse3PrerequisitesRegulationWithConstraints
import kotlinx.serialization.json.Json

class Esse3RulePropertiesApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/regprop-service-v1") {

    /**
     * Informazioni di testata sui regolamenti di propedeuticità
     *
     * @param proposalRuleState stato del regolamento, se non valorizzato vengono recuperati i regolamenti attivi
     * @param facultyId id della facoltà di afferenza del regolamento
     * @param facultyCode codice della facoltà di afferenza del regolamento
     * @param courseOfStudyId id della corso di studio di afferenza del regolamento
     * @param courseOfStudyCode codice del corso di studio di afferenza del regolamento
     * @param courseTypeCode codice del tipo di corso di afferenza del regolamento
     * @param cohort coorte di afferenza del regolamento
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getPropaedeuticityRegulations(
        proposalRuleState: String? = null,
        facultyId: Long? = null,
        facultyCode: String? = null,
        courseOfStudyId: Long? = null,
        courseOfStudyCode: String? = null,
        courseTypeCode: String? = null,
        cohort: Long? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3PrerequisitesRegulation> {
        return executeJsonGetList<Esse3PrerequisitesRegulation>("/regprop", setOf(Esse3PermissionLevel.ANY)) {
            proposalRuleState?.let { parameter("statoRegprop", it) }
            facultyId?.let { parameter("facId", it) }
            facultyCode?.let { parameter("facCod", it) }
            courseOfStudyId?.let { parameter("cdsId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            cohort?.let { parameter("coorte", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * informazioni su un singolo regolamento
     *
     * @param proposalRuleId id univoco del regolamento di propedeuticità
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param filter il parametro consente di applicare dei filtri alla classe di modello utilizzando il linguaggio  [RSQL](https://github.com/jirutka/rsql-parser). La lista degli operatori utilizzabili è disponibile [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosullerigherecuperate) *NB* il filtro viene applicato DOPO aver recuperato i dati
     */
    suspend fun getPropaedeuticityRegulationWithDetails(
        proposalRuleId: Long,
        fields: String? = null,
        optionalFields: String? = null,
        filter: String? = null
    ): Esse3PrerequisitesRegulationWithConstraints {
        return executeJsonGet<Esse3PrerequisitesRegulationWithConstraints>("/regprop/${proposalRuleId}", setOf(Esse3PermissionLevel.ANY)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    /**
     * informazioni su un vincolo di propedeuticità
     *
     * @param proposalRuleId id univoco del regolamento di propedeuticità
     * @param proposalRuleWinnerId id univoco del vincolo di propedeuticità
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param filter il parametro consente di applicare dei filtri alla classe di modello utilizzando il linguaggio  [RSQL](https://github.com/jirutka/rsql-parser). La lista degli operatori utilizzabili è disponibile [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosullerigherecuperate) *NB* il filtro viene applicato DOPO aver recuperato i dati
     */
    suspend fun getRegulationConstraints(
        proposalRuleId: Long,
        proposalRuleWinnerId: Long,
        fields: String? = null,
        optionalFields: String? = null,
        filter: String? = null
    ): List<Esse3PrerequisiteConstraint> {
        return executeJsonGetList<Esse3PrerequisiteConstraint>("/regprop/${proposalRuleId}/${proposalRuleWinnerId}", setOf(Esse3PermissionLevel.ANY)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            filter?.let { parameter("filter", it) }
        }
    }
}
