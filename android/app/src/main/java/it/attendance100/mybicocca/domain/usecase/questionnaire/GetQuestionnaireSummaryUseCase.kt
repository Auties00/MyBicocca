package it.attendance100.mybicocca.domain.usecase.questionnaire

import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireSession
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireSummary
import it.attendance100.mybicocca.domain.repository.QuestionnaireRepository
import javax.inject.Inject

class GetQuestionnaireSummaryUseCase @Inject constructor(
    private val repository: QuestionnaireRepository,
) {
    suspend operator fun invoke(session: QuestionnaireSession): QuestionnaireSummary =
        repository.getSummary(session)
}
