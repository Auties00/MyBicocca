package it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.exam.ExamResult
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.exam.AcceptExamResultUseCase
import it.attendance100.mybicocca.domain.usecase.exam.GetExamResultsUseCase
import it.attendance100.mybicocca.domain.usecase.exam.RejectExamResultUseCase
import it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults.state.ExamResultActionState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults.state.ExamResultEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject

@HiltViewModel
class ExamResultsViewModel @Inject constructor(
    private val getExamResults: GetExamResultsUseCase,
    private val acceptExamResult: AcceptExamResultUseCase,
    private val rejectExamResult: RejectExamResultUseCase,
    observeActiveAccount: ObserveActiveAccountUseCase,
) : ViewModel() {

    private val activeCareerId: StateFlow<CareerId?> = observeActiveAccount()
        .map { it?.academic?.selectedCareerId }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _results = MutableStateFlow<Loadable<List<ExamResult>>>(Loadable.NotYetLoaded)
    val results: StateFlow<Loadable<List<ExamResult>>> = _results.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _actionState = MutableStateFlow<ExamResultActionState>(ExamResultActionState.Idle)
    val actionState: StateFlow<ExamResultActionState> = _actionState.asStateFlow()

    private val _events = Channel<ExamResultEvent>(Channel.BUFFERED)
    val events: Flow<ExamResultEvent> = _events.receiveAsFlow()

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

    fun pullToRefresh() {
        val careerId = activeCareerId.value ?: return
        _results.value = Loadable.NotYetLoaded
        viewModelScope.launch { fetch(careerId) }
    }

    fun accept(result: ExamResult) = acknowledge(result, accept = true)

    fun reject(result: ExamResult) = acknowledge(result, accept = false)

    private fun acknowledge(result: ExamResult, accept: Boolean) {
        val careerId = activeCareerId.value ?: return
        val applicationListId = result.applicationListId ?: return
        if (_actionState.value is ExamResultActionState.InProgress) return
        viewModelScope.launch {
            _actionState.value = ExamResultActionState.InProgress(applicationListId)
            runCatching {
                if (accept) acceptExamResult(careerId, applicationListId)
                else rejectExamResult(careerId, applicationListId)
            }.onSuccess {
                _actionState.value = ExamResultActionState.Idle
                _events.trySend(
                    if (accept) ExamResultEvent.AcceptSucceeded else ExamResultEvent.RejectSucceeded,
                )
                // Optimistic: drop the acted-on outcome so it leaves the eligible list,
                // then reconcile with the server.
                val current = (_results.value as? Loadable.Loaded)?.value.orEmpty()
                _results.value = Loadable.Loaded(current.filterNot { it.applicationListId == applicationListId })
                fetch(careerId)
            }.onFailure { cause ->
                _actionState.value = ExamResultActionState.Idle
                _events.trySend(ExamResultEvent.Failed(cause))
            }
        }
    }

    private suspend fun fetch(careerId: CareerId) {
        if (!refreshMutex.tryLock()) return
        try {
            _syncStatus.value = SyncStatus.Refreshing
            runCatching { getExamResults(careerId) }.fold(
                onSuccess = { list ->
                    _results.value = Loadable.Loaded(list)
                    _syncStatus.value = SyncStatus.Idle
                },
                onFailure = { cause -> _syncStatus.value = SyncStatus.Failed(cause) },
            )
        } finally {
            refreshMutex.unlock()
        }
    }
}
