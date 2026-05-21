package it.attendance100.mybicocca.domain.usecase.elearning.quiz

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptAnswer
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptId
import it.attendance100.mybicocca.domain.repository.ElearningQuizRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveDraftAnswersUseCase @Inject constructor(
    private val repository: ElearningQuizRepository,
) {
    operator fun invoke(accountId: AccountId, attemptId: AttemptId): Flow<List<AttemptAnswer>> =
        repository.observeDraftAnswers(accountId, attemptId)
}
