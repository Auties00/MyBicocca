package it.attendance100.mybicocca.ui.screen.registry

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.exam.AcceptExamResultUseCase
import it.attendance100.mybicocca.domain.usecase.exam.CancelBookingUseCase
import it.attendance100.mybicocca.domain.usecase.exam.GetBookingSlipUseCase
import it.attendance100.mybicocca.domain.usecase.exam.GetBookingsUseCase
import it.attendance100.mybicocca.domain.usecase.exam.GetExamCallsUseCase
import it.attendance100.mybicocca.domain.usecase.exam.GetExamResultsUseCase
import it.attendance100.mybicocca.domain.usecase.exam.GetPresenceCertificateUseCase
import it.attendance100.mybicocca.domain.usecase.exam.RejectExamResultUseCase
import it.attendance100.mybicocca.domain.usecase.tax.GetIseeDeclarationsUseCase
import it.attendance100.mybicocca.domain.usecase.tax.GetPagoPaNoticeUseCase
import it.attendance100.mybicocca.domain.usecase.tax.GetPagoPaReceiptUseCase
import it.attendance100.mybicocca.domain.usecase.tax.GetPaymentStatusUseCase
import it.attendance100.mybicocca.domain.usecase.tax.GetTaxInvoicesUseCase
import it.attendance100.mybicocca.domain.usecase.tax.StartPagoPaPaymentUseCase
import it.attendance100.mybicocca.testing.setBicoccaContent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.BookedExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.BookableExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults.ExamResultsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.TaxesViewModel
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/**
 * State + behaviour coverage for the Registry (Segreterie) tab landing: the always-static service
 * directory grid (one tile per Esse3 service, tagged by service id) and the pinned "Scadenze"
 * banner over it. The four feature ViewModels feeding the banner summary are real
 * [BookedExamsViewModel] / [BookableExamsViewModel] / [TaxesViewModel] / [ExamResultsViewModel]
 * instances built over MockK-faked use cases that resolve to empty lists, so the directory renders
 * without driving any sub-sheet that would mount a `hiltViewModel` child. Behaviour tests tap a
 * tagged directory tile and verify the matching `onOpen*` callback spy fires. Anchored on
 * [RegistryTestTags] and rendered via [setBicoccaContent], which installs the app-wide
 * CompositionLocals (haptics, snackbar controller, device type) the directory tiles assume.
 */
@RunWith(AndroidJUnit4::class)
class RegistryScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val careerId = CareerId(101L)

    private val getBookings: GetBookingsUseCase = mockk()
    private val cancelBooking: CancelBookingUseCase = mockk(relaxed = true)
    private val getBookingSlip: GetBookingSlipUseCase = mockk()
    private val getPresenceCertificate: GetPresenceCertificateUseCase = mockk()
    private val getExamCalls: GetExamCallsUseCase = mockk()
    private val getTaxInvoices: GetTaxInvoicesUseCase = mockk()
    private val getIseeDeclarations: GetIseeDeclarationsUseCase = mockk()
    private val startPagoPaPayment: StartPagoPaPaymentUseCase = mockk()
    private val getPagoPaNotice: GetPagoPaNoticeUseCase = mockk()
    private val getPagoPaReceipt: GetPagoPaReceiptUseCase = mockk()
    private val getPaymentStatus: GetPaymentStatusUseCase = mockk()
    private val getExamResults: GetExamResultsUseCase = mockk()
    private val acceptExamResult: AcceptExamResultUseCase = mockk(relaxed = true)
    private val rejectExamResult: RejectExamResultUseCase = mockk(relaxed = true)
    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()

    private val onOpenAppelli: () -> Unit = mockk(relaxed = true)
    private val onOpenTaxes: () -> Unit = mockk(relaxed = true)
    private val onOpenIsee: () -> Unit = mockk(relaxed = true)
    private val onOpenRefunds: () -> Unit = mockk(relaxed = true)
    private val onOpenExamResults: () -> Unit = mockk(relaxed = true)
    private val onOpenStudyPlan: () -> Unit = mockk(relaxed = true)
    private val onOpenQuestionnaires: () -> Unit = mockk(relaxed = true)
    private val onOpenAppointments: () -> Unit = mockk(relaxed = true)
    private val onOpenLibrary: () -> Unit = mockk(relaxed = true)
    private val onOpenAttendance: () -> Unit = mockk(relaxed = true)
    private val onOpenEnrollments: () -> Unit = mockk(relaxed = true)
    private val onOpenTitles: () -> Unit = mockk(relaxed = true)
    private val onOpenCertificates: () -> Unit = mockk(relaxed = true)

    private fun setRegistryScreen() {
        every { observeActiveAccount() } returns flowOf(account())
        coEvery { getBookings(careerId) } returns emptyList()
        coEvery { getExamCalls(careerId) } returns emptyList()
        coEvery { getTaxInvoices(careerId) } returns emptyList()
        coEvery { getIseeDeclarations(careerId) } returns emptyList()
        coEvery { getExamResults(careerId) } returns emptyList()

        val bookedExamsViewModel = BookedExamsViewModel(
            getBookings, cancelBooking, getBookingSlip, getPresenceCertificate, observeActiveAccount,
        )
        val bookableExamsViewModel = BookableExamsViewModel(getExamCalls, observeActiveAccount)
        val taxesViewModel = TaxesViewModel(
            getTaxInvoices, getIseeDeclarations, startPagoPaPayment,
            getPagoPaNotice, getPagoPaReceipt, getPaymentStatus, observeActiveAccount,
        )
        val examResultsViewModel = ExamResultsViewModel(
            getExamResults, acceptExamResult, rejectExamResult, observeActiveAccount,
        )

        compose.setBicoccaContent {
            RegistryScreen(
                bookedExamsViewModel = bookedExamsViewModel,
                bookableExamsViewModel = bookableExamsViewModel,
                taxesViewModel = taxesViewModel,
                examResultsViewModel = examResultsViewModel,
                onOpenAppelli = onOpenAppelli,
                onOpenTaxes = onOpenTaxes,
                onOpenIsee = onOpenIsee,
                onOpenRefunds = onOpenRefunds,
                onOpenExamResults = onOpenExamResults,
                onOpenStudyPlan = onOpenStudyPlan,
                onOpenQuestionnaires = onOpenQuestionnaires,
                onOpenAppointments = onOpenAppointments,
                onOpenLibrary = onOpenLibrary,
                onOpenAttendance = onOpenAttendance,
                onOpenEnrollments = onOpenEnrollments,
                onOpenTitles = onOpenTitles,
                onOpenCertificates = onOpenCertificates,
            )
        }
    }

    @Test
    fun the_scadenze_banner_and_the_service_grid_render() {
        setRegistryScreen()

        compose.onNodeWithTag(RegistryTestTags.ROOT).assertIsDisplayed()
        compose.onNodeWithTag(RegistryTestTags.SCADENZE_HEADER).assertIsDisplayed()
        compose.onNodeWithTag(RegistryTestTags.SERVICES).assertIsDisplayed()
    }

    @Test
    fun every_directory_group_exposes_a_tile_per_service() {
        setRegistryScreen()

        compose.onNodeWithTag(RegistryTestTags.service("study_plan")).performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag(RegistryTestTags.service("appelli")).performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag(RegistryTestTags.service("enrollments")).performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag(RegistryTestTags.service("taxes")).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun tapping_the_appelli_tile_invokes_onOpenAppelli() {
        setRegistryScreen()

        compose.onNodeWithTag(RegistryTestTags.service("appelli")).performScrollTo().performClick()
        compose.waitForIdle()

        verify { onOpenAppelli() }
    }

    @Test
    fun tapping_the_taxes_tile_invokes_onOpenTaxes() {
        setRegistryScreen()

        compose.onNodeWithTag(RegistryTestTags.service("taxes")).performScrollTo().performClick()
        compose.waitForIdle()

        verify { onOpenTaxes() }
    }

    @Test
    fun tapping_the_study_plan_tile_invokes_onOpenStudyPlan() {
        setRegistryScreen()

        compose.onNodeWithTag(RegistryTestTags.service("study_plan")).performScrollTo().performClick()
        compose.waitForIdle()

        verify { onOpenStudyPlan() }
    }

    @Test
    fun tapping_the_certificates_tile_invokes_onOpenCertificates() {
        setRegistryScreen()

        compose.waitUntil(timeoutMillis = 5000) {
            compose.onAllNodesWithTag(RegistryTestTags.service("certificates"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        compose.onNodeWithTag(RegistryTestTags.service("certificates")).performScrollTo().performClick()
        compose.waitForIdle()

        verify { onOpenCertificates() }
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
