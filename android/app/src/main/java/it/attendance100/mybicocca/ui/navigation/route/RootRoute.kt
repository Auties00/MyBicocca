package it.attendance100.mybicocca.ui.navigation.route

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Top-level destinations for [AppRoot]'s NavDisplay. They are dataless markers: any phase payload
 * (e.g. the account behind [RootPhase.NeedsCareerPick]) is read from [RootViewModel]'s phase in
 * the entry rather than carried here, so the keys stay trivially serializable for the back stack.
 * There is no splash route — the loading window is covered by the OS splash, whose exit animation
 * the Activity drives, not by a NavDisplay entry.
 */
sealed interface RootRoute : NavKey {
    @Serializable data object Login : RootRoute
    @Serializable data object CareerPick : RootRoute
    @Serializable data object Main : RootRoute
}

/**
 * Maps the resolved phase to its destination. [RootPhase.Loading] has no route — [AppRoot] mounts
 * the NavDisplay only once the phase has resolved — so it yields null.
 */
fun RootPhase.toRootRoute(): RootRoute? = when (this) {
    RootPhase.Loading -> null
    RootPhase.Authenticating -> RootRoute.Login
    is RootPhase.NeedsCareerPick -> RootRoute.CareerPick
    is RootPhase.SignedIn -> RootRoute.Main
}
