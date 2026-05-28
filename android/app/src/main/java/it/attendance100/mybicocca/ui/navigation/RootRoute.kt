package it.attendance100.mybicocca.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Top-level destinations for AppRoot's NavDisplay. They are dataless markers: any phase payload
// (e.g. the Account behind NeedsCareerPick) is read from RootViewModel.phase in the entry rather than
// carried here, so the keys stay trivially serializable for the back stack. There is no Splash route —
// the loading window is covered by the OS splash + the Compose SplashRevealOverlay, not a NavDisplay
// entry.
sealed interface RootRoute : NavKey {
    @Serializable data object Login : RootRoute
    @Serializable data object CareerPick : RootRoute
    @Serializable data object Main : RootRoute
}

// Loading has no route: AppRoot mounts the NavDisplay only once the phase resolves, so this returns
// null while loading and a real destination afterwards.
fun RootPhase.toRootRoute(): RootRoute? = when (this) {
    RootPhase.Loading -> null
    RootPhase.Authenticating -> RootRoute.Login
    is RootPhase.NeedsCareerPick -> RootRoute.CareerPick
    is RootPhase.SignedIn -> RootRoute.Main
}
