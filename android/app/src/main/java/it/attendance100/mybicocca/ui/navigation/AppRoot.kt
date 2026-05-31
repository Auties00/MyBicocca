package it.attendance100.mybicocca.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.ui.navigation.transitions.LocalRootSharedTransitionScope
import it.attendance100.mybicocca.ui.screen.account.CareerPickerScreen
import it.attendance100.mybicocca.ui.screen.auth.AuthScreen

// A top-level NavDisplay drives the auth / career-pick / main journey so the MyBicocca wordmark
// morphs as a NATIVE Nav3 shared element (via LocalNavAnimatedContentScope) on login -> main. MainShell
// keeps its own inner NavDisplay + SharedTransitionLayout for list -> detail morphs; the two
// shared-transition scopes are independent (LocalRootSharedTransitionScope vs LocalSharedTransitionScope).
@Composable
fun AppRoot(
    viewModel: RootViewModel = hiltViewModel(
        checkNotNull(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
) {
    val phase by viewModel.phase.collectAsStateWithLifecycle()

    RootNavDisplay(phase = phase, viewModel = viewModel)
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun RootNavDisplay(phase: RootPhase, viewModel: RootViewModel) {
    // The first non-Loading phase seeds the back stack; later phases flow through the LaunchedEffect.
    // The elvis is unreachable (RootNavDisplay is only composed once the phase has resolved).
    val backStack = rememberNavBackStack(phase.toRootRoute() ?: RootRoute.Login)
    // Retain the career-pick account across the exit transition: once the phase advances the cast
    // would be null and blank the still-exiting entry.
    var careerPickAccount by remember { mutableStateOf<Account?>(null) }
    LaunchedEffect(phase) {
        (phase as? RootPhase.NeedsCareerPick)?.let { careerPickAccount = it.account }
        val target = phase.toRootRoute() ?: return@LaunchedEffect
        if (backStack.lastOrNull() != target) {
            backStack.clear()
            backStack.add(target)
        }
    }

    SharedTransitionLayout {
        CompositionLocalProvider(LocalRootSharedTransitionScope provides this) {
            NavDisplay(
                backStack = backStack,
                onBack = {},
                entryDecorators = emptyList(),
                transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
                popTransitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
                predictivePopTransitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
                entryProvider = entryProvider {
                    entry<RootRoute.Login> {
                        AuthScreen(
                            onSignedIn = { account, requiresPick ->
                                viewModel.onSignedIn(account.id, requiresPick)
                            },
                        )
                    }
                    entry<RootRoute.CareerPick> {
                        careerPickAccount?.let { account ->
                            CareerPickerScreen(
                                account = account,
                                onPicked = { careerId ->
                                    viewModel.onCareerPicked(
                                        account.id,
                                        careerId
                                    )
                                },
                            )
                        }
                    }
                    entry<RootRoute.Main> { MainShell() }
                },
            )
        }
    }
}
