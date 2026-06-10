package it.attendance100.mybicocca.domain.usecase.elearning.quiz

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizAttempt
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId
import it.attendance100.mybicocca.domain.repository.ElearningQuizRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams the student's cached attempts at a quiz, newest first, backing the attempt list of
 * the quiz detail sheet — an in-progress attempt is surfaced there as the resume action.
 */
class ObserveQuizAttemptsUseCase @Inject constructor(
    private val repository: ElearningQuizRepository,
) {
    operator fun invoke(accountId: AccountId, quizId: QuizId): Flow<Loadable<List<QuizAttempt>>> =
        repository.observeAttempts(accountId, quizId)
}
