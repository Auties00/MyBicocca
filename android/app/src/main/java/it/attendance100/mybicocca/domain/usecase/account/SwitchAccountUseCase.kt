package it.attendance100.mybicocca.domain.usecase.account

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.repository.AccountRepository
import javax.inject.Inject

/**
 * Makes another saved account the active one, invoked from the account switcher sheet. The
 * whole UI re-keys onto the new account and its last-used timestamp is bumped.
 */
class SwitchAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(accountId: AccountId) {
        accountRepository.switchAccount(accountId)
    }
}
