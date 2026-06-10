package it.attendance100.mybicocca.domain.usecase.account

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.repository.AccountRepository
import javax.inject.Inject

/**
 * Removes a saved account, invoked from the account management UI. Credentials, cached
 * sessions, and every account-scoped Room row are deleted; when the active account is removed,
 * activation falls back to the most recently used remaining account, or to the signed-out state.
 */
class SignOutUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(accountId: AccountId) {
        accountRepository.signOut(accountId)
    }
}
