package it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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
import it.attendance100.mybicocca.domain.model.tax.InvoiceId
import it.attendance100.mybicocca.domain.model.tax.TaxInvoice
import it.attendance100.mybicocca.domain.model.tax.TaxStatus
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.tax.GetIseeDeclarationsUseCase
import it.attendance100.mybicocca.domain.usecase.tax.GetPagoPaNoticeUseCase
import it.attendance100.mybicocca.domain.usecase.tax.GetPagoPaReceiptUseCase
import it.attendance100.mybicocca.domain.usecase.tax.GetPaymentStatusUseCase
import it.attendance100.mybicocca.domain.usecase.tax.GetTaxInvoicesUseCase
import it.attendance100.mybicocca.domain.usecase.tax.StartPagoPaPaymentUseCase
import it.attendance100.mybicocca.testing.setBicoccaContent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.Instant
import java.time.LocalDate

/**
 * State and behaviour coverage for the Tasse sheet body. The screen is driven by a real
 * [TaxesViewModel] over MockK-faked use cases (the Wave-1 construction), the invoice fetch stubbed
 * to the snapshot each test needs. State tests assert exactly one [TaxesTestTags] state marker per
 * Loadable/SyncStatus; behaviour tests open a fattura's hero detail and drive its pagoPA pay action
 * down to [TaxesViewModel.payInvoice]. Content is wrapped via `setBicoccaContent`, which installs the
 * app-wide CompositionLocals (HapticManager, AppSnackbar controller, DeviceType) the rendered cards
 * and segmented switch read, plus the production BicoccaTheme.
 */
@RunWith(AndroidJUnit4::class)
class TaxesPageTest {

    @get:Rule
    val compose = createComposeRule()

    private val careerId = CareerId(101L)
    private val invoiceId = InvoiceId(42L)

    private val getTaxInvoices: GetTaxInvoicesUseCase = mockk()
    private val getIseeDeclarations: GetIseeDeclarationsUseCase = mockk(relaxed = true)
    private val startPagoPaPayment: StartPagoPaPaymentUseCase = mockk()
    private val getPagoPaNotice: GetPagoPaNoticeUseCase = mockk()
    private val getPagoPaReceipt: GetPagoPaReceiptUseCase = mockk()
    private val getPaymentStatus: GetPaymentStatusUseCase = mockk()
    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()

    private fun viewModel(): TaxesViewModel {
        every { observeActiveAccount() } returns flowOf(account(careerId))
        return TaxesViewModel(
            getTaxInvoices,
            getIseeDeclarations,
            startPagoPaPayment,
            getPagoPaNotice,
            getPagoPaReceipt,
            getPaymentStatus,
            observeActiveAccount,
        )
    }

    private fun setScreen(vm: TaxesViewModel) {
        compose.setBicoccaContent {
            TaxesPage(viewModel = vm)
        }
        compose.waitForIdle()
    }

    @Test
    fun a_never_completing_fetch_keeps_the_loading_marker_on_screen() {
        coEvery { getTaxInvoices(careerId) } coAnswers { CompletableDeferred<List<TaxInvoice>>().await() }
        setScreen(viewModel())

        compose.onNodeWithTag(TaxesTestTags.ROOT).assertIsDisplayed()
        compose.onNodeWithTag(TaxesTestTags.STATE_LOADING).assertIsDisplayed()
    }

    @Test
    fun loaded_invoices_render_the_pager_content_and_the_fattura_card() {
        coEvery { getTaxInvoices(careerId) } returns listOf(invoice())
        setScreen(viewModel())

        compose.onNodeWithTag(TaxesTestTags.STATE_CONTENT).assertIsDisplayed()
        compose.onNodeWithTag(TaxesTestTags.FILTER_SWITCH).assertIsDisplayed()
        compose.onNodeWithTag(TaxesTestTags.invoiceCard(invoiceId.value)).assertIsDisplayed()
    }

    @Test
    fun an_empty_career_renders_the_empty_marker() {
        coEvery { getTaxInvoices(careerId) } returns emptyList()
        setScreen(viewModel())

        compose.onNodeWithTag(TaxesTestTags.STATE_EMPTY).assertIsDisplayed()
    }

    @Test
    fun a_fetch_failure_renders_the_error_marker() {
        coEvery { getTaxInvoices(careerId) } throws IOException("offline")
        setScreen(viewModel())

        compose.onNodeWithTag(TaxesTestTags.STATE_ERROR).assertIsDisplayed()
    }

    @Test
    fun tapping_a_fattura_opens_its_detail_and_paying_with_pagoPA_invokes_the_view_model() {
        coEvery { getTaxInvoices(careerId) } returns listOf(invoice())
        coEvery { startPagoPaPayment(careerId, invoiceId, any()) } returns "https://checkout"
        setScreen(viewModel())

        compose.onNodeWithTag(TaxesTestTags.invoiceCard(invoiceId.value)).performClick()

        compose.waitUntil(timeoutMillis = 5000) {
            compose.onAllNodesWithTag(TaxesTestTags.DETAIL_ROOT).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag(TaxesTestTags.DETAIL_ROOT).assertExists()

        compose.waitUntil(timeoutMillis = 5000) {
            compose.onAllNodesWithTag(TaxesTestTags.DETAIL_PAY_BUTTON).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag(TaxesTestTags.DETAIL_PAY_BUTTON).performClick()
        compose.waitForIdle()

        coVerify { startPagoPaPayment(careerId, invoiceId, any()) }
    }

    private fun invoice(): TaxInvoice = TaxInvoice(
        id = invoiceId,
        academicYear = 2025,
        title = "Prima rata",
        amount = 156.5,
        paidAmount = null,
        status = TaxStatus.PENDING,
        issueDate = null,
        expiration = LocalDate.of(2026, 1, 31),
        paymentDate = null,
        pagoPaEnabled = true,
        pagoPaImmediate = true,
        pagoPaNotice = true,
        iuv = null,
        noticeCode = null,
        items = emptyList(),
    )

    private fun account(careerId: CareerId): Account = Account(
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
