package it.attendance100.mybicocca.domain.model.tax

import java.time.LocalDate

/**
 * Live pagoPA outcome for an invoice, read from its latest pagoPA transaction on Esse3. Backs
 * the on-demand "verifica stato pagamento" action of the registry "Tasse" sub-screen and is
 * richer than the invoice's own paid/expired flags.
 *
 * @property invoiceId Invoice the transaction belongs to.
 * @property outcome Coarse transaction state.
 * @property description Human-readable outcome description from pagoPA, when any.
 * @property paid Whether the transaction settled the invoice.
 * @property paymentDate Date the payment went through.
 * @property paidAmount Amount paid in euro.
 * @property receiptPrintable Whether a payment receipt ("quietanza") PDF can be downloaded.
 */
data class PaymentStatus(
    val invoiceId: InvoiceId,
    val outcome: PaymentOutcome,
    val description: String?,
    val paid: Boolean,
    val paymentDate: LocalDate?,
    val paidAmount: Double?,
    val receiptPrintable: Boolean,
)

/** Coarse state of an invoice's latest pagoPA transaction. */
enum class PaymentOutcome { Completed, Pending, Failed, Unknown }
