package it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentHistory
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.enrollment.GetEnrollmentHistoryUseCase
import it.attendance100.mybicocca.domain.usecase.enrollment.GetRenewalWebUrlUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the "Iscrizioni" sheet with the active career's annual-enrollment history.
 *
 * Streams: [history] carries the fetched data as a [Loadable]; [syncStatus] tracks the
 * in-flight refresh separately so stale data can stay on screen while it runs; [events]
 * carries one-shot effects (opening the renewal web flow) that never replay across
 * rotation. A career switch triggers a reload automatically.
 *
 * Actions: [refresh] re-fetches the history; [renew] resolves the Esse3 renewal URL and
 * emits it as an event.
 */
@HiltViewModel
class EnrollmentsViewModel @Inject constructor(
    observeActiveAccount: ObserveActiveAccountUseCase,
    private val getEnrollmentHistory: GetEnrollmentHistoryUseCase,
    private val getRenewalWebUrl: GetRenewalWebUrlUseCase,
) : ViewModel() {

    private val activeCareerId: StateFlow<CareerId?> = observeActiveAccount()
        .map { it?.academic?.selectedCareerId }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _history = MutableStateFlow<Loadable<EnrollmentHistory>>(Loadable.NotYetLoaded)
    val history: StateFlow<Loadable<EnrollmentHistory>> = _history.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _events = Channel<EnrollmentEvent>(Channel.BUFFERED)
    val events: Flow<EnrollmentEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            activeCareerId.filterNotNull().distinctUntilChanged().collect { load(it) }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val id = activeCareerId.filterNotNull().first()
            load(id)
        }
    }

    /**
     * No student REST submission endpoint exists for annual re-enrollment, so renewal
     * opens the official Esse3 web flow. Fired as a one-shot event so it never replays
     * on rotation.
     */
    fun renew() {
        viewModelScope.launch {
            val id = activeCareerId.filterNotNull().first()
            _events.send(EnrollmentEvent.OpenRenewalWeb(getRenewalWebUrl(id)))
        }
    }

    private suspend fun load(careerId: CareerId) {
        _syncStatus.value = SyncStatus.Refreshing
        runCatching { getEnrollmentHistory(careerId) }
            .onSuccess {
                _history.value = Loadable.Loaded(it)
                _syncStatus.value = SyncStatus.Idle
            }
            .onFailure { _syncStatus.value = SyncStatus.Failed(it) }
    }
}

/** One-shot effects of the enrollments sheet, consumed once and never replayed. */
sealed interface EnrollmentEvent {
    /** The Esse3 renewal web flow to open in an external browser. */
    data class OpenRenewalWeb(val url: String) : EnrollmentEvent
}
