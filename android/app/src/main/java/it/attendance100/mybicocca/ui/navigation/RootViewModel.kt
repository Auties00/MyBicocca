package it.attendance100.mybicocca.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.career.PickCareerUseCase
import it.attendance100.mybicocca.domain.usecase.connectivity.ObserveConnectivityUseCase
import it.attendance100.mybicocca.ui.navigation.route.RootPhase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Resolves the app's top-level journey for [AppRoot]: whether to show login, the career picker or
 * the main shell, plus the app-wide connectivity flag. Both exposed streams are derived state;
 * [onSignedIn] and [onCareerPicked] are the actions the auth and career-pick screens report into.
 */
@HiltViewModel
class RootViewModel @Inject constructor(
    observeActiveAccount: ObserveActiveAccountUseCase,
    private val pickCareerUseCase: PickCareerUseCase,
    observeConnectivity: ObserveConnectivityUseCase,
) : ViewModel() {

    private val _pendingPickFor = MutableStateFlow<AccountId?>(null)

    /** Drives the app-wide connectivity band in [AppRoot] and the LocalIsOnline gate for action buttons. */
    val isOnline: StateFlow<Boolean> = observeConnectivity()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    /**
     * The resolved session phase. [RootPhase.Loading] is only the initial value before the active
     * account first emits: the visible splash is driven by the Activity, not by a timed loading
     * phase, so there is no artificial minimum hold — the app resolves and reveals as soon as it can.
     */
    val phase: StateFlow<RootPhase> = combine(
        observeActiveAccount(),
        _pendingPickFor,
    ) { active, pendingPick ->
        when {
            active == null -> RootPhase.Authenticating
            pendingPick == active.id -> RootPhase.NeedsCareerPick(active)
            else -> RootPhase.SignedIn(active)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, RootPhase.Loading)

    /**
     * Records a completed sign-in; with [requiresPick] the phase holds at
     * [RootPhase.NeedsCareerPick] for that account until [onCareerPicked] releases it.
     */
    fun onSignedIn(accountId: AccountId, requiresPick: Boolean) {
        _pendingPickFor.value = if (requiresPick) accountId else null
    }

    /** Persists the chosen career and lifts the pending career-pick gate. */
    fun onCareerPicked(accountId: AccountId, careerId: CareerId) {
        viewModelScope.launch {
            pickCareerUseCase(accountId, careerId)
            if (_pendingPickFor.value == accountId) _pendingPickFor.value = null
        }
    }
}
