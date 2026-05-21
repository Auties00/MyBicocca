package it.attendance100.mybicocca.ui.screen.registry.subscreen.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.exam.ExamCall
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.exam.GetExamCallsUseCase
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

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val getExamCalls: GetExamCallsUseCase,
    observeActiveAccount: ObserveActiveAccountUseCase,
) : ViewModel() {

    private val activeCareerId: StateFlow<CareerId?> = observeActiveAccount()
        .map { it?.academic?.selectedCareerId }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _examCalls = MutableStateFlow<Loadable<List<ExamCall>>>(Loadable.NotYetLoaded)
    val examCalls: StateFlow<Loadable<List<ExamCall>>> = _examCalls.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    // tryLock so a concurrent caller is dropped, not queued — better latency than a
    // serialized queue, and the user shouldn't see two refresh spinners overlap.
    private val refreshMutex = Mutex()

    init {
        viewModelScope.launch {
            activeCareerId.filterNotNull().collect { careerId ->
                fetch(careerId)
            }
        }
    }

    fun refresh() {
        val careerId = activeCareerId.value ?: return
        viewModelScope.launch { fetch(careerId) }
    }

    // pull-to-refresh: invalidate the cached list so the screen falls back to its cold
    // loading indicator while the new fetch is in flight. refresh() keeps the existing
    // list visible (used after a successful booking).
    fun pullToRefresh() {
        val careerId = activeCareerId.value ?: return
        _examCalls.value = Loadable.NotYetLoaded
        viewModelScope.launch { fetch(careerId) }
    }

    private suspend fun fetch(careerId: CareerId) {
        if (!refreshMutex.tryLock()) return
        try {
            _syncStatus.value = SyncStatus.Refreshing
            runCatching { getExamCalls(careerId) }.fold(
                onSuccess = { list ->
                    _examCalls.value = Loadable.Loaded(list)
                    _syncStatus.value = SyncStatus.Idle
                },
                onFailure = { cause ->
                    _syncStatus.value = SyncStatus.Failed(cause)
                },
            )
        } finally {
            refreshMutex.unlock()
        }
    }
}
