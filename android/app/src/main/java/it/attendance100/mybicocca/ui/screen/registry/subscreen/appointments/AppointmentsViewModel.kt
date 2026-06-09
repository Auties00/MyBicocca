package it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.appointment.AppointmentForm
import it.attendance100.mybicocca.domain.model.appointment.AppointmentFormField
import it.attendance100.mybicocca.domain.model.appointment.AppointmentMonthAvailability
import it.attendance100.mybicocca.domain.model.appointment.AppointmentOffering
import it.attendance100.mybicocca.domain.model.appointment.AppointmentReservation
import it.attendance100.mybicocca.domain.model.appointment.AppointmentService
import it.attendance100.mybicocca.domain.model.appointment.AppointmentSlot
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.appointment.BookAppointmentUseCase
import it.attendance100.mybicocca.domain.usecase.appointment.CancelAppointmentReservationUseCase
import it.attendance100.mybicocca.domain.usecase.appointment.GetAppointmentDaySlotsUseCase
import it.attendance100.mybicocca.domain.usecase.appointment.GetAppointmentFormUseCase
import it.attendance100.mybicocca.domain.usecase.appointment.GetAppointmentMonthAvailabilityUseCase
import it.attendance100.mybicocca.domain.usecase.appointment.GetAppointmentOfferingsUseCase
import it.attendance100.mybicocca.domain.usecase.appointment.GetAppointmentPdfUseCase
import it.attendance100.mybicocca.domain.usecase.appointment.GetAppointmentServicesUseCase
import it.attendance100.mybicocca.domain.usecase.appointment.ObserveAppointmentReservationsUseCase
import it.attendance100.mybicocca.domain.usecase.appointment.RefreshAppointmentReservationsUseCase
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.state.AppointmentsEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.state.AppointmentsPage
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject

// Single owner of the whole Appuntamenti modal: the reservations list, the bookable-desk
// directory, the in-sheet navigation stack, and the per-service booking wizard (pick a slot
// from the live grid, fill the dynamic form, confirm). One submit performs the hold and the
// finalization, so abandoning earlier pages never leaves a dangling reservation.
@HiltViewModel
class AppointmentsViewModel @Inject constructor(
    private val getServices: GetAppointmentServicesUseCase,
    observeReservations: ObserveAppointmentReservationsUseCase,
    private val refreshReservations: RefreshAppointmentReservationsUseCase,
    private val cancelReservation: CancelAppointmentReservationUseCase,
    private val getReservationPdf: GetAppointmentPdfUseCase,
    private val getOfferings: GetAppointmentOfferingsUseCase,
    private val getMonthAvailability: GetAppointmentMonthAvailabilityUseCase,
    private val getDaySlots: GetAppointmentDaySlotsUseCase,
    private val getForm: GetAppointmentFormUseCase,
    private val bookAppointment: BookAppointmentUseCase,
    private val observeActiveAccount: ObserveActiveAccountUseCase,
) : ViewModel() {

    // In-sheet navigation. The current page is the stack's last entry; depth is its index.
    private val _backStack = MutableStateFlow<List<AppointmentsPage>>(listOf(AppointmentsPage.Reservations))
    val backStack: StateFlow<List<AppointmentsPage>> = _backStack.asStateFlow()

    private val _services = MutableStateFlow<Loadable<List<AppointmentService>>>(Loadable.NotYetLoaded)
    val services: StateFlow<Loadable<List<AppointmentService>>> = _services.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    // Bookings created from this device, hot from Room.
    val reservations: StateFlow<Loadable<List<AppointmentReservation>>> =
        observeReservations()
            .map<List<AppointmentReservation>, Loadable<List<AppointmentReservation>>> { Loadable.Loaded(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Loadable.NotYetLoaded)

    private val _cancellingCode = MutableStateFlow<String?>(null)
    val cancellingCode: StateFlow<String?> = _cancellingCode.asStateFlow()

    private val _downloadingPdfCode = MutableStateFlow<String?>(null)
    val downloadingPdfCode: StateFlow<String?> = _downloadingPdfCode.asStateFlow()

    private val _events = Channel<AppointmentsEvent>(Channel.BUFFERED)
    val events: Flow<AppointmentsEvent> = _events.receiveAsFlow()

    private val refreshMutex = Mutex()

    // --- Booking session: meaningful only once a service has been picked. ---

    private val _bookingService = MutableStateFlow<AppointmentService?>(null)
    val bookingService: StateFlow<AppointmentService?> = _bookingService.asStateFlow()

    private val _offerings = MutableStateFlow<Loadable<List<AppointmentOffering>>>(Loadable.NotYetLoaded)
    val offerings: StateFlow<Loadable<List<AppointmentOffering>>> = _offerings.asStateFlow()

    private val _form = MutableStateFlow<Loadable<AppointmentForm>>(Loadable.NotYetLoaded)
    val form: StateFlow<Loadable<AppointmentForm>> = _form.asStateFlow()

    private val _setupStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val setupStatus: StateFlow<SyncStatus> = _setupStatus.asStateFlow()

    private val _selectedAreaId = MutableStateFlow<Int?>(null)

    private val _month = MutableStateFlow(YearMonth.now())
    val month: StateFlow<YearMonth> = _month.asStateFlow()

    private val _monthAvailability =
        MutableStateFlow<Loadable<AppointmentMonthAvailability>>(Loadable.NotYetLoaded)
    val monthAvailability: StateFlow<Loadable<AppointmentMonthAvailability>> = _monthAvailability.asStateFlow()

    private val _availabilityStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val availabilityStatus: StateFlow<SyncStatus> = _availabilityStatus.asStateFlow()

    // True once the initial scan has walked the whole booking horizon without finding a single
    // open day: the slot picker swaps to a full "no dates" error page instead of an empty grid.
    private val _noAvailability = MutableStateFlow(false)
    val noAvailability: StateFlow<Boolean> = _noAvailability.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    private val _daySlots = MutableStateFlow<Loadable<List<AppointmentSlot>>>(Loadable.NotYetLoaded)
    val daySlots: StateFlow<Loadable<List<AppointmentSlot>>> = _daySlots.asStateFlow()

    private val _slotsStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val slotsStatus: StateFlow<SyncStatus> = _slotsStatus.asStateFlow()

    private val _selectedSlot = MutableStateFlow<AppointmentSlot?>(null)
    val selectedSlot: StateFlow<AppointmentSlot?> = _selectedSlot.asStateFlow()

    // Form values keyed by field code; consents store "true" when accepted.
    private val _values = MutableStateFlow<Map<String, String>>(emptyMap())
    val values: StateFlow<Map<String, String>> = _values.asStateFlow()

    private val _submitting = MutableStateFlow(false)
    val submitting: StateFlow<Boolean> = _submitting.asStateFlow()

    private val _bookedReservation = MutableStateFlow<AppointmentReservation?>(null)
    val bookedReservation: StateFlow<AppointmentReservation?> = _bookedReservation.asStateFlow()

    // The first required text field the autofill couldn't pre-populate: the form focuses it
    // and pops the keyboard so the user lands on the first thing they actually have to type.
    private val _autoFocusFieldCode = MutableStateFlow<String?>(null)
    val autoFocusFieldCode: StateFlow<String?> = _autoFocusFieldCode.asStateFlow()

    val selectedOffering: AppointmentOffering?
        get() = _offerings.value.valueOrNull()
            ?.let { list -> list.firstOrNull { it.area.id == _selectedAreaId.value } ?: list.firstOrNull() }

    private val minMonth: YearMonth = YearMonth.now()
    private val maxMonth: YearMonth
        get() {
            val horizon = selectedOffering?.horizonDays ?: DefaultHorizonDays
            return YearMonth.from(LocalDate.now().plusDays(horizon.toLong()))
        }

    val canGoToPreviousMonth: Boolean get() = _month.value > minMonth
    val canGoToNextMonth: Boolean get() = _month.value < maxMonth

    init {
        viewModelScope.launch { fetchServices() }
        // Best-effort: drop reservations cancelled elsewhere. Offline is fine — the local
        // snapshot stays.
        viewModelScope.launch { runCatching { refreshReservations() } }
    }

    // --- Navigation ---

    private fun push(page: AppointmentsPage) {
        _backStack.value = _backStack.value + page
    }

    fun back() {
        if (_submitting.value) return
        if (_backStack.value.size > 1) _backStack.value = _backStack.value.dropLast(1)
    }

    // Called when the sheet is dismissed: rewind to the root and forget the booking session
    // so a re-open starts clean.
    fun resetNavigation() {
        _backStack.value = listOf(AppointmentsPage.Reservations)
        clearBookingSession()
    }

    fun openReservation(reservation: AppointmentReservation) {
        push(AppointmentsPage.ReservationDetail(reservation.code))
    }

    fun openSections() = push(AppointmentsPage.Sections)

    fun openSection(sectionName: String) = push(AppointmentsPage.Types(sectionName))

    fun startBooking(service: AppointmentService) {
        clearBookingSession()
        _bookingService.value = service
        push(AppointmentsPage.Slots)
        loadSetup(service.id)
    }

    fun goToForm() {
        if (_selectedSlot.value != null) push(AppointmentsPage.Form)
    }

    fun finishBooking() = resetNavigation()

    private fun clearBookingSession() {
        _bookingService.value = null
        _offerings.value = Loadable.NotYetLoaded
        _form.value = Loadable.NotYetLoaded
        _setupStatus.value = SyncStatus.Idle
        _selectedAreaId.value = null
        _month.value = minMonth
        _monthAvailability.value = Loadable.NotYetLoaded
        _availabilityStatus.value = SyncStatus.Idle
        _noAvailability.value = false
        _selectedDate.value = null
        _daySlots.value = Loadable.NotYetLoaded
        _slotsStatus.value = SyncStatus.Idle
        _selectedSlot.value = null
        _values.value = emptyMap()
        _submitting.value = false
        _bookedReservation.value = null
        _autoFocusFieldCode.value = null
    }

    // --- Reservations list ---

    fun refresh() {
        viewModelScope.launch { fetchServices() }
    }

    fun cancel(reservation: AppointmentReservation) {
        if (_cancellingCode.value != null) return
        _cancellingCode.value = reservation.code
        viewModelScope.launch {
            runCatching { cancelReservation(reservation) }.fold(
                onSuccess = { _events.send(AppointmentsEvent.ReservationCancelled) },
                onFailure = { _events.send(AppointmentsEvent.CancelFailed(it)) },
            )
            _cancellingCode.value = null
        }
    }

    fun downloadPdf(reservation: AppointmentReservation) {
        val entryId = reservation.entryId ?: return
        if (_downloadingPdfCode.value != null) return
        _downloadingPdfCode.value = reservation.code
        viewModelScope.launch {
            runCatching { getReservationPdf(entryId) }.fold(
                onSuccess = { bytes ->
                    _events.send(
                        AppointmentsEvent.PdfReady(
                            fileName = "Appuntamento-${reservation.code}.pdf",
                            bytes = bytes,
                        )
                    )
                },
                onFailure = { _events.send(AppointmentsEvent.PdfFailed(it)) },
            )
            _downloadingPdfCode.value = null
        }
    }

    private suspend fun fetchServices() {
        if (!refreshMutex.tryLock()) return
        try {
            _syncStatus.value = SyncStatus.Refreshing
            runCatching { getServices() }.fold(
                onSuccess = { list ->
                    _services.value = Loadable.Loaded(list)
                    _syncStatus.value = SyncStatus.Idle
                },
                onFailure = { cause -> _syncStatus.value = SyncStatus.Failed(cause) },
            )
        } finally {
            refreshMutex.unlock()
        }
    }

    // --- Booking wizard ---

    fun retrySetup() {
        _bookingService.value?.let { loadSetup(it.id) }
    }

    fun goToPreviousMonth() = shiftMonth(-1)

    fun goToNextMonth() = shiftMonth(+1)

    fun retryMonth() {
        viewModelScope.launch { loadMonth(autoAdvance = false) }
    }

    fun selectDate(date: LocalDate) {
        if (_selectedDate.value == date) return
        _selectedDate.value = date
        _selectedSlot.value = null
        loadDaySlots(date)
    }

    fun retryDaySlots() {
        _selectedDate.value?.let(::loadDaySlots)
    }

    fun selectSlot(slot: AppointmentSlot) {
        _selectedSlot.value = slot
    }

    fun setFieldValue(code: String, value: String) {
        _values.value = _values.value + (code to value)
    }

    fun submit() {
        if (_submitting.value) return
        val service = _bookingService.value ?: return
        val offering = selectedOffering ?: return
        val form = _form.value.valueOrNull() ?: return
        val date = _selectedDate.value ?: return
        val slot = _selectedSlot.value ?: return
        _submitting.value = true
        viewModelScope.launch {
            runCatching {
                bookAppointment(
                    service = service,
                    offering = offering,
                    form = form,
                    slotStart = LocalDateTime.of(date, slot.start),
                    values = _values.value,
                )
            }.fold(
                onSuccess = { booked ->
                    _bookedReservation.value = booked
                    push(AppointmentsPage.Done)
                },
                onFailure = { cause ->
                    _events.send(AppointmentsEvent.BookingFailed(cause))
                    // The chosen slot may be the reason it failed: reload the day so the
                    // grid reflects what is still bookable.
                    _selectedDate.value?.let(::loadDaySlots)
                },
            )
            _submitting.value = false
        }
    }

    private fun loadSetup(serviceId: Int) {
        viewModelScope.launch {
            _setupStatus.value = SyncStatus.Refreshing
            runCatching {
                coroutineScope {
                    val offeringsDeferred = async { getOfferings(serviceId) }
                    val formDeferred = async { getForm(serviceId) }
                    offeringsDeferred.await() to formDeferred.await()
                }
            }.fold(
                onSuccess = { (offerings, form) ->
                    _offerings.value = Loadable.Loaded(offerings)
                    _form.value = Loadable.Loaded(form)
                    _setupStatus.value = SyncStatus.Idle
                    if (_selectedAreaId.value == null) {
                        _selectedAreaId.value = offerings.firstOrNull()?.area?.id
                    }
                    prefillFromAccount(form)
                    _autoFocusFieldCode.value = firstUnfilledRequiredFieldCode(form)
                    loadMonth(autoAdvance = true)
                },
                onFailure = { cause -> _setupStatus.value = SyncStatus.Failed(cause) },
            )
        }
    }

    // Pre-populate the form from what we already know about the student: the campus email (the
    // same address used on the booking portal), the full name / given name / surname, and the
    // fiscal code. Matching is by the server label since the portal field codes are opaque.
    private suspend fun prefillFromAccount(form: AppointmentForm) {
        val account = observeActiveAccount().firstOrNull() ?: return
        val email = account.username.takeIf { it.contains('@') }
        val fullName = account.displayName.trim().takeIf { it.isNotEmpty() }
        // displayName is "firstName lastName"; the last token is the surname, the rest the
        // given name(s). Imperfect for compound surnames, but a safe best-effort prefill.
        val surname = fullName?.substringAfterLast(' ', missingDelimiterValue = "")?.takeIf { it.isNotBlank() }
        val given = fullName?.substringBeforeLast(' ', missingDelimiterValue = "")?.takeIf { it.isNotBlank() }
        val fiscalCode = account.academic.fiscalCode?.trim()?.uppercase()?.takeIf { it.isNotEmpty() }

        val updates = buildMap {
            for (field in form.fields) {
                if (_values.value.containsKey(field.code) || !field.isTextLike) continue
                val label = field.label.lowercase()
                val value = when {
                    field is AppointmentFormField.Email -> email
                    label.contains("nominativ") || label.contains("nome completo") ||
                        (label.contains("nome") && label.contains("cognome")) -> fullName
                    label.contains("cognome") -> surname
                    label.contains("nome") -> given
                    label.contains("codice fiscale") || label.contains("cod. fiscale") || label == "cf" -> fiscalCode
                    label.contains("mail") -> email
                    else -> null
                }
                value?.let { put(field.code, it) }
            }
        }
        if (updates.isNotEmpty()) _values.value = _values.value + updates
    }

    private fun firstUnfilledRequiredFieldCode(form: AppointmentForm): String? =
        form.fields.firstOrNull { it.isTextLike && it.required && _values.value[it.code].isNullOrBlank() }?.code

    private fun shiftMonth(delta: Long) {
        val next = _month.value.plusMonths(delta)
        if (next < minMonth || next > maxMonth) return
        _month.value = next
        _selectedDate.value = null
        _selectedSlot.value = null
        _daySlots.value = Loadable.NotYetLoaded
        viewModelScope.launch { loadMonth(autoAdvance = false) }
    }

    private suspend fun loadMonth(autoAdvance: Boolean) {
        val service = _bookingService.value ?: return
        val offering = selectedOffering ?: return
        _availabilityStatus.value = SyncStatus.Refreshing
        _monthAvailability.value = Loadable.NotYetLoaded
        _noAvailability.value = false
        while (true) {
            val month = _month.value
            val result = runCatching {
                getMonthAvailability(
                    serviceId = service.id,
                    areaId = offering.area.id,
                    month = month,
                    durationSeconds = offering.durationSeconds,
                )
            }.getOrElse { cause ->
                _availabilityStatus.value = SyncStatus.Failed(cause)
                return
            }
            // The current month is often already past its horizon; hop forward to the
            // first month that actually has days instead of showing an empty grid.
            if (autoAdvance && result.availableDays.isEmpty() && month < maxMonth) {
                _month.value = month.plusMonths(1)
                continue
            }
            _monthAvailability.value = Loadable.Loaded(result)
            _availabilityStatus.value = SyncStatus.Idle
            // The autoAdvance loop only reaches here empty after exhausting the whole horizon.
            _noAvailability.value = autoAdvance && result.availableDays.isEmpty()
            if (_selectedDate.value == null) {
                result.availableDays.firstOrNull()?.let(::selectDate)
            }
            return
        }
    }

    private fun loadDaySlots(date: LocalDate) {
        val service = _bookingService.value ?: return
        val offering = selectedOffering ?: return
        _daySlots.value = Loadable.NotYetLoaded
        _slotsStatus.value = SyncStatus.Refreshing
        viewModelScope.launch {
            runCatching {
                getDaySlots(
                    serviceId = service.id,
                    areaId = offering.area.id,
                    date = date,
                    durationSeconds = offering.durationSeconds,
                )
            }.fold(
                onSuccess = { slots ->
                    // Ignore stale responses after the user moved to another day.
                    if (_selectedDate.value == date) {
                        _daySlots.value = Loadable.Loaded(slots)
                        _slotsStatus.value = SyncStatus.Idle
                    }
                },
                onFailure = { cause ->
                    if (_selectedDate.value == date) {
                        _slotsStatus.value = SyncStatus.Failed(cause)
                    }
                },
            )
        }
    }

    private companion object {
        const val DefaultHorizonDays = 60
    }
}

// Free-text inputs the autofill can write into and the keyboard can focus (Select/Consent
// are excluded).
private val AppointmentFormField.isTextLike: Boolean
    get() = this is AppointmentFormField.Email ||
        this is AppointmentFormField.Text ||
        this is AppointmentFormField.Phone ||
        this is AppointmentFormField.TextArea
