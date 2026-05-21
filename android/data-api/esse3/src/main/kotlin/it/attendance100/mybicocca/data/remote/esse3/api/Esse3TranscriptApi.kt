package it.attendance100.mybicocca.data.remote.esse3.api

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ActivitiesToInsert
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3AttendanceReleaseDetail
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3BookableExamFilter
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3BookingFilter
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3BulkAttendanceParameters
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3BulkAttendanceResult
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3BulkPresenceReleaseParameters
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CareerPortion
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExamSessionEnrollment
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExamSessionTranscript
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PatchTranscriptRow
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PermissionLevel
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PrerequisitesCheck
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3RecognitionParameters
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3RecordBookRecognitionType
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3RecordBookStatsDataOriginFilter
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3RecordBookStatsRuleFilter
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3SingleAttendanceParameters
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3SyllabusActivityTranscript
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3SyllabusTeachingUnitTranscript
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TranscriptAverage
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TranscriptPartition
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TranscriptRow
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TranscriptRowPerActivityLog
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TranscriptSegment
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TranscriptStats
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TranscriptTest
import kotlinx.serialization.json.Json

class Esse3TranscriptApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/libretto-service-v2") {

    /**
     * Tratti carriera che contengono i libretti
     *
     * @param matricola codice della matricola dello studente
     * @param courseOfStudyStudentId id del corso di studio di appartenenza dello studente
     * @param courseOfStudyStudentCode codice del corso di studio di appartenenza dello studente
     * @param academicYearOrderStudentId codice dell'ordinamento di appartenenza dello studente
     * @param studyPlanStudentId id del percorso di studio di appartenenza dello studente
     * @param studyPlanStudentCode codice del percorso di studio di appartenenza dello studente
     * @param fiscalCode codice fiscale delo studente
     * @param studentStatusCode codice dello stato della carriera
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getCareerSegments(
        matricola: String? = null,
        courseOfStudyStudentId: Long? = null,
        courseOfStudyStudentCode: String? = null,
        academicYearOrderStudentId: Int? = null,
        studyPlanStudentId: Long? = null,
        studyPlanStudentCode: String? = null,
        fiscalCode: String? = null,
        studentStatusCode: String? = null,
        optionalFields: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3CareerPortion> {
        return executeJsonGetList<Esse3CareerPortion>("/libretti", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            matricola?.let { parameter("matricola", it) }
            courseOfStudyStudentId?.let { parameter("cdsStuId", it) }
            courseOfStudyStudentCode?.let { parameter("cdsStuCod", it) }
            academicYearOrderStudentId?.let { parameter("aaOrdStuId", it) }
            studyPlanStudentId?.let { parameter("pdsStuId", it) }
            studyPlanStudentCode?.let { parameter("pdsStuCod", it) }
            fiscalCode?.let { parameter("codFis", it) }
            studentStatusCode?.let { parameter("staStuCod", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * recupera gli studenti collegati ad una Attività erogata
     *
     * @param academicYearOfferLogId id dell'anno di erogazione della condivisione logistica
     * @param teachingActivityLogCode codice dell'attività didattica della condivisione logistica
     * @param courseOfStudyLogCode codice del corso di erogazione dell'attività didattica nella condivisione logistica
     * @param academicYearOrderLogId id dell'anno ordinamento del corso di erogazione della condivisione logistica
     * @param studyPlanLogCode codice del percorso di erogazione dell'attività didattica nella condivisione logistica
     * @param teachingUnitFreeCode codice dell'ud presente nel libretto dello studente
     * @param studentStatusCode codice dello stato della carriera
     * @param matStatusCode codice dello stato della matricola
     * @param supFlag se 1 indica le attività superate altrimenti quelle non superate
     * @param domicilePartialCode classe dello studente
     * @param allActivityLogs permette di recuperare la tutta la condivisione logistica, se non specificato vale false
     * @param studentId identificativo univoco dello studente
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getStudentClassByTeachingActivityStudyPlanOrderNew(
        academicYearOfferLogId: Int,
        teachingActivityLogCode: String,
        courseOfStudyLogCode: String,
        academicYearOrderLogId: Long,
        studyPlanLogCode: String,
        teachingUnitFreeCode: String? = null,
        studentStatusCode: String? = null,
        matStatusCode: String? = null,
        supFlag: Boolean? = null,
        domicilePartialCode: String? = null,
        allActivityLogs: Boolean? = null,
        studentId: Long? = null,
        start: Int? = null,
        limit: Int? = null,
        fields: String? = null,
        optionalFields: String? = null,
        order: String? = null
    ): List<Esse3TranscriptRowPerActivityLog> {
        return executeJsonGetList<Esse3TranscriptRowPerActivityLog>("/libretti/classe-studenti", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("logAaOffId", academicYearOfferLogId)
            parameter("logAdCod", teachingActivityLogCode)
            parameter("logCdsCod", courseOfStudyLogCode)
            parameter("logAaOrdId", academicYearOrderLogId)
            parameter("logPdsCod", studyPlanLogCode)
            teachingUnitFreeCode?.let { parameter("libUdCod", it) }
            studentStatusCode?.let { parameter("staStuCod", it) }
            matStatusCode?.let { parameter("staMatCod", it) }
            supFlag?.let { parameter("supFlg", it) }
            domicilePartialCode?.let { parameter("domPartCod", it) }
            allActivityLogs?.let { parameter("allAdLog", it) }
            studentId?.let { parameter("stuId", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * recupera la classe di studenti collegati ad una condivisione logistica
     *
     * @param activityLogId id univoco della condivisione logistica
     * @param activityCode codice attività della riga di libretto da ricercare
     * @param courseOfStudyStudentCode codice del corso di studio di appartenenza dello studente
     * @param studentStatusCode codice dello stato della carriera
     * @param matStatusCode codice dello stato della matricola
     * @param supFlag se 1 indica le attività superate altrimenti quelle non superate
     * @param domicilePartialCode classe dello studente
     * @param studentId identificativo univoco dello studente
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getStudentClassByTeachingActivityLogId(
        activityLogId: Long,
        activityCode: String? = null,
        courseOfStudyStudentCode: String? = null,
        studentStatusCode: String? = null,
        matStatusCode: String? = null,
        supFlag: Boolean? = null,
        domicilePartialCode: String? = null,
        studentId: Long? = null,
        start: Int? = null,
        limit: Int? = null,
        fields: String? = null,
        optionalFields: String? = null,
        order: String? = null
    ): List<Esse3TranscriptRowPerActivityLog> {
        return executeJsonGetList<Esse3TranscriptRowPerActivityLog>("/libretti/classe-studenti/${activityLogId}", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            activityCode?.let { parameter("adCod", it) }
            courseOfStudyStudentCode?.let { parameter("cdsStuCod", it) }
            studentStatusCode?.let { parameter("staStuCod", it) }
            matStatusCode?.let { parameter("staMatCod", it) }
            supFlag?.let { parameter("supFlg", it) }
            domicilePartialCode?.let { parameter("domPartCod", it) }
            studentId?.let { parameter("stuId", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * imposta o rimuove la frequenza per una lista di studenti
     *
     * @param body Oggetto che contiene gli studenti a cui assegnare la frequenza
     */
    suspend fun putMassiveAttendance(
        body: Esse3BulkAttendanceParameters
    ): Esse3BulkAttendanceResult {
        return executeJsonPut<Esse3BulkAttendanceResult>("/libretti/freq", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * gestione delle rilevazioni di frequenza
     *
     * @param body Oggetto che contiene gli studenti a cui assegnare la frequenza
     */
    suspend fun putMassiveDetections(
        body: Esse3BulkPresenceReleaseParameters
    ): Esse3BulkAttendanceResult {
        return executeJsonPut<Esse3BulkAttendanceResult>("/libretti/rilevazioni-freq/", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * Tratto carriera che contiene il libretto
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getCareerSegment(
        matId: Long,
        optionalFields: String? = null
    ): Esse3CareerPortion {
        return executeJsonGet<Esse3CareerPortion>("/libretti/${matId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * Lista degli appelli collegati al libretto
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param actorCode tipo di attore richiesto per l'estrazione dati (STU,DOC,SEG), utilizzato per filtrare la configurazione dell'appello, se null viene impostata la configurazione dell'attore corrente
     * @param q il parametro consente di filtrare i campi con delle particolari condizioni predefinite, consultare la documentazione del metodo per verificare i codici che è possibile utilizzare
     *  Accepted values:
     *   - [Esse3BookableExamFilter.AppelliPrenotabili]: recupera tutti gli appelli che non hanno nessuna prenotazione e risultano prenotabili
     *   - [Esse3BookableExamFilter.AppelliPrenotabiliEFuturi]: recupera tutti gli appelli che non hanno nessuna prenotazione e risultano prenotabili o futuri
     * @param filter il parametro consente di applicare dei filtri alla classe di modello utilizzando il linguaggio  [RSQL](https://github.com/jirutka/rsql-parser). La lista degli operatori utilizzabili è disponibile [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosullerigherecuperate) *NB* il filtro viene applicato DOPO aver recuperato i dati
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getRecordBookExamCalls(
        matId: Long,
        actorCode: String? = null,
        q: Esse3BookableExamFilter? = null,
        filter: String? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3ExamSessionTranscript> {
        return executeJsonGetList<Esse3ExamSessionTranscript>("/libretti/${matId}/appelli", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            actorCode?.let { parameter("attoreCod", it) }
            q?.let { parameter("q", it.value) }
            filter?.let { parameter("filter", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * Tutte le medie del libretto
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param initialAveragesReferenceDate data di riferimento iniziale per il calcolo delle medie
     * @param finalAveragesReferenceDate data di riferimento finale per il calcolo delle medie
     */
    suspend fun getRecordBookAverages(
        matId: Long,
        initialAveragesReferenceDate: String? = null,
        finalAveragesReferenceDate: String? = null
    ): List<Esse3TranscriptAverage> {
        return executeJsonGetList<Esse3TranscriptAverage>("/libretti/${matId}/medie", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            initialAveragesReferenceDate?.let { parameter("dataRifMedieIni", it) }
            finalAveragesReferenceDate?.let { parameter("dataRifMedieFin", it) }
        }
    }

    /**
     * Media del libretto richiesta
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param base tipo di base con cui recuperare la media (CDSORD,CDS)
     * @param type tipo di calcolo della media (A,P)
     * @param initialAveragesReferenceDate data di riferimento iniziale per il calcolo delle medie
     * @param finalAveragesReferenceDate data di riferimento finale per il calcolo delle medie
     */
    suspend fun getRecordBookAverage(
        matId: Long,
        base: String,
        type: String,
        initialAveragesReferenceDate: String? = null,
        finalAveragesReferenceDate: String? = null
    ): List<Esse3TranscriptAverage> {
        return executeJsonGetList<Esse3TranscriptAverage>("/libretti/${matId}/medie/${base}/${type}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            initialAveragesReferenceDate?.let { parameter("dataRifMedieIni", it) }
            finalAveragesReferenceDate?.let { parameter("dataRifMedieFin", it) }
        }
    }

    /**
     * Tutte le partizioni delle attività didattiche di un libretto
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     */
    suspend fun getRecordBookPartitions(
        matId: Long,
        fields: String? = null,
        order: String? = null,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3TranscriptPartition> {
        return executeJsonGetList<Esse3TranscriptPartition>("/libretti/${matId}/partizioni", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    /**
     * Recupera le informazioni delle prenotazioni collegate da un tratto di carriera
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param actorCode tipo di attore richiesto per l'estrazione dati (STU,DOC,SEG), utilizzato per filtrare la configurazione dell'appello, se null viene impostata la configurazione dell'attore corrente
     * @param minimumBookingDate data minima di prenotazione
     * @param q il parametro consente di filtrare i campi con delle particolari condizioni predefinite, consultare la documentazione del metodo per verificare i codici che è possibile utilizzare
     *  Accepted values:
     *   - [Esse3BookingFilter.BachecaEsiti]: prenotazioni visualizzate in bacheca esiti (appelli con pubblicazone, esiti pubblicati, data_ultimo_rif trascora, il verbale collegato non è stato generato)
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param filter il parametro consente di applicare dei filtri alla classe di modello utilizzando il linguaggio  [RSQL](https://github.com/jirutka/rsql-parser). La lista degli operatori utilizzabili è disponibile [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosullerigherecuperate) *NB* il filtro viene applicato DOPO aver recuperato i dati
     */
    suspend fun getBookingsByMatId(
        matId: Long,
        actorCode: String? = null,
        minimumBookingDate: String? = null,
        q: Esse3BookingFilter? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null,
        filter: String? = null
    ): List<Esse3ExamSessionEnrollment> {
        return executeJsonGetList<Esse3ExamSessionEnrollment>("/libretti/${matId}/prenotazioni", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            actorCode?.let { parameter("attoreCod", it) }
            minimumBookingDate?.let { parameter("dataMinPren", it) }
            q?.let { parameter("q", it.value) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    /**
     * Tutte le prove delle attività didattiche di un libretto
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     */
    suspend fun getRecordBookTests(
        matId: Long,
        fields: String? = null,
        order: String? = null,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3TranscriptTest> {
        return executeJsonGetList<Esse3TranscriptTest>("/libretti/${matId}/prove", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    /**
     * Tutte le attività del libretto del tratto di carriera selezionato
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param activityCode codice attività della riga di libretto da ricercare
     * @param nonDeletableActivities se 1, recupera le attività didattiche non cancellabili del libretto. Default a 0
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param filter il parametro consente di applicare dei filtri alla classe di modello utilizzando il linguaggio  [RSQL](https://github.com/jirutka/rsql-parser). La lista degli operatori utilizzabili è disponibile [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosullerigherecuperate) *NB* il filtro viene applicato DOPO aver recuperato i dati
     */
    suspend fun getRecordBookRows(
        matId: Long,
        activityCode: String? = null,
        nonDeletableActivities: Int? = null,
        optionalFields: String? = null,
        fields: String? = null,
        order: String? = null,
        filter: String? = null
    ): List<Esse3TranscriptRow> {
        return executeJsonGetList<Esse3TranscriptRow>("/libretti/${matId}/righe/", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            activityCode?.let { parameter("adCod", it) }
            nonDeletableActivities?.let { parameter("adNonCancellabili", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    /**
     * inserisce una riga di libretto
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param body Oggetto che contiene la riga da inserire
     */
    suspend fun postRecordBookRow(
        matId: Long,
        body: Esse3ActivitiesToInsert
    ) {
        val response = executePost("/libretti/${matId}/righe/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Attività richiesta nel libretto selezionato
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param activityChoiceId id della riga di libretto
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getRecordBookRow(
        matId: Long,
        activityChoiceId: Long,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3TranscriptRow {
        return executeJsonGet<Esse3TranscriptRow>("/libretti/${matId}/righe/${activityChoiceId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * cancella una riga di libretto
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param activityChoiceId id della riga di libretto
     */
    suspend fun deleteRecordBookRow(
        matId: Long,
        activityChoiceId: Long
    ) {
        val response = executeDelete("/libretti/${matId}/righe/${activityChoiceId}")
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * modifica riga del libretto
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param activityChoiceId id della riga di libretto
     * @param body Oggetto che contiene la riga da inserire
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun patchRecordBookRow(
        matId: Long,
        activityChoiceId: Long,
        body: Esse3PatchTranscriptRow,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3TranscriptRow {
        return executeJsonPatch<Esse3TranscriptRow>("/libretti/${matId}/righe/${activityChoiceId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * Lista degli appelli collegati alla riga di libretto
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param activityChoiceId id della riga di libretto
     * @param actorCode tipo di attore richiesto per l'estrazione dati (STU,DOC,SEG), utilizzato per filtrare la configurazione dell'appello, se null viene impostata la configurazione dell'attore corrente
     * @param q il parametro consente di filtrare i campi con delle particolari condizioni predefinite, consultare la documentazione del metodo per verificare i codici che è possibile utilizzare
     *  Accepted values:
     *   - [Esse3BookableExamFilter.AppelliPrenotabili]: recupera tutti gli appelli che non hanno nessuna prenotazione e risultano prenotabili
     *   - [Esse3BookableExamFilter.AppelliPrenotabiliEFuturi]: recupera tutti gli appelli che non hanno nessuna prenotazione e risultano prenotabili o futuri
     * @param filter il parametro consente di applicare dei filtri alla classe di modello utilizzando il linguaggio  [RSQL](https://github.com/jirutka/rsql-parser). La lista degli operatori utilizzabili è disponibile [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosullerigherecuperate) *NB* il filtro viene applicato DOPO aver recuperato i dati
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getExamCallsByRecordBookRow(
        matId: Long,
        activityChoiceId: Long,
        actorCode: String? = null,
        q: Esse3BookableExamFilter? = null,
        filter: String? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3ExamSessionTranscript> {
        return executeJsonGetList<Esse3ExamSessionTranscript>("/libretti/${matId}/righe/${activityChoiceId}/appelli", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            actorCode?.let { parameter("attoreCod", it) }
            q?.let { parameter("q", it.value) }
            filter?.let { parameter("filter", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * imposta o rimuove le frequenza dal libretto
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param activityChoiceId id della riga di libretto
     * @param body Oggetto che contiene gli studenti a cui assegnare la frequenza
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun setManualAttendance(
        matId: Long,
        activityChoiceId: Long,
        body: Esse3SingleAttendanceParameters,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3TranscriptRow {
        return executeJsonPut<Esse3TranscriptRow>("/libretti/${matId}/righe/${activityChoiceId}/freq", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * Tutte le partizioni dell'attività didattica selezionata
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param activityChoiceId id della riga di libretto
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getRecordBookRowPartitions(
        matId: Long,
        activityChoiceId: Long,
        fields: String? = null,
        order: String? = null
    ): List<Esse3TranscriptPartition> {
        return executeJsonGetList<Esse3TranscriptPartition>("/libretti/${matId}/righe/${activityChoiceId}/partizioni", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * Partizione richiesta dell'attività didattica selezionata
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param activityChoiceId id della riga di libretto
     * @param activityPartitionId id del segmento collegato alla riga di libretto
     */
    suspend fun getRecordBookRowPartition(
        matId: Long,
        activityChoiceId: Long,
        activityPartitionId: Long
    ): Esse3TranscriptPartition {
        return executeJsonGet<Esse3TranscriptPartition>("/libretti/${matId}/righe/${activityChoiceId}/partizioni/${activityPartitionId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Recupera le informazioni delle prenotazioni collegate da un tratto di carriera
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param activityChoiceId id della riga di libretto
     * @param actorCode tipo di attore richiesto per l'estrazione dati (STU,DOC,SEG), utilizzato per filtrare la configurazione dell'appello, se null viene impostata la configurazione dell'attore corrente
     * @param q il parametro consente di filtrare i campi con delle particolari condizioni predefinite, consultare la documentazione del metodo per verificare i codici che è possibile utilizzare
     *  Accepted values:
     *   - [Esse3BookingFilter.BachecaEsiti]: prenotazioni visualizzate in bacheca esiti (appelli con pubblicazone, esiti pubblicati, data_ultimo_rif trascora, il verbale collegato non è stato generato)
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param filter il parametro consente di applicare dei filtri alla classe di modello utilizzando il linguaggio  [RSQL](https://github.com/jirutka/rsql-parser). La lista degli operatori utilizzabili è disponibile [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosullerigherecuperate) *NB* il filtro viene applicato DOPO aver recuperato i dati
     */
    suspend fun getBookingsByTeachingActivityChoiceId(
        matId: Long,
        activityChoiceId: Long,
        actorCode: String? = null,
        q: Esse3BookingFilter? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null,
        filter: String? = null
    ): List<Esse3ExamSessionEnrollment> {
        return executeJsonGetList<Esse3ExamSessionEnrollment>("/libretti/${matId}/righe/${activityChoiceId}/prenotazioni", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            actorCode?.let { parameter("attoreCod", it) }
            q?.let { parameter("q", it.value) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    /**
     * Recupera le informazioni delle prenotazioni collegate da un tratto di carriera
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param activityChoiceId id della riga di libretto
     * @param applicationListId id univoco della prenotazione di uno studente
     * @param actorCode tipo di attore richiesto per l'estrazione dati (STU,DOC,SEG), utilizzato per filtrare la configurazione dell'appello, se null viene impostata la configurazione dell'attore corrente
     * @param q il parametro consente di filtrare i campi con delle particolari condizioni predefinite, consultare la documentazione del metodo per verificare i codici che è possibile utilizzare
     *  Accepted values:
     *   - [Esse3BookingFilter.BachecaEsiti]: prenotazioni visualizzate in bacheca esiti (appelli con pubblicazone, esiti pubblicati, data_ultimo_rif trascora, il verbale collegato non è stato generato)
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param filter il parametro consente di applicare dei filtri alla classe di modello utilizzando il linguaggio  [RSQL](https://github.com/jirutka/rsql-parser). La lista degli operatori utilizzabili è disponibile [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosullerigherecuperate) *NB* il filtro viene applicato DOPO aver recuperato i dati
     */
    suspend fun getBookingByTeachingActivityChoiceId(
        matId: Long,
        activityChoiceId: Long,
        applicationListId: Long,
        actorCode: String? = null,
        q: Esse3BookingFilter? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null,
        filter: String? = null
    ): Esse3ExamSessionEnrollment {
        return executeJsonGet<Esse3ExamSessionEnrollment>("/libretti/${matId}/righe/${activityChoiceId}/prenotazioni/${applicationListId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            actorCode?.let { parameter("attoreCod", it) }
            q?.let { parameter("q", it.value) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    /**
     * Recupera l'attestato di presenza
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param activityChoiceId id della riga di libretto
     * @param applicationListId id univoco della prenotazione di uno studente
     */
    suspend fun getPresenceCertificateByApplicationListId(
        matId: Long,
        activityChoiceId: Long,
        applicationListId: Long
    ): ByteReadChannel {
        return executeStreamGet("/libretti/${matId}/righe/${activityChoiceId}/prenotazioni/${applicationListId}/attestato-di-presenza", setOf(Esse3PermissionLevel.STUDENT))
    }

    /**
     * Recupera lo statino di prenotazione
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param activityChoiceId id della riga di libretto
     * @param applicationListId id univoco della prenotazione di uno studente
     */
    suspend fun getBookingStatinoByApplicationListId(
        matId: Long,
        activityChoiceId: Long,
        applicationListId: Long
    ): ByteReadChannel {
        return executeStreamGet("/libretti/${matId}/righe/${activityChoiceId}/prenotazioni/${applicationListId}/statino-prenotazione", setOf(Esse3PermissionLevel.STUDENT))
    }

    /**
     * effettua il controllo di propedeuticità
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param activityChoiceId id della riga di libretto
     * @param referenceDate data alla quale calcolare la propedeuticità, se non valorizzata viene utilizzata la data odierna
     */
    suspend fun getCheckProposalRecordBookRow(
        matId: Long,
        activityChoiceId: Long,
        referenceDate: String? = null
    ): Esse3PrerequisitesCheck {
        return executeJsonGet<Esse3PrerequisitesCheck>("/libretti/${matId}/righe/${activityChoiceId}/prop", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            referenceDate?.let { parameter("dataRif", it) }
        }
    }

    /**
     * Tutte le prove dell'attività didattica selezionata
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param activityChoiceId id della riga di libretto
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getRecordBookRowTests(
        matId: Long,
        activityChoiceId: Long,
        fields: String? = null,
        order: String? = null
    ): List<Esse3TranscriptTest> {
        return executeJsonGetList<Esse3TranscriptTest>("/libretti/${matId}/righe/${activityChoiceId}/prove", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * Prova richiesta dell'attività didattica selezionata
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param activityChoiceId id della riga di libretto
     * @param activityRegulationId id della prova collegata alla riga di libretto
     */
    suspend fun getRecordBookRowTest(
        matId: Long,
        activityChoiceId: Long,
        activityRegulationId: Long
    ): Esse3TranscriptTest {
        return executeJsonGet<Esse3TranscriptTest>("/libretti/${matId}/righe/${activityChoiceId}/prove/${activityRegulationId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER))
    }

    /**
     * inserisce un riconoscimento o una convalida
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param activityChoiceId id della riga di libretto
     * @param body Oggetto che contiene i dati da modificare
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param type tipo di caricamento, ric=riconiscimento a libretto, attoCar=atto di carriera per tirocini
     */
    suspend fun putRecordBookRowRecognition(
        matId: Long,
        activityChoiceId: Long,
        body: Esse3RecognitionParameters,
        optionalFields: String? = null,
        type: Esse3RecordBookRecognitionType? = null
    ): Esse3TranscriptRow {
        return executeJsonPut<Esse3TranscriptRow>("/libretti/${matId}/righe/${activityChoiceId}/riconoscimento", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            optionalFields?.let { parameter("optionalFields", it) }
            type?.let { parameter("type", it.value) }
        }
    }

    /**
     * annulla un riconoscimento
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param activityChoiceId id della riga di libretto
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun deleteRecordBookRecognitionRow(
        matId: Long,
        activityChoiceId: Long,
        optionalFields: String? = null
    ): Esse3TranscriptRow {
        return executeJsonDelete<Esse3TranscriptRow>("/libretti/${matId}/righe/${activityChoiceId}/riconoscimento", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * recupera il dettaglio delle rilevazioni
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param activityChoiceId id della riga di libretto
     * @param choiceReleaseId id del gruppo di rilevazioni fatto da rilevazione presenze
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param filter il parametro consente di applicare dei filtri alla classe di modello utilizzando il linguaggio  [RSQL](https://github.com/jirutka/rsql-parser). La lista degli operatori utilizzabili è disponibile [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosullerigherecuperate) *NB* il filtro viene applicato DOPO aver recuperato i dati
     */
    suspend fun getRecordBookRowDetections(
        matId: Long,
        activityChoiceId: Long,
        choiceReleaseId: Long,
        order: String? = null,
        filter: String? = null
    ): List<Esse3AttendanceReleaseDetail> {
        return executeJsonGetList<Esse3AttendanceReleaseDetail>("/libretti/${matId}/righe/${activityChoiceId}/rilevazioni-in-aula/${choiceReleaseId}/eventi", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            order?.let { parameter("order", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    /**
     * Tutti i segmenti dell'attività didattica selezionata
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param activityChoiceId id della riga di libretto
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getRecordBookRowSegments(
        matId: Long,
        activityChoiceId: Long,
        fields: String? = null,
        order: String? = null
    ): List<Esse3TranscriptSegment> {
        return executeJsonGetList<Esse3TranscriptSegment>("/libretti/${matId}/righe/${activityChoiceId}/segmenti", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * Segmento richiesto dell'attività didattica selezionata
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param activityChoiceId id della riga di libretto
     * @param segmentChoiceId id del segmento collegato alla riga di libretto
     */
    suspend fun getRecordBookRowSegment(
        matId: Long,
        activityChoiceId: Long,
        segmentChoiceId: Long
    ): Esse3TranscriptSegment {
        return executeJsonGet<Esse3TranscriptSegment>("/libretti/${matId}/righe/${activityChoiceId}/segmenti/${segmentChoiceId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Syllabus dell'Attività didattica collegata alla riga di libretto
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param activityChoiceId id della riga di libretto
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getSyllabusTeachingActivityRecordBookRow(
        matId: Long,
        activityChoiceId: Long,
        fields: String? = null,
        order: String? = null
    ): List<Esse3SyllabusActivityTranscript> {
        return executeJsonGetList<Esse3SyllabusActivityTranscript>("/libretti/${matId}/righe/${activityChoiceId}/syllabus/AD", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * Syllabus delle Unità didattiche collegate alla riga di libretto
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param activityChoiceId id della riga di libretto
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getSyllabusTeachingUnitRecordBookRow(
        matId: Long,
        activityChoiceId: Long,
        fields: String? = null,
        order: String? = null
    ): List<Esse3SyllabusTeachingUnitTranscript> {
        return executeJsonGetList<Esse3SyllabusTeachingUnitTranscript>("/libretti/${matId}/righe/${activityChoiceId}/syllabus/UD", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * Tutti i segmenti delle attività didattiche di un libretto
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param nonDeletableActivities se 1, recupera le attività didattiche non cancellabili del libretto. Default a 0
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     */
    suspend fun getRecordBookSegments(
        matId: Long,
        nonDeletableActivities: Int? = null,
        fields: String? = null,
        order: String? = null,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3TranscriptSegment> {
        return executeJsonGetList<Esse3TranscriptSegment>("/libretti/${matId}/segmenti", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            nonDeletableActivities?.let { parameter("adNonCancellabili", it) }
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    /**
     * Tutte le medie del libretto
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param initialAveragesReferenceDate data di riferimento iniziale per il calcolo delle medie
     * @param finalAveragesReferenceDate data di riferimento finale per il calcolo delle medie
     * @param pathRulesReason tipo di regole da utilizzare (default OFFF)
     * @param dataOriginReason tipo di origine dati da utilizzare (default LIBRETTO_AD_SUPERATE)
     */
    suspend fun getRecordBookStats(
        matId: Long,
        optionalFields: String? = null,
        initialAveragesReferenceDate: String? = null,
        finalAveragesReferenceDate: String? = null,
        pathRulesReason: Esse3RecordBookStatsRuleFilter? = null,
        dataOriginReason: Esse3RecordBookStatsDataOriginFilter? = null
    ): Esse3TranscriptStats {
        return executeJsonGet<Esse3TranscriptStats>("/libretti/${matId}/stats", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            optionalFields?.let { parameter("optionalFields", it) }
            initialAveragesReferenceDate?.let { parameter("dataRifMedieIni", it) }
            finalAveragesReferenceDate?.let { parameter("dataRifMedieFin", it) }
            pathRulesReason?.let { parameter("motRegolePercorso", it.value) }
            dataOriginReason?.let { parameter("motOrigineDati", it.value) }
        }
    }
}
