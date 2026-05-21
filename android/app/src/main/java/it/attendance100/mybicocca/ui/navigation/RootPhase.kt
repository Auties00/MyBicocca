package it.attendance100.mybicocca.ui.navigation

import it.attendance100.mybicocca.domain.model.account.Account

sealed interface RootPhase {
    data object Loading : RootPhase
    data object Authenticating : RootPhase
    data class NeedsCareerPick(val account: Account) : RootPhase
    data class SignedIn(val account: Account) : RootPhase
    data class AddingAccount(val returnTo: Account) : RootPhase
}
