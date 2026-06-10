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

/**
 * VAL_DID course-evaluation questionnaires: discovery of compilable activities/units and
 * the page-by-page compilation flow.
 *
 * Reads are live-first: questionnaire availability and compilation state are volatile and
 * compilation is an interactive server-side session (same rationale as exam bookings).
 * The two list reads keep an offline snapshot of their last success purely for display
 * when the device has no network; compilation itself is never cached and its actions are
 * disabled offline. Esse3 never resumes drafts, so the in-flight session lives in the
 * compilation ViewModel and every method throws on failure.
 */
interface QuestionnaireRepository {

    suspend fun getActivities(careerId: CareerId): List<QuestionnaireActivity>

    suspend fun getActivityQuestionnaires(
        careerId: CareerId,
        activityChoiceId: Long,
    ): ActivityQuestionnaires

    /**
     * Creates a fresh server-side compilation for the target unit and returns its
     * session ids together with the first page.
     */
    suspend fun startCompilation(
        careerId: CareerId,
        target: QuestionnaireTarget,
    ): QuestionnaireCompilationStart

    suspend fun savePageAnswers(
        session: QuestionnaireSession,
        pageId: Long,
        answers: List<QuestionnaireAnswer>,
    )

    /**
     * The page following pageId; navigation is server-driven and the returned page may
     * be the end marker.
     */
    suspend fun getNextPage(session: QuestionnaireSession, pageId: Long): QuestionnairePage

    suspend fun getPreviousPage(session: QuestionnaireSession, pageId: Long): QuestionnairePage

    /** The server-computed completion summary gating [confirm]. */
    suspend fun getSummary(session: QuestionnaireSession): QuestionnaireSummary

    /** Finalises the compilation; afterwards the answers are immutable server-side. */
    suspend fun confirm(session: QuestionnaireSession)
}
