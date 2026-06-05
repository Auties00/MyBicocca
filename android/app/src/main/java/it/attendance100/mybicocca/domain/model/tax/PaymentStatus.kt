package it.attendance100.mybicocca.domain.model.tax

import java.time.LocalDate

// Live pagoPA outcome for an invoice, from the latest transaction. Used by the on-demand
// "verifica stato pagamento" action — richer than the invoice's paid/expired flag.
data class PaymentStatus(
    val invoiceId: InvoiceId,
    val outcome: PaymentOutcome,
    val description: String?,
    val paid: Boolean,
    val paymentDate: LocalDate?,
    val paidAmount: Double?,
    val receiptPrintable: Boolean,
)

enum class PaymentOutcome { Completed, Pending, Failed, Unknown }
