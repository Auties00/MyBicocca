package it.attendance100.mybicocca.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.career.PickCareerUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    observeActiveAccount: ObserveActiveAccountUseCase,
    private val pickCareerUseCase: PickCareerUseCase,
) : ViewModel() {

    private val _pendingPickFor = MutableStateFlow<AccountId?>(null)

    // Loading is only the initial value before observeActiveAccount() first emits. The visible splash
    // (OS splash + the Compose SplashRevealOverlay) is driven by the Activity, not by a timed Loading
    // phase, so there is no artificial minimum hold here — the app resolves and reveals as soon as it can.
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

    fun onSignedIn(accountId: AccountId, requiresPick: Boolean) {
        _pendingPickFor.value = if (requiresPick) accountId else null
    }

    fun onCareerPicked(accountId: AccountId, careerId: CareerId) {
        viewModelScope.launch {
            pickCareerUseCase(accountId, careerId)
            if (_pendingPickFor.value == accountId) _pendingPickFor.value = null
        }
    }
}
