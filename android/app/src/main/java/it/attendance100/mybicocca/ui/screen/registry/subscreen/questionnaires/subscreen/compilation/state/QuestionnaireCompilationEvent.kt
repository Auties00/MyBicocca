package it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.compilation.state

sealed interface QuestionnaireCompilationEvent {
    data object Confirmed : QuestionnaireCompilationEvent
    data object MissingAnswers : QuestionnaireCompilationEvent
    data class Failed(val cause: Throwable) : QuestionnaireCompilationEvent
}
