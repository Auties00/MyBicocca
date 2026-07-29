package it.attendance100.mybicocca.ui.screen.account.state

import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.career.Career

/**
 * One-shot account lifecycle notification surfaced by the account ViewModel for hosts to
 * react to: outcomes of local actions ([Switched], [SignedOut]) merged with career
 * reconciliation findings forwarded from the domain layer ([RequireReauth],
 * [NewCareerAvailable], [SelectedCareerEnded], [SelectedCareerMissing]).
 */
sealed interface AccountEvent {
    data class Switched(val account: Account) : AccountEvent
    data class SignedOut(val accountId: AccountId) : AccountEvent
    data class RequireReauth(val accountId: AccountId, val cause: Throwable) : AccountEvent
    data class NewCareerAvailable(val accountId: AccountId, val career: Career) : AccountEvent
    data class SelectedCareerEnded(val accountId: AccountId, val career: Career) : AccountEvent
    data class SelectedCareerMissing(val accountId: AccountId) : AccountEvent
    data class SignOutFailed(val accountId: AccountId, val error: Throwable) : AccountEvent
}
