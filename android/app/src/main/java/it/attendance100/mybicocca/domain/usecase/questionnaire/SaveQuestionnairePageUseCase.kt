package it.attendance100.mybicocca.domain.usecase.questionnaire

import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireAnswer
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireSession
import it.attendance100.mybicocca.domain.repository.QuestionnaireRepository
import javax.inject.Inject

/**
 * Saves the student's answers for one page of an in-flight compilation, invoked by the
 * compilation sub-screen before navigating onward. Saved pages exist only within the
 * current compilation instance — Esse3 never resumes drafts across sessions. Throws on
 * failure.
 */
class SaveQuestionnairePageUseCase @Inject constructor(
    private val repository: QuestionnaireRepository,
) {
    suspend operator fun invoke(
        session: QuestionnaireSession,
        pageId: Long,
        answers: List<QuestionnaireAnswer>,
    ) = repository.savePageAnswers(session, pageId, answers)
}
