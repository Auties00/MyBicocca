package it.attendance100.mybicocca.domain.usecase.career

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.repository.AccountRepository
import javax.inject.Inject

/**
 * Changes the account's selected career from the in-app selector; career-scoped features
 * (calendar, transcript, study plan) re-key onto the new selection.
 */
class SwitchCareerUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(accountId: AccountId, careerId: CareerId) {
        accountRepository.selectCareer(accountId, careerId)
    }
}
