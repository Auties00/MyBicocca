package it.attendance100.mybicocca.data.repository

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.text.StringResolver
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.tax.IseeDeclarationEntity
import it.attendance100.mybicocca.data.local.tax.RefundEntity
import it.attendance100.mybicocca.data.local.tax.TaxCacheDao
import it.attendance100.mybicocca.data.local.tax.TaxChargeItemEntity
import it.attendance100.mybicocca.data.local.tax.TaxInvoiceEntity
import it.attendance100.mybicocca.data.local.tax.TaxSummaryEntity
import it.attendance100.mybicocca.data.remote.esse3.api.Esse3Api
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3EnrollmentForTuition
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Invoices
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PagoPATransaction
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PagoPATransactionResponse
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Refunds
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3StudentDebit
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TrafficLight
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Transaction
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.tax.InvoiceId
import it.attendance100.mybicocca.domain.model.tax.PaymentOutcome
import it.attendance100.mybicocca.domain.model.tax.TaxLight
import it.attendance100.mybicocca.domain.model.tax.TaxStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

/**
 * Behaviour coverage for the tuition-fees repository: the live-first triad on the cached
 * list/summary reads (success writes through, offline serves the mirror, offline-empty
 * rethrows) plus the deliberately uncached payment-status / pagoPA action paths, which must
 * always hit the API and never read the offline mirror.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TaxRepositoryImplTest {

    private val careerId: CareerId = RepositoryTestFixtures.careerId
    private val account: Account = RepositoryTestFixtures.account()
    private val personId: Long = account.academic.personId

    private val stringResolver = mockk<StringResolver>(relaxed = true) {
        every { getString(R.string.tax_pagopa_no_link) } returns "Impossibile recuperare il link pagoPA."
        every { getString(any()) } returns "Impossibile recuperare il link pagoPA."
    }
    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val esse3 = mockk<Esse3Api>(relaxed = true)
    private val dao = mockk<TaxCacheDao>(relaxed = true)

    private fun newRepository(): TaxRepositoryImpl {
        every { sessionManager.activeAccount } returns MutableStateFlow(account)
        coEvery { sessionManager.esse3() } returns esse3
        return TaxRepositoryImpl(stringResolver, sessionManager, dao)
    }

    private fun invoiceEntity(invoiceId: Long, order: Int) = TaxInvoiceEntity(
        careerId = careerId.value,
        invoiceId = invoiceId,
        cacheOrder = order,
        academicYear = 2024,
        title = "Tasse a.a. 2024/25",
        amount = 150.0,
        paidAmount = null,
        status = TaxStatus.PENDING.name,
        issueDate = "2024-09-01",
        expiration = "2024-10-31",
        paymentDate = null,
        pagoPaEnabled = true,
        pagoPaImmediate = false,
        pagoPaNotice = true,
        iuv = "IUV-1",
        noticeCode = "NC-1",
    )

    @Test
    fun `getInvoices success maps and writes through to the mirror`() = runTest {
        val repository = newRepository()
        coEvery { esse3.tuitionFees.getInvoicesList(personId = personId) } returns listOf(
            Esse3Invoices(invoiceId = 500L, invoiceAmount = 200.0, academicYearId = 2024L),
        )
        coEvery { esse3.tuitionFees.getStudentChargesList(studentId = careerId.value) } returns listOf(
            Esse3StudentDebit(invoiceId = 500L, taxDescription = "Prima rata", itemAmount = 200.0),
        )

        val result = repository.getInvoices(careerId)

        assertThat(result).hasSize(1)
        assertThat(result.first().id).isEqualTo(InvoiceId(500L))
        assertThat(result.first().title).isEqualTo("Prima rata")
        coVerify { dao.replaceInvoices(careerId.value, any(), any()) }
        coVerify(exactly = 0) { dao.getInvoices(any()) }
    }

    @Test
    fun `getInvoices offline serves the cached invoices rejoined with their charges`() = runTest {
        val repository = newRepository()
        coEvery { esse3.tuitionFees.getInvoicesList(personId = personId) } throws IOException("offline")
        coEvery { esse3.tuitionFees.getStudentChargesList(studentId = careerId.value) } throws IOException("offline")
        coEvery { dao.getInvoices(careerId.value) } returns listOf(invoiceEntity(500L, 0))
        coEvery { dao.getChargeItems(careerId.value) } returns listOf(
            TaxChargeItemEntity(
                careerId = careerId.value,
                invoiceId = 500L,
                itemOrder = 0,
                description = "Prima rata",
                amount = 150.0,
                installmentDescription = null,
                expiration = "2024-10-31",
            ),
        )

        val result = repository.getInvoices(careerId)

        assertThat(result).hasSize(1)
        assertThat(result.first().id).isEqualTo(InvoiceId(500L))
        assertThat(result.first().items).hasSize(1)
        assertThat(result.first().items.first().description).isEqualTo("Prima rata")
        coVerify(exactly = 0) { dao.replaceInvoices(any(), any(), any()) }
    }

    @Test
    fun `getInvoices offline with empty cache rethrows`() = runTest {
        val repository = newRepository()
        coEvery { esse3.tuitionFees.getInvoicesList(personId = personId) } throws IOException("offline")
        coEvery { esse3.tuitionFees.getStudentChargesList(studentId = careerId.value) } throws IOException("offline")
        coEvery { dao.getInvoices(careerId.value) } returns emptyList()
        coEvery { dao.getChargeItems(careerId.value) } returns emptyList()

        assertThrows(IOException::class.java) {
            runBlockingGetInvoices(repository)
        }
    }

    private fun runBlockingGetInvoices(repository: TaxRepositoryImpl) =
        kotlinx.coroutines.runBlocking { repository.getInvoices(careerId) }

    @Test
    fun `getSummary success maps the semaforo and writes through`() = runTest {
        val repository = newRepository()
        coEvery { esse3.tuitionFees.getTrafficLightParameters(studentId = careerId.value) } returns
            Esse3TrafficLight(trafficLight = "ROSSO", dueAmount = 300.0)

        val result = repository.getSummary(careerId)

        assertThat(result.light).isEqualTo(TaxLight.RED)
        assertThat(result.dueAmount).isEqualTo(300.0)
        coVerify { dao.replaceSummary(careerId.value, any()) }
    }

    @Test
    fun `getSummary offline serves the cached summary`() = runTest {
        val repository = newRepository()
        coEvery { esse3.tuitionFees.getTrafficLightParameters(studentId = careerId.value) } throws
            IOException("offline")
        coEvery { dao.getSummary(careerId.value) } returns TaxSummaryEntity(
            careerId = careerId.value,
            light = TaxLight.YELLOW.name,
            dueAmount = 50.0,
            expiredCount = 1,
            dueCount = 2,
        )

        val result = repository.getSummary(careerId)

        assertThat(result.light).isEqualTo(TaxLight.YELLOW)
        assertThat(result.dueAmount).isEqualTo(50.0)
    }

    @Test
    fun `getSummary offline with no cached row rethrows`() = runTest {
        val repository = newRepository()
        coEvery { esse3.tuitionFees.getTrafficLightParameters(studentId = careerId.value) } throws
            IOException("offline")
        coEvery { dao.getSummary(careerId.value) } returns null

        assertThrows(IOException::class.java) {
            kotlinx.coroutines.runBlocking { repository.getSummary(careerId) }
        }
    }

    @Test
    fun `getIseeDeclarations success erases the not-declared sentinel and writes through`() = runTest {
        val repository = newRepository()
        coEvery { esse3.tuitionFees.getEnrollmentsForTaxes(studentId = careerId.value) } returns listOf(
            Esse3EnrollmentForTuition(academicYearEnrollmentId = 2024L, isee = 99_999_999.0),
            Esse3EnrollmentForTuition(academicYearEnrollmentId = 2023L, isee = 12_000.0),
        )

        val result = repository.getIseeDeclarations(careerId)

        assertThat(result).hasSize(2)
        assertThat(result[0].isee).isNull()
        assertThat(result[1].isee).isEqualTo(12_000.0)
        coVerify { dao.replaceIseeDeclarations(careerId.value, any()) }
    }

    @Test
    fun `getIseeDeclarations offline serves the cached rows`() = runTest {
        val repository = newRepository()
        coEvery { esse3.tuitionFees.getEnrollmentsForTaxes(studentId = careerId.value) } throws
            IOException("offline")
        coEvery { dao.getIseeDeclarations(careerId.value) } returns listOf(
            IseeDeclarationEntity(
                careerId = careerId.value,
                cacheOrder = 0,
                academicYearEnrollmentId = 2024L,
                courseDescription = "Informatica",
                isee = 8_000.0,
                iseeThreshold = 20_000.0,
                exemptionDescription = "Fascia 1",
            ),
        )

        val result = repository.getIseeDeclarations(careerId)

        assertThat(result).hasSize(1)
        assertThat(result.first().isee).isEqualTo(8_000.0)
    }

    @Test
    fun `getIseeDeclarations offline with empty cache rethrows`() = runTest {
        val repository = newRepository()
        coEvery { esse3.tuitionFees.getEnrollmentsForTaxes(studentId = careerId.value) } throws
            IOException("offline")
        coEvery { dao.getIseeDeclarations(careerId.value) } returns emptyList()

        assertThrows(IOException::class.java) {
            kotlinx.coroutines.runBlocking { repository.getIseeDeclarations(careerId) }
        }
    }

    @Test
    fun `getRefunds success maps and writes through`() = runTest {
        val repository = newRepository()
        coEvery {
            esse3.tuitionFees.getRefundsList(personId = personId, refundedFlag = 2)
        } returns listOf(
            Esse3Refunds(invoiceId = 7L, invoiceAmount = 90.0, refundedFlag = 1),
        )

        val result = repository.getRefunds(careerId)

        assertThat(result).hasSize(1)
        assertThat(result.first().amount).isEqualTo(90.0)
        assertThat(result.first().refunded).isTrue()
        coVerify { dao.replaceRefunds(careerId.value, any()) }
    }

    @Test
    fun `getRefunds offline serves the cached rows`() = runTest {
        val repository = newRepository()
        coEvery {
            esse3.tuitionFees.getRefundsList(personId = personId, refundedFlag = 2)
        } throws IOException("offline")
        coEvery { dao.getRefunds(careerId.value) } returns listOf(
            RefundEntity(
                careerId = careerId.value,
                cacheOrder = 0,
                invoiceId = 7L,
                academicYear = 2024,
                amount = 90.0,
                description = "Rimborso",
                reasonCode = null,
                mandateNumber = null,
                refunded = true,
                note = null,
                collectedBy = null,
                issueDate = null,
                processingDate = null,
                paymentDate = null,
                creditDate = null,
            ),
        )

        val result = repository.getRefunds(careerId)

        assertThat(result).hasSize(1)
        assertThat(result.first().amount).isEqualTo(90.0)
    }

    @Test
    fun `getRefunds offline with empty cache rethrows`() = runTest {
        val repository = newRepository()
        coEvery {
            esse3.tuitionFees.getRefundsList(personId = personId, refundedFlag = 2)
        } throws IOException("offline")
        coEvery { dao.getRefunds(careerId.value) } returns emptyList()

        assertThrows(IOException::class.java) {
            kotlinx.coroutines.runBlocking { repository.getRefunds(careerId) }
        }
    }

    @Test
    fun `startPagoPaPayment returns the redirect url and never touches the cache`() = runTest {
        val repository = newRepository()
        coEvery {
            esse3.tuitionFees.postInitPagoPaTransaction(body = any())
        } returns Esse3PagoPATransactionResponse(pagopaRedirectUrl = "https://pay.example/abc")

        val url = repository.startPagoPaPayment(careerId, InvoiceId(500L), "myapp://return")

        assertThat(url).isEqualTo("https://pay.example/abc")
        coVerify {
            esse3.tuitionFees.postInitPagoPaTransaction(
                body = Esse3PagoPATransaction(invoiceId = 500L, returnURL = "myapp://return"),
            )
        }
    }

    @Test
    fun `startPagoPaPayment blank redirect url raises a descriptive error`() = runTest {
        every { stringResolver.getString(R.string.tax_pagopa_no_link) } returns "Impossibile recuperare il link pagoPA."
        val repository = newRepository()
        coEvery {
            esse3.tuitionFees.postInitPagoPaTransaction(body = any())
        } returns Esse3PagoPATransactionResponse(pagopaRedirectUrl = "   ")

        val error = assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking { repository.startPagoPaPayment(careerId, InvoiceId(1L), "u") }
        }
        assertThat(error).hasMessageThat().contains("pagoPA")
    }

    @Test
    fun `getPaymentStatus always hits the API and never reads the dao`() = runTest {
        val repository = newRepository()
        coEvery {
            esse3.tuitionFees.getPagoPATransactions(invoiceId = 500L, lastTransaction = 1)
        } returns listOf(
            Esse3Transaction(paidFlag = 1, paymentDate = "2024-10-05", paidAmount = 150.0, printableReceipt = 1),
        )

        val status = repository.getPaymentStatus(careerId, InvoiceId(500L))

        assertThat(status).isNotNull()
        assertThat(status!!.outcome).isEqualTo(PaymentOutcome.Completed)
        assertThat(status.paid).isTrue()
        assertThat(status.receiptPrintable).isTrue()
        coVerify(exactly = 0) { dao.getInvoices(any()) }
        coVerify(exactly = 0) { dao.getSummary(any()) }
    }

    @Test
    fun `getPaymentStatus returns null when there is no transaction yet`() = runTest {
        val repository = newRepository()
        coEvery {
            esse3.tuitionFees.getPagoPATransactions(invoiceId = 500L, lastTransaction = 1)
        } returns emptyList()

        val status = repository.getPaymentStatus(careerId, InvoiceId(500L))

        assertThat(status).isNull()
    }

    @Test
    fun `requireCareer rejects a career absent from the active account`() = runTest {
        val repository = newRepository()

        assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking { repository.getSummary(CareerId(999_999L)) }
        }
    }
}
