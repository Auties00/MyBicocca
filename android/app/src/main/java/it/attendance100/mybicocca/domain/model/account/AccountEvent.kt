package it.attendance100.mybicocca.domain.model.account

import it.attendance100.mybicocca.domain.model.career.Career

/**
 * One-shot session events emitted by the data layer (career reconciliation after a refresh,
 * authentication failures) and surfaced by the account UI as banners and dialogs.
 */
sealed interface AccountEvent {
    /** The selected career stopped being selectable on Esse3, e.g. the student graduated. */
    data class SelectedCareerEnded(val accountId: AccountId, val career: Career) : AccountEvent

    /** The selected career disappeared from Esse3 entirely; another one must be picked. */
    data class SelectedCareerMissing(val accountId: AccountId) : AccountEvent

    /** A career refresh revealed a selectable career that was not stored, e.g. a new enrollment. */
    data class NewCareerAvailable(val accountId: AccountId, val career: Career) : AccountEvent

    /** The stored credentials stopped working against Esse3; a manual sign-in is required. */
    data class RequireReauth(val accountId: AccountId, val cause: Throwable) : AccountEvent
}
