package it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state

import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptAnswer
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptPage
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptReview

sealed interface AttemptUiState {
    data object Idle : AttemptUiState
    data object Loading : AttemptUiState
    data class InProgress(
        val attemptId: AttemptId,
        val page: AttemptPage,
        val answers: List<AttemptAnswer>,
        val flagged: Set<Int>,
    ) : AttemptUiState
    data class Submitting(val attemptId: AttemptId) : AttemptUiState
    data class Reviewing(val review: AttemptReview) : AttemptUiState
}
