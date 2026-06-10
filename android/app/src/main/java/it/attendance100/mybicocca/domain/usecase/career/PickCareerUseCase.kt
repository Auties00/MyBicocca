package it.attendance100.mybicocca.domain.usecase.career

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.repository.AccountRepository
import javax.inject.Inject

/**
 * Confirms the first-time career choice on the picker screen that follows a multi-career
 * sign-in. Performs the same repository call as the in-app career switch, but the two intents
 * stay separate use cases so call sites, telemetry, and flow forks never conflate a first-time
 * pick with a switch from the selector.
 */
class PickCareerUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(accountId: AccountId, careerId: CareerId) {
        accountRepository.selectCareer(accountId, careerId)
    }
}
