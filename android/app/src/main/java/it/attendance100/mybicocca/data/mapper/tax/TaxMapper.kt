package it.attendance100.mybicocca.data.mapper.tax

import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3EnrollmentForTuition
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Invoices
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Refunds
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3StudentDebit
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TrafficLight
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Transaction
import it.attendance100.mybicocca.domain.model.tax.InvoiceId
import it.attendance100.mybicocca.domain.model.tax.IseeDeclaration
import it.attendance100.mybicocca.domain.model.tax.PaymentOutcome
import it.attendance100.mybicocca.domain.model.tax.PaymentStatus
import it.attendance100.mybicocca.domain.model.tax.Refund
import it.attendance100.mybicocca.domain.model.tax.TaxChargeItem
import it.attendance100.mybicocca.domain.model.tax.TaxInvoice
import it.attendance100.mybicocca.domain.model.tax.TaxLight
import it.attendance100.mybicocca.domain.model.tax.TaxStatus
import it.attendance100.mybicocca.domain.model.tax.TaxSummary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Invoices (lista-fatture) carry the payable amount + pagoPA flags; the per-voce
// breakdown lives in the student charges (addebiti-studente), joined here by invoiceId.
internal fun mapInvoices(
    invoices: List<Esse3Invoices>,
    charges: List<Esse3StudentDebit>,
): List<TaxInvoice> {
    val itemsByInvoice = charges.groupBy { it.invoiceId }
    return invoices.mapNotNull { invoice ->
        invoice.toDomain(itemsByInvoice[invoice.invoiceId].orEmpty())
    }
}

internal fun Esse3TrafficLight.toSummary(): TaxSummary = TaxSummary(
    light = when (trafficLight?.uppercase()) {
        "VERDE" -> TaxLight.GREEN
        "GIALLO" -> TaxLight.YELLOW
        "ROSSO" -> TaxLight.RED
        else -> TaxLight.UNKNOWN
    },
    dueAmount = dueAmount ?: 0.0,
    expiredCount = expiredTaxes.size,
    dueCount = dueTaxes.size,
)

internal fun Esse3Transaction.toPaymentStatus(invoiceId: InvoiceId): PaymentStatus {
    val completed = paidFlag == 1 ||
        transactionOutcome.equals("PAGAMENTO_ESEGUITO", ignoreCase = true) ||
        outcomeCode.equals("OK", ignoreCase = true)
    val pending = !completed && (
        finalState == 0 ||
            transactionOutcome?.contains("CORSO", ignoreCase = true) == true ||
            transactionOutcome?.contains("PEND", ignoreCase = true) == true
        )
    val outcome = when {
        completed -> PaymentOutcome.Completed
        pending -> PaymentOutcome.Pending
        finalState == 1 -> PaymentOutcome.Failed
        else -> PaymentOutcome.Unknown
    }
    return PaymentStatus(
        invoiceId = invoiceId,
        outcome = outcome,
        description = outcomeDescription?.takeIf { it.isNotBlank() },
        paid = paidFlag == 1,
        paymentDate = paymentDate.toEsse3LocalDate(),
        paidAmount = paidAmount,
        receiptPrintable = printableReceipt == 1,
    )
}

internal fun Esse3Refunds.toRefund(): Refund = Refund(
    invoiceId = invoiceId,
    academicYear = academicYearId?.toInt(),
    amount = invoiceAmount ?: paidAmount,
    reasonCode = refundReasonCode?.takeIf { it.isNotBlank() },
    mandateNumber = refundMandateNumber?.takeIf { it.isNotBlank() },
    refunded = refundedFlag == 1,
    note = refundNote?.takeIf { it.isNotBlank() },
    issueDate = issuanceDate.toEsse3LocalDate(),
    paymentDate = paymentDate.toEsse3LocalDate(),
    creditDate = creditDate.toEsse3LocalDate(),
)

internal fun Esse3EnrollmentForTuition.toIseeDeclaration(): IseeDeclaration = IseeDeclaration(
    academicYearEnrollmentId = academicYearEnrollmentId,
    courseDescription = courseOfStudyDescription,
    isee = isee,
    iseeThreshold = iseeThreshold,
    exemptionDescription = description,
)

private fun Esse3Invoices.toDomain(rawItems: List<Esse3StudentDebit>): TaxInvoice? {
    val id = invoiceId ?: return null
    val items = rawItems.mapNotNull { it.toChargeItem() }
    val title = items.firstOrNull()?.description?.takeIf { it.isNotBlank() }
        ?: mav1Description?.takeIf { it.isNotBlank() }
        ?: academicYearId?.let { "Tasse a.a. $it/${(it + 1) % 100}" }
        ?: "Fattura $id"
    return TaxInvoice(
        id = InvoiceId(id),
        academicYear = academicYearId?.toInt(),
        title = title,
        amount = invoiceAmount ?: 0.0,
        paidAmount = paidAmount,
        status = invoiceStatus(),
        issueDate = issuanceDate.toEsse3LocalDate(),
        expiration = invoiceExpiration.toEsse3LocalDate(),
        paymentDate = paymentDate.toEsse3LocalDate(),
        pagoPaEnabled = pagopaEnabled == 1,
        pagoPaImmediate = pagopaImmediate == 1,
        pagoPaNotice = pagopaNotice == 1,
        iuv = iuv,
        noticeCode = noticeCode,
        items = items,
    )
}

private fun Esse3Invoices.invoiceStatus(): TaxStatus = when {
    canceledInvoice != null && canceledInvoice != 0L -> TaxStatus.CANCELED
    paidFlag == 1 -> TaxStatus.PAID
    expiredInvoiceFlag == 1 -> TaxStatus.EXPIRED
    else -> TaxStatus.PENDING
}

private fun Esse3StudentDebit.toChargeItem(): TaxChargeItem? {
    val description = taxDescription ?: itemDescription ?: return null
    return TaxChargeItem(
        description = description,
        amount = itemAmount ?: 0.0,
        installmentDescription = installmentDescription,
        expiration = (chargeExpiration ?: invoiceExpiration).toEsse3LocalDate(),
    )
}

// Esse3 returns dates as dd/MM/yyyy (sometimes with a time part) and occasionally ISO;
// be tolerant and return null on anything unparseable rather than throwing.
private val ESSE3_DATE_PATTERNS = listOf(
    DateTimeFormatter.ofPattern("dd/MM/yyyy"),
    DateTimeFormatter.ISO_LOCAL_DATE,
)

private fun String?.toEsse3LocalDate(): LocalDate? {
    val raw = this?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val datePart = raw.substringBefore('T').substringBefore(' ')
    for (formatter in ESSE3_DATE_PATTERNS) {
        runCatching { return LocalDate.parse(datePart, formatter) }
    }
    return null
}
