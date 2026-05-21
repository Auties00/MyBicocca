package it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state

import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptId

sealed interface QuizDetailOneShotEvent {
    data class AttemptStarted(val id: AttemptId) : QuizDetailOneShotEvent
    data class AttemptSubmitted(val id: AttemptId) : QuizDetailOneShotEvent
    data class RefreshFailed(val cause: Throwable) : QuizDetailOneShotEvent
}
