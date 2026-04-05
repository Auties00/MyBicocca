package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3AnnualEnrollment
import it.attendance100.mybicocca.data.dto.esse3.Esse3AttendanceDays
import it.attendance100.mybicocca.data.dto.esse3.Esse3CanteenBandParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3Career
import it.attendance100.mybicocca.data.dto.esse3.Esse3CareerClosureParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3CareerGDPR
import it.attendance100.mybicocca.data.dto.esse3.Esse3CareerMinimalData
import it.attendance100.mybicocca.data.dto.esse3.Esse3CareerMinimalDataGDPR
import it.attendance100.mybicocca.data.dto.esse3.Esse3CareerNotes
import it.attendance100.mybicocca.data.dto.esse3.Esse3CareerParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3EnrollmentClasses
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExemptionTypeParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3GraduationWaitingParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3PhDProgramCareer
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudentTypeParameters
import kotlinx.serialization.json.Json

class Esse3CareersApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/carriere-service-v1") {

    /**
     * Recupero delle carriere degli studenti
     *
     * @param userId id univoco che consente di individuare l'account utente
     * @param surname cognome dell'utente (se viene utilizzato il carattere * viene applicato il like)
     * @param name nome dell'utente (se viene utilizzato il carattere * viene applicato il like)
     * @param fiscalCode codice fiscale dell'utente
     * @param studentStatusCode codice dello stato della carriera
     * @param academicYearId anno di immatricolazione
     * @param govIdentifier identificativo di U-Gov che permette di ritirare le informazioni dei responsabili.
     * @param studentMatricola matricola dello studente
     * @param externalCareerCode codice esterno carriera
     * @param onlyEnrolled se 1 recupero dei soli immatricolati, se 0 recupero anche dei non immatricolati
     * @param fromModificationTime ora, minuti e secondi dell'ultima modifica (HH:MI:SS), verranno recuperati tutti i record inseriti con orario di ultima modifica successivo a questo orario in data daDataMod. Se i secondi verranno omessi saranno impostati automaticamente a 00.
     * @param courseOfStudyId identificativo corso di studi
     * @param courseOfStudyCode codice corso di studi
     * @param courseTypeCode Codice tipo corso di studio.
     * @param academicYearFromId anno di immatricolazione di partenza. Verranno recuperate tutte le carriere con aaId maggiore o uguale a quello specificato
     * @param fromModificationDate data di ultima modifica, verranno recuperati tutti i record inseriti con data di ultima modifica successiva a questa data
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param academicYearEnrollmentId anno di ultima iscrizione annuale
     * @param onlyActive indica se recuperare solo carriere attive, in ipotesi o sospese (no trasferiti in uscita)
     */
    suspend fun getCareers(
        userId: String? = null,
        surname: String? = null,
        name: String? = null,
        fiscalCode: String? = null,
        studentStatusCode: String? = null,
        academicYearId: String? = null,
        govIdentifier: String? = null,
        studentMatricola: String? = null,
        externalCareerCode: String? = null,
        onlyEnrolled: Int? = null,
        fromModificationTime: String? = null,
        courseOfStudyId: Long? = null,
        courseOfStudyCode: String? = null,
        courseTypeCode: String? = null,
        academicYearFromId: Long? = null,
        fromModificationDate: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null,
        academicYearEnrollmentId: Long? = null,
        onlyActive: Int? = null
    ): List<Esse3Career> {
        return executeJsonGetList<Esse3Career>("/carriere", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            userId?.let { parameter("userId", it) }
            surname?.let { parameter("cognome", it) }
            name?.let { parameter("nome", it) }
            fiscalCode?.let { parameter("codFis", it) }
            studentStatusCode?.let { parameter("staStuCod", it) }
            academicYearId?.let { parameter("aaId", it) }
            govIdentifier?.let { parameter("identificativo U-Gov", it) }
            studentMatricola?.let { parameter("matricolaStudente", it) }
            externalCareerCode?.let { parameter("Codice esterno carriera", it) }
            onlyEnrolled?.let { parameter("soloImmatricolati", it) }
            fromModificationTime?.let { parameter("daOraMod", it) }
            courseOfStudyId?.let { parameter("cdsId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            academicYearFromId?.let { parameter("aaIdDa", it) }
            fromModificationDate?.let { parameter("daDataMod", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            academicYearEnrollmentId?.let { parameter("aaIscrId", it) }
            onlyActive?.let { parameter("soloAttivi", it) }
        }
    }

    /**
     * Recupero dei dati minimi delle carriere degli studenti GDPR
     *
     * @param userId id univoco che consente di individuare l'account utente
     * @param surname cognome dell'utente (se viene utilizzato il carattere * viene applicato il like)
     * @param name nome dell'utente (se viene utilizzato il carattere * viene applicato il like)
     * @param fiscalCode codice fiscale dell'utente
     * @param studentStatusCode codice dello stato della carriera
     * @param academicYearId anno di immatricolazione
     * @param govIdentifier identificativo di U-Gov che permette di ritirare le informazioni dei responsabili.
     * @param studentMatricola matricola dello studente
     * @param externalCareerCode codice esterno carriera
     * @param onlyEnrolled se 1 recupero dei soli immatricolati, se 0 recupero anche dei non immatricolati
     * @param fromModificationTime ora, minuti e secondi dell'ultima modifica (HH:MI:SS), verranno recuperati tutti i record inseriti con orario di ultima modifica successivo a questo orario in data daDataMod. Se i secondi verranno omessi saranno impostati automaticamente a 00.
     * @param courseOfStudyId identificativo corso di studi
     * @param courseOfStudyCode codice corso di studi
     * @param courseTypeCode Codice tipo corso di studio.
     * @param academicYearFromId anno di immatricolazione di partenza. Verranno recuperate tutte le carriere con aaId maggiore o uguale a quello specificato
     * @param fromModificationDate data di ultima modifica, verranno recuperati tutti i record inseriti con data di ultima modifica successiva a questa data
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param academicYearEnrollmentId anno di ultima iscrizione annuale
     * @param onlyActive indica se recuperare solo carriere attive, in ipotesi o sospese (no trasferiti in uscita)
     */
    suspend fun getMinimalGdprCareersData(
        userId: String? = null,
        surname: String? = null,
        name: String? = null,
        fiscalCode: String? = null,
        studentStatusCode: String? = null,
        academicYearId: String? = null,
        govIdentifier: String? = null,
        studentMatricola: String? = null,
        externalCareerCode: String? = null,
        onlyEnrolled: Int? = null,
        fromModificationTime: String? = null,
        courseOfStudyId: Long? = null,
        courseOfStudyCode: String? = null,
        courseTypeCode: String? = null,
        academicYearFromId: Long? = null,
        fromModificationDate: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null,
        academicYearEnrollmentId: Long? = null,
        onlyActive: Int? = null
    ): List<Esse3CareerMinimalDataGDPR> {
        return executeJsonGetList<Esse3CareerMinimalDataGDPR>("/carriere-gdpr/datiMin", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            userId?.let { parameter("userId", it) }
            surname?.let { parameter("cognome", it) }
            name?.let { parameter("nome", it) }
            fiscalCode?.let { parameter("codFis", it) }
            studentStatusCode?.let { parameter("staStuCod", it) }
            academicYearId?.let { parameter("aaId", it) }
            govIdentifier?.let { parameter("identificativo U-Gov", it) }
            studentMatricola?.let { parameter("matricolaStudente", it) }
            externalCareerCode?.let { parameter("Codice esterno carriera", it) }
            onlyEnrolled?.let { parameter("soloImmatricolati", it) }
            fromModificationTime?.let { parameter("daOraMod", it) }
            courseOfStudyId?.let { parameter("cdsId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            academicYearFromId?.let { parameter("aaIdDa", it) }
            fromModificationDate?.let { parameter("daDataMod", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            academicYearEnrollmentId?.let { parameter("aaIscrId", it) }
            onlyActive?.let { parameter("soloAttivi", it) }
        }
    }

    /**
     * Recupero della carriera degli studenti
     *
     * @param studentId identificativo della carriera
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getGdprCareerByStudent(
        studentId: Long,
        optionalFields: String? = null
    ): List<Esse3CareerGDPR> {
        return executeJsonGetList<Esse3CareerGDPR>("/carriere-gdpr/${studentId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * permette di aggiornare i dati relativi all'attesa di laurea dello studente
     *
     * @param body Oggetto con i parametri relativi all'attesa di laurea Per identificare lo studente occorre passare come parametro lo stuId o la matricola. Se attlauFlg vale 0, il parametro dataAttlau deve essere null. Se attlauFlg vale 1, il parametro dataAttlau non deve essere null.
     */
    suspend fun putGraduationWaiting(
        body: Esse3GraduationWaitingParameters
    ): Esse3Career {
        return executeJsonPut<Esse3Career>("/carriere/aggiornaAttesaLaurea", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * Recupero delle carriere degli studenti
     *
     * @param userId id univoco che consente di individuare l'account utente
     * @param govIdentifier identificativo di U-Gov che permette di ritirare le informazioni dei responsabili.
     * @param studentId identificativo dello studente
     * @param studentMatricola matricola dello studente
     * @param externalCareerCode codice esterno carriera
     * @param surname cognome dell'utente (se viene utilizzato il carattere * viene applicato il like)
     * @param name nome dell'utente (se viene utilizzato il carattere * viene applicato il like)
     * @param fiscalCode codice fiscale dell'utente
     * @param studentStatusCode codice dello stato della carriera
     * @param academicYearId anno di immatricolazione
     * @param academicYearFromId anno di immatricolazione di partenza. Verranno recuperate tutte le carriere con aaId maggiore o uguale a quello specificato
     * @param fromModificationDate data di ultima modifica, verranno recuperati tutti i record inseriti con data di ultima modifica successiva a questa data
     * @param fromModificationTime ora, minuti e secondi dell'ultima modifica (HH:MI:SS), verranno recuperati tutti i record inseriti con orario di ultima modifica successivo a questo orario in data daDataMod. Se i secondi verranno omessi saranno impostati automaticamente a 00.
     * @param onlyEnrolled se 1 recupero dei soli immatricolati, se 0 recupero anche dei non immatricolati
     * @param courseOfStudyId identificativo corso di studi
     * @param courseOfStudyCode codice corso di studi
     * @param courseTypeCode Codice tipo corso di studio.
     * @param academicYearEnrollmentId anno di ultima iscrizione annuale
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param onlyActive indica se recuperare solo carriere attive, in ipotesi o sospese (no trasferiti in uscita)
     */
    suspend fun getPhdCareersData(
        userId: String? = null,
        govIdentifier: String? = null,
        studentId: Long? = null,
        studentMatricola: String? = null,
        externalCareerCode: String? = null,
        surname: String? = null,
        name: String? = null,
        fiscalCode: String? = null,
        studentStatusCode: String? = null,
        academicYearId: String? = null,
        academicYearFromId: Long? = null,
        fromModificationDate: String? = null,
        fromModificationTime: String? = null,
        onlyEnrolled: Int? = null,
        courseOfStudyId: Long? = null,
        courseOfStudyCode: String? = null,
        courseTypeCode: String? = null,
        academicYearEnrollmentId: Long? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null,
        onlyActive: Int? = null
    ): List<Esse3PhDProgramCareer> {
        return executeJsonGetList<Esse3PhDProgramCareer>("/carriere/datiDottorato", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            userId?.let { parameter("userId", it) }
            govIdentifier?.let { parameter("identificativo U-Gov", it) }
            studentId?.let { parameter("stuId", it) }
            studentMatricola?.let { parameter("matricolaStudente", it) }
            externalCareerCode?.let { parameter("Codice esterno carriera", it) }
            surname?.let { parameter("cognome", it) }
            name?.let { parameter("nome", it) }
            fiscalCode?.let { parameter("codFis", it) }
            studentStatusCode?.let { parameter("staStuCod", it) }
            academicYearId?.let { parameter("aaId", it) }
            academicYearFromId?.let { parameter("aaIdDa", it) }
            fromModificationDate?.let { parameter("daDataMod", it) }
            fromModificationTime?.let { parameter("daOraMod", it) }
            onlyEnrolled?.let { parameter("soloImmatricolati", it) }
            courseOfStudyId?.let { parameter("cdsId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            academicYearEnrollmentId?.let { parameter("aaIscrId", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            onlyActive?.let { parameter("soloAttivi", it) }
        }
    }

    /**
     * Recupero dei dati minimi delle carriere degli studenti
     *
     * @param userId id univoco che consente di individuare l'account utente
     * @param surname cognome dell'utente (se viene utilizzato il carattere * viene applicato il like)
     * @param name nome dell'utente (se viene utilizzato il carattere * viene applicato il like)
     * @param fiscalCode codice fiscale dell'utente
     * @param studentStatusCode codice dello stato della carriera
     * @param academicYearId anno di immatricolazione
     * @param govIdentifier identificativo di U-Gov che permette di ritirare le informazioni dei responsabili.
     * @param studentMatricola matricola dello studente
     * @param externalCareerCode codice esterno carriera
     * @param onlyEnrolled se 1 recupero dei soli immatricolati, se 0 recupero anche dei non immatricolati
     * @param fromModificationTime ora, minuti e secondi dell'ultima modifica (HH:MI:SS), verranno recuperati tutti i record inseriti con orario di ultima modifica successivo a questo orario in data daDataMod. Se i secondi verranno omessi saranno impostati automaticamente a 00.
     * @param courseOfStudyId identificativo corso di studi
     * @param courseOfStudyCode codice corso di studi
     * @param courseTypeCode Codice tipo corso di studio.
     * @param academicYearFromId anno di immatricolazione di partenza. Verranno recuperate tutte le carriere con aaId maggiore o uguale a quello specificato
     * @param fromModificationDate data di ultima modifica, verranno recuperati tutti i record inseriti con data di ultima modifica successiva a questa data
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param academicYearEnrollmentId anno di ultima iscrizione annuale
     * @param onlyActive indica se recuperare solo carriere attive, in ipotesi o sospese (no trasferiti in uscita)
     */
    suspend fun getMinimalCareersData(
        userId: String? = null,
        surname: String? = null,
        name: String? = null,
        fiscalCode: String? = null,
        studentStatusCode: String? = null,
        academicYearId: String? = null,
        govIdentifier: String? = null,
        studentMatricola: String? = null,
        externalCareerCode: String? = null,
        onlyEnrolled: Int? = null,
        fromModificationTime: String? = null,
        courseOfStudyId: Long? = null,
        courseOfStudyCode: String? = null,
        courseTypeCode: String? = null,
        academicYearFromId: Long? = null,
        fromModificationDate: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null,
        academicYearEnrollmentId: Long? = null,
        onlyActive: Int? = null
    ): List<Esse3CareerMinimalData> {
        return executeJsonGetList<Esse3CareerMinimalData>("/carriere/datiMin", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            userId?.let { parameter("userId", it) }
            surname?.let { parameter("cognome", it) }
            name?.let { parameter("nome", it) }
            fiscalCode?.let { parameter("codFis", it) }
            studentStatusCode?.let { parameter("staStuCod", it) }
            academicYearId?.let { parameter("aaId", it) }
            govIdentifier?.let { parameter("identificativo U-Gov", it) }
            studentMatricola?.let { parameter("matricolaStudente", it) }
            externalCareerCode?.let { parameter("Codice esterno carriera", it) }
            onlyEnrolled?.let { parameter("soloImmatricolati", it) }
            fromModificationTime?.let { parameter("daOraMod", it) }
            courseOfStudyId?.let { parameter("cdsId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            academicYearFromId?.let { parameter("aaIdDa", it) }
            fromModificationDate?.let { parameter("daDataMod", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            academicYearEnrollmentId?.let { parameter("aaIscrId", it) }
            onlyActive?.let { parameter("soloAttivi", it) }
        }
    }

    /**
     * verifica che le eventuali iscrizioni successive all'anno passato in ingresso siano annullabili.
     *
     * @param studentId identificativo dello studente
     * @param academicYearId Anno Accademico di ultima iscrizione da mantenere valida.
     * @param skipGraduationCheck Indicatore di forzatura controllo esami superati
     * @param deleteCharges Indicatore di forzatura eliminazione addebiti annullabili
     * @param cancelPlan Indicatore di forzatura annullamento piano
     * @param paymentCheck Indicatore di verifica presenza pagamenti
     * @param reverseCharges Indicatore di stornabilità degli addebiti.
     */
    suspend fun verifyEnrollmentCancellability(
        studentId: Long? = null,
        academicYearId: Long? = null,
        skipGraduationCheck: Long? = null,
        deleteCharges: Long? = null,
        cancelPlan: Long? = null,
        paymentCheck: Long? = null,
        reverseCharges: Long? = null
    ): String {
        return executeJsonGet<String>("/carriere/verifica-annullabilita-iscrizioni", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            studentId?.let { parameter("stuId", it) }
            academicYearId?.let { parameter("aaId", it) }
            skipGraduationCheck?.let { parameter("skipCheckEsa", it) }
            deleteCharges?.let { parameter("elimAddebiti", it) }
            cancelPlan?.let { parameter("annullaPiano", it) }
            paymentCheck?.let { parameter("checkPag", it) }
            reverseCharges?.let { parameter("stornaAddebiti", it) }
        }
    }

    /**
     * Verifica se uno studente ha un'iscrizione attiva in Ateneo.
     *
     * @param fiscalCode codice fiscale dell'utente
     * @param studentMatricola matricola dello studente
     * @param daysAfterGraduation massimo numero di giorni dalla laurea.
     */
    suspend fun verifyEnrollment(
        fiscalCode: String? = null,
        studentMatricola: String? = null,
        daysAfterGraduation: Int? = null
    ): Int {
        return executeJsonGet<Int>("/carriere/verifica-iscrizione", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            fiscalCode?.let { parameter("codFis", it) }
            studentMatricola?.let { parameter("matricolaStudente", it) }
            daysAfterGraduation?.let { parameter("ggDopoLaurea", it) }
        }
    }

    /**
     * aggiorna la data di iscrizione e il tipo esonero, effettuando il recupero per matricola
     *
     * @param matricola matricola dello studente
     * @param body Oggetto con i parametri relativi alla data di iscrizione e alla tipologia esonero
     * @param academicYear anno accademico dell'iscrizione che si vuole recuperare
     */
    suspend fun putEnrollmentDateAndExemptionTypeByMatricola(
        matricola: String,
        body: Esse3ExemptionTypeParameters,
        academicYear: Int
    ): Esse3AnnualEnrollment {
        return executeJsonPut<Esse3AnnualEnrollment>("/carriere/${matricola}/iscrizioni/aggiornaDataIscrAndTipoEsoneroByMat", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            parameter("annoAccademico", academicYear)
        }
    }

    /**
     * Recupero della carriera degli studenti
     *
     * @param studentId identificativo della carriera
     */
    suspend fun getCareerByStudent(
        studentId: Long
    ): Esse3Career {
        return executeJsonGet<Esse3Career>("/carriere/${studentId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * effettua l'aggiornamento del numero di protocollo dello studente
     *
     * @param studentId identificativo della carriera
     * @param body Oggetto con i campi da modificare
     */
    suspend fun putCareerByStudent(
        studentId: Long,
        body: Esse3CareerParameters
    ): Esse3Career {
        return executeJsonPut<Esse3Career>("/carriere/${studentId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * effettua la chiusura della carriera per un dato studente
     *
     * @param studentId identificativo della carriera
     * @param body Oggetto con i parametri per la chiusura della carriera
     */
    suspend fun careerClosure(
        studentId: Long,
        body: Esse3CareerClosureParameters
    ): Esse3Career {
        return executeJsonPut<Esse3Career>("/carriere/${studentId}/chiudiCarriera", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * Recupero del numero di giorni di frequenza da recuperare.
     *
     * @param studentId identificativo della carriera
     */
    suspend fun getAttendanceDays(
        studentId: Long
    ): Esse3AttendanceDays {
        return executeJsonGet<Esse3AttendanceDays>("/carriere/${studentId}/giorni-freq", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.UNKNOWN, Esse3PermissionLevel.ADMIN_OFFICE))
    }

    /**
     * Recupero delle iscrizione di uno studente
     *
     * @param studentId identificativo della carriera
     * @param academicYear anno accademico dell'iscrizione che si vuole recuperare
     * @param lastEnrollmentFlag 1 se si vuole recuperare SOLO l'ultima iscrizione, 0 altrimenti
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getAnnualEnrollment(
        studentId: Long,
        academicYear: Long? = null,
        lastEnrollmentFlag: Int? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null
    ): List<Esse3AnnualEnrollment> {
        return executeJsonGetList<Esse3AnnualEnrollment>("/carriere/${studentId}/iscrizioni", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            academicYear?.let { parameter("annoAccademico", it) }
            lastEnrollmentFlag?.let { parameter("ultimaIscrizioneFlg", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    /**
     * aggiorna la data di iscrizione e il tipo esonero
     *
     * @param studentId identificativo della carriera
     * @param body Oggetto con i parametri relativi alla data di iscrizione e alla tipologia esonero
     * @param academicYear anno accademico dell'iscrizione che si vuole recuperare
     */
    suspend fun putEnrollmentDateAndExemptionTypeByStudentId(
        studentId: Long,
        body: Esse3ExemptionTypeParameters,
        academicYear: Int
    ): Esse3AnnualEnrollment {
        return executeJsonPut<Esse3AnnualEnrollment>("/carriere/${studentId}/iscrizioni/aggiornaDataIscrAndTipoEsonero", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            parameter("annoAccademico", academicYear)
        }
    }

    /**
     * aggiorna fascia mensa di uno studente
     *
     * @param studentId identificativo della carriera
     * @param body Oggetto con i parametri relativi alla fascia mensa
     * @param academicYear anno accademico dell'iscrizione che si vuole recuperare
     */
    suspend fun putCanteenBand(
        studentId: Long,
        body: Esse3CanteenBandParameters,
        academicYear: Int
    ): Esse3AnnualEnrollment {
        return executeJsonPut<Esse3AnnualEnrollment>("/carriere/${studentId}/iscrizioni/aggiornaFasciaMensa", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            parameter("annoAccademico", academicYear)
        }
    }

    /**
     * aggiorna codice tipologia studente
     *
     * @param studentId identificativo della carriera
     * @param body Oggetto con i parametri relativi alla tipologia studente
     * @param academicYear anno accademico dell'iscrizione che si vuole recuperare
     */
    suspend fun putStudentTypeCode(
        studentId: Long,
        body: Esse3StudentTypeParameters,
        academicYear: Int
    ): Esse3AnnualEnrollment {
        return executeJsonPut<Esse3AnnualEnrollment>("/carriere/${studentId}/iscrizioni/aggiornaTipoStuCod", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            parameter("annoAccademico", academicYear)
        }
    }

    /**
     * Recupero dei dati relativi alla classe di iscrizione filtrati per studente e anno di iscrizione.
     *
     * @param academicYearEnrollmentId id anno accademico delle iscrizioni che si vogliono recuperare
     * @param studentId identificativo della carriera
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getEnrollmentClasses(
        academicYearEnrollmentId: Long,
        studentId: Long,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null
    ): List<Esse3EnrollmentClasses> {
        return executeJsonGetList<Esse3EnrollmentClasses>("/carriere/${studentId}/iscrizioni/${academicYearEnrollmentId}/classi", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.PROVISIONAL_ENROLLED_STUDENT, Esse3PermissionLevel.STUDENT)) {
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    /**
     * Recupero delle  note legate alla carriera degli studenti
     *
     * @param studentId identificativo della carriera
     * @param verificationDate data di inserimento
     * @param blockingNote indica dse recuperare  solo note con  tipo controllo bloccante.
     * @param academicYearNote indica dse recuperare  solo note con   tipo controllo  nella mbito Area amministrativa.
     * @param csNote indica dse recuperare  solo note con   tipo controllo  nell ambito Area carriera
     * @param certificateNote indica dse recuperare  solo note con   tipo controllo  nell ambito Area certificati.
     * @param taxNote indica dse recuperare  solo note con   tipo controllo  nell ambito Area Tasse.
     * @param webNote indica dse recuperare  solo note con   tipo controllo  nell ambito Area WEB.
     * @param contractNoteTypeCode Codice tipo blocco note studente.
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getCareerNotesByStudent(
        studentId: Long,
        verificationDate: String? = null,
        blockingNote: Long? = null,
        academicYearNote: Long? = null,
        csNote: Long? = null,
        certificateNote: Long? = null,
        taxNote: Long? = null,
        webNote: Long? = null,
        contractNoteTypeCode: String? = null,
        order: String? = null,
        fields: String? = null
    ): List<Esse3CareerNotes> {
        return executeJsonGetList<Esse3CareerNotes>("/carriere/${studentId}/noteCarriera", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            verificationDate?.let { parameter("dataVerifica", it) }
            blockingNote?.let { parameter("notaBloccante", it) }
            academicYearNote?.let { parameter("notaAa", it) }
            csNote?.let { parameter("notaCs", it) }
            certificateNote?.let { parameter("notaCert", it) }
            taxNote?.let { parameter("notaTax", it) }
            webNote?.let { parameter("notaWeb", it) }
            contractNoteTypeCode?.let { parameter("tipoContrNotaCod", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    /**
     * Recupera lo stato delle immatricolazioni per un dato Dipartimento o gruppo di corsi di studio
     *
     * @param languageCode Codice ISO Lingua
     * @param tcGroupCode Codice Tipo Raggruppamento Tipi Corso di Studio
     * @param courseTypeCode Codice tipo corso di studio.
     * @param facultyCode Codice Facoltà / Dipartimento
     * @param facultyId Identificativo Facoltà / Dipartimento
     * @param courseOfStudyCode codice corso di studi
     * @param courseOfStudyId identificativo corso di studi
     */
    suspend fun getEnrollmentInfo(
        languageCode: String? = null,
        tcGroupCode: String? = null,
        courseTypeCode: String? = null,
        facultyCode: String? = null,
        facultyId: Long? = null,
        courseOfStudyCode: String? = null,
        courseOfStudyId: Long? = null
    ): String {
        return executeJsonGet<String>("/immatricolazioni/informazioni", setOf(Esse3PermissionLevel.ADMIN_OFFICE, Esse3PermissionLevel.TECHNICAL_USER)) {
            languageCode?.let { parameter("codLingua", it) }
            tcGroupCode?.let { parameter("gruppoTcCod", it) }
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            facultyCode?.let { parameter("facCod", it) }
            facultyId?.let { parameter("facId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseOfStudyId?.let { parameter("cdsId", it) }
        }
    }

    /**
     * Recupera lo stato delle immatricolazioni per un dato Dipartimento o gruppo di corsi di studio nel dettaglio
     *
     * @param languageCode Codice ISO Lingua
     * @param tcGroupCode Codice Tipo Raggruppamento Tipi Corso di Studio
     * @param courseTypeCode Codice tipo corso di studio.
     * @param facultyCode Codice Facoltà / Dipartimento
     * @param facultyId Identificativo Facoltà / Dipartimento
     * @param courseOfStudyCode codice corso di studi
     * @param courseOfStudyId identificativo corso di studi
     */
    suspend fun getAllEnrollmentInfo(
        languageCode: String? = null,
        tcGroupCode: String? = null,
        courseTypeCode: String? = null,
        facultyCode: String? = null,
        facultyId: Long? = null,
        courseOfStudyCode: String? = null,
        courseOfStudyId: Long? = null
    ): String {
        return executeJsonGet<String>("/immatricolazioni/informazioni/dettaglio", setOf(Esse3PermissionLevel.ADMIN_OFFICE, Esse3PermissionLevel.TECHNICAL_USER)) {
            languageCode?.let { parameter("codLingua", it) }
            tcGroupCode?.let { parameter("gruppoTcCod", it) }
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            facultyCode?.let { parameter("facCod", it) }
            facultyId?.let { parameter("facId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseOfStudyId?.let { parameter("cdsId", it) }
        }
    }

    /**
     * Recupero delle carriere degli studenti per anno accademico.
     *
     * @param academicYearEnrollmentId id anno accademico delle iscrizioni che si vogliono recuperare
     * @param includeSXH includi iscrizioni sospese per ipotesi.
     * @param includeCondition includi iscrizioni condizionate.
     * @param courseOfStudyId identificativo corso di studi
     * @param courseOfStudyCode codice corso di studi
     * @param courseTypeCode Codice tipo corso di studio.
     * @param enrollmentTypeCode Codice tipo iscrizione.
     * @param courseYear Anno di corso.
     * @param enrollmentStatusCode codice stato iscrizione annuale.
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getEnrollments(
        academicYearEnrollmentId: String,
        includeSXH: Long,
        includeCondition: Long,
        courseOfStudyId: Long? = null,
        courseOfStudyCode: String? = null,
        courseTypeCode: String? = null,
        enrollmentTypeCode: String? = null,
        courseYear: Long? = null,
        enrollmentStatusCode: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null
    ): List<Esse3AnnualEnrollment> {
        return executeJsonGetList<Esse3AnnualEnrollment>("/iscrizioni/${academicYearEnrollmentId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("inclSXH", includeSXH)
            parameter("inclCond", includeCondition)
            courseOfStudyId?.let { parameter("cdsId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            enrollmentTypeCode?.let { parameter("tipoIscrCod", it) }
            courseYear?.let { parameter("annoCorso", it) }
            enrollmentStatusCode?.let { parameter("staIscCod", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }
}
