package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.mapper.questionnaire.toActivityQuestionnaires
import it.attendance100.mybicocca.data.mapper.questionnaire.toDomain
import it.attendance100.mybicocca.data.mapper.questionnaire.toQuestionnaireActivity
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Answer
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TagsList
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.questionnaire.ActivityQuestionnaires
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivity
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireAnswer
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireCompilationStart
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnairePage
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireSession
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireSummary
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireTarget
import it.attendance100.mybicocca.domain.repository.QuestionnaireRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionnaireRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
) : QuestionnaireRepository {

    override suspend fun getActivities(careerId: CareerId): List<QuestionnaireActivity> {
        val career = requireCareer(careerId)
        // questFilter=P returns only activities whose evaluation questionnaires are (or
        // were) reachable; rows whose evaluation window closed drop out server-side.
        val dtos = sessionManager.esse3().questionnaires.getRecordBookQuestionnaires(
            matId = career.enrollmentTraitId,
            questionFilter = "P",
        )
        return withContext(Dispatchers.Default) { dtos.mapNotNull { it.toQuestionnaireActivity() } }
    }

    override suspend fun getActivityQuestionnaires(
        careerId: CareerId,
        activityChoiceId: Long,
    ): ActivityQuestionnaires {
        requireCareer(careerId)
        val dto = sessionManager.esse3().questionnaires.getTeachingUnitQuestionnaireEvaluation(
            activityChoiceId = activityChoiceId,
            eventComponentId = EVENT_COMPONENT,
        )
        return dto.toActivityQuestionnaires()
            ?: error("Esse3 returned teaching units without an activity id.")
    }

    override suspend fun startCompilation(
        careerId: CareerId,
        target: QuestionnaireTarget,
    ): QuestionnaireCompilationStart {
        requireCareer(careerId)
        val page = sessionManager.esse3().questionnaires.setNewSurvey(
            questionnaireId = target.questionnaireId.toString(),
            activityChoiceId = target.activityChoiceId,
            studentId = careerId.value,
            body = Esse3TagsList(tags = target.tags),
            eventComponentId = EVENT_COMPONENT,
            questionConfigId = target.questionnaireConfigId.toLong(),
        )
        val session = QuestionnaireSession(
            careerId = careerId,
            activityChoiceId = target.activityChoiceId,
            questionnaireId = target.questionnaireId,
            questionnaireConfigId = target.questionnaireConfigId,
            compilationId = page.questionComponentId?.toLong()
                ?: error("Esse3 did not return a compilation id."),
            userCompilationId = page.userComponentId?.toLong()
                ?: error("Esse3 did not return a user compilation id."),
        )
        return QuestionnaireCompilationStart(session = session, firstPage = page.toDomain())
    }

    override suspend fun savePageAnswers(
        session: QuestionnaireSession,
        pageId: Long,
        answers: List<QuestionnaireAnswer>,
    ) {
        sessionManager.esse3().questionnaires.savePageAnswers(
            studentId = session.careerId.value,
            questionnaireId = session.questionnaireId.toString(),
            questionComponentId = session.compilationId,
            pageId = pageId,
            body = answers.map { answer ->
                Esse3Answer(
                    applicationId = answer.questionId.toInt(),
                    answerId = answer.optionId.toInt(),
                    // The server rejects answers whose corpoRisposta is absent or null
                    // (422 NO_TEXT) — choice answers must send an empty string.
                    responseBody = answer.freeText,
                )
            },
            eventComponentId = EVENT_COMPONENT,
        )
    }

    override suspend fun getNextPage(
        session: QuestionnaireSession,
        pageId: Long,
    ): QuestionnairePage = sessionManager.esse3().questionnaires.getNextPage(
        studentId = session.careerId.value,
        activityChoiceId = session.activityChoiceId,
        questionnaireId = session.questionnaireId.toString(),
        questionComponentId = session.compilationId,
        pageId = pageId,
        userComponentId = session.userCompilationId,
    ).toDomain()

    override suspend fun getPreviousPage(
        session: QuestionnaireSession,
        pageId: Long,
    ): QuestionnairePage = sessionManager.esse3().questionnaires.getPreviousPage(
        studentId = session.careerId.value,
        activityChoiceId = session.activityChoiceId,
        questionnaireId = session.questionnaireId.toString(),
        questionComponentId = session.compilationId,
        pageId = pageId,
        userComponentId = session.userCompilationId,
    ).toDomain()

    override suspend fun getSummary(session: QuestionnaireSession): QuestionnaireSummary =
        sessionManager.esse3().questionnaires.getComponentSummary(
            studentId = session.careerId.value,
            activityChoiceId = session.activityChoiceId,
            questionComponentId = session.compilationId,
            questionnaireId = session.questionnaireId.toString(),
            questionConfigId = session.questionnaireConfigId.toLong(),
            userComponentId = session.userCompilationId,
            eventComponentId = EVENT_COMPONENT,
        ).toDomain()

    override suspend fun confirm(session: QuestionnaireSession) {
        sessionManager.esse3().questionnaires.putQuestionnaireComponentConfirm(
            activityChoiceId = session.activityChoiceId,
            questionnaireId = session.questionnaireId.toString(),
            questionComponentId = session.compilationId,
            studentId = session.careerId.value,
            questionConfigId = session.questionnaireConfigId.toLong(),
            userComponentId = session.userCompilationId,
            eventComponentId = EVENT_COMPONENT,
        )
    }

    private fun requireCareer(careerId: CareerId): Career {
        val account = sessionManager.activeAccount.value
            ?: error("No active account; cannot resolve career for questionnaires.")
        return account.academic.careers.firstOrNull { it.id == careerId }
            ?: error("Career ${careerId.value} not found on active account.")
    }

    private companion object {
        // The teaching-evaluation compilation event configured at Bicocca. With any other
        // value the unitadidattiche endpoint silently returns null questionnaire ids.
        const val EVENT_COMPONENT = "EV_VAL_DID"
    }
}
