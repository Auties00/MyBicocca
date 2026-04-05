package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3TeacherDiary
import it.attendance100.mybicocca.data.dto.esse3.Esse3TeacherDiaryWithDetails
import it.attendance100.mybicocca.data.dto.esse3.Esse3TeacherRegister
import it.attendance100.mybicocca.data.dto.esse3.Esse3TeacherRegisterWithDetails
import kotlinx.serialization.json.Json

class Esse3TeacherReportingApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/rendicontazione-doc-service-v1") {

    /**
     * filtra le informazioni di testata dei diari docente
     *
     * @param academicYearId id dell' anno del diario
     * @param lecturerId id del docente
     * @param fiscalCode codice fiscale del docente
     * @param matricola matricola del docente
     * @param surname cognome del docente (sono necessari almeno i primi 3 caratteri del cognome, poi è possibile aggiungere un * per effettuare una ricerca in like)
     * @param diaryStatusCode stato del diario
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun filterLecturerDiary(
        academicYearId: Int? = null,
        lecturerId: Long? = null,
        fiscalCode: String? = null,
        matricola: String? = null,
        surname: String? = null,
        diaryStatusCode: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null
    ): List<Esse3TeacherDiary> {
        return executeJsonGetList<Esse3TeacherDiary>("/diari", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            academicYearId?.let { parameter("aaId", it) }
            lecturerId?.let { parameter("docenteId", it) }
            fiscalCode?.let { parameter("codFis", it) }
            matricola?.let { parameter("matricola", it) }
            surname?.let { parameter("cognome", it) }
            diaryStatusCode?.let { parameter("staDiarioCod", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    /**
     * recupera le informazioni sul diario docente
     *
     * @param diaryId id univoco del diario docente
     */
    suspend fun getLecturerDiary(
        diaryId: Long
    ): List<Esse3TeacherDiaryWithDetails> {
        return executeJsonGetList<Esse3TeacherDiaryWithDetails>("/diari/${diaryId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * filtra le informazioni di testata sui registri docente
     *
     * @param academicYearOfferId id dell' anno di offerta
     * @param facultyCode codice della facoltà di afferenza
     * @param activityCode codice dell' attività didattica di afferenza
     * @param lecturerId id del docente
     * @param fiscalCode codice fiscale del docente
     * @param matricola matricola del docente
     * @param surname cognome del docente (sono necessari almeno i primi 3 caratteri del cognome, poi è possibile aggiungere un * per effettuare una ricerca in like)
     * @param regulationStatusCode stato del registro, gli stati validi sono B,C,S,V,A,X è possibile passare più stati passando la lista di stati separati da virgole
     * @param fromLastStateTransitionDate data di transizione di stato minima dalla quale recuperare i registri, vengono recuperati tutti i registri che hanno data maggiore rispetto a quella indicata oppure null
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun filterLecturerRegisters(
        academicYearOfferId: Int? = null,
        facultyCode: String? = null,
        activityCode: String? = null,
        lecturerId: Long? = null,
        fiscalCode: String? = null,
        matricola: String? = null,
        surname: String? = null,
        regulationStatusCode: String? = null,
        fromLastStateTransitionDate: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3TeacherRegister> {
        return executeJsonGetList<Esse3TeacherRegister>("/registri", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            academicYearOfferId?.let { parameter("aaOffId", it) }
            facultyCode?.let { parameter("facCod", it) }
            activityCode?.let { parameter("adCod", it) }
            lecturerId?.let { parameter("docenteId", it) }
            fiscalCode?.let { parameter("codFis", it) }
            matricola?.let { parameter("matricola", it) }
            surname?.let { parameter("cognome", it) }
            regulationStatusCode?.let { parameter("staRegCod", it) }
            fromLastStateTransitionDate?.let { parameter("daUltimaDataTransStato", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * recupera le informazioni di un singolo registro docente
     *
     * @param registrationId id univoco del registro docente
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getLecturerRegister(
        registrationId: Long,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3TeacherRegisterWithDetails> {
        return executeJsonGetList<Esse3TeacherRegisterWithDetails>("/registri/${registrationId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }
}
