package it.attendance100.mybicocca.domain.usecase.elearning.quiz

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptPage
import it.attendance100.mybicocca.domain.repository.ElearningQuizRepository
import javax.inject.Inject

class LoadAttemptPageUseCase @Inject constructor(
    private val repository: ElearningQuizRepository,
) {
    suspend operator fun invoke(accountId: AccountId, attemptId: AttemptId, page: Int): AttemptPage =
        repository.loadAttemptPage(accountId, attemptId, page)
}
