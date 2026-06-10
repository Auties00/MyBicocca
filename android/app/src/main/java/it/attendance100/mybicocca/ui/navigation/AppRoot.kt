package it.attendance100.mybicocca.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.ui.component.bar.ConnectivityBannerHost
import it.attendance100.mybicocca.ui.navigation.route.RootPhase
import it.attendance100.mybicocca.ui.navigation.route.RootRoute
import it.attendance100.mybicocca.ui.navigation.route.toRootRoute
import it.attendance100.mybicocca.ui.navigation.transitions.LocalRootSharedTransitionScope
import it.attendance100.mybicocca.ui.screen.account.CareerPickerScreen
import it.attendance100.mybicocca.ui.screen.auth.AuthScreen
import it.attendance100.mybicocca.ui.screen.lock.AppLockScreen
import it.attendance100.mybicocca.ui.screen.lock.AppLockViewModel
import it.attendance100.mybicocca.ui.theme.LocalIsOnline

/**
 * Root composable of the whole UI: a top-level NavDisplay that cross-fades between login, career
 * pick and the main shell as [RootViewModel] resolves the session phase. Driving the journey
 * through a NavDisplay (rather than a plain AnimatedContent) is what lets the MyBicocca wordmark
 * morph as a native Nav3 shared element (via LocalNavAnimatedContentScope) from the login header
 * into the main app bar; [MainShell] keeps its own inner NavDisplay + SharedTransitionLayout for
 * list-to-detail morphs, and the two shared-transition scopes are deliberately independent
 * ([LocalRootSharedTransitionScope] vs the shell-level scope).
 *
 * The app-wide connectivity band wraps every phase (login included) and pushes the whole app
 * below itself while offline, with LocalIsOnline gating network-dependent actions underneath.
 * The biometric lock screen is a full-screen cover drawn above everything else, and only guards
 * an established session (the signed-in phase).
 */
@Composable
fun AppRoot(
    viewModel: RootViewModel = hiltViewModel(
        checkNotNull(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
    lockViewModel: AppLockViewModel = hiltViewModel(
        checkNotNull(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
) {
    val phase by viewModel.phase.collectAsStateWithLifecycle()
    val locked by lockViewModel.locked.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
        ConnectivityBannerHost(isOnline = isOnline) {
            CompositionLocalProvider(LocalIsOnline provides isOnline) {
                RootNavDisplay(phase = phase, viewModel = viewModel)
            }
        }
        if (locked && phase is RootPhase.SignedIn) {
            AppLockScreen(viewModel = lockViewModel)
        }
    }
}

/**
 * The phase-driven NavDisplay behind [AppRoot], composed only once the phase has resolved. The
 * first non-loading phase seeds the single-entry back stack (the elvis fallback is unreachable);
 * later phase changes replace that entry, each swap playing as a symmetric cross-fade. The
 * career-pick account is retained past the phase change because the cast on [RootPhase] would
 * turn null the moment the phase advances and blank the still-exiting career-pick entry.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun RootNavDisplay(phase: RootPhase, viewModel: RootViewModel) {
    val backStack = rememberNavBackStack(phase.toRootRoute() ?: RootRoute.Login)
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
