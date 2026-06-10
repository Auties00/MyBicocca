package it.attendance100.mybicocca.domain.usecase.questionnaire

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireCompilationStart
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireTarget
import it.attendance100.mybicocca.domain.repository.QuestionnaireRepository
import javax.inject.Inject

/**
 * Starts a fresh VAL_DID compilation for the chosen questionnaire unit, triggered when
 * the student enters the compilation sub-screen from the questionnaires list. Every
 * start creates a brand-new server-side compilation instance (drafts are never
 * resumed) and returns its session ids plus the first page. Throws on failure.
 */
class StartQuestionnaireUseCase @Inject constructor(
    private val repository: QuestionnaireRepository,
) {
    suspend operator fun invoke(
        careerId: CareerId,
        target: QuestionnaireTarget,
    ): QuestionnaireCompilationStart = repository.startCompilation(careerId, target)
}
