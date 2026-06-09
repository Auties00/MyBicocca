package it.attendance100.mybicocca.ui.screen.auth.state

import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.SignInFailure

sealed interface AuthEvent {
    data class SignedIn(val account: Account, val requiresCareerPick: Boolean) : AuthEvent
    data class Failed(val reason: SignInFailure) : AuthEvent
}
