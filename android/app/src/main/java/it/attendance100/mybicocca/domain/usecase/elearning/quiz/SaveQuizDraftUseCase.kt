package it.attendance100.mybicocca.domain.usecase.elearning.quiz

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptAnswer
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptId
import it.attendance100.mybicocca.domain.repository.ElearningQuizRepository
import javax.inject.Inject

class SaveQuizDraftUseCase @Inject constructor(
    private val repository: ElearningQuizRepository,
) {
    suspend operator fun invoke(accountId: AccountId, attemptId: AttemptId, answers: List<AttemptAnswer>) =
        repository.saveDraft(accountId, attemptId, answers)
}
