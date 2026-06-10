package it.attendance100.mybicocca.ui.screen.registry.subscreen.library

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.model.library.Library
import it.attendance100.mybicocca.domain.model.library.LibraryActionKind
import it.attendance100.mybicocca.domain.model.library.LibraryDeepLinkAction
import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.domain.model.library.LibraryReservationState
import it.attendance100.mybicocca.domain.repository.LibraryNotLoggedInException
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.library.BookLibrarySeatUseCase
import it.attendance100.mybicocca.domain.usecase.library.CancelLibraryBookingByTokenUseCase
import it.attendance100.mybicocca.domain.usecase.library.CancelLibraryReservationUseCase
import it.attendance100.mybicocca.domain.usecase.library.ConfirmLibraryEmailValidationUseCase
import it.attendance100.mybicocca.domain.usecase.library.ConsumeLibraryActionUseCase
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
import it.attendance100.mybicocca.domain.usecase.library.ObservePendingLibraryActionUseCase
import it.attendance100.mybicocca.domain.usecase.library.RefreshLibraryReservationsUseCase
import it.attendance100.mybicocca.domain.usecase.library.RequestLibraryEmailValidationUseCase
import it.attendance100.mybicocca.domain.usecase.library.VerifyLibraryPresenceUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryLoginFeedback
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryLoginPhase
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryPage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime

/**
 * State-machine coverage for the Biblioteca modal owner: the in-sheet back-stack, the
 * concluded-reservation filter on the observe stream, the email-validation login phase
 * machine and its in-content feedback, the single-flight cancel guard, presence
 * verification outcomes, deep-link cancellation, and the offline-tolerant sync.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getLibraries: GetLibrariesUseCase = mockk()
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

    private val reservationsFlow = MutableStateFlow<List<LibraryReservation>>(emptyList())

    private fun reservation(id: Int, end: LocalDateTime) = LibraryReservation(
        reservationId = id,
        libraryName = "Biblioteca Centrale",
        librarySecondaryName = null,
        seatName = "A12",
        start = end.minusHours(2),
        end = end,
        note = null,
        reservationCode = "R$id",
        cancellationToken = "tok-$id",
        state = LibraryReservationState.Upcoming,
    )

    private fun library() = Library(
        id = "lib-1",
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

    private fun viewModel(account: Account? = null): LibraryViewModel {
        coEvery { getLibraries() } returns emptyList()
        every { observeReservations() } returns reservationsFlow
        coEvery { refreshReservations() } returns Unit
        every { observeLinkedEmail() } returns flowOf(null)
        every { observeActiveAccount() } returns flowOf(account)
        every { observePendingLibraryAction() } returns MutableStateFlow(null)
        return LibraryViewModel(
            getLibraries, getLiveStatus, getWeekHours, getZones, getAgreements, getConstraints,
            getAvailableSeats, bookSeat, observeReservations, refreshReservations, cancelReservation,
            verifyPresenceUseCase, observeLinkedEmail, requestEmailValidation, confirmEmailValidation,
            logout, cancelBookingByToken, observeActiveAccount, observePendingLibraryAction,
            consumeLibraryAction,
        )
    }

    @Test
    fun `the back stack starts at Home`() = runTest {
        val vm = viewModel()

        assertThat(vm.backStack.value).containsExactly(LibraryPage.Home)
    }

    @Test
    fun `libraries reach Loaded after the init fetch`() = runTest {
        coEvery { getLibraries() } returns listOf(library())
        every { observeReservations() } returns reservationsFlow
        every { observeLinkedEmail() } returns flowOf(null)
        every { observeActiveAccount() } returns flowOf(null)
        every { observePendingLibraryAction() } returns MutableStateFlow(null)

        val vm = LibraryViewModel(
            getLibraries, getLiveStatus, getWeekHours, getZones, getAgreements, getConstraints,
            getAvailableSeats, bookSeat, observeReservations, refreshReservations, cancelReservation,
            verifyPresenceUseCase, observeLinkedEmail, requestEmailValidation, confirmEmailValidation,
            logout, cancelBookingByToken, observeActiveAccount, observePendingLibraryAction,
            consumeLibraryAction,
        )

        assertThat((vm.libraries.value as Loadable.Loaded).value).hasSize(1)
        assertThat(vm.librariesStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `concluded reservations are filtered out of the observe stream`() = runTest {
        reservationsFlow.value = listOf(
            reservation(1, LocalDateTime.now().plusDays(1)),
            reservation(2, LocalDateTime.now().minusDays(1)),
        )
        val vm = viewModel()

        vm.reservations.test {
            var item = awaitItem()
            while (item is Loadable.NotYetLoaded) item = awaitItem()
            assertThat(item).isInstanceOf(Loadable.Loaded::class.java)
            val list = (item as Loadable.Loaded).value
            assertThat(list).hasSize(1)
            assertThat(list.first().reservationId).isEqualTo(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `openLibraries and openLibrary push pages`() = runTest {
        val vm = viewModel()

        vm.openLibraries()
        vm.openLibrary(library())

        assertThat(vm.backStack.value).hasSize(3)
        assertThat(vm.backStack.value.last()).isEqualTo(LibraryPage.LibraryDetail("lib-1"))
    }

    @Test
    fun `back pops one page and stops at Home`() = runTest {
        val vm = viewModel()
        vm.openLibraries()

        vm.back()
        vm.back()

        assertThat(vm.backStack.value).containsExactly(LibraryPage.Home)
    }

    @Test
    fun `openLogin resets the login phase and pushes the login page`() = runTest {
        val vm = viewModel()

        vm.openLogin()

        assertThat(vm.loginPhase.value).isEqualTo(LibraryLoginPhase.EnterEmail)
        assertThat(vm.backStack.value.last()).isEqualTo(LibraryPage.Login)
    }

    @Test
    fun `sendLoginEmail moves to AwaitingClick and emits LoginEmailSent`() = runTest {
        coEvery { requestEmailValidation("mario.rossi@campus.unimib.it") } returns "uuid-1"
        val vm = viewModel(account = account())

        vm.institutionalEmail.test {
            var email = awaitItem()
            while (email == null) email = awaitItem()
            assertThat(email).isEqualTo("mario.rossi@campus.unimib.it")
            vm.events.test {
                vm.sendLoginEmail()
                assertThat(awaitItem()).isInstanceOf(LibraryEvent.LoginEmailSent::class.java)
                cancelAndIgnoreRemainingEvents()
            }
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(vm.loginPhase.value).isEqualTo(LibraryLoginPhase.AwaitingClick)
    }

    @Test
    fun `a validated link shows LoggedIn feedback`() = runTest {
        coEvery { requestEmailValidation(any()) } returns "uuid-1"
        coEvery { confirmEmailValidation("mario.rossi@campus.unimib.it", "uuid-1") } returns true
        val vm = viewModel(account = account())

        vm.institutionalEmail.test {
            var email = awaitItem()
            while (email == null) email = awaitItem()
            assertThat(email).isEqualTo("mario.rossi@campus.unimib.it")
            vm.sendLoginEmail()
            vm.verifyLogin()
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(vm.loginFeedback.value).isEqualTo(LibraryLoginFeedback.LoggedIn)
    }

    @Test
    fun `an unopened link reverts to AwaitingClick with LinkNotOpened feedback`() = runTest {
        coEvery { requestEmailValidation(any()) } returns "uuid-1"
        coEvery { confirmEmailValidation(any(), "uuid-1") } returns false
        val vm = viewModel(account = account())

        vm.institutionalEmail.test {
            var email = awaitItem()
            while (email == null) email = awaitItem()
            assertThat(email).isEqualTo("mario.rossi@campus.unimib.it")
            vm.sendLoginEmail()
            vm.verifyLogin()
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(vm.loginPhase.value).isEqualTo(LibraryLoginPhase.AwaitingClick)
        assertThat(vm.loginFeedback.value).isEqualTo(LibraryLoginFeedback.LinkNotOpened)
    }

    @Test
    fun `cancel emits ReservationCancelled and clears the in-flight id`() = runTest {
        val vm = viewModel()

        vm.events.test {
            vm.cancel(reservation(1, LocalDateTime.now().plusDays(1)))
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
        coEvery { refreshReservations() } returns Unit
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
        coEvery { refreshReservations() } returns Unit
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
        coEvery { getLibraries() } returns emptyList()
        every { observeReservations() } returns reservationsFlow
        every { observeLinkedEmail() } returns flowOf(null)
        every { observeActiveAccount() } returns flowOf(null)
        every { observePendingLibraryAction() } returns pending
        val vm = LibraryViewModel(
            getLibraries, getLiveStatus, getWeekHours, getZones, getAgreements, getConstraints,
            getAvailableSeats, bookSeat, observeReservations, refreshReservations, cancelReservation,
            verifyPresenceUseCase, observeLinkedEmail, requestEmailValidation, confirmEmailValidation,
            logout, cancelBookingByToken, observeActiveAccount, observePendingLibraryAction,
            consumeLibraryAction,
        )

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
