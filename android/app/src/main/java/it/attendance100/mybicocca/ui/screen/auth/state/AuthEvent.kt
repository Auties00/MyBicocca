package it.attendance100.mybicocca.ui.screen.auth.state

import it.attendance100.mybicocca.domain.model.account.Account

sealed interface AuthEvent {
    data class SignedIn(val account: Account, val requiresCareerPick: Boolean) : AuthEvent
}
