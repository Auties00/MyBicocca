package it.attendance100.mybicocca.domain.usecase.questionnaire

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireCompilationStart
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireTarget
import it.attendance100.mybicocca.domain.repository.QuestionnaireRepository
import javax.inject.Inject

class StartQuestionnaireUseCase @Inject constructor(
    private val repository: QuestionnaireRepository,
) {
    suspend operator fun invoke(
        careerId: CareerId,
        target: QuestionnaireTarget,
    ): QuestionnaireCompilationStart = repository.startCompilation(careerId, target)
}
