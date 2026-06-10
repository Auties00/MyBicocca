package it.attendance100.mybicocca.domain.usecase.questionnaire

import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireSession
import it.attendance100.mybicocca.domain.repository.QuestionnaireRepository
import javax.inject.Inject

/**
 * Confirms (finalises) a fully answered VAL_DID questionnaire compilation, the last
 * step of the compilation sub-screen in the registry tab. Once confirmed the unit is
 * marked completed server-side and the answers are immutable. Throws on failure.
 */
class ConfirmQuestionnaireUseCase @Inject constructor(
    private val repository: QuestionnaireRepository,
) {
    suspend operator fun invoke(session: QuestionnaireSession) = repository.confirm(session)
}
