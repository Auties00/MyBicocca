package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3NewTeachers
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3PostTeacherSchedule
import it.attendance100.mybicocca.data.dto.esse3.Esse3PutTeacherNotes
import it.attendance100.mybicocca.data.dto.esse3.Esse3TeacherParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3TeacherRole
import it.attendance100.mybicocca.data.dto.esse3.Esse3TeachersNotes
import it.attendance100.mybicocca.data.dto.esse3.Esse3TeachersTimetable
import kotlinx.serialization.json.Json

class Esse3TeachersApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/docenti-service-v1") {

    /**
     * @param lecturerMatricola matricola del docente
     * @param lecturerSurname cognome del docente (se viene utilizzato il carattere * viene applicato il like)
     * @param lecturerName nome del docente (se viene utilizzato il carattere * viene applicato il like)
     * @param fiscalCode codice fiscale dell'utente
     * @param abbreviatedId ID address book della  persona in ugov
     * @param modificationDate data di ultima modifica
     * @param insertionDate data di inserimento
     * @param positionId ID carica
     * @param courseOfStudyPositionId ID Corso di Studio assiciata alla carica
     * @param facultyPositionId ID Facoltà/Dipartimento assiciata alla carica
     * @param positionValidityDate Filtro di una carica valida in una determinata data (formato DD/MM/YYYY)
     * @param facultyBelongingId ID Facoltà/Dipartimento appartenenza
     * @param lecturerRolesCode Codice ruoli Docenti
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getLecturers(
        lecturerMatricola: String? = null,
        lecturerSurname: String? = null,
        lecturerName: String? = null,
        fiscalCode: String? = null,
        abbreviatedId: Long? = null,
        modificationDate: String? = null,
        insertionDate: String? = null,
        positionId: Long? = null,
        courseOfStudyPositionId: Long? = null,
        facultyPositionId: Long? = null,
        positionValidityDate: String? = null,
        facultyBelongingId: Long? = null,
        lecturerRolesCode: List<String>? = null,
        start: Int? = null,
        limit: Int? = null,
        fields: String? = null,
        optionalFields: String? = null,
        order: String? = null
    ): List<Esse3NewTeachers> {
        return executeJsonGetList<Esse3NewTeachers>("/docenti", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            lecturerMatricola?.let { parameter("docenteMatricola", it) }
            lecturerSurname?.let { parameter("docenteCognome", it) }
            lecturerName?.let { parameter("docenteNome", it) }
            fiscalCode?.let { parameter("codFis", it) }
            abbreviatedId?.let { parameter("idAb", it) }
            modificationDate?.let { parameter("dataMod", it) }
            insertionDate?.let { parameter("dataIns", it) }
            positionId?.let { parameter("caricaId", it) }
            courseOfStudyPositionId?.let { parameter("cdsIdCarica", it) }
            facultyPositionId?.let { parameter("facIdCarica", it) }
            positionValidityDate?.let { parameter("dataValiditaCarica", it) }
            facultyBelongingId?.let { parameter("facIdAppartenenza", it) }
            lecturerRolesCode?.let { parameter("ruoliDocCod", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * Recupero ruoli dei docenti
     *
     * @param lecturerRoleCode Codice ruolo docente.
     * @param csaCode Codice CSA.
     * @param lecturerRoleTypeCode Codice tipologia ruolo docente.
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     */
    suspend fun getLecturerRoles(
        lecturerRoleCode: String? = null,
        csaCode: String? = null,
        lecturerRoleTypeCode: String? = null,
        fields: String? = null,
        order: String? = null,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3TeacherRole> {
        return executeJsonGetList<Esse3TeacherRole>("/docenti/ruoli", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            lecturerRoleCode?.let { parameter("ruoloDocCod", it) }
            csaCode?.let { parameter("csaCod", it) }
            lecturerRoleTypeCode?.let { parameter("tipoRuoloDocCod", it) }
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    /**
     * @param lecturerId id del docente
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getLecturer(
        lecturerId: Long,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3NewTeachers> {
        return executeJsonGetList<Esse3NewTeachers>("/docenti/${lecturerId}", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * effettua l'aggiornamento dell'email di un docente
     *
     * @param lecturerId id del docente
     * @param body Oggetto con i campi da modificare
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun putLecturer(
        lecturerId: Long,
        body: Esse3TeacherParameters,
        optionalFields: String? = null
    ): List<Esse3NewTeachers> {
        return executeJsonPatchList<Esse3NewTeachers>("/docenti/${lecturerId}", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * Recupera le note associate ad un docente
     *
     * @param lecturerId id del docente
     */
    suspend fun getLecturerNotes(
        lecturerId: Long
    ): Esse3TeachersNotes {
        return executeJsonGet<Esse3TeachersNotes>("/docenti/${lecturerId}/note", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER))
    }

    /**
     * Inserisce o aggiorna le note associate ad un docente
     *
     * @param lecturerId id del docente
     * @param body Oggetto con i campi da modificare
     */
    suspend fun putLecturerNotes(
        lecturerId: Long,
        body: Esse3PutTeacherNotes
    ): Esse3TeachersNotes {
        return executeJsonPut<Esse3TeachersNotes>("/docenti/${lecturerId}/note", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * Recupera gli orari associati ad un docente
     *
     * @param lecturerId id del docente
     * @param day giorno della settimana (1=Lunedì, 2=Martedì, 3=Mercoledì, 4=Giovedì, 5=Venerdì, 6=Sabato, 7=Domenica)
     */
    suspend fun getLecturerSchedule(
        lecturerId: Long,
        day: Int? = null
    ): List<Esse3TeachersTimetable> {
        return executeJsonGetList<Esse3TeachersTimetable>("/docenti/${lecturerId}/orario", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)) {
            day?.let { parameter("giorno", it) }
        }
    }

    /**
     * Inserisce gli orari associati ad un docente
     *
     * @param lecturerId id del docente
     * @param body Oggetto con i campi da inserire
     */
    suspend fun postLecturerSchedule(
        lecturerId: Long,
        body: List<Esse3PostTeacherSchedule>
    ): List<Esse3TeachersTimetable> {
        return executeJsonPostList<Esse3TeachersTimetable>("/docenti/${lecturerId}/orario", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * Aggiorna gli orari associati ad un docente
     *
     * @param lecturerId id del docente
     * @param body Oggetto con i campi da inserire
     * @param day giorno della settimana (1=Lunedì, 2=Martedì, 3=Mercoledì, 4=Giovedì, 5=Venerdì, 6=Sabato, 7=Domenica)
     * @param lecturerResearchScheduleId id orario ricevimento docente
     * @param startTime orario inizio ricevimento
     * @param endTime orario fine ricevimento
     */
    suspend fun putLecturerSchedule(
        lecturerId: Long,
        body: Esse3PostTeacherSchedule,
        day: Int? = null,
        lecturerResearchScheduleId: Long? = null,
        startTime: String? = null,
        endTime: String? = null
    ): List<Esse3TeachersTimetable> {
        return executeJsonPutList<Esse3TeachersTimetable>("/docenti/${lecturerId}/orario", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            day?.let { parameter("giorno", it) }
            lecturerResearchScheduleId?.let { parameter("docenteOrarioRicId", it) }
            startTime?.let { parameter("oraInizio", it) }
            endTime?.let { parameter("oraFine", it) }
        }
    }

    /**
     * Elimina gli orari associati ad un docente
     *
     * @param lecturerId id del docente
     * @param day giorno della settimana (1=Lunedì, 2=Martedì, 3=Mercoledì, 4=Giovedì, 5=Venerdì, 6=Sabato, 7=Domenica)
     * @param lecturerResearchScheduleId id orario ricevimento docente
     * @param startTime orario inizio ricevimento
     * @param endTime orario fine ricevimento
     */
    suspend fun deleteLecturerSchedule(
        lecturerId: Long,
        day: Int? = null,
        lecturerResearchScheduleId: Long? = null,
        startTime: String? = null,
        endTime: String? = null
    ) {
        val response = executeDelete("/docenti/${lecturerId}/orario") {
            day?.let { parameter("giorno", it) }
            lecturerResearchScheduleId?.let { parameter("docenteOrarioRicId", it) }
            startTime?.let { parameter("oraInizio", it) }
            endTime?.let { parameter("oraFine", it) }
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER))
    }
}
