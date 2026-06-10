package it.attendance100.mybicocca.domain.usecase.elearning.quiz

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId
import it.attendance100.mybicocca.domain.repository.ElearningQuizRepository
import javax.inject.Inject

/**
 * Syncs the student's attempts and best grade for a quiz from the e-learning platform into the
 * cache when the quiz detail sheet opens and after an attempt is submitted. Throws on failure.
 */
class RefreshQuizAttemptsUseCase @Inject constructor(
    private val repository: ElearningQuizRepository,
) {
    suspend operator fun invoke(accountId: AccountId, quizId: QuizId) =
        repository.refreshAttempts(accountId, quizId)
}
