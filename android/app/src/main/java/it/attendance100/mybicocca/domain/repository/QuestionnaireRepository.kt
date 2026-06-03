package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.questionnaire.ActivityQuestionnaires
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivity
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireAnswer
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireCompilationStart
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnairePage
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireSession
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireSummary
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireTarget

// No local cache: questionnaire availability and compilation state are volatile and
// compilation is an interactive server-side session (same rationale as exam bookings).
interface QuestionnaireRepository {

    suspend fun getActivities(careerId: CareerId): List<QuestionnaireActivity>

    suspend fun getActivityQuestionnaires(
        careerId: CareerId,
        activityChoiceId: Long,
    ): ActivityQuestionnaires

    suspend fun startCompilation(
        careerId: CareerId,
        target: QuestionnaireTarget,
    ): QuestionnaireCompilationStart

    suspend fun savePageAnswers(
        session: QuestionnaireSession,
        pageId: Long,
        answers: List<QuestionnaireAnswer>,
    )

    suspend fun getNextPage(session: QuestionnaireSession, pageId: Long): QuestionnairePage

    suspend fun getPreviousPage(session: QuestionnaireSession, pageId: Long): QuestionnairePage

    suspend fun getSummary(session: QuestionnaireSession): QuestionnaireSummary

    suspend fun confirm(session: QuestionnaireSession)
}
