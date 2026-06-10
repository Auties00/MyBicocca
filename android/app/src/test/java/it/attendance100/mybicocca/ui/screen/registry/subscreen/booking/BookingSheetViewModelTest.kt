package it.attendance100.mybicocca.ui.screen.registry.subscreen.booking

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.model.exam.BookExamRequest
import it.attendance100.mybicocca.domain.model.exam.ExamBooking
import it.attendance100.mybicocca.domain.model.exam.ExamCall
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.model.exam.ExamCallType
import it.attendance100.mybicocca.domain.model.exam.ExamEnrollmentWindow
import it.attendance100.mybicocca.domain.model.exam.ExamType
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.exam.BookExamUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingActionState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingSheetEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingSheetStep
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Instant

/**
 * Coverage for the per-appello booking flow ViewModel: open/close resets, step navigation gated
 * on canBook, and the confirm submission contract (gated re-entry, success/failure one-shot
 * events, the request payload forwarded to the use case).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BookingSheetViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val careerId = CareerId(101L)
    private val key = ExamCallKey(courseOfStudyId = 1L, activityId = 2L, callId = 3)

    private val bookExam: BookExamUseCase = mockk()
    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()

    private fun viewModel(account: Account? = account(careerId)): BookingSheetViewModel {
        every { observeActiveAccount() } returns flowOf(account)
        return BookingSheetViewModel(bookExam, observeActiveAccount)
    }

    @Test
    fun `open sets the target and resets to the Info step and Idle action`() = runTest {
        val vm = viewModel()
        vm.goToConfirm()

        vm.open(examCall(adsceId = 99L))

        assertThat(vm.target.value?.call?.key).isEqualTo(key)
        assertThat(vm.target.value?.activityChoiceId).isEqualTo(99L)
        assertThat(vm.step.value).isEqualTo(BookingSheetStep.Info)
        assertThat(vm.bookingAction.value).isEqualTo(BookingActionState.Idle)
    }

    @Test
    fun `open with a non-positive choice id leaves the target read-only`() = runTest {
        val vm = viewModel()

        vm.open(examCall(adsceId = 0L))

        assertThat(vm.target.value?.activityChoiceId).isNull()
        assertThat(vm.target.value?.canBook).isEqualTo(false)
    }

    @Test
    fun `close clears the target and resets step and action`() = runTest {
        val vm = viewModel()
        vm.open(examCall(adsceId = 99L))
        vm.goToConfirm()

        vm.close()

        assertThat(vm.target.value).isNull()
        assertThat(vm.step.value).isEqualTo(BookingSheetStep.Info)
        assertThat(vm.bookingAction.value).isEqualTo(BookingActionState.Idle)
    }

    @Test
    fun `goToConfirm advances when the target can be booked`() = runTest {
        val vm = viewModel()
        vm.open(examCall(adsceId = 99L))

        vm.goToConfirm()

        assertThat(vm.step.value).isEqualTo(BookingSheetStep.Confirm)
    }

    @Test
    fun `goToConfirm stays on Info when the target cannot be booked`() = runTest {
        val vm = viewModel()
        vm.open(examCall(adsceId = 0L))

        vm.goToConfirm()

        assertThat(vm.step.value).isEqualTo(BookingSheetStep.Info)
    }

    @Test
    fun `goBackToInfo returns to the Info step`() = runTest {
        val vm = viewModel()
        vm.open(examCall(adsceId = 99L))
        vm.goToConfirm()

        vm.goBackToInfo()

        assertThat(vm.step.value).isEqualTo(BookingSheetStep.Info)
    }

    @Test
    fun `confirmBooking forwards the request and emits BookedSuccessfully on success`() = runTest {
        coEvery { bookExam(any(), any(), any()) } returns booking()
        val vm = viewModel()
        vm.open(examCall(adsceId = 99L))

        vm.events.test {
            vm.confirmBooking(note = "  please  ")
            assertThat(awaitItem()).isEqualTo(BookingSheetEvent.BookedSuccessfully)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify {
            bookExam(careerId, key, BookExamRequest(activityChoiceId = 99L, studentNote = "  please  "))
        }
        assertThat(vm.bookingAction.value).isEqualTo(BookingActionState.Idle)
    }

    @Test
    fun `confirmBooking blank note is normalised to null in the request`() = runTest {
        coEvery { bookExam(any(), any(), any()) } returns booking()
        val vm = viewModel()
        vm.open(examCall(adsceId = 99L))

        vm.confirmBooking(note = "   ")

        coVerify {
            bookExam(careerId, key, BookExamRequest(activityChoiceId = 99L, studentNote = null))
        }
    }

    @Test
    fun `confirmBooking emits BookingFailed and returns to Idle on failure`() = runTest {
        val boom = IOException("server down")
        coEvery { bookExam(any(), any(), any()) } throws boom
        val vm = viewModel()
        vm.open(examCall(adsceId = 99L))

        vm.events.test {
            vm.confirmBooking(note = null)
            val event = awaitItem()
            assertThat(event).isInstanceOf(BookingSheetEvent.BookingFailed::class.java)
            assertThat((event as BookingSheetEvent.BookingFailed).cause).isEqualTo(boom)
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(vm.bookingAction.value).isEqualTo(BookingActionState.Idle)
    }

    @Test
    fun `confirmBooking is a no-op when the target carries no choice id`() = runTest {
        val vm = viewModel()
        vm.open(examCall(adsceId = 0L))

        vm.confirmBooking(note = null)

        coVerify(exactly = 0) { bookExam(any(), any(), any()) }
    }

    @Test
    fun `confirmBooking is a no-op when no career is active`() = runTest {
        val vm = viewModel(account = null)
        vm.open(examCall(adsceId = 99L))

        vm.confirmBooking(note = null)

        coVerify(exactly = 0) { bookExam(any(), any(), any()) }
    }

    @Test
    fun `confirmBooking is a no-op when nothing is open`() = runTest {
        val vm = viewModel()

        vm.confirmBooking(note = null)

        coVerify(exactly = 0) { bookExam(any(), any(), any()) }
    }

    private fun examCall(adsceId: Long): ExamCall = ExamCall(
        key = key,
        examCallId = 50L,
        activityChoiceId = adsceId,
        activityCode = "E3101Q123",
        activityDescription = "Algoritmi",
        courseOfStudyDescription = "Informatica",
        callDescription = "Appello I",
        callDate = null,
        callTime = null,
        enrollmentWindow = ExamEnrollmentWindow(opensAt = null, closesAt = null),
        enrolledNumber = 0,
        state = null,
        stateDescription = null,
        callType = ExamCallType.Final,
        examType = ExamType.Written,
        isReserved = false,
        matId = null,
        notes = null,
        president = null,
        bookingTypeDescription = null,
    )

    private fun booking(): ExamBooking = ExamBooking(
        key = key,
        applicationListId = null,
        studentId = 555L,
        activityChoiceId = 99L,
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
