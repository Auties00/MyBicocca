package it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.exam.CancelBookingUseCase
import it.attendance100.mybicocca.domain.usecase.exam.GetBookingSlipUseCase
import it.attendance100.mybicocca.domain.usecase.exam.GetBookingsUseCase
import it.attendance100.mybicocca.domain.usecase.exam.GetPresenceCertificateUseCase
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.state.BookedEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.state.CancelActionState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.state.DocDownloadState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.state.ExamDocument
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

// Drives the main Esami screen: the exams the user has booked (Esami prenotati).
// Loads independently of the bookable calls (see BookableExamsViewModel) so the
// list shows as soon as bookings arrive, without waiting on the catalogue.
@HiltViewModel
class BookedExamsViewModel @Inject constructor(
    private val getBookings: GetBookingsUseCase,
    private val cancelBooking: CancelBookingUseCase,
    private val getBookingSlip: GetBookingSlipUseCase,
    private val getPresenceCertificate: GetPresenceCertificateUseCase,
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

    private val _docDownload = MutableStateFlow<DocDownloadState>(DocDownloadState.Idle)
    val docDownload: StateFlow<DocDownloadState> = _docDownload.asStateFlow()

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
                    // Optimistic: drop the cancelled row so it disappears immediately. The
                    // bookable list re-includes it once its key is no longer booked.
                    val current = (_bookings.value as? Loadable.Loaded)?.value.orEmpty()
                    _bookings.value = Loadable.Loaded(current.filterNot { it.identityKey() == booking.identityKey() })
                    fetch(careerId)
                }
                .onFailure { cause ->
                    _cancelAction.value = CancelActionState.Idle
                    _events.trySend(BookedEvent.CancellationFailed(cause))
                }
        }
    }

    fun downloadBookingSlip(booking: BookedExam) = downloadDocument(booking, ExamDocument.BookingSlip)

    fun downloadPresenceCertificate(booking: BookedExam) =
        downloadDocument(booking, ExamDocument.PresenceCertificate)

    private fun downloadDocument(booking: BookedExam, document: ExamDocument) {
        val careerId = activeCareerId.value ?: return
        val studentId = booking.studentId ?: return
        if (_docDownload.value is DocDownloadState.InProgress) return
        viewModelScope.launch {
            _docDownload.value = DocDownloadState.InProgress(booking.identityKey(), document)
            runCatching {
                when (document) {
                    ExamDocument.BookingSlip -> getBookingSlip(careerId, booking.key, studentId)
                    ExamDocument.PresenceCertificate -> getPresenceCertificate(careerId, booking.key, studentId)
                }
            }.onSuccess { bytes ->
                _events.trySend(BookedEvent.OpenPdf(bytes, document.fileName(booking)))
            }.onFailure { cause ->
                _events.trySend(BookedEvent.ShowMessage(document.errorMessage(cause)))
            }
            _docDownload.value = DocDownloadState.Idle
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

// Stable identity for a booking across refreshes — used for list keys and for matching
// the in-progress cancellation. applicationListId/studentId disambiguate two bookings
// that share an exam-call key (rare, but possible across careers).
internal fun BookedExam.identityKey(): String =
    "${key.courseOfStudyId}/${key.activityId}/${key.callId}/${applicationListId ?: studentId ?: 0}"

private fun ExamDocument.fileName(booking: BookedExam): String {
    val suffix = "${booking.key.activityId}_${booking.key.callId}"
    return when (this) {
        ExamDocument.BookingSlip -> "statino_prenotazione_$suffix.pdf"
        ExamDocument.PresenceCertificate -> "attestato_presenza_$suffix.pdf"
    }
}

// The attendance certificate 422s until the outcome is published; surface a clear,
// actionable message instead of the raw server error.
private fun ExamDocument.errorMessage(cause: Throwable): String = when (this) {
    ExamDocument.BookingSlip -> "Impossibile scaricare lo statino di prenotazione. Riprova tra un momento."
    ExamDocument.PresenceCertificate ->
        "L'attestato di presenza non è ancora disponibile per questo appello."
}
