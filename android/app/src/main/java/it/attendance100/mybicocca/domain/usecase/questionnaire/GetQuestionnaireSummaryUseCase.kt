package it.attendance100.mybicocca.domain.usecase.questionnaire

import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireSession
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireSummary
import it.attendance100.mybicocca.domain.repository.QuestionnaireRepository
import javax.inject.Inject

/**
 * Fetches the server-computed completion summary of an in-flight compilation, used by
 * the compilation sub-screen after the end-marker page to decide whether the confirm
 * action can be offered. Throws on failure.
 */
class GetQuestionnaireSummaryUseCase @Inject constructor(
    private val repository: QuestionnaireRepository,
) {
    suspend operator fun invoke(session: QuestionnaireSession): QuestionnaireSummary =
        repository.getSummary(session)
}
