package it.attendance100.mybicocca.ui.navigation.route

import it.attendance100.mybicocca.domain.model.account.Account

/**
 * Session phase resolved by [RootViewModel] and mapped by [AppRoot] onto a top-level destination.
 * [Loading] is only the pre-resolution initial value, covered by the splash and never rendered as
 * a route; the other phases correspond 1:1 to [RootRoute]s.
 */
sealed interface RootPhase {
    data object Loading : RootPhase
    data object Authenticating : RootPhase

    /** An account that signed in with multiple careers and must choose one before entering the shell. */
    data class NeedsCareerPick(val account: Account) : RootPhase
    data class SignedIn(val account: Account) : RootPhase
}

