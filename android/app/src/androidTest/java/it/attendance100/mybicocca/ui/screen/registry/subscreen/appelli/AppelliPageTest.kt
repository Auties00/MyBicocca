package it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.model.exam.ExamCallType
import it.attendance100.mybicocca.domain.model.exam.ExamType
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.exam.BookExamUseCase
import it.attendance100.mybicocca.domain.usecase.exam.CancelBookingUseCase
import it.attendance100.mybicocca.domain.usecase.exam.GetBookingSlipUseCase
import it.attendance100.mybicocca.domain.usecase.exam.GetBookingsUseCase
import it.attendance100.mybicocca.domain.usecase.exam.GetExamCallsUseCase
import it.attendance100.mybicocca.domain.usecase.exam.GetPresenceCertificateUseCase
import it.attendance100.mybicocca.testing.setBicoccaContent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.BookableExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.BookingSheetViewModel
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime

/**
 * State + behaviour coverage for the unified Appelli sheet root. The page is driven by three real
 * ViewModels built over MockK-faked use cases (the booked-exams list, the bookable calendar and
 * the booking-action sheet), reusing the construction from the Wave 1 unit tests; passing explicit
 * instances bypasses every `hiltViewModel()` default. Anchored on [AppelliTestTags] and wrapped via
 * [setBicoccaContent], which installs the app-wide CompositionLocals (haptics, snackbar, device type)
 * the screens assume alongside the production theme. Covers the root list's empty / content / error
 * markers, that a booking row opens its detail, and that confirming a cancellation runs the cancel
 * use case.
 */
@RunWith(AndroidJUnit4::class)
class AppelliPageTest {

    @get:Rule
    val compose = createComposeRule()

    private val careerId = CareerId(101L)

    private val getBookings: GetBookingsUseCase = mockk()
    private val cancelBooking: CancelBookingUseCase = mockk(relaxed = true)
    private val getBookingSlip: GetBookingSlipUseCase = mockk()
    private val getPresenceCertificate: GetPresenceCertificateUseCase = mockk()
    private val getExamCalls: GetExamCallsUseCase = mockk()
    private val bookExam: BookExamUseCase = mockk()
    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()

    private fun setPage() {
        every { observeActiveAccount() } returns flowOf(account(careerId))
        coEvery { getExamCalls(any()) } returns emptyList()
        val bookedVm = BookedExamsViewModel(
            getBookings,
            cancelBooking,
            getBookingSlip,
            getPresenceCertificate,
            observeActiveAccount,
        )
        val bookableVm = BookableExamsViewModel(getExamCalls, observeActiveAccount)
        val sheetVm = BookingSheetViewModel(bookExam, observeActiveAccount)
        compose.setBicoccaContent {
            AppelliPage(
                bookableViewModel = bookableVm,
                viewModel = bookedVm,
                sheetViewModel = sheetVm,
            )
        }
    }

    @Test
    fun empty_bookings_render_the_empty_marker_and_keep_the_Prenota_footer() {
        coEvery { getBookings(any()) } returns emptyList()
        setPage()

        compose.onNodeWithTag(AppelliTestTags.ROOT).assertIsDisplayed()
        compose.onNodeWithTag(AppelliTestTags.STATE_EMPTY).assertIsDisplayed()
        compose.onNodeWithTag(AppelliTestTags.PRENOTA_BUTTON).assertIsDisplayed()
    }

    @Test
    fun loaded_bookings_render_the_content_list_with_the_booking_row() {
        val booked = booking(studentId = 555L)
        coEvery { getBookings(any()) } returns listOf(booked)
        setPage()

        compose.onNodeWithTag(AppelliTestTags.STATE_CONTENT).assertIsDisplayed()
        compose.onNodeWithTag(AppelliTestTags.item(booked.identityKey())).assertIsDisplayed()
    }

    @Test
    fun a_first_load_failure_renders_the_error_marker() {
        coEvery { getBookings(any()) } throws IOException("offline")
        setPage()

        compose.onNodeWithTag(AppelliTestTags.STATE_ERROR).assertIsDisplayed()
        val noContent = compose.onAllNodesWithTag(AppelliTestTags.STATE_CONTENT)
            .fetchSemanticsNodes().isEmpty()
        assert(noContent)
    }

    @Test
    fun tapping_a_booking_opens_its_detail_page() {
        val booked = booking(studentId = 555L)
        coEvery { getBookings(any()) } returns listOf(booked)
        setPage()

        compose.onNodeWithTag(AppelliTestTags.item(booked.identityKey())).performClick()
        compose.waitForIdle()

        val rootGone = compose.onAllNodesWithTag(AppelliTestTags.STATE_CONTENT)
            .fetchSemanticsNodes().isEmpty()
        assert(rootGone)
    }

    @Test
    fun confirming_a_cancellation_runs_the_cancel_use_case() {
        val booked = booking(studentId = 555L)
        coEvery { getBookings(any()) } returns listOf(booked)
        setPage()

        compose.onNodeWithTag(AppelliTestTags.item(booked.identityKey())).performClick()
        compose.waitForIdle()
        compose.waitUntil(timeoutMillis = 5000) {
            compose.onAllNodesWithTag(AppelliTestTags.DETAIL_CANCEL_BUTTON)
                .fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag(AppelliTestTags.DETAIL_CANCEL_BUTTON).performScrollTo().performClick()
        compose.waitForIdle()
        compose.waitUntil(timeoutMillis = 5000) {
            compose.onAllNodesWithTag(AppelliTestTags.CANCEL_CONFIRM)
                .fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag(AppelliTestTags.CANCEL_CONFIRM).performClick()
        compose.waitForIdle()

        coVerify { cancelBooking(careerId, booked.key, 555L) }
    }

    private fun booking(studentId: Long?): BookedExam = BookedExam(
        key = ExamCallKey(courseOfStudyId = 1L, activityId = 2L, callId = 3),
        applicationListId = 9000L,
        studentId = studentId,
        activityChoiceId = 99L,
        activityDescription = "Algoritmi",
        examCallDescription = "Appello I",
        examType = ExamType.Written,
        callType = ExamCallType.Final,
        examDateTime = LocalDateTime.of(2030, 6, 11, 9, 30),
        classroomDescription = "U9-16",
        buildingDescription = "U9",
        credits = 6f,
        examModeDescription = "Scritto",
        position = 1,
        bookingDate = LocalDateTime.of(2030, 6, 1, 10, 0),
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
