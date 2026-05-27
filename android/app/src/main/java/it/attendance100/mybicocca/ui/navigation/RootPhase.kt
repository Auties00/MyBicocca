package it.attendance100.mybicocca.ui.navigation

import it.attendance100.mybicocca.domain.model.account.Account

sealed interface RootPhase {
    data object Loading : RootPhase
    data object Authenticating : RootPhase
    data class NeedsCareerPick(val account: Account) : RootPhase
    data class SignedIn(val account: Account) : RootPhase
    data class AddingAccount(val returnTo: Account) : RootPhase
}

// Coarse key for AppRoot's AnimatedContent. Crucially, every SignedIn phase shares the single
// "main" key regardless of which Account it carries, so a career switch or photo update (a new
// SignedIn payload) recomposes MainShell in place instead of tearing it down and rebuilding it.
// Authenticating and AddingAccount stay distinct because they pass a different onCancel.
val RootPhase.contentKey: String
    get() = when (this) {
        RootPhase.Loading -> "loading"
        RootPhase.Authenticating -> "auth"
        is RootPhase.AddingAccount -> "add"
        is RootPhase.NeedsCareerPick -> "pick"
        is RootPhase.SignedIn -> "main"
    }
