package it.attendance100.mybicocca.ui.screen.auth.state

import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.SignInFailure

/**
 * One-shot sign-in outcome delivered from the auth ViewModel's event channel to the auth
 * screen: [SignedIn] hands the new [Account] to navigation (its `requiresCareerPick` flag
 * routes through the career picker before the main shell), [Failed] surfaces a snackbar.
 */
sealed interface AuthEvent {
    data class SignedIn(val account: Account, val requiresCareerPick: Boolean) : AuthEvent
    data class Failed(val reason: SignInFailure) : AuthEvent
}
