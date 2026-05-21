package it.attendance100.mybicocca.data.remote.esse3.api

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.parameters
import io.ktor.utils.io.ByteReadChannel
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3AcknowledgmentOfReceipt
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ActivitiesPerExamSession
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3BookingFilter
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3BookingModificationParameters
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3EnrollmentTag
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Esse3SystemLogCommitment
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExamCallSearchFilter
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExamSession
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExamSessionCommissionTeacher
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExamSessionEnrollment
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExamSessionEnrollmentParameters
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExamSessionInsert
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExamSessionSession
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExamSessionShift
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExamSessionTeacherAuthorization
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExamSessionWithDetails
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExamType
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3LecturerExamCallFilter
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3LinkedBookingOperation
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3MinutesState
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3OutcomesInsertionState
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3OutcomesPublicationState
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PermissionLevel
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PublicationParameters
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Session
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3SharedExam
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3SharedExamInsert
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3SharedExamResult
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3SharedExamSession
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ShiftCommissionTeacher
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3SystemLogEventTestExport
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3SystemLogExport
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3SystemLogImport
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3SystemLogImportResult
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3SystemLogSessionsExport
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TeacherAuthorizations
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3UpdateExamSession
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3UpdateResult
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3UpdateSystemLogCommitments
import kotlinx.serialization.json.Json

class Esse3ExamsCalendarApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/calesa-service-v1") {

    /**
     * recupera le informazioni sulle Ablitazioni docente
     *
     * @param courseOfStudyEnableId id del corso di studio di erogazione dell'esame comune
     * @param courseOfStudyEnableCode codice del corso di studio di erogazione dell'esame comune
     * @param activityAuthorizationId id dell'attività didattica di erogazione dell'esame comune
     * @param activityAuthorizationCode codice dell'attività didattica di erogazione dell'esame comune
     * @param academicYearOfferAuthorizationId anno di definizione dell'esame comune
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getLecturerAuthorizations(
        courseOfStudyEnableId: Long? = null,
        courseOfStudyEnableCode: String? = null,
        activityAuthorizationId: Long? = null,
        activityAuthorizationCode: String? = null,
        academicYearOfferAuthorizationId: Int? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3TeacherAuthorizations> {
        return executeJsonGetList<Esse3TeacherAuthorizations>("/abilitazioni", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            courseOfStudyEnableId?.let { parameter("cdsAbilId", it) }
            courseOfStudyEnableCode?.let { parameter("cdsAbilCod", it) }
            activityAuthorizationId?.let { parameter("adAbilId", it) }
            activityAuthorizationCode?.let { parameter("adAbilCod", it) }
            academicYearOfferAuthorizationId?.let { parameter("aaOffAbilId", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * recupera la lista di abilitazioni per un dato docente
     *
     * @param lecturerId id del docente
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getLecturerAuthorizationsByLecturer(
        lecturerId: Long,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3TeacherAuthorizations> {
        return executeJsonGetList<Esse3TeacherAuthorizations>("/abilitazioni/${lecturerId}/", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * recupera la lista degli appelli collegati alle abilitazioni docente
     *
     * @param lecturerId id del docente
     * @param minCallDate minima data dell'appello
     * @param maxCallDate minima data dell'appello
     * @param academicYearCalendarId anno di definizione del calendario esami
     * @param state stato dell'appello
     * @param outcomesInsertionState stato del processo di inserimento esiti
     * @param outcomesPublicationState stato del processo di pubblicazione esiti
     * @param minutesState stato del processo di verbalizzazione
     * @param recentFlag indica se l'appello è considerato recente oppure no
     * @param q il parametro consente di filtrare i campi con delle particolari condizioni predefinite, consultare la documentazione del metodo per verificare i codici che è possibile utilizzare
     *  Accepted values:
     *   - [Esse3LecturerExamCallFilter.AppelliMoodleQuiz]: recupera tutti gli appelli che risultano integrati con moodle per la gestione dei QUIZ
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getExamCallsByLecturerAuthorization(
        lecturerId: Long,
        minCallDate: String,
        maxCallDate: String,
        academicYearCalendarId: Int? = null,
        state: String? = null,
        outcomesInsertionState: Esse3OutcomesInsertionState? = null,
        outcomesPublicationState: Esse3OutcomesPublicationState? = null,
        minutesState: Esse3MinutesState? = null,
        recentFlag: Int? = null,
        q: Esse3LecturerExamCallFilter? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3ExamSessionTeacherAuthorization> {
        return executeJsonGetList<Esse3ExamSessionTeacherAuthorization>("/abilitazioni/${lecturerId}/appelli", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("minDataApp", minCallDate)
            parameter("maxDataApp", maxCallDate)
            academicYearCalendarId?.let { parameter("aaCalId", it) }
            state?.let { parameter("stato", it) }
            outcomesInsertionState?.let { parameter("statoInsEsiti", it.value) }
            outcomesPublicationState?.let { parameter("statoPubblEsiti", it.value) }
            minutesState?.let { parameter("statoVerb", it.value) }
            recentFlag?.let { parameter("recenteFlg", it) }
            q?.let { parameter("q", it.value) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * recupera la lista di abilitazioni docente per una coppia CDS/AD data
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param lecturerId id del docente
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getLecturerAuthorizationsCourseTeachingActivity(
        courseOfStudyId: Long,
        activityId: Long,
        lecturerId: Long,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3TeacherAuthorizations> {
        return executeJsonGetList<Esse3TeacherAuthorizations>("/abilitazioni/${lecturerId}/${courseOfStudyId}/${activityId}/", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * recupera una abilitazione docente per una coppia CDS/AD di un determinato docente
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param lecturerId id del docente
     * @param academicYearOfferTeacherAuthorizationId anno di abilitiazione del docente sulla abilita docenti
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getLecturerAuthorization(
        courseOfStudyId: Long,
        activityId: Long,
        lecturerId: Long,
        academicYearOfferTeacherAuthorizationId: Long,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3TeacherAuthorizations {
        return executeJsonGet<Esse3TeacherAuthorizations>("/abilitazioni/${lecturerId}/${courseOfStudyId}/${activityId}/${academicYearOfferTeacherAuthorizationId}", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * recupera delle possibili coppie AD/CDS sulle quali è possibile operare
     *
     * @param courseOfStudyEnableId id del corso di studio di erogazione dell'esame comune
     * @param courseOfStudyEnableCode codice del corso di studio di erogazione dell'esame comune
     * @param activityAuthorizationId id dell'attività didattica di erogazione dell'esame comune
     * @param activityAuthorizationCode codice dell'attività didattica di erogazione dell'esame comune
     * @param academicYearOfferAuthorizationId anno di definizione dell'esame comune
     * @param matricola codice della matricola dello studente
     * @param matId id del tratto di carriera di iscrizione dello studente
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getActivitiesForExamCalls(
        courseOfStudyEnableId: Long? = null,
        courseOfStudyEnableCode: String? = null,
        activityAuthorizationId: Long? = null,
        activityAuthorizationCode: String? = null,
        academicYearOfferAuthorizationId: Int? = null,
        matricola: String? = null,
        matId: Long? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3ActivitiesPerExamSession> {
        return executeJsonGetList<Esse3ActivitiesPerExamSession>("/appelli", setOf(Esse3PermissionLevel.STUDENT)) {
            courseOfStudyEnableId?.let { parameter("cdsAbilId", it) }
            courseOfStudyEnableCode?.let { parameter("cdsAbilCod", it) }
            activityAuthorizationId?.let { parameter("adAbilId", it) }
            activityAuthorizationCode?.let { parameter("adAbilCod", it) }
            academicYearOfferAuthorizationId?.let { parameter("aaOffAbilId", it) }
            matricola?.let { parameter("matricola", it) }
            matId?.let { parameter("matId", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * recupera la lista degli appelli di una coppia CDS/AD data
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param academicYearCalendarId anno di definizione del calendario esami
     * @param minCallDate data minima degli appelli sui quali calcolare le statistiche
     * @param maxCallDate data massima degli appelli sui quali calcolare le statistiche
     * @param state stato dell'appello
     * @param outcomesInsertionState stato del processo di inserimento esiti
     * @param outcomesPublicationState stato del processo di pubblicazione esiti
     * @param minutesState stato del processo di verbalizzazione
     * @param recentFlag indica se l'appello è considerato recente oppure no
     * @param personId Id dello studente di cui cercare gli appelli possibili. Valido solo per utente tecnico
     * @param actorCode tipo di attore richiesto per l'estrazione dati (STU,DOC,SEG), utilizzato per filtrare la configurazione dell'appello, se null viene impostata la configurazione dell'attore corrente
     * @param q il parametro consente di filtrare i campi con delle particolari condizioni predefinite, consultare la documentazione del metodo per verificare i codici che è possibile utilizzare
     *  Accepted values:
     *   - [Esse3ExamCallSearchFilter.AppelliPrenotabili]: recupera tutti gli appelli che non hanno nessuna prenotazione e risultano prenotabili (vale solo per user = STUDENTE e TECNICO)
     *   - [Esse3ExamCallSearchFilter.AppelliMoodleQuiz]: recupera tutti gli appelli che risultano integrati con moodle per la gestione dei QUIZ
     *   - [Esse3ExamCallSearchFilter.AppelliPrenotabiliEFuturi]: recupera tutti gli appelli che non hanno nessuna prenotazione e risultano prenotabili o futuri (vale solo per user = STUDENTE e TECNICO)
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getExamCalls(
        courseOfStudyId: Long,
        activityId: Long,
        academicYearCalendarId: Int? = null,
        minCallDate: String? = null,
        maxCallDate: String? = null,
        state: String? = null,
        outcomesInsertionState: Esse3OutcomesInsertionState? = null,
        outcomesPublicationState: Esse3OutcomesPublicationState? = null,
        minutesState: Esse3MinutesState? = null,
        recentFlag: Int? = null,
        personId: Long? = null,
        actorCode: String? = null,
        q: Esse3ExamCallSearchFilter? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3ExamSession> {
        return executeJsonGetList<Esse3ExamSession>("/appelli/${courseOfStudyId}/${activityId}/", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            academicYearCalendarId?.let { parameter("aaCalId", it) }
            minCallDate?.let { parameter("minDataApp", it) }
            maxCallDate?.let { parameter("maxDataApp", it) }
            state?.let { parameter("stato", it) }
            outcomesInsertionState?.let { parameter("statoInsEsiti", it.value) }
            outcomesPublicationState?.let { parameter("statoPubblEsiti", it.value) }
            minutesState?.let { parameter("statoVerb", it.value) }
            recentFlag?.let { parameter("recenteFlg", it) }
            personId?.let { parameter("persId", it) }
            actorCode?.let { parameter("attoreCod", it) }
            q?.let { parameter("q", it.value) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * inserisce un nuovo appello
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param body Oggetto che contiene la riga da inserire
     */
    suspend fun postExamCall(
        courseOfStudyId: Long,
        activityId: Long,
        body: Esse3ExamSessionInsert
    ) {
        val response = executePost("/appelli/${courseOfStudyId}/${activityId}/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * informazioni dell'appello
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param callId progressivo dell'appello all'interno della coppa CDS/AD di definizione; è parte della chiave (cds_id,ad_id,app_id)
     * @param personId Id dello studente di cui cercare gli appelli possibili. Valido solo per utente tecnico
     * @param actorCode tipo di attore richiesto per l'estrazione dati (STU,DOC,SEG), utilizzato per filtrare la configurazione dell'appello, se null viene impostata la configurazione dell'attore corrente
     * @param actorCodeExamType tipo di attore richiesto per l'estrazione dati (STU,DOC,SEG), utilizzato per filtrare le tipologie di svolgimento esame possibili per l'appello. Se null, viene impostata la configurazione dell'attore corrente
     * @param q il parametro consente di filtrare i campi con delle particolari condizioni predefinite, consultare la documentazione del metodo per verificare i codici che è possibile utilizzare
     *  Accepted values:
     *   - [Esse3ExamCallSearchFilter.AppelliPrenotabili]: recupera l'appello solo se non è presente la prenotazione dello studente e questo risulta prenotabile (utilizzabile solo per user=STUDENTE)
     *   - [Esse3ExamCallSearchFilter.AppelliMoodleQuiz]: recupera tutti gli appelli che risultano integrati con moodle per la gestione dei QUIZ
     *   - [Esse3ExamCallSearchFilter.AppelliPrenotabiliEFuturi]: recupera l'appello solo se non è presente la prenotazione dello studente e questo risulta prenotabile o futuri (utilizzabile solo per user=STUDENTE)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getExamCall(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        personId: Long? = null,
        actorCode: String? = null,
        actorCodeExamType: String? = null,
        q: Esse3ExamCallSearchFilter? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3ExamSessionWithDetails {
        return executeJsonGet<Esse3ExamSessionWithDetails>("/appelli/${courseOfStudyId}/${activityId}/${callId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            personId?.let { parameter("persId", it) }
            actorCode?.let { parameter("attoreCod", it) }
            actorCodeExamType?.let { parameter("attoreCodTipoSvolgEsame", it) }
            q?.let { parameter("q", it.value) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * aggiorna le informazioni di un appello
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param callId progressivo dell'appello all'interno della coppa CDS/AD di definizione; è parte della chiave (cds_id,ad_id,app_id)
     * @param body Oggetto che contiene la riga da inserire
     * @param actorCodeExamType tipo di attore richiesto per l'estrazione dati (STU,DOC,SEG), utilizzato per filtrare le tipologie di svolgimento esame possibili per l'appello. Se null, viene impostata la configurazione dell'attore corrente
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun putExamCall(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        body: Esse3UpdateExamSession,
        actorCodeExamType: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3ExamSessionWithDetails {
        return executeJsonPut<Esse3ExamSessionWithDetails>("/appelli/${courseOfStudyId}/${activityId}/${callId}", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            actorCodeExamType?.let { parameter("attoreCodTipoSvolgEsame", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * cancella un appello
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param callId progressivo dell'appello all'interno della coppa CDS/AD di definizione; è parte della chiave (cds_id,ad_id,app_id)
     * @param forceCancellation flag che consente di forzare la cancellazione
     */
    suspend fun deleteExamCall(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        forceCancellation: Long
    ) {
        val response = executeDelete("/appelli/${courseOfStudyId}/${activityId}/${callId}") {
            parameter("forzaCanc", forceCancellation)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Recupera la commissione dell'appello
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param callId progressivo dell'appello all'interno della coppa CDS/AD di definizione; è parte della chiave (cds_id,ad_id,app_id)
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getLecturersExamCall(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        order: String? = null,
        fields: String? = null
    ): List<Esse3ExamSessionCommissionTeacher> {
        return executeJsonGetList<Esse3ExamSessionCommissionTeacher>("/appelli/${courseOfStudyId}/${activityId}/${callId}/comm", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    /**
     * Recupera un docente della commissione
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param callId progressivo dell'appello all'interno della coppa CDS/AD di definizione; è parte della chiave (cds_id,ad_id,app_id)
     * @param lecturerId id del docente
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getLecturerExamCall(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        lecturerId: Long,
        fields: String? = null
    ): Esse3ExamSessionCommissionTeacher {
        return executeJsonGet<Esse3ExamSessionCommissionTeacher>("/appelli/${courseOfStudyId}/${activityId}/${callId}/comm/${lecturerId}", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
        }
    }

    /**
     * Recupera gli esami comuni associati all'appello
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param callId progressivo dell'appello all'interno della coppa CDS/AD di definizione; è parte della chiave (cds_id,ad_id,app_id)
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getCommonExamsExamCall(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        order: String? = null,
        fields: String? = null
    ): List<Esse3SharedExamSession> {
        return executeJsonGetList<Esse3SharedExamSession>("/appelli/${courseOfStudyId}/${activityId}/${callId}/esacom", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    /**
     * Recupera la lista iscritti degli studenti iscritti all'appello
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param callId progressivo dell'appello all'interno della coppa CDS/AD di definizione; è parte della chiave (cds_id,ad_id,app_id)
     * @param callLogId id progressivo del turno
     * @param actorCode tipo di attore richiesto per l'estrazione dati (STU,DOC), obbligatorio se l'autenticazione è di tipo USER_TECNICO
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param filter il parametro consente di applicare dei filtri alla classe di modello utilizzando il linguaggio  [RSQL](https://github.com/jirutka/rsql-parser). La lista degli operatori utilizzabili è disponibile [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosullerigherecuperate) *NB* il filtro viene applicato DOPO aver recuperato i dati
     */
    suspend fun getExamCallEnrolledList(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        callLogId: Int? = null,
        actorCode: String? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null,
        filter: String? = null
    ): List<Esse3ExamSessionEnrollment> {
        return executeJsonGetList<Esse3ExamSessionEnrollment>("/appelli/${courseOfStudyId}/${activityId}/${callId}/iscritti", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            callLogId?.let { parameter("appLogId", it) }
            actorCode?.let { parameter("attoreCod", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    /**
     * Effettua la prenotazione di uno studente all'appello
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param callId progressivo dell'appello all'interno della coppa CDS/AD di definizione; è parte della chiave (cds_id,ad_id,app_id)
     * @param body Oggetto che contiene la riga da inserire
     */
    suspend fun postExamCallEnrolledList(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        body: Esse3ExamSessionEnrollmentParameters
    ) {
        val response = executePost("/appelli/${courseOfStudyId}/${activityId}/${callId}/iscritti") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Recupera le informazioni della preonotazione di uno studente ad un appello
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param callId progressivo dell'appello all'interno della coppa CDS/AD di definizione; è parte della chiave (cds_id,ad_id,app_id)
     * @param studentId id della carriera dello studente
     * @param actorCode tipo di attore richiesto per l'estrazione dati (STU,DOC), obbligatorio se l'autenticazione è di tipo USER_TECNICO
     * @param q il parametro consente di filtrare i campi con delle particolari condizioni predefinite, consultare la documentazione del metodo per verificare i codici che è possibile utilizzare
     *  Accepted values:
     *   - [Esse3BookingFilter.BachecaEsiti]: prenotazioni visualizzate in bacheca esiti (appelli con pubblicazone, esiti pubblicati, data_ultimo_rif trascora, il verbale collegato non è stato generato)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getEnrolledExamCall(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        studentId: Long,
        actorCode: String? = null,
        q: Esse3BookingFilter? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3ExamSessionEnrollment {
        return executeJsonGet<Esse3ExamSessionEnrollment>("/appelli/${courseOfStudyId}/${activityId}/${callId}/iscritti/${studentId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            actorCode?.let { parameter("attoreCod", it) }
            q?.let { parameter("q", it.value) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * modifica una prenotazione di uno studente
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param callId progressivo dell'appello all'interno della coppa CDS/AD di definizione; è parte della chiave (cds_id,ad_id,app_id)
     * @param studentId id della carriera dello studente
     * @param body Oggetto che contiene le informazioni sulla prenotazione
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun putModifyBooking(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        studentId: Long,
        body: Esse3BookingModificationParameters,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3ExamSessionEnrollment {
        return executeJsonPut<Esse3ExamSessionEnrollment>("/appelli/${courseOfStudyId}/${activityId}/${callId}/iscritti/${studentId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * cancella una singola iscrizione dell'appello
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param callId progressivo dell'appello all'interno della coppa CDS/AD di definizione; è parte della chiave (cds_id,ad_id,app_id)
     * @param studentId id della carriera dello studente
     */
    suspend fun deleteExamCallEnrolledList(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        studentId: Long
    ) {
        val response = executeDelete("/appelli/${courseOfStudyId}/${activityId}/${callId}/iscritti/${studentId}")
        ensureSuccess(response, setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Recupera l'attestato di presenza
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param callId progressivo dell'appello all'interno della coppa CDS/AD di definizione; è parte della chiave (cds_id,ad_id,app_id)
     * @param studentId id della carriera dello studente
     */
    suspend fun getPresenceCertificate(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        studentId: Long
    ): ByteReadChannel {
        return executeStreamGet("/appelli/${courseOfStudyId}/${activityId}/${callId}/iscritti/${studentId}/attestato-di-presenza", setOf(Esse3PermissionLevel.STUDENT))
    }

    /**
     * inserisce un esito per uno studente
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param callId progressivo dell'appello all'interno della coppa CDS/AD di definizione; è parte della chiave (cds_id,ad_id,app_id)
     * @param studentId id della carriera dello studente
     * @param body Oggetto che contiene le informazioni sull'esito
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun putApplicationListOutcome(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        studentId: Long,
        body: Esse3UpdateResult,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3ExamSessionEnrollment {
        return executeJsonPut<Esse3ExamSessionEnrollment>("/appelli/${courseOfStudyId}/${activityId}/${callId}/iscritti/${studentId}/esito", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * effettua la presa visione dell'esito pubblicato
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param callId progressivo dell'appello all'interno della coppa CDS/AD di definizione; è parte della chiave (cds_id,ad_id,app_id)
     * @param studentId id della carriera dello studente
     * @param acknowledgmentOfReceipt presaVisone
     * @param linkedOperation tipo di operazione da applicare alla prenotazione collegata alla presa visione
     * @param linkedCourseOfStudyId cdsId di un appello collegato su cui effettuare la prenotazione
     * @param linkedActivityId adId di un appello collegato su cui effettuare la prenotazione
     * @param linkedCallId appId di un appello collegato su cui effettuare la prenotazione
     * @param linkedCallLogId id del turno su cui effettuare la prenotazione
     * @param linkedStudentNote nota dello studente per la prenotazione dell'appello collegato
     * @param linkedTagCode livello di uscita della lingua perl a prenotazione dell'appello collegato
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun putAcknowledgmentOfReceipt(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        studentId: Long,
        acknowledgmentOfReceipt: Esse3AcknowledgmentOfReceipt,
        linkedOperation: Esse3LinkedBookingOperation? = null,
        linkedCourseOfStudyId: Long? = null,
        linkedActivityId: Long? = null,
        linkedCallId: Long? = null,
        linkedCallLogId: Long? = null,
        linkedStudentNote: String? = null,
        linkedTagCode: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3ExamSessionEnrollment {
        return executeJsonPut<Esse3ExamSessionEnrollment>("/appelli/${courseOfStudyId}/${activityId}/${callId}/iscritti/${studentId}/presaVisione", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER)) {
            setBody(FormDataContent(parameters {
                append("presaVisione", acknowledgmentOfReceipt.value)
                linkedOperation?.let { append("appRelOpType", it.value) }
                linkedCourseOfStudyId?.let { append("cdsRelAppId", it.toString()) }
                linkedActivityId?.let { append("adRelAppId", it.toString()) }
                linkedCallId?.let { append("appRelAppId", it.toString()) }
                linkedCallLogId?.let { append("appLogRelAppId", it.toString()) }
                linkedStudentNote?.let { append("notaStuRelApp", it) }
                linkedTagCode?.let { append("tagCodRelApp", it) }
            }))
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * Recupera lo statino di prenotazione
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param callId progressivo dell'appello all'interno della coppa CDS/AD di definizione; è parte della chiave (cds_id,ad_id,app_id)
     * @param studentId id della carriera dello studente
     */
    suspend fun getBookingStatino(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        studentId: Long
    ): ByteReadChannel {
        return executeStreamGet("/appelli/${courseOfStudyId}/${activityId}/${callId}/iscritti/${studentId}/statino-prenotazione", setOf(Esse3PermissionLevel.STUDENT))
    }

    /**
     * inserisce la pubblicazione per gli studenti dell'appello
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param callId progressivo dell'appello all'interno della coppa CDS/AD di definizione; è parte della chiave (cds_id,ad_id,app_id)
     * @param body Oggetto che contiene le informazioni sull'esito
     */
    suspend fun putExamCallPublication(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        body: Esse3PublicationParameters
    ) {
        val response = executePut("/appelli/${courseOfStudyId}/${activityId}/${callId}/pubblicazione") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TEACHER))
    }

    /**
     * Recupera le sessioni associate all'appello
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param callId progressivo dell'appello all'interno della coppa CDS/AD di definizione; è parte della chiave (cds_id,ad_id,app_id)
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getExamCallSessions(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        order: String? = null,
        fields: String? = null
    ): List<Esse3ExamSessionSession> {
        return executeJsonGetList<Esse3ExamSessionSession>("/appelli/${courseOfStudyId}/${activityId}/${callId}/sessioni", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    /**
     * Recupera le sessioni associate all'appello
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param callId progressivo dell'appello all'interno della coppa CDS/AD di definizione; è parte della chiave (cds_id,ad_id,app_id)
     * @param academicYearSessionId anno di sessione
     * @param sessionId progressivo della sessione all'interno della coppia CDS/AASES; è parte della chiave (cds_id,aa_ses_id,ses_id)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getExamCallSession(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        academicYearSessionId: Long,
        sessionId: Long,
        fields: String? = null
    ): Esse3ExamSessionSession {
        return executeJsonGet<Esse3ExamSessionSession>("/appelli/${courseOfStudyId}/${activityId}/${callId}/sessioni/${academicYearSessionId}/${sessionId}", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
        }
    }

    /**
     * Recupera la lista dei tag validi per una determinata prenotazione
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param callId progressivo dell'appello all'interno della coppa CDS/AD di definizione; è parte della chiave (cds_id,ad_id,app_id)
     * @param activityChoiceId id della riga del libretto
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getTagsByBooking(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        activityChoiceId: Long,
        order: String? = null
    ): List<Esse3EnrollmentTag> {
        return executeJsonGetList<Esse3EnrollmentTag>("/appelli/${courseOfStudyId}/${activityId}/${callId}/tags/${activityChoiceId}", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            order?.let { parameter("order", it) }
        }
    }

    /**
     * Recupera i tipi svolgimento esame validi per l'appello
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param callId progressivo dell'appello all'interno della coppa CDS/AD di definizione; è parte della chiave (cds_id,ad_id,app_id)
     * @param actorCodeExamType tipo di attore richiesto per l'estrazione dati (STU,DOC,SEG), utilizzato per filtrare le tipologie di svolgimento esame possibili per l'appello. Se null, viene impostata la configurazione dell'attore corrente
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param filter il parametro consente di applicare dei filtri alla classe di modello utilizzando il linguaggio  [RSQL](https://github.com/jirutka/rsql-parser). La lista degli operatori utilizzabili è disponibile [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosullerigherecuperate) *NB* il filtro viene applicato DOPO aver recuperato i dati
     */
    suspend fun getExamCallTypes(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        actorCodeExamType: String? = null,
        order: String? = null,
        fields: String? = null,
        filter: String? = null
    ): List<Esse3ExamType> {
        return executeJsonGetList<Esse3ExamType>("/appelli/${courseOfStudyId}/${activityId}/${callId}/tipi-svolgimento-esame", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            actorCodeExamType?.let { parameter("attoreCodTipoSvolgEsame", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    /**
     * informazioni sui turni di un appello
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param callId progressivo dell'appello all'interno della coppa CDS/AD di definizione; è parte della chiave (cds_id,ad_id,app_id)
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getExamCallShifts(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3ExamSessionShift> {
        return executeJsonGetList<Esse3ExamSessionShift>("/appelli/${courseOfStudyId}/${activityId}/${callId}/turni", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * Recupera un turno dell'appello
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param callId progressivo dell'appello all'interno della coppa CDS/AD di definizione; è parte della chiave (cds_id,ad_id,app_id)
     * @param callLogId progressivo del turno all'interno dell'appello definito dalla terna CDS/AD/APP_ID; è parte della chiave (cds_id,ad_id,app_id,app_log_id)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getExamCallShift(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        callLogId: Long,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3ExamSessionShift {
        return executeJsonGet<Esse3ExamSessionShift>("/appelli/${courseOfStudyId}/${activityId}/${callId}/turni/${callLogId}", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * Recupera la commissione dell'appello
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param callId progressivo dell'appello all'interno della coppa CDS/AD di definizione; è parte della chiave (cds_id,ad_id,app_id)
     * @param callLogId progressivo del turno all'interno dell'appello definito dalla terna CDS/AD/APP_ID; è parte della chiave (cds_id,ad_id,app_id,app_log_id)
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getLecturersShift(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        callLogId: Long,
        order: String? = null,
        fields: String? = null
    ): List<Esse3ShiftCommissionTeacher> {
        return executeJsonGetList<Esse3ShiftCommissionTeacher>("/appelli/${courseOfStudyId}/${activityId}/${callId}/turni/${callLogId}/comm", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    /**
     * recupra il docente della commissione del turno
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param callId progressivo dell'appello all'interno della coppa CDS/AD di definizione; è parte della chiave (cds_id,ad_id,app_id)
     * @param callLogId progressivo del turno all'interno dell'appello definito dalla terna CDS/AD/APP_ID; è parte della chiave (cds_id,ad_id,app_id,app_log_id)
     * @param lecturerId id del docente
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getLecturerShift(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        callLogId: Long,
        lecturerId: Long,
        fields: String? = null
    ): Esse3ShiftCommissionTeacher {
        return executeJsonGet<Esse3ShiftCommissionTeacher>("/appelli/${courseOfStudyId}/${activityId}/${callId}/turni/${callLogId}/comm/${lecturerId}", setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
        }
    }

    /**
     * inserisce la pubblicazione per gli studenti del turno
     *
     * @param courseOfStudyId id del corso di studio di erogazione dell'appello
     * @param activityId id dell' attività didattica di erogazione dell'appello
     * @param callId progressivo dell'appello all'interno della coppa CDS/AD di definizione; è parte della chiave (cds_id,ad_id,app_id)
     * @param callLogId progressivo del turno all'interno dell'appello definito dalla terna CDS/AD/APP_ID; è parte della chiave (cds_id,ad_id,app_id,app_log_id)
     * @param body Oggetto che contiene le informazioni sull'esito
     */
    suspend fun putShiftPublication(
        courseOfStudyId: Long,
        activityId: Long,
        callId: Long,
        callLogId: Long,
        body: Esse3PublicationParameters
    ) {
        val response = executePut("/appelli/${courseOfStudyId}/${activityId}/${callId}/turni/${callLogId}/pubblicazione") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TEACHER))
    }

    /**
     * recupera un esame comune
     *
     * @param academicYearId anno accademico di definizione dell'esame comune
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getEsacomByAcademicYearId(
        academicYearId: Int,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null
    ): List<Esse3SharedExam> {
        return executeJsonGetList<Esse3SharedExam>("/esacom/${academicYearId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    /**
     * recupera un esame comune
     *
     * @param academicYearId anno accademico di definizione dell'esame comune
     * @param courseOfStudyGraduationId id del corso di studio padre dell'esame comune
     * @param activityExamId id dell' attività didattica padre dell'esame comune
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getEsacomParent(
        academicYearId: Int,
        courseOfStudyGraduationId: Long,
        activityExamId: Long,
        order: String? = null,
        fields: String? = null
    ): List<Esse3SharedExam> {
        return executeJsonGetList<Esse3SharedExam>("/esacom/${academicYearId}/${courseOfStudyGraduationId}/${activityExamId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    /**
     * recupera il figlio di esame comune
     *
     * @param academicYearId anno accademico di definizione dell'esame comune
     * @param courseOfStudyGraduationId id del corso di studio padre dell'esame comune
     * @param activityExamId id dell' attività didattica padre dell'esame comune
     * @param childCourseOfStudyId id del corso di studio figlio dell'esame comune
     * @param childActivityId id dell' attività didattia figlia dell'esame comune
     */
    suspend fun getEsacomChild(
        academicYearId: Int,
        courseOfStudyGraduationId: Long,
        activityExamId: Long,
        childCourseOfStudyId: Long,
        childActivityId: Long
    ): Esse3SharedExam {
        return executeJsonGet<Esse3SharedExam>("/esacom/${academicYearId}/${courseOfStudyGraduationId}/${activityExamId}/figli/${childCourseOfStudyId}/${childActivityId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * inserisce un figlio di esame comune
     *
     * @param academicYearId anno accademico di definizione dell'esame comune
     * @param courseOfStudyGraduationId id del corso di studio padre dell'esame comune
     * @param activityExamId id dell' attività didattica padre dell'esame comune
     * @param childCourseOfStudyId id del corso di studio figlio dell'esame comune
     * @param childActivityId id dell' attività didattia figlia dell'esame comune
     * @param body Oggetto che contiene i parametri necessari per l'inserimento
     * @param forceFlag consente di applicare la forzatura su determinati controlli forzabili
     */
    suspend fun putEsacomChild(
        academicYearId: Int,
        courseOfStudyGraduationId: Long,
        activityExamId: Long,
        childCourseOfStudyId: Long,
        childActivityId: Long,
        body: Esse3SharedExamInsert,
        forceFlag: Boolean? = null
    ): Esse3SharedExamResult {
        return executeJsonPut<Esse3SharedExamResult>("/esacom/${academicYearId}/${courseOfStudyGraduationId}/${activityExamId}/figli/${childCourseOfStudyId}/${childActivityId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            forceFlag?.let { parameter("forceFlg", it) }
        }
    }

    /**
     * inserisce un figlio di esame comune
     *
     * @param academicYearId anno accademico di definizione dell'esame comune
     * @param courseOfStudyGraduationId id del corso di studio padre dell'esame comune
     * @param activityExamId id dell' attività didattica padre dell'esame comune
     * @param childCourseOfStudyId id del corso di studio figlio dell'esame comune
     * @param childActivityId id dell' attività didattia figlia dell'esame comune
     * @param forceFlag consente di applicare la forzatura su determinati controlli forzabili
     */
    suspend fun deleteEsacomChild(
        academicYearId: Int,
        courseOfStudyGraduationId: Long,
        activityExamId: Long,
        childCourseOfStudyId: Long,
        childActivityId: Long,
        forceFlag: Boolean? = null
    ): Esse3SharedExamResult {
        return executeJsonDelete<Esse3SharedExamResult>("/esacom/${academicYearId}/${courseOfStudyGraduationId}/${activityExamId}/figli/${childCourseOfStudyId}/${childActivityId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            forceFlag?.let { parameter("forceFlg", it) }
        }
    }

    /**
     * Recupera le informazioni delle prenotazioni collegate da un tratto di carriera
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param actorCode tipo di attore richiesto per l'estrazione dati (STU,DOC), obbligatorio se l'autenticazione è di tipo USER_TECNICO
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
        q: Esse3BookingFilter? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null,
        filter: String? = null
    ): List<Esse3ExamSessionEnrollment> {
        return executeJsonGetList<Esse3ExamSessionEnrollment>("/prenotazioni/${matId}/", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            actorCode?.let { parameter("attoreCod", it) }
            q?.let { parameter("q", it.value) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    /**
     * Recupera le informazioni degli appelli prenotabili collegati da un tratto di carriera
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param applicationListId id univoco della prenotazione di uno studente
     * @param actorCode tipo di attore richiesto per l'estrazione dati (STU,DOC), obbligatorio se l'autenticazione è di tipo USER_TECNICO
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getBooking(
        matId: Long,
        applicationListId: Long,
        actorCode: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3ExamSessionEnrollment {
        return executeJsonGet<Esse3ExamSessionEnrollment>("/prenotazioni/${matId}/${applicationListId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            actorCode?.let { parameter("attoreCod", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * effettua la presa visione dell'esito pubblicato
     *
     * @param matId id del tratto di carriera per cui recuperare il libretto
     * @param applicationListId id univoco della prenotazione di uno studente
     * @param acknowledgmentOfReceipt presaVisone
     * @param linkedOperation tipo di operazione da applicare alla prenotazione collegata alla presa visione
     * @param linkedCourseOfStudyId cdsId di un appello collegato su cui effettuare la prenotazione
     * @param linkedActivityId adId di un appello collegato su cui effettuare la prenotazione
     * @param linkedCallId appId di un appello collegato su cui effettuare la prenotazione
     * @param linkedCallLogId id del turno su cui effettuare la prenotazione
     * @param linkedStudentNote nota dello studente per la prenotazione dell'appello collegato
     * @param linkedTagCode livello di uscita della lingua perl a prenotazione dell'appello collegato
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun putAcknowledgmentOfReceiptApplicationList(
        matId: Long,
        applicationListId: Long,
        acknowledgmentOfReceipt: Esse3AcknowledgmentOfReceipt,
        linkedOperation: Esse3LinkedBookingOperation? = null,
        linkedCourseOfStudyId: Long? = null,
        linkedActivityId: Long? = null,
        linkedCallId: Long? = null,
        linkedCallLogId: Long? = null,
        linkedStudentNote: String? = null,
        linkedTagCode: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3ExamSessionEnrollment {
        return executeJsonPut<Esse3ExamSessionEnrollment>("/prenotazioni/${matId}/${applicationListId}/presaVisione", setOf(Esse3PermissionLevel.STUDENT)) {
            setBody(FormDataContent(parameters {
                append("presaVisione", acknowledgmentOfReceipt.value)
                linkedOperation?.let { append("appRelOpType", it.value) }
                linkedCourseOfStudyId?.let { append("cdsRelAppId", it.toString()) }
                linkedActivityId?.let { append("adRelAppId", it.toString()) }
                linkedCallId?.let { append("appRelAppId", it.toString()) }
                linkedCallLogId?.let { append("appLogRelAppId", it.toString()) }
                linkedStudentNote?.let { append("notaStuRelApp", it) }
                linkedTagCode?.let { append("tagCodRelApp", it) }
            }))
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * informazioni sulle sessioni
     *
     * @param facultyId id della facoltà/dipartimento collegato al corso di studio a cui si riferisce la sessione
     * @param courseOfStudyId id corso di studio a cui si riferisce la sessione
     * @param facultyCode codice della facoltà/dipartimento collegato al corso di studio a cui si riferisce la sessione
     * @param courseOfStudyCode codice del corso di studio a cui si riferisce la sessione
     * @param minStartDate minima data di inizio della sessione
     * @param maxEndDate minima data di fine della sessione
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getSessions(
        facultyId: Long? = null,
        courseOfStudyId: Long? = null,
        facultyCode: String? = null,
        courseOfStudyCode: String? = null,
        minStartDate: String? = null,
        maxEndDate: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3Session> {
        return executeJsonGetList<Esse3Session>("/sessioni", setOf(Esse3PermissionLevel.ANY)) {
            facultyId?.let { parameter("facId", it) }
            courseOfStudyId?.let { parameter("cdsId", it) }
            facultyCode?.let { parameter("facCod", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            minStartDate?.let { parameter("minDataInizio", it) }
            maxEndDate?.let { parameter("maxDataFine", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * informazioni sulle sessioni di un anno
     *
     * @param academicYearSessionId id dell'anno si sessione
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getSessionsByAcademicYearSession(
        academicYearSessionId: Long,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3Session> {
        return executeJsonGetList<Esse3Session>("/sessioni/${academicYearSessionId}", setOf(Esse3PermissionLevel.ANY)) {
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * informazioni sulle sessioni di un anno e corso di studio
     *
     * @param academicYearSessionId id dell'anno si sessione
     * @param courseOfStudyId id del corso di studio per il quale recuperare la sessione
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getSessionsByAcademicYearSessionAndCourseOfStudy(
        academicYearSessionId: Long,
        courseOfStudyId: Long,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3Session> {
        return executeJsonGetList<Esse3Session>("/sessioni/${academicYearSessionId}/${courseOfStudyId}", setOf(Esse3PermissionLevel.ANY)) {
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * effettua l'export delle aule del sistema esterno
     *
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun exportSystemLog(
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3SystemLogExport> {
        return executeJsonGetList<Esse3SystemLogExport>("/sistLogExt/export", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * effettua l'export delle aule del sistema esterno dato l'elabId
     *
     * @param processingId id univoco
     */
    suspend fun exportSystemLogByProcessingId(
        processingId: Long
    ): Esse3SystemLogExport {
        return executeJsonGet<Esse3SystemLogExport>("/sistLogExt/export/${processingId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * effettua l'export delle aule del sistema esterno dato l'elabId
     *
     * @param processingId id univoco
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param excludeIdenticalPackages consente di escludere i pacchetti identici rispetto all'elaborazione precedente indicata in diff_elab_id. Se non impostato, il default è false
     */
    suspend fun exportSystemLogEvents(
        processingId: Long,
        start: Int? = null,
        limit: Int? = null,
        excludeIdenticalPackages: Boolean? = null
    ): List<Esse3SystemLogEventTestExport> {
        return executeJsonGetList<Esse3SystemLogEventTestExport>("/sistLogExt/export/${processingId}/eventi", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            excludeIdenticalPackages?.let { parameter("escludiPacchettiUguali", it) }
        }
    }

    /**
     * effettua l'export delle aule del sistema esterno dato l'elabId
     *
     * @param processingId id univoco
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     */
    suspend fun exportSystemLogSessions(
        processingId: Long,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3SystemLogSessionsExport> {
        return executeJsonGetList<Esse3SystemLogSessionsExport>("/sistLogExt/export/${processingId}/sessioni", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    /**
     * recupera gli impegni inviati dal sistema di logistica esterno
     *
     * @param referenceDate Data di riferimento con la quale effettuare il recupero
     */
    suspend fun getCommitmentsByDate(
        referenceDate: String? = null
    ): List<Esse3Esse3SystemLogCommitment> {
        return executeJsonGetList<Esse3Esse3SystemLogCommitment>("/sistLogExt/impegni", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            referenceDate?.let { parameter("dataRif", it) }
        }
    }

    /**
     * effettua l'import delle aule del sistema esterno
     *
     * @param body Oggetto con gli impegni da importare
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun importSystemLog(
        body: Esse3SystemLogImport,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3SystemLogImportResult {
        return executeJsonPut<Esse3SystemLogImportResult>("/sistLogExt/import/", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * effettua l'aggiornamento
     *
     * @param body Oggetto con gli impegni da importare
     */
    suspend fun updateCommitment(
        body: Esse3UpdateSystemLogCommitments
    ): Esse3SystemLogImportResult {
        return executeJsonPut<Esse3SystemLogImportResult>("/sistLogExt/update/", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}
