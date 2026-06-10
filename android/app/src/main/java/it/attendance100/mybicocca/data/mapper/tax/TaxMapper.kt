package it.attendance100.mybicocca.data.mapper.tax

import it.attendance100.mybicocca.data.mapper.common.parseEsse3DateOrIso
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

/**
 * Builds domain invoices from the two Esse3 sources: invoices (lista-fatture) carry the
 * payable amount + pagoPA flags; the per-voce breakdown lives in the student charges
 * (addebiti-studente), joined here by invoiceId.
 */
internal fun mapInvoices(
    invoices: List<Esse3Invoices>,
    charges: List<Esse3StudentDebit>,
): List<TaxInvoice> {
    val itemsByInvoice = charges.groupBy { it.invoiceId }
    return invoices.mapNotNull { invoice ->
        invoice.toDomain(itemsByInvoice[invoice.invoiceId].orEmpty())
    }
}

/** Maps the Esse3 semaforo payload; the VERDE/GIALLO/ROSSO literals become [TaxLight] values. */
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

/**
 * Derives the coarse [PaymentOutcome] of a pagoPA transaction: completed when the paid flag is
 * set or the outcome reads PAGAMENTO_ESEGUITO/OK, pending when the transaction is not final or
 * its outcome mentions an in-progress state, failed when final without success.
 */
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
        paymentDate = paymentDate.parseEsse3DateOrIso(),
        paidAmount = paidAmount,
        receiptPrintable = printableReceipt == 1,
    )
}

/**
 * Maps an Esse3 refund row; the amount prefers the invoice amount over the paid amount, and
 * the description falls back across the two MAV descriptions.
 */
internal fun Esse3Refunds.toRefund(): Refund = Refund(
    invoiceId = invoiceId,
    academicYear = academicYearId?.toInt(),
    amount = invoiceAmount ?: paidAmount,
    description = (mav1Description ?: mav2Description)?.takeIf { it.isNotBlank() },
    reasonCode = refundReasonCode?.takeIf { it.isNotBlank() },
    mandateNumber = refundMandateNumber?.takeIf { it.isNotBlank() },
    refunded = refundedFlag == 1,
    note = refundNote?.takeIf { it.isNotBlank() },
    collectedBy = collectedBy?.takeIf { it.isNotBlank() },
    issueDate = issuanceDate.parseEsse3DateOrIso(),
    processingDate = processingDate.parseEsse3DateOrIso(),
    paymentDate = paymentDate.parseEsse3DateOrIso(),
    creditDate = creditDate.parseEsse3DateOrIso(),
)

/**
 * Esse3 encodes "no ISEE presented" as 99999999 (the max contribution band acts as infinite
 * income), not as a missing field.
 */
private const val ISEE_NOT_DECLARED = 99_999_999.0

/**
 * Maps an enrollments-for-taxes row to its ISEE declaration; the [ISEE_NOT_DECLARED] sentinel
 * is erased so a null isee really means no declaration on file for that year.
 */
internal fun Esse3EnrollmentForTuition.toIseeDeclaration(): IseeDeclaration = IseeDeclaration(
    academicYearEnrollmentId = academicYearEnrollmentId,
    courseDescription = courseOfStudyDescription,
    isee = isee?.takeIf { it < ISEE_NOT_DECLARED },
    iseeThreshold = iseeThreshold,
    exemptionDescription = description,
)

/**
 * Maps one invoice with its joined charge lines. The display title is derived with a fallback
 * chain: first charge description, then the MAV description, then a synthetic
 * "Tasse a.a. …" / "Fattura …" label.
 */
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
        issueDate = issuanceDate.parseEsse3DateOrIso(),
        expiration = invoiceExpiration.parseEsse3DateOrIso(),
        paymentDate = paymentDate.parseEsse3DateOrIso(),
        pagoPaEnabled = pagopaEnabled == 1,
        pagoPaImmediate = pagopaImmediate == 1,
        pagoPaNotice = pagopaNotice == 1,
        iuv = iuv,
        noticeCode = noticeCode,
        items = items,
    )
}

/** Flag precedence: cancellation beats paid, paid beats expired, anything else is pending. */
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
        expiration = (chargeExpiration ?: invoiceExpiration).parseEsse3DateOrIso(),
    )
}
