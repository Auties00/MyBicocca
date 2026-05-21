package it.attendance100.mybicocca.data.remote.esse3.api

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Answer
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CompiledQuestionnaires
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3InternshipQuestionnaires
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PermissionLevel
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3QuestionnaireCompiledEventTagsGeneralQuestionnaire
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3QuestionnaireCompiledEventTagsPostLogin
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3QuestionnaireCompiledEventTagsTeacherAvailability
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3QuestionnaireCompiledEventTagsTeachingEvaluation
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3QuestionnairePage
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3QuestionnaireSummary
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TagsList
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TeachingUnitWithQuestionnaire
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TranscriptRowWithQuestionnaireStatus
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3UserCompiledEventTagsGeneralQuestionnaire
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3UserCompiledEventTagsPostLogin
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3UserCompiledEventTagsTeacherAvailability
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3UserCompiledEventTagsTeachingEvaluation
import kotlinx.serialization.json.Json

class Esse3QuestionnairesApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/questionari-service-v1") {

    /**
     * Salva le risposte di una pagina di questionario
     *
     * @param studentId id della carriera dello studente
     * @param questionnaireId codice del questionario
     * @param questionComponentId id del questionario compilato
     * @param pageId identifica la pagina di un questionario
     * @param body Oggetto che contiene le risposte alle domande
     * @param eventComponentId id dell'evento di compilazione del questionario (?)
     */
    suspend fun savePageAnswers(
        studentId: Long,
        questionnaireId: String,
        questionComponentId: Long,
        pageId: Long,
        body: List<Esse3Answer>,
        eventComponentId: String
    ): String {
        return executeJsonPut<String>("/questionari/compilazione/${studentId}/quest/${questionnaireId}/${questionComponentId}/save/${pageId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            parameter("eventCompId", eventComponentId)
        }
    }

    /**
     * Crea un nuovo questionario da compilare
     *
     * @param questionnaireId codice del questionario
     * @param activityChoiceId id dell'attivita' didattica di libretto
     * @param studentId id della carriera dello studente
     * @param body Metodo alternativo di passare la lista dei tag, il body viene trattato con precedenza. È comunque obbligatorio passare la lista dei tag attraverso il body o attraverso query string
     * @param eventComponentId id dell'evento di compilazione del questionario (?)
     * @param questionConfigId id della configurazione del questionario (?)
     * @param tagList (DEPRECATO, usare tagListB) nodo restituito sul dataset UD_LOG_PDS_LIST_WEB dalla retrieve LISTA_UD_QUEST_VAL_DID
     * @param raw Gestisce delle operazioni sulla descrizione delle domande e recupero tag * 1 Recupera la lista dei tag e le domande senza sostituire i placeholder. * 0 (default) recupera le domande con i placeholder restituiti, NON recupera la lista dei tag.
     */
    suspend fun setNewSurvey(
        questionnaireId: String,
        activityChoiceId: Long,
        studentId: Long,
        body: Esse3TagsList,
        eventComponentId: String,
        questionConfigId: Long,
        tagList: String? = null,
        raw: Long? = null
    ): Esse3QuestionnairePage {
        return executeJsonPut<Esse3QuestionnairePage>("/questionari/compilazione/${studentId}/${activityChoiceId}/quest/${questionnaireId}/start", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            parameter("eventCompId", eventComponentId)
            tagList?.let { parameter("tagList", it) }
            parameter("questConfigId", questionConfigId)
            raw?.let { parameter("raw", it) }
        }
    }

    /**
     * Conferma la compilazione di un questionario
     *
     * @param activityChoiceId id dell'attivita' didattica di libretto
     * @param questionnaireId codice del questionario
     * @param questionComponentId id del questionario compilato
     * @param studentId id della carriera dello studente
     * @param questionConfigId id della configurazione del questionario (?)
     * @param userComponentId id della sessione di compilazione dell'utente
     * @param eventComponentId id dell'evento di compilazione del questionario (?)
     */
    suspend fun putQuestionnaireComponentConfirm(
        activityChoiceId: Long,
        questionnaireId: String,
        questionComponentId: Long,
        studentId: Long,
        questionConfigId: Long,
        userComponentId: Long,
        eventComponentId: String
    ): String {
        return executeJsonPut<String>("/questionari/compilazione/${studentId}/${activityChoiceId}/quest/${questionnaireId}/${questionComponentId}/conferma", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("questConfigId", questionConfigId)
            parameter("userCompId", userComponentId)
            parameter("eventCompId", eventComponentId)
        }
    }

    /**
     * Recupera una pagina del questionario
     *
     * @param studentId id della carriera dello studente
     * @param activityChoiceId id dell'attivita' didattica di libretto
     * @param questionnaireId codice del questionario
     * @param questionComponentId id del questionario compilato
     * @param pageId identifica la pagina di un questionario
     * @param userComponentId id della sessione di compilazione dell'utente
     * @param raw Gestisce delle operazioni sulla descrizione delle domande e recupero tag * 1 Recupera la lista dei tag e le domande senza sostituire i placeholder. * 0 (default) recupera le domande con i placeholder restituiti, NON recupera la lista dei tag.
     */
    suspend fun getPage(
        studentId: Long,
        activityChoiceId: Long,
        questionnaireId: String,
        questionComponentId: Long,
        pageId: Long,
        userComponentId: Long,
        raw: Long? = null
    ): Esse3QuestionnairePage {
        return executeJsonGet<Esse3QuestionnairePage>("/questionari/compilazione/${studentId}/${activityChoiceId}/quest/${questionnaireId}/${questionComponentId}/getPagina/${pageId}/", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("userCompId", userComponentId)
            raw?.let { parameter("raw", it) }
        }
    }

    /**
     * Recupera la pagina successiva di un questionario
     *
     * @param studentId id della carriera dello studente
     * @param activityChoiceId id dell'attivita' didattica di libretto
     * @param questionnaireId codice del questionario
     * @param questionComponentId id del questionario compilato
     * @param pageId identifica la pagina di un questionario
     * @param userComponentId id della sessione di compilazione dell'utente
     * @param raw Gestisce delle operazioni sulla descrizione delle domande e recupero tag * 1 Recupera la lista dei tag e le domande senza sostituire i placeholder. * 0 (default) recupera le domande con i placeholder restituiti, NON recupera la lista dei tag.
     */
    suspend fun getNextPage(
        studentId: Long,
        activityChoiceId: Long,
        questionnaireId: String,
        questionComponentId: Long,
        pageId: Long,
        userComponentId: Long,
        raw: Long? = null
    ): Esse3QuestionnairePage {
        return executeJsonGet<Esse3QuestionnairePage>("/questionari/compilazione/${studentId}/${activityChoiceId}/quest/${questionnaireId}/${questionComponentId}/pagina/${pageId}/next", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("userCompId", userComponentId)
            raw?.let { parameter("raw", it) }
        }
    }

    /**
     * Recupera la pagina precedente di un questionario
     *
     * @param studentId id della carriera dello studente
     * @param activityChoiceId id dell'attivita' didattica di libretto
     * @param questionnaireId codice del questionario
     * @param questionComponentId id del questionario compilato
     * @param pageId identifica la pagina di un questionario
     * @param userComponentId id della sessione di compilazione dell'utente
     * @param raw Gestisce delle operazioni sulla descrizione delle domande e recupero tag * 1 Recupera la lista dei tag e le domande senza sostituire i placeholder. * 0 (default) recupera le domande con i placeholder restituiti, NON recupera la lista dei tag.
     */
    suspend fun getPreviousPage(
        studentId: Long,
        activityChoiceId: Long,
        questionnaireId: String,
        questionComponentId: Long,
        pageId: Long,
        userComponentId: Long,
        raw: Long? = null
    ): Esse3QuestionnairePage {
        return executeJsonGet<Esse3QuestionnairePage>("/questionari/compilazione/${studentId}/${activityChoiceId}/quest/${questionnaireId}/${questionComponentId}/pagina/${pageId}/prev", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("userCompId", userComponentId)
            raw?.let { parameter("raw", it) }
        }
    }

    /**
     * Recupera il riepilogo del questionario compilato
     *
     * @param studentId id della carriera dello studente
     * @param activityChoiceId id dell'attivita' didattica di libretto
     * @param questionComponentId id del questionario compilato
     * @param questionnaireId codice del questionario
     * @param questionConfigId id della configurazione del questionario (?)
     * @param userComponentId id della sessione di compilazione dell'utente
     * @param eventComponentId id dell'evento di compilazione del questionario (?)
     * @param raw Gestisce delle operazioni sulla descrizione delle domande e recupero tag * 1 Recupera la lista dei tag e le domande senza sostituire i placeholder. * 0 (default) recupera le domande con i placeholder restituiti, NON recupera la lista dei tag.
     */
    suspend fun getComponentSummary(
        studentId: Long,
        activityChoiceId: Long,
        questionComponentId: Long,
        questionnaireId: String,
        questionConfigId: Long,
        userComponentId: Long,
        eventComponentId: String,
        raw: Long? = null
    ): Esse3QuestionnaireSummary {
        return executeJsonGet<Esse3QuestionnaireSummary>("/questionari/compilazione/${studentId}/${activityChoiceId}/quest/${questionnaireId}/${questionComponentId}/summary", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("questConfigId", questionConfigId)
            parameter("userCompId", userComponentId)
            parameter("eventCompId", eventComponentId)
            raw?.let { parameter("raw", it) }
        }
    }

    /**
     * Recupera gli ID degli accessi ai questionari compilati. I parametri opzionali filtrano una serie di acccessi. Vista di query V02_QUEST_DOC_VALDID_TAG_USER
     *
     * @param questionnaireId codice del questionario
     * @param academicYearId Id dell'anno accademico di compilazione del questianario
     * @param userId id utente
     * @param academicYearOfferActivityId Id dell'anno accademico di offerta della AD valutata
     * @param courseOfStudyTeachingActivityCode codice del corso di studio della AD valutata
     * @param courseOfStudyTeachingActivityDescription descrizione del corso di studio della AD valutata (se viene utilizzato il carattere * viene applicato il like)
     * @param academicYearOrderActivityId Id dell'ordinamento della AD valutata
     * @param studyPlanTeachingActivityId id del percorso di studio della AD valutata
     * @param activityCode codice dell'attivita didattica
     * @param activityDescription descrizione  dell'attivita didattica (se viene utilizzato il carattere * viene applicato il like)
     * @param lecturerMatricola matricola del docente valutato
     * @param lecturerSurname cognome del docente valutato
     * @param lecturerName nome del docente valutato
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getUserComponentEventInfoAvaDoc(
        questionnaireId: String,
        academicYearId: Int,
        userId: Long? = null,
        academicYearOfferActivityId: Int? = null,
        courseOfStudyTeachingActivityCode: String? = null,
        courseOfStudyTeachingActivityDescription: String? = null,
        academicYearOrderActivityId: Int? = null,
        studyPlanTeachingActivityId: Long? = null,
        activityCode: String? = null,
        activityDescription: String? = null,
        lecturerMatricola: String? = null,
        lecturerSurname: String? = null,
        lecturerName: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3UserCompiledEventTagsTeacherAvailability> {
        return executeJsonGetList<Esse3UserCompiledEventTagsTeacherAvailability>("/questionari/eventoAvaDoc/datiAccesso/${questionnaireId}/${academicYearId}/", setOf(Esse3PermissionLevel.ANY)) {
            userId?.let { parameter("idUser", it) }
            academicYearOfferActivityId?.let { parameter("aaOffAdId", it) }
            courseOfStudyTeachingActivityCode?.let { parameter("cdsAdCod", it) }
            courseOfStudyTeachingActivityDescription?.let { parameter("cdsAdDes", it) }
            academicYearOrderActivityId?.let { parameter("aaOrdAdId", it) }
            studyPlanTeachingActivityId?.let { parameter("pdsAdId", it) }
            activityCode?.let { parameter("adCod", it) }
            activityDescription?.let { parameter("adDes", it) }
            lecturerMatricola?.let { parameter("docenteMatricola", it) }
            lecturerSurname?.let { parameter("docenteCognome", it) }
            lecturerName?.let { parameter("docenteNome", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * Recupera gli ID dei questionari compilati. I parametri opzionali filtrano una serie di questionari. Vista di query V02_QUEST_DOC_VALDID_TAG_COMP
     *
     * @param questionnaireId codice del questionario
     * @param academicYearId Id dell'anno accademico di compilazione del questianario
     * @param academicYearOfferActivityId Id dell'anno accademico di offerta della AD valutata
     * @param courseOfStudyTeachingActivityCode codice del corso di studio della AD valutata
     * @param courseOfStudyTeachingActivityDescription descrizione del corso di studio della AD valutata (se viene utilizzato il carattere * viene applicato il like)
     * @param academicYearOrderActivityId Id dell'ordinamento della AD valutata
     * @param studyPlanTeachingActivityId id del percorso di studio della AD valutata
     * @param activityCode codice dell'attivita didattica
     * @param activityDescription descrizione  dell'attivita didattica (se viene utilizzato il carattere * viene applicato il like)
     * @param lecturerMatricola matricola del docente valutato
     * @param lecturerSurname cognome del docente valutato
     * @param lecturerName nome del docente valutato
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getQuestionnaireComponentEventInfoAvaDoc(
        questionnaireId: String,
        academicYearId: Int,
        academicYearOfferActivityId: Int? = null,
        courseOfStudyTeachingActivityCode: String? = null,
        courseOfStudyTeachingActivityDescription: String? = null,
        academicYearOrderActivityId: Int? = null,
        studyPlanTeachingActivityId: Long? = null,
        activityCode: String? = null,
        activityDescription: String? = null,
        lecturerMatricola: String? = null,
        lecturerSurname: String? = null,
        lecturerName: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3QuestionnaireCompiledEventTagsTeacherAvailability> {
        return executeJsonGetList<Esse3QuestionnaireCompiledEventTagsTeacherAvailability>("/questionari/eventoAvaDoc/datiCompilazione/${questionnaireId}/${academicYearId}/", setOf(Esse3PermissionLevel.ANY)) {
            academicYearOfferActivityId?.let { parameter("aaOffAdId", it) }
            courseOfStudyTeachingActivityCode?.let { parameter("cdsAdCod", it) }
            courseOfStudyTeachingActivityDescription?.let { parameter("cdsAdDes", it) }
            academicYearOrderActivityId?.let { parameter("aaOrdAdId", it) }
            studyPlanTeachingActivityId?.let { parameter("pdsAdId", it) }
            activityCode?.let { parameter("adCod", it) }
            activityDescription?.let { parameter("adDes", it) }
            lecturerMatricola?.let { parameter("docenteMatricola", it) }
            lecturerSurname?.let { parameter("docenteCognome", it) }
            lecturerName?.let { parameter("docenteNome", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * Recupera gli ID degli accessi ai questionari compilati. I parametri opzionali filtrano una serie di acccessi. Vista di query V02_QUEST_GEN_TAG_USER
     *
     * @param questionnaireId codice del questionario
     * @param academicYearId Id dell'anno accademico di compilazione del questianario
     * @param userId id utente
     * @param courseOfStudyCode codice del corso di studio dello studente
     * @param courseOfStudyDescription descrizione del corso di studio dello studente (se viene utilizzato il carattere * viene applicato il like)
     * @param academicYearOrderId Id dell'ordinamento dello studente
     * @param studyPlanId id del percorso di studio dello studente
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getUserComponentEventInfoGeneralQuestionnaire(
        questionnaireId: String,
        academicYearId: Int,
        userId: Long? = null,
        courseOfStudyCode: String? = null,
        courseOfStudyDescription: String? = null,
        academicYearOrderId: Int? = null,
        studyPlanId: Long? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3UserCompiledEventTagsGeneralQuestionnaire> {
        return executeJsonGetList<Esse3UserCompiledEventTagsGeneralQuestionnaire>("/questionari/eventoGenQuest/datiAccesso/${questionnaireId}/${academicYearId}/", setOf(Esse3PermissionLevel.ANY)) {
            userId?.let { parameter("idUser", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseOfStudyDescription?.let { parameter("cdsDes", it) }
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanId?.let { parameter("pdsId", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * Recupera gli ID dei questionari compilati. I parametri opzionali filtrano una serie di questionari. Vista di query V02_QUEST_GEN_TAG_COMP
     *
     * @param questionnaireId codice del questionario
     * @param academicYearId Id dell'anno accademico di compilazione del questianario
     * @param courseOfStudyCode codice del corso di studio dello studente
     * @param courseOfStudyDescription descrizione del corso di studio dello studente (se viene utilizzato il carattere * viene applicato il like)
     * @param academicYearOrderId Id dell'ordinamento dello studente
     * @param studyPlanId id del percorso di studio dello studente
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getQuestionnaireComponentEventInfoGeneralQuestionnaire(
        questionnaireId: String,
        academicYearId: Int,
        courseOfStudyCode: String? = null,
        courseOfStudyDescription: String? = null,
        academicYearOrderId: Int? = null,
        studyPlanId: Long? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3QuestionnaireCompiledEventTagsGeneralQuestionnaire> {
        return executeJsonGetList<Esse3QuestionnaireCompiledEventTagsGeneralQuestionnaire>("/questionari/eventoGenQuest/datiCompilazione/${questionnaireId}/${academicYearId}/", setOf(Esse3PermissionLevel.ANY)) {
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseOfStudyDescription?.let { parameter("cdsDes", it) }
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanId?.let { parameter("pdsId", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * Recupera gli ID degli accessi ai questionari compilati. I parametri opzionali filtrano una serie di acccessi. Vista di query V02_QUEST_POST_LOGIN_TAG_USER
     *
     * @param questionnaireId codice del questionario
     * @param academicYearId Id dell'anno accademico di compilazione del questianario
     * @param userId id utente
     * @param courseOfStudyCode codice del corso di studio dello studente
     * @param courseOfStudyDescription descrizione del corso di studio dello studente (se viene utilizzato il carattere * viene applicato il like)
     * @param academicYearOrderId Id dell'ordinamento dello studente
     * @param studyPlanId id del percorso di studio dello studente
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getUserComponentEventInfoPostLogin(
        questionnaireId: String,
        academicYearId: Int,
        userId: Long? = null,
        courseOfStudyCode: String? = null,
        courseOfStudyDescription: String? = null,
        academicYearOrderId: Int? = null,
        studyPlanId: Long? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3UserCompiledEventTagsPostLogin> {
        return executeJsonGetList<Esse3UserCompiledEventTagsPostLogin>("/questionari/eventoPostLogin/datiAccesso/${questionnaireId}/${academicYearId}/", setOf(Esse3PermissionLevel.ANY)) {
            userId?.let { parameter("idUser", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseOfStudyDescription?.let { parameter("cdsDes", it) }
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanId?.let { parameter("pdsId", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * Recupera gli ID dei questionari compilati. I parametri opzionali filtrano una serie di questionari. Vista di query V02_QUEST_POST_LOGIN_TAG_COMP
     *
     * @param questionnaireId codice del questionario
     * @param academicYearId Id dell'anno accademico di compilazione del questianario
     * @param courseOfStudyCode codice del corso di studio dello studente
     * @param courseOfStudyDescription descrizione del corso di studio dello studente (se viene utilizzato il carattere * viene applicato il like)
     * @param academicYearOrderId Id dell'ordinamento dello studente
     * @param studyPlanId id del percorso di studio dello studente
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getQuestionnaireComponentEventInfoPostLogin(
        questionnaireId: String,
        academicYearId: Int,
        courseOfStudyCode: String? = null,
        courseOfStudyDescription: String? = null,
        academicYearOrderId: Int? = null,
        studyPlanId: Long? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3QuestionnaireCompiledEventTagsPostLogin> {
        return executeJsonGetList<Esse3QuestionnaireCompiledEventTagsPostLogin>("/questionari/eventoPostLogin/datiCompilazione/${questionnaireId}/${academicYearId}/", setOf(Esse3PermissionLevel.ANY)) {
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseOfStudyDescription?.let { parameter("cdsDes", it) }
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            studyPlanId?.let { parameter("pdsId", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * Recupera gli ID degli accessi ai questionari compilati. I parametri opzionali filtrano una serie di acccessi. Vista di query V02_QUEST_VALDID_TAG_USER
     *
     * @param questionnaireId codice del questionario
     * @param academicYearId Id dell'anno accademico di compilazione del questianario
     * @param userId id utente
     * @param academicYearOfferActivityId Id dell'anno accademico di offerta della AD valutata
     * @param courseOfStudyTeachingActivityCode codice del corso di studio della AD valutata
     * @param courseOfStudyTeachingActivityDescription descrizione del corso di studio della AD valutata (se viene utilizzato il carattere * viene applicato il like)
     * @param academicYearOrderActivityId Id dell'ordinamento della AD valutata
     * @param studyPlanTeachingActivityId id del percorso di studio della AD valutata
     * @param activityCode codice dell'attivita didattica
     * @param activityDescription descrizione  dell'attivita didattica (se viene utilizzato il carattere * viene applicato il like)
     * @param lecturerMatricola matricola del docente valutato
     * @param lecturerSurname cognome del docente valutato
     * @param lecturerName nome del docente valutato
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getUserComponentEventInfoDidacticEvaluation(
        questionnaireId: String,
        academicYearId: Int,
        userId: Long? = null,
        academicYearOfferActivityId: Int? = null,
        courseOfStudyTeachingActivityCode: String? = null,
        courseOfStudyTeachingActivityDescription: String? = null,
        academicYearOrderActivityId: Int? = null,
        studyPlanTeachingActivityId: Long? = null,
        activityCode: String? = null,
        activityDescription: String? = null,
        lecturerMatricola: String? = null,
        lecturerSurname: String? = null,
        lecturerName: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3UserCompiledEventTagsTeachingEvaluation> {
        return executeJsonGetList<Esse3UserCompiledEventTagsTeachingEvaluation>("/questionari/eventoValDid/datiAccesso/${questionnaireId}/${academicYearId}/", setOf(Esse3PermissionLevel.ANY)) {
            userId?.let { parameter("idUser", it) }
            academicYearOfferActivityId?.let { parameter("aaOffAdId", it) }
            courseOfStudyTeachingActivityCode?.let { parameter("cdsAdCod", it) }
            courseOfStudyTeachingActivityDescription?.let { parameter("cdsAdDes", it) }
            academicYearOrderActivityId?.let { parameter("aaOrdAdId", it) }
            studyPlanTeachingActivityId?.let { parameter("pdsAdId", it) }
            activityCode?.let { parameter("adCod", it) }
            activityDescription?.let { parameter("adDes", it) }
            lecturerMatricola?.let { parameter("docenteMatricola", it) }
            lecturerSurname?.let { parameter("docenteCognome", it) }
            lecturerName?.let { parameter("docenteNome", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * Recupera gli ID dei questionari compilati. I parametri opzionali filtrano una serie di questionari. Vista di query V02_QUEST_VALDID_TAG_COMP
     *
     * @param questionnaireId codice del questionario
     * @param academicYearId Id dell'anno accademico di compilazione del questianario
     * @param academicYearOfferActivityId Id dell'anno accademico di offerta della AD valutata
     * @param courseOfStudyTeachingActivityCode codice del corso di studio della AD valutata
     * @param courseOfStudyTeachingActivityDescription descrizione del corso di studio della AD valutata (se viene utilizzato il carattere * viene applicato il like)
     * @param academicYearOrderActivityId Id dell'ordinamento della AD valutata
     * @param studyPlanTeachingActivityId id del percorso di studio della AD valutata
     * @param activityCode codice dell'attivita didattica
     * @param activityDescription descrizione  dell'attivita didattica (se viene utilizzato il carattere * viene applicato il like)
     * @param lecturerMatricola matricola del docente valutato
     * @param lecturerSurname cognome del docente valutato
     * @param lecturerName nome del docente valutato
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getQuestionnaireComponentEventInfoDidacticEvaluation(
        questionnaireId: String,
        academicYearId: Int,
        academicYearOfferActivityId: Int? = null,
        courseOfStudyTeachingActivityCode: String? = null,
        courseOfStudyTeachingActivityDescription: String? = null,
        academicYearOrderActivityId: Int? = null,
        studyPlanTeachingActivityId: Long? = null,
        activityCode: String? = null,
        activityDescription: String? = null,
        lecturerMatricola: String? = null,
        lecturerSurname: String? = null,
        lecturerName: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3QuestionnaireCompiledEventTagsTeachingEvaluation> {
        return executeJsonGetList<Esse3QuestionnaireCompiledEventTagsTeachingEvaluation>("/questionari/eventoValDid/datiCompilazione/${questionnaireId}/${academicYearId}/", setOf(Esse3PermissionLevel.ANY)) {
            academicYearOfferActivityId?.let { parameter("aaOffAdId", it) }
            courseOfStudyTeachingActivityCode?.let { parameter("cdsAdCod", it) }
            courseOfStudyTeachingActivityDescription?.let { parameter("cdsAdDes", it) }
            academicYearOrderActivityId?.let { parameter("aaOrdAdId", it) }
            studyPlanTeachingActivityId?.let { parameter("pdsAdId", it) }
            activityCode?.let { parameter("adCod", it) }
            activityDescription?.let { parameter("adDes", it) }
            lecturerMatricola?.let { parameter("docenteMatricola", it) }
            lecturerSurname?.let { parameter("docenteCognome", it) }
            lecturerName?.let { parameter("docenteNome", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    /**
     * Recupera unita' didattiche e situazione questionari valutazione
     *
     * @param activityChoiceId id dell'attivita' didattica di libretto
     * @param eventComponentId id dell'evento di compilazione del questionario (?)
     * @param domicilePartialCode codice della partizione
     */
    suspend fun getTeachingUnitQuestionnaireEvaluation(
        activityChoiceId: Long,
        eventComponentId: String,
        domicilePartialCode: String? = null
    ): Esse3TeachingUnitWithQuestionnaire {
        return executeJsonGet<Esse3TeachingUnitWithQuestionnaire>("/questionari/libretto/${activityChoiceId}/unitadidattiche", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            parameter("eventCompId", eventComponentId)
            domicilePartialCode?.let { parameter("domPartCod", it) }
        }
    }

    /**
     * Tutte le attività del libretto del tratto di carriera selezionato
     *
     * @param matId id del tratto di carriera su cui calcolare le statistiche
     * @param questionFilter se valorizzato permette di recuperare determinate attività * P recupera tutte le attività con questionari a prescindere dal loro stato * C recupera tutte le attività con questionari ancora da compilare
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param filter il parametro consente di applicare dei filtri alla classe di modello utilizzando il linguaggio  [RSQL](https://github.com/jirutka/rsql-parser). La lista degli operatori utilizzabili è disponibile [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosullerigherecuperate) *NB* il filtro viene applicato DOPO aver recuperato i dati
     */
    suspend fun getRecordBookQuestionnaires(
        matId: Long,
        questionFilter: String? = null,
        optionalFields: String? = null,
        fields: String? = null,
        order: String? = null,
        filter: String? = null
    ): List<Esse3TranscriptRowWithQuestionnaireStatus> {
        return executeJsonGetList<Esse3TranscriptRowWithQuestionnaireStatus>("/questionari/libretto/${matId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            questionFilter?.let { parameter("questFilter", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            fields?.let { parameter("fields", it) }
            order?.let { parameter("order", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    /**
     * Tutte le attività del libretto del tratto di carriera selezionato
     *
     * @param matId id del tratto di carriera su cui calcolare le statistiche
     * @param activityChoiceId id dell'attivita' didattica di libretto
     * @param questionFilter se valorizzato permette di recuperare determinate attività * P recupera tutte le attività con questionari a prescindere dal loro stato * C recupera tutte le attività con questionari ancora da compilare
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getRecordBookRow(
        matId: Long,
        activityChoiceId: Long,
        questionFilter: String? = null,
        optionalFields: String? = null
    ): Esse3TranscriptRowWithQuestionnaireStatus {
        return executeJsonGet<Esse3TranscriptRowWithQuestionnaireStatus>("/questionari/libretto/${matId}/righe/${activityChoiceId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            questionFilter?.let { parameter("questFilter", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    /**
     * Recupera tutti gli elementi del questionario compilato in chiave, con le risposte scelte dal compilatore. Vista di query V02_GEN_QUESTIONARIO ordinata per ORD_VIS ASC
     *
     * @param questionComponentId id del questionario compilato
     */
    suspend fun getCompiledQuestionnaires(
        questionComponentId: Long
    ): List<Esse3CompiledQuestionnaires> {
        return executeJsonGetList<Esse3CompiledQuestionnaires>("/questionari/questionariCompilati/${questionComponentId}/", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * @param domicileInternshipId codice della domanda di tirocinio
     */
    suspend fun getInternshipQuestionnaires(
        domicileInternshipId: Long
    ): Esse3InternshipQuestionnaires {
        return executeJsonGet<Esse3InternshipQuestionnaires>("/questionari/tirocinio/${domicileInternshipId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * @param userId id utente che effettua la modifica
     * @param questionComponentId id del questionario compilato
     * @param visibleKind tipologia di visibilità, VIS_DEST_FLG oppure VIS_PUB_FLG
     * @param visibleValue visibilità da impostare
     */
    suspend fun setVisibility(
        userId: Long,
        questionComponentId: Long,
        visibleKind: String,
        visibleValue: Boolean
    ) {
        val response = executePost("/questionari/visibility/${userId}/${questionComponentId}/${visibleKind}/${visibleValue}")
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }
}
