package it.attendance100.mybicocca.domain.usecase.elearning.quiz

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptId
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId
import it.attendance100.mybicocca.domain.repository.ElearningQuizRepository
import javax.inject.Inject

/**
 * Starts a new attempt at a quiz on the e-learning platform when the student begins the quiz
 * from the quiz detail sheet (resuming an existing attempt goes through page loading instead);
 * force requests a brand-new attempt. Caches the created attempt and returns its id; throws on
 * failure.
 */
class StartQuizAttemptUseCase @Inject constructor(
    private val repository: ElearningQuizRepository,
) {
    suspend operator fun invoke(accountId: AccountId, quizId: QuizId, force: Boolean = false): AttemptId =
        repository.startAttempt(accountId, quizId, force)
}
