package it.attendance100.mybicocca.domain.usecase.elearning.quiz

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.quiz.BestGrade
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId
import it.attendance100.mybicocca.domain.repository.ElearningQuizRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams the student's cached best grade for a quiz, shown as the headline result of the quiz
 * detail sheet; the loaded value is null while no best grade is cached.
 */
class ObserveQuizBestGradeUseCase @Inject constructor(
    private val repository: ElearningQuizRepository,
) {
    operator fun invoke(accountId: AccountId, quizId: QuizId): Flow<Loadable<BestGrade?>> =
        repository.observeBestGrade(accountId, quizId)
}
