package it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.state

import java.time.LocalDate

/**
 * Presentation model for the [Invoice] ticket: the header status (paid date or unpaid),
 * the notice/payment-mode info rows and the per-item tax breakdown.
 */
data class InvoiceData(
    val id: String,
    val invoiceNumber: String,
    val description: String,
    val expiryDate: LocalDate,
    val amount: Double,
    val modalita: String,
    val bulletinCode: String? = null,
    val items: List<InvoiceItem> = emptyList(),
    val paymentDate: LocalDate? = null,
)

/** One tax line on the [Invoice] ticket: its year, installment, description and amount. */
data class InvoiceItem(
    val year: String,
    val installment: String,
    val description: String,
    val amount: Double,
)
