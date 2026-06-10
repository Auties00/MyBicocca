package it.attendance100.mybicocca.domain.usecase.elearning.quiz

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptReview
import it.attendance100.mybicocca.domain.repository.ElearningQuizRepository
import javax.inject.Inject

/**
 * Loads the graded review of a finished attempt from the e-learning platform for the review
 * page of the quiz detail sheet; also reconciles the cached attempt state. Throws on failure.
 */
class LoadAttemptReviewUseCase @Inject constructor(
    private val repository: ElearningQuizRepository,
) {
    suspend operator fun invoke(accountId: AccountId, attemptId: AttemptId): AttemptReview =
        repository.loadReview(accountId, attemptId)
}
