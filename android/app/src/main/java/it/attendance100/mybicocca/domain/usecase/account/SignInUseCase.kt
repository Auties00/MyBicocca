package it.attendance100.mybicocca.domain.usecase.account

import it.attendance100.mybicocca.domain.model.account.SignInFailure
import it.attendance100.mybicocca.domain.model.account.SignInResult
import it.attendance100.mybicocca.domain.repository.AccountRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Signs a student in with university credentials from the auth screen; on success the account
 * is saved, activated, and the result says whether a career pick must follow.
 *
 * Re-adding an account that is already stored is rejected before any network call. Users type
 * either the bare username or the full Ateneo e-mail, so the duplicate check compares the
 * normalized local part against both stored identities (Esse3 and Moodle usernames).
 */
class SignInUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(username: String, password: String): SignInResult {
        val typed = username.normalizedLocalPart()
        val alreadySignedIn = accountRepository.observeAccounts().first().any { account ->
            account.username.normalizedLocalPart() == typed ||
                account.learning.lmsUsername.normalizedLocalPart() == typed
        }
        if (alreadySignedIn) {
            return SignInResult.Failure(SignInFailure.AlreadySignedIn)
        }
        return accountRepository.signIn(username, password)
    }
}

private fun String.normalizedLocalPart(): String = trim().lowercase().substringBefore('@')
