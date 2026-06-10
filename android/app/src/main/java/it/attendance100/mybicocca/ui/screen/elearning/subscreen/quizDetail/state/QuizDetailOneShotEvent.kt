package it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state

import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptId

/**
 * One-shot events of the quiz sheet: attempt lifecycle markers ([AttemptStarted],
 * [AttemptSubmitted]) and [RefreshFailed], which carries refresh and attempt-action failures
 * alike and surfaces as an error snackbar.
 */
sealed interface QuizDetailOneShotEvent {
    data class AttemptStarted(val id: AttemptId) : QuizDetailOneShotEvent
    data class AttemptSubmitted(val id: AttemptId) : QuizDetailOneShotEvent
    data class RefreshFailed(val cause: Throwable) : QuizDetailOneShotEvent
}
