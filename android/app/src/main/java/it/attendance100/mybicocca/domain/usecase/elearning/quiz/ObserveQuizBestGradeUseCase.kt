package it.attendance100.mybicocca.domain.usecase.elearning.quiz

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.quiz.BestGrade
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId
import it.attendance100.mybicocca.domain.repository.ElearningQuizRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveQuizBestGradeUseCase @Inject constructor(
    private val repository: ElearningQuizRepository,
) {
    operator fun invoke(accountId: AccountId, quizId: QuizId): Flow<Loadable<BestGrade?>> =
        repository.observeBestGrade(accountId, quizId)
}
