package it.attendance100.mybicocca.domain.usecase.account

import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams every saved account, most recently used first, for the account switcher and the
 * sign-in duplicate check.
 */
class ObserveAccountsUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    operator fun invoke(): Flow<List<Account>> = accountRepository.observeAccounts()
}
