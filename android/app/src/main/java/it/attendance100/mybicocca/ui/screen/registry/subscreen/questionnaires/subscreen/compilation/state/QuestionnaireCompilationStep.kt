package it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.compilation.state

import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnairePage

// The wizard's position. Pages are server-driven (branching), so there is no known
// total — index is only a display counter.
sealed interface QuestionnaireCompilationStep {
    data object Starting : QuestionnaireCompilationStep
    data class StartFailed(val cause: Throwable) : QuestionnaireCompilationStep
    data class Page(val page: QuestionnairePage, val index: Int) : QuestionnaireCompilationStep
    data class Summary(val complete: Boolean) : QuestionnaireCompilationStep
}
