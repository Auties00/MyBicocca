package it.attendance100.mybicocca.domain.usecase.elearning.quiz

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptAnswer
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptId
import it.attendance100.mybicocca.domain.repository.ElearningQuizRepository
import javax.inject.Inject

/**
 * Submits a quiz attempt for grading with the student's final answers when the wizard is
 * finished, or with timeUp when the time limit expires; clears the locally persisted draft
 * answers. Callers refresh the attempt list afterwards to surface the finished state. Throws
 * on failure.
 */
class SubmitQuizAttemptUseCase @Inject constructor(
    private val repository: ElearningQuizRepository,
) {
    suspend operator fun invoke(
        accountId: AccountId,
        attemptId: AttemptId,
        answers: List<AttemptAnswer>,
        timeUp: Boolean = false,
    ) = repository.submitAttempt(accountId, attemptId, answers, timeUp)
}
