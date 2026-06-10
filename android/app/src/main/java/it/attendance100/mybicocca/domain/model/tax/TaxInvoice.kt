package it.attendance100.mybicocca.domain.model.tax

import java.time.LocalDate

/**
 * A tuition/fee invoice ("fattura") on the student's account, sourced live from the Esse3
 * invoice list and enriched with its charge breakdown. Listed on the registry "Tasse"
 * sub-screen, which also drives pagoPA payments and PDF downloads from it.
 *
 * @property id Esse3 invoice identifier.
 * @property academicYear Starting solar year of the academic year the invoice belongs to.
 * @property title Human-readable invoice title, ready for display.
 * @property amount Total payable amount in euro.
 * @property paidAmount Amount already paid in euro, when any.
 * @property status Payment lifecycle state.
 * @property issueDate Date the invoice was issued.
 * @property expiration Payment due date.
 * @property paymentDate Date the invoice was paid, when paid.
 * @property pagoPaEnabled Whether the invoice can be paid through pagoPA.
 * @property pagoPaImmediate Whether an immediate online pagoPA payment can be started.
 * @property pagoPaNotice Whether a pagoPA payment notice ("avviso") PDF is printable.
 * @property iuv pagoPA unique payment identifier (IUV).
 * @property noticeCode pagoPA notice code ("codice avviso").
 * @property items Charge breakdown ("voci") joined from the student-charges endpoint.
 */
data class TaxInvoice(
    val id: InvoiceId,
    val academicYear: Int?,
    val title: String,
    val amount: Double,
    val paidAmount: Double?,
    val status: TaxStatus,
    val issueDate: LocalDate?,
    val expiration: LocalDate?,
    val paymentDate: LocalDate?,
    val pagoPaEnabled: Boolean,
    val pagoPaImmediate: Boolean,
    val pagoPaNotice: Boolean,
    val iuv: String?,
    val noticeCode: String?,
    val items: List<TaxChargeItem>,
)
