package it.attendance100.mybicocca.domain.model.account

import it.attendance100.mybicocca.domain.model.career.Career

sealed interface AccountEvent {
    data class SelectedCareerEnded(val accountId: AccountId, val career: Career) : AccountEvent
    data class SelectedCareerMissing(val accountId: AccountId) : AccountEvent
    data class NewCareerAvailable(val accountId: AccountId, val career: Career) : AccountEvent
    data class RequireReauth(val accountId: AccountId, val cause: Throwable) : AccountEvent
}
