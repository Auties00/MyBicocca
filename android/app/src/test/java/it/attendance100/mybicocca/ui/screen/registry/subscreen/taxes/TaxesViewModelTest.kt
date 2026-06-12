package it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes

import android.content.Context
import androidx.test.core.app.ApplicationProvider
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
import it.attendance100.mybicocca.domain.model.tax.InvoiceId
import it.attendance100.mybicocca.domain.model.tax.IseeDeclaration
import it.attendance100.mybicocca.domain.model.tax.PaymentOutcome
import it.attendance100.mybicocca.domain.model.tax.PaymentStatus
import it.attendance100.mybicocca.domain.model.tax.TaxInvoice
import it.attendance100.mybicocca.domain.model.tax.TaxStatus
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.tax.GetIseeDeclarationsUseCase
import it.attendance100.mybicocca.domain.usecase.tax.GetPagoPaNoticeUseCase
import it.attendance100.mybicocca.domain.usecase.tax.GetPagoPaReceiptUseCase
import it.attendance100.mybicocca.domain.usecase.tax.GetPaymentStatusUseCase
import it.attendance100.mybicocca.domain.usecase.tax.GetTaxInvoicesUseCase
import it.attendance100.mybicocca.domain.usecase.tax.StartPagoPaPaymentUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.state.TaxEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import java.time.Instant
import java.time.LocalDate

/**
 * State-machine coverage for the Tasse ViewModel: the parallel invoice + ISEE fetch where the
 * ISEE branch degrades to an empty Loaded on failure while invoices set Failed independently,
 * the invoice lookup, and the pagoPA action one-shot events (OpenUrl, OpenPdf with derived
 * filenames, the payment-status message produced by every PaymentStatus branch).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "it")
class TaxesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val careerId = CareerId(101L)
    private val invoiceId = InvoiceId(42L)

    private val getTaxInvoices: GetTaxInvoicesUseCase = mockk()
    private val getIseeDeclarations: GetIseeDeclarationsUseCase = mockk()
    private val startPagoPaPayment: StartPagoPaPaymentUseCase = mockk()
    private val getPagoPaNotice: GetPagoPaNoticeUseCase = mockk()
    private val getPagoPaReceipt: GetPagoPaReceiptUseCase = mockk()
    private val getPaymentStatus: GetPaymentStatusUseCase = mockk()
    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun viewModel(): TaxesViewModel {
        every { observeActiveAccount() } returns flowOf(account(careerId))
        return TaxesViewModel(
            context,
            getTaxInvoices,
            getIseeDeclarations,
            startPagoPaPayment,
            getPagoPaNotice,
            getPagoPaReceipt,
            getPaymentStatus,
            observeActiveAccount,
        )
    }

    @Test
    fun `init loads both invoices and isee and settles sync status Idle`() = runTest {
        val invoices = listOf(invoice())
        val isee = listOf(iseeDeclaration())
        coEvery { getTaxInvoices(careerId) } returns invoices
        coEvery { getIseeDeclarations(careerId) } returns isee
        val vm = viewModel()

        assertThat((vm.invoices.value as Loadable.Loaded).value).isEqualTo(invoices)
        assertThat((vm.isee.value as Loadable.Loaded).value).isEqualTo(isee)
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `isee fetch failure degrades to an empty Loaded list`() = runTest {
        coEvery { getTaxInvoices(careerId) } returns listOf(invoice())
        coEvery { getIseeDeclarations(careerId) } throws IOException("no isee")
        val vm = viewModel()

        val isee = vm.isee.value
        assertThat(isee).isInstanceOf(Loadable.Loaded::class.java)
        assertThat((isee as Loadable.Loaded).value).isEmpty()
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `invoice fetch failure sets Failed and does not blank the isee list`() = runTest {
        val boom = IOException("offline")
        coEvery { getTaxInvoices(careerId) } throws boom
        coEvery { getIseeDeclarations(careerId) } returns listOf(iseeDeclaration())
        val vm = viewModel()

        val status = vm.syncStatus.value
        assertThat(status).isInstanceOf(SyncStatus.Failed::class.java)
        assertThat((status as SyncStatus.Failed).cause).isEqualTo(boom)
        assertThat(vm.invoices.value).isEqualTo(Loadable.NotYetLoaded)
        assertThat((vm.isee.value as Loadable.Loaded).value).hasSize(1)
    }

    @Test
    fun `invoice looks up a loaded invoice by id`() = runTest {
        val target = invoice()
        coEvery { getTaxInvoices(careerId) } returns listOf(target)
        coEvery { getIseeDeclarations(careerId) } returns emptyList()
        val vm = viewModel()

        assertThat(vm.invoice(invoiceId)).isEqualTo(target)
        assertThat(vm.invoice(InvoiceId(999L))).isNull()
    }

    @Test
    fun `payInvoice emits OpenUrl with the checkout url and toggles actionInProgress`() = runTest {
        coEvery { getTaxInvoices(careerId) } returns listOf(invoice())
        coEvery { getIseeDeclarations(careerId) } returns emptyList()
        coEvery { startPagoPaPayment(careerId, invoiceId, any()) } returns "https://checkout"
        val vm = viewModel()

        vm.events.test {
            vm.payInvoice(invoiceId)
            assertThat(awaitItem()).isEqualTo(TaxEvent.OpenUrl("https://checkout"))
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { startPagoPaPayment(careerId, invoiceId, "https://s3w.si.unimib.it/esse3/") }
        assertThat(vm.actionInProgress.value).isFalse()
    }

    @Test
    fun `printNotice emits OpenPdf with the avviso filename`() = runTest {
        coEvery { getTaxInvoices(careerId) } returns listOf(invoice())
        coEvery { getIseeDeclarations(careerId) } returns emptyList()
        coEvery { getPagoPaNotice(careerId, invoiceId) } returns byteArrayOf(1)
        val vm = viewModel()

        vm.events.test {
            vm.printNotice(invoiceId)
            val event = awaitItem()
            assertThat(event).isInstanceOf(TaxEvent.OpenPdf::class.java)
            assertThat((event as TaxEvent.OpenPdf).fileName).isEqualTo("avviso_pagopa_42.pdf")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `printReceipt emits OpenPdf with the quietanza filename`() = runTest {
        coEvery { getTaxInvoices(careerId) } returns listOf(invoice())
        coEvery { getIseeDeclarations(careerId) } returns emptyList()
        coEvery { getPagoPaReceipt(careerId, invoiceId) } returns byteArrayOf(1)
        val vm = viewModel()

        vm.events.test {
            vm.printReceipt(invoiceId)
            val event = awaitItem()
            assertThat(event).isInstanceOf(TaxEvent.OpenPdf::class.java)
            assertThat((event as TaxEvent.OpenPdf).fileName).isEqualTo("quietanza_pagopa_42.pdf")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoice action failure surfaces the exception message as a ShowMessage`() = runTest {
        coEvery { getTaxInvoices(careerId) } returns listOf(invoice())
        coEvery { getIseeDeclarations(careerId) } returns emptyList()
        coEvery { getPagoPaNotice(careerId, invoiceId) } throws IllegalStateException("boom")
        val vm = viewModel()

        vm.events.test {
            vm.printNotice(invoiceId)
            assertThat(awaitItem()).isEqualTo(TaxEvent.ShowMessage("boom"))
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(vm.actionInProgress.value).isFalse()
    }

    @Test
    fun `checkPaymentStatus null status reports no transaction`() = runTest {
        coEvery { getTaxInvoices(careerId) } returns listOf(invoice())
        coEvery { getIseeDeclarations(careerId) } returns emptyList()
        coEvery { getPaymentStatus(careerId, invoiceId) } returns null
        val vm = viewModel()

        vm.events.test {
            vm.checkPaymentStatus(invoiceId)
            assertThat(awaitItem()).isEqualTo(
                TaxEvent.ShowMessage("Nessuna transazione pagoPA trovata per questa fattura."),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `checkPaymentStatus completed status joins the head with date and amount`() = runTest {
        coEvery { getTaxInvoices(careerId) } returns listOf(invoice())
        coEvery { getIseeDeclarations(careerId) } returns emptyList()
        coEvery { getPaymentStatus(careerId, invoiceId) } returns paymentStatus(
            outcome = PaymentOutcome.Completed,
            paymentDate = LocalDate.of(2026, 3, 5),
            paidAmount = 156.5,
        )
        val vm = viewModel()

        vm.events.test {
            vm.checkPaymentStatus(invoiceId)
            val event = awaitItem()
            assertThat(event).isInstanceOf(TaxEvent.ShowMessage::class.java)
            val message = (event as TaxEvent.ShowMessage).message
            assertThat(message).startsWith("Pagamento eseguito · ")
            assertThat(message).contains("5 marzo 2026")
            assertThat(message).contains("156,50")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `checkPaymentStatus pending without details shows only the head`() = runTest {
        coEvery { getTaxInvoices(careerId) } returns listOf(invoice())
        coEvery { getIseeDeclarations(careerId) } returns emptyList()
        coEvery { getPaymentStatus(careerId, invoiceId) } returns paymentStatus(
            outcome = PaymentOutcome.Pending,
            paymentDate = null,
            paidAmount = null,
        )
        val vm = viewModel()

        vm.events.test {
            vm.checkPaymentStatus(invoiceId)
            assertThat(awaitItem()).isEqualTo(TaxEvent.ShowMessage("Pagamento in corso"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `checkPaymentStatus failed outcome reports the failure head`() = runTest {
        coEvery { getTaxInvoices(careerId) } returns listOf(invoice())
        coEvery { getIseeDeclarations(careerId) } returns emptyList()
        coEvery { getPaymentStatus(careerId, invoiceId) } returns paymentStatus(
            outcome = PaymentOutcome.Failed,
            paymentDate = null,
            paidAmount = null,
        )
        val vm = viewModel()

        vm.events.test {
            vm.checkPaymentStatus(invoiceId)
            assertThat(awaitItem()).isEqualTo(TaxEvent.ShowMessage("Pagamento non riuscito"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `checkPaymentStatus unknown outcome falls back to the description`() = runTest {
        coEvery { getTaxInvoices(careerId) } returns listOf(invoice())
        coEvery { getIseeDeclarations(careerId) } returns emptyList()
        coEvery { getPaymentStatus(careerId, invoiceId) } returns paymentStatus(
            outcome = PaymentOutcome.Unknown,
            description = "In attesa di riscontro",
            paymentDate = null,
            paidAmount = null,
        )
        val vm = viewModel()

        vm.events.test {
            vm.checkPaymentStatus(invoiceId)
            assertThat(awaitItem()).isEqualTo(TaxEvent.ShowMessage("In attesa di riscontro"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `checkPaymentStatus drops a non-positive paid amount from the details`() = runTest {
        coEvery { getTaxInvoices(careerId) } returns listOf(invoice())
        coEvery { getIseeDeclarations(careerId) } returns emptyList()
        coEvery { getPaymentStatus(careerId, invoiceId) } returns paymentStatus(
            outcome = PaymentOutcome.Completed,
            paymentDate = LocalDate.of(2026, 3, 5),
            paidAmount = 0.0,
        )
        val vm = viewModel()

        vm.events.test {
            vm.checkPaymentStatus(invoiceId)
            val message = (awaitItem() as TaxEvent.ShowMessage).message
            assertThat(message).contains("5 marzo 2026")
            assertThat(message).doesNotContain("€")
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun invoice(): TaxInvoice = TaxInvoice(
        id = invoiceId,
        academicYear = 2025,
        title = "Prima rata",
        amount = 156.5,
        paidAmount = null,
        status = TaxStatus.PENDING,
        issueDate = null,
        expiration = null,
        paymentDate = null,
        pagoPaEnabled = true,
        pagoPaImmediate = true,
        pagoPaNotice = true,
        iuv = null,
        noticeCode = null,
        items = emptyList(),
    )

    private fun iseeDeclaration(): IseeDeclaration = IseeDeclaration(
        academicYearEnrollmentId = 1L,
        courseDescription = "Informatica",
        isee = 12000.0,
        iseeThreshold = 23000.0,
        exemptionDescription = null,
    )

    private fun paymentStatus(
        outcome: PaymentOutcome,
        description: String? = null,
        paymentDate: LocalDate?,
        paidAmount: Double?,
    ): PaymentStatus = PaymentStatus(
        invoiceId = invoiceId,
        outcome = outcome,
        description = description,
        paid = outcome == PaymentOutcome.Completed,
        paymentDate = paymentDate,
        paidAmount = paidAmount,
        receiptPrintable = false,
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
