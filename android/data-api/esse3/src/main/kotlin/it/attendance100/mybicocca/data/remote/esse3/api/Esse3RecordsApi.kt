package it.attendance100.mybicocca.data.remote.esse3.api

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Batch
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3BatchRecord
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3BatchRecordWithDetails
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3BatchWithDetails
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ImportBatch
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PermissionLevel
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Record
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3RecordWithDetails
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3RecordsImportResponse
import kotlinx.serialization.json.Json

class Esse3RecordsApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/verbali-service-v1") {

    /**
     * recupera le informazioni di un batch di importazione
     *
     * @param activityCode codice dell'ad di definizione del lotto
     * @param courseOfStudyCode codice del cds di definizione del lotto
     * @param toDate recupero dei vebali con data di verbalizzazione fino ad una certa data
     * @param fromDate recupero dei vebali con data di verbalizzazione a partire da una certa data
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getBatches(
        activityCode: String? = null,
        courseOfStudyCode: String? = null,
        toDate: String? = null,
        fromDate: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3BatchRecord> {
        return executeJsonGetList<Esse3BatchRecord>("/batches", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            activityCode?.let { parameter("adCod", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            toDate?.let { parameter("aData", it) }
            fromDate?.let { parameter("daData", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * import dei verbali
     *
     * @param body Oggetto che contiene la riga da inserire
     */
    suspend fun importMinutes(
        body: Esse3ImportBatch
    ): Esse3RecordsImportResponse {
        return executeJsonPost<Esse3RecordsImportResponse>("/batches", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * upload di un immagine del verbale
     *
     * @param uploadId identificativo dell'upload per il caricamento di un blob di un verbale
     */
    suspend fun uploadMinutesBlob(
        uploadId: Long,
        body: kotlinx.serialization.json.JsonObject
    ): String {
        return executeJsonPut<String>("/batches/upload/${uploadId}/blob", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * recupera le informazioni di un batch di importazione
     *
     * @param batchId identificativo del batch
     */
    suspend fun getBatch(
        batchId: Long
    ): Esse3BatchRecordWithDetails {
        return executeJsonGet<Esse3BatchRecordWithDetails>("/batches/${batchId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Recupero dei lotti presenti a sistema
     *
     * @param studentMatricola matricola dello studente per il quale cercare i lotti
     * @param lecturerMatricola matricola del docente per il quale cercare i lotti
     * @param studentFiscalCode matricola dello studente per il quale cercare i lotti
     * @param lecturerFiscalCode matricola del docente per il quale cercare i lotti
     * @param callManagementTypeCode codice del tipo di verbalizzazione assegnata al lotto
     * @param batchState stato del lotto
     * @param activityCode codice dell'ad di definizione del lotto
     * @param courseOfStudyCode codice del cds di definizione del lotto
     * @param fromDate recupero dei vebali con data di verbalizzazione a partire da una certa data
     * @param toDate recupero dei vebali con data di verbalizzazione fino ad una certa data
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun findBatches(
        studentMatricola: String? = null,
        lecturerMatricola: String? = null,
        studentFiscalCode: String? = null,
        lecturerFiscalCode: String? = null,
        callManagementTypeCode: String? = null,
        batchState: String? = null,
        activityCode: String? = null,
        courseOfStudyCode: String? = null,
        fromDate: String? = null,
        toDate: String? = null,
        start: Int? = null,
        limit: Int? = null,
        fields: String? = null,
        order: String? = null
    ): List<Esse3Batch> {
        return executeJsonGetList<Esse3Batch>("/lotti", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)) {
            studentMatricola?.let { parameter("matricolaStu", it) }
            lecturerMatricola?.let { parameter("matricolaDoc", it) }
            studentFiscalCode?.let { parameter("codFisStu", it) }
            lecturerFiscalCode?.let { parameter("codFisDoc", it) }
            callManagementTypeCode?.let { parameter("tipoGestAppCod", it) }
            batchState?.let { parameter("statoLotto", it) }
            activityCode?.let { parameter("adCod", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            fromDate?.let { parameter("daData", it) }
            toDate?.let { parameter("aData", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * recupera un lotto
     *
     * @param lotBatchId identificativo del lotto
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getBatch(
        lotBatchId: Long,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3BatchWithDetails> {
        return executeJsonGetList<Esse3BatchWithDetails>("/lotti/${lotBatchId}", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * recupera i verbali di un lotto
     *
     * @param lotBatchId identificativo del lotto
     * @param filter il parametro consente di applicare dei filtri alla classe di modello utilizzando il linguaggio  [RSQL](https://github.com/jirutka/rsql-parser). La lista degli operatori utilizzabili è disponibile [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosullerigherecuperate) *NB* il filtro viene applicato DOPO aver recuperato i dati
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getMinutesByBatch(
        lotBatchId: Long,
        filter: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3RecordWithDetails> {
        return executeJsonGetList<Esse3RecordWithDetails>("/lotti/${lotBatchId}/verbali", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)) {
            filter?.let { parameter("filter", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * recupera un verbale di un lotto
     *
     * @param lotBatchId identificativo del lotto
     * @param minutesId identificativo del verbale
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getMinutes(
        lotBatchId: Long,
        minutesId: Long,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3RecordWithDetails {
        return executeJsonGet<Esse3RecordWithDetails>("/lotti/${lotBatchId}/verbali/${minutesId}", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.STUDENT)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * Recupero dei verbali presenti a sistema
     *
     * @param studentMatricola matricola dello studente per il quale cercare i lotti
     * @param lecturerMatricola matricola del docente per il quale cercare i lotti
     * @param studentFiscalCode matricola dello studente per il quale cercare i lotti
     * @param lecturerFiscalCode matricola del docente per il quale cercare i lotti
     * @param minutesState stato del verbale
     * @param activityCode codice dell'ad di definizione del lotto
     * @param courseOfStudyCode codice del cds di definizione del lotto
     * @param studentActivityCode codice dell'ad di definizione del lotto
     * @param courseOfStudyStudentCode codice del cds di definizione del lotto
     * @param fromDate recupero dei vebali con data di verbalizzazione a partire da una certa data
     * @param toDate recupero dei vebali con data di verbalizzazione fino ad una certa data
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun findMinutes(
        studentMatricola: String? = null,
        lecturerMatricola: String? = null,
        studentFiscalCode: String? = null,
        lecturerFiscalCode: String? = null,
        minutesState: String? = null,
        activityCode: String? = null,
        courseOfStudyCode: String? = null,
        studentActivityCode: String? = null,
        courseOfStudyStudentCode: String? = null,
        fromDate: String? = null,
        toDate: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3Record> {
        return executeJsonGetList<Esse3Record>("/verbali", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.STUDENT)) {
            studentMatricola?.let { parameter("matricolaStu", it) }
            lecturerMatricola?.let { parameter("matricolaDoc", it) }
            studentFiscalCode?.let { parameter("codFisStu", it) }
            lecturerFiscalCode?.let { parameter("codFisDoc", it) }
            minutesState?.let { parameter("statoVerbale", it) }
            activityCode?.let { parameter("adCod", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            studentActivityCode?.let { parameter("adStuCod", it) }
            courseOfStudyStudentCode?.let { parameter("cdsStuCod", it) }
            fromDate?.let { parameter("daData", it) }
            toDate?.let { parameter("aData", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }
}
