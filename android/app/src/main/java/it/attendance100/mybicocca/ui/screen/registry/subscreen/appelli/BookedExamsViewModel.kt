package it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.exam.CancelBookingUseCase
import it.attendance100.mybicocca.domain.usecase.exam.GetBookingSlipUseCase
import it.attendance100.mybicocca.domain.usecase.exam.GetBookingsUseCase
import it.attendance100.mybicocca.domain.usecase.exam.GetExamCallTotalBookingsUseCase
import it.attendance100.mybicocca.domain.usecase.exam.GetPresenceCertificateUseCase
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.state.BookedEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.state.CancelActionState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.state.DocDownloadState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.state.ExamDocument
import kotlinx.coroutines.CancellationException
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject

/**
 * Drives the booked-exams (prenotazioni) side of the Appelli sheet. Bookings come live from
 * Esse3 on every fetch — no local cache — and load independently of the bookable calls (see
 * BookableExamsViewModel), so the list shows as soon as bookings arrive without waiting on
 * the catalogue. The active career is observed and each change triggers a refetch.
 *
 * Streams by role: [bookings] is the loadable booking list; [syncStatus] tracks the
 * in-flight refresh separately so stale data keeps rendering; [cancelAction] and
 * [docDownload] scope the per-booking spinners; [events] is the one-shot channel for
 * cancellation outcomes, PDFs to open, and user-facing messages.
 *
 * Actions: [refresh] refetches for the active career (refreshes are mutex-guarded, so
 * overlapping requests collapse into one); [cancel] cancels a booking, optimistically
 * dropping the row before reconciling with the server; [downloadBookingSlip] and
 * [downloadPresenceCertificate] fetch the corresponding PDF.
 */
@HiltViewModel
class BookedExamsViewModel @Inject constructor(
    private val getBookings: GetBookingsUseCase,
    private val cancelBooking: CancelBookingUseCase,
    private val getBookingSlip: GetBookingSlipUseCase,
    private val getPresenceCertificate: GetPresenceCertificateUseCase,
    private val getCallTotalBookings: GetExamCallTotalBookingsUseCase,
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

    /** Freshly fetched numIscritti per call, keyed by call identity; see [loadTotalBookings]. */
    private val _callTotals = MutableStateFlow<Map<ExamCallKey, Int>>(emptyMap())
    val callTotals: StateFlow<Map<ExamCallKey, Int>> = _callTotals.asStateFlow()

    private val totalsInFlight = mutableSetOf<ExamCallKey>()

    private val refreshMutex = Mutex()

    init {
        viewModelScope.launch {
            activeCareerId.filterNotNull().collect { careerId ->
                _callTotals.value = emptyMap()
                fetch(careerId)
            }
        }
    }

    /**
     * Fetches the call's total bookings from the per-appello detail endpoint — too slow
     * for list reads (0.3–5 s per call) but fine as one lazy call when a booking's detail page opens.
     * Always refetches (the count is volatile while enrollment is open), with
     * an in-flight guard per call; failures stay silent and the page keeps showing the
     * booking's last persisted total, or the bare position.
     *
     * [totalsInFlight] needs no synchronization: this entry point is UI-only and
     * viewModelScope resumes on Main.immediate, so every access is main-thread-confined.
     */
    fun loadTotalBookings(booking: BookedExam) {
        val careerId = activeCareerId.value ?: return
        val key = booking.key
        if (!totalsInFlight.add(key)) return
        viewModelScope.launch {
            try {
                val total = getCallTotalBookings(careerId, key)
                total?.let { fetched -> _callTotals.update { it + (key to fetched) } }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Silent by design; see KDoc.
            } finally {
                totalsInFlight.remove(key)
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
                    val current = (_bookings.value as? Loadable.Loaded)?.value.orEmpty()
                    _bookings.value = Loadable.Loaded(current.filterNot { it.identityKey() == booking.identityKey() })
                    // awaitTurn: a refresh already in flight predates the cancellation, so its
                    // response still contains the dropped row; queueing (rather than tryLock's
                    // silent skip) guarantees the reconcile lands last with post-cancel data.
                    fetch(careerId, awaitTurn = true)
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

    /**
     * [awaitTurn] false (the default) collapses overlapping refreshes into one; true queues
     * behind an in-flight refresh instead, for callers whose fetch must not be skipped —
     * the post-cancellation reconcile, whose data supersedes any refresh already running.
     */
    private suspend fun fetch(careerId: CareerId, awaitTurn: Boolean = false) {
        if (awaitTurn) refreshMutex.lock() else if (!refreshMutex.tryLock()) return
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

/**
 * Stable identity for a booking across refreshes — used for list keys and for matching
 * the in-flight cancellation. applicationListId/studentId disambiguate two bookings
 * that share an exam-call key (rare, but possible across careers).
 */
internal fun BookedExam.identityKey(): String =
    "${key.courseOfStudyId}/${key.activityId}/${key.callId}/${applicationListId ?: studentId ?: 0}"

private fun ExamDocument.fileName(booking: BookedExam): String {
    val suffix = "${booking.key.activityId}_${booking.key.callId}"
    return when (this) {
        ExamDocument.BookingSlip -> "statino_prenotazione_$suffix.pdf"
        ExamDocument.PresenceCertificate -> "attestato_presenza_$suffix.pdf"
    }
}

/**
 * User-facing failure copy per document. Esse3 answers 422 for the presence certificate
 * until the outcome is published, so it gets a clear "not yet available" message rather
 * than the raw server error.
 */
private fun ExamDocument.errorMessage(cause: Throwable): String = when (this) {
    ExamDocument.BookingSlip -> "Impossibile scaricare lo statino di prenotazione. Riprova tra un momento."
    ExamDocument.PresenceCertificate -> "L'attestato di presenza non è ancora disponibile per questo appello."
}
