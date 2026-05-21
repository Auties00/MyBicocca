package it.attendance100.mybicocca.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.domain.model.account.Account
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
    private val _addingFor = MutableStateFlow<Account?>(null)

    val phase: StateFlow<RootPhase> = combine(
        observeActiveAccount(),
        _pendingPickFor,
        _addingFor,
    ) { active, pendingPick, addingFor ->
        when {
            active == null -> RootPhase.Authenticating
            pendingPick == active.id -> RootPhase.NeedsCareerPick(active)
            addingFor != null -> RootPhase.AddingAccount(returnTo = addingFor)
            else -> RootPhase.SignedIn(active)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, RootPhase.Loading)

    fun onSignedIn(accountId: AccountId, requiresPick: Boolean) {
        _pendingPickFor.value = if (requiresPick) accountId else null
        // Clearing here covers both first-time sign-in (no-op) and adding a second account
        // (drops the AddingAccount overlay so the new sign-in's active account takes over).
        _addingFor.value = null
    }

    fun onCareerPicked(accountId: AccountId, careerId: CareerId) {
        viewModelScope.launch {
            pickCareerUseCase(accountId, careerId)
            if (_pendingPickFor.value == accountId) _pendingPickFor.value = null
        }
    }

    fun requestAddAccount(returnTo: Account) {
        _addingFor.value = returnTo
    }

    fun cancelAddAccount() {
        _addingFor.value = null
    }
}
