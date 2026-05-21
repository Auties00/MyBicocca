package it.attendance100.mybicocca.ui.screen.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.usecase.account.GetUserPhotoUseCase
import it.attendance100.mybicocca.domain.usecase.account.ObserveAccountEventsUseCase
import it.attendance100.mybicocca.domain.usecase.account.ObserveAccountsUseCase
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.career.PickCareerUseCase
import it.attendance100.mybicocca.domain.usecase.account.SignOutUseCase
import it.attendance100.mybicocca.domain.usecase.account.SwitchAccountUseCase
import it.attendance100.mybicocca.domain.usecase.career.SwitchCareerUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import it.attendance100.mybicocca.ui.screen.account.state.AccountEvent
import it.attendance100.mybicocca.domain.model.account.AccountEvent as DomainAccountEvent

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AccountViewModel @Inject constructor(
    observeAccounts: ObserveAccountsUseCase,
    observeActiveAccount: ObserveActiveAccountUseCase,
    observeEvents: ObserveAccountEventsUseCase,
    private val switchAccountUseCase: SwitchAccountUseCase,
    private val switchCareerUseCase: SwitchCareerUseCase,
    private val pickCareerUseCase: PickCareerUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getUserPhotoUseCase: GetUserPhotoUseCase,
) : ViewModel() {

    val accounts: StateFlow<List<Account>> = observeAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(KEEP_ALIVE_MS), emptyList())

    val activeAccount: StateFlow<Account?> = observeActiveAccount()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val careers: StateFlow<List<Career>> = activeAccount
        .map { it?.academic?.careers.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(KEEP_ALIVE_MS), emptyList())

    val selectedCareerId: StateFlow<CareerId?> = activeAccount
        .map { it?.academic?.selectedCareerId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val userPhoto: StateFlow<File?> = activeAccount
        .map { it?.id }
        .distinctUntilChanged()
        .flatMapLatest { id ->
            if (id == null) flowOf<File?>(null)
            else flow<File?> {
                emit(null)
                runCatching { getUserPhotoUseCase(id) }
                    .getOrNull()
                    ?.let { emit(File(it)) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(KEEP_ALIVE_MS), null)

    private val _ownEvents = Channel<AccountEvent>(Channel.BUFFERED)

    val events: Flow<AccountEvent> = merge(
        _ownEvents.receiveAsFlow(),
        observeEvents().map(::mapDomainEvent),
    )

    fun switchAccount(accountId: AccountId) {
        viewModelScope.launch {
            switchAccountUseCase(accountId)
            activeAccount.value
                ?.takeIf { it.id == accountId }
                ?.let { _ownEvents.send(AccountEvent.Switched(it)) }
        }
    }

    fun switchCareer(accountId: AccountId, careerId: CareerId) {
        viewModelScope.launch { switchCareerUseCase(accountId, careerId) }
    }

    fun pickCareer(accountId: AccountId, careerId: CareerId) {
        viewModelScope.launch { pickCareerUseCase(accountId, careerId) }
    }

    fun signOut(accountId: AccountId) {
        viewModelScope.launch {
            signOutUseCase(accountId)
            _ownEvents.send(AccountEvent.SignedOut(accountId))
        }
    }

    private fun mapDomainEvent(event: DomainAccountEvent): AccountEvent = when (event) {
        is DomainAccountEvent.NewCareerAvailable -> AccountEvent.NewCareerAvailable(event.accountId, event.career)
        is DomainAccountEvent.SelectedCareerEnded -> AccountEvent.SelectedCareerEnded(event.accountId, event.career)
        is DomainAccountEvent.SelectedCareerMissing -> AccountEvent.SelectedCareerMissing(event.accountId)
        is DomainAccountEvent.RequireReauth -> AccountEvent.RequireReauth(event.accountId, event.cause)
    }

    private companion object {
        const val KEEP_ALIVE_MS = 5_000L
    }
}
