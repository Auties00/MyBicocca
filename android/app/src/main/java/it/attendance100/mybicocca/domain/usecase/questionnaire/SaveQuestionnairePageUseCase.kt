package it.attendance100.mybicocca.domain.usecase.questionnaire

import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireAnswer
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireSession
import it.attendance100.mybicocca.domain.repository.QuestionnaireRepository
import javax.inject.Inject

class SaveQuestionnairePageUseCase @Inject constructor(
    private val repository: QuestionnaireRepository,
) {
    suspend operator fun invoke(
        session: QuestionnaireSession,
        pageId: Long,
        answers: List<QuestionnaireAnswer>,
    ) = repository.savePageAnswers(session, pageId, answers)
}
