package it.attendance100.mybicocca.domain.usecase.career

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.repository.AccountRepository
import javax.inject.Inject

/**
 * Re-fetches the account's career list from Esse3 and replaces the cached one, invoked from
 * the account management UI. Emits account events when the refresh reveals a new career or
 * invalidates the selected one; throws when the network call fails.
 */
class RefreshCareersUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(accountId: AccountId) {
        accountRepository.refreshCareers(accountId)
    }
}
