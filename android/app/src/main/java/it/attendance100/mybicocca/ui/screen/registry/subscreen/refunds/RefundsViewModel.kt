package it.attendance100.mybicocca.ui.screen.registry.subscreen.refunds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.tax.Refund
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.tax.GetRefundsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject

/**
 * Backs the Rimborsi sheet: the active career's reimbursements fetched live from Esse3 (no
 * local cache), sorted newest academic year first. [refunds] is the list as a [Loadable]
 * snapshot, [syncStatus] tracks the fetch, and [refresh] re-fetches on demand.
 */
@HiltViewModel
class RefundsViewModel @Inject constructor(
    private val getRefunds: GetRefundsUseCase,
    observeActiveAccount: ObserveActiveAccountUseCase,
) : ViewModel() {

    private val activeCareerId: StateFlow<CareerId?> = observeActiveAccount()
        .map { it?.academic?.selectedCareerId }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _refunds = MutableStateFlow<Loadable<List<Refund>>>(Loadable.NotYetLoaded)
    val refunds: StateFlow<Loadable<List<Refund>>> = _refunds.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val refreshMutex = Mutex()

    init {
        viewModelScope.launch {
            activeCareerId.filterNotNull().collect { fetch(it) }
        }
    }

    fun refresh() {
        val careerId = activeCareerId.value ?: return
        viewModelScope.launch { fetch(careerId) }
    }

    private suspend fun fetch(careerId: CareerId) {
        if (!refreshMutex.tryLock()) return
        try {
            _syncStatus.value = SyncStatus.Refreshing
            runCatching { getRefunds(careerId) }.fold(
                onSuccess = { list ->
                    _refunds.value = Loadable.Loaded(list.sortedByDescending { it.academicYear ?: Int.MIN_VALUE })
                    _syncStatus.value = SyncStatus.Idle
                },
                onFailure = { cause -> _syncStatus.value = SyncStatus.Failed(cause) },
            )
        } finally {
            refreshMutex.unlock()
        }
    }
}
