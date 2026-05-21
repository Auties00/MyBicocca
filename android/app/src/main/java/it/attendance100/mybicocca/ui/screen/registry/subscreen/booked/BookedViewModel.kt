package it.attendance100.mybicocca.ui.screen.registry.subscreen.booked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.exam.CancelBookingUseCase
import it.attendance100.mybicocca.domain.usecase.exam.GetBookingsUseCase
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.state.BookedEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.state.CancelActionState
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
class BookedViewModel @Inject constructor(
    private val getBookings: GetBookingsUseCase,
    private val cancelBooking: CancelBookingUseCase,
    observeActiveAccount: ObserveActiveAccountUseCase,
) : ViewModel() {

    private val activeCareerId: StateFlow<CareerId?> = observeActiveAccount()
        .map { it?.academic?.selectedCareerId }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _bookings = MutableStateFlow<Loadable<List<BookedExam>>>(Loadable.NotYetLoaded)
    val bookings: StateFlow<Loadable<List<BookedExam>>> = _bookings.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _cancelAction = MutableStateFlow<CancelActionState>(CancelActionState.Idle)
    val cancelAction: StateFlow<CancelActionState> = _cancelAction.asStateFlow()

    private val _events = Channel<BookedEvent>(Channel.BUFFERED)
    val events: Flow<BookedEvent> = _events.receiveAsFlow()

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
    // loading indicator while the new fetch is in flight.
    fun pullToRefresh() {
        val careerId = activeCareerId.value ?: return
        _bookings.value = Loadable.NotYetLoaded
        viewModelScope.launch { fetch(careerId) }
    }

    fun cancel(booking: BookedExam) {
        val careerId = activeCareerId.value ?: return
        val studentId = booking.studentId ?: return
        if (_cancelAction.value is CancelActionState.InProgress) return
        viewModelScope.launch {
            _cancelAction.value = CancelActionState.InProgress(booking.identityKey())
            runCatching { cancelBooking(careerId, booking.key, studentId) }
                .onSuccess {
                    _cancelAction.value = CancelActionState.Idle
                    _events.trySend(BookedEvent.CancellationSucceeded)
                    // Optimistic: drop the cancelled row locally so the user sees it gone
                    // immediately. The next refresh re-confirms from the server.
                    val current = (_bookings.value as? Loadable.Loaded)?.value.orEmpty()
                    _bookings.value = Loadable.Loaded(current.filterNot { it.identityKey() == booking.identityKey() })
                    // And kick a background refresh so seat counts on the booking screen
                    // can update too if the user navigates there.
                    fetch(careerId)
                }
                .onFailure { cause ->
                    _cancelAction.value = CancelActionState.Idle
                    _events.trySend(BookedEvent.CancellationFailed(cause))
                }
        }
    }

    private suspend fun fetch(careerId: CareerId) {
        if (!refreshMutex.tryLock()) return
        try {
            _syncStatus.value = SyncStatus.Refreshing
            runCatching { getBookings(careerId) }.fold(
                onSuccess = { list ->
                    _bookings.value = Loadable.Loaded(list)
                    _syncStatus.value = SyncStatus.Idle
                },
                onFailure = { cause -> _syncStatus.value = SyncStatus.Failed(cause) },
            )
        } finally {
            refreshMutex.unlock()
        }
    }
}

internal fun BookedExam.identityKey(): String =
    "${key.courseOfStudyId}/${key.activityId}/${key.callId}/${applicationListId ?: studentId ?: 0}"
