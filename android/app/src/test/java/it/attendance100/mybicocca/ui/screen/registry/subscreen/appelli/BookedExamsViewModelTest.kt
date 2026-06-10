package it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli

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
import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.model.exam.ExamCallType
import it.attendance100.mybicocca.domain.model.exam.ExamType
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.exam.CancelBookingUseCase
import it.attendance100.mybicocca.domain.usecase.exam.GetBookingSlipUseCase
import it.attendance100.mybicocca.domain.usecase.exam.GetBookingsUseCase
import it.attendance100.mybicocca.domain.usecase.exam.GetPresenceCertificateUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.state.BookedEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.state.CancelActionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Instant

/**
 * State-machine coverage for the booked-exams (prenotazioni) ViewModel: the live-first fetch,
 * cancellation gating + optimistic drop + reconcile, the per-document download outcomes (OpenPdf
 * with the derived filename, ShowMessage on failure), and the identityKey worked example.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BookedExamsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val careerId = CareerId(101L)

    private val getBookings: GetBookingsUseCase = mockk()
    private val cancelBooking: CancelBookingUseCase = mockk(relaxed = true)
    private val getBookingSlip: GetBookingSlipUseCase = mockk()
    private val getPresenceCertificate: GetPresenceCertificateUseCase = mockk()
    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()

    private fun viewModel(account: Account? = account(careerId)): BookedExamsViewModel {
        every { observeActiveAccount() } returns flowOf(account)
        return BookedExamsViewModel(
            getBookings,
            cancelBooking,
            getBookingSlip,
            getPresenceCertificate,
            observeActiveAccount,
        )
    }

    @Test
    fun `init fetches and goes NotYetLoaded then Loaded`() = runTest {
        val list = listOf(booking(studentId = 555L))
        coEvery { getBookings(careerId) } returns list
        val vm = viewModel()

        val data = vm.bookings.value
        assertThat(data).isInstanceOf(Loadable.Loaded::class.java)
        assertThat((data as Loadable.Loaded).value).isEqualTo(list)
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `fetch failure sets Failed without clearing already-loaded bookings`() = runTest {
        val list = listOf(booking(studentId = 555L))
        coEvery { getBookings(careerId) } returns list
        val vm = viewModel()

        val boom = IOException("offline")
        coEvery { getBookings(careerId) } throws boom
        vm.refresh()

        val status = vm.syncStatus.value
        assertThat(status).isInstanceOf(SyncStatus.Failed::class.java)
        assertThat((status as SyncStatus.Failed).cause).isEqualTo(boom)
        assertThat((vm.bookings.value as Loadable.Loaded).value).isEqualTo(list)
    }

    @Test
    fun `identityKey composes the call triple with applicationListId preference`() {
        val withList = booking(studentId = 555L, applicationListId = 9000L)
        assertThat(withList.identityKey()).isEqualTo("1/2/3/9000")

        val withoutList = booking(studentId = 555L, applicationListId = null)
        assertThat(withoutList.identityKey()).isEqualTo("1/2/3/555")

        val withNeither = booking(studentId = null, applicationListId = null)
        assertThat(withNeither.identityKey()).isEqualTo("1/2/3/0")
    }

    @Test
    fun `cancel succeeds, emits CancellationSucceeded, drops the row, then refetches`() = runTest {
        val target = booking(studentId = 555L, applicationListId = 9000L)
        coEvery { getBookings(careerId) } returns listOf(target)
        val vm = viewModel()

        coEvery { getBookings(careerId) } returns emptyList()

        vm.events.test {
            vm.cancel(target)
            assertThat(awaitItem()).isEqualTo(BookedEvent.CancellationSucceeded)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { cancelBooking(careerId, target.key, 555L) }
        assertThat(vm.cancelAction.value).isEqualTo(CancelActionState.Idle)
        assertThat((vm.bookings.value as Loadable.Loaded).value).isEmpty()
    }

    @Test
    fun `cancel failure emits CancellationFailed and keeps the booking`() = runTest {
        val target = booking(studentId = 555L, applicationListId = 9000L)
        coEvery { getBookings(careerId) } returns listOf(target)
        val vm = viewModel()

        val boom = IOException("cannot cancel")
        coEvery { cancelBooking(any(), any(), any()) } throws boom

        vm.events.test {
            vm.cancel(target)
            val event = awaitItem()
            assertThat(event).isInstanceOf(BookedEvent.CancellationFailed::class.java)
            assertThat((event as BookedEvent.CancellationFailed).cause).isEqualTo(boom)
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(vm.cancelAction.value).isEqualTo(CancelActionState.Idle)
        assertThat((vm.bookings.value as Loadable.Loaded).value).containsExactly(target)
    }

    @Test
    fun `cancel is a no-op when the booking has no studentId`() = runTest {
        coEvery { getBookings(careerId) } returns emptyList()
        val vm = viewModel()

        vm.cancel(booking(studentId = null))

        coVerify(exactly = 0) { cancelBooking(any(), any(), any()) }
    }

    @Test
    fun `downloadBookingSlip emits OpenPdf with the statino filename`() = runTest {
        val target = booking(studentId = 555L)
        coEvery { getBookings(careerId) } returns listOf(target)
        coEvery { getBookingSlip(careerId, target.key, 555L) } returns byteArrayOf(1, 2, 3)
        val vm = viewModel()

        vm.events.test {
            vm.downloadBookingSlip(target)
            val event = awaitItem()
            assertThat(event).isInstanceOf(BookedEvent.OpenPdf::class.java)
            assertThat((event as BookedEvent.OpenPdf).fileName).isEqualTo("statino_prenotazione_2_3.pdf")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `downloadPresenceCertificate failure emits the not-yet-available message`() = runTest {
        val target = booking(studentId = 555L)
        coEvery { getBookings(careerId) } returns listOf(target)
        coEvery { getPresenceCertificate(careerId, target.key, 555L) } throws IOException("422")
        val vm = viewModel()

        vm.events.test {
            vm.downloadPresenceCertificate(target)
            val event = awaitItem()
            assertThat(event).isInstanceOf(BookedEvent.ShowMessage::class.java)
            assertThat((event as BookedEvent.ShowMessage).message)
                .isEqualTo("L'attestato di presenza non è ancora disponibile per questo appello.")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `download is a no-op when the booking has no studentId`() = runTest {
        coEvery { getBookings(careerId) } returns emptyList()
        val vm = viewModel()

        vm.downloadBookingSlip(booking(studentId = null))

        coVerify(exactly = 0) { getBookingSlip(any(), any(), any()) }
    }

    private fun booking(studentId: Long?, applicationListId: Long? = null): BookedExam = BookedExam(
        key = ExamCallKey(courseOfStudyId = 1L, activityId = 2L, callId = 3),
        applicationListId = applicationListId,
        studentId = studentId,
        activityChoiceId = 99L,
        activityDescription = "Algoritmi",
        examCallDescription = "Appello I",
        examType = ExamType.Written,
        callType = ExamCallType.Final,
        examDateTime = null,
        classroomDescription = null,
        buildingDescription = null,
        credits = 6f,
        examModeDescription = null,
        position = null,
        bookingDate = null,
        cancellableUntil = null,
        studentNote = null,
    )

    private companion object {
        fun account(careerId: CareerId): Account = Account(
            id = AccountId("acc-1"),
            username = "mario.rossi@campus.unimib.it",
            displayName = "Mario Rossi",
            academic = AcademicIdentity(
                recordUserId = "u1",
                personId = 7L,
                fiscalCode = null,
                careers = listOf(
                    Career(
                        id = careerId,
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
                selectedCareerId = careerId,
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
}
