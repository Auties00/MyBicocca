package it.attendance100.mybicocca.ui.screen.registry.subscreen.library

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.model.library.Library
import it.attendance100.mybicocca.domain.model.library.LibraryActionKind
import it.attendance100.mybicocca.domain.model.library.LibraryAgreement
import it.attendance100.mybicocca.domain.model.library.LibraryBookingConstraints
import it.attendance100.mybicocca.domain.model.library.LibraryBookingDraft
import it.attendance100.mybicocca.domain.model.library.LibraryCampus
import it.attendance100.mybicocca.domain.model.library.LibraryCatalog
import it.attendance100.mybicocca.domain.model.library.LibraryCrowdingSlot
import it.attendance100.mybicocca.domain.model.library.LibraryDayCrowdingState
import it.attendance100.mybicocca.domain.model.library.LibraryDeepLinkAction
import it.attendance100.mybicocca.domain.model.library.LibraryLiveStatus
import it.attendance100.mybicocca.domain.model.library.LibraryPendingValidation
import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.domain.model.library.LibraryReservationState
import it.attendance100.mybicocca.domain.model.library.LibrarySeat
import it.attendance100.mybicocca.domain.model.library.LibraryService
import it.attendance100.mybicocca.domain.model.library.LibraryServiceType
import it.attendance100.mybicocca.domain.model.library.LibrarySlot
import it.attendance100.mybicocca.domain.model.library.LibraryWeekdayCrowding
import it.attendance100.mybicocca.domain.model.library.LibraryZone
import it.attendance100.mybicocca.domain.model.library.LibraryZoneColor
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
import it.attendance100.mybicocca.testing.MainDispatcherRule
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.emailInitials
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryBookingsTab
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryLoginFeedback
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryLoginPhase
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.depth
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * State-machine coverage for the Biblioteca modal owner: public-first home directory, in-sheet
 * back-stack navigation, next-reservation selection, email-validation login (including silent
 * polling, resend, and post-verification availability checks), draft persistence & process-death
 * restore, logout and expired-session handling, cancel/presence/deep-link operations.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getLibraries: GetLibrariesUseCase = mockk()
    private val getLibrary: GetLibraryUseCase = mockk(relaxed = true)
    private val getLibraryServices: GetLibraryServicesUseCase = mockk(relaxed = true)
    private val getLiveStatus: GetLibraryLiveStatusUseCase = mockk(relaxed = true)
    private val getWeekHours: GetLibraryWeekHoursUseCase = mockk(relaxed = true)
    private val getZones: GetLibraryZonesUseCase = mockk(relaxed = true)
    private val getAgreements: GetLibraryAgreementsUseCase = mockk(relaxed = true)
    private val getConstraints: GetLibraryBookingConstraintsUseCase = mockk(relaxed = true)
    private val getAvailableSeats: GetAvailableSeatsUseCase = mockk(relaxed = true)
    private val bookSeat: BookLibrarySeatUseCase = mockk(relaxed = true)
    private val observeReservations: ObserveLibraryReservationsUseCase = mockk()
    private val refreshReservations: RefreshLibraryReservationsUseCase = mockk(relaxed = true)
    private val cancelReservation: CancelLibraryReservationUseCase = mockk(relaxed = true)
    private val verifyPresenceUseCase: VerifyLibraryPresenceUseCase = mockk()
    private val observeLinkedEmail: ObserveLibraryLinkedEmailUseCase = mockk()
    private val requestEmailValidation: RequestLibraryEmailValidationUseCase = mockk()
    private val confirmEmailValidation: ConfirmLibraryEmailValidationUseCase = mockk()
    private val logout: LogoutLibraryUseCase = mockk(relaxed = true)
    private val cancelBookingByToken: CancelLibraryBookingByTokenUseCase = mockk(relaxed = true)
    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()
    private val observePendingLibraryAction: ObservePendingLibraryActionUseCase = mockk()
    private val consumeLibraryAction: ConsumeLibraryActionUseCase = mockk(relaxed = true)
    private val getPendingState: GetLibraryPendingStateUseCase = mockk()
    private val saveBookingDraft: SaveLibraryBookingDraftUseCase = mockk(relaxed = true)
    private val clearBookingDraft: ClearLibraryBookingDraftUseCase = mockk(relaxed = true)
    private val getPreferredCampus: GetPreferredLibraryCampusUseCase = mockk()
    private val recordLastUsedLibrary: RecordLastUsedLibraryUseCase = mockk(relaxed = true)
    private val observeCrowdingHistory: ObserveLibraryCrowdingHistoryUseCase = mockk()

    private val reservationsFlow = MutableStateFlow<List<LibraryReservation>>(emptyList())
    private val linkedEmailFlow = MutableStateFlow<String?>(null)
    private val crowdingHistoryFlow = MutableStateFlow<Map<DayOfWeek, LibraryWeekdayCrowding>>(emptyMap())

    private val fixedInstant = Instant.parse("2026-06-15T10:00:00Z")
    private val fixedDate = LocalDate.of(2026, 6, 15)
    private val tomorrow = LocalDate.of(2026, 6, 16)

    @Before
    fun setUp() {
        AppClock.clock = Clock.fixed(fixedInstant, ZoneOffset.UTC)
        coEvery { getPendingState.pendingValidation() } returns null
        coEvery { getPendingState.bookingDraft() } returns null
        coEvery { getPreferredCampus() } returns null
        coEvery { getLibraryServices(any()) } returns emptyList()
        every { observeCrowdingHistory(any()) } returns crowdingHistoryFlow
    }

    @After
    fun tearDown() {
        AppClock.clock = Clock.systemDefaultZone()
    }

    private fun reservation(
        id: Int,
        start: LocalDateTime = LocalDateTime.of(2026, 6, 16, 10, 0),
        end: LocalDateTime = start.plusHours(2),
        state: LibraryReservationState = LibraryReservationState.Upcoming,
    ) = LibraryReservation(
        reservationId = id,
        libraryName = "Biblioteca Centrale",
        librarySecondaryName = null,
        seatName = "A12",
        start = start,
        end = end,
        note = null,
        reservationCode = "R$id",
        cancellationToken = "tok-$id",
        state = state,
    )

    private fun library(id: String = "lib-1") = Library(
        id = id,
        slug = "centrale",
        name = "Biblioteca Centrale",
        secondaryName = null,
        address = null,
        latitude = null,
        longitude = null,
        pictureUrl = null,
        phone = null,
        email = null,
        websiteUrl = null,
        bookable = true,
        liveStatus = null,
    )

    private fun zone(id: Int = 1) = LibraryZone(
        resourceTypeId = id,
        name = "Sala lettura",
        subdescription = null,
        imageUrl = null,
        color = LibraryZoneColor.Verde,
    )

    private fun seat(
        id: Int,
        start: LocalTime = LocalTime.of(14, 0),
        placesBookable: Int = 1,
    ) = LibrarySeat(
        resourceId = id,
        name = "Posto $id",
        shortName = "$id",
        hasPowerOutlet = true,
        description = null,
        noteRequired = false,
        noteLabel = null,
        slots = listOf(LibrarySlot(start, 120, placesBookable)),
    )

    private fun viewModel(
        account: Account? = null,
        linkedEmail: String? = null,
        libraries: List<Library> = emptyList(),
        pendingAction: MutableStateFlow<LibraryDeepLinkAction?> = MutableStateFlow(null),
    ): LibraryViewModel {
        linkedEmailFlow.value = linkedEmail
        coEvery { getLibraries() } returns libraries
        every { observeReservations() } returns reservationsFlow
        coEvery { refreshReservations() } returns Unit
        every { observeLinkedEmail() } returns linkedEmailFlow
        every { observeActiveAccount() } returns flowOf(account)
        every { observePendingLibraryAction() } returns pendingAction
        return LibraryViewModel(
            getLibraries, getLibrary, getLibraryServices, getLiveStatus, getWeekHours, getZones,
            getAgreements, getConstraints, getAvailableSeats, bookSeat, observeReservations,
            refreshReservations, cancelReservation, verifyPresenceUseCase, observeLinkedEmail,
            requestEmailValidation, confirmEmailValidation, logout, cancelBookingByToken,
            observeActiveAccount, observePendingLibraryAction, consumeLibraryAction,
            getPendingState, saveBookingDraft, clearBookingDraft, getPreferredCampus,
            recordLastUsedLibrary, observeCrowdingHistory,
        )
    }

    private fun advanceToConfirm(
        vm: LibraryViewModel,
        lib: Library = library(),
        selectedZone: LibraryZone = zone(1),
        selectedSeat: LibrarySeat = seat(10, LocalTime.of(14, 0)),
        otherSeats: List<LibrarySeat> = emptyList(),
        agreements: List<LibraryAgreement> = emptyList(),
    ) {
        coEvery { getZones(lib.id) } returns listOf(selectedZone)
        coEvery { getAgreements(lib.id) } returns agreements
        coEvery { getConstraints(lib.id, selectedZone.resourceTypeId, any()) } returns LibraryBookingConstraints(
            openDays = listOf(tomorrow),
            openHours = listOf(LocalTime.of(14, 0)),
            durationsMinutes = listOf(120),
        )
        coEvery { getAvailableSeats(lib.id, selectedZone.resourceTypeId, tomorrow, 120, null) } returns
            listOf(selectedSeat) + otherSeats

        vm.openLibrary(lib)
        vm.startBooking(lib)
        vm.selectZone(selectedZone)
        vm.selectStartTime(LocalTime.of(14, 0))
        vm.goToSeats()
        vm.selectSeat(selectedSeat)
    }

    @Test
    fun `the back stack starts at Home`() = runTest {
        val vm = viewModel()

        assertThat(vm.backStack.value).containsExactly(LibraryPage.Home)
    }

    @Test
    fun `signed-out home exposes loaded libraries without errors`() = runTest {
        coEvery { refreshReservations() } throws LibraryNotLoggedInException()
        val vm = viewModel(linkedEmail = null, libraries = listOf(library()))

        assertThat(vm.linkedEmail.value).isNull()
        assertThat((vm.libraries.value as Loadable.Loaded).value).hasSize(1)
        assertThat(vm.librariesStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `failed silent refreshLibraries keeps already loaded libraries visible`() = runTest {
        val vm = viewModel(libraries = listOf(library("lib-1")))
        coEvery { getLibraries() } throws IOException("offline")

        vm.refreshLibraries()

        assertThat((vm.libraries.value as Loadable.Loaded).value.map { it.id }).containsExactly("lib-1")
        assertThat(vm.librariesStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `concluded reservations are filtered out of the observe stream`() = runTest {
        reservationsFlow.value = listOf(
            reservation(1, start = LocalDateTime.of(2026, 6, 16, 10, 0)),
            reservation(2, start = LocalDateTime.of(2026, 6, 14, 8, 0), end = LocalDateTime.of(2026, 6, 14, 10, 0)),
        )
        val vm = viewModel()

        val loaded = vm.reservations.value as Loadable.Loaded
        assertThat(loaded.value.map { it.reservationId }).containsExactly(1)
    }

    @Test
    fun `nextReservation picks earliest-starting non-ended non-cancelled non-past booking`() = runTest {
        val now = LocalDateTime.now(AppClock.clock)
        reservationsFlow.value = listOf(
            reservation(1, start = now.minusHours(3), end = now.minusHours(1), state = LibraryReservationState.Upcoming),
            reservation(2, start = now.plusHours(1), end = now.plusHours(3), state = LibraryReservationState.Cancelled),
            reservation(3, start = now.plusHours(2), end = now.plusHours(4), state = LibraryReservationState.Past),
            reservation(4, start = now.plusHours(5), end = now.plusHours(7), state = LibraryReservationState.Upcoming),
            reservation(5, start = now.minusMinutes(30), end = now.plusMinutes(30), state = LibraryReservationState.Ongoing),
            reservation(6, start = now.plusHours(3), end = now.plusHours(4), state = LibraryReservationState.AwaitingConfirmation),
        )
        val vm = viewModel()

        assertThat(vm.nextReservation.value?.reservationId).isEqualTo(5)
    }

    @Test
    fun `openBookings while signed out opens Login instead`() = runTest {
        val vm = viewModel(linkedEmail = null)

        vm.openBookings()

        assertThat(vm.backStack.value).containsExactly(LibraryPage.Home, LibraryPage.Login(fromBooking = false)).inOrder()
    }

    @Test
    fun `openBookings while signed in opens Bookings`() = runTest {
        val vm = viewModel(linkedEmail = "mario.rossi@campus.unimib.it")

        vm.openBookings()

        assertThat(vm.backStack.value).containsExactly(LibraryPage.Home, LibraryPage.Bookings).inOrder()
    }

    @Test
    fun `openLibrary pushes LibraryDetail directly from Home`() = runTest {
        val vm = viewModel()

        vm.openLibrary(library())

        assertThat(vm.backStack.value).containsExactly(LibraryPage.Home, LibraryPage.LibraryDetail("lib-1")).inOrder()
    }

    @Test
    fun `back pops one page and stops at Home`() = runTest {
        val vm = viewModel()
        vm.openLibrary(library())

        vm.back()
        vm.back()

        assertThat(vm.backStack.value).containsExactly(LibraryPage.Home)
    }

    @Test
    fun `openLogin starts in EnterEmail when no pending validation exists`() = runTest {
        val vm = viewModel(account = account())

        vm.openLogin()

        assertThat(vm.loginPhase.value).isEqualTo(LibraryLoginPhase.EnterEmail)
        assertThat(vm.backStack.value.last()).isEqualTo(LibraryPage.Login(fromBooking = false))
    }

    @Test
    fun `openLogin resumes AwaitingClick when a valid pending validation matches current email`() = runTest {
        coEvery { getPendingState.pendingValidation() } returns LibraryPendingValidation(
            email = "mario.rossi@campus.unimib.it",
            requestUuid = "persisted-uuid",
            createdAt = fixedInstant.minusSeconds(120),
        )
        coEvery { confirmEmailValidation("mario.rossi@campus.unimib.it", "persisted-uuid") } returns true
        val vm = viewModel(account = account())

        vm.openLogin(fromBooking = false)

        assertThat(vm.loginPhase.value).isEqualTo(LibraryLoginPhase.AwaitingClick)
        vm.verifyLogin()
        assertThat(vm.loginFeedback.value).isEqualTo(LibraryLoginFeedback.LoggedIn)
    }

    @Test
    fun `sendLoginEmail moves to AwaitingClick and emits LoginEmailSent`() = runTest {
        coEvery { requestEmailValidation("mario.rossi@campus.unimib.it") } returns "uuid-1"
        val vm = viewModel(account = account())

        vm.events.test {
            vm.sendLoginEmail()
            assertThat(awaitItem()).isInstanceOf(LibraryEvent.LoginEmailSent::class.java)
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(vm.loginPhase.value).isEqualTo(LibraryLoginPhase.AwaitingClick)
    }

    @Test
    fun `resendLoginEmail requests a new link while in AwaitingClick`() = runTest {
        coEvery { requestEmailValidation("mario.rossi@campus.unimib.it") } returnsMany listOf("uuid-1", "uuid-2")
        coEvery { confirmEmailValidation("mario.rossi@campus.unimib.it", "uuid-2") } returns true
        val vm = viewModel(account = account())

        vm.sendLoginEmail()
        vm.resendLoginEmail()
        vm.verifyLogin()

        coVerify(exactly = 2) { requestEmailValidation("mario.rossi@campus.unimib.it") }
        assertThat(vm.loginFeedback.value).isEqualTo(LibraryLoginFeedback.LoggedIn)
    }

    @Test
    fun `a validated link shows LoggedIn feedback`() = runTest {
        coEvery { requestEmailValidation(any()) } returns "uuid-1"
        coEvery { confirmEmailValidation("mario.rossi@campus.unimib.it", "uuid-1") } returns true
        val vm = viewModel(account = account())

        vm.sendLoginEmail()
        vm.verifyLogin()

        assertThat(vm.loginFeedback.value).isEqualTo(LibraryLoginFeedback.LoggedIn)
    }

    @Test
    fun `an unopened link reverts to AwaitingClick with LinkNotOpened feedback`() = runTest {
        coEvery { requestEmailValidation(any()) } returns "uuid-1"
        coEvery { confirmEmailValidation(any(), "uuid-1") } returns false
        val vm = viewModel(account = account())

        vm.sendLoginEmail()
        vm.verifyLogin()

        assertThat(vm.loginPhase.value).isEqualTo(LibraryLoginPhase.AwaitingClick)
        assertThat(vm.loginFeedback.value).isEqualTo(LibraryLoginFeedback.LinkNotOpened)
    }

    @Test
    fun `submit while signed out saves draft, opens Login fromBooking, and does not book`() = runTest {
        val vm = viewModel(account = account(), linkedEmail = null, libraries = listOf(library()))
        advanceToConfirm(
            vm = vm,
            agreements = listOf(LibraryAgreement(1, "Regolamento", null, mandatory = true)),
        )
        vm.setNote("Mario Rossi")
        vm.setConsent(false)

        val savedDraft = slot<LibraryBookingDraft>()
        coEvery { saveBookingDraft(capture(savedDraft)) } returns Unit

        vm.submit()

        assertThat(vm.backStack.value.last()).isEqualTo(LibraryPage.Login(fromBooking = true))
        coVerify(exactly = 0) { bookSeat(any(), any(), any(), any(), any(), any(), any(), any()) }
        coVerify(exactly = 1) { saveBookingDraft(any()) }
        assertThat(savedDraft.captured.libraryId).isEqualTo("lib-1")
        assertThat(savedDraft.captured.zoneId).isEqualTo(1)
        assertThat(savedDraft.captured.seatResourceId).isEqualTo(10)
        assertThat(savedDraft.captured.note).isEqualTo("Mario Rossi")
        assertThat(savedDraft.captured.consentAccepted).isFalse()
        assertThat(savedDraft.captured.savedAt).isEqualTo(fixedInstant)
    }

    @Test
    fun `submit opening Login fromBooking when data layer throws LibraryNotLoggedInException`() = runTest {
        val vm = viewModel(account = account(), linkedEmail = "mario.rossi@campus.unimib.it", libraries = listOf(library()))
        advanceToConfirm(vm)
        coEvery { bookSeat(any(), any(), any(), any(), any(), any(), any(), any()) } throws LibraryNotLoggedInException()

        vm.submit()

        assertThat(vm.backStack.value.last()).isEqualTo(LibraryPage.Login(fromBooking = true))
        coVerify(exactly = 1) { saveBookingDraft(any()) }
    }

    @Test
    fun `post-verification outcome 1 stays on Confirm when chosen seat is still bookable and never submits`() = runTest {
        coEvery { requestEmailValidation(any()) } returns "uuid-1"
        coEvery { confirmEmailValidation(any(), "uuid-1") } returns true
        val vm = viewModel(account = account(), linkedEmail = null, libraries = listOf(library()))
        advanceToConfirm(vm, selectedSeat = seat(10, LocalTime.of(14, 0), placesBookable = 1))

        vm.submit()
        vm.sendLoginEmail()
        vm.verifyLogin()
        vm.dismissLoginFeedback()

        assertThat(vm.backStack.value.last()).isEqualTo(LibraryPage.Confirm)
        assertThat(vm.selectedSeat.value?.resourceId).isEqualTo(10)
        coVerify(exactly = 1) { clearBookingDraft() }
        coVerify(exactly = 0) { bookSeat(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `post-verification outcome 2 emits SeatNoLongerAvailable and cuts to Seats when another seat is free`() = runTest {
        coEvery { requestEmailValidation(any()) } returns "uuid-1"
        coEvery { confirmEmailValidation(any(), "uuid-1") } returns true
        val vm = viewModel(account = account(), linkedEmail = null, libraries = listOf(library()))
        advanceToConfirm(vm, selectedSeat = seat(10, LocalTime.of(14, 0), placesBookable = 1))

        vm.submit()
        vm.sendLoginEmail()
        vm.verifyLogin()

        coEvery { getAvailableSeats("lib-1", 1, tomorrow, 120, null) } returns listOf(
            seat(10, LocalTime.of(14, 0), placesBookable = 0),
            seat(11, LocalTime.of(14, 0), placesBookable = 1),
        )

        vm.events.test {
            assertThat(awaitItem()).isInstanceOf(LibraryEvent.LoginEmailSent::class.java)
            vm.dismissLoginFeedback()
            assertThat(awaitItem()).isEqualTo(LibraryEvent.SeatNoLongerAvailable)
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(vm.backStack.value.last()).isEqualTo(LibraryPage.Seats)
        assertThat(vm.selectedSeat.value).isNull()
        assertThat(vm.selectedStartTime.value).isEqualTo(LocalTime.of(14, 0))
        coVerify(exactly = 1) { clearBookingDraft() }
        coVerify(exactly = 0) { bookSeat(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `post-verification outcome 3 cuts to DateTime and clears start time when no seat is bookable at that time`() = runTest {
        coEvery { requestEmailValidation(any()) } returns "uuid-1"
        coEvery { confirmEmailValidation(any(), "uuid-1") } returns true
        val vm = viewModel(account = account(), linkedEmail = null, libraries = listOf(library()))
        advanceToConfirm(vm, selectedSeat = seat(10, LocalTime.of(14, 0), placesBookable = 1))

        vm.submit()
        vm.sendLoginEmail()
        vm.verifyLogin()

        coEvery { getAvailableSeats("lib-1", 1, tomorrow, 120, null) } returns listOf(
            seat(10, LocalTime.of(16, 0), placesBookable = 1),
        )

        vm.dismissLoginFeedback()

        assertThat(vm.backStack.value.last()).isEqualTo(LibraryPage.DateTime)
        assertThat(vm.selectedStartTime.value).isNull()
        assertThat(vm.selectedSeat.value).isNull()
        coVerify(exactly = 1) { clearBookingDraft() }
        coVerify(exactly = 0) { bookSeat(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `post-verification outcome 4 stays on Confirm when availability reload fails`() = runTest {
        coEvery { requestEmailValidation(any()) } returns "uuid-1"
        coEvery { confirmEmailValidation(any(), "uuid-1") } returns true
        val vm = viewModel(account = account(), linkedEmail = null, libraries = listOf(library()))
        advanceToConfirm(vm, selectedSeat = seat(10, LocalTime.of(14, 0), placesBookable = 1))

        vm.submit()
        vm.sendLoginEmail()
        vm.verifyLogin()

        coEvery { getAvailableSeats("lib-1", 1, tomorrow, 120, null) } throws IOException("network error")

        vm.events.test {
            assertThat(awaitItem()).isInstanceOf(LibraryEvent.LoginEmailSent::class.java)
            vm.dismissLoginFeedback()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(vm.backStack.value.last()).isEqualTo(LibraryPage.Confirm)
        assertThat(vm.selectedSeat.value?.resourceId).isEqualTo(10)
        coVerify(exactly = 1) { clearBookingDraft() }
        coVerify(exactly = 0) { bookSeat(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `checkValidationSilently changes nothing visible when link is not opened yet or on error`() = runTest {
        coEvery { requestEmailValidation(any()) } returns "uuid-1"
        coEvery { confirmEmailValidation(any(), "uuid-1") } returns false
        val vm = viewModel(account = account())
        vm.openLogin()
        vm.sendLoginEmail()

        vm.events.test {
            assertThat(awaitItem()).isInstanceOf(LibraryEvent.LoginEmailSent::class.java)
            vm.checkValidationSilently()
            expectNoEvents()

            coEvery { confirmEmailValidation(any(), "uuid-1") } throws IOException("offline")
            vm.checkValidationSilently()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(vm.loginPhase.value).isEqualTo(LibraryLoginPhase.AwaitingClick)
        assertThat(vm.loginFeedback.value).isNull()
    }

    @Test
    fun `checkValidationSilently does not run concurrently with manual verify`() = runTest {
        val gate = CompletableDeferred<Boolean>()
        coEvery { requestEmailValidation(any()) } returns "uuid-1"
        coEvery { confirmEmailValidation(any(), "uuid-1") } coAnswers { gate.await() }
        val vm = viewModel(account = account())
        vm.openLogin()
        vm.sendLoginEmail()

        vm.verifyLogin()
        vm.checkValidationSilently()
        gate.complete(true)

        coVerify(exactly = 1) { confirmEmailValidation("mario.rossi@campus.unimib.it", "uuid-1") }
        assertThat(vm.loginFeedback.value).isEqualTo(LibraryLoginFeedback.LoggedIn)
    }

    @Test
    fun `restorePendingBooking rebuilds full stack and selections on happy path`() = runTest {
        val lib = library("lib-1")
        val targetZone = zone(2)
        val targetSeat = seat(205, LocalTime.of(15, 0), placesBookable = 1)
        val draft = LibraryBookingDraft(
            libraryId = lib.id,
            zoneId = targetZone.resourceTypeId,
            date = tomorrow,
            durationMinutes = 120,
            startTime = LocalTime.of(15, 0),
            seatResourceId = targetSeat.resourceId,
            note = "Restored note",
            consentAccepted = true,
            savedAt = fixedInstant.minusSeconds(300),
        )
        val pending = LibraryPendingValidation(
            email = "mario.rossi@campus.unimib.it",
            requestUuid = "uuid-restored",
            createdAt = fixedInstant.minusSeconds(300),
        )
        coEvery { getPendingState.bookingDraft() } returns draft
        coEvery { getPendingState.pendingValidation() } returns pending
        coEvery { getZones(lib.id) } returns listOf(targetZone)
        coEvery { getAgreements(lib.id) } returns emptyList()
        coEvery { getConstraints(lib.id, targetZone.resourceTypeId, fixedDate) } returns LibraryBookingConstraints(
            openDays = listOf(tomorrow),
            openHours = listOf(LocalTime.of(15, 0)),
            durationsMinutes = listOf(120),
        )
        coEvery { getAvailableSeats(lib.id, targetZone.resourceTypeId, tomorrow, 120, null) } returns listOf(targetSeat)

        val vm = viewModel(account = account(), linkedEmail = null, libraries = listOf(lib))
        vm.restorePendingBooking()

        assertThat(vm.backStack.value).containsExactly(
            LibraryPage.Home,
            LibraryPage.Libraries,
            LibraryPage.LibraryDetail("lib-1"),
            LibraryPage.Zones,
            LibraryPage.DateTime,
            LibraryPage.Seats,
            LibraryPage.Confirm,
            LibraryPage.Login(fromBooking = true),
        ).inOrder()
        assertThat(vm.loginPhase.value).isEqualTo(LibraryLoginPhase.AwaitingClick)
        assertThat(vm.bookingLibrary.value).isEqualTo(lib)
        assertThat(vm.selectedZone.value).isEqualTo(targetZone)
        assertThat(vm.selectedDate.value).isEqualTo(tomorrow)
        assertThat(vm.selectedDuration.value).isEqualTo(120)
        assertThat(vm.selectedStartTime.value).isEqualTo(LocalTime.of(15, 0))
        assertThat(vm.selectedSeat.value).isEqualTo(targetSeat)
        assertThat(vm.note.value).isEqualTo("Restored note")
        assertThat(vm.consentAccepted.value).isTrue()
    }

    @Test
    fun `restorePendingBooking stays on Home when draft is expired`() = runTest {
        coEvery { getPendingState.bookingDraft() } returns null
        coEvery { getPendingState.pendingValidation() } returns LibraryPendingValidation(
            email = "mario.rossi@campus.unimib.it",
            requestUuid = "uuid-1",
            createdAt = fixedInstant.minusSeconds(60),
        )
        val vm = viewModel(linkedEmail = null, libraries = listOf(library()))

        vm.restorePendingBooking()

        assertThat(vm.backStack.value).containsExactly(LibraryPage.Home)
    }

    @Test
    fun `restorePendingBooking does nothing when student is already linked`() = runTest {
        coEvery { getPendingState.bookingDraft() } returns LibraryBookingDraft(
            libraryId = "lib-1",
            zoneId = 1,
            date = tomorrow,
            durationMinutes = 120,
            startTime = LocalTime.of(14, 0),
            seatResourceId = 10,
            note = "",
            consentAccepted = false,
            savedAt = fixedInstant,
        )
        coEvery { getPendingState.pendingValidation() } returns LibraryPendingValidation(
            email = "mario.rossi@campus.unimib.it",
            requestUuid = "uuid-1",
            createdAt = fixedInstant,
        )
        val vm = viewModel(linkedEmail = "mario.rossi@campus.unimib.it", libraries = listOf(library()))

        vm.restorePendingBooking()

        assertThat(vm.backStack.value).containsExactly(LibraryPage.Home)
    }

    @Test
    fun `restorePendingBooking clears draft and stays on Home when seat cannot be rebuilt`() = runTest {
        coEvery { getPendingState.bookingDraft() } returns LibraryBookingDraft(
            libraryId = "lib-1",
            zoneId = 1,
            date = tomorrow,
            durationMinutes = 120,
            startTime = LocalTime.of(14, 0),
            seatResourceId = 999,
            note = "",
            consentAccepted = false,
            savedAt = fixedInstant,
        )
        coEvery { getPendingState.pendingValidation() } returns LibraryPendingValidation(
            email = "mario.rossi@campus.unimib.it",
            requestUuid = "uuid-1",
            createdAt = fixedInstant,
        )
        coEvery { getZones("lib-1") } returns listOf(zone(1))
        coEvery { getAvailableSeats("lib-1", 1, tomorrow, 120, null) } returns emptyList()
        val vm = viewModel(linkedEmail = null, libraries = listOf(library()))

        vm.restorePendingBooking()

        assertThat(vm.backStack.value).containsExactly(LibraryPage.Home)
        coVerify(exactly = 1) { clearBookingDraft() }
    }

    @Test
    fun `backing out of Login fromBooking clears draft`() = runTest {
        val vm = viewModel(account = account(), linkedEmail = null, libraries = listOf(library()))
        advanceToConfirm(vm)
        vm.submit()

        vm.back()

        assertThat(vm.backStack.value.last()).isEqualTo(LibraryPage.Confirm)
        coVerify(exactly = 1) { clearBookingDraft() }
    }

    @Test
    fun `logoutAccount from Account returns to Home`() = runTest {
        val vm = viewModel(linkedEmail = "mario.rossi@campus.unimib.it")
        vm.openAccount()

        vm.logoutAccount()

        coVerify(exactly = 1) { logout() }
        assertThat(vm.backStack.value).containsExactly(LibraryPage.Home)
    }

    @Test
    fun `expired session pops Bookings, Account, or ReservationDetail back to Home`() = runTest {
        val vm = viewModel(linkedEmail = "mario.rossi@campus.unimib.it")
        vm.openBookings()
        vm.openReservation(reservation(1))

        linkedEmailFlow.value = null

        assertThat(vm.backStack.value).containsExactly(LibraryPage.Home)
    }

    @Test
    fun `bookingEmail prefers linkedEmail and falls back to institutionalEmail`() = runTest {
        val vm = viewModel(account = account(), linkedEmail = null)
        assertThat(vm.bookingEmail.value).isEqualTo("mario.rossi@campus.unimib.it")

        linkedEmailFlow.value = "linked@campus.unimib.it"
        assertThat(vm.bookingEmail.value).isEqualTo("linked@campus.unimib.it")
    }

    @Test
    fun `cancel emits ReservationCancelled and clears the in-flight id`() = runTest {
        val vm = viewModel()

        vm.events.test {
            vm.cancel(reservation(1))
            assertThat(awaitItem()).isEqualTo(LibraryEvent.ReservationCancelled)
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(vm.cancellingId.value).isNull()
    }

    @Test
    fun `verifyPresence with a valid code emits PresenceVerified`() = runTest {
        coEvery { verifyPresenceUseCase("CODE") } returns true
        val vm = viewModel()

        vm.events.test {
            vm.verifyPresence("CODE")
            assertThat(awaitItem()).isEqualTo(LibraryEvent.PresenceVerified)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `verifyPresence with an invalid code emits PresenceInvalidCode`() = runTest {
        coEvery { verifyPresenceUseCase("BAD") } returns false
        val vm = viewModel()

        vm.events.test {
            vm.verifyPresence("BAD")
            assertThat(awaitItem()).isEqualTo(LibraryEvent.PresenceInvalidCode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `verifyPresence ignores a blank code`() = runTest {
        val vm = viewModel()

        vm.events.test {
            vm.verifyPresence("   ")
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 0) { verifyPresenceUseCase(any()) }
    }

    @Test
    fun `refresh failure emits SyncFailed and sets Failed for a non-auth error`() = runTest {
        val vm = viewModel()
        coEvery { refreshReservations() } throws IOException("offline")

        vm.events.test {
            vm.refresh()
            assertThat(awaitItem()).isInstanceOf(LibraryEvent.SyncFailed::class.java)
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(vm.syncStatus.value).isInstanceOf(SyncStatus.Failed::class.java)
    }

    @Test
    fun `a not-logged-in refresh failure settles Idle without an event`() = runTest {
        val vm = viewModel()
        coEvery { refreshReservations() } throws LibraryNotLoggedInException()

        vm.events.test {
            vm.refresh()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `an external cancel deep link cancels by token and emits ReservationCancelled`() = runTest {
        val pending = MutableStateFlow<LibraryDeepLinkAction?>(null)
        val vm = viewModel(pendingAction = pending)

        vm.events.test {
            pending.value = LibraryDeepLinkAction(
                kind = LibraryActionKind.Cancel,
                reservationToken = "tok-xyz",
            )
            assertThat(awaitItem()).isEqualTo(LibraryEvent.ReservationCancelled)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { cancelBookingByToken("tok-xyz") }
    }

    @Test
    fun `setNote and setConsent update their selections`() = runTest {
        val vm = viewModel()

        vm.setNote("hello")
        vm.setConsent(true)

        assertThat(vm.note.value).isEqualTo("hello")
        assertThat(vm.consentAccepted.value).isTrue()
    }

    @Test
    fun `double tap submit while signed out saves draft once and pushes only one Login`() = runTest {
        val vm = viewModel(account = account(), linkedEmail = null, libraries = listOf(library()))
        advanceToConfirm(vm)
        val saveGate = CompletableDeferred<Unit>()
        coEvery { saveBookingDraft(any()) } coAnswers { saveGate.await() }

        vm.submit()
        vm.submit()
        saveGate.complete(Unit)

        coVerify(exactly = 1) { saveBookingDraft(any()) }
        assertThat(vm.backStack.value.count { it is LibraryPage.Login }).isEqualTo(1)
        assertThat(vm.backStack.value.last()).isEqualTo(LibraryPage.Login(fromBooking = true))
    }

    @Test
    fun `backing out during draft save does not push Login over another page`() = runTest {
        val vm = viewModel(account = account(), linkedEmail = null, libraries = listOf(library()))
        advanceToConfirm(vm)
        val saveGate = CompletableDeferred<Unit>()
        coEvery { saveBookingDraft(any()) } coAnswers { saveGate.await() }

        vm.submit()
        vm.back()
        saveGate.complete(Unit)

        assertThat(vm.backStack.value.last()).isEqualTo(LibraryPage.Seats)
        assertThat(vm.backStack.value.none { it is LibraryPage.Login }).isTrue()
    }

    @Test
    fun `emailInitials extracts initials from dotted and single-word local parts`() {
        assertThat(emailInitials("m.rossi99@campus.unimib.it")).isEqualTo("MR")
        assertThat(emailInitials("mario.rossi@campus.unimib.it")).isEqualTo("MR")
        assertThat(emailInitials("student@campus.unimib.it")).isEqualTo("S")
        assertThat(emailInitials("student99@campus.unimib.it")).isEqualTo("S")
        assertThat(emailInitials("")).isEmpty()
    }

    @Test
    fun `page depths match the round 2 navigation hierarchy`() {
        assertThat(LibraryPage.Home.depth()).isEqualTo(0)
        assertThat(LibraryPage.Libraries.depth()).isEqualTo(1)
        assertThat(LibraryPage.Bookings.depth()).isEqualTo(1)
        assertThat(LibraryPage.Account.depth()).isEqualTo(1)
        assertThat(LibraryPage.Services.depth()).isEqualTo(1)
        assertThat(LibraryPage.Login(fromBooking = false).depth()).isEqualTo(1)
        assertThat(LibraryPage.LibraryDetail("id").depth()).isEqualTo(2)
        assertThat(LibraryPage.ReservationDetail(1).depth()).isEqualTo(2)
        assertThat(LibraryPage.Zones.depth()).isEqualTo(3)
        assertThat(LibraryPage.DateTime.depth()).isEqualTo(4)
        assertThat(LibraryPage.Seats.depth()).isEqualTo(5)
        assertThat(LibraryPage.Confirm.depth()).isEqualTo(6)
        assertThat(LibraryPage.Login(fromBooking = true).depth()).isEqualTo(7)
        assertThat(LibraryPage.Done.depth()).isEqualTo(8)
    }

    @Test
    fun `openLibraries and openServices push depth-1 pages and Bookings empty action replaces Bookings with Libraries`() = runTest {
        val vm = viewModel(linkedEmail = "mario.rossi@campus.unimib.it")

        vm.openServices()
        assertThat(vm.backStack.value).containsExactly(LibraryPage.Home, LibraryPage.Services).inOrder()
        vm.back()

        vm.openBookings()
        assertThat(vm.backStack.value).containsExactly(LibraryPage.Home, LibraryPage.Bookings).inOrder()
        vm.openLibraries()
        assertThat(vm.backStack.value).containsExactly(LibraryPage.Home, LibraryPage.Libraries).inOrder()
    }

    @Test
    fun `libraries are ordered with preferredCampus first and Centrale first when null`() = runTest {
        val centrale = library(LibraryCatalog.CENTRALE_ID)
        val medicina = library(LibraryCatalog.MEDICINA_ID)
        coEvery { getPreferredCampus() } returns LibraryCampus.Monza

        val vm = viewModel(libraries = listOf(centrale, medicina))

        assertThat(vm.preferredCampus.value).isEqualTo(LibraryCampus.Monza)
        assertThat((vm.libraries.value as Loadable.Loaded).value.map { it.id })
            .containsExactly(LibraryCatalog.MEDICINA_ID, LibraryCatalog.CENTRALE_ID).inOrder()
    }

    @Test
    fun `onSheetOpened fetches live status once per library and openLibrary reuses it without fetching twice`() = runTest {
        val centrale = library(LibraryCatalog.CENTRALE_ID)
        val medicina = library(LibraryCatalog.MEDICINA_ID)
        val centraleLive = LibraryLiveStatus(
            isOpen = true,
            statusText = "Aperta fino alle 22:00",
            occupancyPercentage = 55,
            crowdLevel = null,
            todayForecast = emptyList(),
            forecastDate = fixedDate,
        )
        val medicinaLive = LibraryLiveStatus(
            isOpen = true,
            statusText = "Aperta fino alle 20:00",
            occupancyPercentage = 30,
            crowdLevel = null,
            todayForecast = emptyList(),
            forecastDate = fixedDate,
        )
        coEvery { getLiveStatus(LibraryCatalog.CENTRALE_ID) } returns centraleLive
        coEvery { getLiveStatus(LibraryCatalog.MEDICINA_ID) } returns medicinaLive

        val vm = viewModel(libraries = listOf(centrale, medicina))
        vm.onSheetOpened()

        // Armchairs receive live statuses fetched on open.
        val afterOpen = (vm.libraries.value as Loadable.Loaded).value.associateBy { it.id }
        assertThat(afterOpen[LibraryCatalog.CENTRALE_ID]?.liveStatus).isEqualTo(centraleLive)
        assertThat(afterOpen[LibraryCatalog.MEDICINA_ID]?.liveStatus).isEqualTo(medicinaLive)

        // Opening the detail page reuses the cached live status instead of fetching twice.
        vm.openLibrary(centrale)
        assertThat((vm.liveStatus.value as Loadable.Loaded).value).isEqualTo(centraleLive)
        coVerify(exactly = 1) { getLiveStatus(LibraryCatalog.CENTRALE_ID) }
        coVerify(exactly = 1) { getLiveStatus(LibraryCatalog.MEDICINA_ID) }
        coVerify(exactly = 1) { recordLastUsedLibrary(LibraryCatalog.CENTRALE_ID) }

        // Explicit retry forces a fresh fetch.
        vm.retryDetail()
        coVerify(exactly = 2) { getLiveStatus(LibraryCatalog.CENTRALE_ID) }
    }

    @Test
    fun `openLibrary loads facility services, ignores service failure, and merges full site contact info`() = runTest {
        val centrale = library(LibraryCatalog.CENTRALE_ID)
        val services = listOf(LibraryService(LibraryServiceType.Wifi, "wifi", "Wi-Fi"))
        coEvery { getLibraryServices(LibraryCatalog.CENTRALE_ID) } returns services
        coEvery { getLibrary(LibraryCatalog.CENTRALE_ID) } returns centrale.copy(
            address = "Piazza dell'Ateneo Nuovo 1, Milano, Italie",
            phone = "+390264486251",
            email = "biblioteca.centrale@unimib.it",
        )
        val vm = viewModel(libraries = listOf(centrale))

        vm.openLibrary(centrale)

        assertThat(vm.siteServices.value).isEqualTo(services)
        val enriched = (vm.libraries.value as Loadable.Loaded).value.single()
        assertThat(enriched.address).isEqualTo("Piazza dell'Ateneo Nuovo 1, Milano, Italie")
        assertThat(enriched.phone).isEqualTo("+390264486251")
        assertThat(enriched.email).isEqualTo("biblioteca.centrale@unimib.it")

        // A failing services call omits chips without failing the library page.
        coEvery { getLibraryServices(LibraryCatalog.CENTRALE_ID) } throws IOException("fail")
        vm.retryDetail()
        assertThat(vm.siteServices.value).isEmpty()
        assertThat(vm.detailStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `pastReservations splits ended bookings newest first excluding cancelled and bookingsTab resets on close`() = runTest {
        val now = LocalDateTime.now(AppClock.clock)
        reservationsFlow.value = listOf(
            reservation(1, start = now.plusHours(2), end = now.plusHours(4), state = LibraryReservationState.Upcoming),
            reservation(2, start = now.minusDays(3), end = now.minusDays(3).plusHours(2), state = LibraryReservationState.Past),
            reservation(3, start = now.minusDays(1), end = now.minusDays(1).plusHours(2), state = LibraryReservationState.Past),
            reservation(4, start = now.minusDays(2), end = now.minusDays(2).plusHours(2), state = LibraryReservationState.Cancelled),
        )
        val vm = viewModel()

        assertThat((vm.reservations.value as Loadable.Loaded).value.map { it.reservationId })
            .containsExactly(1)
        assertThat((vm.pastReservations.value as Loadable.Loaded).value.map { it.reservationId })
            .containsExactly(3, 2).inOrder()

        vm.selectBookingsTab(LibraryBookingsTab.History)
        assertThat(vm.bookingsTab.value).isEqualTo(LibraryBookingsTab.History)
        vm.resetNavigation()
        assertThat(vm.bookingsTab.value).isEqualTo(LibraryBookingsTab.Upcoming)
    }

    @Test
    fun `addressChooser cleans address, attaches building code, and dismisses via dismiss or back`() = runTest {
        val centrale = library(LibraryCatalog.CENTRALE_ID).copy(
            address = "Piazza dell'Ateneo Nuovo 1, 20126 Milano MI, Italie",
        )
        val vm = viewModel(libraries = listOf(centrale))
        vm.openLibrary(centrale)

        vm.openAddressChooser(centrale)
        assertThat(vm.addressChooser.value?.address).isEqualTo("Piazza dell'Ateneo Nuovo 1, 20126 Milano MI")
        assertThat(vm.addressChooser.value?.buildingCode).isEqualTo("U06")

        vm.back()
        assertThat(vm.addressChooser.value).isNull()
        assertThat(vm.backStack.value.last()).isEqualTo(LibraryPage.LibraryDetail(LibraryCatalog.CENTRALE_ID))

        val unknown = library("unknown-id").copy(address = "Via Roma 1, Italia")
        vm.openAddressChooser(unknown)
        assertThat(vm.addressChooser.value?.address).isEqualTo("Via Roma 1")
        assertThat(vm.addressChooser.value?.buildingCode).isNull()
        vm.dismissAddressChooser()
        assertThat(vm.addressChooser.value).isNull()
    }

    @Test
    fun `crowdingWeek reflects observed history for the opened library`() = runTest {
        val centrale = library(LibraryCatalog.CENTRALE_ID)
        crowdingHistoryFlow.value = mapOf(
            DayOfWeek.TUESDAY to LibraryWeekdayCrowding(
                weekday = DayOfWeek.TUESDAY,
                date = LocalDate.of(2026, 6, 9),
                slots = listOf(LibraryCrowdingSlot(LocalTime.of(8, 30), LocalTime.of(10, 0), 48)),
                isClosed = false,
            ),
            DayOfWeek.SUNDAY to LibraryWeekdayCrowding(
                weekday = DayOfWeek.SUNDAY,
                date = LocalDate.of(2026, 6, 14),
                slots = emptyList(),
                isClosed = true,
            ),
        )
        val vm = viewModel(libraries = listOf(centrale))
        vm.openLibrary(centrale)

        val tuesday = vm.crowdingWeek.value.first { it.weekday == DayOfWeek.TUESDAY }
        assertThat(tuesday.state).isInstanceOf(LibraryDayCrowdingState.Data::class.java)
        val sunday = vm.crowdingWeek.value.first { it.weekday == DayOfWeek.SUNDAY }
        assertThat(sunday.state).isEqualTo(LibraryDayCrowdingState.Closed)
    }

    private fun account(): Account = Account(
        id = AccountId("acc-1"),
        username = "mario.rossi@campus.unimib.it",
        displayName = "Mario Rossi",
        academic = AcademicIdentity(
            recordUserId = "u1",
            personId = 7L,
            fiscalCode = null,
            careers = listOf(
                Career(
                    id = CareerId(101L),
                    enrollmentTraitId = 1L,
                    programId = 2L,
                    easyStaffProgramCode = "E32",
                    academicYearEnrollmentId = 3L,
                    studentNumber = "900001",
                    description = "Informatica",
                    academicYear = 2024,
                    status = CareerStatus.ACTIVE,
                ),
            ),
            selectedCareerId = CareerId(101L),
        ),
        learning = LearningIdentity(
            lmsUserId = 11,
            lmsUsername = "mario.rossi@campus.unimib.it",
            locale = "it",
            isSiteAdmin = false,
            maxUploadFileSizeBytes = 0L,
            storageQuotaBytes = 0L,
        ),
        createdAt = Instant.EPOCH,
        lastUsedAt = Instant.EPOCH,
        lastSyncedAt = Instant.EPOCH,
    )
}
