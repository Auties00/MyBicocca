package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel
import it.attendance100.mybicocca.data.dto.esse3.Esse3CareerPortion
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3PostPlanBody
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudyPlan
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudyPlanHeader
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudyPlansStatistics
import kotlinx.serialization.json.Json

class Esse3PlansApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/piani-service-v1") {

    /**
     * matricole in cui è possibile trovare un piano di studio
     *
     * @param matricola codice della matricola dello studente
     * @param courseOfStudyStudentId id del corso di studio di appartenenza dello studente
     * @param courseOfStudyStudentCode codice del corso di studio di appartenenza dello studente
     * @param academicYearOrderStudentId anno di ordinamento del regolamento del piano
     * @param studyPlanStudentId id del percorso di studio di appartenenza dello studente
     * @param studyPlanStudentCode codice del percorso di studio di appartenenza dello studente
     * @param cohort coorte del regolamento di scelta collegato al piano
     * @param fiscalCode codice fiscale dello studente
     * @param studentStatusCode codice dello stato della carriera a cui si riferisce il libretto
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getCareerSegments(
        matricola: String? = null,
        courseOfStudyStudentId: Long? = null,
        courseOfStudyStudentCode: String? = null,
        academicYearOrderStudentId: Int? = null,
        studyPlanStudentId: Long? = null,
        studyPlanStudentCode: String? = null,
        cohort: Int? = null,
        fiscalCode: String? = null,
        studentStatusCode: String? = null,
        optionalFields: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null
    ): List<Esse3CareerPortion> {
        return executeJsonGetList<Esse3CareerPortion>("/piani", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            matricola?.let { parameter("matricola", it) }
            courseOfStudyStudentId?.let { parameter("cdsStuId", it) }
            courseOfStudyStudentCode?.let { parameter("cdsStuCod", it) }
            academicYearOrderStudentId?.let { parameter("aaOrdStuId", it) }
            studyPlanStudentId?.let { parameter("pdsStuId", it) }
            studyPlanStudentCode?.let { parameter("pdsStuCod", it) }
            cohort?.let { parameter("coorte", it) }
            fiscalCode?.let { parameter("codiceFiscale", it) }
            studentStatusCode?.let { parameter("staStuCod", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    /**
     * Recupera la le statitiche dei piano
     *
     * @param facultyCode codice facoltà
     * @param cohort coorte del regolamento di scelta collegato al piano
     * @param minimumCohort coorte minima del regolamento di scelta collegato al piano
     * @param courseTypeCode codice del tipo corso di studio
     * @param courseOfStudyCode codice del corso di studio
     */
    suspend fun getPlansStatistics(
        facultyCode: String? = null,
        cohort: Int? = null,
        minimumCohort: Int? = null,
        courseTypeCode: String? = null,
        courseOfStudyCode: String? = null
    ): List<Esse3StudyPlansStatistics> {
        return executeJsonGetList<Esse3StudyPlansStatistics>("/piani/stats", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            facultyCode?.let { parameter("facCod", it) }
            cohort?.let { parameter("coorte", it) }
            minimumCohort?.let { parameter("coorteMin", it) }
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
        }
    }

    /**
     * recupera le testate del piano di una carriera
     *
     * @param studentId id della carriera dello studente
     * @param planState stato del piano (B => Bozza, P => Proposto, V => in Valutuazione, A => Approvato, R => Respinto, X => Annullato)
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getStudentPlanHeaders(
        studentId: Long,
        planState: List<String>? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3StudyPlanHeader> {
        return executeJsonGetList<Esse3StudyPlanHeader>("/piani/${studentId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            planState?.let { parameter("statoPiano", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * Inserisce un nuovo piano di studio
     *
     * @param studentId id della carriera dello studente
     * @param body Oggetto con i dati per la creazione del piano di studio
     */
    suspend fun postStudentPlan(
        studentId: Long,
        body: Esse3PostPlanBody
    ) {
        val response = executePost("/piani/${studentId}") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * recupera il piano di uno studente
     *
     * @param studentId id della carriera dello studente
     * @param planId id della testata del piano dello studente (progressivo rispetto allo stu_id)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param filter il parametro consente di applicare dei filtri alla classe di modello utilizzando il linguaggio  [RSQL](https://github.com/jirutka/rsql-parser). La lista degli operatori utilizzabili è disponibile [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosullerigherecuperate) *NB* il filtro viene applicato DOPO aver recuperato i dati
     */
    suspend fun getStudentPlan(
        studentId: Long,
        planId: Long,
        fields: String? = null,
        optionalFields: String? = null,
        filter: String? = null
    ): Esse3StudyPlan {
        return executeJsonGet<Esse3StudyPlan>("/piani/${studentId}/${planId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    /**
     * Recupera la stampa del piano
     *
     * @param studentId id della carriera dello studente
     * @param planId id della testata del piano dello studente (progressivo rispetto allo stu_id)
     */
    suspend fun getPlanPrint(
        studentId: Long,
        planId: Long
    ): ByteReadChannel {
        return executeStreamGet("/piani/${studentId}/${planId}/stampa", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER))
    }
}
