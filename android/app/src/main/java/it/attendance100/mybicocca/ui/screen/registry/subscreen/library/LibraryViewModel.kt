package it.attendance100.mybicocca.ui.screen.registry.subscreen.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.domain.model.library.Library
import it.attendance100.mybicocca.domain.model.library.LibraryActionKind
import it.attendance100.mybicocca.domain.model.library.LibraryAgreement
import it.attendance100.mybicocca.domain.model.library.LibraryBookingConstraints
import it.attendance100.mybicocca.domain.model.library.LibraryBookingDraft
import it.attendance100.mybicocca.domain.model.library.LibraryCampus
import it.attendance100.mybicocca.domain.model.library.LibraryCatalog
import it.attendance100.mybicocca.domain.model.library.LibraryDeepLinkAction
import it.attendance100.mybicocca.domain.model.library.LibraryLiveStatus
import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.domain.model.library.LibraryReservationState
import it.attendance100.mybicocca.domain.model.library.LibrarySeat
import it.attendance100.mybicocca.domain.model.library.LibraryService
import it.attendance100.mybicocca.domain.model.library.LibraryWeekHours
import it.attendance100.mybicocca.domain.model.library.LibraryWeekdayCrowding
import it.attendance100.mybicocca.domain.model.library.LibraryWeekdayCrowdingPage
import it.attendance100.mybicocca.domain.model.library.LibraryZone
import it.attendance100.mybicocca.domain.model.library.buildWeeklyCrowdingChart
import it.attendance100.mybicocca.domain.model.library.isBookableAt
import it.attendance100.mybicocca.domain.model.library.isEndedAt
import it.attendance100.mybicocca.domain.repository.LibraryNotLoggedInException
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.library.BookLibrarySeatUseCase
import it.attendance100.mybicocca.domain.usecase.library.CancelLibraryBookingByTokenUseCase
import it.attendance100.mybicocca.domain.usecase.library.CancelLibraryReservationUseCase
import it.attendance100.mybicocca.domain.usecase.library.ClearLibraryBookingDraftUseCase
import it.attendance100.mybicocca.domain.usecase.library.ConfirmLibraryEmailValidationUseCase
import it.attendance100.mybicocca.domain.usecase.library.ConsumeLibraryActionUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetAvailableSeatsUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibrariesUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibraryAgreementsUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibraryBookingConstraintsUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibraryLiveStatusUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibraryPendingStateUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibraryServicesUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibraryUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibraryWeekHoursUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibraryZonesUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetPreferredLibraryCampusUseCase
import it.attendance100.mybicocca.domain.usecase.library.LogoutLibraryUseCase
import it.attendance100.mybicocca.domain.usecase.library.ObserveLibraryCrowdingHistoryUseCase
import it.attendance100.mybicocca.domain.usecase.library.ObserveLibraryLinkedEmailUseCase
import it.attendance100.mybicocca.domain.usecase.library.ObserveLibraryReservationsUseCase
import it.attendance100.mybicocca.domain.usecase.library.ObservePendingLibraryActionUseCase
import it.attendance100.mybicocca.domain.usecase.library.RecordLastUsedLibraryUseCase
import it.attendance100.mybicocca.domain.usecase.library.RefreshLibraryReservationsUseCase
import it.attendance100.mybicocca.domain.usecase.library.RequestLibraryEmailValidationUseCase
import it.attendance100.mybicocca.domain.usecase.library.SaveLibraryBookingDraftUseCase
import it.attendance100.mybicocca.domain.usecase.library.VerifyLibraryPresenceUseCase
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryAddressChooserState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryBookingsTab
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryLoginFeedback
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryLoginPhase
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryPage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

/**
 * Single owner of the Biblioteca modal: public library browsing, preferred-campus sorting,
 * crowding history and charts, account login (email validation with process-death persistence),
 * server-synced upcoming and past bookings (cached in Room for offline), and the seat booking
 * wizard.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getLibraries: GetLibrariesUseCase,
    private val getLibrary: GetLibraryUseCase,
    private val getLibraryServices: GetLibraryServicesUseCase,
    private val getLiveStatus: GetLibraryLiveStatusUseCase,
    private val getWeekHours: GetLibraryWeekHoursUseCase,
    private val getZones: GetLibraryZonesUseCase,
    private val getAgreements: GetLibraryAgreementsUseCase,
    private val getConstraints: GetLibraryBookingConstraintsUseCase,
    private val getAvailableSeats: GetAvailableSeatsUseCase,
    private val bookSeat: BookLibrarySeatUseCase,
    observeReservations: ObserveLibraryReservationsUseCase,
    private val refreshReservations: RefreshLibraryReservationsUseCase,
    private val cancelReservation: CancelLibraryReservationUseCase,
    private val verifyPresenceUseCase: VerifyLibraryPresenceUseCase,
    observeLinkedEmail: ObserveLibraryLinkedEmailUseCase,
    private val requestEmailValidation: RequestLibraryEmailValidationUseCase,
    private val confirmEmailValidation: ConfirmLibraryEmailValidationUseCase,
    private val logout: LogoutLibraryUseCase,
    private val cancelBookingByToken: CancelLibraryBookingByTokenUseCase,
    private val observeActiveAccount: ObserveActiveAccountUseCase,
    observePendingLibraryAction: ObservePendingLibraryActionUseCase,
    consumeLibraryAction: ConsumeLibraryActionUseCase,
    private val getPendingState: GetLibraryPendingStateUseCase,
    private val saveBookingDraft: SaveLibraryBookingDraftUseCase,
    private val clearBookingDraft: ClearLibraryBookingDraftUseCase,
    private val getPreferredCampus: GetPreferredLibraryCampusUseCase,
    private val recordLastUsedLibrary: RecordLastUsedLibraryUseCase,
    private val observeCrowdingHistory: ObserveLibraryCrowdingHistoryUseCase,
) : ViewModel() {

    private val _backStack = MutableStateFlow<List<LibraryPage>>(listOf(LibraryPage.Home))
    val backStack: StateFlow<List<LibraryPage>> = _backStack.asStateFlow()

    val linkedEmail: StateFlow<String?> =
        observeLinkedEmail().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _preferredCampus = MutableStateFlow<LibraryCampus?>(null)
    val preferredCampus: StateFlow<LibraryCampus?> = _preferredCampus.asStateFlow()

    private val _rawLibraries = MutableStateFlow<Loadable<List<Library>>>(Loadable.NotYetLoaded)

    /** Bookable libraries ordered with [preferredCampus] first (Centrale first when null). */
    val libraries: StateFlow<Loadable<List<Library>>> =
        combine(_rawLibraries, _preferredCampus) { loadable, campus ->
            when (loadable) {
                is Loadable.Loaded -> Loadable.Loaded(LibraryCatalog.sortForCampus(loadable.value, campus))
                Loadable.NotYetLoaded -> Loadable.NotYetLoaded
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, Loadable.NotYetLoaded)

    private val _librariesStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val librariesStatus: StateFlow<SyncStatus> = _librariesStatus.asStateFlow()

    /** Active and upcoming non-cancelled bookings, earliest start first. */
    val reservations: StateFlow<Loadable<List<LibraryReservation>>> =
        observeReservations()
            .map<List<LibraryReservation>, Loadable<List<LibraryReservation>>> { list ->
                val now = LocalDateTime.now(AppClock.clock)
                Loadable.Loaded(
                    list.filter { it.state != LibraryReservationState.Cancelled && !it.isEndedAt(now) }
                        .sortedBy { it.start },
                )
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, Loadable.NotYetLoaded)

    /** Ended non-cancelled bookings ("Storico"), newest first, view-only. */
    val pastReservations: StateFlow<Loadable<List<LibraryReservation>>> =
        observeReservations()
            .map<List<LibraryReservation>, Loadable<List<LibraryReservation>>> { list ->
                val now = LocalDateTime.now(AppClock.clock)
                Loadable.Loaded(
                    list.filter { it.state != LibraryReservationState.Cancelled && it.isEndedAt(now) }
                        .sortedWith(
                            compareByDescending<LibraryReservation> { it.start }
                                .thenByDescending { it.end },
                        ),
                )
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, Loadable.NotYetLoaded)

    private val _bookingsTab = MutableStateFlow(LibraryBookingsTab.Upcoming)
    val bookingsTab: StateFlow<LibraryBookingsTab> = _bookingsTab.asStateFlow()

    /** Earliest-starting reservation that has not ended and is neither cancelled nor past. */
    val nextReservation: StateFlow<LibraryReservation?> =
        observeReservations()
            .map { list ->
                val now = LocalDateTime.now(AppClock.clock)
                list.filter {
                    it.state != LibraryReservationState.Cancelled && !it.isEndedAt(now)
                }.minByOrNull { it.start }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _cancellingId = MutableStateFlow<Int?>(null)
    val cancellingId: StateFlow<Int?> = _cancellingId.asStateFlow()

    private val _events = Channel<LibraryEvent>(Channel.BUFFERED)
    val events: Flow<LibraryEvent> = _events.receiveAsFlow()

    private val _openSheetRequests = Channel<Unit>(Channel.BUFFERED)
    val openSheetRequests: Flow<Unit> = _openSheetRequests.receiveAsFlow()

    /**
     * Pinned to the institutional (Esse3/Elearning) email — not user-editable — so bookings and
     * the account session always use the same address and therefore sync.
     */
    val institutionalEmail: StateFlow<String?> =
        observeActiveAccount().map { it?.username }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** The linked Affluences e-mail, falling back to the institutional address. */
    val bookingEmail: StateFlow<String?> =
        combine(linkedEmail, institutionalEmail) { linked, institutional ->
            linked?.trim()?.takeIf { it.isNotEmpty() }
                ?: institutional?.trim()?.takeIf { it.isNotEmpty() }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _loginPhase = MutableStateFlow(LibraryLoginPhase.EnterEmail)
    val loginPhase: StateFlow<LibraryLoginPhase> = _loginPhase.asStateFlow()

    private val _loginFeedback = MutableStateFlow<LibraryLoginFeedback?>(null)

    /** Transient in-content feedback shown over the login form after a verify attempt. */
    val loginFeedback: StateFlow<LibraryLoginFeedback?> = _loginFeedback.asStateFlow()

    private var requestUuid: String? = null
    private var pendingEmail: String? = null
    private var silentCheckJob: Job? = null
    private var saveDraftJob: Job? = null
    private var savingDraft = false
    private var restoring = false

    private var fetchLibrariesJob: Job? = null
    private var fetchLibrariesIncludesLive = false
    private var detailJob: Job? = null
    private val liveStatusDeferreds = mutableMapOf<String, Deferred<LibraryLiveStatus?>>()
    private val _liveStatusesByLibrary = MutableStateFlow<Map<String, LibraryLiveStatus>>(emptyMap())
    private val enrichedLibrariesById = mutableMapOf<String, Library>()

    private val _detailLibraryId = MutableStateFlow<String?>(null)

    private val _detailStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val detailStatus: StateFlow<SyncStatus> = _detailStatus.asStateFlow()

    private val _liveStatus = MutableStateFlow<Loadable<LibraryLiveStatus>>(Loadable.NotYetLoaded)
    val liveStatus: StateFlow<Loadable<LibraryLiveStatus>> = _liveStatus.asStateFlow()

    private val _weekHours = MutableStateFlow<Loadable<LibraryWeekHours>>(Loadable.NotYetLoaded)
    val weekHours: StateFlow<Loadable<LibraryWeekHours>> = _weekHours.asStateFlow()

    private val _siteServices = MutableStateFlow<List<LibraryService>>(emptyList())
    val siteServices: StateFlow<List<LibraryService>> = _siteServices.asStateFlow()

    /** Cached weekday crowding history for the currently viewed library detail. */
    val crowdingHistory: StateFlow<Map<DayOfWeek, LibraryWeekdayCrowding>> =
        _detailLibraryId
            .flatMapLatest { id ->
                if (id == null) flowOf(emptyMap()) else observeCrowdingHistory(id)
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    /** Seven Monday-to-Sunday crowding chart pages for the currently viewed library detail. */
    val crowdingWeek: StateFlow<List<LibraryWeekdayCrowdingPage>> =
        combine(_liveStatus, _weekHours, crowdingHistory) { live, hours, history ->
            buildWeeklyCrowdingChart(
                today = LocalDate.now(AppClock.clock),
                nowTime = LocalTime.now(AppClock.clock),
                liveStatus = live.valueOrNull(),
                weekHours = hours.valueOrNull(),
                history = history,
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _addressChooser = MutableStateFlow<LibraryAddressChooserState?>(null)
    val addressChooser: StateFlow<LibraryAddressChooserState?> = _addressChooser.asStateFlow()

    private val _bookingLibrary = MutableStateFlow<Library?>(null)
    val bookingLibrary: StateFlow<Library?> = _bookingLibrary.asStateFlow()

    private val _zones = MutableStateFlow<Loadable<List<LibraryZone>>>(Loadable.NotYetLoaded)
    val zones: StateFlow<Loadable<List<LibraryZone>>> = _zones.asStateFlow()

    private val _zonesStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val zonesStatus: StateFlow<SyncStatus> = _zonesStatus.asStateFlow()

    private val _agreements = MutableStateFlow<List<LibraryAgreement>>(emptyList())
    val agreements: StateFlow<List<LibraryAgreement>> = _agreements.asStateFlow()

    private val _selectedZone = MutableStateFlow<LibraryZone?>(null)
    val selectedZone: StateFlow<LibraryZone?> = _selectedZone.asStateFlow()

    private val _constraints = MutableStateFlow<Loadable<LibraryBookingConstraints>>(Loadable.NotYetLoaded)
    val constraints: StateFlow<Loadable<LibraryBookingConstraints>> = _constraints.asStateFlow()

    private val _constraintsStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val constraintsStatus: StateFlow<SyncStatus> = _constraintsStatus.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    private val _selectedDuration = MutableStateFlow<Int?>(null)
    val selectedDuration: StateFlow<Int?> = _selectedDuration.asStateFlow()

    private val _seats = MutableStateFlow<Loadable<List<LibrarySeat>>>(Loadable.NotYetLoaded)
    val seats: StateFlow<Loadable<List<LibrarySeat>>> = _seats.asStateFlow()

    private val _seatsStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val seatsStatus: StateFlow<SyncStatus> = _seatsStatus.asStateFlow()

    private val _selectedStartTime = MutableStateFlow<LocalTime?>(null)
    val selectedStartTime: StateFlow<LocalTime?> = _selectedStartTime.asStateFlow()

    private val _selectedSeat = MutableStateFlow<LibrarySeat?>(null)
    val selectedSeat: StateFlow<LibrarySeat?> = _selectedSeat.asStateFlow()

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    private val _consentAccepted = MutableStateFlow(false)
    val consentAccepted: StateFlow<Boolean> = _consentAccepted.asStateFlow()

    private val _submitting = MutableStateFlow(false)
    val submitting: StateFlow<Boolean> = _submitting.asStateFlow()

    val availableStartTimes: StateFlow<List<LocalTime>> = _seats
        .map { loadable ->
            loadable.valueOrNull().orEmpty()
                .flatMap { seat -> seat.slots.filter { it.placesBookable > 0 }.map { it.start } }
                .distinct()
                .sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val seatsAtSelectedTime: List<LibrarySeat>
        get() {
            val start = _selectedStartTime.value ?: return emptyList()
            return _seats.value.valueOrNull().orEmpty().filter { it.isBookableAt(start) }
        }

    val requiresConsent: Boolean get() = _agreements.value.any { it.mandatory }
    val mandatoryAgreement: LibraryAgreement? get() = _agreements.value.firstOrNull { it.mandatory }

    init {
        viewModelScope.launch { refreshPreferredCampus() }
        fetchLibraries(fetchLiveStatuses = false)
        viewModelScope.launch { syncIfLoggedIn() }
        viewModelScope.launch {
            observePendingLibraryAction().filterNotNull().collect { action ->
                consumeLibraryAction()
                _openSheetRequests.send(Unit)
                handleDeepLink(action)
            }
        }
        viewModelScope.launch {
            var previousEmail: String? = null
            observeLinkedEmail().collect { currentEmail ->
                if (previousEmail != null && currentEmail == null) {
                    val top = _backStack.value.lastOrNull()
                    if (
                        top is LibraryPage.Bookings ||
                        top is LibraryPage.Account ||
                        top is LibraryPage.ReservationDetail
                    ) {
                        _backStack.value = listOf(LibraryPage.Home)
                    }
                }
                previousEmail = currentEmail
            }
        }
    }

    /** Best-effort initial sync; signed-out state is never surfaced as an error. */
    private suspend fun syncIfLoggedIn() {
        runCatching { refreshReservations() }
            .onFailure { if (it !is LibraryNotLoggedInException) _events.send(LibraryEvent.SyncFailed(it)) }
    }

    /**
     * Entry point invoked each time the Biblioteca sheet opens: refreshes preferred campus,
     * fetches libraries together with live status for every library (reused across armchairs and
     * detail pages), syncs reservations when linked, and restores any interrupted booking flow.
     */
    fun onSheetOpened() {
        if (_backStack.value.size == 1) viewModelScope.launch { refreshPreferredCampus() }
        fetchLibraries(fetchLiveStatuses = true)
        syncReservationsOnOpen()
        restorePendingBooking()
    }

    /** Called when the sheet opens: syncs reservations when linked without erroring if signed out. */
    fun syncReservationsOnOpen() {
        if (linkedEmail.value != null) refresh()
    }

    private suspend fun refreshPreferredCampus() {
        _preferredCampus.value = runCatching { getPreferredCampus() }.getOrNull()
    }

    // The campus is only re-read when the sheet opens, so the list never reorders under the student.
    private fun rememberLastUsed(libraryId: String) {
        viewModelScope.launch { runCatching { recordLastUsedLibrary(libraryId) } }
    }

    private fun push(page: LibraryPage) { _backStack.value = _backStack.value + page }

    private fun cutBackStackTo(target: LibraryPage) {
        val index = _backStack.value.indexOfLast { it == target }
        if (index >= 0) _backStack.value = _backStack.value.take(index + 1)
    }

    fun back() {
        if (_submitting.value) return
        if (_addressChooser.value != null) {
            _addressChooser.value = null
            return
        }
        val top = _backStack.value.lastOrNull()
        if (_backStack.value.size > 1) {
            _backStack.value = _backStack.value.dropLast(1)
            if (top is LibraryPage.Login && top.fromBooking) {
                viewModelScope.launch { runCatching { clearBookingDraft() } }
            }
            if (top is LibraryPage.LibraryDetail) {
                _addressChooser.value = null
            }
        }
    }

    fun resetNavigation() {
        silentCheckJob?.cancel()
        silentCheckJob = null
        saveDraftJob?.cancel()
        saveDraftJob = null
        savingDraft = false
        detailJob?.cancel()
        detailJob = null
        clearSessionLiveCache()
        _backStack.value = listOf(LibraryPage.Home)
        _bookingsTab.value = LibraryBookingsTab.Upcoming
        _addressChooser.value = null
        _detailLibraryId.value = null
        clearBookingSession()
        _liveStatus.value = Loadable.NotYetLoaded
        _weekHours.value = Loadable.NotYetLoaded
        _siteServices.value = emptyList()
        _detailStatus.value = SyncStatus.Idle
        viewModelScope.launch { runCatching { clearBookingDraft() } }
    }

    fun openLibraries() {
        when (_backStack.value.lastOrNull()) {
            LibraryPage.Libraries -> Unit
            LibraryPage.Bookings -> _backStack.value = _backStack.value.dropLast(1) + LibraryPage.Libraries
            else -> push(LibraryPage.Libraries)
        }
    }

    fun openServices() = push(LibraryPage.Services)

    fun selectBookingsTab(tab: LibraryBookingsTab) {
        _bookingsTab.value = tab
    }

    fun openReservation(reservation: LibraryReservation) =
        push(LibraryPage.ReservationDetail(reservation.reservationId))

    fun openBookings() {
        if (linkedEmail.value == null) openLogin(fromBooking = false)
        else push(LibraryPage.Bookings)
    }

    fun openAccount() = push(LibraryPage.Account)

    fun openLibrary(library: Library) {
        _addressChooser.value = null
        _detailLibraryId.value = library.id
        push(LibraryPage.LibraryDetail(library.id))
        rememberLastUsed(library.id)
        loadDetail(library.id, forceRefresh = false)
    }

    fun openAddressChooser(library: Library) {
        val cleanedAddress = LibraryCatalog.cleanAddress(library.address) ?: return
        _addressChooser.value = LibraryAddressChooserState(
            address = cleanedAddress,
            buildingCode = LibraryCatalog.byId(library.id)?.mapBuildingCode,
        )
    }

    fun dismissAddressChooser() {
        _addressChooser.value = null
    }

    fun finishBooking() = resetNavigation()

    fun openLogin(fromBooking: Boolean = false) {
        silentCheckJob?.cancel()
        silentCheckJob = null
        _loginPhase.value = LibraryLoginPhase.EnterEmail
        _loginFeedback.value = null
        requestUuid = null
        pendingEmail = null
        val target = LibraryPage.Login(fromBooking)
        push(target)
        viewModelScope.launch {
            val email = institutionalEmail.value?.trim()?.takeIf { it.isNotEmpty() }
                ?: observeActiveAccount().firstOrNull()?.username?.trim()?.takeIf { it.isNotEmpty() }
                ?: return@launch
            val pending = runCatching { getPendingState.pendingValidation() }.getOrNull() ?: return@launch
            if (
                pending.email.equals(email, ignoreCase = true) &&
                _backStack.value.lastOrNull() == target &&
                _loginPhase.value == LibraryLoginPhase.EnterEmail
            ) {
                requestUuid = pending.requestUuid
                pendingEmail = pending.email
                _loginPhase.value = LibraryLoginPhase.AwaitingClick
            }
        }
    }

    fun sendLoginEmail() {
        val email = institutionalEmail.value?.trim().orEmpty()
        if (email.isBlank()) return
        if (_loginPhase.value == LibraryLoginPhase.Sending || _loginPhase.value == LibraryLoginPhase.Verifying) return
        val previousUuid = requestUuid
        _loginPhase.value = LibraryLoginPhase.Sending
        viewModelScope.launch {
            silentCheckJob?.cancelAndJoin()
            silentCheckJob = null
            if (_loginFeedback.value == LibraryLoginFeedback.LoggedIn) return@launch
            runCatching { requestEmailValidation(email) }.fold(
                onSuccess = { uuid ->
                    requestUuid = uuid
                    pendingEmail = email
                    _loginPhase.value = LibraryLoginPhase.AwaitingClick
                    _events.send(LibraryEvent.LoginEmailSent(email))
                },
                onFailure = {
                    _loginPhase.value = if (previousUuid != null) {
                        LibraryLoginPhase.AwaitingClick
                    } else {
                        LibraryLoginPhase.EnterEmail
                    }
                    _events.send(LibraryEvent.LoginRequestFailed(it))
                },
            )
        }
    }

    fun resendLoginEmail() {
        if (_loginPhase.value != LibraryLoginPhase.AwaitingClick) return
        sendLoginEmail()
    }

    /**
     * On success the login page shows its in-content feedback panel and a reservations refresh
     * starts, so the bookings are populated on return; navigation itself waits for
     * [dismissLoginFeedback].
     */
    fun verifyLogin() {
        val email = (pendingEmail ?: institutionalEmail.value)?.trim().orEmpty()
        val uuid = requestUuid ?: return
        if (email.isBlank()) return
        if (_loginPhase.value == LibraryLoginPhase.Verifying || _loginPhase.value == LibraryLoginPhase.Sending) return
        _loginPhase.value = LibraryLoginPhase.Verifying
        viewModelScope.launch {
            silentCheckJob?.cancelAndJoin()
            silentCheckJob = null
            if (_loginFeedback.value == LibraryLoginFeedback.LoggedIn) return@launch
            runCatching { confirmEmailValidation(email, uuid) }.fold(
                onSuccess = { validated ->
                    if (validated) {
                        _loginFeedback.value = LibraryLoginFeedback.LoggedIn
                        refresh()
                    } else {
                        _loginPhase.value = LibraryLoginPhase.AwaitingClick
                        _loginFeedback.value = LibraryLoginFeedback.LinkNotOpened
                    }
                },
                onFailure = {
                    _loginPhase.value = LibraryLoginPhase.AwaitingClick
                    _events.send(LibraryEvent.LoginFailed(it))
                },
            )
        }
    }

    /**
     * Polls validation once while Login is on top in [LibraryLoginPhase.AwaitingClick].
     * On success behaves like manual verification; otherwise leaves visible state untouched.
     */
    fun checkValidationSilently() {
        if (_backStack.value.lastOrNull() !is LibraryPage.Login) return
        if (_loginPhase.value != LibraryLoginPhase.AwaitingClick) return
        if (_loginFeedback.value != null) return
        if (silentCheckJob?.isActive == true) return
        val email = (pendingEmail ?: institutionalEmail.value)?.trim().orEmpty()
        val uuid = requestUuid ?: return
        if (email.isBlank()) return
        silentCheckJob = viewModelScope.launch {
            try {
                val validated = confirmEmailValidation(email, uuid)
                if (validated && _backStack.value.lastOrNull() is LibraryPage.Login) {
                    _loginFeedback.value = LibraryLoginFeedback.LoggedIn
                    refresh()
                }
            } catch (cause: CancellationException) {
                throw cause
            } catch (_: Throwable) {
                // Silent poll never flashes spinners, panels, or error outcomes.
            } finally {
                silentCheckJob = null
            }
        }
    }

    /**
     * Called by the login page once its in-content feedback animation has played: a warning
     * reverts to the form, a success pops the login page and re-checks seat availability if the
     * login started from Confirm.
     */
    fun dismissLoginFeedback() {
        val feedback = _loginFeedback.value ?: return
        _loginFeedback.value = null
        val loginPage = _backStack.value.lastOrNull() as? LibraryPage.Login ?: return
        if (feedback == LibraryLoginFeedback.LoggedIn) {
            _backStack.value = _backStack.value.dropLast(1)
            if (loginPage.fromBooking) {
                viewModelScope.launch { onReturnedFromBookingLogin() }
            }
        }
    }

    private suspend fun onReturnedFromBookingLogin() {
        runCatching { clearBookingDraft() }
        val library = _bookingLibrary.value ?: return
        val zone = _selectedZone.value ?: return
        val date = _selectedDate.value ?: return
        val duration = _selectedDuration.value ?: return
        val startTime = _selectedStartTime.value ?: return
        val selectedSeat = _selectedSeat.value ?: return

        _seatsStatus.value = SyncStatus.Refreshing
        runCatching {
            getAvailableSeats(library.id, zone.resourceTypeId, date, duration, startTime = null)
        }.fold(
            onSuccess = { seats ->
                if (_backStack.value.lastOrNull() != LibraryPage.Confirm) {
                    _seatsStatus.value = SyncStatus.Idle
                    return
                }
                _seats.value = Loadable.Loaded(seats)
                _seatsStatus.value = SyncStatus.Idle
                val refreshedSeat = seats.firstOrNull {
                    it.resourceId == selectedSeat.resourceId && it.isBookableAt(startTime)
                }
                when {
                    refreshedSeat != null -> _selectedSeat.value = refreshedSeat
                    seats.any { it.isBookableAt(startTime) } -> {
                        _selectedSeat.value = null
                        cutBackStackTo(LibraryPage.Seats)
                        _events.send(LibraryEvent.SeatNoLongerAvailable)
                    }
                    else -> {
                        _selectedSeat.value = null
                        _selectedStartTime.value = null
                        cutBackStackTo(LibraryPage.DateTime)
                        _events.send(LibraryEvent.SeatNoLongerAvailable)
                    }
                }
            },
            onFailure = {
                _seatsStatus.value = SyncStatus.Idle
            },
        )
    }

    /**
     * Rebuilds an interrupted booking-plus-login flow after process death when the sheet opens
     * on Home while signed out and both a valid draft and pending validation are stored.
     */
    fun restorePendingBooking() {
        if (restoring) return
        if (_backStack.value != listOf(LibraryPage.Home)) return
        if (linkedEmail.value != null) return
        restoring = true
        viewModelScope.launch {
            try {
                val draft = runCatching { getPendingState.bookingDraft() }.getOrNull() ?: return@launch
                val pending = runCatching { getPendingState.pendingValidation() }.getOrNull() ?: return@launch
                if (_backStack.value != listOf(LibraryPage.Home) || linkedEmail.value != null) return@launch

                val libraries = getLibraries()
                _rawLibraries.value = Loadable.Loaded(mergeCachedState(libraries))
                _librariesStatus.value = SyncStatus.Idle
                val library = libraries.firstOrNull { it.id == draft.libraryId }
                    ?: return@launch failRestore()

                val (zones, agreements) = coroutineScope {
                    val zonesDeferred = async { getZones(library.id) }
                    val agreementsDeferred = async { runCatching { getAgreements(library.id) }.getOrDefault(emptyList()) }
                    zonesDeferred.await() to agreementsDeferred.await()
                }
                val zone = zones.firstOrNull { it.resourceTypeId == draft.zoneId }
                    ?: return@launch failRestore()

                val (constraints, seats) = coroutineScope {
                    val constraintsDeferred = async {
                        getConstraints(library.id, zone.resourceTypeId, LocalDate.now(AppClock.clock))
                    }
                    val seatsDeferred = async {
                        getAvailableSeats(library.id, zone.resourceTypeId, draft.date, draft.durationMinutes, startTime = null)
                    }
                    constraintsDeferred.await() to seatsDeferred.await()
                }
                val seat = seats.firstOrNull { it.resourceId == draft.seatResourceId }
                    ?: return@launch failRestore()

                if (_backStack.value != listOf(LibraryPage.Home) || linkedEmail.value != null) return@launch

                _detailLibraryId.value = library.id
                _bookingLibrary.value = library
                _zones.value = Loadable.Loaded(zones)
                _zonesStatus.value = SyncStatus.Idle
                _agreements.value = agreements
                _selectedZone.value = zone
                _constraints.value = Loadable.Loaded(constraints)
                _constraintsStatus.value = SyncStatus.Idle
                _selectedDate.value = draft.date
                _selectedDuration.value = draft.durationMinutes
                _seats.value = Loadable.Loaded(seats)
                _seatsStatus.value = SyncStatus.Idle
                _selectedStartTime.value = draft.startTime
                _selectedSeat.value = seat
                _note.value = draft.note
                _consentAccepted.value = draft.consentAccepted
                requestUuid = pending.requestUuid
                pendingEmail = pending.email
                _loginFeedback.value = null
                _loginPhase.value = LibraryLoginPhase.AwaitingClick
                _backStack.value = listOf(
                    LibraryPage.Home,
                    LibraryPage.Libraries,
                    LibraryPage.LibraryDetail(library.id),
                    LibraryPage.Zones,
                    LibraryPage.DateTime,
                    LibraryPage.Seats,
                    LibraryPage.Confirm,
                    LibraryPage.Login(fromBooking = true),
                )
            } catch (cause: CancellationException) {
                throw cause
            } catch (_: Throwable) {
                failRestore()
            } finally {
                restoring = false
            }
        }
    }

    private suspend fun failRestore() {
        clearBookingSession()
        runCatching { clearBookingDraft() }
    }

    fun logoutAccount() {
        viewModelScope.launch {
            runCatching { logout() }
            if (_backStack.value.lastOrNull() is LibraryPage.Account) {
                _backStack.value = listOf(LibraryPage.Home)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.Refreshing
            runCatching { refreshReservations() }.fold(
                onSuccess = { _syncStatus.value = SyncStatus.Idle },
                onFailure = {
                    _syncStatus.value = if (it is LibraryNotLoggedInException) SyncStatus.Idle else SyncStatus.Failed(it)
                    if (it !is LibraryNotLoggedInException) _events.send(LibraryEvent.SyncFailed(it))
                },
            )
        }
    }

    private fun fetchLibraries(fetchLiveStatuses: Boolean) {
        if (fetchLibrariesJob?.isActive == true) {
            if (fetchLibrariesIncludesLive || !fetchLiveStatuses) return
            fetchLibrariesJob?.cancel()
        }
        fetchLibrariesIncludesLive = fetchLiveStatuses
        fetchLibrariesJob = viewModelScope.launch {
            _librariesStatus.value = SyncStatus.Refreshing
            runCatching { getLibraries() }.fold(
                onSuccess = { fetched ->
                    val merged = mergeCachedState(fetched)
                    _rawLibraries.value = Loadable.Loaded(merged)
                    _librariesStatus.value = SyncStatus.Idle
                    if (fetchLiveStatuses) {
                        prefetchLiveStatuses(merged)
                    }
                },
                onFailure = { cause ->
                    if (cause is CancellationException) throw cause
                    // A failed silent refresh keeps already-loaded libraries visible.
                    _librariesStatus.value = if (_rawLibraries.value is Loadable.Loaded) {
                        SyncStatus.Idle
                    } else {
                        SyncStatus.Failed(cause)
                    }
                },
            )
        }
    }

    private fun prefetchLiveStatuses(libraries: List<Library>) {
        for (library in libraries) {
            if (_liveStatusesByLibrary.value.containsKey(library.id)) continue
            if (liveStatusDeferreds[library.id]?.isActive == true) continue
            val deferred = viewModelScope.async {
                try {
                    val live = getLiveStatus(library.id)
                    cacheLiveStatus(library.id, live)
                    live
                } catch (cause: CancellationException) {
                    throw cause
                } catch (_: Throwable) {
                    null
                }
            }
            liveStatusDeferreds[library.id] = deferred
        }
    }

    private fun cacheLiveStatus(libraryId: String, live: LibraryLiveStatus) {
        _liveStatusesByLibrary.value = _liveStatusesByLibrary.value + (libraryId to live)
        val current = _rawLibraries.value as? Loadable.Loaded ?: return
        _rawLibraries.value = Loadable.Loaded(
            current.value.map { lib ->
                if (lib.id == libraryId) lib.copy(liveStatus = live) else lib
            },
        )
    }

    private fun mergeLibraryDetails(libraryId: String, detail: Library) {
        if (detail.id != libraryId) return
        enrichedLibrariesById[libraryId] = detail
        val current = _rawLibraries.value as? Loadable.Loaded ?: return
        _rawLibraries.value = Loadable.Loaded(
            current.value.map { existing ->
                if (existing.id != libraryId) existing else applyEnrichedDetails(existing, detail)
            },
        )
    }

    private fun mergeCachedState(fetched: List<Library>): List<Library> {
        val cachedLive = _liveStatusesByLibrary.value
        return fetched.map { lib ->
            val withDetails = enrichedLibrariesById[lib.id]?.let { applyEnrichedDetails(lib, it) } ?: lib
            cachedLive[lib.id]?.let { withDetails.copy(liveStatus = it) } ?: withDetails
        }
    }

    private fun applyEnrichedDetails(base: Library, detail: Library): Library = base.copy(
        secondaryName = detail.secondaryName?.takeIf { it.isNotBlank() } ?: base.secondaryName,
        address = detail.address?.takeIf { it.isNotBlank() } ?: base.address,
        latitude = detail.latitude ?: base.latitude,
        longitude = detail.longitude ?: base.longitude,
        pictureUrl = detail.pictureUrl?.takeIf { it.isNotBlank() } ?: base.pictureUrl,
        phone = detail.phone?.takeIf { it.isNotBlank() } ?: base.phone,
        email = detail.email?.takeIf { it.isNotBlank() } ?: base.email,
        websiteUrl = detail.websiteUrl?.takeIf { it.isNotBlank() } ?: base.websiteUrl,
    )

    private fun clearSessionLiveCache() {
        liveStatusDeferreds.values.forEach { it.cancel() }
        liveStatusDeferreds.clear()
        _liveStatusesByLibrary.value = emptyMap()
    }

    fun refreshLibraries() {
        clearSessionLiveCache()
        fetchLibraries(fetchLiveStatuses = true)
    }

    fun cancel(reservation: LibraryReservation) {
        if (_cancellingId.value != null) return
        _cancellingId.value = reservation.reservationId
        viewModelScope.launch {
            runCatching { cancelReservation(reservation) }.fold(
                onSuccess = { _events.send(LibraryEvent.ReservationCancelled) },
                onFailure = { _events.send(LibraryEvent.CancelFailed(it)) },
            )
            _cancellingId.value = null
        }
    }

    fun verifyPresence(code: String) {
        val trimmed = code.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            runCatching { verifyPresenceUseCase(trimmed) }.fold(
                onSuccess = { ok -> _events.send(if (ok) LibraryEvent.PresenceVerified else LibraryEvent.PresenceInvalidCode) },
                onFailure = { _events.send(LibraryEvent.PresenceFailed(it)) },
            )
        }
    }

    fun handleDeepLink(action: LibraryDeepLinkAction) {
        viewModelScope.launch {
            when (action.kind) {
                LibraryActionKind.Cancel -> runCatching { cancelBookingByToken(action.reservationToken) }.fold(
                    onSuccess = { _events.send(LibraryEvent.ReservationCancelled) },
                    onFailure = { _events.send(LibraryEvent.CancelFailed(it)) },
                )
            }
        }
    }

    fun retryDetail() {
        currentDetailLibraryId()?.let { loadDetail(it, forceRefresh = true) }
    }

    private fun currentDetailLibraryId(): String? =
        (_backStack.value.lastOrNull { it is LibraryPage.LibraryDetail } as? LibraryPage.LibraryDetail)?.libraryId

    private fun loadDetail(libraryId: String, forceRefresh: Boolean) {
        detailJob?.cancel()
        detailJob = viewModelScope.launch {
            _detailStatus.value = SyncStatus.Refreshing
            val cachedLive = if (forceRefresh) null else _liveStatusesByLibrary.value[libraryId]
            _liveStatus.value = if (cachedLive != null) Loadable.Loaded(cachedLive) else Loadable.NotYetLoaded
            _weekHours.value = Loadable.NotYetLoaded
            _siteServices.value = emptyList()

            try {
                coroutineScope {
                    val liveDeferred = async {
                        if (cachedLive != null) return@async cachedLive
                        if (!forceRefresh) {
                            liveStatusDeferreds[libraryId]?.await()?.let { return@async it }
                        }
                        val fresh = getLiveStatus(libraryId)
                        cacheLiveStatus(libraryId, fresh)
                        fresh
                    }
                    val hoursDeferred = async { getWeekHours(libraryId) }
                    val servicesDeferred = async {
                        runCatching { getLibraryServices(libraryId) }.getOrDefault(emptyList())
                    }
                    val siteDeferred = async {
                        runCatching { getLibrary(libraryId) }.getOrNull()
                    }

                    val live = liveDeferred.await()
                    _liveStatus.value = Loadable.Loaded(live)
                    val hours = hoursDeferred.await()
                    _weekHours.value = Loadable.Loaded(hours)
                    _siteServices.value = servicesDeferred.await()
                    siteDeferred.await()?.let { mergeLibraryDetails(libraryId, it) }
                    _detailStatus.value = SyncStatus.Idle
                }
            } catch (cause: CancellationException) {
                throw cause
            } catch (cause: Throwable) {
                _detailStatus.value = SyncStatus.Failed(cause)
            }
        }
    }

    fun startBooking(library: Library) {
        clearBookingSession()
        _bookingLibrary.value = library
        push(LibraryPage.Zones)
        loadZones(library.id)
        viewModelScope.launch { prefillFromAccount() }
    }

    fun retryZones() { _bookingLibrary.value?.let { loadZones(it.id) } }

    private fun loadZones(libraryId: String) {
        viewModelScope.launch {
            _zonesStatus.value = SyncStatus.Refreshing
            runCatching {
                coroutineScope {
                    val zonesDeferred = async { getZones(libraryId) }
                    val agreementsDeferred = async { runCatching { getAgreements(libraryId) }.getOrDefault(emptyList()) }
                    zonesDeferred.await() to agreementsDeferred.await()
                }
            }.fold(
                onSuccess = { (zones, agreements) ->
                    _zones.value = Loadable.Loaded(zones)
                    _agreements.value = agreements
                    _zonesStatus.value = SyncStatus.Idle
                },
                onFailure = { _zonesStatus.value = SyncStatus.Failed(it) },
            )
        }
    }

    fun selectZone(zone: LibraryZone) {
        _selectedZone.value = zone
        _selectedDate.value = null
        _selectedDuration.value = null
        _selectedStartTime.value = null
        _selectedSeat.value = null
        _seats.value = Loadable.NotYetLoaded
        push(LibraryPage.DateTime)
        loadConstraints(zone)
    }

    fun retryConstraints() { _selectedZone.value?.let(::loadConstraints) }

    private fun loadConstraints(zone: LibraryZone) {
        val library = _bookingLibrary.value ?: return
        viewModelScope.launch {
            _constraintsStatus.value = SyncStatus.Refreshing
            runCatching { getConstraints(library.id, zone.resourceTypeId, LocalDate.now(AppClock.clock)) }.fold(
                onSuccess = { constraints ->
                    _constraints.value = Loadable.Loaded(constraints)
                    _constraintsStatus.value = SyncStatus.Idle
                    if (_selectedDuration.value == null) _selectedDuration.value = constraints.durationsMinutes.firstOrNull()
                    if (_selectedDate.value == null) constraints.openDays.firstOrNull()?.let { _selectedDate.value = it }
                    maybeLoadSeats()
                },
                onFailure = { _constraintsStatus.value = SyncStatus.Failed(it) },
            )
        }
    }

    fun selectDate(date: LocalDate) {
        if (_selectedDate.value == date) return
        _selectedDate.value = date
        _selectedStartTime.value = null
        _selectedSeat.value = null
        maybeLoadSeats()
    }

    fun selectDuration(minutes: Int) {
        if (_selectedDuration.value == minutes) return
        _selectedDuration.value = minutes
        _selectedStartTime.value = null
        _selectedSeat.value = null
        maybeLoadSeats()
    }

    fun selectStartTime(time: LocalTime) {
        _selectedStartTime.value = time
        _selectedSeat.value = null
    }

    fun retrySeats() = maybeLoadSeats()

    private fun maybeLoadSeats() {
        val library = _bookingLibrary.value ?: return
        val zone = _selectedZone.value ?: return
        val date = _selectedDate.value ?: return
        val duration = _selectedDuration.value ?: return
        _seats.value = Loadable.NotYetLoaded
        _seatsStatus.value = SyncStatus.Refreshing
        viewModelScope.launch {
            runCatching {
                getAvailableSeats(library.id, zone.resourceTypeId, date, duration, startTime = null)
            }.fold(
                onSuccess = { seats ->
                    if (_selectedDate.value == date && _selectedDuration.value == duration) {
                        _seats.value = Loadable.Loaded(seats)
                        _seatsStatus.value = SyncStatus.Idle
                        val start = _selectedStartTime.value
                        if (start != null && seats.none { it.isBookableAt(start) }) _selectedStartTime.value = null
                    }
                },
                onFailure = {
                    if (_selectedDate.value == date && _selectedDuration.value == duration) {
                        _seatsStatus.value = SyncStatus.Failed(it)
                    }
                },
            )
        }
    }

    fun goToSeats() { if (_selectedStartTime.value != null) push(LibraryPage.Seats) }

    fun selectSeat(seat: LibrarySeat) {
        _selectedSeat.value = seat
        push(LibraryPage.Confirm)
    }

    fun autoSelectSeat() { seatsAtSelectedTime.firstOrNull()?.let(::selectSeat) }

    fun setNote(value: String) { _note.value = value }
    fun setConsent(value: Boolean) { _consentAccepted.value = value }

    fun submit() {
        if (_submitting.value) return
        val library = _bookingLibrary.value ?: return
        val zone = _selectedZone.value ?: return
        val seat = _selectedSeat.value ?: return
        val date = _selectedDate.value ?: return
        val startTime = _selectedStartTime.value ?: return
        val duration = _selectedDuration.value ?: return
        val email = bookingEmail.value?.trim().orEmpty()
        if (email.isBlank()) return

        if (linkedEmail.value == null) {
            saveDraftAndOpenLogin(library, zone, seat, date, startTime, duration)
            return
        }

        if (requiresConsent && !_consentAccepted.value) return

        _submitting.value = true
        viewModelScope.launch {
            runCatching {
                bookSeat(library, zone, seat, date, startTime, duration, email, _note.value.trim().takeIf { it.isNotBlank() })
            }.fold(
                onSuccess = {
                    runCatching { clearBookingDraft() }
                    rememberLastUsed(library.id)
                    push(LibraryPage.Done)
                },
                onFailure = { cause ->
                    if (cause is LibraryNotLoggedInException) {
                        saveDraftAndOpenLogin(library, zone, seat, date, startTime, duration)
                    } else {
                        _events.send(LibraryEvent.BookingFailed(cause))
                        maybeLoadSeats()
                    }
                },
            )
            _submitting.value = false
        }
    }

    private fun saveDraftAndOpenLogin(
        library: Library,
        zone: LibraryZone,
        seat: LibrarySeat,
        date: LocalDate,
        startTime: LocalTime,
        duration: Int,
    ) {
        if (savingDraft) return
        savingDraft = true
        val draft = LibraryBookingDraft(
            libraryId = library.id,
            zoneId = zone.resourceTypeId,
            date = date,
            durationMinutes = duration,
            startTime = startTime,
            seatResourceId = seat.resourceId,
            note = _note.value,
            consentAccepted = _consentAccepted.value,
            savedAt = Instant.now(AppClock.clock),
        )
        saveDraftJob = viewModelScope.launch {
            try {
                runCatching { saveBookingDraft(draft) }
                if (_backStack.value.lastOrNull() == LibraryPage.Confirm) {
                    openLogin(fromBooking = true)
                }
            } finally {
                savingDraft = false
                saveDraftJob = null
            }
        }
    }

    private suspend fun prefillFromAccount() {
        val account = observeActiveAccount().firstOrNull() ?: return
        if (_note.value.isBlank()) account.displayName.trim().takeIf { it.isNotEmpty() }?.let { _note.value = it }
    }

    private fun clearBookingSession() {
        _bookingLibrary.value = null
        _zones.value = Loadable.NotYetLoaded
        _zonesStatus.value = SyncStatus.Idle
        _agreements.value = emptyList()
        _selectedZone.value = null
        _constraints.value = Loadable.NotYetLoaded
        _constraintsStatus.value = SyncStatus.Idle
        _selectedDate.value = null
        _selectedDuration.value = null
        _seats.value = Loadable.NotYetLoaded
        _seatsStatus.value = SyncStatus.Idle
        _selectedStartTime.value = null
        _selectedSeat.value = null
        _note.value = ""
        _consentAccepted.value = false
        _submitting.value = false
    }
}
