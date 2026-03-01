package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3Answer
import it.attendance100.mybicocca.data.dto.esse3.Esse3CompiledQuestionnaires
import it.attendance100.mybicocca.data.dto.esse3.Esse3InternshipQuestionnaires
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3QuestionnaireCompiledEventTagsGeneralQuestionnaire
import it.attendance100.mybicocca.data.dto.esse3.Esse3QuestionnaireCompiledEventTagsPostLogin
import it.attendance100.mybicocca.data.dto.esse3.Esse3QuestionnaireCompiledEventTagsTeacherAvailability
import it.attendance100.mybicocca.data.dto.esse3.Esse3QuestionnaireCompiledEventTagsTeachingEvaluation
import it.attendance100.mybicocca.data.dto.esse3.Esse3QuestionnairePage
import it.attendance100.mybicocca.data.dto.esse3.Esse3QuestionnaireSummary
import it.attendance100.mybicocca.data.dto.esse3.Esse3TagsList
import it.attendance100.mybicocca.data.dto.esse3.Esse3TeachingUnitWithQuestionnaire
import it.attendance100.mybicocca.data.dto.esse3.Esse3TranscriptRowWithQuestionnaireStatus
import it.attendance100.mybicocca.data.dto.esse3.Esse3UserCompiledEventTagsGeneralQuestionnaire
import it.attendance100.mybicocca.data.dto.esse3.Esse3UserCompiledEventTagsPostLogin
import it.attendance100.mybicocca.data.dto.esse3.Esse3UserCompiledEventTagsTeacherAvailability
import it.attendance100.mybicocca.data.dto.esse3.Esse3UserCompiledEventTagsTeachingEvaluation
import kotlinx.serialization.json.Json

class Esse3QuestionnairesApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/questionari-service-v1") {

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

    suspend fun getCompiledQuestionnaires(
        questionComponentId: Long
    ): List<Esse3CompiledQuestionnaires> {
        return executeJsonGetList<Esse3CompiledQuestionnaires>("/questionari/questionariCompilati/${questionComponentId}/", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getInternshipQuestionnaires(
        domicileInternshipId: Long
    ): Esse3InternshipQuestionnaires {
        return executeJsonGet<Esse3InternshipQuestionnaires>("/questionari/tirocinio/${domicileInternshipId}", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

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
