package it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.compilation.state

/** One-shot effects of the compilation wizard, consumed once and never replayed. */
sealed interface QuestionnaireCompilationEvent {
    /** The questionnaire was definitively confirmed server-side. */
    data object Confirmed : QuestionnaireCompilationEvent

    /** A forward attempt left mandatory questions unanswered. */
    data object MissingAnswers : QuestionnaireCompilationEvent

    data class Failed(val cause: Throwable) : QuestionnaireCompilationEvent
}
