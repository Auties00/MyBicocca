package it.attendance100.mybicocca.domain.usecase.elearning.quiz

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptAnswer
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptId
import it.attendance100.mybicocca.domain.repository.ElearningQuizRepository
import javax.inject.Inject

/**
 * Saves the student's current answers as the attempt wizard moves between pages — locally
 * first, so an interrupted attempt keeps its answers, then to the e-learning platform's
 * attempt autosave. Throws when the remote save fails.
 */
class SaveQuizDraftUseCase @Inject constructor(
    private val repository: ElearningQuizRepository,
) {
    suspend operator fun invoke(accountId: AccountId, attemptId: AttemptId, answers: List<AttemptAnswer>) =
        repository.saveDraft(accountId, attemptId, answers)
}
