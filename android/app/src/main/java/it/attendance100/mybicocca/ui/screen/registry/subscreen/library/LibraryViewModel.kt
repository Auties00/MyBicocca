package it.attendance100.mybicocca.ui.screen.registry.subscreen.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.data.deeplink.LibraryActionKind
import it.attendance100.mybicocca.data.deeplink.LibraryDeepLinkAction
import it.attendance100.mybicocca.data.deeplink.PendingLibraryAction
import it.attendance100.mybicocca.domain.model.library.Library
import it.attendance100.mybicocca.domain.model.library.LibraryAgreement
import it.attendance100.mybicocca.domain.model.library.LibraryBookingConstraints
import it.attendance100.mybicocca.domain.model.library.LibraryLiveStatus
import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.domain.model.library.LibrarySeat
import it.attendance100.mybicocca.domain.model.library.LibraryWeekHours
import it.attendance100.mybicocca.domain.model.library.LibraryZone
import it.attendance100.mybicocca.domain.model.library.isBookableAt
import it.attendance100.mybicocca.domain.repository.LibraryNotLoggedInException
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.library.BookLibrarySeatUseCase
import it.attendance100.mybicocca.domain.usecase.library.CancelLibraryBookingByTokenUseCase
import it.attendance100.mybicocca.domain.usecase.library.CancelLibraryReservationUseCase
import it.attendance100.mybicocca.domain.usecase.library.ConfirmLibraryEmailValidationUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetAvailableSeatsUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibrariesUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibraryAgreementsUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibraryBookingConstraintsUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibraryLiveStatusUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibraryWeekHoursUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibraryZonesUseCase
import it.attendance100.mybicocca.domain.usecase.library.LogoutLibraryUseCase
import it.attendance100.mybicocca.domain.usecase.library.ObserveLibraryLinkedEmailUseCase
import it.attendance100.mybicocca.domain.usecase.library.ObserveLibraryReservationsUseCase
import it.attendance100.mybicocca.domain.usecase.library.RefreshLibraryReservationsUseCase
import it.attendance100.mybicocca.domain.usecase.library.RequestLibraryEmailValidationUseCase
import it.attendance100.mybicocca.domain.usecase.library.VerifyLibraryPresenceUseCase
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryLoginPhase
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryPage
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

// Single owner of the Biblioteca modal: account login (email validation), the server-synced
// bookings list (cached in Room for offline), the library directory with live occupancy, and the
// per-seat booking wizard.
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getLibraries: GetLibrariesUseCase,
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
    private val pendingLibraryAction: PendingLibraryAction,
) : ViewModel() {

    private val _backStack = MutableStateFlow<List<LibraryPage>>(listOf(LibraryPage.Home))
    val backStack: StateFlow<List<LibraryPage>> = _backStack.asStateFlow()

    val linkedEmail: StateFlow<String?> =
        observeLinkedEmail().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _libraries = MutableStateFlow<Loadable<List<Library>>>(Loadable.NotYetLoaded)
    val libraries: StateFlow<Loadable<List<Library>>> = _libraries.asStateFlow()

    private val _librariesStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val librariesStatus: StateFlow<SyncStatus> = _librariesStatus.asStateFlow()

    val reservations: StateFlow<Loadable<List<LibraryReservation>>> =
        observeReservations()
            // Hide concluded bookings — only what's still upcoming or in progress is actionable.
            .map<List<LibraryReservation>, Loadable<List<LibraryReservation>>> { list ->
                val now = LocalDateTime.now()
                Loadable.Loaded(list.filter { it.end.isAfter(now) })
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Loadable.NotYetLoaded)

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _cancellingId = MutableStateFlow<Int?>(null)
    val cancellingId: StateFlow<Int?> = _cancellingId.asStateFlow()

    private val _events = Channel<LibraryEvent>(Channel.BUFFERED)
    val events: Flow<LibraryEvent> = _events.receiveAsFlow()

    private val _openSheetRequests = Channel<Unit>(Channel.BUFFERED)
    val openSheetRequests: Flow<Unit> = _openSheetRequests.receiveAsFlow()

    // --- Login (email validation) ---

    // Pinned to the institutional (Esse3/Elearning) email — not user-editable — so bookings and the
    // account session always use the same address and therefore sync.
    val institutionalEmail: StateFlow<String?> =
        observeActiveAccount().map { it?.username }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _loginPhase = MutableStateFlow(LibraryLoginPhase.EnterEmail)
    val loginPhase: StateFlow<LibraryLoginPhase> = _loginPhase.asStateFlow()

    private var requestUuid: String? = null

    // --- Library detail ---

    private val _detailStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val detailStatus: StateFlow<SyncStatus> = _detailStatus.asStateFlow()

    private val _liveStatus = MutableStateFlow<Loadable<LibraryLiveStatus>>(Loadable.NotYetLoaded)
    val liveStatus: StateFlow<Loadable<LibraryLiveStatus>> = _liveStatus.asStateFlow()

    private val _weekHours = MutableStateFlow<Loadable<LibraryWeekHours>>(Loadable.NotYetLoaded)
    val weekHours: StateFlow<Loadable<LibraryWeekHours>> = _weekHours.asStateFlow()

    // --- Booking session ---

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
        viewModelScope.launch { fetchLibraries() }
        viewModelScope.launch { syncIfLoggedIn() }
        viewModelScope.launch {
            pendingLibraryAction.pending.filterNotNull().collect { action ->
                pendingLibraryAction.consume()
                _openSheetRequests.send(Unit)
                handleDeepLink(action)
            }
        }
    }

    // Best-effort initial sync; the repo throws LibraryNotLoggedInException when there's no session.
    private suspend fun syncIfLoggedIn() {
        runCatching { refreshReservations() }
            .onFailure { if (it !is LibraryNotLoggedInException) _events.send(LibraryEvent.SyncFailed(it)) }
    }

    // --- Navigation ---

    private fun push(page: LibraryPage) { _backStack.value = _backStack.value + page }

    fun back() {
        if (_submitting.value) return
        if (_backStack.value.size > 1) _backStack.value = _backStack.value.dropLast(1)
    }

    fun resetNavigation() {
        _backStack.value = listOf(LibraryPage.Home)
        clearBookingSession()
        _liveStatus.value = Loadable.NotYetLoaded
        _weekHours.value = Loadable.NotYetLoaded
        _detailStatus.value = SyncStatus.Idle
    }

    fun openReservation(reservation: LibraryReservation) =
        push(LibraryPage.ReservationDetail(reservation.reservationId))

    fun openLibraries() = push(LibraryPage.Libraries)

    fun openLibrary(library: Library) {
        push(LibraryPage.LibraryDetail(library.id))
        loadDetail(library.id)
    }

    fun finishBooking() = resetNavigation()

    // --- Login ---

    fun openLogin() {
        _loginPhase.value = LibraryLoginPhase.EnterEmail
        requestUuid = null
        push(LibraryPage.Login)
    }

    fun sendLoginEmail() {
        val email = institutionalEmail.value?.trim().orEmpty()
        if (email.isBlank() || _loginPhase.value == LibraryLoginPhase.Sending) return
        _loginPhase.value = LibraryLoginPhase.Sending
        viewModelScope.launch {
            runCatching { requestEmailValidation(email) }.fold(
                onSuccess = { uuid ->
                    requestUuid = uuid
                    _loginPhase.value = LibraryLoginPhase.AwaitingClick
                    _events.send(LibraryEvent.LoginEmailSent(email))
                },
                onFailure = {
                    _loginPhase.value = LibraryLoginPhase.EnterEmail
                    _events.send(LibraryEvent.LoginRequestFailed(it))
                },
            )
        }
    }

    fun verifyLogin() {
        val email = institutionalEmail.value?.trim().orEmpty()
        val uuid = requestUuid ?: return
        if (email.isBlank()) return
        if (_loginPhase.value == LibraryLoginPhase.Verifying) return
        _loginPhase.value = LibraryLoginPhase.Verifying
        viewModelScope.launch {
            runCatching { confirmEmailValidation(email, uuid) }.fold(
                onSuccess = { validated ->
                    if (validated) {
                        _events.send(LibraryEvent.LoggedIn)
                        if (_backStack.value.lastOrNull() == LibraryPage.Login) back()
                    } else {
                        _loginPhase.value = LibraryLoginPhase.AwaitingClick
                        _events.send(LibraryEvent.LoginNotYetValidated)
                    }
                },
                onFailure = {
                    _loginPhase.value = LibraryLoginPhase.AwaitingClick
                    _events.send(LibraryEvent.LoginFailed(it))
                },
            )
        }
    }

    fun logoutAccount() {
        viewModelScope.launch { runCatching { logout() } }
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

    private suspend fun fetchLibraries() {
        _librariesStatus.value = SyncStatus.Refreshing
        runCatching { getLibraries() }.fold(
            onSuccess = { _libraries.value = Loadable.Loaded(it); _librariesStatus.value = SyncStatus.Idle },
            onFailure = { _librariesStatus.value = SyncStatus.Failed(it) },
        )
    }

    fun refreshLibraries() {
        viewModelScope.launch { fetchLibraries() }
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

    // --- Library detail ---

    fun retryDetail() {
        currentDetailLibraryId()?.let(::loadDetail)
    }

    private fun currentDetailLibraryId(): String? =
        (_backStack.value.lastOrNull { it is LibraryPage.LibraryDetail } as? LibraryPage.LibraryDetail)?.libraryId

    private fun loadDetail(libraryId: String) {
        viewModelScope.launch {
            _detailStatus.value = SyncStatus.Refreshing
            _liveStatus.value = Loadable.NotYetLoaded
            _weekHours.value = Loadable.NotYetLoaded
            runCatching {
                coroutineScope {
                    val live = async { getLiveStatus(libraryId) }
                    val hours = async { getWeekHours(libraryId) }
                    live.await() to hours.await()
                }
            }.fold(
                onSuccess = { (live, hours) ->
                    _liveStatus.value = Loadable.Loaded(live)
                    _weekHours.value = Loadable.Loaded(hours)
                    _detailStatus.value = SyncStatus.Idle
                },
                onFailure = { _detailStatus.value = SyncStatus.Failed(it) },
            )
        }
    }

    // --- Booking wizard ---

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
            runCatching { getConstraints(library.id, zone.resourceTypeId, LocalDate.now()) }.fold(
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
        val email = institutionalEmail.value?.trim().orEmpty()
        if (email.isBlank()) return
        if (requiresConsent && !_consentAccepted.value) return

        _submitting.value = true
        viewModelScope.launch {
            runCatching {
                bookSeat(library, zone, seat, date, startTime, duration, email, _note.value.trim().takeIf { it.isNotBlank() })
            }.fold(
                onSuccess = { push(LibraryPage.Done) },
                onFailure = { cause ->
                    _events.send(LibraryEvent.BookingFailed(cause))
                    maybeLoadSeats()
                },
            )
            _submitting.value = false
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
