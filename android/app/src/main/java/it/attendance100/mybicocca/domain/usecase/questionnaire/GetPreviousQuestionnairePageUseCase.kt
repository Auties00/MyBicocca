package it.attendance100.mybicocca.domain.usecase.questionnaire

import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnairePage
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireSession
import it.attendance100.mybicocca.domain.repository.QuestionnaireRepository
import javax.inject.Inject

/**
 * Fetches the page that precedes pageId in an in-flight compilation, used by the
 * compilation sub-screen's back navigation; the returned page carries the answers
 * already saved on the server so they can be re-rendered. Throws on failure.
 */
class GetPreviousQuestionnairePageUseCase @Inject constructor(
    private val repository: QuestionnaireRepository,
) {
    suspend operator fun invoke(session: QuestionnaireSession, pageId: Long): QuestionnairePage =
        repository.getPreviousPage(session, pageId)
}
