package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3ActivityLog
import it.attendance100.mybicocca.data.dto.esse3.Esse3ActivityLogWithSyllabus
import it.attendance100.mybicocca.data.dto.esse3.Esse3Building
import it.attendance100.mybicocca.data.dto.esse3.Esse3Classroom
import it.attendance100.mybicocca.data.dto.esse3.Esse3CoverageDeletable
import it.attendance100.mybicocca.data.dto.esse3.Esse3DeletedLogistics
import it.attendance100.mybicocca.data.dto.esse3.Esse3EasystaffActivityLogWithDetails
import it.attendance100.mybicocca.data.dto.esse3.Esse3EasystaffCourseOrderWithDetails
import it.attendance100.mybicocca.data.dto.esse3.Esse3LogisticsPerTeacher
import it.attendance100.mybicocca.data.dto.esse3.Esse3LogisticsWithDetails
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3SyllabusActivityPatch
import it.attendance100.mybicocca.data.dto.esse3.Esse3SyllabusActivityPatchResult
import it.attendance100.mybicocca.data.dto.esse3.Esse3SystemLogImportResult
import it.attendance100.mybicocca.data.dto.esse3.Esse3TeachingUnitLogWithDetails
import kotlinx.serialization.json.Json

class Esse3LogisticsApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/logistica-service-v1") {

    /**
     * informazioni logistiche per easystaff.
     *
     * @param academicYearOfferId Id dell'anno di offerta
     * @param departmentCode codice del dipartimento
     * @param courseOfStudyList lista dei codici corso da estrarre separata da ,
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getEasystaffLogistics(
        academicYearOfferId: Int,
        departmentCode: String? = null,
        courseOfStudyList: String? = null,
        optionalFields: String? = null
    ): List<Esse3EasystaffActivityLogWithDetails> {
        return executeJsonGetList<Esse3EasystaffActivityLogWithDetails>("/easystaff/logistica/${academicYearOfferId}", setOf(Esse3PermissionLevel.ANY)) {
            departmentCode?.let { parameter("dipCod", it) }
            courseOfStudyList?.let { parameter("listaCds", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * informazioni di struttura per easystaff.
     *
     * @param academicYearOfferId Id dell'anno di offerta
     * @param departmentCode codice del dipartimento
     * @param courseOfStudyList lista dei codici corso da estrarre separata da ,
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getEasystaffStructure(
        academicYearOfferId: Int,
        departmentCode: String? = null,
        courseOfStudyList: String? = null,
        optionalFields: String? = null
    ): List<Esse3EasystaffCourseOrderWithDetails> {
        return executeJsonGetList<Esse3EasystaffCourseOrderWithDetails>("/easystaff/struttura/${academicYearOfferId}", setOf(Esse3PermissionLevel.ANY)) {
            departmentCode?.let { parameter("dipCod", it) }
            courseOfStudyList?.let { parameter("listaCds", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * Recupera la lista dei record della tabella di testata P09_AD_LOG, filtrati per i parametri opzionali. I parametri lavorano solo sulla AD FISICA della condivisione.
     *
     * @param academicYearOfferId Id dell'anno di offerta
     * @param courseOfStudyCode codice del corso di studio
     * @param courseOfStudyDescription descrizione del corso di studio (se viene utilizzato il carattere * viene applicato il like)
     * @param academicYearOrderId Id dell'ordinamento
     * @param studyPlanId id del percorso di studio
     * @param studyPlanCode codice del percorso di studio
     * @param studyPlanDescription descrizione del percorso di studio (se viene utilizzato il carattere * viene applicato il like)
     * @param activityId id dell'attivita didattica
     * @param activityCode codice dell'attivita didattica
     * @param activityDescription descrizione  dell'attivita didattica (se viene utilizzato il carattere * viene applicato il like)
     * @param teachingLanguageCode codice ISO6392 della lingua di erogazione della didattica
     * @param siteDescription descrizione della sede (se viene utilizzato il carattere * viene applicato il like)
     * @param logModificationDate data di ultima modifica della logistica
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getLogistics(
        academicYearOfferId: Int? = null,
        courseOfStudyCode: String? = null,
        courseOfStudyDescription: String? = null,
        academicYearOrderId: Int? = null,
        studyPlanId: Long? = null,
        studyPlanCode: String? = null,
        studyPlanDescription: String? = null,
        activityId: Long? = null,
        activityCode: String? = null,
        activityDescription: String? = null,
        teachingLanguageCode: String? = null,
        siteDescription: String? = null,
        logModificationDate: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3ActivityLog> {
        return executeJsonGetList<Esse3ActivityLog>("/logistica", setOf(Esse3PermissionLevel.ANY)) {
            academicYearOfferId?.let { parameter("aaOffId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseOfStudyDescription?.let { parameter("cdsDes", it) }
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanId?.let { parameter("pdsId", it) }
            studyPlanCode?.let { parameter("pdsCod", it) }
            studyPlanDescription?.let { parameter("pdsDes", it) }
            activityId?.let { parameter("adId", it) }
            activityCode?.let { parameter("adCod", it) }
            activityDescription?.let { parameter("adDes", it) }
            teachingLanguageCode?.let { parameter("linguaDidCod", it) }
            siteDescription?.let { parameter("sedeDes", it) }
            logModificationDate?.let { parameter("dataModLog", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * Indica se una copertura risulta cancellabile.
     *
     * @param coverageId identificativo copertura
     * @param matricola codice della matricola dello studente
     * @param academicYearOfferId identificativo dell'anno di offerta
     * @param courseOfStudyCode codice del corso di studio
     * @param academicYearOrderId Id dell'ordinamento
     * @param studyPlanCode codice del percorso di studio
     * @param activityCode codice dell'attivita didattica
     * @param teachingUnitCode codice dell'unità didattica
     * @param domicilePartialCode Codice dominio di partizione degli studenti all'interno di un fattore di partizione
     */
    suspend fun getCancellableCoverage(
        coverageId: Long? = null,
        matricola: String? = null,
        academicYearOfferId: Int? = null,
        courseOfStudyCode: String? = null,
        academicYearOrderId: Int? = null,
        studyPlanCode: String? = null,
        activityCode: String? = null,
        teachingUnitCode: String? = null,
        domicilePartialCode: String? = null
    ): Esse3CoverageDeletable {
        return executeJsonGet<Esse3CoverageDeletable>("/logistica/copertura/cancellabile", setOf(Esse3PermissionLevel.ANY)) {
            coverageId?.let { parameter("coperId", it) }
            matricola?.let { parameter("matricola", it) }
            academicYearOfferId?.let { parameter("aaOffId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanCode?.let { parameter("pdsCod", it) }
            activityCode?.let { parameter("adCod", it) }
            teachingUnitCode?.let { parameter("udCod", it) }
            domicilePartialCode?.let { parameter("domPartCod", it) }
        }
    }

    /**
     * Aggiorna le informazioni del syllabus associate alla logistica in chiave (completa o parziale). Se non specificati i codici di dominio e/o partizione, verranno utilizzate le informazioni fornite per identificare le entità da aggiornare. In caso di UTENTE_TECNICO, è necessario valorizzare il campo docenteMatricola.
     *
     * @param body Informazioni del syllabus da aggiornare. I campi opzionali non indicati non verranno considerati in fase di aggiornamento.
     * @param lecturerMatricola matricola del docente
     */
    suspend fun putSyllabusTeachingActivity(
        body: Esse3SyllabusActivityPatch,
        lecturerMatricola: String? = null
    ): Esse3SyllabusActivityPatchResult {
        return executeJsonPut<Esse3SyllabusActivityPatchResult>("/logistica/syllabusAD", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            lecturerMatricola?.let { parameter("docenteMatricola", it) }
        }
    }

    /**
     * Recupera il syllabus  della logistica in chiave. I parametri opzionali filtrano una AD qualsiasi della condivisione.
     *
     * @param activityLogId id della logistica
     * @param academicYearOfferId Id dell'anno di offerta
     * @param courseOfStudyCode codice del corso di studio
     * @param courseOfStudyDescription descrizione del corso di studio (se viene utilizzato il carattere * viene applicato il like)
     * @param academicYearOrderId Id dell'ordinamento
     * @param studyPlanId id del percorso di studio
     * @param studyPlanCode codice del percorso di studio
     * @param studyPlanDescription descrizione del percorso di studio (se viene utilizzato il carattere * viene applicato il like)
     * @param activityCode codice dell'attivita didattica
     * @param activityDescription descrizione  dell'attivita didattica (se viene utilizzato il carattere * viene applicato il like)
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getTeachingActivityLogWithSyllabus(
        activityLogId: Long,
        academicYearOfferId: Int? = null,
        courseOfStudyCode: String? = null,
        courseOfStudyDescription: String? = null,
        academicYearOrderId: Int? = null,
        studyPlanId: Long? = null,
        studyPlanCode: String? = null,
        studyPlanDescription: String? = null,
        activityCode: String? = null,
        activityDescription: String? = null,
        order: String? = null,
        optionalFields: String? = null
    ): List<Esse3ActivityLogWithSyllabus> {
        return executeJsonGetList<Esse3ActivityLogWithSyllabus>("/logistica/${activityLogId}/adLogConSyllabus", setOf(Esse3PermissionLevel.ANY)) {
            academicYearOfferId?.let { parameter("aaOffId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseOfStudyDescription?.let { parameter("cdsDes", it) }
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanId?.let { parameter("pdsId", it) }
            studyPlanCode?.let { parameter("pdsCod", it) }
            studyPlanDescription?.let { parameter("pdsDes", it) }
            activityCode?.let { parameter("adCod", it) }
            activityDescription?.let { parameter("adDes", it) }
            order?.let { parameter("order", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * Recupera i dettagli  delle UD della logistica in chiave, ossia il syllabus delle UD e il carico docenti. I parametri opzionali filtrano una UD qualsiasi della condivisione.
     *
     * @param activityLogId id della logistica
     * @param academicYearOfferId Id dell'anno di offerta
     * @param courseOfStudyCode codice del corso di studio
     * @param courseOfStudyDescription descrizione del corso di studio (se viene utilizzato il carattere * viene applicato il like)
     * @param academicYearOrderId Id dell'ordinamento
     * @param studyPlanId id del percorso di studio
     * @param studyPlanCode codice del percorso di studio
     * @param studyPlanDescription descrizione del percorso di studio (se viene utilizzato il carattere * viene applicato il like)
     * @param activityCode codice dell'attivita didattica
     * @param activityDescription descrizione  dell'attivita didattica (se viene utilizzato il carattere * viene applicato il like)
     * @param teachingUnitCode codice dell'unità didattica
     * @param teachingUnitDescription descrizione dell'unit� didattica (se viene utilizzato il carattere * viene applicato il like)
     * @param lecturerMatricola matricola del docente
     * @param lecturerSurname cognome del docente
     * @param lecturerName nome del docente
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getTeachingUnitLogWithDetails(
        activityLogId: Long,
        academicYearOfferId: Int? = null,
        courseOfStudyCode: String? = null,
        courseOfStudyDescription: String? = null,
        academicYearOrderId: Int? = null,
        studyPlanId: Long? = null,
        studyPlanCode: String? = null,
        studyPlanDescription: String? = null,
        activityCode: String? = null,
        activityDescription: String? = null,
        teachingUnitCode: String? = null,
        teachingUnitDescription: String? = null,
        lecturerMatricola: String? = null,
        lecturerSurname: String? = null,
        lecturerName: String? = null,
        order: String? = null,
        optionalFields: String? = null
    ): List<Esse3TeachingUnitLogWithDetails> {
        return executeJsonGetList<Esse3TeachingUnitLogWithDetails>("/logistica/${activityLogId}/udLogConDettagli", setOf(Esse3PermissionLevel.ANY)) {
            academicYearOfferId?.let { parameter("aaOffId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseOfStudyDescription?.let { parameter("cdsDes", it) }
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanId?.let { parameter("pdsId", it) }
            studyPlanCode?.let { parameter("pdsCod", it) }
            studyPlanDescription?.let { parameter("pdsDes", it) }
            activityCode?.let { parameter("adCod", it) }
            activityDescription?.let { parameter("adDes", it) }
            teachingUnitCode?.let { parameter("udCod", it) }
            teachingUnitDescription?.let { parameter("udDes", it) }
            lecturerMatricola?.let { parameter("docenteMatricola", it) }
            lecturerSurname?.let { parameter("docenteCognome", it) }
            lecturerName?.let { parameter("docenteNome", it) }
            order?.let { parameter("order", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * Recupera tutte le informazioni della logistica in chiave. I parametri opzionali filtrano una AD qualsiasi della condivisione.
     *
     * @param activityLogId id della logistica
     * @param academicYearOfferId Id dell'anno di offerta
     * @param courseOfStudyCode codice del corso di studio
     * @param courseOfStudyDescription descrizione del corso di studio (se viene utilizzato il carattere * viene applicato il like)
     * @param academicYearOrderId Id dell'ordinamento
     * @param studyPlanId id del percorso di studio
     * @param studyPlanCode codice del percorso di studio
     * @param studyPlanDescription descrizione del percorso di studio (se viene utilizzato il carattere * viene applicato il like)
     * @param activityCode codice dell'attivita didattica
     * @param activityDescription descrizione  dell'attivita didattica (se viene utilizzato il carattere * viene applicato il like)
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getFullLogistics(
        activityLogId: Long,
        academicYearOfferId: Int? = null,
        courseOfStudyCode: String? = null,
        courseOfStudyDescription: String? = null,
        academicYearOrderId: Int? = null,
        studyPlanId: Long? = null,
        studyPlanCode: String? = null,
        studyPlanDescription: String? = null,
        activityCode: String? = null,
        activityDescription: String? = null,
        order: String? = null,
        optionalFields: String? = null
    ): List<Esse3LogisticsWithDetails> {
        return executeJsonGetList<Esse3LogisticsWithDetails>("/logisticaFull/${activityLogId}/", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            academicYearOfferId?.let { parameter("aaOffId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseOfStudyDescription?.let { parameter("cdsDes", it) }
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanId?.let { parameter("pdsId", it) }
            studyPlanCode?.let { parameter("pdsCod", it) }
            studyPlanDescription?.let { parameter("pdsDes", it) }
            activityCode?.let { parameter("adCod", it) }
            activityDescription?.let { parameter("adDes", it) }
            order?.let { parameter("order", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * Recupero logistica per corso di studio.
     *
     * @param academicYearOfferId Id dell'anno di offerta
     * @param courseOfStudyId identificativo del corso di studio
     * @param courseOfStudyCode codice del corso di studio
     * @param courseOfStudyDescription descrizione del corso di studio (se viene utilizzato il carattere * viene applicato il like)
     * @param academicYearOrderId Id dell'ordinamento
     * @param studyPlanId id del percorso di studio
     * @param studyPlanCode codice del percorso di studio
     * @param studyPlanDescription descrizione del percorso di studio (se viene utilizzato il carattere * viene applicato il like)
     * @param activityCode codice dell'attivita didattica
     * @param activityDescription descrizione  dell'attivita didattica (se viene utilizzato il carattere * viene applicato il like)
     * @param logModificationDate data di ultima modifica della logistica
     * @param teachingActivityPublicationFlag Flag che indica se le descrizioni delle attività didattiche sono pubblicabili
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getFullLogisticsByTeachingActivity(
        academicYearOfferId: Int,
        courseOfStudyId: Long? = null,
        courseOfStudyCode: String? = null,
        courseOfStudyDescription: String? = null,
        academicYearOrderId: Int? = null,
        studyPlanId: Long? = null,
        studyPlanCode: String? = null,
        studyPlanDescription: String? = null,
        activityCode: String? = null,
        activityDescription: String? = null,
        logModificationDate: String? = null,
        teachingActivityPublicationFlag: Int? = null,
        order: String? = null,
        optionalFields: String? = null
    ): List<Esse3LogisticsWithDetails> {
        return executeJsonGetList<Esse3LogisticsWithDetails>("/logisticaPerAdFull/${academicYearOfferId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            courseOfStudyId?.let { parameter("cdsId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseOfStudyDescription?.let { parameter("cdsDes", it) }
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanId?.let { parameter("pdsId", it) }
            studyPlanCode?.let { parameter("pdsCod", it) }
            studyPlanDescription?.let { parameter("pdsDes", it) }
            activityCode?.let { parameter("adCod", it) }
            activityDescription?.let { parameter("adDes", it) }
            logModificationDate?.let { parameter("dataModLog", it) }
            teachingActivityPublicationFlag?.let { parameter("desAdPubblFlg", it) }
            order?.let { parameter("order", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * Recupero logistica per docente.
     *
     * @param lecturerId id del docente
     * @param lecturerMatricola matricola del docente
     * @param lecturerSurname cognome del docente
     * @param userId id univoco che consente di individuare l'account utente
     * @param academicYearOfferId Id dell'anno di offerta
     * @param activityLogId id della logistica
     * @param tcDocumentModificationDate data di ultima modifica della logistica per docente (recupera le modifiche alla anagrafica oppure al carico docente)
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getLogisticsByLecturer(
        lecturerId: Long? = null,
        lecturerMatricola: String? = null,
        lecturerSurname: String? = null,
        userId: String? = null,
        academicYearOfferId: Int? = null,
        activityLogId: Long? = null,
        tcDocumentModificationDate: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3LogisticsPerTeacher> {
        return executeJsonGetList<Esse3LogisticsPerTeacher>("/logisticaPerDocente", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            lecturerId?.let { parameter("docenteId", it) }
            lecturerMatricola?.let { parameter("docenteMatricola", it) }
            lecturerSurname?.let { parameter("docenteCognome", it) }
            userId?.let { parameter("userId", it) }
            academicYearOfferId?.let { parameter("aaOffId", it) }
            activityLogId?.let { parameter("adLogId", it) }
            tcDocumentModificationDate?.let { parameter("dataModTCDoc", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * Recupero logistica per corso di studio. Versione con transfer-encoding.
     *
     * @param academicYearOfferId Id dell'anno di offerta
     * @param courseOfStudyOfferId id del corso di studio
     * @param courseOfStudyCode codice del corso di studio
     * @param courseOfStudyDescription descrizione del corso di studio (se viene utilizzato il carattere * viene applicato il like)
     * @param academicYearOrderId Id dell'ordinamento
     * @param studyPlanId id del percorso di studio
     * @param studyPlanCode codice del percorso di studio
     * @param studyPlanDescription descrizione del percorso di studio (se viene utilizzato il carattere * viene applicato il like)
     * @param activityCode codice dell'attivita didattica
     * @param activityDescription descrizione  dell'attivita didattica (se viene utilizzato il carattere * viene applicato il like)
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getFullLogisticsByOdStreamed(
        academicYearOfferId: Int,
        courseOfStudyOfferId: Long,
        courseOfStudyCode: String? = null,
        courseOfStudyDescription: String? = null,
        academicYearOrderId: Int? = null,
        studyPlanId: Long? = null,
        studyPlanCode: String? = null,
        studyPlanDescription: String? = null,
        activityCode: String? = null,
        activityDescription: String? = null,
        order: String? = null,
        optionalFields: String? = null
    ): List<Esse3LogisticsWithDetails> {
        return executeJsonGetList<Esse3LogisticsWithDetails>("/logisticaPerOdFull/streamed/${academicYearOfferId}/${courseOfStudyOfferId}/", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseOfStudyDescription?.let { parameter("cdsDes", it) }
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanId?.let { parameter("pdsId", it) }
            studyPlanCode?.let { parameter("pdsCod", it) }
            studyPlanDescription?.let { parameter("pdsDes", it) }
            activityCode?.let { parameter("adCod", it) }
            activityDescription?.let { parameter("adDes", it) }
            order?.let { parameter("order", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * Recupero logistica per corso di studio.
     *
     * @param academicYearOfferId Id dell'anno di offerta
     * @param courseOfStudyOfferId id del corso di studio
     * @param courseOfStudyCode codice del corso di studio
     * @param courseOfStudyDescription descrizione del corso di studio (se viene utilizzato il carattere * viene applicato il like)
     * @param academicYearOrderId Id dell'ordinamento
     * @param studyPlanId id del percorso di studio
     * @param studyPlanCode codice del percorso di studio
     * @param studyPlanDescription descrizione del percorso di studio (se viene utilizzato il carattere * viene applicato il like)
     * @param activityCode codice dell'attivita didattica
     * @param activityDescription descrizione  dell'attivita didattica (se viene utilizzato il carattere * viene applicato il like)
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getFullLogisticsByOd(
        academicYearOfferId: Int,
        courseOfStudyOfferId: Long,
        courseOfStudyCode: String? = null,
        courseOfStudyDescription: String? = null,
        academicYearOrderId: Int? = null,
        studyPlanId: Long? = null,
        studyPlanCode: String? = null,
        studyPlanDescription: String? = null,
        activityCode: String? = null,
        activityDescription: String? = null,
        order: String? = null,
        optionalFields: String? = null
    ): List<Esse3LogisticsWithDetails> {
        return executeJsonGetList<Esse3LogisticsWithDetails>("/logisticaPerOdFull/${academicYearOfferId}/${courseOfStudyOfferId}/", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseOfStudyDescription?.let { parameter("cdsDes", it) }
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanId?.let { parameter("pdsId", it) }
            studyPlanCode?.let { parameter("pdsCod", it) }
            studyPlanDescription?.let { parameter("pdsDes", it) }
            activityCode?.let { parameter("adCod", it) }
            activityDescription?.let { parameter("adDes", it) }
            order?.let { parameter("order", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * @param logModificationDate data di ultima modifica della logistica
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getDeletedLogistics(
        logModificationDate: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3DeletedLogistics> {
        return executeJsonGetList<Esse3DeletedLogistics>("/logisticheEliminate", setOf(Esse3PermissionLevel.ANY)) {
            logModificationDate?.let { parameter("dataModLog", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * lista degli edifici
     *
     * @param classroomCode codice dell'aula (se viene utilizzato il carattere * viene applicato il like)
     * @param externalClassroomCode codice dell'aula nel sistema esterno (se viene utilizzato il carattere * viene applicato il like)
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getAllClassrooms(
        classroomCode: String? = null,
        externalClassroomCode: String? = null,
        optionalFields: String? = null,
        fields: String? = null,
        order: String? = null
    ): List<Esse3Classroom> {
        return executeJsonGetList<Esse3Classroom>("/risFisse/aule", setOf(Esse3PermissionLevel.ANY)) {
            classroomCode?.let { parameter("aulaCod", it) }
            externalClassroomCode?.let { parameter("extAulaCod", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * lista degli edifici
     *
     * @param filter il parametro consente di applicare dei filtri alla classe di modello utilizzando il linguaggio  [RSQL](https://github.com/jirutka/rsql-parser). La lista degli operatori utilizzabili è disponibile [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosullerigherecuperate) *NB* il filtro viene applicato DOPO aver recuperato i dati
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getBuildings(
        filter: String? = null,
        optionalFields: String? = null,
        fields: String? = null,
        order: String? = null
    ): List<Esse3Building> {
        return executeJsonGetList<Esse3Building>("/risFisse/edifici", setOf(Esse3PermissionLevel.ANY)) {
            filter?.let { parameter("filter", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * dettaglio del singolo edificio
     *
     * @param buildingId id dell'edificio
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getBuilding(
        buildingId: Long,
        optionalFields: String? = null,
        fields: String? = null
    ): Esse3Building {
        return executeJsonGet<Esse3Building>("/risFisse/edifici/${buildingId}", setOf(Esse3PermissionLevel.ANY)) {
            optionalFields?.let { parameter("optionalFields", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    /**
     * lista delle aule di un edificio
     *
     * @param buildingId id dell'edificio
     * @param filter il parametro consente di applicare dei filtri alla classe di modello utilizzando il linguaggio  [RSQL](https://github.com/jirutka/rsql-parser). La lista degli operatori utilizzabili è disponibile [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosullerigherecuperate) *NB* il filtro viene applicato DOPO aver recuperato i dati
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getClassrooms(
        buildingId: Long,
        filter: String? = null,
        optionalFields: String? = null,
        fields: String? = null,
        order: String? = null
    ): List<Esse3Classroom> {
        return executeJsonGetList<Esse3Classroom>("/risFisse/edifici/${buildingId}/aule", setOf(Esse3PermissionLevel.ANY)) {
            filter?.let { parameter("filter", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * dettaglio della singola aula
     *
     * @param classroomId id dell'aula
     * @param buildingId id dell'edificio
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getClassroom(
        classroomId: Long,
        buildingId: Long,
        optionalFields: String? = null,
        fields: String? = null
    ): List<Esse3Classroom> {
        return executeJsonGetList<Esse3Classroom>("/risFisse/edifici/${buildingId}/aule/${classroomId}", setOf(Esse3PermissionLevel.ANY)) {
            optionalFields?.let { parameter("optionalFields", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    /**
     * sincronizzazione con il sistema di logisitica esterno
     */
    suspend fun syncSystemLog(): Esse3SystemLogImportResult {
        return executeJsonPut<Esse3SystemLogImportResult>("/risFisse/syncConSistLog", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }
}
