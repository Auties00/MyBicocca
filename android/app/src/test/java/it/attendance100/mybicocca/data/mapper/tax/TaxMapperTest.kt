package it.attendance100.mybicocca.data.mapper.tax

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3EnrollmentForTuition
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Invoices
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Refunds
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3StudentDebit
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TrafficLight
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Transaction
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TuitionFees
import it.attendance100.mybicocca.domain.model.tax.InvoiceId
import it.attendance100.mybicocca.domain.model.tax.PaymentOutcome
import it.attendance100.mybicocca.domain.model.tax.TaxLight
import it.attendance100.mybicocca.domain.model.tax.TaxStatus
import org.junit.Test
import java.time.LocalDate

/**
 * Covers the live Esse3 -> domain tax mappers: invoice join and title fallback, traffic-light
 * literals, pagoPA payment-outcome derivation (paid flags not trusted loosely), refund amount
 * and description fallbacks, the ISEE 99_999_999 not-declared sentinel, and invoice status flag
 * precedence.
 */
class TaxMapperTest {

    @Test
    fun `mapInvoices joins charges by invoice id and titles from first charge`() {
        val invoice = Esse3Invoices(invoiceId = 10L, invoiceAmount = 120.0, academicYearId = 2023L)
        val charge = Esse3StudentDebit(
            invoiceId = 10L,
            taxDescription = "Prima rata",
            itemAmount = 120.0,
        )

        val result = mapInvoices(listOf(invoice), listOf(charge))

        assertThat(result).hasSize(1)
        val mapped = result.first()
        assertThat(mapped.id).isEqualTo(InvoiceId(10L))
        assertThat(mapped.title).isEqualTo("Prima rata")
        assertThat(mapped.amount).isEqualTo(120.0)
        assertThat(mapped.items).hasSize(1)
        assertThat(mapped.items.first().description).isEqualTo("Prima rata")
    }

    @Test
    fun `mapInvoices drops invoices without an id`() {
        val invoice = Esse3Invoices(invoiceId = null)
        assertThat(mapInvoices(listOf(invoice), emptyList())).isEmpty()
    }

    @Test
    fun `mapInvoices falls back title to MAV description when no charges`() {
        val invoice = Esse3Invoices(invoiceId = 5L, mav1Description = "Avviso pagamento")
        assertThat(mapInvoices(listOf(invoice), emptyList()).first().title).isEqualTo("Avviso pagamento")
    }

    @Test
    fun `mapInvoices synthesizes academic-year title when no description present`() {
        val invoice = Esse3Invoices(invoiceId = 7L, academicYearId = 2023L)
        assertThat(mapInvoices(listOf(invoice), emptyList()).first().title).isEqualTo("Tasse a.a. 2023/24")
    }

    @Test
    fun `mapInvoices synthesizes a Fattura title when nothing else available`() {
        val invoice = Esse3Invoices(invoiceId = 99L)
        assertThat(mapInvoices(listOf(invoice), emptyList()).first().title).isEqualTo("Fattura 99")
    }

    @Test
    fun `mapInvoices defaults amount to zero when absent`() {
        val invoice = Esse3Invoices(invoiceId = 1L, invoiceAmount = null)
        assertThat(mapInvoices(listOf(invoice), emptyList()).first().amount).isEqualTo(0.0)
    }

    @Test
    fun `invoice status is canceled when fattAnnullata is non-zero even if paid`() {
        val invoice = Esse3Invoices(invoiceId = 1L, canceledInvoice = 1L, paidFlag = 1)
        assertThat(mapInvoices(listOf(invoice), emptyList()).first().status).isEqualTo(TaxStatus.CANCELED)
    }

    @Test
    fun `invoice status canceled flag of zero does not count as canceled`() {
        val invoice = Esse3Invoices(invoiceId = 1L, canceledInvoice = 0L, paidFlag = 1)
        assertThat(mapInvoices(listOf(invoice), emptyList()).first().status).isEqualTo(TaxStatus.PAID)
    }

    @Test
    fun `invoice status is expired when not paid and expired flag set`() {
        val invoice = Esse3Invoices(invoiceId = 1L, expiredInvoiceFlag = 1)
        assertThat(mapInvoices(listOf(invoice), emptyList()).first().status).isEqualTo(TaxStatus.EXPIRED)
    }

    @Test
    fun `invoice status defaults to pending`() {
        val invoice = Esse3Invoices(invoiceId = 1L)
        assertThat(mapInvoices(listOf(invoice), emptyList()).first().status).isEqualTo(TaxStatus.PENDING)
    }

    @Test
    fun `invoice pagoPa flags map from one-valued ints`() {
        val invoice = Esse3Invoices(
            invoiceId = 1L,
            pagopaEnabled = 1,
            pagopaImmediate = 1,
            pagopaNotice = 0,
        )
        val mapped = mapInvoices(listOf(invoice), emptyList()).first()
        assertThat(mapped.pagoPaEnabled).isTrue()
        assertThat(mapped.pagoPaImmediate).isTrue()
        assertThat(mapped.pagoPaNotice).isFalse()
    }

    @Test
    fun `charge item drops rows without any description`() {
        val invoice = Esse3Invoices(invoiceId = 1L)
        val charge = Esse3StudentDebit(invoiceId = 1L, taxDescription = null, itemDescription = null)
        assertThat(mapInvoices(listOf(invoice), listOf(charge)).first().items).isEmpty()
    }

    @Test
    fun `charge item falls back to item description and item expiration`() {
        val invoice = Esse3Invoices(invoiceId = 1L)
        val charge = Esse3StudentDebit(
            invoiceId = 1L,
            taxDescription = null,
            itemDescription = "Voce generica",
            itemAmount = 50.0,
            chargeExpiration = null,
            invoiceExpiration = "31/12/2024",
        )
        val item = mapInvoices(listOf(invoice), listOf(charge)).first().items.first()
        assertThat(item.description).isEqualTo("Voce generica")
        assertThat(item.amount).isEqualTo(50.0)
        assertThat(item.expiration).isEqualTo(LocalDate.of(2024, 12, 31))
    }

    @Test
    fun `toSummary maps the VERDE GIALLO ROSSO literals`() {
        assertThat(Esse3TrafficLight(trafficLight = "VERDE").toSummary().light).isEqualTo(TaxLight.GREEN)
        assertThat(Esse3TrafficLight(trafficLight = "giallo").toSummary().light).isEqualTo(TaxLight.YELLOW)
        assertThat(Esse3TrafficLight(trafficLight = "ROSSO").toSummary().light).isEqualTo(TaxLight.RED)
    }

    @Test
    fun `toSummary maps unknown and null light to UNKNOWN`() {
        assertThat(Esse3TrafficLight(trafficLight = null).toSummary().light).isEqualTo(TaxLight.UNKNOWN)
        assertThat(Esse3TrafficLight(trafficLight = "BLU").toSummary().light).isEqualTo(TaxLight.UNKNOWN)
    }

    @Test
    fun `toSummary counts expired and due taxes and defaults due amount to zero`() {
        val summary = Esse3TrafficLight(
            trafficLight = "ROSSO",
            dueAmount = null,
            expiredTaxes = listOf(Esse3TuitionFees(invoiceId = 1L), Esse3TuitionFees(invoiceId = 2L)),
            dueTaxes = listOf(Esse3TuitionFees(invoiceId = 3L)),
        ).toSummary()
        assertThat(summary.dueAmount).isEqualTo(0.0)
        assertThat(summary.expiredCount).isEqualTo(2)
        assertThat(summary.dueCount).isEqualTo(1)
    }

    @Test
    fun `payment status completed when paid flag set`() {
        val status = Esse3Transaction(paidFlag = 1).toPaymentStatus(InvoiceId(1L))
        assertThat(status.outcome).isEqualTo(PaymentOutcome.Completed)
        assertThat(status.paid).isTrue()
    }

    @Test
    fun `payment status completed when transaction outcome is PAGAMENTO_ESEGUITO`() {
        val status = Esse3Transaction(transactionOutcome = "PAGAMENTO_ESEGUITO").toPaymentStatus(InvoiceId(1L))
        assertThat(status.outcome).isEqualTo(PaymentOutcome.Completed)
        assertThat(status.paid).isFalse()
    }

    @Test
    fun `payment status completed when outcome code is OK`() {
        val status = Esse3Transaction(outcomeCode = "ok").toPaymentStatus(InvoiceId(1L))
        assertThat(status.outcome).isEqualTo(PaymentOutcome.Completed)
    }

    @Test
    fun `payment status pending when not final`() {
        val status = Esse3Transaction(finalState = 0).toPaymentStatus(InvoiceId(1L))
        assertThat(status.outcome).isEqualTo(PaymentOutcome.Pending)
    }

    @Test
    fun `payment status pending when outcome mentions in-corso`() {
        val status = Esse3Transaction(finalState = 1, transactionOutcome = "IN CORSO").toPaymentStatus(InvoiceId(1L))
        assertThat(status.outcome).isEqualTo(PaymentOutcome.Pending)
    }

    @Test
    fun `payment status failed when final without success`() {
        val status = Esse3Transaction(finalState = 1, transactionOutcome = "RIFIUTATO").toPaymentStatus(InvoiceId(1L))
        assertThat(status.outcome).isEqualTo(PaymentOutcome.Failed)
    }

    @Test
    fun `payment status unknown when neither final nor pending`() {
        val status = Esse3Transaction(finalState = null).toPaymentStatus(InvoiceId(1L))
        assertThat(status.outcome).isEqualTo(PaymentOutcome.Unknown)
    }

    @Test
    fun `payment status carries through receipt printable and blank description`() {
        val status = Esse3Transaction(
            outcomeDescription = "   ",
            printableReceipt = 1,
            paymentDate = "10/01/2025",
            paidAmount = 200.0,
        ).toPaymentStatus(InvoiceId(42L))
        assertThat(status.description).isNull()
        assertThat(status.receiptPrintable).isTrue()
        assertThat(status.paymentDate).isEqualTo(LocalDate.of(2025, 1, 10))
        assertThat(status.paidAmount).isEqualTo(200.0)
        assertThat(status.invoiceId).isEqualTo(InvoiceId(42L))
    }

    @Test
    fun `refund prefers invoice amount over paid amount`() {
        val refund = Esse3Refunds(invoiceId = 1L, invoiceAmount = 100.0, paidAmount = 80.0).toRefund()
        assertThat(refund.amount).isEqualTo(100.0)
    }

    @Test
    fun `refund falls back to paid amount when invoice amount absent`() {
        val refund = Esse3Refunds(invoiceId = 1L, invoiceAmount = null, paidAmount = 80.0).toRefund()
        assertThat(refund.amount).isEqualTo(80.0)
    }

    @Test
    fun `refund description falls back across the two MAV descriptions`() {
        assertThat(Esse3Refunds(mav1Description = "Mav1").toRefund().description).isEqualTo("Mav1")
        assertThat(Esse3Refunds(mav1Description = null, mav2Description = "Mav2").toRefund().description)
            .isEqualTo("Mav2")
        assertThat(Esse3Refunds(mav1Description = " ", mav2Description = "Mav2").toRefund().description)
            .isNull()
    }

    @Test
    fun `refund maps refunded flag and academic year and dates`() {
        val refund = Esse3Refunds(
            invoiceId = 9L,
            academicYearId = 2024L,
            refundedFlag = 1,
            issuanceDate = "01/02/2025",
            creditDate = "05/02/2025",
        ).toRefund()
        assertThat(refund.refunded).isTrue()
        assertThat(refund.academicYear).isEqualTo(2024)
        assertThat(refund.issueDate).isEqualTo(LocalDate.of(2025, 2, 1))
        assertThat(refund.creditDate).isEqualTo(LocalDate.of(2025, 2, 5))
    }

    @Test
    fun `isee declaration erases the not-declared sentinel`() {
        val declaration = Esse3EnrollmentForTuition(isee = 99_999_999.0).toIseeDeclaration()
        assertThat(declaration.isee).isNull()
    }

    @Test
    fun `isee declaration keeps a real value below the sentinel`() {
        val declaration = Esse3EnrollmentForTuition(
            isee = 18_500.0,
            iseeThreshold = 23_000.0,
            academicYearEnrollmentId = 3L,
            courseOfStudyDescription = "Informatica",
            description = "Esonero parziale",
        ).toIseeDeclaration()
        assertThat(declaration.isee).isEqualTo(18_500.0)
        assertThat(declaration.iseeThreshold).isEqualTo(23_000.0)
        assertThat(declaration.academicYearEnrollmentId).isEqualTo(3L)
        assertThat(declaration.courseDescription).isEqualTo("Informatica")
        assertThat(declaration.exemptionDescription).isEqualTo("Esonero parziale")
    }

    @Test
    fun `isee declaration keeps null when isee absent`() {
        assertThat(Esse3EnrollmentForTuition(isee = null).toIseeDeclaration().isee).isNull()
    }
}
