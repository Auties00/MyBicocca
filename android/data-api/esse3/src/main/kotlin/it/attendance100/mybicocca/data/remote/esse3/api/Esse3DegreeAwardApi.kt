package it.attendance100.mybicocca.data.remote.esse3.api

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3AntiplagiarismDataInsert
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExamSession
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExamSessionLocation
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExternalSubject
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ImportSupervisorsResponse
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PermissionLevel
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3SupervisorTeacher
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3SupervisorTypeRegulation
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3SupervisorsInsert
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TeachingDomainCancellation
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TeachingDomainInsert
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TeachingDomainSummary
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ThesisAttachmentInsertMetadata
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ThesisConsultationMode
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ThesisDiscussionModeCodeInsert
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ThesisIntoTeachingDomainInsert
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ThesisSummary
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ThesisSupervisorsDepartments
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ThesisTeachingDomainSummary
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ThesisTypes
import kotlinx.serialization.json.Json

class Esse3DegreeAwardApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/consTit-service-v1") {

    /**
     * inserimento metadati allegato
     *
     * @param body Oggetto che contiene i metadati dell'allegato da inserire
     */
    suspend fun postThesisAttachmentMetadata(
        body: Esse3ThesisAttachmentInsertMetadata
    ) {
        val response = executePost("/allegati/tesi/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * Inserimento dati antiplagio per gli allegati di tesi.
     *
     * @param attachmentId identificativo allegato
     * @param antiplagiarismIndex riferimento antiplagio
     * @param antiplagiarismLink link antiplagio
     * @param antiplagiarismNote nota antiplagio
     * @param attachmentApprovalFlag Flag che indica se approvare l'allegato definitivo e quindi anche la tesi
     */
    suspend fun putAntiplagiarismData(
        attachmentId: Long? = null,
        antiplagiarismIndex: String? = null,
        antiplagiarismLink: String? = null,
        antiplagiarismNote: String? = null,
        attachmentApprovalFlag: Long? = null
    ): Esse3AntiplagiarismDataInsert {
        return executeJsonPut<Esse3AntiplagiarismDataInsert>("/allegati/tesi/antiplagio", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            attachmentId?.let { parameter("allegatoId", it) }
            antiplagiarismIndex?.let { parameter("indxAntiplagio", it) }
            antiplagiarismLink?.let { parameter("linkAntiplagio", it) }
            antiplagiarismNote?.let { parameter("notaAntiplagio", it) }
            attachmentApprovalFlag?.let { parameter("approvazioneAllegFlg", it) }
        }
    }

    /**
     * recupera la lista degli appelli
     *
     * @param courseTypeCode codice dela Tipologia del Corso di studio
     * @param courseOfStudyCode codice del Corso di studio
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getExamCalls(
        courseTypeCode: String? = null,
        courseOfStudyCode: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3ExamSession> {
        return executeJsonGetList<Esse3ExamSession>("/appelliCt", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * recupera le informazioni sull'appello di laurea
     *
     * @param callCommitteeId id dell'appello di laurea
     */
    suspend fun getCommitteeCall(
        callCommitteeId: Long
    ): List<Esse3ExamSession> {
        return executeJsonGetList<Esse3ExamSession>("/appelliCt/${callCommitteeId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * effettua l'inserimento della domanda di laurea in base ai dati inseriti nelle varie azioni del processo
     *
     * @param body Oggetto che contiene la riga da inserire
     */
    suspend fun postCommitteeApplication(
        body: Esse3TeachingDomainInsert
    ) {
        val response = executePost("/domandeCt") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * effettua l'annullamento della domanda di laurea in base ai dati inseriti nelle varie azioni del processo
     *
     * @param domicileTitleDeliveryId id della domanda di conseguimento titolo
     * @param studentId id dello studente
     * @param deadlinesCheck indica se controllare le scadenze. True per effettuare il controllo altrimenti false
     */
    suspend fun putCancelCommitteeApplication(
        domicileTitleDeliveryId: Long? = null,
        studentId: Long? = null,
        deadlinesCheck: Boolean? = null
    ): Esse3TeachingDomainCancellation {
        return executeJsonPut<Esse3TeachingDomainCancellation>("/domandeCt/annullaDomandaCt", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            domicileTitleDeliveryId?.let { parameter("domConsTitId", it) }
            studentId?.let { parameter("studId", it) }
            deadlinesCheck?.let { parameter("checkScadenze", it) }
        }
    }

    /**
     * recupera i dati delle tesi di laurea legate alle domande legate ad un particolare appello
     *
     * @param callCommitteeId id dell'appello di laurea
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     */
    suspend fun getThesesByCommitteeCallId(
        callCommitteeId: Long,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3ThesisTeachingDomainSummary> {
        return executeJsonGetList<Esse3ThesisTeachingDomainSummary>("/domandeCt/appCtId/${callCommitteeId}/tesi", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    /**
     * recupera i dati della domanda conseguimento titolo
     *
     * @param matId id della matricola
     */
    suspend fun getCommitteeApplicationByMatId(
        matId: Long
    ): List<Esse3TeachingDomainSummary> {
        return executeJsonGetList<Esse3TeachingDomainSummary>("/domandeCt/matId/${matId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * recupera i dati della domanda conseguimento titolo
     *
     * @param studentId id dello studente
     */
    suspend fun getCommitteeApplicationByStudentId(
        studentId: Long
    ): List<Esse3TeachingDomainSummary> {
        return executeJsonGetList<Esse3TeachingDomainSummary>("/domandeCt/stuId/${studentId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * effettua l'inserimento di una nuova tesi
     *
     * @param body Oggetto che contiene la riga da inserire
     */
    suspend fun postThesisIntoCommitteeApplication(
        body: Esse3ThesisIntoTeachingDomainInsert
    ) {
        val response = executePost("/domandeCt/tesi") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * recupera i dati della domanda conseguimento titolo
     *
     * @param domicileCommitteeId id della domanda di conseguimento titolo
     */
    suspend fun getCommitteeApplication(
        domicileCommitteeId: Long
    ): Esse3TeachingDomainSummary {
        return executeJsonGet<Esse3TeachingDomainSummary>("/domandeCt/${domicileCommitteeId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * recupera i dati della tesi di laurea leagata alla domanda conseguimento titolo dello studente
     *
     * @param domicileCommitteeId id della domanda di conseguimento titolo
     */
    suspend fun getTheses(
        domicileCommitteeId: Long
    ): Esse3ThesisTeachingDomainSummary {
        return executeJsonGet<Esse3ThesisTeachingDomainSummary>("/domandeCt/${domicileCommitteeId}/tesi", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * recupera la lista delle modalità di consultazione tesi
     *
     * @param authorizationFlag flag che indica se la modalità di consultazione tesi è abilitata oppure no
     */
    suspend fun getThesisDiscussionMode(
        authorizationFlag: Long? = null
    ): List<Esse3ThesisConsultationMode> {
        return executeJsonGetList<Esse3ThesisConsultationMode>("/modConsTesi", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            authorizationFlag?.let { parameter("abilFlg", it) }
        }
    }

    /**
     * recupera elenco dei docenti relatori di tesi disponibili per lo studente
     *
     * @param surname cognome del relatore, la ricerca avviene in like nel formato COGNOME%
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getThesisRelatedDocuments(
        surname: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3SupervisorTeacher> {
        return executeJsonGetList<Esse3SupervisorTeacher>("/relatori/docenti", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            surname?.let { parameter("cognome", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * recupera elenco dei soggetti esterni disponibili per lo studente
     *
     * @param surname cognome del relatore, la ricerca avviene in like nel formato COGNOME%
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getExternalSubject(
        surname: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3ExternalSubject> {
        return executeJsonGetList<Esse3ExternalSubject>("/relatori/soggEst/", setOf(Esse3PermissionLevel.STUDENT)) {
            surname?.let { parameter("cognome", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * recuperare le statistiche sui relatori di tesi
     *
     * @param departmentCode codice dipartimento
     * @param startDate data inizio, formato data DD/MM/YYYY
     * @param endDate data fine, formato data DD/MM/YYYY
     * @param lecturerId identificativo del docente
     * @param externalSubjectId identificativo del docente
     * @param relationTypeCode codice tipo relatore
     */
    suspend fun getThesisSupervisorsReport(
        departmentCode: String,
        startDate: String,
        endDate: String,
        lecturerId: Long? = null,
        externalSubjectId: Long? = null,
        relationTypeCode: String? = null
    ): Esse3ThesisSupervisorsDepartments {
        return executeJsonGet<Esse3ThesisSupervisorsDepartments>("/report/tesi/relatori/${departmentCode}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("dataIni", startDate)
            parameter("dataFine", endDate)
            lecturerId?.let { parameter("docenteId", it) }
            externalSubjectId?.let { parameter("soggEstId", it) }
            relationTypeCode?.let { parameter("tipoRelCod", it) }
        }
    }

    /**
     * recupera le informazioni sulle sedute e le rispettive commisioni di Laurea
     *
     * @param academicYearId identificativo dell'anno di conseguimento titolo
     * @param department codice dipartimento
     */
    suspend fun getCommitteeCallSession(
        academicYearId: Long? = null,
        department: String? = null
    ): List<Esse3ExamSessionLocation> {
        return executeJsonGetList<Esse3ExamSessionLocation>("/sedAppCt", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            academicYearId?.let { parameter("aaId", it) }
            department?.let { parameter("dipartimento", it) }
        }
    }

    /**
     * recupera i dati della tesi di laurea specificata dal tesId
     *
     * @param thesisId id della tesi
     */
    suspend fun getThesisByThesisId(
        thesisId: Long
    ): Esse3ThesisSummary {
        return executeJsonGet<Esse3ThesisSummary>("/tesi/${thesisId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * effettua l'aggiornamento del codice dalla modalità di consultazione tesi
     *
     * @param thesisId id della tesi
     * @param thesisDiscussionModeCode codice della modalità di consultazione tesi
     */
    suspend fun putThesisDiscussionMode(
        thesisId: Long,
        thesisDiscussionModeCode: String
    ): Esse3ThesisDiscussionModeCodeInsert {
        return executeJsonPut<Esse3ThesisDiscussionModeCodeInsert>("/tesi/${thesisId}/modConsTesi", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("modConsTesiCod", thesisDiscussionModeCode)
        }
    }

    /**
     * effettua l'inserimento di un relatore per una tesi
     *
     * @param thesisId id della tesi
     * @param body Oggetto che contiene la riga da inserire
     */
    suspend fun putThesisRelation(
        thesisId: Long,
        body: List<Esse3SupervisorsInsert>
    ): Esse3ImportSupervisorsResponse {
        return executeJsonPut<Esse3ImportSupervisorsResponse>("/tesi/${thesisId}/relatori", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * recupera la lista delle modalità di consultazione tesi
     *
     * @param studentId id dello studente
     */
    suspend fun getThesisRelationTypes(
        studentId: Long
    ): List<Esse3SupervisorTypeRegulation> {
        return executeJsonGetList<Esse3SupervisorTypeRegulation>("/tipiRelTesi/${studentId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * recupera la lista delle tipologie di tesi
     *
     * @param committeeRegulationId id delle regole di conseguimento titolo
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getThesisType(
        committeeRegulationId: Long? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3ThesisTypes> {
        return executeJsonGetList<Esse3ThesisTypes>("/tipiTesiStu", setOf(Esse3PermissionLevel.STUDENT)) {
            committeeRegulationId?.let { parameter("regCtId", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }
}
