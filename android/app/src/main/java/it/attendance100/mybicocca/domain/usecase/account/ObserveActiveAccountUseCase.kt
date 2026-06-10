package it.attendance100.mybicocca.domain.usecase.account

import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams the active account, null when nobody is signed in. Drives the root routing between
 * the auth flow and the main shell, and every screen that renders per-account data.
 */
class ObserveActiveAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    operator fun invoke(): Flow<Account?> = accountRepository.observeActiveAccount()
}
