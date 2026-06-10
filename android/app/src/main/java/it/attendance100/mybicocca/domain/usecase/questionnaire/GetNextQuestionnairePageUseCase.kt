package it.attendance100.mybicocca.domain.usecase.questionnaire

import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnairePage
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireSession
import it.attendance100.mybicocca.domain.repository.QuestionnaireRepository
import javax.inject.Inject

/**
 * Fetches the page that follows pageId in an in-flight compilation, used by the
 * compilation sub-screen after saving the current page. Navigation is server-driven
 * (answers can branch the flow); a returned end-marker page means the questionnaire is
 * ready for its summary and confirmation. Throws on failure.
 */
class GetNextQuestionnairePageUseCase @Inject constructor(
    private val repository: QuestionnaireRepository,
) {
    suspend operator fun invoke(session: QuestionnaireSession, pageId: Long): QuestionnairePage =
        repository.getNextPage(session, pageId)
}
