package it.attendance100.mybicocca.ui.screen.registry.subscreen.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.exam.BookExamRequest
import it.attendance100.mybicocca.domain.model.exam.ExamCall
import it.attendance100.mybicocca.domain.model.exam.ExamCallDetail
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.exam.BookExamUseCase
import it.attendance100.mybicocca.domain.usecase.exam.GetExamCallDetailUseCase
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingActionState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingSheetEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingSheetStep
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingTarget
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingSheetViewModel @Inject constructor(
    private val getDetail: GetExamCallDetailUseCase,
    private val bookExam: BookExamUseCase,
    observeActiveAccount: ObserveActiveAccountUseCase,
) : ViewModel() {

    private val activeCareerId: StateFlow<CareerId?> = observeActiveAccount()
        .map { it?.academic?.selectedCareerId }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _target = MutableStateFlow<BookingTarget?>(null)
    val target: StateFlow<BookingTarget?> = _target.asStateFlow()

    private val _detail = MutableStateFlow<Loadable<ExamCallDetail>>(Loadable.NotYetLoaded)
    val detail: StateFlow<Loadable<ExamCallDetail>> = _detail.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _step = MutableStateFlow(BookingSheetStep.Info)
    val step: StateFlow<BookingSheetStep> = _step.asStateFlow()

    private val _bookingAction = MutableStateFlow<BookingActionState>(BookingActionState.Idle)
    val bookingAction: StateFlow<BookingActionState> = _bookingAction.asStateFlow()

    private val _events = Channel<BookingSheetEvent>(Channel.BUFFERED)
    val events: Flow<BookingSheetEvent> = _events.receiveAsFlow()

    fun open(call: ExamCall) {
        val sameTarget = _target.value?.call?.key == call.key
        _target.value = BookingTarget(
            call = call,
            activityChoiceId = call.activityChoiceId?.takeIf { it > 0 },
        )
        _step.value = BookingSheetStep.Info
        _bookingAction.value = BookingActionState.Idle
        if (!sameTarget) {
            _detail.value = Loadable.NotYetLoaded
        }
        refresh()
    }

    fun close() {
        _target.value = null
        _step.value = BookingSheetStep.Info
        _bookingAction.value = BookingActionState.Idle
        _syncStatus.value = SyncStatus.Idle
    }

    fun goToConfirm() {
        if (_target.value?.canBook == true) _step.value = BookingSheetStep.Confirm
    }

    fun goBackToInfo() {
        _step.value = BookingSheetStep.Info
    }

    fun refresh() {
        val careerId = activeCareerId.value ?: return
        val key = _target.value?.call?.key ?: return
        viewModelScope.launch { fetch(careerId, key) }
    }

    fun confirmBooking(note: String?) {
        val careerId = activeCareerId.value ?: return
        val target = _target.value ?: return
        val adsceId = target.activityChoiceId ?: return
        if (_bookingAction.value is BookingActionState.InProgress) return
        viewModelScope.launch {
            _bookingAction.value = BookingActionState.InProgress
            runCatching {
                bookExam(
                    careerId,
                    target.call.key,
                    BookExamRequest(adsceId, note?.takeIf { it.isNotBlank() }),
                )
            }.onSuccess {
                _bookingAction.value = BookingActionState.Idle
                _events.trySend(BookingSheetEvent.BookedSuccessfully)
            }.onFailure { cause ->
                _bookingAction.value = BookingActionState.Idle
                _events.trySend(BookingSheetEvent.BookingFailed(cause))
            }
        }
    }

    private suspend fun fetch(careerId: CareerId, key: ExamCallKey) {
        _syncStatus.value = SyncStatus.Refreshing
        runCatching { getDetail(careerId, key) }
            .onSuccess {
                _detail.value = Loadable.Loaded(it)
                _syncStatus.value = SyncStatus.Idle
            }
            .onFailure { cause ->
                _syncStatus.value = SyncStatus.Failed(cause)
            }
    }
}
